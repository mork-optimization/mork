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
  comparison-path: ./instances/comparison.index
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
    workers: 1
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

## Trial lifecycle

1. **Generate:** `codex exec --json` works in a fresh clone and returns a structured inventory/component/filter manifest. The default prompt preserves existing source, validators, tests, objectives, and build files. All newly discovered algorithm components must end in `New`; utility and supporting classes are exempt.
2. **Prepare:** Mork independently runs the build/tests, checks preserved-source hashes and dependency contents, constructs the actual search space, checks the parameter limit and seed encodings, executes seeds and targeted component probes, and runs a short real autoconfig pilot. Dependency content fingerprints ignore ZIP timestamps and entry order; raw library checksums are also retained. Generation/startup and individual evaluations have bounded deadlines and a configured heap.
3. **Repair:** preparation failures return diagnostics and log paths through `codex exec resume <exact-thread-id>`. Up to `max-repairs` additional turns are allowed. Each turn and verification attempt is retained.
4. **Tune:** a verified component set, search space, and JAR are frozen. Main tuning starts in a fresh directory with the original seeds and the fixed evaluation budget. Pilot elites and racing state are not reused.
5. **Compare:** a separate JVM runs exactly `SOTA` and `IraceProposal`, with paired repetition seeds and the same outer restart policy and per-instance time budgets. It uses the existing validator, metrics, Excel serializer, and customization hook.

Expected rejected combinations, infeasible results, and missing AUC evidence remain normal tuning penalties. A component exception found by a targeted probe produces repair feedback. A normal time-limit expiry with a valid incumbent remains successful; an excessive overrun is rejected. A hard hang in preparation stops that attempt and may consume a repair turn. A hard hang in main tuning or comparison fails that trial and the campaign continues to the next one. 
The dashboard shows trial stages, repair counts, tuning outcomes, component/parameter JSON for the winner, comparison summaries and convergence curves, logs, and downloadable artifacts. Cancel stops owned processes. Resume keeps completed stages and trials, and restarts interrupted work in a new attempt directory with the same inputs and seed. Completed failed trials remain failed. Only one campaign runs per workspace.

## Saved evidence

Each UUID directory contains `campaign.json`, append-only campaign events, frozen control files and instance manifests, baseline metadata, and independent `trials/001`, `trials/002`, etc. Trial directories retain source, a frozen source ZIP and hashes, Codex prompts/events/usage/manifest/session ID, build logs, seed/component probes, pilot and main evaluation journals, all encountered configurations, elite history, final elites, irace R data, winner specifications, and comparison output.

Each generation or repair attempt also retains `conversations/<thread-id>.jsonl`: an immutable, byte-for-byte snapshot of the native Codex transcript, including previous turns and locally persisted child conversations linked to that thread. These contain the messages, tool activity, and reasoning exposed by Codex's local storage. AutoLLM reads `CODEX_HOME` (default `~/.codex`) and supports native JSONL sessions under `sessions` and `archived_sessions`. It verifies the exact thread ID and trial directory; unrelated conversations and authentication files are excluded. A required archive failure stops that trial without requesting a repair. Archival runs after success, failure, timeout, cancellation, and recovery of an interrupted attempt.

Each attempt saves `evidence-before.json`, `evidence.json`, and `usage.json`. Usage preserves reported input, cached input, cache-write input, output, reasoning output, and total tokens, plus resolved models, CLI version, and the counter source. Per-response native records are preferred and response IDs are deduplicated; older cumulative native counters use validated deltas from the pre-attempt checkpoint. CLI-reported usage is supplemental because resumed turns may report cumulative totals. Missing fields remain `null`; usage is marked `COMPLETE`, `PARTIAL`, or `UNAVAILABLE` and does not block tuning. Cached input is part of input and reasoning output is part of output, so these fields must not be added again to total tokens.

`trials/<number>/llm-usage.json` and `reports/llm-usage.json` summarize all generation and repair attempts, including failed and interrupted attempts. Partial totals include only known counts and may underestimate usage. The dashboard and campaign API expose these summaries and transcript downloads. Report regeneration uses saved artifacts. Older campaigns remain readable with unavailable evidence; missing historical usage is not reconstructed. No cost or pricing statistics are collected.

Comparison records preserve objective values, runtime, time-to-best, curves, seeds, instance paths, repetitions, and failure/interruption outcomes. Each comparison retains its normal workbook; `reports/campaign.xlsx` pools successful cached result rows without loading generated classes into the parent. `reports/comparison-rows.json` also retains partial and failed trial data. Workbook gaps use the existing best-reference calculation. Artifact listings include SHA-256 checksums.

This experiment measures the combined Codex-and-tuning procedure. Component selection reports which components the winner uses; it does not establish each component's individual contribution. Every independent trial's data remains available for later analysis.

## API

The frontend uses `/api/autollm/defaults`, `/validate`, and `/campaigns`; campaign actions are `POST /campaigns/{id}/start`, `/cancel`, and `/resume`. `POST /campaigns/{id}/report` regenerates cached reports while the workspace is idle, without retuning. Trial `/progress` and `/comparison` endpoints expose persisted child data. Campaign `/artifacts`, `/artifact?path=...`, and paged `/log?path=...&offset=0&limit=100` provide evidence and downloads. AutoLLM endpoints are available only when the parent is launched with `--autollm`.
