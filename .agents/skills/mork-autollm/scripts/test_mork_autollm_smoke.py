"""Opt-in packaged-backend contract test; creates drafts but runs no experiments."""
import json
import os
from pathlib import Path
import re
import signal
import subprocess
import sys
import tempfile
import time
import unittest
from urllib.request import urlopen
import zipfile


@unittest.skipUnless(os.environ.get("MORK_AUTOLLM_TEST_JAR") and os.environ.get("MORK_AUTOLLM_TEST_CWD"),
                     "Set MORK_AUTOLLM_TEST_JAR and MORK_AUTOLLM_TEST_CWD for the packaged-backend smoke test")
class PackagedBackendTest(unittest.TestCase):
    def test_owned_backend_lifecycle_watch_diagnostics_and_dashboard(self):
        jar = Path(os.environ["MORK_AUTOLLM_TEST_JAR"]).resolve()
        cwd = Path(os.environ["MORK_AUTOLLM_TEST_CWD"]).resolve()
        repo = Path(subprocess.check_output(["git", "-C", str(cwd), "rev-parse", "--show-toplevel"], text=True).strip())
        helper = Path(__file__).with_name("mork_autollm.py")
        with tempfile.TemporaryDirectory(prefix="mork-autollm-client-smoke-") as temporary:
            workspace = Path(temporary)
            command = [sys.executable, "-B", str(helper), "--workspace", str(workspace)]

            def run(*arguments):
                result = subprocess.run([*command, *arguments], capture_output=True, text=True, timeout=120)
                log = workspace / "backend.log"
                details = log.read_text(errors="replace")[-12000:] if result.returncode and log.exists() else ""
                self.assertEqual(result.returncode, 0, result.stderr + "\n" + result.stdout + "\n" + details)
                return result.stdout

            try:
                backend = json.loads(run("backend", "--jar", str(jar), "--cwd", str(cwd), "--port", "0", "--timeout", "90"))
                again = json.loads(run("backend", "--jar", str(jar), "--cwd", str(cwd), "--port", "0"))
                self.assertEqual(again["pid"], backend["pid"])
                self.assertEqual(backend["apiVersion"], 2)
                sota, instance = workspace / "sota.json", workspace / "fixture.txt"
                sota.write_text('{"$component":"DraftOnlyFixture"}')
                instance.write_text("draft input; no workload consumes this fixture\n")
                overrides = workspace / "overrides.json"
                overrides.write_text(json.dumps({"project": {"repository": str(repo), "artifact": str(jar.relative_to(repo))},
                    "execution": {"workers": 32}, "sotaFile": str(sota), "trainingPath": str(instance), "comparisonPath": str(instance)}))
                configuration = workspace / "configuration.yml"
                run("config", "--overrides", str(overrides), "--spring", "--output", str(configuration))
                self.assertTrue(json.loads(run("campaign", "validate", "--config", str(configuration)))["valid"])
                campaign = json.loads(run("campaign", "create", "--config", str(configuration)))
                identifier = campaign["id"]
                self.assertEqual(campaign["status"], "DRAFT")
                self.assertEqual(campaign["config"]["execution"]["workers"], 32)
                self.assertEqual(json.loads(run("campaign", "get", "--id", identifier))["id"], identifier)
                self.assertTrue(json.loads(run("campaign", "summary", "--id", identifier)))
                watch = [json.loads(line) for line in run("watch", "--once", "--json").splitlines()]
                self.assertEqual(watch[0]["id"], identifier)
                self.assertEqual(watch[0]["status"], "DRAFT")
                self.assertEqual(watch[-1]["reason"], "once")
                selection = json.loads(run("screening", "create", "--config", str(configuration), "--input", str(instance)))
                self.assertEqual(selection["status"], "DRAFT")
                self.assertEqual(json.loads(run("screening", "get", "--id", selection["id"]))["id"], selection["id"])
                self.assertIn("DRAFT", run("watch", "--screening", selection["id"], "--once"))
                self.assertEqual(json.loads(run("screening", "cancel", "--id", selection["id"]))["status"], "CANCELLED")
                self.assertEqual(json.loads(run("screening", "list"))["jobs"][0]["status"], "CANCELLED")
                self.assertEqual(json.loads(run("status", "--campaign", identifier))["campaign"]["status"], "DRAFT")
                archive = workspace / "diagnostics.zip"
                run("diagnose", "--campaign", identifier, "--output", str(archive))
                with zipfile.ZipFile(archive) as bundle:
                    self.assertTrue({"capabilities.json", "campaign.json", "summary.json", "artifact-index.json"}.issubset(bundle.namelist()))
                expected_html = (repo / "template/src/main/resources/static/index.html").read_text()
                asset = re.search(r'src="([^\"]*main[^\"]*\.js)"', expected_html).group(1)
                with urlopen(backend["baseUrl"], timeout=20) as response:
                    self.assertIn(asset, response.read().decode())
                with urlopen(backend["baseUrl"] + "/" + asset, timeout=20) as response:
                    self.assertEqual(response.status, 200)
                self.assertFalse((workspace / identifier / "trials").exists())
            finally:
                launch = workspace / "backend-launch.json"
                if launch.exists():
                    record = json.loads(launch.read_text())
                    pid = record["pid"]
                    process = subprocess.run(["ps", "-p", str(pid), "-o", "command="], capture_output=True, text=True)
                    if str(workspace) in process.stdout and str(jar) in process.stdout:
                        os.kill(pid, signal.SIGTERM)
                        deadline = time.monotonic() + 10
                        while time.monotonic() < deadline:
                            if subprocess.run(["ps", "-p", str(pid)], capture_output=True).returncode != 0:
                                break
                            time.sleep(0.1)
                        else:
                            self.fail(f"Owned backend {pid} did not stop after SIGTERM")


if __name__ == "__main__":
    unittest.main()
