#!/usr/bin/env python3
"""Generate replacement tours and check them with the archived C and Java implementations."""
import argparse
from concurrent.futures import ThreadPoolExecutor
import importlib.util
import json
import os
from pathlib import Path
import random
import subprocess
from types import SimpleNamespace
import zipfile

ROOT = Path(__file__).resolve().parents[1]
VARIANTS = ("c-original", "c-fixed", "java", "java-c-bug", "java-long", "java-arrays")


def write_json(path, data):
    path.write_text(json.dumps(data, indent=2) + "\n")


def archived_jars():
    case = ROOT / "repair-comparisons/2026-10-25-n80w200.001-100x10s/artifacts"
    return {"java": case / "java-original.jar", "java-c-bug": case / "java-c-bug.jar",
            "java-arrays": case / "java-arrays.jar",
            "java-long": ROOT / "repair-comparisons/2026-10-25-java-long/java-long.jar"}


def generate(experiment):
    output = experiment / "cross-validation"
    spec = importlib.util.spec_from_file_location("historical", output / "historical_harness.py")
    harness = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(harness)
    harness.ROOT = ROOT
    harness.C_SOURCE = ROOT / "src/c/beam-aco/trunk"
    instance = experiment / "n80w200.001.txt"
    data = harness.read_instance(instance)
    with ThreadPoolExecutor(max_workers=5) as executor:
        futures = []
        for variant in VARIANTS:
            if variant == "java":
                continue  # The quick experiment already supplies original Java's 30-second tours.
            args = SimpleNamespace(output=output, jar=archived_jars().get(variant),
                                   repetitions=10, seconds=10, first_seed=1234)
            if variant.startswith("c-"):
                futures.append(executor.submit(harness.run_native, args, variant, instance, data))
            else:
                futures.append(executor.submit(harness.run_java, args, instance, data, variant))
        for future in futures:
            future.result()


def instance_data(path):
    fields = path.read_text().split()
    n = int(fields[0])
    values = list(map(float, fields[1:]))
    assert len(values) == n * n + 2 * n
    return ([values[i * n:(i + 1) * n] for i in range(n)],
            values[n * n::2], values[n * n + 1::2])


def evaluate(tour, data):
    distances, starts, ends = data
    n = len(starts)
    valid = len(tour) == n + 1 and tour[0] == tour[-1] == 0 and sorted(tour[:-1]) == list(range(n))
    result = {"validPermutation": valid, "feasible": False}
    if not valid:
        return result
    cost = arrival = lateness = 0
    violations = 0
    arrivals = [0]
    for previous, node in zip(tour, tour[1:]):
        cost += distances[previous][node]
        arrival = max(arrival + distances[previous][node], starts[node])
        arrivals.append(arrival)
        if arrival > ends[node]:
            violations += 1
            lateness += arrival - ends[node]
    result.update(cost=cost, makespan=arrival, violations=violations, infeasibility=lateness,
                  arrivals=arrivals, feasible=violations == 0)
    return result


def java_command(variant, output):
    runtime = output / ("java-runtime" if variant == "java" else variant + "-runtime")
    if not runtime.exists():
        runtime.mkdir()
        with zipfile.ZipFile(archived_jars()[variant]) as archive:
            for name in archive.namelist():
                if name.endswith("/"):
                    continue
                if name.startswith("BOOT-INF/classes/"):
                    target = runtime / "classes" / name.removeprefix("BOOT-INF/classes/")
                elif name.startswith("BOOT-INF/lib/"):
                    target = runtime / "lib" / Path(name).name
                else:
                    continue
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes(archive.read(name))
        classpath = os.pathsep.join(map(str, [runtime / "classes", runtime / "lib/*"]))
        subprocess.run(["javac", "--release", "21", "-cp", classpath, "-d", str(runtime / "probe"),
                        str(ROOT / "scripts/cross-validation/CrossCheck.java")], check=True)
    classpath = os.pathsep.join(map(str, [runtime / "probe", runtime / "classes", runtime / "lib/*"]))
    return ["java", "-Xmx256m", "-cp", classpath, "es.urjc.etsii.grafo.tsptw.model.CrossCheck"]


def validate(experiment):
    output = experiment / "cross-validation"
    tours = []
    for variant in VARIANTS:
        if variant == "java":
            rows = [json.loads(line) for line in (experiment / "java-30s.jsonl").read_text().splitlines()]
        elif variant.startswith("java"):
            rows = [json.loads(line) for line in (output / variant / "runs.jsonl").read_text().splitlines()]
        else:
            rows = [json.loads(path.read_text()) for path in sorted((output / variant).glob("*.json"))]
        assert len(rows) == 10
        for row in rows:
            tours.append({"id": variant.replace("-", "_") + "_" + str(row["seed"]), "origin": variant,
                          "tour": row["tour"], "reportedCost": row["cost"]})
    main_instance = experiment / "n80w200.001.txt"
    data = instance_data(main_instance)
    for row in tours:
        expected = evaluate(row["tour"], data)
        assert expected["feasible"] and expected["cost"] == row["reportedCost"]
    rng = random.Random(81251)
    random_tours = []
    for i in range(100):
        customers = list(range(1, len(data[1])))
        rng.shuffle(customers)
        random_tours.append({"id": "random_" + str(i), "tour": [0, *customers, 0]})
    fixtures = [("benchmark", main_instance, tours + random_tours)]
    for name, windows in (("deadline-equality", [(0, 100), (0, 1), (0, 100), (0, 100)]),
                          ("depot-return", [(0, 3), (0, 100), (0, 100), (0, 100)]),
                          ("waiting", [(0, 100), (5, 5), (0, 100), (0, 100)])):
        instance = output / (name + ".txt")
        instance.write_text("4\n0 1 1 1\n1 0 1 1\n1 1 0 1\n1 1 1 0\n" + "".join(f"{a} {b}\n" for a, b in windows))
        cases = [{"id": name + "_" + str(i), "tour": tour} for i, tour in enumerate(
            ([0, 1, 2, 3, 0], [0, 2, 1, 3, 0], [0, 1, 1, 3, 0], [0, 1, 2, 0], [0, 1, 2, 9, 0]))]
        fixtures.append((name, instance, cases))
    checks = []
    mismatches = []
    for name, instance, cases in fixtures:
        fixture_data = instance_data(instance)
        jsonl = output / (name + "-cases.jsonl")
        jsonl.write_text("".join(json.dumps(row) + "\n" for row in cases))
        native_input = output / (name + "-cases.txt")
        native_input.write_text("".join(row["id"] + " " + " ".join(map(str, row["tour"])) + "\n" for row in cases))
        expected = {row["id"]: evaluate(row["tour"], fixture_data) for row in cases}
        for engine in VARIANTS:
            if engine.startswith("c-"):
                command = [str(output / "build" / engine / "cross-check"), str(instance), str(native_input)]
            else:
                kind = "TSPTWCRepairBugSolution" if engine == "java-c-bug" else "TSPTWSolution"
                command = [*java_command(engine, output), str(instance), str(jsonl), kind]
            result = subprocess.run(command, capture_output=True, text=True, check=True)
            (output / (name + "-" + engine + ".stdout")).write_text(result.stdout)
            (output / (name + "-" + engine + ".stderr")).write_text(result.stderr)
            actual = [json.loads(line) for line in result.stdout.splitlines() if line.startswith("{")]
            assert len(actual) == len(cases)
            assert {row["id"] for row in actual} == set(expected)
            for row in actual:
                target = expected[row["id"]]
                for key, value in target.items():
                    if key == "arrivals" and engine.startswith("c-"):
                        continue
                    if row.get(key) != value:
                        mismatches.append({"engine": engine, "id": row["id"], "field": key,
                                           "expected": value, "actual": row.get(key)})
                checks.append({"engine": engine, **row})
    write_json(output / "mismatches.json", mismatches)
    write_json(output / "summary.json", {"replacementTours": len(tours), "toursPerOrigin": 10,
        "crossLanguageChecks": 160, "allTourEvaluations": len(tours) * len(VARIANTS),
        "controlCases": sum(len(cases) for _, _, cases in fixtures) - len(tours),
        "totalEvaluations": len(checks), "mismatches": len(mismatches),
        "original600ToursAvailable": False, "evaluators": list(VARIANTS)})
    if mismatches:
        raise ValueError(f"{len(mismatches)} evaluation mismatches")
    print((output / "summary.json").read_text())


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("action", choices=("generate", "validate"))
    parser.add_argument("experiment", type=Path)
    arguments = parser.parse_args()
    (generate if arguments.action == "generate" else validate)(arguments.experiment.resolve())
