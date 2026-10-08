#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Build the cumulative moocore-java performance report from checkpoint results."""

from __future__ import annotations

import argparse
import json
import math
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[2]
DEFAULT_OUTPUT = ROOT / "benchmarks" / "moocore-java-performance.md"


def geometric_mean(values: list[float]) -> float | None:
    positive = [value for value in values if value > 0.0 and math.isfinite(value)]
    if not positive:
        return None
    return math.exp(sum(math.log(value) for value in positive) / len(positive))


def load_stage(argument: str) -> tuple[str, Path, dict[str, Any]]:
    if "=" not in argument:
        raise ValueError(f"Stage must have the form label=directory: {argument}")
    label, configured_path = argument.split("=", 1)
    directory = Path(configured_path).resolve()
    with (directory / "results.json").open(encoding="utf-8") as source:
        payload = json.load(source)
    return label, directory, payload


def java_results(payload: dict[str, Any]) -> dict[str, dict[str, Any]]:
    return {
        result["case_id"]: result
        for result in payload["results"]
        if result["runtime"] == "java"
    }


def format_ratio(value: float | None) -> str:
    return "—" if value is None else f"{value:.3f}×"


def operation_groups(results: dict[str, dict[str, Any]]) -> dict[str, list[dict[str, Any]]]:
    groups: dict[str, list[dict[str, Any]]] = {}
    for result in results.values():
        groups.setdefault(result["operation"], []).append(result)
    return groups


def ratio_to_moocore(results: dict[str, dict[str, Any]],
                      operation: str | None = None,
                      operations: set[str] | None = None) -> float | None:
    values = []
    for result in results.values():
        if operation is not None and result["operation"] != operation:
            continue
        if operations is not None and result["operation"] not in operations:
            continue
        ratio = result.get("ratio_to_moocore")
        if ratio is not None:
            values.append(ratio)
    return geometric_mean(values)


def allocation_groups(directory: Path) -> dict[str, list[float]]:
    path = directory / "gc-summary.json"
    if not path.is_file():
        return {}
    with path.open(encoding="utf-8") as source:
        records = json.load(source)["records"]
    groups: dict[str, list[float]] = {}
    for record in records:
        groups.setdefault(record["operation"], []).append(record["bytes_per_op"])
    return groups


def targeted_operations(label: str) -> set[str] | None:
    lowered = label.lower()
    if "sort" in lowered or "pareto" in lowered:
        return {"nondominated", "pareto_rank", "hypervolume_contributions"}
    if "hypervolume" in lowered:
        return {"hypervolume", "hypervolume_contributions"}
    if "eaf" in lowered:
        return {"eaf", "eaf_difference_points", "eaf_difference_rectangles"}
    if "vector" in lowered or "approximation" in lowered:
        return {"hypervolume_approximation"}
    return None


def display_path(path: Path) -> str:
    try:
        return path.relative_to(ROOT).as_posix()
    except ValueError:
        return path.as_posix()


def generate(stages: list[tuple[str, Path, dict[str, Any]]]) -> str:
    baseline_allocations = allocation_groups(stages[0][1])
    lines = [
        "# moocore-java performance checkpoints",
        "",
        "This report is regenerated after every performance work package. Times include public",
        "API dispatch, computation, and returned-result allocation, but exclude input loading and",
        "process/JVM startup. Package checkpoints use the same representative cases and reuse the",
        "baseline C+Python measurements; only Java changes. Java/C+Python ratios below 1.0 mean",
        "Java is faster; ratios above 1.0 mean C+Python is faster. Short checkpoints are",
        "directional; the full matrix remains the final/overnight validation.",
        "",
        "## Checkpoint summary",
        "",
        "| Checkpoint | Artifact | Cases | All operations Java/C+Python | Package target Java/C+Python |",
        "|---|---|---:|---:|---:|",
    ]
    for label, _, payload in stages:
        current = java_results(payload)
        artifact = Path(payload["environment"]["benchmark_jar"]).stem
        targets = targeted_operations(label)
        lines.append(
            f"| {label} | `{artifact}` | {len(current)} | "
            f"{format_ratio(ratio_to_moocore(current))} | "
            f"{format_ratio(None if targets is None else ratio_to_moocore(current, operations=targets))} |"
        )

    for label, directory, payload in stages:
        current = java_results(payload)
        current_groups = operation_groups(current)
        allocations = allocation_groups(directory)
        lines.extend([
            "",
            f"## {label}",
            "",
            f"- Results: `{display_path(directory)}`",
            f"- Generated: `{payload['environment']['timestamp_utc']}`",
            f"- CPU: `{payload['environment']['cpu']}`",
            f"- Java: `{(payload['environment'].get('java') or 'unknown').splitlines()[0]}`",
            "",
            "| Operation | Cases | Java/C+Python | Allocation B/op | Alloc vs Java baseline |",
            "|---|---:|---:|---:|---:|",
        ])
        for operation in sorted(current_groups):
            ratios = [
                result["ratio_to_moocore"] for result in current_groups[operation]
                if result.get("ratio_to_moocore") is not None
            ]
            allocation = geometric_mean(allocations.get(operation, []))
            baseline_allocation = geometric_mean(baseline_allocations.get(operation, []))
            allocation_text = "—" if allocation is None else f"{allocation:.0f}"
            allocation_ratio = (
                None if allocation is None or baseline_allocation is None
                else baseline_allocation / allocation
            )
            lines.append(
                f"| {operation} | {len(current_groups[operation])} | "
                f"{format_ratio(geometric_mean(ratios))} | "
                f"{allocation_text} | {format_ratio(allocation_ratio)} |"
            )

    lines.extend([
        "",
        "## Interpretation",
        "",
        "- Java/C+Python ratios compare measurements for identical cases; below 1.0 favors Java.",
        "- Package target ratios include only the operations changed by that package.",
        "- Allocation ratios still compare with the Java baseline because C+Python allocation data is unavailable.",
        "- Raw JMH/Python samples and environment metadata remain in the checkpoint directories.",
        "",
    ])
    return "\n".join(lines)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--stage", action="append", required=True,
                        help="Checkpoint in label=directory form; repeat in chronological order.")
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    arguments = parser.parse_args()
    stages = [load_stage(argument) for argument in arguments.stage]
    arguments.output.write_text(generate(stages), encoding="utf-8")
    print(arguments.output)


if __name__ == "__main__":
    main()
