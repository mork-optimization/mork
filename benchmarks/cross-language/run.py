#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-2.1-or-later
"""Compare Python/C moocore with mork-moocore Java."""

from __future__ import annotations

import argparse
import csv
import importlib
import importlib.metadata
import json
import math
import os
import platform
import statistics
import struct
import subprocess
import sys
import time
from dataclasses import asdict, dataclass
from pathlib import Path
from typing import Any, Callable


ROOT = Path(__file__).resolve().parents[2]
SCRIPT_DIR = Path(__file__).resolve().parent
DEFAULT_OUTPUT = ROOT / "benchmarks" / "target" / "cross-language"
DEFAULT_JAR = ROOT / "benchmarks" / "target" / "benchmarks.jar"
TESTSUITE_DATA = ROOT / "testsuite" / "data"
os.environ.setdefault("MPLCONFIGDIR", str(DEFAULT_OUTPUT / "matplotlib-cache"))
SAMPLES = 262_144
SEED = 42
FORKS = 2
WARMUP_ITERATIONS = 3
MEASUREMENT_ITERATIONS = 5
ITERATION_SECONDS = 1.0
SLOW_CASE_SECONDS = 10.0

REQUIRED_IMPORTS = {
    "numpy": "numpy",
    "matplotlib": "matplotlib",
    "py-cpuinfo": "cpuinfo",
    "moocore": "moocore",
}

OPERATIONS = (
    "nondominated",
    "pareto_rank",
    "hypervolume",
    "hypervolume_contributions",
    "hypervolume_approximation",
    "epsilon_additive",
    "epsilon_multiplicative",
    "igd_plus",
    "exact_r2",
    "eaf",
    "eaf_difference_points",
    "eaf_difference_rectangles",
    "weighted_hypervolume",
    "hype_weighted_hypervolume",
)


def enable_quick_mode() -> None:
    global FORKS, WARMUP_ITERATIONS, MEASUREMENT_ITERATIONS
    global ITERATION_SECONDS, SLOW_CASE_SECONDS
    FORKS = 1
    WARMUP_ITERATIONS = 1
    MEASUREMENT_ITERATIONS = 3
    ITERATION_SECONDS = 0.25
    SLOW_CASE_SECONDS = 3.0


if os.environ.get("MOOCORE_BENCHMARK_QUICK") == "1":
    enable_quick_mode()


def progress(message: str) -> None:
    timestamp = time.strftime("%Y-%m-%d %H:%M:%S")
    print(f"[{timestamp}] {message}", flush=True)


@dataclass
class Case:
    case_id: str
    operation: str
    dataset: str
    size: int
    objectives: int
    points: str
    reference: str | None = None
    oracle: str | None = None
    keep_weakly: bool | None = None
    method: str | None = None
    exact_hv: float | None = None
    sets: str | None = None
    right_points: str | None = None
    right_sets: str | None = None
    auxiliary: str | None = None
    ideal: str | None = None
    mu: str | None = None
    distribution: str | None = None
    samples: int | None = None
    seed: int | None = None
    format: str | None = None


def fail_missing_dependencies() -> None:
    missing = []
    for distribution, module in REQUIRED_IMPORTS.items():
        try:
            importlib.import_module(module)
        except Exception as error:
            missing.append(f"{distribution} ({module}): {error}")
    if not missing:
        return
    setup = "python3 benchmarks/cross-language/setup_environment.py"
    environment_python = (
        "benchmarks\\cross-language\\venv\\Scripts\\python.exe"
        if os.name == "nt"
        else "benchmarks/cross-language/venv/bin/python"
    )
    manual = (
        "python3 -m venv benchmarks/cross-language/venv\n"
        f"{environment_python} -m pip install "
        "-r benchmarks/cross-language/requirements.txt"
    )
    raise SystemExit(
        "Missing required cross-language benchmark dependencies:\n  - "
        + "\n  - ".join(missing)
        + f"\n\nCreate the benchmark environment with:\n  {setup}"
        + f"\n\nEquivalent manual commands from the repository root:\n{manual}"
    )


def write_matrix(path: Path, values: Any) -> None:
    import numpy as np

    matrix = np.asarray(values, dtype="<f8")
    if matrix.ndim == 1:
        matrix = matrix.reshape((-1, 1))
    if matrix.ndim != 2:
        raise ValueError(f"Expected a two-dimensional matrix, got {matrix.shape}")
    matrix = np.ascontiguousarray(matrix, dtype="<f8")
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("wb") as output:
        output.write(b"MOOC")
        output.write(struct.pack("<II", matrix.shape[0], matrix.shape[1]))
        output.write(matrix.tobytes(order="C"))


def read_matrix(path: Path) -> Any:
    import numpy as np

    with path.open("rb") as source:
        if source.read(4) != b"MOOC":
            raise ValueError(f"Invalid matrix header: {path}")
        rows, columns = struct.unpack("<II", source.read(8))
        payload = source.read()
    expected = rows * columns * 8
    if len(payload) != expected:
        raise ValueError(
            f"Invalid matrix payload in {path}: expected {expected}, got {len(payload)}"
        )
    return np.frombuffer(payload, dtype="<f8").reshape((rows, columns)).copy()


def geometric_sizes(length: int, start: int, stop: int, count: int) -> list[int]:
    import numpy as np

    upper = min(stop, length)
    if upper < start:
        return [upper]
    return [int(value) for value in np.unique(
        np.geomspace(start, upper, num=count, dtype=int)
    )]


def linear_sizes(length: int, start: int, stop: int, step: int) -> list[int]:
    upper = min(stop, length)
    return list(range(start, upper + 1, step))


def read_dataset(path: Path, remove_set_column: bool) -> Any:
    import moocore
    import numpy as np

    if remove_set_column:
        data = moocore.read_datasets(path)[:, :-1]
    else:
        data = np.loadtxt(path)
    return moocore.filter_dominated(data)


class CaseBuilder:
    def __init__(self, output: Path):
        self.output = output
        self.input_dir = output / "inputs"
        self.oracle_dir = output / "oracles"
        self.case_dir = output / "cases"
        self.cases: list[Case] = []
        self._matrices: dict[str, Any] = {}

    def matrix(self, name: str, values: Any) -> str:
        relative = Path("..") / "inputs" / f"{name}.bin"
        write_matrix(self.input_dir / f"{name}.bin", values)
        self._matrices[name] = values
        return relative.as_posix()

    def oracle(self, case_id: str, values: Any) -> str:
        relative = Path("..") / "oracles" / f"{case_id}.bin"
        write_matrix(self.oracle_dir / f"{case_id}.bin", values)
        return relative.as_posix()

    def add(self, case: Case) -> None:
        self.cases.append(case)
        properties = {
            "points": case.points,
            "size": str(case.size),
            "oracle": case.oracle or "",
        }
        if case.reference is not None:
            properties["reference"] = case.reference
        if case.keep_weakly is not None:
            properties["keepWeakly"] = str(case.keep_weakly).lower()
        if case.method is not None:
            properties["method"] = java_method(case.method)
            properties["samples"] = str(SAMPLES)
            properties["seed"] = str(SEED)
        for field, key in (
            ("sets", "sets"),
            ("right_points", "rightPoints"),
            ("right_sets", "rightSets"),
            ("auxiliary", "auxiliary"),
            ("ideal", "ideal"),
            ("mu", "mu"),
        ):
            value = getattr(case, field)
            if value is not None:
                properties[key] = value
        if case.distribution is not None:
            properties["distribution"] = case.distribution.upper()
        if case.samples is not None:
            properties["samples"] = str(case.samples)
        if case.seed is not None:
            properties["seed"] = str(case.seed)
        if case.format is not None:
            properties["format"] = case.format.upper()
        self.case_dir.mkdir(parents=True, exist_ok=True)
        with (self.case_dir / f"{case.case_id}.properties").open(
            "w", encoding="utf-8"
        ) as output:
            for key, value in properties.items():
                output.write(f"{key}={value}\n")

    def relative(self, path: str) -> Path:
        return (self.case_dir / path).resolve()


def java_method(method: str) -> str:
    return {
        "DZ2019-MC": "DZ2019_MC",
        "DZ2019-HW": "DZ2019_HW",
        "Rphi-FWE+": "RPHI_FWE_PLUS",
    }[method]


def safe_id(value: str) -> str:
    return "".join(character.lower() if character.isalnum() else "-"
                   for character in value).strip("-")


def add_case(
    builder: CaseBuilder,
    operation: str,
    dataset: str,
    size: int,
    points_file: str,
    points: Any,
    reference_file: str | None = None,
    reference: Any | None = None,
    keep_weakly: bool | None = None,
    method: str | None = None,
    sets_file: str | None = None,
    sets: Any | None = None,
    right_points_file: str | None = None,
    right_points: Any | None = None,
    right_sets_file: str | None = None,
    right_sets: Any | None = None,
    auxiliary_file: str | None = None,
    auxiliary: Any | None = None,
    ideal_file: str | None = None,
    ideal: Any | None = None,
    mu_file: str | None = None,
    mu: Any | None = None,
    distribution: str | None = None,
    samples: int | None = None,
    seed: int | None = None,
    output_format: str | None = None,
) -> None:
    import moocore
    import numpy as np

    suffix = ""
    if keep_weakly is not None:
        suffix = "-weak" if keep_weakly else "-strict"
    if method is not None:
        suffix = "-" + safe_id(method)
    if distribution is not None:
        suffix = "-" + safe_id(distribution)
    if output_format is not None:
        suffix = "-" + safe_id(output_format)
    case_id = f"{safe_id(operation)}-{safe_id(dataset)}-{size}{suffix}"
    selected = points[:size, :]
    exact_hv = None
    if operation == "nondominated":
        oracle = moocore.is_nondominated(
            selected, maximise=True, keep_weakly=keep_weakly
        ).astype(float)
    elif operation == "pareto_rank":
        oracle = moocore.pareto_rank(selected).astype(float)
    elif operation == "hypervolume":
        oracle = [[moocore.hypervolume(selected, ref=reference)]]
    elif operation == "hypervolume_contributions":
        oracle = moocore.hv_contributions(selected, ref=reference)
    elif operation == "hypervolume_approximation":
        exact_hv = float(moocore.hypervolume(selected, ref=reference))
        if method == "DZ2019-MC":
            oracle = [[exact_hv]]
        else:
            oracle = [[moocore.hv_approx(
                selected, ref=reference, nsamples=SAMPLES,
                seed=SEED, method=method
            )]]
    elif operation == "epsilon_additive":
        oracle = [[moocore.epsilon_additive(selected, ref=reference)]]
    elif operation == "epsilon_multiplicative":
        oracle = [[moocore.epsilon_mult(selected, ref=reference)]]
    elif operation == "igd_plus":
        oracle = [[moocore.igd_plus(selected, ref=reference)]]
    elif operation == "exact_r2":
        oracle = [[moocore.r2_exact(selected, ref=reference)]]
    elif operation == "eaf":
        oracle = moocore.eaf(selected, sets=np.asarray(sets)[:size])
    elif operation in ("eaf_difference_points", "eaf_difference_rectangles"):
        left_matrix = np.column_stack((selected, np.asarray(sets)[:size]))
        right_matrix = np.column_stack((right_points, right_sets))
        oracle = moocore.eafdiff(
            left_matrix, right_matrix,
            rectangles=operation == "eaf_difference_rectangles",
        )
    elif operation == "weighted_hypervolume":
        oracle = [[moocore.whv_rect(selected, auxiliary, ref=reference)]]
    elif operation == "hype_weighted_hypervolume":
        native_mu = float(mu[0]) if distribution == "exponential" else mu
        oracle = [[moocore.whv_hype(
            selected, ref=reference, ideal=ideal, nsamples=samples,
            seed=seed, dist=distribution, mu=native_mu,
        )]]
    else:
        raise ValueError(f"Unknown operation: {operation}")
    oracle_file = builder.oracle(case_id, np.asarray(oracle, dtype=float))
    builder.add(Case(
        case_id=case_id,
        operation=operation,
        dataset=dataset,
        size=size,
        objectives=selected.shape[1],
        points=points_file,
        reference=reference_file,
        oracle=oracle_file,
        keep_weakly=keep_weakly,
        method=method,
        exact_hv=exact_hv,
        sets=sets_file,
        right_points=right_points_file,
        right_sets=right_sets_file,
        auxiliary=auxiliary_file,
        ideal=ideal_file,
        mu=mu_file,
        distribution=distribution,
        samples=samples,
        seed=seed,
        format=output_format,
    ))


def prepare_cases(output: Path, selected_operations: set[str]) -> list[Case]:
    import moocore
    import numpy as np

    builder = CaseBuilder(output)

    if "nondominated" in selected_operations:
        datasets = {
            "test2D-200k": (
                read_dataset(TESTSUITE_DATA / "test2D-200k.inp.xz", True),
                (1_000, 50_000, 10),
            ),
            "ran3d-40k": (
                read_dataset(TESTSUITE_DATA / "ran.40000pts.3d.1.xz", True),
                (1_000, 40_000, 10),
            ),
            "sphere-4d": (
                moocore.generate_ndset(25_000, 4, method="sphere", seed=SEED),
                (500, 25_000, 10),
            ),
            "convex-4d": (
                moocore.generate_ndset(
                    25_000, 4, method="convex-sphere", seed=SEED
                ),
                (500, 25_000, 10),
            ),
            "sphere-5d": (
                moocore.generate_ndset(20_000, 5, method="sphere", seed=SEED),
                (100, 20_000, 10),
            ),
            "rmnk-10d": (
                read_dataset(
                    TESTSUITE_DATA / "rmnk_0.0_10_16_1_0_ref.txt.xz", True
                ),
                (100, 20_000, 10),
            ),
        }
        for name, (points, size_range) in datasets.items():
            points_file = builder.matrix(f"nondominated-{safe_id(name)}", points)
            for size in geometric_sizes(len(points), *size_range):
                add_case(
                    builder, "nondominated", name, size, points_file, points,
                    keep_weakly=True,
                )
            strict = np.unique(points, axis=0)
            strict_file = builder.matrix(
                f"nondominated-{safe_id(name)}-strict", strict
            )
            for size in geometric_sizes(len(strict), *size_range):
                add_case(
                    builder, "nondominated", name, size, strict_file, strict,
                    keep_weakly=False,
                )

    if "pareto_rank" in selected_operations:
        rng = np.random.default_rng(SEED)
        for objectives in (2, 3, 4, 5, 9, 10):
            length = 20_000 if objectives <= 5 else 10_000
            points = rng.random(size=(length, objectives))
            while len(np.unique(points, axis=0)) < len(points):
                points = rng.random(size=(length, objectives))
            name = f"ran-{objectives}d"
            points_file = builder.matrix(f"pareto-rank-{name}", points)
            for size in geometric_sizes(
                len(points), 100 if objectives <= 5 else 10, length, 10
            ):
                add_case(builder, "pareto_rank", name, size, points_file, points)

    if "hypervolume" in selected_operations:
        definitions = {
            "DTLZLinearShape.3d": (
                "DTLZLinearShape.3d.front.1000pts.10", (200, 2_000, 10)
            ),
            "DTLZLinearShape.4d": (
                "DTLZLinearShape.4d.front.1000pts.10", (100, 1_200, 10)
            ),
            "DTLZLinearShape.5d": (
                "DTLZLinearShape.5d.front.500pts.10", (10, 600, 10)
            ),
            "DTLZLinearShape.6d": (
                "DTLZLinearShape.6d.front.700pts.10.xz", (10, 300, 10)
            ),
        }
        for name, (filename, size_range) in definitions.items():
            points = read_dataset(TESTSUITE_DATA / filename, True)
            reference = np.ones(points.shape[1])
            points_file = builder.matrix(f"hypervolume-{safe_id(name)}", points)
            reference_file = builder.matrix(
                f"hypervolume-{safe_id(name)}-reference", reference.reshape(1, -1)
            )
            for size in geometric_sizes(len(points), *size_range):
                add_case(
                    builder, "hypervolume", name, size, points_file, points,
                    reference_file, reference,
                )

    if "hypervolume_contributions" in selected_operations:
        rng = np.random.default_rng(SEED + 1)
        for objectives in (2, 3):
            random_points = rng.random((1_000, objectives))
            simplex_points = -np.log(np.maximum(
                rng.random((1_000, objectives)), np.finfo(float).tiny
            ))
            simplex_points /= simplex_points.sum(axis=1, keepdims=True)
            reference = np.ones(objectives)
            reference_file = builder.matrix(
                f"hvc-{objectives}d-reference", reference.reshape(1, -1)
            )
            for shape, points in (("random", random_points), ("simplex", simplex_points)):
                name = f"{shape}-{objectives}d"
                points_file = builder.matrix(f"hvc-{name}", points)
                for size in (100, 500, 1_000):
                    add_case(
                        builder, "hypervolume_contributions", name, size,
                        points_file, points, reference_file, reference,
                    )

    if "hypervolume_approximation" in selected_operations:
        definitions = {
            "DTLZLinearShape.3d": (
                "DTLZLinearShape.3d.front.1000pts.10", 1.0, (100, 1_000, 100)
            ),
            "DTLZLinearShape.4d": (
                "DTLZLinearShape.4d.front.1000pts.10", 1.0, (100, 1_000, 100)
            ),
            "DTLZLinearShape.6d": (
                "DTLZLinearShape.6d.front.700pts.10.xz", 1.0, (50, 600, 50)
            ),
            "DTLZSphereShape.6d": (
                "DTLZSphereShape.6d.front.1000pts.10.xz", 1.1, (50, 600, 50)
            ),
            "DTLZLinearShape.9d": (
                "DTLZLinearShape.9d.front.60pts.10", 1.0, (50, 150, 10)
            ),
        }
        for name, (filename, ref_value, size_range) in definitions.items():
            points = read_dataset(TESTSUITE_DATA / filename, True)
            reference = np.full(points.shape[1], ref_value)
            points_file = builder.matrix(f"hvapprox-{safe_id(name)}", points)
            reference_file = builder.matrix(
                f"hvapprox-{safe_id(name)}-reference", reference.reshape(1, -1)
            )
            for size in linear_sizes(len(points), *size_range):
                for method in ("DZ2019-MC", "DZ2019-HW", "Rphi-FWE+"):
                    add_case(
                        builder, "hypervolume_approximation", name, size,
                        points_file, points, reference_file, reference,
                        method=method,
                    )

    if "epsilon_additive" in selected_operations:
        points = read_dataset(
            TESTSUITE_DATA / "rmnk_0.0_10_16_1_0_random_search_1.txt.xz", False
        )
        reference = read_dataset(
            TESTSUITE_DATA / "rmnk_0.0_10_16_1_0_ref.txt.xz", False
        )
        points_file = builder.matrix("epsilon-rmnk-points", points)
        reference_file = builder.matrix("epsilon-rmnk-reference", reference)
        for size in linear_sizes(len(points), 200, 1_000, 200):
            add_case(
                builder, "epsilon_additive", "rmnk-10d", size,
                points_file, points, reference_file, reference,
            )

    if "igd_plus" in selected_operations:
        points = read_dataset(TESTSUITE_DATA / "ran.40000pts.3d.1.xz", False)
        reference = read_dataset(TESTSUITE_DATA / "ran.40001pts.3d.1.xz", False)
        points_file = builder.matrix("igd-plus-points", points)
        reference_file = builder.matrix("igd-plus-reference", reference)
        for size in linear_sizes(len(points), 100, 1_300, 200):
            add_case(
                builder, "igd_plus", "ran-40000pts-3d", size,
                points_file, points, reference_file, reference,
            )

    if "exact_r2" in selected_operations:
        rng = np.random.default_rng(SEED + 2)
        points = rng.random((1_000, 2))
        reference = np.ones(2)
        points_file = builder.matrix("exact-r2-random-2d", points)
        reference_file = builder.matrix("exact-r2-reference", reference.reshape(1, -1))
        for size in (100, 500, 1_000):
            add_case(
                builder, "exact_r2", "random-2d", size,
                points_file, points, reference_file, reference,
            )

    if "epsilon_multiplicative" in selected_operations:
        rng = np.random.default_rng(SEED + 3)
        points = 0.1 + rng.random((1_000, 5))
        reference = 0.1 + rng.random((1_000, 5))
        points_file = builder.matrix("epsilon-multiplicative-points", points)
        reference_file = builder.matrix("epsilon-multiplicative-reference", reference)
        for size in (200, 600, 1_000):
            add_case(
                builder, "epsilon_multiplicative", "random-positive-5d", size,
                points_file, points, reference_file, reference,
            )

    if "eaf" in selected_operations:
        definitions = (
            ("input1-2d", "input1.dat", (20, 50, 100)),
            ("ran-3d", "ran.1000pts.3d.10", (100, 500, 1_000)),
        )
        for dataset_name, filename, sizes in definitions:
            dataset = moocore.read_datasets(TESTSUITE_DATA / filename)
            set_ids = np.unique(dataset[:, -1])
            for size in sizes:
                rows_per_set = size // len(set_ids)
                selected_rows = np.vstack([
                    dataset[dataset[:, -1] == set_id][:rows_per_set]
                    for set_id in set_ids
                ])
                points = selected_rows[:, :-1]
                sets = selected_rows[:, -1]
                name = f"{dataset_name}-{size}"
                points_file = builder.matrix(f"eaf-{name}-points", points)
                sets_file = builder.matrix(f"eaf-{name}-sets", sets)
                add_case(
                    builder, "eaf", dataset_name, size,
                    points_file, points, sets_file=sets_file, sets=sets,
                )

    if {"eaf_difference_points", "eaf_difference_rectangles"} & selected_operations:
        rng = np.random.default_rng(SEED + 5)
        for size in (100, 500):
            left = rng.random((size, 2))
            right = np.clip(rng.random((size, 2)) + np.array([0.03, -0.03]), 0.0, 1.0)
            left_sets = np.arange(size) % 10 + 1
            right_sets = np.arange(size) % 10 + 1
            prefix = f"eaf-difference-random-2d-{size}"
            left_file = builder.matrix(f"{prefix}-left", left)
            left_sets_file = builder.matrix(f"{prefix}-left-sets", left_sets)
            right_file = builder.matrix(f"{prefix}-right", right)
            right_sets_file = builder.matrix(f"{prefix}-right-sets", right_sets)
            for operation, output_format in (
                ("eaf_difference_points", "POINTS"),
                ("eaf_difference_rectangles", "RECTANGLES"),
            ):
                if operation not in selected_operations:
                    continue
                add_case(
                    builder, operation, "random-2d", size,
                    left_file, left,
                    sets_file=left_sets_file, sets=left_sets,
                    right_points_file=right_file, right_points=right,
                    right_sets_file=right_sets_file, right_sets=right_sets,
                    output_format=output_format,
                )

    if "weighted_hypervolume" in selected_operations:
        rng = np.random.default_rng(SEED + 6)
        points = rng.random((1_000, 2))
        reference = np.ones(2)
        rectangle_count = 100
        rectangles = np.empty((rectangle_count, 5))
        for row in range(rectangle_count):
            rectangles[row] = (
                row / rectangle_count, 0.0, (row + 1) / rectangle_count, 1.0,
                0.5 + (row % 7) / 7.0,
            )
        points_file = builder.matrix("weighted-hv-points", points)
        reference_file = builder.matrix("weighted-hv-reference", reference.reshape(1, -1))
        rectangles_file = builder.matrix("weighted-hv-rectangles", rectangles)
        for size in (100, 500, 1_000):
            add_case(
                builder, "weighted_hypervolume", "random-2d", size,
                points_file, points, reference_file, reference,
                auxiliary_file=rectangles_file, auxiliary=rectangles,
            )

    if "hype_weighted_hypervolume" in selected_operations:
        rng = np.random.default_rng(SEED + 7)
        points = rng.random((100, 2))
        reference = np.ones(2)
        ideal = np.zeros(2)
        points_file = builder.matrix("hype-points", points)
        reference_file = builder.matrix("hype-reference", reference.reshape(1, -1))
        ideal_file = builder.matrix("hype-ideal", ideal.reshape(1, -1))
        for distribution, mu in (
            ("uniform", None),
            ("exponential", np.array([0.2])),
            ("point", np.array([0.5, 0.5])),
        ):
            mu_file = None if mu is None else builder.matrix(
                f"hype-{distribution}-mu", mu.reshape(1, -1)
            )
            add_case(
                builder, "hype_weighted_hypervolume", "random-2d", 100,
                points_file, points, reference_file, reference,
                ideal_file=ideal_file, ideal=ideal,
                mu_file=mu_file, mu=mu, distribution=distribution,
                samples=SAMPLES, seed=SEED,
            )

    manifest = {"schema_version": 1, "cases": [asdict(case) for case in builder.cases]}
    with (output / "manifest.json").open("w", encoding="utf-8") as destination:
        json.dump(manifest, destination, indent=2)
        destination.write("\n")
    return builder.cases


def load_cases(manifest_path: Path) -> list[Case]:
    with manifest_path.open(encoding="utf-8") as source:
        manifest = json.load(source)
    if manifest.get("schema_version") != 1:
        raise ValueError(f"Unsupported manifest schema in {manifest_path}")
    return [Case(**entry) for entry in manifest["cases"]]


def representative_cases(cases: list[Case]) -> list[Case]:
    selected: dict[tuple[Any, ...], Case] = {}
    for case in cases:
        dataset = case.dataset if case.operation in {
            "nondominated", "hypervolume_contributions"
        } else None
        key = (
            case.operation, case.objectives, case.method, case.keep_weakly,
            dataset, case.distribution, case.format,
        )
        current = selected.get(key)
        if current is None or case.size > current.size:
            selected[key] = case
    return sorted(selected.values(), key=lambda case: case.case_id)


def write_manifest(path: Path, cases: list[Case]) -> None:
    payload = {"schema_version": 1, "cases": [asdict(case) for case in cases]}
    with path.open("w", encoding="utf-8") as destination:
        json.dump(payload, destination, indent=2)
        destination.write("\n")


def case_data(case: Case, manifest_path: Path) -> tuple[Any, Any | None, Any, dict[str, Any]]:
    case_directory = manifest_path.parent / "cases"
    points = read_matrix((case_directory / case.points).resolve())[:case.size, :]
    reference = None
    if case.reference is not None:
        reference = read_matrix((case_directory / case.reference).resolve())
    oracle = read_matrix((case_directory / case.oracle).resolve())
    extras = {}
    for field in (
        "sets", "right_points", "right_sets", "auxiliary", "ideal", "mu"
    ):
        path = getattr(case, field)
        if path is not None:
            extras[field] = read_matrix((case_directory / path).resolve())
    return points, reference, oracle, extras


def relative_error(exact: float, approximation: float) -> float:
    if exact == 0.0:
        return abs(approximation)
    return abs(exact - approximation) / abs(exact)


def make_python_operation(
    case: Case, points: Any, reference_matrix: Any | None,
    extras: dict[str, Any] | None = None,
) -> tuple[str, Callable[[], Any]]:
    import moocore
    import numpy as np

    if extras is None:
        extras = {}

    if case.operation == "nondominated":
        return (
            "moocore",
            lambda: moocore.is_nondominated(
                points, maximise=True, keep_weakly=bool(case.keep_weakly)
            ),
        )

    if case.operation == "pareto_rank":
        return "moocore", lambda: moocore.pareto_rank(points)

    if case.operation == "hypervolume":
        reference = reference_matrix[0]
        indicator = moocore.Hypervolume(ref=reference)
        return "moocore", lambda: indicator(points)

    if case.operation == "hypervolume_contributions":
        reference = reference_matrix[0]
        return "moocore", lambda: moocore.hv_contributions(points, ref=reference)

    if case.operation == "hypervolume_approximation":
        reference = reference_matrix[0]
        method = case.method
        return f"moocore {method}", lambda: moocore.hv_approx(
            points, ref=reference, nsamples=SAMPLES,
            seed=SEED, method=method
        )

    if case.operation == "epsilon_additive":
        return "moocore", lambda: moocore.epsilon_additive(
            points, ref=reference_matrix
        )

    if case.operation == "igd_plus":
        return "moocore", lambda: moocore.igd_plus(
            points, ref=reference_matrix
        )

    if case.operation == "epsilon_multiplicative":
        return "moocore", lambda: moocore.epsilon_mult(
            points, ref=reference_matrix
        )

    if case.operation == "exact_r2":
        reference = reference_matrix[0]
        return "moocore", lambda: moocore.r2_exact(points, ref=reference)

    if case.operation == "eaf":
        sets = extras["sets"][:, 0]
        return "moocore", lambda: moocore.eaf(points, sets=sets)

    if case.operation in ("eaf_difference_points", "eaf_difference_rectangles"):
        left = np.column_stack((points, extras["sets"][:, 0]))
        right = np.column_stack((
            extras["right_points"], extras["right_sets"][:, 0]
        ))
        rectangles = case.operation == "eaf_difference_rectangles"
        return "moocore", lambda: moocore.eafdiff(
            left, right, rectangles=rectangles
        )

    if case.operation == "weighted_hypervolume":
        reference = reference_matrix[0]
        rectangles = extras["auxiliary"]
        return "moocore", lambda: moocore.whv_rect(
            points, rectangles, ref=reference
        )

    if case.operation == "hype_weighted_hypervolume":
        reference = reference_matrix[0]
        ideal = extras["ideal"][0]
        mu_matrix = extras.get("mu")
        mu = None if mu_matrix is None else (
            float(mu_matrix[0, 0]) if mu_matrix.size == 1 else mu_matrix.reshape(-1)
        )
        return "moocore", lambda: moocore.whv_hype(
            points, ref=reference, ideal=ideal, nsamples=case.samples,
            seed=case.seed, dist=case.distribution, mu=mu,
        )

    raise ValueError(f"Unknown operation: {case.operation}")


def verify_python_result(case: Case, implementation: str, value: Any, oracle: Any) -> None:
    import numpy as np

    if case.operation in ("nondominated", "pareto_rank"):
        expected = oracle[:, 0].astype(int)
        actual = np.asarray(value, dtype=int)
        np.testing.assert_array_equal(
            actual, expected,
            err_msg=f"{implementation} differs for {case.case_id}",
        )
        return
    if case.operation == "hypervolume_contributions":
        np.testing.assert_allclose(
            np.asarray(value), oracle[:, 0], rtol=1e-9, atol=1e-12,
            err_msg=f"{implementation} differs for {case.case_id}",
        )
        return
    if case.operation in ("eaf", "eaf_difference_points", "eaf_difference_rectangles"):
        actual = np.asarray(value)
        expected = np.asarray(oracle)
        if actual.shape != expected.shape:
            raise AssertionError(
                f"{implementation} shape differs for {case.case_id}: "
                f"expected={expected.shape}, actual={actual.shape}"
            )
        actual = actual[np.lexsort(actual.T[::-1])]
        expected = expected[np.lexsort(expected.T[::-1])]
        np.testing.assert_allclose(
            actual, expected, rtol=1e-9, atol=1e-12,
            err_msg=f"{implementation} differs for {case.case_id}",
        )
        return
    if case.operation == "hypervolume_approximation":
        actual = float(value)
        if not math.isfinite(actual):
            raise AssertionError(
                f"{implementation} returned a non-finite error for {case.case_id}"
            )
        if case.method == "DZ2019-MC":
            if relative_error(float(case.exact_hv), actual) > 0.05:
                raise AssertionError(
                    f"{implementation} exceeds 5% relative error for {case.case_id}"
                )
        else:
            np.testing.assert_allclose(
                actual, float(oracle[0, 0]), rtol=1e-9, atol=1e-12,
                err_msg=f"{implementation} differs for {case.case_id}",
            )
        return
    if case.operation == "hype_weighted_hypervolume":
        if relative_error(float(oracle[0, 0]), float(value)) > 0.05:
            raise AssertionError(
                f"{implementation} exceeds 5% relative error for {case.case_id}"
            )
        return
    np.testing.assert_allclose(
        float(value), float(oracle[0, 0]), rtol=1e-9, atol=1e-12,
        err_msg=f"{implementation} differs for {case.case_id}",
    )


def timed_batch(function: Callable[[], Any], repetitions: int) -> tuple[float, Any]:
    started = time.perf_counter_ns()
    value = None
    for _ in range(repetitions):
        value = function()
    return (time.perf_counter_ns() - started) / 1_000_000_000.0, value


def calibrate(function: Callable[[], Any]) -> tuple[int, Any, float]:
    repetitions = 1
    elapsed, value = timed_batch(function, repetitions)
    if elapsed > SLOW_CASE_SECONDS:
        return repetitions, value, elapsed
    while elapsed < ITERATION_SECONDS / 4.0:
        if elapsed <= 0.0:
            repetitions *= 10
        else:
            repetitions = max(
                repetitions + 1,
                int(math.ceil(repetitions * (ITERATION_SECONDS / 3.0) / elapsed)),
            )
        elapsed, value = timed_batch(function, repetitions)
    repetitions = max(1, int(round(repetitions * ITERATION_SECONDS / elapsed)))
    return repetitions, value, elapsed


def measure(function: Callable[[], Any]) -> tuple[list[float], Any, float]:
    repetitions, value, calibration_seconds = calibrate(function)
    if calibration_seconds > SLOW_CASE_SECONDS:
        return [], value, calibration_seconds
    for _ in range(WARMUP_ITERATIONS):
        elapsed, value = timed_batch(function, repetitions)
        repetitions = max(1, int(round(repetitions * ITERATION_SECONDS / elapsed)))
    samples = []
    for _ in range(MEASUREMENT_ITERATIONS):
        elapsed, value = timed_batch(function, repetitions)
        samples.append(elapsed * 1_000_000_000.0 / repetitions)
    return samples, value, calibration_seconds


def python_worker(manifest: Path, output: Path, fork: int) -> None:
    fail_missing_dependencies()
    cases = load_cases(manifest)
    records = []
    skipped = []
    disabled: set[tuple[str, str, str]] = set()
    progress(f"Python fork {fork + 1}/{FORKS}: starting {len(cases)} cases")
    for index, case in enumerate(cases, start=1):
        progress(
            f"Python fork {fork + 1}/{FORKS}: case {index}/{len(cases)} "
            f"{case.case_id}"
        )
        points, reference, oracle, extras = case_data(case, manifest)
        implementation, function = make_python_operation(
            case, points, reference, extras
        )
        group = (case.operation, case.dataset, implementation)
        if group in disabled:
            skipped.append({
                "case_id": case.case_id,
                "implementation": implementation,
                "reason": "earlier size exceeded the 10 second single-call limit",
            })
            continue
        value = function()
        verify_python_result(case, implementation, value, oracle)
        samples, value, calibration_seconds = measure(function)
        if not samples:
            disabled.add(group)
            skipped.append({
                "case_id": case.case_id,
                "implementation": implementation,
                "reason": (
                    "single call exceeded the 10 second limit "
                    f"({calibration_seconds:.3f} s)"
                ),
            })
            continue
        verify_python_result(case, implementation, value, oracle)
        records.append({
            "case_id": case.case_id,
            "operation": case.operation,
            "dataset": case.dataset,
            "size": case.size,
            "objectives": case.objectives,
            "method": case.method,
            "implementation": implementation,
            "runtime": "python",
            "fork": fork,
            "samples_ns_op": samples,
        })
    with output.open("w", encoding="utf-8") as destination:
        json.dump({"records": records, "skipped": skipped}, destination, indent=2)
        destination.write("\n")
    progress(f"Python fork {fork + 1}/{FORKS}: results written to {output}")


def run_python_workers(manifest: Path, raw_directory: Path) -> list[Path]:
    outputs = []
    for fork in range(FORKS):
        output = raw_directory / f"python-fork-{fork}.json"
        command = [
            sys.executable,
            str(Path(__file__).resolve()),
            "--worker",
            str(manifest),
            "--worker-output",
            str(output),
            "--fork-index",
            str(fork),
        ]
        subprocess.run(command, cwd=ROOT, check=True)
        outputs.append(output)
    return outputs


def benchmark_method(operation: str) -> str:
    return {
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
    }[operation]


def run_java_benchmarks(
    cases: list[Case], jar: Path, case_directory: Path, raw_directory: Path
) -> list[Path]:
    outputs = []
    iteration_time = f"{round(ITERATION_SECONDS * 1_000)}ms"
    for operation in OPERATIONS:
        case_ids = [case.case_id for case in cases if case.operation == operation]
        if not case_ids:
            continue
        progress(f"Java JMH: starting {operation} ({len(case_ids)} cases)")
        output = raw_directory / f"jmh-{operation}.json"
        benchmark = (
            "^es\\.urjc\\.etsii\\.grafo\\.benchmarks\\."
            "CrossLanguageMoocoreBenchmark\\."
            + benchmark_method(operation)
            + "$"
        )
        command = [
            "java",
            "--add-modules",
            "jdk.incubator.vector",
            "-jar",
            str(jar.resolve()),
            benchmark,
            "-p",
            "selectedCaseId=" + ",".join(case_ids),
            "-f",
            str(FORKS),
            "-wi",
            str(WARMUP_ITERATIONS),
            "-i",
            str(MEASUREMENT_ITERATIONS),
            "-w",
            iteration_time,
            "-r",
            iteration_time,
            "-rf",
            "json",
            "-rff",
            str(output.resolve()),
            "-jvmArgsPrepend",
            "--add-modules=jdk.incubator.vector",
        ]
        environment = os.environ.copy()
        environment["MOOCORE_CROSS_LANGUAGE_CASES"] = str(case_directory.resolve())
        subprocess.run(command, cwd=ROOT, check=True, env=environment)
        outputs.append(output)
    return outputs


def score_multiplier(unit: str) -> float:
    return {
        "ns/op": 1.0,
        "us/op": 1_000.0,
        "µs/op": 1_000.0,
        "ms/op": 1_000_000.0,
        "s/op": 1_000_000_000.0,
    }[unit]


def parse_jmh(paths: list[Path], cases: dict[str, Case]) -> list[dict[str, Any]]:
    records = []
    for path in paths:
        with path.open(encoding="utf-8") as source:
            results = json.load(source)
        for result in results:
            case_id = result["params"]["selectedCaseId"]
            case = cases[case_id]
            metric = result["primaryMetric"]
            multiplier = score_multiplier(metric["scoreUnit"])
            raw_data = metric.get("rawData")
            if not raw_data:
                raw_data = [[metric["score"]]]
            for fork, samples in enumerate(raw_data):
                records.append({
                    "case_id": case_id,
                    "operation": case.operation,
                    "dataset": case.dataset,
                    "size": case.size,
                    "objectives": case.objectives,
                    "method": case.method,
                    "implementation": (
                        f"mork-moocore {case.method}"
                        if case.method else "mork-moocore"
                    ),
                    "runtime": "java",
                    "fork": fork,
                    "samples_ns_op": [float(value) * multiplier for value in samples],
                })
    return records


def aggregate(records: list[dict[str, Any]]) -> list[dict[str, Any]]:
    groups: dict[tuple[str, str, str], dict[str, Any]] = {}
    for record in records:
        key = (record["case_id"], record["implementation"], record["runtime"])
        if key not in groups:
            groups[key] = {
                field: record.get(field)
                for field in (
                    "case_id", "operation", "dataset", "size", "objectives",
                    "method", "implementation", "runtime"
                )
            }
            groups[key]["samples_ns_op"] = []
        groups[key]["samples_ns_op"].extend(record["samples_ns_op"])
    results = []
    for group in groups.values():
        samples = group.pop("samples_ns_op")
        group["mean_ns_op"] = statistics.fmean(samples)
        group["stdev_ns_op"] = statistics.stdev(samples) if len(samples) > 1 else 0.0
        group["sample_count"] = len(samples)
        results.append(group)
    means = {
        (result["case_id"], result["implementation"]): result["mean_ns_op"]
        for result in results
    }
    for result in results:
        baseline = (
            f"moocore {result['method']}" if result["method"] else "moocore"
        )
        baseline_mean = means.get((result["case_id"], baseline))
        result["ratio_to_moocore"] = (
            result["mean_ns_op"] / baseline_mean if baseline_mean else None
        )
    results.sort(key=lambda item: (
        item["operation"], item["dataset"], item["size"], item["implementation"]
    ))
    return results


def command_output(command: list[str]) -> str | None:
    try:
        result = subprocess.run(
            command, cwd=ROOT, check=True, capture_output=True, text=True
        )
        return (result.stdout + result.stderr).strip()
    except (OSError, subprocess.CalledProcessError):
        return None


def distribution_versions() -> dict[str, str]:
    versions = {}
    for distribution in REQUIRED_IMPORTS:
        try:
            versions[distribution] = importlib.metadata.version(distribution)
        except importlib.metadata.PackageNotFoundError:
            versions[distribution] = "unknown"
    return versions


def environment_metadata(jar: Path) -> dict[str, Any]:
    import cpuinfo

    return {
        "timestamp_utc": time.strftime("%Y-%m-%dT%H:%M:%SZ", time.gmtime()),
        "command": sys.argv,
        "os": platform.platform(),
        "machine": platform.machine(),
        "cpu": cpuinfo.get_cpu_info().get("brand_raw", "unknown"),
        "logical_cpus": os.cpu_count(),
        "python": sys.version,
        "python_executable": sys.executable,
        "packages": distribution_versions(),
        "java": command_output(["java", "-version"]),
        "compiler": command_output(["cc", "--version"]),
        "benchmark_jar": str(jar.resolve()),
        "repository_revision": command_output(["git", "rev-parse", "HEAD"]),
        "repository_status": command_output(["git", "status", "--short"]),
        "moocore_revision": command_output(["git", "-C", "moocore", "rev-parse", "HEAD"]),
        "testsuite_revision": command_output(["git", "-C", "testsuite", "rev-parse", "HEAD"]),
        "measurement": {
            "forks": FORKS,
            "warmup_iterations": WARMUP_ITERATIONS,
            "measurement_iterations": MEASUREMENT_ITERATIONS,
            "iteration_seconds": ITERATION_SECONDS,
        },
    }


def write_csv(path: Path, results: list[dict[str, Any]]) -> None:
    fields = [
        "operation", "dataset", "size", "objectives", "method",
        "implementation", "runtime", "mean_ns_op", "stdev_ns_op",
        "sample_count", "ratio_to_moocore", "case_id",
    ]
    with path.open("w", newline="", encoding="utf-8") as destination:
        writer = csv.DictWriter(destination, fieldnames=fields)
        writer.writeheader()
        for result in results:
            writer.writerow({field: result.get(field) for field in fields})


def write_markdown(
    path: Path,
    results: list[dict[str, Any]],
    skipped: list[dict[str, Any]],
    environment: dict[str, Any],
) -> None:
    lines = [
        "# moocore cross-language benchmark report",
        "",
        f"Generated: `{environment['timestamp_utc']}`",
        "",
        "Times are native public API execution in ns/op; lower is better. Input loading,",
        "conversion, process startup, and JVM startup are excluded.",
        "",
        "| Operation | Dataset | Size | Implementation | Mean ns/op | Stddev | vs moocore |",
        "|---|---|---:|---|---:|---:|---:|",
    ]
    for result in results:
        ratio = result["ratio_to_moocore"]
        ratio_text = "" if ratio is None else f"{ratio:.3f}×"
        lines.append(
            f"| {result['operation']} | {result['dataset']} | {result['size']} "
            f"| {result['implementation']} | {result['mean_ns_op']:.3f} "
            f"| {result['stdev_ns_op']:.3f} | {ratio_text} |"
        )
    if skipped:
        lines.extend(["", "## Skipped slow cases", ""])
        for item in skipped:
            lines.append(
                f"- `{item['case_id']}` / `{item['implementation']}`: {item['reason']}"
            )
    lines.extend([
        "",
        "## Environment",
        "",
        f"- CPU: `{environment['cpu']}`",
        f"- OS: `{environment['os']}`",
        f"- Python: `{platform.python_version()}`",
        f"- Java: `{(environment['java'] or 'unavailable').splitlines()[0]}`",
        f"- Repository: `{environment['repository_revision']}`",
        f"- moocore submodule: `{environment['moocore_revision']}`",
        f"- testsuite submodule: `{environment['testsuite_revision']}`",
        "",
    ])
    path.write_text("\n".join(lines), encoding="utf-8")


def write_plots(output: Path, results: list[dict[str, Any]]) -> None:
    import matplotlib

    matplotlib.use("Agg")
    import matplotlib.pyplot as plt

    groups: dict[tuple[str, str], list[dict[str, Any]]] = {}
    for result in results:
        groups.setdefault((result["operation"], result["dataset"]), []).append(result)
    plot_directory = output / "plots"
    plot_directory.mkdir(parents=True, exist_ok=True)
    for (operation, dataset), values in groups.items():
        by_implementation: dict[str, list[dict[str, Any]]] = {}
        for value in values:
            by_implementation.setdefault(value["implementation"], []).append(value)
        figure, axis = plt.subplots()
        for implementation, measurements in sorted(by_implementation.items()):
            measurements.sort(key=lambda item: item["size"])
            axis.plot(
                [item["size"] for item in measurements],
                [item["mean_ns_op"] / 1_000_000.0 for item in measurements],
                marker="o",
                label=implementation,
            )
        axis.set_xscale("log")
        axis.set_yscale("log")
        axis.set_xlabel("Points")
        axis.set_ylabel("Time (ms/op)")
        axis.set_title(f"{operation}: {dataset}")
        axis.grid(True)
        axis.legend(fontsize="small")
        figure.tight_layout()
        figure.savefig(plot_directory / f"{safe_id(operation)}-{safe_id(dataset)}.png")
        plt.close(figure)


def merge_results(
    output: Path,
    cases: list[Case],
    python_paths: list[Path],
    jmh_paths: list[Path],
    jar: Path,
) -> None:
    records = []
    skipped = []
    for path in python_paths:
        with path.open(encoding="utf-8") as source:
            worker = json.load(source)
        records.extend(worker["records"])
        skipped.extend(worker["skipped"])
    case_map = {case.case_id: case for case in cases}
    records.extend(parse_jmh(jmh_paths, case_map))
    results = aggregate(records)
    environment = environment_metadata(jar)
    payload = {
        "schema_version": 1,
        "environment": environment,
        "results": results,
        "skipped": skipped,
    }
    with (output / "results.json").open("w", encoding="utf-8") as destination:
        json.dump(payload, destination, indent=2)
        destination.write("\n")
    with (output / "environment.json").open("w", encoding="utf-8") as destination:
        json.dump(environment, destination, indent=2)
        destination.write("\n")
    write_csv(output / "results.csv", results)
    write_markdown(output / "report.md", results, skipped, environment)
    write_plots(output, results)


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--operations", nargs="+", choices=OPERATIONS, default=list(OPERATIONS),
        help="Benchmark families to run; all are required by default.",
    )
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    parser.add_argument("--java-jar", type=Path, default=DEFAULT_JAR)
    parser.add_argument(
        "--quick", action="store_true",
        help="Run a fixed representative matrix with short iterations.",
    )
    parser.add_argument(
        "--reuse-python", type=Path,
        help="Reuse Python worker results from a compatible checkpoint.",
    )
    parser.add_argument("--worker", type=Path, help=argparse.SUPPRESS)
    parser.add_argument("--worker-output", type=Path, help=argparse.SUPPRESS)
    parser.add_argument("--fork-index", type=int, help=argparse.SUPPRESS)
    parser.add_argument("--check-dependencies", action="store_true", help=argparse.SUPPRESS)
    return parser.parse_args()


def main() -> None:
    arguments = parse_arguments()
    if arguments.quick:
        os.environ["MOOCORE_BENCHMARK_QUICK"] = "1"
        enable_quick_mode()
    if arguments.check_dependencies:
        fail_missing_dependencies()
        return
    if arguments.worker is not None:
        if arguments.worker_output is None or arguments.fork_index is None:
            raise SystemExit("Internal worker arguments are incomplete")
        python_worker(arguments.worker, arguments.worker_output, arguments.fork_index)
        return
    fail_missing_dependencies()
    if not arguments.java_jar.is_file():
        raise SystemExit(
            f"Java benchmark JAR not found: {arguments.java_jar}\n"
            "Run: ./mvnw -pl benchmarks -am -DskipTests package\n"
            "or invoke benchmarks/cross-language/setup_environment.py"
        )
    if not TESTSUITE_DATA.is_dir():
        raise SystemExit(
            "Pinned testsuite is not initialized. Run:\n"
            "git submodule update --init moocore testsuite"
        )
    output = arguments.output.resolve()
    raw_directory = output / "raw"
    raw_directory.mkdir(parents=True, exist_ok=True)
    manifest = output / "manifest.json"
    if arguments.reuse_python is not None and manifest.is_file():
        progress(f"Reusing prepared cases from {output}")
        enabled = set(arguments.operations)
        cases = [case for case in load_cases(manifest) if case.operation in enabled]
        if arguments.quick:
            cases = representative_cases(cases)
        write_manifest(manifest, cases)
    else:
        progress("Preparing shared inputs and Python correctness oracles")
        cases = prepare_cases(output, set(arguments.operations))
        if arguments.quick:
            cases = representative_cases(cases)
            write_manifest(manifest, cases)
    progress(f"Prepared {len(cases)} benchmark cases")
    if arguments.reuse_python is None:
        python_paths = run_python_workers(manifest, raw_directory)
    else:
        source = arguments.reuse_python.resolve() / "raw"
        python_paths = sorted(source.glob("python-fork-*.json"))
        if not python_paths:
            raise SystemExit(f"No Python worker results found in {source}")
        progress(f"Reusing Python measurements from {source.parent}")
    jmh_paths = run_java_benchmarks(
        cases, arguments.java_jar, output / "cases", raw_directory
    )
    progress("Merging Python and Java results")
    merge_results(output, cases, python_paths, jmh_paths, arguments.java_jar)
    progress(f"Cross-language report: {output / 'report.md'}")


if __name__ == "__main__":
    main()
