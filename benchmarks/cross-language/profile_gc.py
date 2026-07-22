#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Run representative cross-language JMH cases with the GC profiler."""

from __future__ import annotations

import argparse
import json
import os
import subprocess
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[2]


METHODS = {
    "nondominated": "nondominated",
    "pareto_rank": "paretoRank",
    "hypervolume": "hypervolume",
    "hypervolume_contributions": "hypervolumeContributions",
    "hypervolume_approximation": "approximateHypervolume",
    "epsilon_additive": "epsilonAdditive",
    "epsilon_multiplicative": "epsilonMultiplicative",
    "igd_plus": "igdPlus",
    "exact_r2": "exactR2",
    "eaf": "eaf",
    "eaf_difference_points": "eafDifference",
    "eaf_difference_rectangles": "eafDifference",
    "weighted_hypervolume": "weightedHypervolume",
    "hype_weighted_hypervolume": "hypeWeightedHypervolume",
}


def representative_cases(cases: list[dict[str, Any]]) -> list[dict[str, Any]]:
    selected: dict[tuple[str, int, str | None], dict[str, Any]] = {}
    for case in cases:
        key = (case["operation"], case["objectives"], case.get("method"))
        current = selected.get(key)
        if current is None or case["size"] > current["size"]:
            selected[key] = case
    return sorted(selected.values(), key=lambda case: (
        case["operation"], case["objectives"], case.get("method") or ""
    ))


def score(metric: dict[str, Any]) -> float:
    return float(metric["score"])


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--stage", type=Path, required=True)
    parser.add_argument("--java-jar", type=Path, required=True)
    parser.add_argument("--operations", nargs="+", choices=tuple(METHODS))
    arguments = parser.parse_args()
    stage = arguments.stage.resolve()
    with (stage / "manifest.json").open(encoding="utf-8") as source:
        cases = json.load(source)["cases"]
    if arguments.operations:
        enabled = set(arguments.operations)
        cases = [case for case in cases if case["operation"] in enabled]
    raw = stage / "gc"
    raw.mkdir(parents=True, exist_ok=True)
    environment = os.environ.copy()
    environment["MOOCORE_CROSS_LANGUAGE_CASES"] = str((stage / "cases").resolve())
    records = []
    for case in representative_cases(cases):
        operation = case["operation"]
        method = METHODS[operation]
        output = raw / f"{case['case_id']}.json"
        benchmark = (
            "^es\\.urjc\\.etsii\\.grafo\\.benchmarks\\."
            f"CrossLanguageMoocoreBenchmark\\.{method}$"
        )
        command = [
            "java", "--add-modules", "jdk.incubator.vector",
            "-jar", str(arguments.java_jar.resolve()), benchmark,
            "-p", f"selectedCaseId={case['case_id']}",
            "-f", "1", "-wi", "1", "-i", "2", "-w", "1s", "-r", "1s",
            "-prof", "gc", "-rf", "json", "-rff", str(output),
            "-jvmArgsPrepend", "--add-modules=jdk.incubator.vector",
        ]
        subprocess.run(command, cwd=ROOT, check=True, env=environment)
        with output.open(encoding="utf-8") as source:
            result = json.load(source)[0]
        allocation = result["secondaryMetrics"]["gc.alloc.rate.norm"]
        records.append({
            "case_id": case["case_id"],
            "operation": operation,
            "objectives": case["objectives"],
            "method": case.get("method"),
            "bytes_per_op": score(allocation),
        })
    with (stage / "gc-summary.json").open("w", encoding="utf-8") as destination:
        json.dump({"schema_version": 1, "records": records}, destination, indent=2)
        destination.write("\n")


if __name__ == "__main__":
    main()
