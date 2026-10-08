#!/usr/bin/env python3
# SPDX-License-Identifier: MPL-2.0
"""Generate deterministic behavioral fixtures with an installed Python moocore."""

import argparse
import json
from pathlib import Path

import moocore
import numpy as np


def rows(values):
    return np.asarray(values, dtype=float)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("output", type=Path)
    args = parser.parse_args()

    front = rows([[5, 5], [4, 6], [2, 7], [7, 4]])
    left = rows([
        [3, 2, 1], [2, 3, 1],
        [2.5, 1, 2], [1, 2, 2],
        [1, 2, 3],
    ])
    right = rows([
        [4, 2.5, 1], [3, 3, 1], [2.5, 3.5, 1],
        [3, 3, 2], [2.5, 3.5, 2],
        [2, 1, 3],
    ])

    fixtures = {
        "upstream_version": getattr(moocore, "__version__", "unknown"),
        "hypervolume": moocore.hypervolume(front, ref=[10, 10]),
        "hypervolume_contributions": moocore.hv_contributions(
            front, ref=[10, 10]
        ).tolist(),
        "r2_exact": moocore.r2_exact(front, ref=[0, 0]),
        "hv_approx_rphi": moocore.hv_approx(
            front, ref=[10, 10], method="Rphi-FWE+"
        ),
        "hv_approx_hua_wang": moocore.hv_approx(
            front, ref=[10, 10], method="DZ2019-HW"
        ),
        "eaf_difference_points": moocore.eafdiff(left, right).tolist(),
        "eaf_difference_rectangles": moocore.eafdiff(
            left, right, rectangles=True
        ).tolist(),
    }
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(
        json.dumps(fixtures, indent=2, sort_keys=True) + "\n", encoding="utf-8"
    )


if __name__ == "__main__":
    main()
