#!/usr/bin/env python3
"""Cross-check saved time-budget tours with both actual native Solution implementations."""
import argparse
import hashlib
import json
from pathlib import Path
import subprocess

from cross_validate_tours import evaluate, instance_data


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("experiment", type=Path)
    parser.add_argument("--native-build", required=True, type=Path)
    args = parser.parse_args()
    experiment = args.experiment.resolve()
    manifest = json.loads((experiment / "manifest.json").read_text())
    rows = [json.loads(line) for line in (experiment / "runs.jsonl").read_text().splitlines()]
    seeds = {seed for worker in manifest["workers"]
             for seed in range(worker["firstSeed"], worker["firstSeed"] + worker["repetitions"])}
    if len(rows) != manifest["repetitions"] or {row["seed"] for row in rows} != seeds:
        raise ValueError("Missing or duplicate tour records")
    output = experiment / "native-validation"
    output.mkdir(exist_ok=False)
    instance = experiment / manifest["instance"]
    data = instance_data(instance)
    expected = {}
    for row in rows:
        reference = evaluate(row["tour"], data)
        if not reference["feasible"] or any(reference[key] != row[key] for key in ("cost", "violations", "infeasibility")):
            raise ValueError(f"Independent validation failed for seed {row['seed']}")
        expected["seed_" + str(row["seed"])] = reference
    tours = output / "tours.txt"
    tours.write_text("".join("seed_" + str(row["seed"]) + " " + " ".join(map(str, row["tour"])) + "\n" for row in rows))
    engines = {}
    mismatches = []
    for engine in ("c-original", "c-fixed"):
        executable = args.native_build.resolve() / engine / "cross-check"
        command = [str(executable), str(instance), str(tours)]
        result = subprocess.run(command, capture_output=True, text=True, check=True)
        (output / (engine + ".stdout")).write_text(result.stdout)
        (output / (engine + ".stderr")).write_text(result.stderr)
        actual = [json.loads(line) for line in result.stdout.splitlines() if line.startswith("{")]
        if len(actual) != len(rows) or {row["id"] for row in actual} != set(expected):
            raise ValueError("Missing or duplicate native evaluations")
        for row in actual:
            reference = expected[row["id"]]
            for key in ("validPermutation", "feasible", "cost", "violations", "infeasibility", "makespan"):
                if row[key] != reference[key]:
                    mismatches.append({"engine": engine, "id": row["id"], "field": key,
                                       "expected": reference[key], "actual": row[key]})
            if not row["cacheConsistent"]:
                mismatches.append({"engine": engine, "id": row["id"], "field": "cacheConsistent"})
        engines[engine] = {"evaluatedTours": len(actual), "command": command,
                           "executableHash": hashlib.sha256(executable.read_bytes()).hexdigest()}
    (output / "mismatches.json").write_text(json.dumps(mismatches, indent=2) + "\n")
    summary = {"independentlyValidatedTours": len(rows), "nativeEvaluations": 2 * len(rows),
               "mismatches": len(mismatches), "engines": engines}
    (output / "summary.json").write_text(json.dumps(summary, indent=2) + "\n")
    print(json.dumps(summary, indent=2))
    if mismatches:
        raise ValueError("Native evaluations disagree with independent validation")


if __name__ == "__main__":
    main()
