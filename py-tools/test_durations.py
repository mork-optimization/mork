#!/usr/bin/env python3
"""Report slow unit cases and check Maven unit/integration test discovery."""

import argparse
from pathlib import Path
import sys
import xml.etree.ElementTree as ET


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--threshold", type=float, default=1.0, help="Unit-test review threshold in seconds")
    parser.add_argument("--modules", nargs="+", help="Only inspect reports from these reactor modules")
    parser.add_argument("--require-integration", action="store_true", help="Require complete, unskipped integration reports")
    args = parser.parse_args()
    cases = {"surefire": [], "failsafe": []}
    problems = []
    for kind, rows in cases.items():
        for report in sorted(args.root.glob(f"*/target/{kind}-reports/TEST-*.xml")):
            if args.modules and report.relative_to(args.root).parts[0] not in args.modules:
                continue
            suite = ET.parse(report).getroot()
            for case in suite.findall("testcase"):
                name = case.get("classname", suite.get("name", ""))
                rows.append((float(case.get("time", "0")), name, case.get("name", "")))
                if kind == "surefire" and name.endswith("IT"):
                    problems.append(f"Integration class ran in the unit suite: {name}")
                if args.require_integration and kind == "failsafe" and case.find("skipped") is not None:
                    problems.append(f"Integration case was skipped: {name}.{case.get('name')}")
                if case.find("failure") is not None or case.find("error") is not None:
                    problems.append(f"Failed case: {name}.{case.get('name')}")

    for kind, rows in cases.items():
        print(f"{kind}: {len(rows)} cases, {sum(row[0] for row in rows):.3f}s cumulative case time")
    print(f"Unit cases over {args.threshold:g}s (review, not a timeout):")
    slow = sorted((row for row in cases["surefire"] if row[0] > args.threshold), reverse=True)
    for seconds, name, method in slow:
        print(f"  {seconds:8.3f}s  {name}.{method}")
    if not slow:
        print("  none")
    if not cases["surefire"]:
        problems.append("No unit-test reports found")
    if args.require_integration:
        names = {row[1].rsplit(".", 1)[-1] for row in cases["failsafe"]}
        for required in ("SshWorkloadIT", "AutoLlmLifecycleIT", "AutoLlmCampaignIT", "IraceIT", "ComplexityAnalysisIT"):
            if required not in names:
                problems.append(f"Missing required integration class: {required}")
    for problem in problems:
        print(problem, file=sys.stderr)
    return 1 if problems else 0


if __name__ == "__main__":
    sys.exit(main())
