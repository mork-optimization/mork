#!/usr/bin/env python3
"""Compare C and Java GVNS with corrected and original C repair classification."""
import argparse
from concurrent.futures import ThreadPoolExecutor
import datetime
import difflib
import hashlib
import json
import math
import os
from pathlib import Path
import platform
import random
import re
import shutil
import statistics
import subprocess
import time
import zipfile
import csv

ROOT = Path(__file__).resolve().parents[1]
C_SOURCE = ROOT / "src/c/beam-aco/trunk"
IMPLEMENTATIONS = ("c-original", "c-fixed", "java", "java-c-bug")


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, value):
    temporary = path.with_suffix(path.suffix + ".tmp")
    temporary.write_text(json.dumps(value, indent=2) + "\n")
    temporary.replace(path)


def fixed_source(source):
    start = source.index("void\nSolution::compute_feas_set(")
    end = source.index("bool\nSolution::feasibility_1shift_first_code(", start)
    repair = source[start:end]
    if repair.count("window_end[i]") != 6:
        raise ValueError("C repair source changed: expected two classifiers and four assertions")
    repair = repair.replace("window_end[i]", "window_end[permutation[i]]")
    # Correct classification can leave a phase with no customers to move.
    # Avoid the original do/while calling back() on an empty vector.
    for function, values in (("backward_violated", "infeas"), ("forward_nonviolated", "feas"),
                             ("forward_violated", "infeas"), ("backward_nonviolated", "feas")):
        begin = repair.index("Solution::" + function + "(")
        position = repair.index("    Solution sol;", begin)
        repair = repair[:position] + f"    if ({values}.empty()) return false;\n\n" + repair[position:]
    return source[:start] + repair + source[end:]


def build_native(output, compiler):
    build = output / "build"
    build.mkdir()
    source = (C_SOURCE / "tsptw_solution.cpp").read_text()
    corrected = fixed_source(source)
    (build / "repair-fix.patch").write_text("".join(difflib.unified_diff(
        source.splitlines(True), corrected.splitlines(True),
        fromfile="original/tsptw_solution.cpp", tofile="fixed/tsptw_solution.cpp")))
    commands = []
    for implementation in IMPLEMENTATIONS[:2]:
        directory = build / implementation
        directory.mkdir()
        for path in C_SOURCE.iterdir():
            if path.suffix in (".h", ".cc", ".cpp"):
                shutil.copy2(path, directory / path.name)
        (directory / "tsptw_solution.cpp").write_text(source if implementation == "c-original" else corrected)
        main = (directory / "gvns.cpp").read_text()
        if platform.system() == "Darwin":
            main = main.replace("extern char * program_invocation_short_name;",
                                'static const char * program_invocation_short_name = "gvns_tsptw";')
        # Identical reporting-only adjustment in both variants; preserve the original timer.
        main = main.replace("Time = %.1f)", "Time = %.9f)")
        (directory / "gvns.cpp").write_text(main)
        executable = directory / "gvns_tsptw"
        command = [compiler, "-std=c++14", "-O3", "-DNDEBUG", "-DDEBUG=0", "-DMINIMISE_TOURCOST",
                   "-DNUMBER_TYPE_IS=1", "-I", str(ROOT / "src/c/libmisc/current"),
                   *[str(directory / name) for name in ("gvns.cpp", "tsptw_solution.cpp", "Random.cc", "Timer.cc")],
                   "-o", str(executable)]
        with (build / (implementation + ".log")).open("w") as log:
            subprocess.run(command, stdout=log, stderr=subprocess.STDOUT, check=True)
        commands.append(command)
    return commands


def read_instance(path):
    tokens = path.read_text().split()
    n = int(tokens[0])
    values = list(map(float, tokens[1:]))
    if n < 4 or len(values) != n * n + 2 * n or any(not math.isfinite(v) or not v.is_integer() for v in values):
        raise ValueError("This experiment requires an integer matrix and integer time windows")
    distances = [values[i * n:(i + 1) * n] for i in range(n)]
    windows = values[n * n:]
    if any(distances[i][j] < 0 or distances[i][j] != distances[j][i] for i in range(n) for j in range(n)):
        raise ValueError("GVNS requires nonnegative symmetric distances")
    return distances, windows[::2], windows[1::2]


def validate(row, instance):
    distances, starts, ends = instance
    n = len(distances)
    tour = row["tour"]
    if len(tour) != n + 1 or tour[0] != 0 or tour[-1] != 0 or sorted(tour[:-1]) != list(range(n)):
        raise ValueError("Invalid permutation")
    cost = arrival = infeasibility = 0
    violations = 0
    for previous, node in zip(tour, tour[1:]):
        cost += distances[previous][node]
        arrival = max(arrival + distances[previous][node], starts[node])
        if arrival > ends[node]:
            violations += 1
            infeasibility += arrival - ends[node]
    if cost != row["cost"] or violations != row["violations"] or infeasibility != row["infeasibility"]:
        raise ValueError("Reported cost or feasibility differs from independent tour evaluation")
    if violations:
        raise ValueError(f"Tour violates {violations} time windows")
    return row


def parse_native(stdout, implementation, seed, seconds, wall):
    lines = stdout.splitlines()
    result = next(line for line in reversed(lines) if line.strip() and not line.startswith("#"))
    fields = result.split()
    end = re.search(r"Evaluations = (\d+), Iterations = (\d+), Time = ([\d.]+)", stdout)
    if end is None or float(fields[0]) != float(fields[3]):
        raise ValueError("Invalid C output or binary does not minimize tour cost")
    return {"implementation": implementation, "seed": seed, "budgetSeconds": seconds,
            "cost": float(fields[3]), "violations": int(fields[4]), "infeasibility": float(fields[5]),
            "wallSeconds": wall, "cpuSeconds": float(end[3]), "iterations": int(end[2]),
            "evaluations": int(end[1]), "tour": [0] + list(map(int, fields[6:])) + [0]}


def run_native(args, implementation, instance_path, instance):
    directory = args.output / implementation
    directory.mkdir()
    executable = args.output / "build" / implementation / "gvns_tsptw"
    for i in range(args.repetitions):
        seed = args.first_seed + i
        command = [str(executable), "-i", str(instance_path), "-t", str(args.seconds), "-r", "1",
                   "-s", str(seed), "-T", str(directory / f"{seed}.trace")]
        start = time.monotonic()
        with (directory / f"{seed}.stdout").open("w") as stdout, (directory / f"{seed}.stderr").open("w") as stderr:
            subprocess.run(command, stdout=stdout, stderr=stderr, check=True, timeout=max(120, args.seconds * 10))
        wall = time.monotonic() - start
        row = parse_native((directory / f"{seed}.stdout").read_text(), implementation, seed, args.seconds, wall)
        validate(row, instance)
        write_json(directory / f"{seed}.json", row)
        print(f"{implementation} {i + 1}/{args.repetitions} seed={seed} cost={row['cost']:.0f}", flush=True)


def run_java(args, instance_path, instance, implementation="java"):
    directory = args.output / implementation
    directory.mkdir()
    command = ["java", "-Xms256m", "-Xmx1g", "-jar", str(args.jar), "repair-comparison-java",
               str(instance_path), str(args.repetitions), str(args.seconds), str(args.first_seed),
               str(directory / "runs.jsonl")]
    if implementation != "java":
        command.append(implementation)
    with (directory / "invocation.log").open("w") as log:
        log.write(json.dumps(command) + "\n")
        log.flush()
        subprocess.run(command, cwd=ROOT, stdout=log, stderr=subprocess.STDOUT, check=True)
    for line in (directory / "runs.jsonl").read_text().splitlines():
        row = validate(json.loads(line), instance)
        if implementation == "java-long" and row.get("arithmetic") != "long":
            raise ValueError("Java long run did not report long arithmetic")
        write_json(directory / f"{row['seed']}.json", row)
    print(f"{implementation} {args.repetitions}/{args.repetitions} completed", flush=True)


def paired_costs(baseline, candidate):
    baseline_costs = {row["seed"]: row["cost"] for row in baseline}
    differences = [row["cost"] - baseline_costs[row["seed"]] for row in candidate if row["seed"] in baseline_costs]
    if not differences:
        return None
    rng = random.Random(81251)
    bootstrap = sorted(statistics.mean(rng.choices(differences, k=len(differences))) for _ in range(5000))
    wins, losses = sum(d < 0 for d in differences), sum(d > 0 for d in differences)
    non_ties = wins + losses
    sign_p = min(1.0, 2 * sum(math.comb(non_ties, k) for k in range(min(wins, losses) + 1)) / 2 ** non_ties)
    return {"pairs": len(differences), "candidateWins": wins, "ties": differences.count(0), "candidateLosses": losses,
            "meanCandidateMinusBaseline": statistics.mean(differences),
            "bootstrap95PercentInterval": [bootstrap[125], bootstrap[4874]], "twoSidedSignTestP": sign_p}


def report(output):
    manifest = json.loads((output / "manifest.json").read_text())
    settings = manifest["settings"]
    instance_path = output / settings["instance"]
    if digest(instance_path) != settings["instanceHash"]:
        raise ValueError("Instance hash differs from manifest")
    instance = read_instance(instance_path)
    implementations = settings.get("implementations", IMPLEMENTATIONS[:3])
    rows = []
    groups = {}
    for implementation in implementations:
        group = []
        directory = output / implementation
        paths = list(directory.glob("*.json"))
        if implementation.startswith("java") and not paths and (directory / "runs.jsonl").exists():
            pending = [json.loads(line) for line in (directory / "runs.jsonl").read_text().splitlines()]
        else:
            pending = [json.loads(path.read_text()) for path in paths]
        seen = set()
        for row in pending:
            if (row["implementation"] != implementation or row["budgetSeconds"] != settings["seconds"]
                    or not settings["firstSeed"] <= row["seed"] < settings["firstSeed"] + settings["repetitions"]
                    or row["seed"] in seen):
                raise ValueError("Duplicate seed or mismatched run settings")
            seen.add(row["seed"])
            group.append(validate(row, instance))
        group.sort(key=lambda row: row["seed"])
        groups[implementation] = group
        rows.extend(group)
    columns = ["implementation", "seed", "cost", "violations", "infeasibility", "wallSeconds", "cpuSeconds"]
    with (output / "results.csv").open("w") as writer:
        csv_writer = csv.DictWriter(writer, fieldnames=columns, extrasaction="ignore")
        csv_writer.writeheader()
        csv_writer.writerows(rows)
    target = settings["target"]
    summary = {}
    for name, group in groups.items():
        if not group:
            continue
        costs = [r["cost"] for r in group]
        summary[name] = {"runs": len(group), "best": min(costs), "mean": statistics.mean(costs),
                         "median": statistics.median(costs), "sampleStdDev": statistics.stdev(costs) if len(costs) > 1 else 0,
                         "worst": max(costs), "targetHits": sum(c <= target for c in costs),
                         "meanWallSeconds": statistics.mean(r["wallSeconds"] for r in group),
                         "meanCpuSeconds": statistics.mean(r["cpuSeconds"] for r in group if r["cpuSeconds"] is not None)}
    paired = paired_costs(groups["c-original"], groups["c-fixed"])
    paired_java = paired_costs(groups["java"], groups.get("java-c-bug", []))
    paired_long = paired_costs(groups["java"], groups.get("java-long", []))
    complete = all(len(groups[name]) == settings["repetitions"] for name in implementations)
    write_json(output / "summary.json", {"complete": complete, "implementations": summary,
                                        "pairedCComparison": paired, "pairedJavaComparison": paired_java,
                                        "pairedLongComparison": paired_long})
    lines = ["# C and Java GVNS comparison", "", f"Instance: `{settings['instance']}`; target: {target:g}.",
             f"Budget: {settings['seconds']:g} seconds; requested runs per implementation: {settings['repetitions']}.",
             f"Status: {'complete' if complete else 'partial'}; every included tour independently validated.", "",
             "| Implementation | Runs | Best | Mean | Median | Sample SD | Worst | Target hits | Mean wall s | Mean CPU s |",
             "|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|"]
    for name, s in summary.items():
        lines.append(f"| {name} | {s['runs']} | {s['best']:g} | {s['mean']:.3f} | {s['median']:g} | "
                     f"{s['sampleStdDev']:.3f} | {s['worst']:g} | {s['targetHits']} | "
                     f"{s['meanWallSeconds']:.3f} | {s['meanCpuSeconds']:.3f} |")
    if paired:
        lines.extend(["", "Matched C seeds (negative cost difference favors fixed C):", "", "```json",
                      json.dumps(paired, indent=2), "```"])
    if paired_java:
        lines.extend(["", "Matched Java seeds (candidate is java-c-bug; negative difference favors the bug variant):",
                      "", "```json", json.dumps(paired_java, indent=2), "```"])
    if paired_long:
        lines.extend(["", "Matched Java seeds (candidate is java-long with corrected repair; negative difference favors long arithmetic):",
                      "", "```json", json.dumps(paired_long, indent=2), "```"])
    lines.extend(["", "Original C and fixed C use identical compiler flags, seeds, CPU-time limits, and search settings.",
                  "Fixed C changes deadline indexing in the two repair sets and four assertions, and guards empty repair sets.",
                  "Java uses its existing RNG and wall-time stopping checks; metrics are disabled and five unmeasured one-second warm-ups precede its runs.",
                  "The java-c-bug variant deliberately uses arrival[position] > deadline[position] in repair; actual feasibility and cost checks remain customer-indexed.",
                  "The java-long variant uses long distance, cost, delta, arrival, and lateness calculations with corrected repair. Mork's score API converts the final long to double.",
                  "CPU seconds mean process CPU time for C and current-thread CPU time for Java; they exclude different runtime overheads.",
                  f"At most {settings['jobs']} arms run concurrently, with one search at a time per implementation.",
                  "The original C repair bug can affect solution quality in either direction. This comparison does not align Java's RNG or stopping behavior.", ""])
    if manifest.get("additionalVariants"):
        lines.extend(["Appended variants: " + ", ".join(manifest["additionalVariants"]) + ". Each used a separate execution with the same seeds, budget, warm-up, JVM flags, and metrics settings.",
                      "The original three arms and their recorded data were retained; their run used three concurrent arms, while each appended arm ran alone.", ""])
    (output / "report.md").write_text("\n".join(lines))
    print(json.dumps({"complete": complete, **summary, "pairedCComparison": paired,
                      "pairedJavaComparison": paired_java, "pairedLongComparison": paired_long}, indent=2))


def append_java_variant(args):
    args.output = args.output.resolve()
    args.jar = args.jar.resolve()
    manifest_path = args.output / "manifest.json"
    manifest = json.loads(manifest_path.read_text())
    settings = manifest["settings"]
    implementation = args.implementation
    if (args.output / implementation).exists():
        raise ValueError(implementation + " already exists; use report to inspect its saved results")
    long_build = None
    if implementation == "java-long":
        long_build = json.loads((args.jar.parent / "manifest.json").read_text())
        if long_build["longHash"] != digest(args.jar):
            raise ValueError("Long artifact differs from its build manifest")
        with zipfile.ZipFile(args.jar) as archive:
            if json.loads(archive.read("META-INF/tsptw-arithmetic.json"))["kind"] != "long":
                raise ValueError("Artifact does not declare long arithmetic")
        if not (args.jar.parent / "validation/summary.json").is_file():
            raise ValueError("Verify the long artifact before measuring it")
    original_summary = json.loads((args.output / "summary.json").read_text())
    if not original_summary["complete"]:
        raise ValueError("Complete the original comparison before appending this variant")
    instance_path = args.output / settings["instance"]
    if digest(instance_path) != settings["instanceHash"]:
        raise ValueError("Instance hash differs from manifest")
    instance = read_instance(instance_path)
    args.repetitions, args.seconds, args.first_seed = settings["repetitions"], settings["seconds"], settings["firstSeed"]
    archive = args.output / ("before-" + implementation)
    archive.mkdir()
    for name in ("manifest.json", "summary.json", "report.md", "results.csv", "findings.md", "cost-distribution.svg", "cost-distribution.png"):
        path = args.output / name
        if path.exists():
            shutil.copy2(path, archive / name)
    artifacts = args.output / "artifacts"
    artifacts.mkdir(exist_ok=True)
    artifact = artifacts / (implementation + ".jar")
    build_directory = args.jar.parent
    shutil.copy2(args.jar, artifact)
    args.jar = artifact
    metadata = {
        "createdUtc": datetime.datetime.now(datetime.timezone.utc).isoformat(),
        "javaJar": str(artifact), "javaJarHash": digest(artifact), "concurrentArms": 1,
        "java": subprocess.run(["java", "-version"], text=True, capture_output=True, check=True).stderr,
        "sourceHashes": {str(p.relative_to(ROOT)): digest(p) for p in sorted((ROOT / "src/main/java").rglob("*.java"))}}
    if long_build:
        metadata["sourceHashes"] = long_build["sourceHashes"]
        metadata["sourceSnapshot"] = str(build_directory / "source/src/main/java")
        metadata["longMathBuildManifest"] = str(build_directory / "manifest.json")
        metadata["arithmetic"] = long_build["arithmetic"]
    manifest.setdefault("additionalVariants", {})[implementation] = metadata
    settings["implementations"] = list(settings.get("implementations", IMPLEMENTATIONS[:3])) + [implementation]
    write_json(manifest_path, manifest)
    try:
        run_java(args, instance_path, instance, implementation)
    except Exception as error:
        write_json(args.output / (implementation + "-failure.json"), {"error": str(error)})
        raise
    report(args.output)


def run(args):
    args.output = args.output.resolve()
    args.jar = args.jar.resolve()
    if (args.repetitions < 1 or not math.isfinite(args.seconds) or args.seconds <= 0 or args.first_seed < 1
            or args.first_seed + args.repetitions - 1 >= 2147483647 or args.jobs not in (1, 2, 3, 4)):
        raise ValueError("Invalid repetitions, seconds, seed range, or job count")
    if not args.jar.is_file():
        raise ValueError("Build example-tsptw first or supply --jar")
    args.output.mkdir(parents=True, exist_ok=False)
    with zipfile.ZipFile(args.archive) as archive:
        content = archive.read(args.instance)
    instance_path = args.output / Path(args.instance).name
    instance_path.write_bytes(content)
    instance = read_instance(instance_path)
    commands = build_native(args.output, args.compiler)
    settings = {"instance": instance_path.name, "instanceHash": digest(instance_path), "seconds": args.seconds,
                "repetitions": args.repetitions, "firstSeed": args.first_seed, "target": args.target, "jobs": args.jobs,
                "implementations": list(IMPLEMENTATIONS)}
    source_hashes = {str(p.relative_to(ROOT)): digest(p) for p in sorted((ROOT / "src").rglob("*"))
                     if p.is_file() and p.suffix in (".java", ".cpp", ".cc", ".h", ".hpp")}
    manifest = {"settings": settings, "createdUtc": datetime.datetime.now(datetime.timezone.utc).isoformat(),
                "platform": platform.platform(), "logicalCpus": os.cpu_count(),
                "compiler": subprocess.run([args.compiler, "--version"], text=True, capture_output=True, check=True).stdout,
                "java": subprocess.run(["java", "-version"], text=True, capture_output=True, check=True).stderr,
                "compileCommands": commands, "javaJar": str(args.jar), "javaJarHash": digest(args.jar),
                "nativeHashes": {name: digest(args.output / "build" / name / "gvns_tsptw") for name in IMPLEMENTATIONS[:2]},
                "sourceHashes": source_hashes}
    write_json(args.output / "manifest.json", manifest)
    failures = []
    with ThreadPoolExecutor(max_workers=args.jobs) as executor:
        futures = {name: executor.submit(run_java, args, instance_path, instance, name) if name.startswith("java")
                   else executor.submit(run_native, args, name, instance_path, instance) for name in IMPLEMENTATIONS}
        for name, future in futures.items():
            try:
                future.result()
            except Exception as error:
                failures.append({"implementation": name, "error": str(error)})
                print(f"FAILED {name}: {error}", flush=True)
    write_json(args.output / "failures.json", failures)
    report(args.output)
    if failures:
        raise ValueError("Comparison incomplete; see failures.json and raw logs")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    campaign = commands.add_parser("run")
    campaign.add_argument("--output", type=Path, required=True, help="New directory for this experiment")
    campaign.add_argument("--archive", type=Path, default=ROOT / "instances/GendreauDumasExtended.zip")
    campaign.add_argument("--instance", default="n80w200.001.txt")
    campaign.add_argument("--jar", type=Path, default=ROOT / "target/tsptw-0.23-SNAPSHOT.jar")
    campaign.add_argument("--compiler", default="c++")
    campaign.add_argument("--repetitions", type=int, default=100)
    campaign.add_argument("--seconds", type=float, default=10)
    campaign.add_argument("--first-seed", type=int, default=1234)
    campaign.add_argument("--target", type=float, default=490)
    campaign.add_argument("--jobs", type=int, default=4 if (os.cpu_count() or 1) >= 6 else 1)
    campaign.set_defaults(action=run)
    summary = commands.add_parser("report")
    summary.add_argument("output", type=Path)
    summary.set_defaults(action=lambda args: report(args.output))
    for implementation in ("java-c-bug", "java-long"):
        append = commands.add_parser("append-" + implementation, help="Append a measured Java variant to an existing comparison")
        append.add_argument("--output", type=Path, required=True)
        append.add_argument("--jar", type=Path, default=ROOT / "target/tsptw-0.23-SNAPSHOT.jar")
        append.set_defaults(action=append_java_variant, implementation=implementation)
    args = parser.parse_args()
    try:
        args.action(args)
    except (ValueError, OSError, subprocess.CalledProcessError) as error:
        parser.exit(1, str(error) + "\n")


if __name__ == "__main__":
    main()
