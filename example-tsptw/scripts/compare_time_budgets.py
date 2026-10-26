#!/usr/bin/env python3
"""Run archived Java GVNS in parallel JVMs and compare with recorded 10-second costs."""
import argparse
from concurrent.futures import ThreadPoolExecutor, as_completed
import csv
import datetime
import hashlib
import json
import math
from pathlib import Path
import random
import re
import shutil
import statistics
import subprocess
import time

from cross_validate_tours import evaluate, instance_data

ROOT = Path(__file__).resolve().parents[1]


def write_json(path, value):
    path.write_text(json.dumps(value, indent=2) + "\n")


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def run_worker(worker, instance, expected):
    directory = Path(worker["directory"])
    directory.mkdir()
    started = datetime.datetime.now(datetime.timezone.utc).isoformat()
    start = time.monotonic()
    with (directory / "invocation.log").open("w") as log:
        log.write(json.dumps(worker["command"]) + "\n")
        log.flush()
        process = subprocess.Popen(worker["command"], stdout=log, stderr=subprocess.STDOUT)
        write_json(directory / "process.json", {"pid": process.pid, "startedUtc": started})
        returncode = process.wait()
    write_json(directory / "execution.json", {"returncode": returncode, "startedUtc": started,
               "finishedUtc": datetime.datetime.now(datetime.timezone.utc).isoformat(),
               "elapsedSeconds": time.monotonic() - start})
    if returncode:
        raise RuntimeError(f"Worker {worker['worker']} failed with status {returncode}; see {directory}")
    rows = [json.loads(line) for line in (directory / "runs.jsonl").read_text().splitlines()]
    seeds = set(range(worker["firstSeed"], worker["firstSeed"] + worker["repetitions"]))
    if len(rows) != len(seeds) or {row["seed"] for row in rows} != seeds:
        raise ValueError("Missing or duplicate worker seeds")
    for row in rows:
        if row["budgetSeconds"] != worker["budgetSeconds"] or row["implementation"] != "java":
            raise ValueError("Worker reported unexpected experiment settings")
        actual = evaluate(row["tour"], instance)
        if not actual["feasible"]:
            raise ValueError(f"Infeasible tour for seed {row['seed']}")
        for field in ("cost", "violations", "infeasibility"):
            if actual[field] != row[field]:
                raise ValueError(f"Incorrect {field} for seed {row['seed']}")
        if row["seed"] not in expected:
            raise ValueError("Missing baseline seed")
        row["worker"] = worker["worker"]
    return rows


def summarize(rows, baseline):
    differences = [row["cost"] - baseline[row["seed"]] for row in rows]
    wins, losses = sum(d < 0 for d in differences), sum(d > 0 for d in differences)
    non_ties = wins + losses
    rng = random.Random(81251)
    bootstrap = sorted(statistics.mean(rng.choices(differences, k=len(differences))) for _ in range(5000))
    return {"runs": len(rows), "baselineBudgetSeconds": 10, "candidateBudgetSeconds": rows[0]["budgetSeconds"],
            "baselineMean": statistics.mean(baseline[row["seed"]] for row in rows),
            "candidateMean": statistics.mean(row["cost"] for row in rows),
            "baselineTargetHits": sum(baseline[row["seed"]] == 490 for row in rows),
            "candidateTargetHits": sum(row["cost"] == 490 for row in rows),
            "candidateBest": min(row["cost"] for row in rows), "candidateWorst": max(row["cost"] for row in rows),
            "wins": wins, "ties": differences.count(0), "losses": losses,
            "meanCandidateMinusBaseline": statistics.mean(differences),
            "bootstrap95PercentInterval": [bootstrap[125], bootstrap[4874]],
            "twoSidedSignTestP": min(1., 2 * sum(math.comb(non_ties, k) for k in range(min(wins, losses) + 1)) / 2 ** non_ties),
            "meanWallSeconds": statistics.mean(row["wallSeconds"] for row in rows),
            "meanCpuSeconds": statistics.mean(row["cpuSeconds"] for row in rows),
            "minCpuSeconds": min(row["cpuSeconds"] for row in rows),
            "maxCpuSeconds": max(row["cpuSeconds"] for row in rows),
            "improvedAfter10Seconds": sum(row["cost"] < baseline[row["seed"]] and row["timeToBestSeconds"] > 10 for row in rows),
            "independentlyValidatedTours": len(rows)}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--instance", required=True, type=Path)
    parser.add_argument("--jar", required=True, type=Path)
    parser.add_argument("--baseline-log", required=True, type=Path)
    parser.add_argument("--repetitions", type=int, default=100)
    parser.add_argument("--workers", type=int, default=4)
    parser.add_argument("--seconds", type=int, default=30)
    parser.add_argument("--first-seed", type=int, default=1234)
    args = parser.parse_args()
    if args.workers < 1 or args.repetitions < args.workers or args.seconds < 1:
        parser.error("Require positive workers and budget, with at least one repetition per worker")
    output = args.output.resolve()
    baseline = {int(seed): float(cost) for seed, cost in re.findall(
        r"java \d+/100 seed=(\d+) cost=(\d+)", args.baseline_log.read_text())}
    if not set(range(args.first_seed, args.first_seed + args.repetitions)) <= baseline.keys():
        parser.error("Baseline log does not cover all requested seeds")
    output.mkdir(parents=True, exist_ok=False)
    jar = output / "java-original.jar"
    instance = output / args.instance.name
    shutil.copy2(args.jar, jar)
    shutil.copy2(args.instance, instance)
    shutil.copy2(args.baseline_log, output / "java-10s-original-invocation.log")
    write_json(output / "baseline-costs.json", baseline)
    workers = []
    first = args.first_seed
    for index in range(args.workers):
        count = args.repetitions // args.workers + (index < args.repetitions % args.workers)
        directory = output / f"worker-{index + 1}"
        command = ["java", "-Xms256m", "-Xmx1g", "-jar", str(jar), "repair-comparison-java",
                   str(instance), str(count), str(args.seconds), str(first), str(directory / "runs.jsonl")]
        workers.append({"worker": index + 1, "directory": str(directory), "firstSeed": first,
                        "repetitions": count, "budgetSeconds": args.seconds,
                        "warmupSeeds": list(range(first + count, first + count + 5)), "command": command})
        first += count
    manifest = {"createdUtc": datetime.datetime.now(datetime.timezone.utc).isoformat(),
                "instance": instance.name, "instanceHash": digest(instance), "javaJarHash": digest(jar),
                "archivedJavaJar": str(args.jar.resolve()), "baselineLog": str(args.baseline_log.resolve()),
                "repetitions": args.repetitions, "workers": workers, "maxConcurrentSearches": args.workers,
                "cpuAffinityPinned": False, "budgetSeconds": args.seconds, "budgetClock": "wall",
                "metricsEnabled": False, "arithmetic": "double", "repairClassification": "corrected",
                "permutationStorage": "ArrayList<Integer>", "warmupRunsPerWorker": 5, "warmupSeconds": 1,
                "baselineWarmupSeeds": list(range(1334, 1339)), "baselineToursAvailable": False,
                "javaVersion": subprocess.run(["java", "-version"], capture_output=True, text=True, check=True).stderr}
    write_json(output / "manifest.json", manifest)
    data = instance_data(instance)
    rows = []
    started = time.monotonic()
    with ThreadPoolExecutor(max_workers=args.workers) as executor:
        futures = [executor.submit(run_worker, worker, data, baseline) for worker in workers]
        for future in as_completed(futures):
            group = future.result()
            rows.extend(group)
            print(f"Worker {group[0]['worker']} finished: {len(group)} tours validated", flush=True)
    rows.sort(key=lambda row: row["seed"])
    (output / "runs.jsonl").write_text("".join(json.dumps(row) + "\n" for row in rows))
    summary = summarize(rows, baseline)
    summary["elapsedSeconds"] = time.monotonic() - started
    summary["parallelWorkers"] = args.workers
    write_json(output / "summary.json", summary)
    with (output / "paired-results.csv").open("w") as stream:
        fields = ["seed", "worker", "cost10s", "cost30s", "difference", "wallSeconds", "cpuSeconds", "timeToBestSeconds"]
        writer = csv.DictWriter(stream, fieldnames=fields)
        writer.writeheader()
        for row in rows:
            writer.writerow({"seed": row["seed"], "worker": row["worker"], "cost10s": baseline[row["seed"]],
                "cost30s": row["cost"], "difference": row["cost"] - baseline[row["seed"]],
                **{field: row[field] for field in ("wallSeconds", "cpuSeconds", "timeToBestSeconds")}})
    lines = ["# Java 30-second comparison: 100 runs with four parallel workers", "",
             f"Instance: `{instance.name}`; seeds {args.first_seed}–{args.first_seed + args.repetitions - 1}.",
             "The archived original Java executable was reused without rebuilding. Metrics are disabled; arithmetic is double, repair is corrected, and tours use the original ArrayList storage.", "",
             "| Java wall-time budget | Runs | Mean cost | Cost-490 hits |", "|---|---:|---:|---:|",
             f"| 10 seconds, recorded baseline | {len(rows)} | {summary['baselineMean']:.2f} | {summary['baselineTargetHits']}/{len(rows)} |",
             f"| {args.seconds} seconds, four workers | {len(rows)} | {summary['candidateMean']:.2f} | {summary['candidateTargetHits']}/{len(rows)} |", "",
             f"Matched seeds: {summary['wins']} wins, {summary['ties']} ties, {summary['losses']} losses. Mean candidate-minus-baseline difference: {summary['meanCandidateMinusBaseline']:.2f}; paired bootstrap 95% interval: {summary['bootstrap95PercentInterval']}; two-sided sign-test p-value: {summary['twoSidedSignTestP']:.6g}.", "",
             f"Mean measured wall time: {summary['meanWallSeconds']:.6f} seconds. Mean current-thread CPU time: {summary['meanCpuSeconds']:.3f} seconds; range {summary['minCpuSeconds']:.3f}–{summary['maxCpuSeconds']:.3f}. Total experiment elapsed time: {summary['elapsedSeconds']:.1f} seconds.", "",
             "Four JVMs each execute 25 single-threaded searches sequentially. This limits simultaneous searches to four; the operating system schedules them without CPU affinity pinning. JVM flags remain -Xms256m -Xmx1g. Each JVM performs five unmeasured one-second warm-ups, with exact seeds recorded in manifest.json. Warm-up seeds and the multi-JVM execution context differ from the earlier one-JVM baseline. Other processes can affect wall-time budgets; measured current-thread CPU time is retained for each run.", "",
             "Each saved tour passed the archived Java validator and an independent Python recomputation of cost, permutation completeness, time windows, depot return, and lateness. The earlier tour files are unavailable, so the baseline comparison uses costs recovered from its retained invocation log. Earlier experiments and their results were not overwritten.", ""]
    (output / "report.md").write_text("\n".join(lines))
    print(json.dumps(summary, indent=2), flush=True)


if __name__ == "__main__":
    main()
