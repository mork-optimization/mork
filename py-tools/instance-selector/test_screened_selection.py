"""Selection stays valid when runtime screening leaves a small or degenerate pool."""
import importlib.util
from pathlib import Path
import tempfile
import unittest

import numpy as np
import pandas as pd

spec = importlib.util.spec_from_file_location("instance_selector", Path(__file__).with_name("instance-selector.py"))
selector = importlib.util.module_from_spec(spec)
spec.loader.exec_module(selector)


class ScreenedSelectionTest(unittest.TestCase):
    def test_selected_tokens_preserve_logical_paths_through_symlinks(self):
        with tempfile.TemporaryDirectory() as directory:
            real = Path(directory, "real")
            real.mkdir()
            alias = Path(directory, "alias")
            alias.symlink_to(real, target_is_directory=True)
            token = str(alias / "instances.zip") + "!nested/instance.txt"
            self.assertEqual(selector.absolute_load_token(token), token)
            self.assertEqual(selector.absolute_load_token(str(alias / "instance.txt")), str(alias / "instance.txt"))

    def select(self, count, properties):
        data = pd.DataFrame({"id": ["same-id"] * count,
                             "path": [f"/data/archive.zip!group-{i}/instance.txt" for i in range(count)],
                             **properties})
        with tempfile.TemporaryDirectory() as directory:
            prepared = selector.prepare_df(data)
            reduced = selector.save_pca(prepared, directory)
            model, clusters = selector.elbow_method(reduced, directory, random_state=1234)
            labels = model.predict(reduced)
            rows = selector.select_instances(data, reduced, model, labels, clusters, .15)
            selector.write_selected_manifest(data, rows, directory)
            tokens = Path(directory, "selected_instances.index").read_text().splitlines()
            self.assertEqual(len(tokens), int(np.ceil(.15 * count)))
            self.assertEqual(len(tokens), len(set(tokens)))
            self.assertTrue(set(tokens) <= set(data["path"]))
            return rows

    def test_one_instance_can_be_selected_for_training_and_full_testing(self):
        self.select(1, {"n": [10]})

    def test_two_instances_select_the_preliminary_fraction(self):
        self.select(2, {"n": [10, 20]})

    def test_small_pool_bounds_elbow_search(self):
        self.select(5, {"n": list(range(5)), "k": [2, 2, 3, 3, 4]})

    def test_constant_or_missing_features_have_deterministic_one_cluster_fallback(self):
        first = self.select(20, {"n": [7] * 20, "optional": [float("nan")] * 20})
        second = self.select(20, {"n": [7] * 20, "optional": [float("nan")] * 20})
        self.assertEqual(first, second)
        self.select(3, {})


if __name__ == "__main__":
    unittest.main()
