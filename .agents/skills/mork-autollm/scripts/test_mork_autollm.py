import contextlib
import importlib.util
import io
import json
from pathlib import Path
from types import SimpleNamespace
import tempfile
import time
import unittest
import zipfile
from unittest.mock import patch

spec = importlib.util.spec_from_file_location("mork_autollm", Path(__file__).with_name("mork_autollm.py"))
client = importlib.util.module_from_spec(spec)
spec.loader.exec_module(client)


class OperatorClientTest(unittest.TestCase):
    def test_configuration_keeps_worker_override_and_omits_spring_nulls(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            overrides = root / "overrides.json"
            overrides.write_text(json.dumps({"execution": {"workers": 32, "ssh": {"host": "server", "configFile": None}}}))
            args = SimpleNamespace(overrides=overrides, output=root / "remote.yml", spring=True, workspace=root)
            defaults = {"workspace": str(root), "execution": {"workers": -1, "ssh": {"javaExecutable": "java", "configFile": None}}}
            with patch.object(client, "request", side_effect=[defaults, {"trials": 10}]):
                result = client.configuration(args, "http://localhost")
            config = json.loads(Path(result["configurationPath"]).read_text())["autollm"]
            self.assertEqual(config["execution"]["workers"], 32)
            self.assertNotIn("configFile", config["execution"]["ssh"])
            self.assertEqual(config["execution"]["ssh"]["javaExecutable"], "java")
            self.assertEqual(defaults["execution"]["workers"], -1)

    def test_rejects_backend_without_features_or_wrong_workspace(self):
        with patch.object(client, "request", return_value={"mode": "AUTOLLM", "apiVersion": 1}):
            with self.assertRaisesRegex(RuntimeError, "capabilities"):
                client.capabilities("http://localhost")
        info = {"mode": "AUTOLLM", "apiVersion": 3, "checkpointVersion": 3, "runnerProtocol": 3, "workspace": "/other", "features": list(client.FEATURES)}
        with patch.object(client, "request", return_value=info):
            with self.assertRaisesRegex(RuntimeError, "workspace"):
                client.capabilities("http://localhost", "/requested")

    def test_reuses_only_matching_artifact_without_launching(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            jar = root / "app.jar"
            jar.write_bytes(b"frozen executable")
            args = SimpleNamespace(workspace=root, jar=jar, cwd=root, url="http://localhost")
            info = {"mode": "AUTOLLM", "apiVersion": 3, "checkpointVersion": 3, "runnerProtocol": 3, "workspace": str(root), "workingDirectory": str(root), "features": list(client.FEATURES), "artifactSha256": client.fingerprint(jar)}
            with patch.object(client, "request", return_value=info), patch.object(client.subprocess, "Popen") as launch:
                self.assertEqual(client.backend(args), info)
                launch.assert_not_called()
            info["artifactSha256"] = "different"
            with patch.object(client, "request", return_value=info), patch.object(client.subprocess, "Popen") as launch:
                with self.assertRaisesRegex(RuntimeError, "incompatible"):
                    client.backend(args)
                launch.assert_not_called()

    def test_status_reads_saved_work_without_mutation(self):
        campaign = {"id": "saved", "status": "NEEDS_ATTENTION", "trials": [{"number": 1, "status": "BLOCKED", "repairTurns": 0}]}
        args = SimpleNamespace(workspace="/work", campaign=None, screening=None, timeout=10)
        with patch.object(client, "request", side_effect=[{"active": "", "campaigns": [campaign]}, {"active": "", "jobs": []}, campaign, {"status": "BLOCKED"}, {}]) as request:
            result = client.status(args, "http://localhost")
        self.assertEqual(result["campaign"]["status"], "NEEDS_ATTENTION")
        self.assertTrue(all(len(call.args) == 2 for call in request.call_args_list))

    def test_backend_waits_for_ready_mode_and_starts_without_workloads(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            jar = root / "app.jar"
            jar.write_bytes(b"executable")
            args = SimpleNamespace(workspace=root, jar=jar, cwd=root, url=None, java="java", port=0, timeout=10)
            ready = {"pid": 12345, "mode": "AUTOLLM", "apiVersion": 3, "checkpointVersion": 3, "runnerProtocol": 3, "workspace": str(root), "workingDirectory": str(root), "features": list(client.FEATURES), "artifactSha256": client.fingerprint(jar)}
            launching = dict(ready, mode="STARTING")
            with patch.object(client, "request", side_effect=[RuntimeError("offline"), launching, ready]), patch.object(client.socket, "socket") as socket, patch.object(client.subprocess, "Popen") as launch, patch.object(client.time, "sleep"):
                socket.return_value.__enter__.return_value.getsockname.return_value = ("127.0.0.1", 18081)
                launch.return_value.pid = 12345
                launch.return_value.poll.return_value = None
                self.assertEqual(client.backend(args), ready)
                self.assertIn("--instances.preload=false", launch.call_args.args[0])
                self.assertIn("--solver.benchmark=false", launch.call_args.args[0])
                self.assertTrue(launch.call_args.kwargs["start_new_session"])
                self.assertEqual(launch.call_args.kwargs["stdin"], client.subprocess.DEVNULL)

    def test_explicit_cursor_reads_one_bounded_page_without_draining_the_file(self):
        args = SimpleNamespace(cursor=0, screening=None, campaign="saved", path="stdout.log", follow=False, lines=500, timeout=10)
        pages = [{"text": "α", "nextCursor": 2, "more": True, "reset": False}, {"text": "β\n", "nextCursor": 5, "more": False, "reset": False}]
        output = io.StringIO()
        with patch.object(client, "request", side_effect=pages) as request, contextlib.redirect_stdout(output), contextlib.redirect_stderr(io.StringIO()):
            client.logs(args, "http://localhost")
        self.assertEqual(output.getvalue(), "α")
        request.assert_called_once()
        self.assertIn("maximumBytes=65536", request.call_args.args[1])

    def test_status_keeps_healthy_trials_when_another_progress_endpoint_fails(self):
        self.root = "/work"
        saved = {"id": "saved", "status": "RUNNING", "targets": {}, "trials": [
            {"number": 1, "targetId": "a", "status": "TUNING"}, {"number": 2, "targetId": "b", "status": "TUNING"}, {"number": 3, "status": "PENDING"}]}
        def response(base, path, **kwargs):
            if path == "/campaigns": return {"active": "saved", "campaigns": [saved]}
            if path == "/screenings": raise RuntimeError("screenings offline")
            if path == "/campaigns/saved": return saved
            if path.endswith("/1/progress"): raise RuntimeError("worker unavailable")
            if path.endswith("/2/progress"): return {"elapsedMillis": 3600000, "workload": {"budget": {"used": 2500, "maximum": 10000}}, "remote": {"lastSynchronizedAt": "now"}}
            if path.endswith("/summary"): raise RuntimeError("summary unavailable")
            self.fail(path)
        output = io.StringIO()
        with patch.object(client, "capabilities"), patch.object(client, "request", side_effect=response), contextlib.redirect_stdout(output):
            self.assertEqual(client.main(["--workspace", self.root, "status", "--table", "--campaign", "saved"]), 0)
        text = output.getvalue()
        for expected in ["PARTIAL STATUS", "2,500/10,000 (25.0%)", "1h 0m", "PENDING", "worker unavailable", "summary unavailable"]:
            self.assertIn(expected, text)

    def test_trial_recovery_and_requalification_send_only_explicit_saved_ids(self):
        self.root = "/work"
        _, _, request = self.cli(["trial", "actions", "--campaign", "saved", "--number", "9"], [{"trials": {"9": {"fail": {"allowed": True}}}}])
        self.assertEqual(request.call_args.args[1], "/campaigns/saved/recovery")
        _, _, request = self.cli(["trial", "fail", "--campaign", "saved", "--number", "9", "--reason", " Sampling exhausted "], [{}])
        request.assert_called_once_with("http://localhost", "/campaigns/saved/trials/9/fail", {"reason": "Sampling exhausted"}, timeout=30)
        _, _, request = self.cli(["trial", "resume", "--campaign", "saved", "--number", "3"], [{}])
        self.assertEqual(request.call_args.args[1:], ("/campaigns/saved/trials/3/resume", {}))
        _, _, request = self.cli(["target", "requalify", "--campaign", "saved", "--id", "a"], [{}])
        self.assertEqual(request.call_args.args[1:], ("/campaigns/saved/targets/a/requalify", {}))

    def test_default_logs_use_only_a_tail_and_follow_resumes_at_the_utf8_cursor(self):
        self.root = "/work"
        page = {"text": "recent α", "nextCursor": 123, "truncated": True, "lineCount": 1}
        _, output, request = self.cli(["logs", "--campaign", "saved", "--path", "stdout.log", "--tail", "100"], [page])
        self.assertEqual(output, "recent α")
        self.assertIn("/preview?", request.call_args.args[1]); self.assertIn("lines=100", request.call_args.args[1])
        args = SimpleNamespace(cursor=None, screening=None, campaign="saved", path="stdout.log", follow=True, lines=500, timeout=10)
        output = io.StringIO()
        with patch.object(client, "request", side_effect=[page, {"text": "€", "nextCursor": 126, "reset": False, "more": False}]) as request, patch.object(client.time, "sleep", side_effect=[None, KeyboardInterrupt]), contextlib.redirect_stdout(output), contextlib.redirect_stderr(io.StringIO()):
            with self.assertRaises(KeyboardInterrupt): client.logs(args, "http://localhost")
        self.assertEqual(output.getvalue(), "recent α€")
        self.assertIn("cursor=123", request.call_args.args[1])

    def test_follow_skips_backlogs_and_restarts_with_a_bounded_tail(self):
        args = SimpleNamespace(cursor=None, screening="screen", campaign=None, path="stdout.log", follow=True, lines=100, timeout=10)
        pages = [{"text": "old\n", "nextCursor": 10, "truncated": False},
                 {"text": "stale backlog", "nextCursor": 50, "reset": False, "more": True},
                 {"text": "latest\n", "nextCursor": 1000000, "truncated": True, "lineCount": 100}]
        output = io.StringIO()
        with patch.object(client, "request", side_effect=pages) as request, patch.object(client.time, "sleep", side_effect=[None, KeyboardInterrupt]), contextlib.redirect_stdout(output), contextlib.redirect_stderr(io.StringIO()):
            with self.assertRaises(KeyboardInterrupt): client.logs(args, "http://localhost")
        self.assertEqual(output.getvalue(), "old\nlatest\n")
        self.assertIn("/screenings/screen/preview?", request.call_args.args[1])

    def test_trial_diagnostics_filter_evidence_and_enforce_file_and_payload_limits(self):
        with tempfile.TemporaryDirectory() as self.root:
            root = Path(self.root); directory = "targets/a/trials/009"
            saved = {"id": "saved", "trials": [{"number": 9, "directory": directory, "targetId": "a", "problem": {"code": "SAMPLING", "message": "No valid sample"}}, {"number": 10}], "targets": {"a": {}, "b": {}}}
            artifacts = [{"path": directory + name, "size": 100} for name in ["/stdout.log", "/stderr.log", "/failure.json"]] + [{"path": "targets/b/trials/010/stdout.log", "size": 100}]
            def response(base, path, **kwargs):
                if path == "/campaigns/saved": return saved
                if path.partition('?')[0].endswith("/artifacts"):
                    self.assertIn("hashes=false", path); self.assertIn("trial=9", path)
                    return artifacts
                if path == "/capabilities" or path.endswith("/summary"): return {}
                if path.endswith("/progress"): raise RuntimeError("progress unavailable")
                if "/artifact?" in path: return {"code": "SAMPLING"}
                if "/preview?" in path: return {"text": "x" * 5000}
                self.fail(path)
            with patch.object(client, "capabilities"), patch.object(client, "request", side_effect=response) as request, contextlib.redirect_stdout(io.StringIO()):
                client.main(["--workspace", self.root, "diagnose", "--campaign", "saved", "--trial", "9", "--output", str(root / "diag.zip"), "--max-files", "2", "--max-bytes", "4096"])
            with zipfile.ZipFile(root / "diag.zip") as archive:
                summary = json.loads(archive.read("diagnostic-summary.json"))
                self.assertEqual(summary["trial"], 9); self.assertLessEqual(summary["payloadBytes"], 4096)
                self.assertEqual(summary["failureSummary"][0]["code"], "SAMPLING")
                self.assertIn("progress.json", summary["errors"])
                self.assertFalse(any("trials/010" in name for name in archive.namelist()))
                self.assertEqual(len(json.loads(archive.read("campaign.json"))["trials"]), 1)
            self.assertLessEqual(sum("/preview?" in call.args[1] or "/artifact?" in call.args[1] for call in request.call_args_list), 2)

    def cli(self, argv, responses):
        output = io.StringIO()
        with patch.object(client, "capabilities"), patch.object(client, "request", side_effect=responses) as request, contextlib.redirect_stdout(output):
            code = client.main(["--workspace", self.root, "--url", "http://localhost", *argv])
        return code, output.getvalue(), request

    def configuration_file(self, root, spring=False):
        value = {"workspace": str(root), "execution": {"workers": 32, "ssh": {"configFile": None}}}
        path = root / ("config.yml" if spring else "config.json")
        path.write_text(json.dumps({"autollm": value} if spring else value))
        return str(path)

    def test_campaign_validation_reports_invalid_configuration_with_nonzero_exit(self):
        with tempfile.TemporaryDirectory() as self.root:
            path = self.configuration_file(Path(self.root), spring=True)
            code, output, request = self.cli(["campaign", "validate", "--config", path], [{"valid": False, "errors": ["missing SOTA"]}])
        self.assertEqual(code, 2)
        self.assertEqual(json.loads(output)["errors"], ["missing SOTA"])
        self.assertEqual(request.call_args.args[1], "/validate")
        self.assertEqual(request.call_args.args[2]["execution"]["workers"], 32)
        self.assertNotIn("configFile", request.call_args.args[2]["execution"]["ssh"])

    def test_create_returns_same_saved_draft_without_starting_it(self):
        with tempfile.TemporaryDirectory() as self.root:
            path = self.configuration_file(Path(self.root))
            code, output, request = self.cli(["campaign", "create", "--config", path], [{"id": "saved", "status": "DRAFT"}])
        self.assertEqual(code, 0)
        self.assertEqual(json.loads(output), {"id": "saved", "status": "DRAFT"})
        request.assert_called_once()
        self.assertEqual(request.call_args.args[1], "/campaigns")

    def test_configuration_workspace_mismatch_does_not_create_work(self):
        with tempfile.TemporaryDirectory() as self.root:
            path = Path(self.root) / "config.json"
            path.write_text('{"workspace":"/another-workspace"}')
            with self.assertRaisesRegex(RuntimeError, "workspace"), patch.object(client, "capabilities"), patch.object(client, "request") as request:
                client.main(["--workspace", self.root, "campaign", "create", "--config", str(path)])
            request.assert_not_called()

    def test_campaign_actions_use_explicit_id_and_correct_method_and_timeout(self):
        self.root = "/work"
        for action in ("get", "baseline-check", "summary", "start", "resume", "cancel", "report"):
            with self.subTest(action=action):
                _, _, request = self.cli(["campaign", action, "--id", "saved", "--timeout", "91"], [{"id": "saved"}])
                call = request.call_args
                self.assertEqual(call.args[:2], ("http://localhost", "/campaigns/saved" + ("" if action == "get" else "/" + action)))
                self.assertEqual(call.kwargs["timeout"], 91)
                self.assertEqual(call.args[2:], () if action in ("get", "summary") else ({},))

    def test_probe_uses_complete_config_and_its_own_deadline(self):
        with tempfile.TemporaryDirectory() as self.root:
            path = self.configuration_file(Path(self.root))
            _, _, request = self.cli(["campaign", "probe", "--config", path, "--timeout", "180"], [{"effectiveWorkers": 32}])
        self.assertEqual(request.call_args.args[1], "/targets/probe")
        self.assertEqual(request.call_args.kwargs["timeout"], 180)

    def test_screening_create_has_full_input_pool_and_does_not_approve(self):
        with tempfile.TemporaryDirectory() as self.root:
            path = self.configuration_file(Path(self.root))
            settings = Path(self.root) / "settings.json"
            settings.write_text('{"python":"/venv/bin/python3"}')
            _, _, request = self.cli(["screening", "create", "--config", path, "--settings", str(settings), "--input", "full-pool.index", "--training-fraction", "0.2"], [{"id": "selection", "status": "DRAFT"}])
        request.assert_called_once()
        body = request.call_args.args[2]
        self.assertEqual(request.call_args.args[1], "/screenings")
        self.assertEqual(body["config"]["execution"]["workers"], 32)
        self.assertEqual(body["settings"], {"python": "/venv/bin/python3"})
        self.assertEqual(body["inputPath"], "full-pool.index")
        self.assertEqual(body["trainingFraction"], 0.2)

    def test_screening_approval_sends_only_exact_supplied_hash(self):
        self.root = "/work"
        reviewed = "abc123" * 10 + "abcd"
        _, _, request = self.cli(["screening", "approve", "--id", "selection", "--proposal-sha256", reviewed], [{"status": "RUNNING"}])
        request.assert_called_once_with("http://localhost", "/screenings/selection/approve", {"proposalSha256": reviewed}, timeout=30)

    def test_parser_rejects_missing_ids_hashes_and_unbounded_watch_before_api_calls(self):
        for argv in (["campaign", "start"], ["screening", "approve", "--id", "selection"],
                     ["screening", "approve", "--id", "selection", "--proposal-sha256", "latest"],
                     ["watch", "--timeout", "nan"], ["watch", "--interval", "0"],
                     ["watch", "--campaign", "one", "--screening", "two"],
                     ["trial", "fail", "--campaign", "saved", "--number", "9"],
                     ["trial", "fail", "--campaign", "saved", "--number", "9", "--reason", " "],
                     ["logs", "--campaign", "saved", "--path", "stdout.log", "--tail", "0"],
                     ["diagnose", "--campaign", "saved", "--output", "diag.zip", "--max-files", "100000"]):
            with self.subTest(argv=argv), patch.object(client, "request") as request, contextlib.redirect_stderr(io.StringIO()):
                with self.assertRaises(SystemExit) as error:
                    client.main(argv)
                self.assertEqual(error.exception.code, 2)
                request.assert_not_called()

    def test_lost_post_response_is_not_replayed(self):
        with patch.object(client, "urlopen", side_effect=TimeoutError("response lost")) as send:
            with self.assertRaisesRegex(RuntimeError, "may have accepted"):
                client.request("http://localhost", "/campaigns/saved/start", {})
        send.assert_called_once()

    def test_bounded_diagnostic_http_reads_reject_oversized_responses(self):
        with patch.object(client, "urlopen") as open_url:
            response = open_url.return_value.__enter__.return_value
            response.read.return_value = b"x" * 101
            with self.assertRaisesRegex(RuntimeError, "diagnostic limit"):
                client.request("http://localhost", "/campaigns/saved/artifact", maximum_bytes=100)
            response.read.assert_called_once_with(101)

    def test_watch_keeps_other_workers_when_an_activity_endpoint_is_unavailable(self):
        self.root = "/work"
        saved = {"status": "RUNNING", "trials": [
            {"number": 1, "targetId": "a", "status": "GENERATION", "attempts": [{"number": 1, "stage": "GENERATION"}]},
            {"number": 2, "targetId": "b", "status": "GENERATION", "attempts": [{"number": 1, "stage": "GENERATION"}]}]}
        def response(base, path, **kwargs):
            if path == "/campaigns/saved": return saved
            if path.endswith("/progress"): return {"status": "GENERATION"}
            if "/1/activity?" in path: raise RuntimeError("activity offline")
            if "/2/activity?" in path: return {"text": '{"type":"message","text":"healthy worker"}\n', "nextCursor": 50, "more": False, "reset": False}
            self.fail(path)
        output = io.StringIO()
        with patch.object(client, "capabilities"), patch.object(client, "request", side_effect=response), contextlib.redirect_stdout(output):
            client.main(["--workspace", self.root, "watch", "--campaign", "saved", "--once", "--json"])
        values = [json.loads(line) for line in output.getvalue().splitlines()]
        self.assertIn("1/1", values[0]["activityErrors"])
        self.assertTrue(any(value.get("event", {}).get("text") == "healthy worker" for value in values))

    def test_watch_prefers_active_work_and_pins_it(self):
        args = SimpleNamespace(campaign=None, screening=None, workspace="/missing")
        with patch.object(client, "request", side_effect=[{"active": ""}, {"active": "selection"}]) as request:
            self.assertEqual(client.watch_owner(args, "http://localhost", time.monotonic() + 10), ("screening", "selection"))
        self.assertTrue(all(len(call.args) == 2 for call in request.call_args_list))

    def test_watch_uses_backend_session_when_idle(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            (root / "campaign-session.json").write_text('{"campaignId":"saved"}')
            args = SimpleNamespace(campaign=None, screening=None, workspace=root)
            with patch.object(client, "request", return_value={"active": ""}):
                self.assertEqual(client.watch_owner(args, "http://localhost", time.monotonic() + 10), ("campaign", "saved"))

    def test_activity_cursor_handles_partial_json_and_resets_without_mixing_attempts(self):
        event = json.dumps({"type": "item.completed", "text": "message α"}) + "\n"
        pages = [{"text": event[:15], "nextCursor": 15, "more": True, "reset": False},
                 {"text": event[15:], "nextCursor": len(event.encode()), "more": False, "reset": False}]
        cursors = {}
        deadline = time.monotonic() + 10
        with patch.object(client, "request", side_effect=pages) as request:
            events, caught_up = client.watch_activity("http://localhost", "saved", (1, 2), cursors, deadline)
        self.assertTrue(caught_up)
        self.assertEqual([e["event"]["text"] for e in events], ["message α"])
        self.assertIn("cursor=15", request.call_args.args[1])
        empty = {"text": "", "nextCursor": len(event.encode()), "more": False, "reset": False}
        with patch.object(client, "request", return_value=empty):
            self.assertEqual(client.watch_activity("http://localhost", "saved", (1, 2), cursors, deadline), ([], True))
        with patch.object(client, "request", return_value=dict(empty, nextCursor=0)) as request:
            client.watch_activity("http://localhost", "saved", (1, 3), cursors, deadline)
        self.assertIn("attempt=3&cursor=0", request.call_args.args[1])
        cursors[(1, 2)]["partial"] = "discard me"
        with patch.object(client, "request", return_value=dict(empty, text=event, reset=True)):
            events, caught_up = client.watch_activity("http://localhost", "saved", (1, 2), cursors, deadline)
        self.assertEqual(events[0]["type"], "activity-reset")
        self.assertEqual(events[1]["event"]["text"], "message α")

    def test_watch_pages_are_bounded(self):
        page = {"text": "", "nextCursor": 1, "more": True, "reset": False}
        with patch.object(client, "request", return_value=page) as request:
            _, caught_up = client.watch_activity("http://localhost", "saved", (1, 1), {}, time.monotonic() + 10)
        self.assertEqual(request.call_count, 4)
        self.assertFalse(caught_up)

    def test_watch_keeps_generation_activity_during_preparation(self):
        campaign = {"status": "RUNNING", "trials": [{"number": 1, "status": "PREPARATION", "attempts": [
            {"number": 1, "stage": "GENERATION"}, {"number": 2, "stage": "REPAIR"}, {"number": 3, "stage": "PREPARATION"}]}]}
        with patch.object(client, "request", side_effect=[campaign, {"substage": "BUILD"}]):
            snapshot, source = client.watch_snapshot("http://localhost", "campaign", "saved", time.monotonic() + 10)
        self.assertEqual(source, [(1, 2)])
        self.assertEqual(snapshot["trials"][0]["progress"]["substage"], "BUILD")

    def test_watch_reports_infrastructure_failure_and_stops_without_actions(self):
        self.root = "/work"
        campaign = {"status": "NEEDS_ATTENTION", "problem": {"category": "INFRASTRUCTURE", "code": "BUILD", "message": "framework failure"},
                    "trials": [{"number": 1, "status": "BLOCKED", "attempts": [{"number": 1, "stage": "GENERATION"}]}]}
        progress = {"attempt": {"stage": "PREPARATION"}, "substage": "BUILD", "repairTurns": 0, "infrastructureRetries": 2,
                    "elapsedMillis": 1000, "remote": {"stageDirectory": "build", "status": "FAILED"}}
        event = {"type": "item.completed", "text": "inspect build log"}
        page = {"text": json.dumps(event) + "\n", "nextCursor": 75, "more": False, "reset": False}
        code, output, request = self.cli(["watch", "--campaign", "saved", "--json"], [campaign, progress, page])
        values = [json.loads(line) for line in output.splitlines()]
        self.assertEqual(code, 0)
        self.assertEqual(values[0]["status"], "NEEDS_ATTENTION")
        self.assertEqual(values[0]["trials"][0]["progress"]["repairTurns"], 0)
        self.assertEqual(values[1]["event"], event)
        self.assertEqual(values[-1]["reason"], "status")
        self.assertTrue(all(len(call.args) == 2 for call in request.call_args_list))

    def test_watch_waits_for_cancellation_confirmation_and_times_out(self):
        self.root = "/work"
        states = [{"status": "CANCELLATION_PENDING", "trials": []}, {"status": "CANCELLED", "trials": []}]
        with patch.object(client.time, "sleep"):
            _, output, request = self.cli(["watch", "--campaign", "saved", "--json"], states)
        values = [json.loads(line) for line in output.splitlines()]
        self.assertEqual([v["status"] for v in values if v["type"] == "status"], ["CANCELLATION_PENDING", "CANCELLED"])
        self.assertEqual(request.call_count, 2)
        with patch.object(client.time, "monotonic", side_effect=[0, 0, 2, 2]):
            _, output, request = self.cli(["watch", "--campaign", "saved", "--timeout", "1", "--json"], [{"status": "WAITING_FOR_REMOTE", "trials": []}])
        self.assertEqual(json.loads(output.splitlines()[-1])["reason"], "timeout")
        request.assert_called_once()

    def test_watch_screening_shows_counts_and_stops_for_review(self):
        self.root = "/work"
        state = {"status": "AWAITING_APPROVAL", "activity": {"phase": "SCREENING"}, "progress": {
            "phase": "CONFIRMATIONS", "initialCompleted": 10, "initialTotal": 10, "confirmationsCompleted": 30, "confirmationsTotal": 30}}
        _, output, request = self.cli(["watch", "--screening", "selection"], [state])
        self.assertIn("CONFIRMATIONS", output)
        self.assertIn("initial=10/10", output)
        self.assertIn("confirmations=30/30", output)
        request.assert_called_once()

    def test_readable_watch_uses_observed_counters_and_separate_retry_counts(self):
        snapshot = {"kind": "campaign", "id": "saved", "status": "RUNNING", "trials": [{"number": 1, "targetId": "a", "status": "GENERATION", "progress": {
            "generation": {"state": "ACTIVE", "turns": 2, "commandsStarted": 3, "commandsCompleted": 2, "edits": 1,
                           "messages": 4, "lastActivityAt": "2026-10-21T12:00:00Z"}, "repairTurns": 0, "infrastructureRetries": 2}}]}
        line = client.watch_line(snapshot)
        for expected in ("turns=2", "commands=2/3", "edits=1", "messages=4", "repairs=0 infraRetries=2", "lastActivity=2026-10-21"):
            self.assertIn(expected, line)

    def test_target_mutations_use_explicit_ids_and_http_methods(self):
        with tempfile.TemporaryDirectory() as self.root:
            server = Path(self.root) / "server.json"
            server.write_text('{"id":"b","host":"second","workspace":"/work"}')
            _, _, request = self.cli(["target", "add", "--campaign", "saved", "--id", "b", "--server", str(server)], [{}])
            self.assertEqual(request.call_args.args[1], "/campaigns/saved/targets/b")
            self.assertEqual(request.call_args.kwargs["method"], "PUT")
            _, _, request = self.cli(["target", "remove", "--campaign", "saved", "--id", "b"], [{}])
            self.assertEqual(request.call_args.kwargs["method"], "DELETE")

    def test_watch_reads_all_active_trials_and_keeps_activity_cursors_separate(self):
        saved = {"status":"RUNNING", "poolRevision":2, "targets":{"a":{"status":"BUSY"},"b":{"status":"BUSY"}}, "trials":[
            {"number":1,"targetId":"a","status":"GENERATION","attempts":[{"number":1,"stage":"GENERATION"}]},
            {"number":2,"targetId":"b","status":"TUNING","attempts":[{"number":1,"stage":"GENERATION"}]}]}
        with patch.object(client, "request", side_effect=[saved, {"status":"GENERATION"}, {"status":"TUNING"}]):
            snapshot, sources = client.watch_snapshot("http://localhost", "campaign", "saved", time.monotonic()+10)
        self.assertEqual([t["targetId"] for t in snapshot["trials"]], ["a","b"])
        self.assertEqual(sources, [(1,1),(2,1)])
        self.assertIn("server=b:BUSY", client.watch_line(snapshot))


if __name__ == "__main__":
    unittest.main()
