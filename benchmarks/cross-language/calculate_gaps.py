#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Calculate Java performance gaps from a cross-language results.csv file."""

from __future__ import annotations

import argparse
import csv
import math
import statistics
from collections import defaultdict
from pathlib import Path


PAIR_KEY = ("operation", "case_id", "method")


def geometric_mean(values: list[float]) -> float:
    return math.exp(sum(math.log(value) for value in values) / len(values))


def format_time(value: float) -> str:
    absolute = abs(value)
    if absolute >= 1_000_000_000:
        return f"{value / 1_000_000_000:.3f} s"
    if absolute >= 1_000_000:
        return f"{value / 1_000_000:.3f} ms"
    if absolute >= 1_000:
        return f"{value / 1_000:.3f} us"
    return f"{value:.3f} ns"


def format_gap(value: float) -> str:
    return f"{value:+.1f}%"


def load_gaps(path: Path) -> list[dict[str, object]]:
    pairs: dict[tuple[str, str, str], dict[str, dict[str, str]]] = defaultdict(dict)
    with path.open(newline="", encoding="utf-8") as source:
        for row in csv.DictReader(source):
            key = tuple(row[field] for field in PAIR_KEY)
            runtime = row["runtime"]
            if runtime in pairs[key]:
                raise ValueError(f"Duplicate {runtime} result for {key}")
            pairs[key][runtime] = row

    gaps: list[dict[str, object]] = []
    for key, runtimes in pairs.items():
        if set(runtimes) != {"java", "python"}:
            raise ValueError(f"Expected Java and C+Python results for {key}, got {set(runtimes)}")
        java = runtimes["java"]
        native = runtimes["python"]
        java_mean = float(java["mean_ns_op"])
        native_mean = float(native["mean_ns_op"])
        ratio = java_mean / native_mean
        gaps.append({
            "operation": java["operation"],
            "dataset": java["dataset"],
            "size": int(java["size"]),
            "objectives": int(java["objectives"]),
            "method": java["method"],
            "case_id": java["case_id"],
            "native_ns_op": native_mean,
            "java_ns_op": java_mean,
            "absolute_gap_ns_op": java_mean - native_mean,
            "java_to_native_ratio": ratio,
            "relative_gap_percent": (ratio - 1.0) * 100.0,
            "winner": "Java" if ratio < 1.0 else "C+Python" if ratio > 1.0 else "Tie",
        })
    return sorted(gaps, key=lambda row: (
        str(row["operation"]), str(row["dataset"]), int(row["objectives"]),
        int(row["size"]), str(row["method"]),
    ))


def write_csv(path: Path, gaps: list[dict[str, object]]) -> None:
    with path.open("w", newline="", encoding="utf-8") as output:
        writer = csv.DictWriter(output, fieldnames=list(gaps[0]))
        writer.writeheader()
        writer.writerows(gaps)


def summary_row(name: str, gaps: list[dict[str, object]]) -> str:
    ratios = [float(row["java_to_native_ratio"]) for row in gaps]
    relative_gaps = [float(row["relative_gap_percent"]) for row in gaps]
    ratio = geometric_mean(ratios)
    java_wins = sum(value < 1.0 for value in ratios)
    native_wins = sum(value > 1.0 for value in ratios)
    return (
        f"| {name} | {len(gaps)} | {ratio:.3f}x | {format_gap((ratio - 1.0) * 100.0)} | "
        f"{format_gap(statistics.median(relative_gaps))} | {java_wins} | {native_wins} |"
    )


def case_row(row: dict[str, object]) -> str:
    method = str(row["method"]) or "—"
    return (
        f"| {row['operation']} | {row['dataset']} | {row['size']} | {row['objectives']} | {method} | "
        f"{format_time(float(row['native_ns_op']))} | {format_time(float(row['java_ns_op']))} | "
        f"{format_time(float(row['absolute_gap_ns_op']))} | "
        f"{float(row['java_to_native_ratio']):.3f}x | "
        f"{format_gap(float(row['relative_gap_percent']))} | {row['winner']} |"
    )


def generate_markdown(source: Path, gaps: list[dict[str, object]]) -> str:
    by_operation: dict[str, list[dict[str, object]]] = defaultdict(list)
    by_method_and_objectives: dict[tuple[str, int, str], list[dict[str, object]]] = defaultdict(list)
    for row in gaps:
        by_operation[str(row["operation"])].append(row)
        if row["method"]:
            key = (str(row["operation"]), int(row["objectives"]), str(row["method"]))
            by_method_and_objectives[key].append(row)

    ratios = [float(row["java_to_native_ratio"]) for row in gaps]
    overall_ratio = geometric_mean(ratios)
    java_wins = sum(ratio < 1.0 for ratio in ratios)
    native_wins = sum(ratio > 1.0 for ratio in ratios)
    within_five_percent = sum(0.95 <= ratio <= 1.05 for ratio in ratios)

    lines = [
        "# Current Java gaps against C+Python moocore",
        "",
        f"Source: `{source}`",
        "",
        "The ratio is Java time divided by C+Python time. The relative gap is",
        "`(Java / C+Python - 1) * 100`: negative values favor Java and positive values",
        "favor C+Python. Operation and overall ratios are geometric means so that each",
        "benchmark case has equal multiplicative weight.",
        "",
        "## Overall",
        "",
        f"Across {len(gaps)} matched cases, the geometric-mean Java/C+Python ratio is "
        f"**{overall_ratio:.3f}x** ({format_gap((overall_ratio - 1.0) * 100.0)} gap).",
        f"Java is faster in **{java_wins}** cases, C+Python is faster in **{native_wins}**, "
        f"and **{within_five_percent}** are within 5%.",
        "",
        "## Operation summary",
        "",
        "| Operation | Cases | Geomean Java/C+Python | Geomean gap | Median case gap | Java wins | C+Python wins |",
        "|---|---:|---:|---:|---:|---:|---:|",
    ]
    for operation in sorted(by_operation):
        lines.append(summary_row(operation, by_operation[operation]))

    if by_method_and_objectives:
        lines.extend([
            "",
            "## Method and objective summary",
            "",
            "| Operation / objectives / method | Cases | Geomean Java/C+Python | Geomean gap | Median case gap | Java wins | C+Python wins |",
            "|---|---:|---:|---:|---:|---:|---:|",
        ])
        for key in sorted(by_method_and_objectives):
            operation, objectives, method = key
            lines.append(summary_row(
                f"{operation} / {objectives}D / {method}",
                by_method_and_objectives[key],
            ))

    best = sorted(gaps, key=lambda row: float(row["java_to_native_ratio"]))[:10]
    worst = sorted(gaps, key=lambda row: float(row["java_to_native_ratio"]), reverse=True)[:10]
    for title, rows in (("Largest Java advantages", best), ("Largest Java deficits", worst)):
        lines.extend([
            "",
            f"## {title}",
            "",
            "| Case | Method | Java/C+Python | Gap |",
            "|---|---|---:|---:|",
        ])
        for row in rows:
            method = str(row["method"]) or "—"
            lines.append(
                f"| `{row['case_id']}` | {method} | "
                f"{float(row['java_to_native_ratio']):.3f}x | "
                f"{format_gap(float(row['relative_gap_percent']))} |"
            )

    lines.extend([
        "",
        "## Every measured gap",
        "",
        "| Operation | Dataset | Size | Obj. | Method | C+Python | Java | Absolute gap | Java/C+Python | Relative gap | Faster |",
        "|---|---|---:|---:|---|---:|---:|---:|---:|---:|---|",
    ])
    lines.extend(case_row(row) for row in gaps)
    lines.append("")
    return "\n".join(lines)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("results", type=Path, help="Cross-language results.csv")
    parser.add_argument("--csv-output", type=Path)
    parser.add_argument("--markdown-output", type=Path)
    arguments = parser.parse_args()

    results = arguments.results.resolve()
    csv_output = arguments.csv_output or results.with_name("gaps.csv")
    markdown_output = arguments.markdown_output or results.with_name("gaps.md")
    gaps = load_gaps(results)
    write_csv(csv_output, gaps)
    markdown_output.write_text(generate_markdown(results, gaps), encoding="utf-8")
    print(f"Wrote {len(gaps)} gaps to {csv_output} and {markdown_output}")


if __name__ == "__main__":
    main()
