# SPDX-License-Identifier: MIT

import importlib.util
import json
import sys
import tempfile
import unittest
from unittest import mock
from pathlib import Path

import numpy as np


SCRIPT = Path(__file__).with_name("run.py")
SPEC = importlib.util.spec_from_file_location("cross_language_run", SCRIPT)
benchmark = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = benchmark
SPEC.loader.exec_module(benchmark)


class CrossLanguageBenchmarkTest(unittest.TestCase):

    def test_requires_only_moocore_and_reporting_packages(self):
        self.assertEqual(
            {
                "numpy": "numpy",
                "matplotlib": "matplotlib",
                "py-cpuinfo": "cpuinfo",
                "moocore": "moocore",
            },
            benchmark.REQUIRED_IMPORTS,
        )

    def test_approximation_operation_returns_raw_moocore_value(self):
        case = benchmark.Case(
            case_id="approximation",
            operation="hypervolume_approximation",
            dataset="data",
            size=2,
            objectives=2,
            points="points.bin",
            method="DZ2019-MC",
            exact_hv=1.0,
        )
        points = np.array([[0.2, 0.8], [0.8, 0.2]])
        reference = np.array([[1.0, 1.0]])
        with mock.patch("moocore.hv_approx", return_value=0.75) as hv_approx:
            implementation, operation = benchmark.make_python_operation(
                case, points, reference
            )
            self.assertEqual("moocore DZ2019-MC", implementation)
            self.assertEqual(0.75, operation())
        hv_approx.assert_called_once()
        arguments, keywords = hv_approx.call_args
        self.assertIs(points, arguments[0])
        np.testing.assert_array_equal(reference[0], keywords.pop("ref"))
        self.assertEqual(
            {
                "nsamples": benchmark.SAMPLES,
                "seed": benchmark.SEED,
                "method": "DZ2019-MC",
            },
            keywords,
        )

    def test_hypervolume_operation_binds_points(self):
        case = benchmark.Case(
            case_id="hypervolume",
            operation="hypervolume",
            dataset="data",
            size=2,
            objectives=2,
            points="points.bin",
        )
        points = np.array([[0.2, 0.8], [0.8, 0.2]])
        reference = np.array([[1.0, 1.0]])
        implementation, operation = benchmark.make_python_operation(
            case, points, reference
        )
        self.assertEqual("moocore", implementation)
        self.assertAlmostEqual(0.28, operation())

    def test_matrix_round_trip(self):
        values = np.array([[1.25, -2.5], [3.75, 4.0]])
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "matrix.bin"
            benchmark.write_matrix(path, values)
            np.testing.assert_array_equal(values, benchmark.read_matrix(path))

    def test_aggregate_calculates_ratio_to_python_moocore(self):
        common = {
            "case_id": "case",
            "operation": "hypervolume",
            "dataset": "data",
            "size": 100,
            "objectives": 3,
            "method": None,
            "fork": 0,
        }
        records = [
            common | {
                "implementation": "moocore",
                "runtime": "python",
                "samples_ns_op": [10.0, 12.0],
            },
            common | {
                "implementation": "mork-moocore",
                "runtime": "java",
                "samples_ns_op": [20.0, 24.0],
            },
        ]
        results = benchmark.aggregate(records)
        java = next(result for result in results if result["runtime"] == "java")
        self.assertEqual(2.0, java["ratio_to_moocore"])

    def test_parse_jmh_normalizes_microseconds(self):
        case = benchmark.Case(
            case_id="case",
            operation="hypervolume",
            dataset="data",
            size=100,
            objectives=3,
            points="points.bin",
        )
        payload = [{
            "benchmark": "example.hypervolume",
            "params": {"selectedCaseId": "case"},
            "primaryMetric": {
                "score": 2.0,
                "scoreUnit": "us/op",
                "rawData": [[1.5, 2.5]],
            },
        }]
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "jmh.json"
            path.write_text(json.dumps(payload), encoding="utf-8")
            records = benchmark.parse_jmh([path], {"case": case})
        self.assertEqual([1500.0, 2500.0], records[0]["samples_ns_op"])


if __name__ == "__main__":
    unittest.main()
