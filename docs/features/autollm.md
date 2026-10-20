# AutoLLM campaigns

AutoLLM repeats the component-generation and autoconfig procedure independently. Each trial starts from the same Git commit, opens a new Codex conversation, verifies the proposed components, tunes them with irace, and compares the first-ranked proposal against a fixed SOTA configuration.

Start the backend using the example project's application:

```bash
java -jar target/project.jar --autollm
```

Open its usual frontend URL, for example `http://localhost:8080`. AutoLLM binds the server to `127.0.0.1`, keeps it running after a campaign finishes, and provides a separate campaign dashboard. Generation and child processes continue when the browser is closed.

## Configure with Codex

This repository includes the [mork-autollm skill](../../.agents/skills/mork-autollm/SKILL.md). Invoke it in a Codex conversation working in this checkout, for example:

```text
$mork-autollm Configure AutoLLM for example-bmssc with 10 independent trials.
$mork-autollm Start the campaign using the saved configuration in autollm/bmssc.json.
$mork-autollm Check the status of campaign <campaign-id>.
```

The skill discovers project settings and prerequisites, saves a complete configuration, and validates it through Mork. Configuration-only requests leave the campaign unstarted; run/start requests launch it. The skill returns the dashboard URL, campaign ID, and evidence location, and supports later status, cancel, resume, and report requests through the existing API.

## Prerequisites

Use a local Git repository and a committed baseline. Uncommitted changes in the original checkout are not included. AutoLLM clones the selected commit and its submodules without modifying the original checkout. Supply baseline-only projects when evaluating fresh proposals; AutoLLM does not remove previously generated components from the existing examples.

The project must have a `SolutionValidator`, a build command that runs its tests, and either a `TimeLimitCalculator` or an explicit positive comparison time limit. Launch the parent using the same example project as the campaign so its objective definitions, instance importer, and Excel customizer match the child results.

Install Git, the project's Java/build tools, R, and the Codex CLI. Authenticate Codex with the account/API setup normally used by the CLI. AutoLLM checks `codex login status` before generating components. R needs `irace`, `httr`, and `jsonlite`; use the setup described in [irace](irace.md). AutoLLM does not install packages during measured work.

## Campaign settings

Configure the campaign through the frontend, or provide defaults under `autollm` in the application's configuration. Duration values are **milliseconds**. The workspace is fixed when the backend starts; the frontend can choose other campaign settings.

```yaml
autollm:
  project:
    repository: /absolute/path/to/baseline-project
    ref: HEAD
    build-command: ["./mvnw", "verify"]
    artifact: target/project.jar
    # Optional application arguments, including the baseline inventory filter.
    run-arguments: ["--blacklist=MyBlacklist"]
  workspace: ./autollm-runs
  trials: 10
  seed: 1234
  max-parameters: 299
  sota-file: ./autollm/sota.json
  # Optional additional seed component specifications.
  initial-configurations-file: ./autollm/seeds.json
  training-path: ./instances/train.index
  comparison-path: ./instances/test.index
  codex:
    executable: codex
    # Optional: prompt-file, prompt, model, reasoning-effort.
  preparation:
    pilot-duration-millis: 600000
    max-repairs: 3
  tuning:
    evaluations: 10000
    ignore-initial-millis: 10000
    interval-duration-millis: 50000
    log-scale-area: true
  comparison:
    repetitions: 30
    # Zero uses the project's calculator; a positive value overrides it.
    time-limit-millis: 0
  execution:
    # -1 uses Mork's max(1, availableProcessors / 2) on the execution host.
    workers: -1
    target: LOCAL
    watchdog-grace-millis: 30000
    codex-timeout-millis: 3600000
    build-timeout-millis: 1800000
    startup-timeout-millis: 300000
    search-space-timeout-millis: 120000
    phase-timeout-millis: 300000
    stage-timeout-millis: 604800000
    heap: 2g
```

Repository-relative paths resolve against `project.repository`; the artifact is relative to the trial clone. Instance inputs support the normal Mork directories, files, archives, and index manifests. Missing training/comparison settings fall back to the application's `instances.path.irace` and `instances.path.autollm-comparison` configuration. Training and comparison may overlap; the overlap is recorded.

For campaigns prepared with the AutoLLM skill, run [trivial-instance screening](instance-screening.md) on the full original pool before selecting preliminary training inputs. Mork's instance selector chooses the preliminary subset from retained inputs; final testing uses the entire retained pool, including training. Reuse only matching approved screening evidence and indexes. Legacy or missing indexes trigger preparation and review before campaign creation.

For a multi-module repository, configure the build argument array and JAR path explicitly, for example `["./mvnw", "-pl", "example-bmssc", "-am", "verify"]` and `example-bmssc/target/project.jar`. Build commands run in the clone. `run-arguments` supplies project-specific settings; it cannot override campaign-owned seeds, budgets, execution mode, workers, instance paths, or irace settings. A generated blacklist replaces the baseline inventory filter.

The SOTA file contains one [component JSON specification](autoconfig.md), such as:

```json
{
  "$component": "SimpleAlgorithm",
  "constructive": { "$component": "MyConstructive", "alpha": 0.3 },
  "improver": { "$component": "MyImprover" }
}
```

The optional additional-seed file contains an array of specifications. SOTA is always included and identical seeds are deduplicated. The baseline must be able to encode and actually execute the supplied seeds before generation starts. SOTA-based comparison limits are resolved once on the baseline, frozen, and shared by all trials and both comparison algorithms.

## Remote execution over SSH

The skill asks whether workloads should run locally or on a configured SSH server before preflight, builds or screening, unless the choice was already explicit. The controller, source analysis, Codex generation/repairs, authentication, transcripts, dashboard and report analysis stay local in both modes.

In SSH mode, builds/tests, metadata checks, probes, screening/selection, pilot, R/irace, winner export and comparisons execute on one Unix/Linux server. R and the evaluation JVM communicate over remote loopback. A detached supervisor owns each workload and its deadlines; closing SSH does not stop it. Schedulers, multiple nodes and parallel trials are not supported.

Build the attached runner with `./mvnw -pl autoconfig -am package`. The normal dependency JAR retains its purpose; `autoconfig/target/autoconfig-0.23-SNAPSHOT-remote-runner.jar` is the separate executable transferred to the server. Use a committed baseline containing the matching AutoLLM implementation. The server needs compatible Java/build tools, Git, R with `irace`, `httr` and `jsonlite`, and the selector's Python dependencies. Codex is required only locally. Preflight reports prerequisites without installing them.

For example, an additional Spring configuration file for TSPTW can contain:

```yaml
autollm:
  project:
    repository: /local/path/to/mork
    ref: THE_COMMITTED_BASELINE
    build-command: ["./mvnw", "-pl", "example-tsptw", "-am", "verify"]
    artifact: example-tsptw/target/tsptw-0.23-SNAPSHOT.jar
  execution:
    target: SSH
    workers: 32
    ssh:
      host: experiment-server
      workspace: /srv/mork/experiments
      java-executable: java
      runner-artifact: autoconfig/target/autoconfig-0.23-SNAPSHOT-remote-runner.jar
      # Optional alternative to the normal ~/.ssh/config:
      # config-file: /local/path/to/ssh-config
      environment:
        JAVA_HOME: /remote/path/to/jdk
        PATH: /remote/path/to/jdk/bin:/remote/path/to/maven/bin:/usr/local/bin:/usr/bin:/bin
        MORK_SELECTOR_PYTHON: /remote/path/to/selector-venv/bin/python3
  tuning:
    evaluations: 10000
    ignore-initial-millis: 1000
    interval-duration-millis: 9000
  comparison:
    repetitions: 30
    time-limit-millis: 10000
instance-screening:
  python: /remote/path/to/selector-venv/bin/python3
```

The project repository, controller workspace, runner JAR and optional SSH config file are local. SSH workspace/Java/environment and screening Python settings refer to the server. Use normal SSH aliases or `user@host`; existing SSH configuration supplies keys, ports and jump hosts. Both preflight and workloads use the configured noninteractive environment. Private keys and Codex authentication are not transferred.

Start the local example backend with `--autollm --spring.config.additional-location=/local/path/remote.yml`. `POST /api/autollm/targets/probe` accepts a complete configuration and returns host/runtime and effective workers. `/validate` performs static validation without connecting to SSH.

Target diagnostics include resolved executable paths, versions/output, individual prerequisite failures, physical memory and usable disk space. Java, Git, R packages and the selector are checked in the workload's noninteractive environment. Committed Maven/Gradle wrappers are checked after their source snapshot is staged. Prepare an isolated selector environment from `py-tools/instance-selector/requirements.txt` before measurements; its pinned setuptools dependency supplies the distutils compatibility needed by Yellowbrick on Python 3.12. Omit absent optional YAML settings; blank `ssh.configFile` and `ssh.runnerArtifact` are normalized to absent values too.

Workers default to `-1`, resolving Mork's `max(1, Runtime.availableProcessors() / 2)` on the execution host, including its resource allocation. Auto on 32 visible processors uses 16 workers; explicit `32` uses 32. Pilot/main evaluations and comparisons use the effective count. Screening retains one sequential warmed worker. Workers share the evaluation JVM's configured heap. Requested/effective counts and host identity are saved as evidence.

Complete [remote screening and approval](instance-screening.md#remote-screening), then configure the printed local mirror's published `train.index`/`test.index`. Remote campaigns require this approval and adopt the exact screened baseline JAR. Review remains local, while host/executable verification and approval execute remotely.

Create and save a draft ID before calling `POST /api/autollm/campaigns/{id}/baseline-check`. This metadata-only preparation returns `parameters`, `maxParameters`, `withinLimit` and its evidence directory without running probes, pilot or tuning. Read `baseline-preflight/check/execution/check.json` and start that same draft after preparation and authorization. During generation/repair, Codex receives a local helper command for remote builds/tests and metadata checks. It uses a file queue inside the trial clone, requiring neither SSH credentials nor network access in Codex's sandbox; diagnostics are mirrored locally. The controller owns all pilots, tuning and comparisons.

Baseline preparation also rebuilds the unchanged commit in `baseline-replay/source` and independently checks its dependency fingerprints and metadata before the first generation. Metadata request IDs and results are retained outside editable source in `metadata-requests`; the clone-local mailbox can be recreated without repeating accepted requests. Helper waits are bounded. A helper failure classified as infrastructure pauses the controller rather than asking Codex to repair the framework.

Transfers include content manifests, modes and Git/submodule bundles. Path mapping is explicit. Complete output is checksum-verified; live logs use cursors. Original remote journals are preserved and local reports resolve their instance paths through the mirror.

`WAITING_FOR_REMOTE` retains the saved job for reconnection. `CANCELLATION_PENDING` means server termination has not yet been confirmed; the remote watchdog remains active. Resume reconciles saved jobs instead of duplicating them. Remote PIDs are never treated as local processes. Collected reports can be regenerated offline without retuning. Evidence is retained until explicitly cleaned up.

Connection/status/collection retries are bounded to two automatic retries. Persistent faults enter `NEEDS_ATTENTION`, leaving detached workloads under their remote watchdogs. An unresolved remote job reserves its local workspace until its result or cancellation is collected. Resume or cancel that saved operation before starting conflicting work. An idle cancellation request reconciles only saved remote jobs, without launching generation or a fresh experiment. Laptop sleep does not stop a remote workload; local Codex and progression to later stages require the controller to return.

## Backend discovery and operator helper

Checkpoint and runner protocol version 2 use a fresh workspace. There is no migration or recovery adapter for version 1 campaigns; inspect those artifacts with the original controller. `GET /api/autollm/capabilities` identifies the mode, API/checkpoint/runner versions, supported features, executable checksum, working directory, PID and workspace. Verify these before reusing a backend.

The skill includes a dependency-free Python helper:

```bash
python3 .agents/skills/mork-autollm/scripts/mork_autollm.py --workspace /absolute/new-workspace \
  backend --jar /absolute/app.jar --cwd /absolute/application --port 0
python3 .agents/skills/mork-autollm/scripts/mork_autollm.py --workspace /absolute/new-workspace \
  config --overrides /absolute/choices.json --output /absolute/campaign.json
python3 .agents/skills/mork-autollm/scripts/mork_autollm.py --workspace /absolute/new-workspace status
```

`backend` reuses only a matching executable and workspace, otherwise starts a detached owned process with stdin closed and redirected logs. `--port 0` selects a free port without replacing unrelated backends. The backend maintains `session.json`, `campaign-session.json` and `screening-session.json`; the launcher retains `backend-launch.json`. Configuration merging preserves explicit values and omits optional nulls. Add `--spring` with a `.yml`/`.yaml` output to write JSON-compatible Spring YAML.

`logs --campaign ID --path trials/.../stdout.log --follow` reads bounded byte cursors. `diagnose --campaign ID --output diagnostics.zip` exports status, failure evidence, an artifact index and bounded log tails without changing the experiment. The API's `/effective-config` and campaign `/summary` expose tuning/comparison horizons, resolved comparison limits, requested/effective workers and workload estimates. Estimates exclude generation, builds and repairs and do not guarantee wall time. Unspecified settings retain backend defaults; explicit user choices take precedence.

The same helper exposes typed lifecycle actions; use its `campaign --help` or `screening --help` rather than assembling HTTP requests. For example, append these commands to `python3 .agents/skills/mork-autollm/scripts/mork_autollm.py --workspace /absolute/new-workspace`:

```text
campaign validate --config /absolute/campaign.json
campaign probe --config /absolute/campaign.json
campaign create --config /absolute/campaign.json
campaign baseline-check --id SAVED_ID
campaign summary --id SAVED_ID
campaign start --id SAVED_ID
watch --campaign SAVED_ID
```

Configuration commands accept plain JSON and the JSON-compatible Spring YAML produced by `config --spring`. Validation prints its errors and exits with status 2 when invalid. `create` returns a saved draft without starting it. Lifecycle mutations require an explicit saved `--id`; `resume`, `cancel` and `report` use the same campaign. `probe` defaults to a 120-second HTTP timeout and `baseline-check` to 3600 seconds, both adjustable with `--timeout`. The helper never automatically repeats a mutation after a lost response; inspect saved work before retrying.

`watch` selects an active campaign or screening job, or the latest backend-maintained session when idle. Explicit `--campaign ID` or `--screening ID` pins the selection. It shows the active stage, elapsed time, observable generation counters, separate repair/retry counts, remote state and structured failures. Readable activity follows the selected trial's latest generation/repair attempt through preparation, with independent byte cursors and partial-line buffering. Each poll reads at most four activity pages; `activityCaughtUp` indicates whether that batch reached the end. Use transcripts or logs for complete history.

Watching changes no campaign state. It stops on completion, a draft, interruption, an approval request or `NEEDS_ATTENTION`; `WAITING_FOR_REMOTE` and `CANCELLATION_PENDING` keep polling. Defaults are a two-second interval and a five-minute watch limit. `--once` reads one bounded batch; `--json` emits newline-delimited status/activity records with a final `watch-ended` reason. A watch timeout or Ctrl-C leaves workloads running.

Helper tests use the standard library: `python3 -B -m unittest discover -s .agents/skills/mork-autollm/scripts`. To include the packaged-backend smoke test, set `MORK_AUTOLLM_TEST_JAR` to an absolute executable JAR and `MORK_AUTOLLM_TEST_CWD` to its application directory. The smoke test uses a temporary workspace, checks draft/lifecycle/watch/diagnostic commands and served dashboard assets, and stops only its owned backend; it runs no screening, generation or tuning workloads.

SSH integration tests create an isolated loopback `sshd` with temporary keys and configuration. Set `MORK_TEST_SSHD` to override `/usr/sbin/sshd`; these fixtures do not use a real server or the user's SSH settings. The complete remote campaign fixture also needs the R packages above and the selector dependencies in `example-tsptw/venv`.

## Trial lifecycle

1. **Generate:** `codex exec --json` works in a fresh clone and returns a structured inventory/component/filter manifest. Shared source instructions are appended to every generation and repair turn, including custom prompts and resumed conversations. They preserve existing code with exceptions for the project's exploration filter and additive model support, described below. All newly discovered algorithm components must end in `New`; utility and supporting classes are exempt.
2. **Prepare:** Mork independently runs the build/tests, checks dependency contents, constructs the actual search space, checks the parameter limit and seed encodings, executes seeds and targeted component probes, and runs a short real autoconfig pilot. Fingerprint version 2 ignores ZIP timestamps/order and only the generated root `builddef.lst` entry in `mork`/`mork-common` dependencies. All class files and other resources remain checked; raw JAR and excluded metadata hashes remain evidence. Mismatches list the exact changed archive entries. Generation/startup and individual evaluations have bounded deadlines and a configured heap.
3. **Repair or pause:** positively attributed component failures return diagnostics through `codex exec resume <exact-thread-id>`. Up to `max-repairs` completed repair turns are allowed. Infrastructure, integrity and unclassified failures pause in `NEEDS_ATTENTION` without charging component repairs. Transient infrastructure failures allow two retries, retaining each attempt and its diagnostics. A compiler failure naming a changed source can be attributed; a generic failed build or unchanged framework test is not sufficient evidence for component repair.
4. **Tune:** a verified component set, search space, and JAR are frozen. Main tuning starts in a fresh directory with the original seeds and the fixed evaluation budget. Pilot elites and racing state are not reused.
5. **Compare:** a separate JVM runs exactly `SOTA` and `IraceProposal`, with paired repetition seeds and the same outer restart policy and per-instance time budgets. It uses the existing validator, metrics, Excel serializer, and customization hook.

Java assertions are enabled for seed/component probes and the short preparation pilot. Main tuning and the final SOTA/proposal comparison explicitly disable assertions in their fresh JVMs, avoiding per-move debug validation during measured optimization. Metadata checks and winner export also disable assertions. Maven tests keep assertions enabled. Final solution validation still runs during tuning and comparison.

Source instructions allow changing the existing project exploration filter to prune composition branches while keeping every supplied seed encodable and executable. Adding a second active exploration filter can make dependency injection ambiguous; add one only when the project has none. Inventory blacklists exclude entire component classes and cannot replace branch-specific exploration rules.

Generation and repair instructions require measuring the actual emitted irace parameter declaration count before submitting a proposal. Metadata-only search-space checks are permitted; agents must not run their own pilot, tuning, or comparison. If the space exceeds the configured cap, prune and recheck while preserving every supplied seed. Overlapping composition wrappers are a useful first target: for example, exclude `SequentialImprover` when `VND` is enabled and no seed requires it, or prune nested wrappers. This is a search-budget choice; the wrappers have different execution semantics. The controller independently enforces the cap before probes and tuning, and returns failures as repair feedback.

Instance, Solution, and Move classes may gain helper methods and state, including necessary initialization, copy, and update wiring. Preserve existing APIs, scoring, feasibility, and behavior, and add tests for the extensions. Copy existing algorithm components before changing them. Validators, objective definitions, importers, existing tests, build files, framework code, and dependency contents remain subject to preservation instructions. Add new utilities, factories, and parameter providers where needed; select inventory filters and configure tree depth or derivation repetition through existing settings.

Source preservation is instruction-based: preparation does not automatically reject edits to existing trial sources, and tests and probes do not prove semantic equivalence. Baseline and final source hashes remain evidence; frozen inputs, archive consistency, dependency contents, and frozen artifacts retain their integrity checks. The original checkout and baseline clone remain separate from trial edits.

Expected rejected combinations, infeasible results, and missing AUC evidence remain normal tuning penalties. A component exception found by a targeted probe produces repair feedback. A normal time-limit expiry with a valid incumbent remains successful; an excessive overrun is rejected. A hard hang in preparation stops that attempt and may consume a repair turn. A hard hang in main tuning or comparison fails that trial and the campaign continues to the next one. 
The dashboard shows preparation substages, elapsed time, generation readiness and last activity, readable messages/commands/edits, separate repair and infrastructure retry counts, tuning outcomes, winner JSON and saved comparisons. `LAUNCHING` means no session event has arrived; `SESSION_READY` records a thread; `ACTIVE` records observed turn/tool activity. Startup timeout uses `execution.startupTimeoutMillis`, independently of the full generation deadline. Turns, commands, edit events and assistant messages count observable CLI events, not hidden model iterations. Live reported usage remains separate from authoritative archived usage. Large outputs are previewed with raw downloads retained.

Cancel stops owned processes and confirms remote evidence collection. Resume preserves completed work, rejoins unresolved SSH jobs, and uses fresh attempts for interrupted work that needs restarting. Completed failed trials stay failed. Campaigns, screening and baseline checks share workspace ownership.

## Saved evidence

Each UUID directory contains `campaign.json`, append-only campaign events, frozen control files and instance manifests, baseline metadata, and independent `trials/001`, `trials/002`, etc. Trial directories retain source, a frozen source ZIP and hashes, Codex prompts/events/usage/manifest/session ID, build logs, seed/component probes, pilot and main evaluation journals, all encountered configurations, elite history, final elites, irace R data, winner specifications, and comparison output.

Each generation or repair attempt also retains `conversations/<thread-id>.jsonl`: an immutable, byte-for-byte snapshot of the native Codex transcript, including previous turns and locally persisted child conversations linked to that thread. These contain the messages, tool activity, and reasoning exposed by Codex's local storage. AutoLLM reads `CODEX_HOME` (default `~/.codex`) and supports native JSONL sessions under `sessions` and `archived_sessions`. It verifies the exact thread ID and trial directory; unrelated conversations and authentication files are excluded. A required archive failure pauses as an integrity problem without requesting a repair. Archival runs after success, failure, timeout, cancellation, and recovery of an interrupted attempt.

Each attempt saves `evidence-before.json`, `evidence.json`, and `usage.json`. Usage preserves reported input, cached input, cache-write input, output, reasoning output, and total tokens, plus resolved models, CLI version, and the counter source. Per-response native records are preferred and response IDs are deduplicated; older cumulative native counters use validated deltas from the pre-attempt checkpoint. CLI-reported usage is supplemental because resumed turns may report cumulative totals. Missing fields remain `null`; usage is marked `COMPLETE`, `PARTIAL`, or `UNAVAILABLE` and does not block tuning. Cached input is part of input and reasoning output is part of output, so these fields must not be added again to total tokens.

`trials/<number>/llm-usage.json` and `reports/llm-usage.json` summarize all generation and repair attempts, including failed and interrupted attempts. Partial totals include only known counts and may underestimate usage. The dashboard and campaign API expose these summaries and transcript downloads. Report regeneration uses saved artifacts. Historical campaigns require their original controller; missing historical usage is not reconstructed. No cost or pricing statistics are collected.

Comparison records preserve objective values, runtime, time-to-best, curves, seeds, instance paths, repetitions, and failure/interruption outcomes. Each comparison retains its normal workbook; `reports/campaign.xlsx` pools successful cached result rows without loading generated classes into the parent. `reports/comparison-rows.json` also retains partial and failed trial data. Workbook gaps use the existing best-reference calculation. Artifact listings include SHA-256 checksums.

This experiment measures the combined Codex-and-tuning procedure. Component selection reports which components the winner uses; it does not establish each component's individual contribution. Every independent trial's data remains available for later analysis.

## API

All paths below are relative to `/api/autollm`; endpoints are registered when the parent is launched with `--autollm`.

| Endpoint | Purpose |
| --- | --- |
| `GET /capabilities`, `/defaults` | Controller identity, supported protocols and default settings |
| `POST /validate`, `/effective-config`, `/targets/probe` | Static validation, budget summary and target prerequisite diagnostics |
| `GET/POST /campaigns`, `GET /campaigns/{id}` | List, save a draft, inspect checkpoints |
| `POST /campaigns/{id}/baseline-check` | Baseline build/reproducibility/metadata checks without tuning |
| `POST /campaigns/{id}/start`, `/resume`, `/cancel`, `/report` | Lifecycle actions; reporting uses cached results |
| `GET /campaigns/{id}/summary` | Effective budgets and estimates, including frozen comparison limits |
| `GET /campaigns/{id}/trials/{number}/progress`, `/comparison` | Active attempt, failure, generation and nested workload progress; saved comparison data |
| `GET /campaigns/{id}/trials/{number}/activity?attempt=N&cursor=0` | Readable generation events in bounded byte pages |
| `GET /campaigns/{id}/artifacts`, `/artifact?path=...` | Evidence index and downloads |
| `GET /campaigns/{id}/log?path=...&cursor=0&maximumBytes=65536` | UTF-8-safe byte cursor; returns `cursor`, `nextCursor`, `text`, `more`, `reset` |
| `GET/POST /screenings`, `GET /screenings/{id}` | Durable dataset preparation jobs; create returns a draft |
| `POST /screenings/{id}/resume`, `/cancel`, `/approve` | Start/resume, reconcile cancellation, publish an explicitly reviewed selection |
| `GET /screenings/{id}/review`, `/artifact?path=...`, `/log?path=...&cursor=0` | Completed proposal/report/hash, evidence and bounded logs |
