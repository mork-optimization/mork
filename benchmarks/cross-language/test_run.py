# SPDX-License-Identifier: MIT

import importlib.util
import json
import sys
import tempfile
import unittest
from pathlib import Path

import numpy as np


SCRIPT = Path(__file__).with_name("run.py")
SPEC = importlib.util.spec_from_file_location("cross_language_run", SCRIPT)
benchmark = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = benchmark
SPEC.loader.exec_module(benchmark)


class CrossLanguageBenchmarkTest(unittest.TestCase):

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
