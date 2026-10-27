#!/usr/bin/env python3
"""Dependency-free AutoLLM operator client. Mork owns experiment state and retries."""
import argparse
from concurrent.futures import ThreadPoolExecutor
from datetime import datetime, timezone
import hashlib
import json
import math
import os
from pathlib import Path
import socket
import subprocess
import sys
import time
from urllib.error import HTTPError, URLError
from urllib.parse import quote, urlencode
from urllib.request import Request, urlopen
import zipfile

FEATURES = {"SSH", "SCREENING_JOBS", "CODEX_PROGRESS", "BYTE_LOG_CURSORS", "STRUCTURED_FAILURES", "SSH_SERVER_POOL", "DYNAMIC_TARGETS"}


def request(base, path, body=None, timeout=30, method=None, maximum_bytes=None):
    req = Request(base.rstrip("/") + "/api/autollm" + path,
                  data=None if body is None else json.dumps(body).encode(),
                  headers={"Content-Type": "application/json"}, method=method)
    try:
        with urlopen(req, timeout=timeout) as response:
            if maximum_bytes is None:
                return json.load(response)
            data = response.read(maximum_bytes + 1)
            if len(data) > maximum_bytes:
                raise RuntimeError(f"Response from {path} exceeds the {maximum_bytes}-byte diagnostic limit")
            return json.loads(data)
    except HTTPError as error:
        message = error.read(16384).decode(errors="replace")
        try:
            message = json.loads(message).get("message", message)
        except (ValueError, AttributeError):
            pass
        raise RuntimeError(f"HTTP {error.code}: {message}") from error
    except OSError as error:
        message = f"Request to {path} failed: {error}"
        if body is not None or method in ("PUT", "POST", "DELETE"):
            message += ". The backend may have accepted it; inspect saved work before retrying."
        raise RuntimeError(message) from error


def clean(value):
    if isinstance(value, dict):
        return {key: clean(item) for key, item in value.items() if item is not None}
    if isinstance(value, list):
        return [clean(item) for item in value]
    return value


def merge(defaults, overrides):
    result = dict(defaults)
    for key, value in overrides.items():
        result[key] = merge(result[key], value) if isinstance(value, dict) and isinstance(result.get(key), dict) else value
    return result


def write_json(path, value):
    path = Path(path).resolve()
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_name(path.name + ".tmp")
    with open(temporary, "w", encoding="utf-8", opener=lambda name, flags: os.open(name, flags, 0o600)) as output:
        json.dump(value, output, indent=2, ensure_ascii=False)
        output.write("\n")
    temporary.replace(path)
    return str(path)


def fingerprint(path):
    digest = hashlib.sha256()
    with open(path, "rb") as source:
        for chunk in iter(lambda: source.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def capabilities(base, workspace=None, jar=None, cwd=None):
    return check_capabilities(request(base, "/capabilities"), workspace, jar, cwd)


def check_capabilities(info, workspace=None, jar=None, cwd=None):
    if info.get("mode") != "AUTOLLM" or info.get("apiVersion") != 4 or info.get("checkpointVersion") != 4 or info.get("runnerProtocol") != 4 or not FEATURES.issubset(info.get("features", [])):
        raise RuntimeError("Backend lacks required AutoLLM capabilities; use the matching rebuilt executable")
    if workspace and Path(info["workspace"]).resolve() != Path(workspace).resolve():
        raise RuntimeError("Backend workspace does not match")
    if jar and info.get("artifactSha256") != fingerprint(jar):
        raise RuntimeError("Backend executable differs from the requested JAR")
    if cwd and Path(info["workingDirectory"]).resolve() != Path(cwd).resolve():
        raise RuntimeError("Backend application working directory differs")
    return info


def base_url(args):
    if args.url:
        return args.url.rstrip("/")
    for name in ("session.json", "backend-launch.json"):
        session = Path(args.workspace).resolve() / name
        if session.exists() and json.loads(session.read_text()).get("baseUrl"):
            return json.loads(session.read_text())["baseUrl"]
    return "http://127.0.0.1:18080"


def backend(args):
    workspace, jar, cwd = Path(args.workspace).resolve(), Path(args.jar).resolve(), Path(args.cwd).resolve()
    if not jar.is_file() or not cwd.is_dir():
        raise RuntimeError("Provide an existing executable JAR and application working directory")
    try:
        return capabilities(base_url(args), workspace, jar, cwd)
    except (OSError, URLError, RuntimeError, ValueError, KeyError):
        if args.url:
            raise RuntimeError("Requested URL is unavailable or incompatible; select another URL or port")
    with socket.socket() as probe:
        try:
            probe.bind(("127.0.0.1", args.port))
        except OSError as error:
            raise RuntimeError("Port is occupied; use --port 0 for a free port") from error
        port = probe.getsockname()[1]
    workspace.mkdir(parents=True, exist_ok=True)
    for name in ("session.json", "backend-launch.json"):
        saved = workspace / name
        if not saved.exists():
            continue
        previous = json.loads(saved.read_text())
        try:
            os.kill(int(previous["pid"]), 0)
        except ProcessLookupError:
            pass
        else:
            raise RuntimeError("Saved backend PID still exists; inspect ownership and reconnect before starting another")
    command = [args.java, "-jar", str(jar), "--autollm", f"--autollm.workspace={workspace}",
               "--server.address=127.0.0.1", f"--server.port={port}", "--instances.preload=false", "--solver.benchmark=false"]
    log = workspace / "backend.log"
    base = f"http://127.0.0.1:{port}"
    with open(log, "ab") as output:
        process = subprocess.Popen(command, cwd=cwd, stdin=subprocess.DEVNULL, stdout=output,
                                   stderr=subprocess.STDOUT, start_new_session=True)
    write_json(workspace / "backend-launch.json", {"pid": process.pid, "command": command,
               "workingDirectory": str(cwd), "baseUrl": base, "log": str(log), "artifactSha256": fingerprint(jar)})
    deadline = time.monotonic() + args.timeout
    while time.monotonic() < deadline:
        if process.poll() is not None:
            raise RuntimeError(f"Backend exited with {process.returncode}; inspect {log}")
        try:
            info = request(base, "/capabilities")
        except (URLError, OSError, RuntimeError):
            time.sleep(0.25)
            continue
        if info.get("pid") != process.pid:
            process.terminate()
            raise RuntimeError("Port was claimed by another process; the newly launched parent was stopped")
        try:
            return check_capabilities(info, workspace, jar, cwd)
        except RuntimeError:
            time.sleep(0.25)
    raise RuntimeError(f"Backend readiness timed out; process {process.pid} remains owned and logged in {workspace}. Inspect it before retrying")


def configuration(args, base):
    value = request(base, "/defaults")
    if args.overrides:
        value = merge(value, json.loads(Path(args.overrides).read_text()))
    value = clean(value)
    if Path(value["workspace"]).resolve() != Path(args.workspace).resolve():
        raise RuntimeError("Configuration workspace differs from the connected backend")
    if args.spring and Path(args.output).suffix not in (".yml", ".yaml"):
        raise RuntimeError("Spring configuration output must end in .yml or .yaml")
    path = write_json(args.output, {"autollm": value} if args.spring else value)
    return {"configurationPath": path, "effective": request(base, "/effective-config", value)}


def read_object(path):
    value = json.loads(Path(path).read_text(encoding="utf-8"))
    if not isinstance(value, dict):
        raise RuntimeError(f"Expected a JSON object in {path}")
    return value


def read_configuration(args):
    value = read_object(args.config)
    value = value.get("autollm", value)
    if not isinstance(value, dict) or not value.get("workspace"):
        raise RuntimeError("Provide a complete configuration produced by the config command")
    if Path(value["workspace"]).resolve() != Path(args.workspace).resolve():
        raise RuntimeError("Configuration workspace differs from the connected backend")
    return clean(value)


def lifecycle(args, base):
    root = "/campaigns" if args.command == "campaign" else "/screenings"
    if args.action == "list":
        return request(base, root)
    if args.action in ("validate", "probe", "create"):
        config = read_configuration(args)
        if args.command == "campaign":
            path = {"validate": "/validate", "probe": "/targets/probe", "create": root}[args.action]
            return request(base, path, config, timeout=args.timeout)
        settings = read_object(args.settings) if args.settings else {}
        body = clean({"config": config, "settings": settings, "inputPath": args.input,
                      "trainingFraction": args.training_fraction})
        return request(base, root, body, timeout=args.timeout)
    path = root + "/" + quote(args.id, safe="")
    if args.action in ("get", "review", "summary", "recovery"):
        return request(base, path if args.action == "get" else path + "/" + args.action, timeout=args.timeout)
    body = {"proposalSha256": args.proposal_sha256} if args.action == "approve" else {}
    if args.command == "campaign" and args.action == "start":
        body = {"targets": args.target}
    return request(base, path + "/" + args.action, body, timeout=args.timeout)


def target(args, base):
    root = f"/campaigns/{quote(args.campaign, safe='')}/targets"
    if args.action == "list":
        return request(base, root)
    path = root + "/" + quote(args.id, safe="")
    if args.action == "add":
        server = read_object(args.server)
        if server.get("id") != args.id:
            raise RuntimeError("Server definition ID differs from --id")
        return request(base, path, server, method="PUT")
    if args.action == "remove":
        return request(base, path, method="DELETE")
    return request(base, path + "/" + args.action, {})


def trial(args, base):
    root = f"/campaigns/{quote(args.campaign, safe='')}"
    if args.action == "actions":
        recovery = request(base, root + "/recovery", timeout=args.timeout)
        actions = recovery["trials"].get(str(args.number))
        if actions is None:
            raise RuntimeError(f"Unknown trial {args.number}")
        return actions
    body = {"reason": args.reason.strip()} if args.action == "fail" else {}
    return request(base, root + f"/trials/{args.number}/{args.action}", body, timeout=args.timeout)


def read_progress(base, root, trials, timeout):
    if not trials:
        return {}, {}
    deadline = time.monotonic() + timeout
    def read(item):
        number = item["number"]
        try:
            remaining = deadline - time.monotonic()
            if remaining <= 0:
                raise RuntimeError("Progress read deadline expired before this request")
            value = request(base, root + f"/trials/{number}/progress", timeout=remaining)
            return str(number), value, None
        except (RuntimeError, OSError, ValueError) as error:
            return str(number), None, str(error)
    progress, errors = {}, {}
    with ThreadPoolExecutor(max_workers=4) as executor:
        for number, value, error in executor.map(read, trials):
            if error is None:
                progress[number] = value
            else:
                errors[number] = error
    return progress, errors


def watch_timeout(deadline):
    return max(0.1, min(30, deadline - time.monotonic()))


def watch_owner(args, base, deadline):
    if args.campaign:
        return "campaign", args.campaign
    if args.screening:
        return "screening", args.screening
    for kind in ("campaign", "screening"):
        listing = request(base, f"/{kind}s", timeout=watch_timeout(deadline))
        if listing.get("active"):
            return kind, listing["active"]
    sessions = []
    for kind in ("campaign", "screening"):
        path = Path(args.workspace) / f"{kind}-session.json"
        if path.is_file():
            identifier = read_object(path).get(kind + "Id")
            if identifier:
                sessions.append((path.stat().st_mtime_ns, kind, identifier))
    if sessions:
        _, kind, identifier = max(sessions)
        return kind, identifier
    raise RuntimeError("No active operation or saved session; select --campaign ID or --screening ID")


def watch_snapshot(base, kind, identifier, deadline):
    root = f"/{kind}s/{quote(identifier, safe='')}"
    saved = request(base, root + ("/status" if kind == "campaign" else ""), timeout=watch_timeout(deadline))
    snapshot = {"type": "status", "kind": kind, "id": identifier, "status": saved["status"],
                "problem": saved.get("problem"), "remote": saved.get("remote")}
    if kind == "screening":
        snapshot.update(activity=saved.get("activity"), progress=saved.get("progress"))
        return snapshot, None
    trials = saved.get("trials", [])
    selected = [t for t in trials if t["status"] not in ("PENDING", "SUCCEEDED", "FAILED")]
    if not selected:
        selected = next(([t] for t in reversed(trials) if t["status"] != "PENDING"), [])
    snapshot.update(poolRevision=saved.get("poolRevision"), targets=saved.get("targets", {}), trials=[])
    progress_by_trial, progress_errors = read_progress(base, root, selected, watch_timeout(deadline))
    sources = []
    for trial in selected:
        number = str(trial["number"])
        snapshot["trials"].append({"number": trial["number"], "targetId": trial.get("targetId"), "status": trial["status"], "progress": progress_by_trial.get(number), "progressError": progress_errors.get(number)})
        generation = next((a for a in reversed(trial.get("attempts", [])) if a["stage"] in ("GENERATION", "REPAIR")), None)
        if generation:
            sources.append((trial["number"], generation["number"]))
    return snapshot, sources


def watch_activity(base, identifier, source, cursors, deadline):
    trial, attempt = source
    state = cursors.setdefault(source, {"cursor": 0, "partial": ""})
    events = []
    for _ in range(4):
        query = urlencode({"attempt": attempt, "cursor": state["cursor"]})
        page = request(base, f"/campaigns/{quote(identifier, safe='')}/trials/{trial}/activity?{query}", timeout=watch_timeout(deadline))
        if page["reset"]:
            state["partial"] = ""
            events.append({"type": "activity-reset", "trial": trial, "attempt": attempt})
        lines = (state["partial"] + page["text"]).split("\n")
        state["partial"] = lines.pop()
        for line in lines:
            if line.strip():
                event = json.loads(line)
                events.append({"type": "activity", "trial": trial, "attempt": attempt, "event": event})
        state["cursor"] = page["nextCursor"]
        if not page["more"] or time.monotonic() >= deadline:
            break
    return events, not page["more"] and not state["partial"]


def watch_line(snapshot):
    progress = snapshot.get("progress") or {}
    activity = snapshot.get("activity") or {}
    parts = [f"{snapshot['kind']} {snapshot['id']}: {snapshot['status']}"]
    if snapshot["kind"] == "campaign":
        for trial in snapshot.get("trials", []):
            progress = trial.get("progress") or {}
            attempt = progress.get("attempt") or {}
            description = f"trial={trial['number']} target={trial.get('targetId')} {trial['status']}"
            if attempt:
                description += " " + "/".join(filter(None, (attempt.get("stage"), progress.get("substage"))))
            generation = progress.get("generation") or {}
            if generation:
                description += (f" {generation.get('state', '')} turns={generation.get('turns', 0)}"
                                f" commands={generation.get('commandsCompleted', 0)}/{generation.get('commandsStarted', 0)}"
                                f" edits={generation.get('edits', 0)} messages={generation.get('messages', 0)}"
                                f" lastActivity={generation.get('lastActivityAt')}")
            description += f" repairs={progress.get('repairTurns', 0)} infraRetries={progress.get('infrastructureRetries', 0)}"
            if progress.get("quotaRetryAt"):
                description += f" quotaRetryAt={progress['quotaRetryAt']}"
            remote = progress.get("remote") or {}
            if remote:
                description += f" remote={remote.get('status')} lastSync={remote.get('lastSynchronizedAt')}"
            if progress.get("problem"):
                description += " problem=" + str(progress["problem"])
            budget = (progress.get("workload") or {}).get("budget") or {}
            if budget:
                description += f" budget={budget.get('used', '?')}/{budget.get('maximum', '?')}"
            if trial.get("progressError"):
                description += " progressUnavailable=" + trial["progressError"]
            parts.append(description)
        progress = {}
        for identifier, target in snapshot.get("targets", {}).items():
            parts.append(f"server={identifier}:{target.get('status')} trial={target.get('assignedTrial')}")
            if target.get("problem"):
                parts.append(str(target["problem"]))

    else:
        phase = activity.get("phase")
        if phase == "SCREENING" or not phase:
            phase = progress.get("phase", phase)
        if phase:
            parts.append(phase)
        for name in ("initial", "confirmations"):
            if name + "Total" in progress:
                parts.append(f"{name}={progress.get(name + 'Completed', 0)}/{progress[name + 'Total']}")
    remote = progress.get("remote") or activity.get("remote") or snapshot.get("remote") or {}
    if remote:
        parts.append(f"remote={remote.get('stageDirectory', '')}:{remote.get('status', '')}")
    problem = snapshot.get("problem") or progress.get("problem")
    if problem:
        parts.append(f"problem={problem.get('category', '')}/{problem.get('code', '')}: {problem.get('message', '')}")
    for source, error in snapshot.get("activityErrors", {}).items():
        parts.append(f"activityUnavailable={source}: {error}")
    return " | ".join(parts)


def watch_output(args, value):
    if args.json:
        print(json.dumps(value, ensure_ascii=False), flush=True)
    elif value["type"] == "status":
        print(watch_line(value), flush=True)
    elif value["type"] == "activity":
        event = value["event"]
        print(f"  [{event.get('type', 'activity')}] {event.get('text', '')}", flush=True)
    elif value["type"] == "activity-reset":
        print("  [Activity stream restarted]", flush=True)
    else:
        print(f"Watch ended: {value['reason']} ({value['status']})", flush=True)


def watch(args, base):
    deadline = time.monotonic() + args.timeout
    kind, identifier = watch_owner(args, base, deadline)
    cursors = {}
    stopped = {"DRAFT", "NEEDS_ATTENTION", "INTERRUPTED", "CANCELLED", "FAILED", "SUCCEEDED",
               "COMPLETED_WITH_FAILURES", "AWAITING_APPROVAL", "APPROVED"}
    while True:
        snapshot, sources = watch_snapshot(base, kind, identifier, deadline)
        events, caught_up = [], True
        for source in dict.fromkeys([*(sources or []), *cursors]):
            try:
                batch, complete = watch_activity(base, identifier, source, cursors, deadline)
            except (RuntimeError, OSError, ValueError) as error:
                snapshot.setdefault("activityErrors", {})[f"{source[0]}/{source[1]}"] = str(error)
                caught_up = False
                continue
            events.extend(batch)
            caught_up &= complete
            if complete and source not in (sources or []):
                del cursors[source]
        snapshot["activityCaughtUp"] = caught_up
        watch_output(args, snapshot)
        for event in events:
            watch_output(args, event)
        reason = None
        if args.once:
            reason = "once"
        elif snapshot["status"] in stopped and caught_up:
            reason = "status"
        elif time.monotonic() >= deadline:
            reason = "timeout"
        if reason:
            watch_output(args, {"type": "watch-ended", "reason": reason, "status": snapshot["status"],
                                "kind": kind, "id": identifier})
            return
        time.sleep(min(args.interval, max(0, deadline - time.monotonic())))


def status(args, base):
    campaigns = request(base, "/campaigns", timeout=args.timeout)
    result = {"baseUrl": base, "workspace": str(Path(args.workspace).resolve()), "activeCampaign": campaigns["active"],
              "checkedAt": datetime.now(timezone.utc).isoformat(), "errors": {}}
    try:
        screenings = request(base, "/screenings", timeout=args.timeout)
        result.update(activeScreening=screenings["active"], screenings=screenings["jobs"])
    except (RuntimeError, OSError, ValueError) as error:
        result["errors"]["screenings"] = str(error)
    candidates = campaigns["campaigns"]
    selected = args.campaign or campaigns["active"] or (candidates[0]["id"] if candidates else None)
    if selected:
        root = "/campaigns/" + quote(selected, safe="")
        campaign = request(base, root + "/status", timeout=args.timeout)
        result["campaign"] = {key: campaign.get(key) for key in ("id", "status", "problem", "poolRevision", "targets")}
        result["campaign"]["trials"] = [{key: trial.get(key) for key in ("number", "targetId", "status", "repairTurns", "infrastructureRetries", "quotaRetryAt", "problem")} for trial in campaign["trials"]]
        selected_trials = [trial for trial in campaign["trials"] if trial["status"] != "PENDING"]
        result["progress"], result["progressErrors"] = read_progress(base, root, selected_trials, args.timeout)
        try:
            result["summary"] = request(base, root + "/summary", timeout=args.timeout)
        except (RuntimeError, OSError, ValueError) as error:
            result["errors"]["summary"] = str(error)
    if args.screening:
        try:
            result["screening"] = request(base, "/screenings/" + quote(args.screening, safe=""), timeout=args.timeout)
        except (RuntimeError, OSError, ValueError) as error:
            result["errors"]["screening"] = str(error)
    result["partial"] = bool(result["errors"] or result.get("progressErrors"))
    return result


def status_table(value):
    campaign = value.get("campaign") or {}
    lines = [f"Checked {value['checkedAt']} | {value['baseUrl']}",
             f"Campaign {campaign.get('id', 'none')}: {campaign.get('status', 'none')}" + (" | PARTIAL STATUS" if value["partial"] else "")]
    rows = [["TRIAL", "SERVER", "STATE / STAGE", "TUNING BUDGET", "ELAPSED", "LAST SYNC / ACTIVITY"]]
    for trial in campaign.get("trials", []):
        number = str(trial["number"])
        progress = value.get("progress", {}).get(number) or {}
        budget = (progress.get("workload") or {}).get("budget") or {}
        used, maximum = budget.get("used"), budget.get("maximum")
        budget_text = f"{used:,}/{maximum:,} ({100 * used / maximum:.1f}%)" if isinstance(used, (int, float)) and maximum else "—"
        millis = progress.get("elapsedMillis")
        elapsed = f"{int(millis // 3600000)}h {int(millis // 60000 % 60)}m" if isinstance(millis, (int, float)) else "—"
        remote = progress.get("remote") or {}
        generation = progress.get("generation") or {}
        state = trial["status"]
        if progress.get("substage"):
            state += "/" + progress["substage"]
        if number in value.get("progressErrors", {}):
            state += " (unavailable)"
        rows.append([number, trial.get("targetId") or "Unassigned", state, budget_text, elapsed,
                     remote.get("lastSynchronizedAt") or generation.get("lastActivityAt") or "—"])
    widths = [max(len(row[index]) for row in rows) for index in range(len(rows[0]))]
    lines.extend("  ".join(cell.ljust(widths[index]) for index, cell in enumerate(row)) for row in rows)
    for trial in campaign.get("trials", []):
        number = str(trial["number"])
        progress = value.get("progress", {}).get(number) or {}
        problem = progress.get("problem") or trial.get("problem")
        if problem:
            lines.append(f"Trial {number}: {problem.get('category')}/{problem.get('code')}: {str(problem.get('message', ''))[:1000]}")
        if trial.get("quotaRetryAt"):
            lines.append(f"Trial {number}: quota retry after {trial['quotaRetryAt']}")
    for key, error in value.get("progressErrors", {}).items():
        lines.append(f"Trial {key} progress unavailable: {error[:1000]}")
    for key, error in value["errors"].items():
        lines.append(f"{key} unavailable: {error[:1000]}")
    if value.get("screening"):
        lines.append(f"Screening {value['screening'].get('id')}: {value['screening'].get('status')}")
    lines.append("Tuning budgets are the latest published checkpoints; unavailable values are not zero.")
    return "\n".join(lines)


def logs(args, base):
    identifier = args.screening or args.campaign
    root = f"/screenings/{quote(identifier, safe='')}" if args.screening else f"/campaigns/{quote(identifier, safe='')}"
    def tail():
        page = request(base, root + "/preview?" + urlencode({"path": args.path, "lines": args.lines}), timeout=args.timeout)
        if page["truncated"]:
            print(f"[Latest {page['lineCount']} lines; preview capped at 256 KiB. Full file: {base}/api/autollm{root}/artifact?{urlencode({'path': args.path})}]", file=sys.stderr)
        return page
    if args.cursor is None:
        page = tail()
    else:
        page = request(base, root + "/log?" + urlencode({"path": args.path, "cursor": args.cursor, "maximumBytes": 65536}), timeout=args.timeout)
        if page["more"]:
            print("[One bounded page shown; use --follow for updates or download the full file]", file=sys.stderr)
    sys.stdout.write(page["text"]); sys.stdout.flush()
    cursor = page["nextCursor"]
    while args.follow:
        time.sleep(2)
        page = request(base, root + "/log?" + urlencode({"path": args.path, "cursor": cursor, "maximumBytes": 65536}), timeout=args.timeout)
        if page["reset"] or page["more"]:
            print("\n[Log restarted or advanced beyond one page; showing the latest bounded tail]\n", file=sys.stderr)
            page = tail()
        sys.stdout.write(page["text"]); sys.stdout.flush()
        cursor = page["nextCursor"]


def diagnose(args, base):
    prefix = "/campaigns/" + quote(args.campaign, safe="")
    campaign = request(base, prefix, timeout=args.timeout, maximum_bytes=2_000_000)
    chosen = None
    if args.trial is not None:
        chosen = next((trial for trial in campaign["trials"] if trial["number"] == args.trial), None)
        if chosen is None:
            raise RuntimeError(f"Unknown trial {args.trial}")
    artifact_query = {"hashes": "false"}
    if chosen:
        artifact_query["trial"] = args.trial
    artifacts = request(base, prefix + "/artifacts?" + urlencode(artifact_query), timeout=args.timeout, maximum_bytes=2_000_000)
    if chosen:
        directory = chosen.get("directory")
        if not directory:
            directory = f"targets/{chosen['targetId']}/trials/{args.trial:03d}" if chosen.get("targetId") else f"trials/{args.trial:03d}"
        artifacts = [artifact for artifact in artifacts if artifact["path"].startswith(directory.rstrip("/") + "/")]
        campaign = dict(campaign, trials=[chosen], targets={key: target for key, target in campaign.get("targets", {}).items() if key == chosen.get("targetId")})
    failures, failure_count = [], 0
    for item in campaign.get("trials", []):
        problem = item.get("problem") or {}
        if problem or item.get("failure"):
            failure_count += 1
            if len(failures) >= 64:
                continue
            failures.append({"trial": item["number"], "category": problem.get("category"), "code": problem.get("code"),
                             "message": str(problem.get("message") or item.get("failure", ""))[:4000],
                             "recovery": problem.get("recovery"), "evidence": problem.get("evidence", [])[:16]})
    output = Path(args.output).resolve(); output.parent.mkdir(parents=True, exist_ok=True)
    total, included, evidence, log_tails, omitted = 0, 0, 0, 0, 0
    errors = {}
    evidence_names = {"failure.json", "dependency-differences.json", "dependencies.json", "probes.json", "check.json", "remote-status.json", "remote-job.json", "codex-progress.json", "terminal.json"}
    with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as archive:
        def save(name, data):
            nonlocal total
            if len(data) > 2_000_000 or total + len(data) > args.maximum_bytes:
                errors[name] = "Omitted: diagnostic byte limit reached"
                return False
            archive.writestr(name, data); total += len(data)
            return True
        def save_json(name, data):
            return save(name, json.dumps(data, indent=2, ensure_ascii=False).encode())
        save_json("campaign.json", campaign)
        save_json("artifact-index.json", artifacts)
        endpoints = [("capabilities.json", "/capabilities"), ("summary.json", prefix + "/summary")]
        if chosen:
            endpoints.append(("progress.json", prefix + f"/trials/{args.trial}/progress"))
        for name, path in endpoints:
            try:
                save_json(name, request(base, path, timeout=args.timeout, maximum_bytes=2_000_000))
            except (RuntimeError, OSError, ValueError) as error:
                errors[name] = str(error)[:1000]
        candidates = sorted(artifacts, key=lambda artifact: (Path(artifact["path"]).name != "failure.json", not artifact["path"].endswith("stderr.log"), artifact["path"]))
        for artifact in candidates:
            path = artifact["path"]
            is_log = path.endswith((".log", ".jsonl", ".txt"))
            is_evidence = Path(path).name in evidence_names
            if "/source/" in path or path.startswith("control/") or not (is_log or is_evidence):
                continue
            if included >= args.maximum_files or total >= args.maximum_bytes or is_evidence and artifact["size"] > 2_000_000:
                omitted += 1; continue
            # Both the number of attempts and the archive payload are bounded, including failed reads.
            included += 1
            try:
                if is_evidence:
                    value = request(base, prefix + "/artifact?" + urlencode({"path": path}), timeout=args.timeout, maximum_bytes=2_000_000)
                    if save_json("evidence/" + path, value): evidence += 1
                else:
                    page = request(base, prefix + "/preview?" + urlencode({"path": path, "lines": args.lines}), timeout=args.timeout, maximum_bytes=2_000_000)
                    if save("logs/" + path + ".tail.txt", page["text"].encode()): log_tails += 1
            except (RuntimeError, OSError, ValueError) as error:
                errors[path] = str(error)[:1000]
        summary = {"campaign": args.campaign, "trial": args.trial, "failureSummary": failures, "omittedFailureSummaries": failure_count - len(failures),
                   "limits": {"linesPerLog": args.lines, "maximumFiles": args.maximum_files, "maximumPayloadBytes": args.maximum_bytes},
                   "payloadBytes": total, "logTails": log_tails, "evidenceFiles": evidence, "omittedFiles": omitted, "errors": errors}
        # The bounded manifest is retained even if the evidence payload exhausted its allowance.
        archive.writestr("diagnostic-summary.json", json.dumps(summary, indent=2))
    return {"diagnosticBundle": str(output), **summary, "note": "Bounded diagnostics; full files remain downloadable from the campaign artifact endpoint."}


def positive_seconds(value):
    seconds = float(value)
    if not math.isfinite(seconds) or seconds <= 0:
        raise argparse.ArgumentTypeError("Use a finite positive number of seconds")
    return seconds


def bounded_integer(minimum, maximum):
    def parse(value):
        try:
            number = int(value)
        except ValueError as error:
            raise argparse.ArgumentTypeError("Expected an integer") from error
        if not minimum <= number <= maximum:
            raise argparse.ArgumentTypeError(f"Expected a value between {minimum} and {maximum}")
        return number
    return parse


def nonblank(value):
    if not value.strip():
        raise argparse.ArgumentTypeError("A nonblank reason is required")
    return value.strip()


def reviewed_sha256(value):
    if len(value) != 64 or any(character not in "0123456789abcdefABCDEF" for character in value):
        raise argparse.ArgumentTypeError("Provide the exact reviewed 64-character proposal SHA-256")
    return value.lower()


def lifecycle_commands(commands):
    for kind in ("campaign", "screening"):
        group = commands.add_parser(kind, help=f"Operate saved {kind}s through Mork's API")
        actions = group.add_subparsers(dest="action", required=True)
        actions.add_parser("list")
        names = ("validate", "probe", "create") if kind == "campaign" else ("create",)
        for name in names:
            action = actions.add_parser(name)
            action.add_argument("--config", required=True, help="Complete JSON or JSON-compatible Spring YAML from config")
            action.add_argument("--timeout", type=positive_seconds, default=120 if name == "probe" else 30)
            if kind == "screening":
                action.add_argument("--settings", help="JSON screening settings; omitted values use backend defaults")
                action.add_argument("--input", required=True, help="Full original instance pool, relative to the configured repository")
                action.add_argument("--training-fraction", type=float)
        names = ("get", "baseline-check", "summary", "recovery", "start", "resume", "cancel", "report") if kind == "campaign" else ("get", "review", "resume", "cancel", "approve")
        for name in names:
            action = actions.add_parser(name)
            action.add_argument("--id", required=True, help=f"Saved {kind} ID; mutating commands never select another operation")
            action.add_argument("--timeout", type=positive_seconds, default=3600 if name == "baseline-check" else 30)
            if kind == "campaign" and name == "start":
                action.add_argument("--target", action="append", default=[], help="Start only these qualified servers; repeat for an explicit healthy subset")
            if name == "approve":
                action.add_argument("--proposal-sha256", required=True, type=reviewed_sha256)


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--workspace", default="autollm-runs")
    parser.add_argument("--url")
    commands = parser.add_subparsers(dest="command", required=True)
    start = commands.add_parser("backend", help="Discover a matching backend or start an owned persistent one")
    start.add_argument("--jar", required=True)
    start.add_argument("--cwd", required=True)
    start.add_argument("--java", default="java")
    start.add_argument("--port", type=int, default=18080)
    start.add_argument("--timeout", type=float, default=120)
    config = commands.add_parser("config", help="Merge defaults and JSON overrides; omit optional nulls")
    config.add_argument("--overrides")
    config.add_argument("--output", required=True)
    config.add_argument("--spring", action="store_true", help="Write JSON-compatible Spring YAML")
    state = commands.add_parser("status")
    state.add_argument("--campaign")
    state.add_argument("--screening")
    state.add_argument("--table", action="store_true", help="Compact all-trial status, budgets and observation timestamps")
    state.add_argument("--timeout", type=positive_seconds, default=10, help="Read deadline in seconds; unavailable sections remain explicitly unknown")
    tail = commands.add_parser("logs")
    owner = tail.add_mutually_exclusive_group(required=True)
    owner.add_argument("--campaign")
    owner.add_argument("--screening")
    tail.add_argument("--path", required=True)
    tail.add_argument("--cursor", type=bounded_integer(0, 2**63 - 1), help="Read one bounded byte page instead of the default tail")
    tail.add_argument("--tail", "--lines", dest="lines", type=bounded_integer(1, 5000), default=500, help="Latest lines, capped at 256 KiB (default: 500)")
    tail.add_argument("--follow", action="store_true")
    tail.add_argument("--timeout", type=positive_seconds, default=10)
    bundle = commands.add_parser("diagnose")
    bundle.add_argument("--campaign", required=True)
    bundle.add_argument("--output", required=True)
    bundle.add_argument("--trial", type=bounded_integer(1, 2**31 - 1), help="Include only this trial's evidence")
    bundle.add_argument("--lines", type=bounded_integer(1, 5000), default=500)
    bundle.add_argument("--max-files", dest="maximum_files", type=bounded_integer(1, 64), default=16)
    bundle.add_argument("--max-bytes", dest="maximum_bytes", type=bounded_integer(1024, 64 * 1024 * 1024), default=8 * 1024 * 1024, help="Maximum uncompressed evidence payload; a bounded manifest is additional")
    bundle.add_argument("--timeout", type=positive_seconds, default=10)
    lifecycle_commands(commands)
    servers = commands.add_parser("target", help="List, enroll, drain, recover, or remove campaign servers")
    actions = servers.add_subparsers(dest="action", required=True)
    for name in ("list", "add", "drain", "resume", "requalify", "remove"):
        action = actions.add_parser(name)
        action.add_argument("--campaign", required=True)
        if name != "list":
            action.add_argument("--id", required=True)
        if name == "add":
            action.add_argument("--server", required=True, help="JSON file containing one complete server definition")
    trials = commands.add_parser("trial", help="Inspect availability or recover an explicitly selected saved trial")
    actions = trials.add_subparsers(dest="action", required=True)
    for name in ("actions", "resume", "fail"):
        action = actions.add_parser(name)
        action.add_argument("--campaign", required=True)
        action.add_argument("--number", required=True, type=bounded_integer(1, 2**31 - 1))
        action.add_argument("--timeout", type=positive_seconds, default=30)
        if name == "fail":
            action.add_argument("--reason", required=True, type=nonblank)
    monitor = commands.add_parser("watch", help="Follow current progress and readable generation activity without changing work")
    owner = monitor.add_mutually_exclusive_group()
    owner.add_argument("--campaign")
    owner.add_argument("--screening")
    monitor.add_argument("--interval", type=positive_seconds, default=2)
    monitor.add_argument("--timeout", type=positive_seconds, default=300)
    monitor.add_argument("--once", action="store_true", help="Read one bounded status/activity batch")
    monitor.add_argument("--json", action="store_true", help="Emit newline-delimited JSON status and activity events")
    args = parser.parse_args(argv)
    if args.command == "backend":
        result = backend(args)
    else:
        base = base_url(args)
        capabilities(base, args.workspace)
        result = {"config": configuration, "status": status, "logs": logs, "diagnose": diagnose,
                  "campaign": lifecycle, "screening": lifecycle, "watch": watch, "target": target, "trial": trial}[args.command](args, base)
    if result is not None:
        print(status_table(result) if args.command == "status" and args.table else json.dumps(result, indent=2, ensure_ascii=False))
    return 2 if args.command == "campaign" and args.action == "validate" and not result["valid"] else 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except KeyboardInterrupt:
        raise SystemExit(130)
    except (OSError, URLError, RuntimeError, ValueError, KeyError) as error:
        print(str(error), file=sys.stderr)
        raise SystemExit(1)
