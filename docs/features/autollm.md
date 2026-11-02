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

The skill discovers project settings and prerequisites, saves a complete configuration, and validates it through Mork. Configuration-only requests leave a validated draft. A run request prepares a concrete campaign proposal; the user reviews its dataset and effective configuration before approving launch. The skill returns the dashboard URL, campaign ID, and evidence location, and supports later status, cancel, resume, and report requests through the existing API.

## Prerequisites

Use a local Git repository and a committed baseline. Uncommitted changes in the original checkout are not included. AutoLLM clones the selected commit and its submodules without modifying the original checkout. Supply baseline-only projects when evaluating fresh proposals; AutoLLM does not remove previously generated components from the existing examples.

The project must have a `SolutionValidator`, a build command that runs its tests, and either a `TimeLimitCalculator` or an explicit positive comparison time limit. Launch the parent using the same example project as the campaign so its objective definitions, instance importer, and Excel customizer match the child results.

Install Git, the project's Java/build tools, R, and the Codex CLI. Configure Codex with the provider and credentials normally used by the CLI. AutoLLM checks the CLI version and lets Codex handle provider authentication during generation; local providers do not require an OpenAI login. R needs `irace`, `httr`, and `jsonlite`; use the setup described in [irace](irace.md). AutoLLM does not install packages during measured work.

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
    max-concurrent-sessions: 2
    # Optional: prompt-file, prompt, model, reasoning-effort.
  preparation:
    probe-duration-millis: 600000
    pilot-duration-millis: 600000
    max-repairs: 3
    local-builds: 2
    ready-queue-size: 4
    local-workers: 1
    local-heap: 2g
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

Campaigns and target workspaces retain training/comparison indexes and hash manifests that reference the original inputs. They do not create another loose copy of each instance. Archive members keep their `archive.zip!member` load paths. Membership, manifests and original payloads are checked before recovery and child workloads, so keep the referenced datasets available and unchanged. SSH stages send the required original files or archive containers in verified input layers, with indexes and time-limit keys translated for the remote workspace; local staging does not extract or copy those payloads into each target directory. Existing historical snapshots remain readable through their saved paths.

For campaigns prepared with the AutoLLM skill, run [trivial-instance screening](instance-screening.md) on the full original pool before selecting preliminary training inputs. Mork's instance selector chooses the preliminary subset from retained inputs; final testing uses the entire retained pool, including training. Reuse matching screening evidence and proposed indexes; approval applies to the complete campaign. Legacy or missing indexes trigger preparation and review before campaign creation.

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

## Review, approval and local preparation

Create a draft and run its baseline check. `GET /campaigns/{id}/proposal` returns a digest, exact training/testing membership, screening decisions (including trivial exclusions and inconclusive inputs), selected servers/readiness and the complete effective configuration. Testing includes training. `POST /campaigns/{id}/proposal` with `{"targets":["server-a"]}` selects an explicit launch subset for review.

Review these values with the user. Submit the reviewed `digest` to `POST /campaigns/{id}/approve`, then start the campaign. Start/resume verifies the current digest and input provenance. A broad optimization request permits preparation but still requires explicit approval of this concrete proposal. Unchanged recovery/reconnect retains approval. An enrolled server can qualify before an amendment is approved, but receives trials only after approval includes it. `POST /campaigns/{id}/amend-trials` proposes an increased total trial count; it creates no trials until the amended proposal is approved.

`PUT /campaigns/{id}/draft` accepts revised configuration. Settings that require requalification produce a replacement draft while retaining screening evidence. Changes to trial count/seed can retain qualification when other settings match. `PUT /campaigns/{id}/dataset` accepts `training`, `testing` and an `excluded` object mapping ignored instance paths to reasons; testing plus exclusions must account for the complete original pool. Dataset edits create a replacement draft and do not rerun screening. Approval is tied to the exact revision. The dashboard provides prepare, review/edit and approve/launch actions.

Trial data lives at `trials/<number>/`, independently of server assignment. Generation and local build/metadata preparation proceed while servers are occupied, bounded by Codex permits, `preparation.localBuilds` and ready queue capacity. Local preparation has its own workers/heap. Probes/pilot still execute on the selected measurement host. Pre-tuning component repairs return to local preparation after evidence collection; tuning and later stages retain their target. Resume reuses prepared outputs and saved remote job IDs.

Detailed component method timing defaults off for AutoLLM children. Opt in with `project.runArguments: ["--solver.time-stats=true"]`. Objective curves, per-evaluation execution times and time to own best are retained. Result collection records archive size and download/collection duration.

Generated irace real domains use two decimal places consistently for export, seeds and probe sampling. Bounds must be finite, strictly ordered and exactly representable at that precision. For example `(0.9, 0.9999)` fails during metadata construction with a declaration diagnostic requiring four digits; it never reaches the pilot. Invalid newly generated components receive repair feedback; baseline/framework failures remain outside component repairs.

## Compact operations and comparison tables

The operator helper defaults to compact status and mutation receipts. Use global `--json` for machine-readable output, `campaign get` for full saved state, and bounded diagnostics when needed. API/checkpoint/runner version 7 requires the matching controller and remote runner. Historical campaigns retain their matching controller/operator; version 7 does not migrate them.

```bash
python3 .agents/skills/mork-autollm/scripts/mork_autollm.py --workspace WORKSPACE backend status
python3 .agents/skills/mork-autollm/scripts/mork_autollm.py --workspace WORKSPACE backend restart
python3 .agents/skills/mork-autollm/scripts/mork_autollm.py --workspace WORKSPACE status --campaign ID
python3 .agents/skills/mork-autollm/scripts/mork_autollm.py --workspace WORKSPACE remote --campaign ID --trial 3
python3 .agents/skills/mork-autollm/scripts/mork_autollm.py --workspace WORKSPACE recover --campaign ID --trial 3 --trial 6
python3 .agents/skills/mork-autollm/scripts/mork_autollm.py --workspace WORKSPACE compare --campaign ID --objective Cost --format markdown
```

`backend status` reports the saved address separately from reachability and observed process/artifact identity. It never launches a process. `backend restart` reuses saved launch metadata and checks the saved artifact. Compact campaign observations omit historical conversation/curve data and mark unavailable progress explicitly. Remote inspection checks saved job/host identity, actual supervisor/child identity, terminal and collection evidence, and progress timestamps; inspection does not reconcile or restart the job.

SSH reconnect policy lives under `execution.ssh.reconnect`: `initialDelayMillis` defaults to 2000, `maximumDelayMillis` to 60000 and `budgetMillis` to 1800000. Consecutive failures use exponential backoff with jitter. The continuous-disconnection budget and next attempt are persisted; successful synchronization resets backoff. Explicit SSH-agent refusal or communication failures use this reconnect policy, including when followed by `Permission denied (publickey)`. Reconnection reconciles the saved job; exhaustion leaves it detached for intervention. Authentication failures without an agent-unavailability diagnostic, configuration, host identity and integrity failures require intervention. Cancellation remains pending until confirmed remotely. Collection retries reuse the completed job rather than retuning.

New campaigns use schema 7 and require a fresh workspace; historical checkpoints stay with their original controller. A trial retains its editable source, conversation, seeds and local generation evidence. Each execution assignment owns `trials/<trial>/assignments/<id>` with its frozen source input, resolved target configuration, host identity, transport, staging and remote jobs. Assignment IDs also appear in worker namespaces and attempt records. A repair may release an assignment only after all its jobs are terminal and collected; the replacement repeats host preparation. A durable pin written before tuning submission keeps tuning, export and comparison on the same assignment. Recovery, reports and cleanup resolve the recorded assignment rather than a later target definition.

Version 7 uses a host-wide worker registry, shared across campaign directories and SSH aliases. Provision it once, after draining older controllers: `/var/lib/mork/worker` on Linux, `/Library/Application Support/Mork/worker` on macOS, or `%ProgramData%/Mork/worker` on Windows. Run the matching remote-runner JAR with `worker-init` and the absolute registry directory as its second argument, then grant participating accounts access to that directory and its files, including inherited permissions for newly created files (for example, a shared group with default ACLs). Provision the controller host too for local measurements and resource ownership. Missing permissions or nonexclusive filesystem locks fail preflight; there is no workspace-specific fallback. The registry supplies the persistent worker identity. Leases protect cooperating Mork workloads; unrelated software can still run on the host.

An assignment reserves its worker before preparation and keeps the reservation through tuning, comparison and collection. Screening and qualification hold operation-scoped reservations. Controller disconnection and heartbeat expiry do not release them. Durable submission intent, process identity, token and generation prevent duplicate submissions and stale reuse. Uncertain surviving children block release. `worker status --owner PATH/execution-owner.json` reports the reservation, and `worker release --owner PATH/execution-owner.json` releases only reconciled work. `WAITING_FOR_WORKER` means another owner holds the host; it consumes no component repair and does not mark the host unhealthy.

`remote --campaign ID --trial N` inspects the active assignment. Add `--assignment ID` or `--all-assignments` to inspect historical assignments through their recorded hosts. Inspection includes assignment and worker identities. Qualification, worker availability, host health and trial blockage are separate fields. A blocked component/build trial need not imply a failed host. Last successful remote contact is separate from persisted evaluation progress sequence and last meaningful change; unchanged polls do not advance evaluation progress.

### Complete cleanup

`cleanup plan --kind campaign --id ID` reviews deletion of a campaign and its exclusively associated resources. Other scopes are `screening`, `probe`, and `workspace` (the last requires no ID). Plans show owned local/remote directories, exact native transcripts, remote jobs, shared resources retained and ownership/reachability blockers. Review the digest, then run `cleanup apply --digest DIGEST`. Use `cleanup status --digest DIGEST` and repeat apply to resume pending hosts. Changed references or boundaries require a new plan. The dashboard exposes campaign cleanup through the same API.

Apply cancels selected work, confirms termination and lease release, then deletes complete exclusively owned directories, including generated files not present at review time. It never follows directory symlinks, deletes repository roots, guesses native session ownership, or treats a reused PID as the recorded process. Shared dependencies remain. Offline hosts leave cleanup pending, with per-host receipts outside the deleted workspace. Disposable result archives need not be downloaded. Add `--export /outside/scope/evidence.zip` to retain collected diagnostic evidence before deletion. Add `--retain-receipt` to retain the completion record; otherwise successful cleanup removes its operational plan and receipt, so later status is `UNKNOWN`. Shared registry infrastructure and content-addressed worker runner installations remain; cleanup plans list the runner installations explicitly as shared infrastructure.

Workspace plans may add `--shutdown`: after scoped coordinators stop, an external Java helper verifies the recorded controller identity, stops it and finishes deletion. No shutdown occurs for narrower scopes. The helper reads external receipts for `cleanup status` after shutdown; `cleanup apply` can restart the recorded, checksum-verified external helper if remote deletion remains pending. `storage --maintain` remains conservative evidence compaction and is not complete cleanup.

The operator's `storage --campaign ID` command reads local campaign file sizes and filesystem free space through `GET /campaigns/{id}/storage`. It reports stored file lengths by category: compressed evidence contributes its archive size. Physical allocation is explicitly unavailable because portable Java APIs cannot account for shared filesystem extents. Inspection follows no symbolic links and does not clean up files. Its totals can change while a campaign is running; remote worker usage is not included. Use `storage --campaign ID --maintain` to retry verified maintenance for a completed campaign through `POST /campaigns/{id}/storage/maintain`. This requires an idle controller and campaign ownership, skips historical layouts, and never resumes experiments. Inspection defaults to a 120-second request timeout; maintenance defaults to 3600 seconds.

Fresh source checkouts are created from campaign-owned Git bundles containing the selected commit's reachable history, avoiding copies of the original repository's unrelated objects and temporary garbage. Writable checkouts remain independent. New-format campaigns share identical raw trial, baseline and runner JARs in `.store/artifacts`, and immutable transfer layers in `.store/layers`, using content hashes and persistent owner references under `.store/references`. Releasing one trial's reference cannot delete an asset still needed by another trial, runner or retained checkout. SSH workers share campaign transfer layers too; their runner cache is separately addressed by content hash in the worker workspace. Shared storage uses ordinary files and Git, without hard links, reflinks or APFS-specific features. Historical campaigns are not migrated or compacted automatically.

New campaigns record `control/storage-layout.json` and freeze trial sources as a baseline commit plus binary patches, with recursive submodule bundles stored once under `.store/git`. Snapshots include ignored and untracked inputs outside disposable `target`, `node_modules`, `.codex`, and `.mork-metadata` directories. Raw file overrides preserve bytes changed by Git filters or line-ending normalization. Preparation restores each snapshot into a fresh temporary checkout and verifies file hashes before publishing `frozen/source-snapshot.json`; it retains the build recipe and observed tool versions for future rebuilds. Existing campaigns without the storage marker keep their previous source archive behavior.

Use `source restore --campaign ID --trial N --output PATH` to restore a frozen trial through the controller. `PATH` must be a new directory outside the controller workspace. Restoration requires Git, verifies bundles, patches, file contents and submodule snapshots, and needs neither the original checkout nor the original submodule remotes. The controller endpoint is `POST /campaigns/{id}/trials/{number}/source/restore` with an `output` path. This command restores sources only; it does not build, delete executable JARs, or rerun experiments. The implementation targets Linux/macOS/Windows using ordinary files and Git commands; unsupported filesystem names or symbolic-link capabilities fail restoration rather than silently changing source content. Linux and Windows end-to-end validation of these storage changes remains pending.

After a new-format trial finishes and its remote jobs have collection receipts, the controller restores its source snapshot into a fresh temporary checkout, runs the saved build command using a local build slot, and verifies the resulting executable. The fingerprint compares application classes, resources, manifests and nested dependency contents, ignoring ZIP ordering/timestamps/compression and recognized AspectJ `builddef.lst` metadata. Both raw JAR hashes and excluded metadata hashes remain recorded. A metadata-only subprocess must also reproduce the frozen component inventory and search space; it runs no experiments. Older project commits without `--autollm-inventory` support retain their JARs when this check fails.

Only after those checks pass does maintenance release the trial's shared executable reference (or its loose `frozen/project.jar`) and a matching build artifact in the trial checkout. Failed builds, unavailable dependencies, changed contents, metadata mismatches or cancellation retain the executable without changing the trial outcome. The receipt at `storage/artifact-retention.json` records release status, verification evidence and released paths or references; `storage --campaign ID` includes these receipts. Verification adds a clean local build and temporarily requires its checkout/build space before reclaiming artifacts. A shared blob remains until its last owner releases it.

Terminal checkout retirement preserves a separate final source snapshot without changing the measured source snapshot. Failed trials without frozen sources get a verified source snapshot too. Files outside the source snapshot, including build reports and Git index/refs, are preserved in `storage/checkout/extras.zip`; extra JARs retain references to shared raw assets instead of being embedded repeatedly in these archives. Unique staged, reflog and unreachable Git objects are verified and retained under `.store/git-objects`; baseline history remains in the shared bundles. A complete reconstruction and a check of the remaining originals precede removal of the loose checkout. A measured trial whose artifact verification failed keeps its checkout. Unknown external Git object stores, changed files, corruption and incomplete collection retain data rather than forcing cleanup.

`storage/checkout/checkout.json` records the complete retained checkout and `retention.json` records its maintenance outcome. `AutoLlmCheckoutRetirement.restore(campaign, evidenceDirectory, destination, command)` is the Java recovery API for a new directory outside the campaign; it restores source, diagnostics, retained JARs, Git refs and the original staged index. The `source restore` command continues to restore the frozen source state only. After every campaign trial is terminal and all jobs are collected, target and local-preparation `baseline/source` and `baseline-replay/source` checkouts receive the same preservation checks. Unverified baseline JAR bytes remain retained in shared assets. Active, blocked and otherwise resumable work is not retired.

Closed trial logs, JSONL journals/transcripts, timing CSVs and text evidence of at least 4 KiB can be compressed into indexed ZIPs under sibling `.evidence` directories. Each archive contains independently compressed chunks, their checksums and the original whole-file identity. Every chunk and the complete content are verified before the loose copy is removed; conflicting or corrupt archives leave originals intact. Reporting, comparison, dashboard curves, artifact downloads and log byte/tail APIs continue to use the original logical filename and uncompressed byte offsets. Reading a selected range does not require unpacking the complete journal. Frozen control data, source snapshots and active evidence remain outside this compaction.

Remote collection verifies the result archive and retained local members, persists a durable collection receipt, and then acknowledges the exact job, archive hash and manifest hash to the worker. The worker records the acknowledgement before deleting that job's input/result ZIPs. Lost acknowledgements can be retried without rerunning the workload. Later retirement requires no active workload and acknowledgements for every owned job; it removes only files matching known collected/input/layer identities, then releases shared layer references. Job identities, states and acknowledgement receipts remain available. Unknown or changed remote files, including Git metadata that was never archived locally, are retained. Local transfer caches are released only after the corresponding collection and retirement checks.

Campaign proposals include a `storage` estimate and a single approval line showing estimated additional peak storage versus available space on the controller and each selected worker. The heuristic range accounts for remaining trials, concurrency, retained evidence/source patches, checkouts/builds, shared assets, transfer staging, rebuild/compression scratch space and possible dependency-cache growth. Its high range allows failed cleanup and retained artifacts; it is not a hard bound or a reservation. Controller capacity uses a live filesystem reading; worker capacity uses a bounded read-only query of an already staged runner. An unavailable current reading remains unknown, with any older observation labeled separately. The line warns when the estimated range may not fit. Observed capacity and estimates do not alter the reviewed proposal digest merely because time or free space changes.

Use `artifact rebuild --campaign ID --trial N --output PATH` to export a verified executable for a completed trial, including after its original JAR has been released. `PATH` must be a new directory outside the controller workspace. The controller endpoint is `POST /campaigns/{id}/trials/{number}/artifact/rebuild` with an `output` path. The result contains `project.jar`, restored sources and verification evidence. Builds may download dependencies using the saved project's build system; a later rebuild can fail if its toolchain or dependencies are no longer available. A failed export preserves its diagnostics and must be retried with a new output directory; it never changes the recorded measured artifact identity or reruns measurements.

Comparison tables use completed journal measurements, each trial's own host-matched SOTA, equal instance weights and explicit observed/expected repetitions. Invalid/incomplete results are excluded with reasons. Relative improvement is undefined for zero SOTA; absolute differences remain available. Common-reference ranking is explicit (`--ranking COMMON_SOTA`), requires matching instance identities and positive references, and uses each instance's mean SOTA across the selected complete runs. It implies no statistical significance. Time to own best does not measure time to a common target.

## Remote execution over SSH

For external VM provisioning, worker dependencies, and reusable images, see [preparing OpenStack workers](autollm-openstack.md). VM creation and provisioning stay outside Mork; the backend consumes ready SSH targets.

The skill asks whether workloads should run locally or on a pool of configured SSH servers before preflight, builds or screening, unless the choice was already explicit. The controller, source analysis, Codex generation/repairs, authentication, transcripts, dashboard and report analysis stay local in both modes.

In SSH mode, generation and initial build/tests/metadata checks run locally before a server is assigned. Remote build/dependency/metadata validation, probes, pilot, R/irace, winner export and comparisons execute on the assigned Unix/Linux server. Screening and selection run once on the selected screening server. R and the evaluation JVM communicate over remote loopback. A detached supervisor owns each workload and its deadlines; closing SSH does not stop it. The coordinator dispatches one remote trial per server and runs independent trials concurrently. It never splits an irace race across servers. Generation and repairs share a local semaphore, defaulting to two Codex sessions; adjust `codex.max-concurrent-sessions` before campaign approval.

Build the attached runner with `./mvnw -pl autoconfig -am package`. The normal dependency JAR retains its purpose; `autoconfig/target/autoconfig-0.23-SNAPSHOT-remote-runner.jar` is the separate executable transferred to the server. Use a committed baseline containing the matching AutoLLM implementation. Every execution server needs compatible Java/build tools, Git and R with `irace`, `httr` and `jsonlite`. Only the screening server needs the selector’s Python dependencies. Codex is required only locally. Preflight reports prerequisites without installing them.

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
      screening-server-id: server-a
      runner-artifact: /local/path/to/autoconfig/target/autoconfig-0.23-SNAPSHOT-remote-runner.jar
      servers:
        - id: server-a
          host: experiment-server-a
          workspace: /srv/mork/experiments
          java-executable: java
          environment: {PATH: /opt/jdk/bin:/usr/local/bin:/usr/bin:/bin, MORK_SELECTOR_PYTHON: /srv/selector/bin/python3}
        - id: server-b
          host: experiment-server-b
          workspace: /srv/mork/experiments
          java-executable: java
          workers: 16
          heap: 4g
          environment: {PATH: /opt/jdk/bin:/usr/local/bin:/usr/bin:/bin}
```

The project repository, controller workspace, runner JAR and optional SSH config file are local. SSH workspace/Java/environment and screening Python settings refer to the server. Use normal SSH aliases or `user@host`; existing SSH configuration supplies keys, ports and jump hosts. Both preflight and workloads use the configured noninteractive environment. Private keys and Codex authentication are not transferred.

Start the local example backend with `--autollm --spring.config.additional-location=/local/path/remote.yml`. `POST /api/autollm/targets/probe` accepts a complete configuration and immediately returns a saved probe ID, `RUNNING` status, and queued targets. Poll `GET /api/autollm/targets/probes/{id}` for per-target host/runtime, effective workers, prerequisites, and failures. Up to four targets run concurrently; checks within each target stay sequential. Results and diagnostics remain under `target-probes/{id}/` across HTTP disconnects. Interrupted controller processes leave inspectable `INTERRUPTED` results. `/validate` performs static validation without connecting to SSH.

Target diagnostics include resolved executable paths, versions/output, individual prerequisite failures, physical memory and usable disk space. Java, Git, R packages and the selector are checked in the workload's noninteractive environment. Committed Maven/Gradle wrappers are checked after their source snapshot is staged. Prepare an isolated selector environment from `py-tools/instance-selector/requirements.txt` before measurements; its pinned setuptools dependency supplies the distutils compatibility needed by Yellowbrick on Python 3.12. Omit absent optional YAML settings; blank server `configFile` and pool `runnerArtifact` are normalized to absent values too.

Workers default to `-1`, resolving Mork's `max(1, Runtime.availableProcessors() / 2)` on the execution host, including its resource allocation. Auto on 32 visible processors uses 16 workers; explicit `32` uses 32. Pilot/main evaluations and comparisons use the effective count. Screening retains one sequential warmed worker. Workers share the evaluation JVM's configured heap. Requested/effective counts and host identity are saved as evidence.

Complete [remote screening](instance-screening.md#remote-screening), then configure the local mirror's `proposed-train.index`/`proposed-test.index`. Review this evidence as part of campaign approval. Remote target qualification adopts the screened baseline JAR and verifies reproducibility; local preparation builds its own platform's baseline.

Create and save a draft ID before calling `POST /api/autollm/campaigns/{id}/baseline-check`. This preparation returns `parameters`, `maxParameters`, `withinLimit` and a `targets` map containing each server’s parameter count, failure, and evidence directory after baseline validation and probes, without pilot or tuning. Read `targets/<id>/baseline-preflight/check/execution/check.json` and review and approve that same draft before launch. During generation/repair, Codex receives a local helper command for preparation builds/tests and metadata checks. It uses a file queue inside the trial clone, requiring neither SSH credentials nor network access in Codex's sandbox; diagnostics are mirrored locally. The controller owns all pilots, tuning and comparisons.

Baseline preparation also rebuilds the unchanged commit in `baseline-replay/source` and independently checks its dependency fingerprints and metadata before the first generation. Metadata request IDs and results are retained outside editable source in `metadata-requests`; the clone-local mailbox can be recreated without repeating accepted requests. Helper waits are bounded. A helper failure classified as infrastructure pauses the controller rather than asking Codex to repair the framework.

Transfers include content manifests, modes and Git/submodule bundles. Path mapping is explicit. Complete output is checksum-verified; live logs use cursors. Original remote journals are preserved and local reports resolve their instance paths through the mirror.

`WAITING_FOR_REMOTE` retains the saved job for reconnection. `CANCELLATION_PENDING` means server termination has not yet been confirmed; the remote watchdog remains active. Resume reconciles saved jobs instead of duplicating them. Remote PIDs are never treated as local processes. Collected reports can be regenerated offline without retuning. Evidence is retained until explicitly cleaned up.

Connection/status/collection retries are bounded to two automatic retries. Persistent faults enter `NEEDS_ATTENTION`, leaving detached workloads under their remote watchdogs. An unresolved remote job reserves its local workspace until its result or cancellation is collected. Resume or cancel that saved operation before starting conflicting work. An idle cancellation request reconciles only saved remote jobs, without launching generation or a fresh experiment. Laptop sleep does not stop a remote workload; local Codex and progression to later stages require the controller to return.

### Pool scheduling and changes

Stable trial seeds are allocated before dispatch. A ready server takes the next unassigned trial, and every stage of that trial remains on its assigned server. Each server independently verifies the pinned commit, screened baseline artifact, rebuilt dependency contents, search-space metadata, frozen input hashes and seed probes before accepting work. Resolved duplicate SSH endpoints are rejected. Per-server worker and heap settings inherit the campaign defaults when absent. SSH control calls use a 60-second deadline and keepalives; artifact transfers retain their longer budget.

The reviewed manifest explicitly records `QUALIFIED_SERVER_POOL` reuse. Screening timings and exclusions remain measurements from the original screening host; qualifying another server does not claim that its timings match. The same training/testing indexes are frozen for the pool, with both SOTA and proposal evaluated together on the trial’s assigned host.

Add servers with the dashboard or the helper after a campaign starts:

```text
target list --campaign SAVED_ID
target add --campaign SAVED_ID --id server-c --server /absolute/server-c.json
target drain --campaign SAVED_ID --id server-a
target resume --campaign SAVED_ID --id server-a
target remove --campaign SAVED_ID --id server-a
```

The server JSON uses the fields in one `execution.ssh.servers` entry. IDs and definitions are immutable; repeating an identical PUT is idempotent. New servers qualify and take pending work automatically while the campaign is active. Draining stops new assignments and lets the current trial finish. Removal requires an idle, reconciled server without an unfinished pinned trial. A removed ID cannot be reused. A pool with pending work and only drained servers remains `WAITING_FOR_TARGETS` so servers can be added or resumed.

Failures quarantine the affected server and preserve its trial assignment and job descriptors; healthy servers continue. Transient retries are bounded and consume no component repairs. Resume reconnects the original server/job; it never migrates a partially executed trial. Global canonical-input integrity failures stop dispatch and cancel outstanding work. Cancel fans out to every assigned target and retains the workspace reservation until all remote jobs are reconciled. Controller shutdown interrupts local work while detached remote jobs retain their saved identities for explicit resume. The dashboard labels stale remote status and unavailable per-trial progress.

An operator can end a blocked or interrupted trial with `POST /api/autollm/campaigns/{id}/trials/{number}/fail` and a JSON body such as `{"reason":"IRACE exhausted model sampling; preserve the failed run and continue"}`. The target must remain qualified and have no active or uncollected jobs. This records the reason in `trial-failed-by-operator`, preserves the trial's original error, attempts, evidence, seeds and repair count, and marks it `FAILED`. In a running campaign it releases the target for pending work. In a stopped campaign it locks the workspace, saves the disposition, and leaves dispatch stopped until an explicit resume; another active operation prevents the change. A drained target stays drained. Repeating the action on an already failed trial has no effect. Completed failed trials stay failed on resume.

## Backend discovery and operator helper

API, checkpoint and runner protocol version 7 require a fresh workspace. There is no migration or recovery adapter for older campaigns; inspect those artifacts with the original controller. `GET /api/autollm/capabilities` identifies the mode, API/checkpoint/runner versions, supported features, executable checksum, working directory, PID and workspace. Verify these before reusing a backend.

The skill includes a dependency-free Python helper:

```bash
python3 .agents/skills/mork-autollm/scripts/mork_autollm.py --workspace /absolute/new-workspace \
  backend start --jar /absolute/app.jar --cwd /absolute/application --port 0
python3 .agents/skills/mork-autollm/scripts/mork_autollm.py --workspace /absolute/new-workspace \
  config --overrides /absolute/choices.json --output /absolute/campaign.json
python3 .agents/skills/mork-autollm/scripts/mork_autollm.py --workspace /absolute/new-workspace status
```

`backend` reuses only a matching executable and workspace, otherwise starts a detached owned process with stdin closed and redirected logs. `--port 0` selects a free port without replacing unrelated backends. The backend maintains `session.json`, `campaign-session.json` and `screening-session.json`; the launcher retains `backend-launch.json`. The helper writes the configuration returned by the server effective-configuration endpoint. Record constructors supply defaults; the server normalizes optional blanks, equivalent local paths, inherited server allocations and equivalent heap sizes. Commands, seed lists and dataset order remain significant; remote paths never resolve through the controller filesystem. Add `--spring` with a `.yml`/`.yaml` output to write JSON-compatible Spring YAML.

`logs --campaign ID --path targets/SERVER/trials/.../stdout.log --follow` reads bounded byte cursors. `diagnose --campaign ID --output diagnostics.zip` exports status, failure evidence, an artifact index and bounded log tails without changing the experiment. The API's `/effective-config` and campaign `/summary` expose tuning/comparison horizons, resolved comparison limits, requested/effective workers and workload estimates. Estimates exclude generation, builds and repairs and do not guarantee wall time. Unspecified settings retain backend defaults; explicit user choices take precedence.

`status --campaign ID` uses one compact observation request for the selected campaign, with explicit progress timestamps and partial errors. Terminal trials do not refresh generation activity. Text is the default; global `--json` returns the compact machine-readable observation. Full checkpoints are available through `campaign get`. Watch output includes available budgets and isolates unavailable progress/activity endpoints.

`campaign recovery --id ID` and `trial actions --campaign ID --number N` show current recovery eligibility, blocking reasons and effects. The dashboard uses the same backend rules. `trial resume --campaign ID --number N` resumes only that blocked/interrupted trial on its original server and saved checkpoints; `target requalify --campaign ID --id SERVER` runs qualification before resuming that server's blocked work. Requalification is unavailable while the server has an active workload or uncollected remote jobs. Resume existing work to reconcile those jobs first. A stopped campaign must be resumed explicitly before trial/server execution resumes.

`trial fail --campaign ID --number N --reason "Sampling exhausted"` wraps the operator-failure API described above. The dashboard requires the reason beside the action and explains whether pending work can start or the server remains drained. Recovery commands recheck eligibility at execution time and retain audit events; the helper never automatically repeats an uncertain mutation response.

Logs now start with `--tail 500` by default (`--lines` is an alias), capped at 256 KiB. An explicit `--cursor` reads one 64 KiB page. `--follow` starts at the selected tail, preserves UTF-8 boundaries and jumps to a fresh tail after rotation or a backlog larger than one page. The truncation notice includes a full-file download URL. `diagnose --campaign ID --trial N --output diagnostics.zip` limits the bundle to that trial's evidence and includes a failure summary. Defaults: 500 lines per log, 16 file attempts, 8 MiB uncompressed evidence payload, plus a bounded diagnostic manifest. Override with `--lines`, `--max-files`, `--max-bytes` and `--timeout`. The manifest records omissions and unavailable evidence; complete files remain downloadable separately.

Diagnostics request `GET /campaigns/{id}/artifacts?trial=N&hashes=false` to scope the index on the server and avoid reading entire growing files just to compute checksums. Omit `trial` for a campaign-wide metadata index. Normal artifact listing retains checksums by default; `hashes=false` returns null checksums. Failure summaries are limited to 64 entries, with omissions recorded in the diagnostic manifest.

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

Configuration commands accept plain JSON and the JSON-compatible Spring YAML produced by `config --spring`. Validation prints its errors and exits with status 2 when invalid. `create` returns a saved draft without starting it. Lifecycle mutations require an explicit saved `--id`; `resume`, `cancel` and `report` use the same campaign. `probe` waits up to 120 seconds for its saved operation by default; `--timeout` sets this total wait, not a per-target allowance. Each status read is capped at ten seconds. If work is still running when the wait ends, the helper returns its ID and current state; inspect it with `campaign probe-status --id ID`. `baseline-check` defaults to a 3600-second HTTP timeout, adjustable with `--timeout`. The helper never automatically repeats a mutation after a lost response; inspect saved work before retrying.

`watch` selects an active campaign or screening job, or the latest backend-maintained session when idle. Explicit `--campaign ID` or `--screening ID` pins the selection. It shows all active trials, their server assignments, generation counters, separate repair/retry counts, target state and structured failures. Readable activity follows each active trial’s latest generation/repair attempt through preparation, with independent byte cursors and partial-line buffering. Each poll reads at most four activity pages per tracked attempt; `activityCaughtUp` indicates whether that batch reached the end. Watching starts from a recent tail. Use `--history` to start at the beginning. Events are limited to 4 KiB and displayed activity to 256 KiB per invocation, with explicit truncation notices; `--unlimited-output` removes these display limits. Use transcripts or logs for complete history.

Watching changes no campaign state. It stops on completion, a draft, interruption, an approval request or `NEEDS_ATTENTION`; `WAITING_FOR_REMOTE` and `CANCELLATION_PENDING` keep polling. Defaults are a two-second interval and a five-minute watch limit. `--once` reads one bounded batch; `--json` emits newline-delimited status/activity records with a final `watch-ended` reason. A watch timeout or Ctrl-C leaves workloads running.

Helper tests use the standard library: `python3 -B -m unittest discover -s .agents/skills/mork-autollm/scripts`. To include the packaged-backend smoke test, set `MORK_AUTOLLM_TEST_JAR` to an absolute executable JAR, `MORK_AUTOLLM_TEST_CWD` to its application directory, and `MORK_AUTOLLM_TEST_RUNNER` to the matching remote-runner JAR. The fixture provisions a temporary worker registry. The smoke test uses a temporary workspace, checks draft/lifecycle/watch/diagnostic commands and served dashboard assets, and stops only its owned backend; it runs no screening, generation or tuning workloads.

SSH integration tests create an isolated loopback `sshd` with temporary keys and configuration. Set `MORK_TEST_SSHD` to override `/usr/sbin/sshd`; these fixtures do not use a real server or the user's SSH settings. The complete remote campaign fixture also needs the R packages above and the selector dependencies in `example-tsptw/venv`.

## Trial lifecycle

1. **Generate:** `codex exec --json` works in a fresh clone and returns a structured inventory/component/filter manifest. Shared source instructions are appended to every generation and repair turn, including custom prompts and resumed conversations. They preserve existing code with exceptions for the project's exploration filter and additive model support, described below. All newly discovered algorithm components must end in `New`; utility and supporting classes are exempt.
2. **Prepare:** Mork independently runs the build/tests, checks dependency contents, constructs the actual search space, checks the parameter limit and seed encodings, executes seeds and targeted component probes, and runs a short real autoconfig pilot. Fingerprint version 2 ignores ZIP timestamps/order and only the generated root `builddef.lst` entry in `mork`/`mork-common` dependencies. All class files and other resources remain checked; raw JAR and excluded metadata hashes remain evidence. Mismatches list the exact changed archive entries. Generation/startup and individual evaluations have bounded deadlines and a configured heap.
3. **Repair or pause:** positively attributed component failures return diagnostics through `codex exec resume <exact-thread-id>`. Up to `max-repairs` completed repair turns are allowed. Infrastructure, integrity and unclassified failures pause in `NEEDS_ATTENTION` without charging component repairs. Transient infrastructure failures allow two retries, retaining each attempt and its diagnostics. A compiler failure naming a changed source can be attributed; a generic failed build or unchanged framework test is not sufficient evidence for component repair.

Explicit Codex usage-limit responses enter `WAITING_FOR_QUOTA` and resume the same conversation automatically. The checkpoint, progress API, operator watch and dashboard expose `quotaRetryAt`. The controller uses exhausted-window reset timestamps from the archived conversation, or the CLI's displayed local reset time, plus two seconds of grace. If neither is available, it checks again after five minutes. Quota waits release the generation slot, consume no component repairs or infrastructure retries, and remain cancellable. A completed component repair is counted once even when interrupted by quota exhaustion. Controller interruption preserves the deadline and pending repair charge; resuming the campaign restores the wait. Authentication, network and unrelated CLI failures retain their existing recovery behavior.

Explicit model-capacity errors enter `WAITING_FOR_CAPACITY`, keeping the model and conversation unchanged. The checkpoint, progress API, operator and dashboard expose `capacityWait` with the first failure, retry count, diagnostic, next retry and deadline. Delays use equal jitter: 15–30 seconds initially, doubling to a 150–300 second range. The one-hour deadline survives controller restarts. Expiry pauses the trial with `CODEX_CAPACITY_EXHAUSTED`; explicit recovery starts a new budget while preserving prior attempts. Waiting releases the generation slot, remains cancellable and consumes neither component repairs nor infrastructure retries. An error before a conversation exists retries initiation; otherwise retries resume the exact conversation. Evidence-archive failures still block as integrity errors. No fallback models are selected automatically.

Campaign approval requires an explicit `codex.model`; Mork does not choose a default model for the user. Draft creation, screening and baseline preparation may leave it unset. The proposal’s `generation` section and the dashboard/helper approval view show the model for generation and repairs, reasoning effort, provider policy and the absence of automatic model fallback. Blank reasoning effort is clearly labeled as an unpinned Codex default. Provider selection comes from Codex configuration on initial launch; resumed sessions retain their observed provider. Set `codex.reasoningEffort` too when a pinned effort is required. Model and reasoning settings are part of the reviewed digest, so changing either requires renewed approval. Approval and launch reject a missing model even through direct API calls.

Each generation attempt retains `model-selection.json` with the requested model, launch selection and observed model/provider from the exact archived session. Missing observations remain explicitly unavailable or partial. When the model was implicit, resuming a known conversation supplies its observed model and provider instead of reselecting from changing defaults. These details are also available in trial progress and the dashboard.
4. **Tune:** a verified component set, search space, and JAR are frozen. Main tuning starts in a fresh directory with the original seeds and the fixed evaluation budget. Pilot elites and racing state are not reused.
5. **Compare:** a separate JVM runs exactly `SOTA` and `IraceProposal`, with paired repetition seeds and the same outer restart policy and per-instance time budgets. It uses the existing validator, metrics, Excel serializer, and customization hook.

Java assertions are enabled for seed/component probes and the short preparation pilot. Main tuning and the final SOTA/proposal comparison explicitly disable assertions in their fresh JVMs, avoiding per-move debug validation during measured optimization. Metadata checks and winner export also disable assertions. Maven tests keep assertions enabled. Final solution validation still runs during tuning and comparison.

Preparation has independent probe and pilot budgets: `preparation.probeDurationMillis` and `preparation.pilotDurationMillis`, each defaulting to 600000 ms (ten minutes). The probe child has a hard timeout equal to its probe budget plus the startup allowance, for both baseline and generated-component verification. A successful probe always leaves the pilot its full configured admission window, starting when the pilot child reports readiness; probe execution, transfers and result collection are not subtracted. Startup, phase and watchdog allowances still bound process startup and completion of admitted work. Changing these defaults does not update an already-running controller or its saved campaign configuration.

Seed/component probes use the shared `instances.path.reference` override when configured; it must identify an instance in the approved training inputs. Otherwise they use the fastest-loading training instance, importing candidates with unknown load times and retaining their measured loading times. This is the same selection used for JVM warm-up; see [reference-instance selection](instance-manager.md#shared-reference-instance-for-warm-up-and-probes). Probe evidence records the chosen load path in `probe-instance.json`. This selection does not change training/testing membership, probe evaluation horizons, or the pilot's instance pool.

All supplied seeds and reachable component representatives are submitted as one preflight batch. Evaluations run concurrently in the execution host's existing worker pool, bounded by its resolved worker count; a single-worker target executes the same batch sequentially. Each evaluation keeps its own algorithm, random state, metrics and watchdog deadline, and writes its journal result as it finishes. Configuration construction, aggregate `probes.json` writes, and acceptance checks stay sequential. The aggregate outcomes are written in request order after the batch finishes; individual progress and failures remain available in the evaluation journal during execution. Probe evaluations do not consume the main tuning counter, and the pilot starts only after the existing seed/component acceptance checks pass.

Source instructions allow changing the existing project exploration filter to prune composition branches while keeping every supplied seed encodable and executable. Adding a second active exploration filter can make dependency injection ambiguous; add one only when the project has none. Inventory blacklists exclude entire component classes and cannot replace branch-specific exploration rules.

Generation and repair instructions require measuring the actual emitted irace parameter declaration count before submitting a proposal. Metadata-only search-space checks are permitted; agents must not run their own pilot, tuning, or comparison. If the space exceeds the configured cap, prune and recheck while preserving every supplied seed. Overlapping composition wrappers are a useful first target: for example, exclude `SequentialImprover` when `VND` is enabled and no seed requires it, or prune nested wrappers. This is a search-budget choice; the wrappers have different execution semantics. The controller independently enforces the cap before probes and tuning, and returns failures as repair feedback.

The default generation prompt asks the model to choose a portfolio of behaviorally diverse components based on the problem, SOTA, and evaluation criteria, including new metaheuristics when justified. It requires an expansion review after each verified implementation batch, with no fixed component count or per-role quotas. This expansion loop applies to generation; repair turns address reported component failures and revalidate the resulting portfolio. The model may exclude existing components that no supplied seed requires, or replace and simplify its own additions, to release capacity for more useful behaviors. It must remeasure the emitted declarations, preserve all seeds, and report unused capacity, portfolio tradeoffs, and its reason for stopping. Declaration utilization alone does not measure behavioral coverage, and these design requirements are prompt instructions rather than additional controller acceptance checks.

Instance, Solution, and Move classes may gain helper methods and state, including necessary initialization, copy, and update wiring. Preserve existing APIs, scoring, feasibility, and behavior, and add tests for the extensions. Copy existing algorithm components before changing them. Validators, objective definitions, importers, existing tests, build files, framework code, and dependency contents remain subject to preservation instructions. Add new utilities, factories, and parameter providers where needed; select inventory filters and configure tree depth or derivation repetition through existing settings.

Source preservation is instruction-based: preparation does not automatically reject edits to existing trial sources, and tests and probes do not prove semantic equivalence. Baseline and final source hashes remain evidence; frozen inputs, archive consistency, dependency contents, and frozen artifacts retain their integrity checks. The original checkout and baseline clone remain separate from trial edits.

Expected rejected combinations, infeasible results, and missing AUC evidence remain normal tuning penalties. A component exception found by a targeted probe produces repair feedback. A normal time-limit expiry with a valid incumbent remains successful; an excessive overrun is rejected. A hard hang in preparation stops that attempt and may consume a repair turn. A hard hang in main tuning or comparison fails that trial and the campaign continues to the next one. 
The dashboard separates Overview, Trials, Results, Infrastructure and Artifacts. Campaign setup uses Project, Dataset, Budget, Execution and Review steps; the common duration fields use seconds, while API/JSON durations remain milliseconds. Trial inspection separates Summary, Results, Algorithm, Activity and Diagnostics. Seeds, model/session details and raw records remain available in Diagnostics instead of the trial table. Results requires an explicit objective and direction, and shows host-matched SOTA statistics, coverage and exclusions without an implicit cross-host ranking.

The dashboard shows preparation substages, elapsed time, generation readiness and last activity, readable messages/commands/edits, separate repair and infrastructure retry counts, tuning outcomes, winner JSON and saved comparisons. `LAUNCHING` means no session event has arrived; `SESSION_READY` records a thread; `ACTIVE` records observed turn/tool activity. Startup timeout uses `execution.startupTimeoutMillis`, independently of the full generation deadline. Turns, commands, edit events and assistant messages count observable CLI events, not hidden model iterations. Live reported usage remains separate from authoritative archived usage. Large outputs are previewed with raw downloads retained.

AutoLLM child processes disable the terminal progress bar with `solver.progress-bar=false` to avoid repeated redraws in captured logs. Structured progress records, dashboard updates and metrics remain enabled.

The baseline response separates `withinLimit` (all target parameter counts are known and fit the cap) from `ready` (the entire pool also passed qualification). `qualifiedTargets`, `failedTargets`, and per-target results explain partial readiness. Starting without a target selection rejects saved qualification failures. To start a healthy subset, send `{"targets":["worker-a","worker-c"]}` to `/start`, use repeated `campaign start --target ID` arguments, or select servers in the dashboard. Omitted servers are drained; trials remain pinned when resuming.

`instances.path.reference` remains the shared warm-up/probe override. A parent setting is frozen when the campaign is created; a per-campaign `project.runArguments` entry `--instances.path.reference=...` takes precedence, followed by the child application's setting. Relative references resolve against the configured project repository. Original identities are mapped through screening copies and the same SSH input manifest as training data, including archive members. The selected reference must still belong to the experiment inputs. Without an override, fastest-loading selection remains automatic.

Campaign, target, trial, attempt, screening, preflight, and remote-job lifecycle states use typed enums. New checkpoints add LOCAL_PREPARATION and WAITING_FOR_TARGET states. Campaign resume requests use the same eligibility rules exposed by the recovery API: pending cancellation blocks resume until reconciliation finishes, and a stopped campaign waiting for a remote job remains resumable. Trial requeueing preserves its assignment and seeds; completed trials stay terminal.

Qualification captures execution-host prerequisites once. Baseline preparation reuses their structured results for version provenance, including R runtime/package versions, while checking the Codex version on the controller. Build-wrapper validation still happens after staging the actual source, and independent baseline reproducibility builds remain required.

Runner state contains one execution context and one trial. Stage outputs are committed under `outputs`, keyed by `GENERATION`, `PREPARATION`, `TUNING`, `WINNER_EXPORT`, and `COMPARISON`; `nextStage` determines the remaining work. All evidence paths use the same campaign-relative namespace, including the target prefix.

Execution results are saved before evidence collection. Resume retries local compaction, SSH result collection, or failed conversation archiving without repeating a completed workload or successful generation. Earlier failed transcript collection attempts remain in `collection-failures/`; successfully archived evidence remains sealed. Immutable input payloads and Git bundles use separate content-addressed transfer layers, prepared once per content identity and installed once per worker workspace.

Polling uses `status.json`, which omits prompts, stack traces, winner payloads, and per-attempt conversation evidence; the dashboard fetches full detail on inspection through the campaign endpoint and preserves it separately from polling snapshots. Artifact downloads retain the complete evidence. Connection retry totals are updated as remote progress arrives. Comparison summaries incrementally index complete journal rows and report available instances, per-instance algorithm completion counts, and `omittedEvents`. Live statistics use online accumulators (exact quantiles remain in reports); the index retains file offsets rather than curve points. `/trials/{number}/curves?instanceId=...` returns all available successful measured repetitions for that instance and both algorithms in one response, sampled to at most 256 points per curve. The dashboard loads the selected instance automatically and refreshes when its completion counts change. Reads cover a snapshot of the journal length, and incomplete trailing rows wait for the next refresh. Malformed rows and rows exceeding 4 MiB are reported as omitted; download the original journal for complete data. Dashboard reads time out after ten seconds and independent sections retain healthy updates.

Cancel stops owned processes and confirms remote evidence collection. Resume preserves completed work, rejoins unresolved SSH jobs, and uses fresh attempts for interrupted work that needs restarting. Completed failed trials stay failed. Campaigns, screening and baseline checks share workspace ownership. Full target qualification, including requalification, refreshes every prerequisite result. Routine trial resume checks the saved host identity and allocation without replacing prerequisite evidence. The SSH preflight deadline covers the sum of all sequential check deadlines, their termination grace, and dispatch/identity overhead.

## Saved evidence

Each UUID directory contains `campaign.json`, append-only campaign events, frozen control files and instance manifests, a revisioned target pool and immutable trial assignments. `targets/<id>/` contains isolated controls, transfers, metadata and baseline evidence; trials use their recorded campaign-relative `directory`. Trial evidence retains a frozen commit-plus-patch source snapshot (source ZIP for legacy campaigns) and hashes, Codex prompts/events/usage/manifest/session ID, build logs, seed/component probes, pilot and main evaluation journals, all encountered configurations, elite history, final elites, irace R data, winner specifications, and comparison output. Completed checkouts may be retired and closed text evidence compressed as described above; their logical evidence remains recoverable.

After a workload finishes, its default `solutions/` exports are retained in one `solutions.zip`. Every member is checksum-verified before loose exports are removed. Control files and workbooks remain loose; journals remain readable through the logical evidence APIs even after compression, so reporting and recovery do not require extracting tours. New SSH runners compact exports before transfer; local collection also compacts loose exports returned by older runners. Archives contain the original files and a `.mork-transfer.json` manifest with content hashes, permissions and symlink metadata. A retry verifies an existing archive and any remaining loose files before completing cleanup; conflicting or corrupt archives leave the loose files intact.

For explicitly archived historical checkouts and other staging trees, `DirectoryArchiveUtil.archive(directory, retainedNames)` can still pack a sibling `source.zip`, including Git metadata and build output. It verifies the full archive and remaining originals before removing loose files; executable JARs can stay at their original paths. `DirectoryArchiveUtil.restore(directory)` verifies and restores files, permissions and symbolic links, refusing to overwrite changed files. Campaign checkout reuse restores these archives automatically when `.git` is absent. This utility is separate from new-format terminal retirement and does not authorize automatic historical migration. Do not pack a checkout used by a running controller or an unfinished workload.

Completed screening evidence can use `runs.zip` beside `run-hashes.json`. Screening recovery and proposal verification read loose or archived records and still enforce the exact original reviewed payload hashes. Reports, approvals, indexes and provenance files remain loose and unchanged. These readers require a controller built with archive support; older controllers need the evidence restored before reuse. Use the archive utility to restore trees containing symbolic links, because link targets and permissions are stored in the manifest.

Each generation or repair attempt also retains `conversations/<thread-id>.jsonl`: an immutable, byte-for-byte snapshot of the native Codex transcript, including previous turns and locally persisted child conversations linked to that thread. These contain the messages, tool activity, and reasoning exposed by Codex's local storage. AutoLLM reads `CODEX_HOME` (default `~/.codex`) and supports native JSONL sessions under `sessions` and `archived_sessions`. It verifies the exact thread ID and trial directory; unrelated conversations and authentication files are excluded. A required archive failure pauses as an integrity problem without requesting a repair. Archival runs after success, failure, timeout, cancellation, and recovery of an interrupted attempt.

Each attempt saves `evidence-before.json`, `evidence.json`, and `usage.json`. Usage preserves reported input, cached input, cache-write input, output, reasoning output, and total tokens, plus resolved models, CLI version, and the counter source. Per-response native records are preferred and response IDs are deduplicated; older cumulative native counters use validated deltas from the pre-attempt checkpoint. CLI-reported usage is supplemental because resumed turns may report cumulative totals. Missing fields remain `null`; usage is marked `COMPLETE`, `PARTIAL`, or `UNAVAILABLE` and does not block tuning. Cached input is part of input and reasoning output is part of output, so these fields must not be added again to total tokens.

`trials/<number>/llm-usage.json` and `reports/llm-usage.json` summarize all generation and repair attempts, including failed and interrupted attempts. Partial totals include only known counts and may underestimate usage. The dashboard and campaign API expose these summaries and transcript downloads. Report regeneration uses saved artifacts. Historical campaigns require their original controller; missing historical usage is not reconstructed. No cost or pricing statistics are collected.

Comparison records preserve objective values, runtime, time-to-best, curves, seeds, instance paths, repetitions, and failure/interruption outcomes. Each comparison retains its normal workbook; `reports/targets/<id>/campaign.xlsx` groups successful cached result rows by execution server without loading generated classes into the parent. `reports/comparison-rows.json` also retains partial and failed trial data. Workbooks never pool servers into a single ranking. Rows retain target ID, pool revision and execution-host provenance; failed and incomplete rows stay in the portable JSON evidence. Workbook gaps use the existing best-reference calculation. Artifact listings include SHA-256 checksums.

This experiment measures the combined Codex-and-tuning procedure. Component selection reports which components the winner uses; it does not establish each component's individual contribution. Every independent trial's data remains available for later analysis.

## API

All paths below are relative to `/api/autollm`; endpoints are registered when the parent is launched with `--autollm`.

| Endpoint | Purpose |
| --- | --- |
| `GET /capabilities`, `/defaults` | Controller identity, supported protocols and default settings |
| `POST /validate`, `/effective-config`, `/targets/probe` | Static validation, budget summary and creation of saved prerequisite diagnostics |
| `GET /targets/probes/{id}` | Saved preflight status, per-target results and failures |
| `GET/POST /campaigns`, `GET /campaigns/{id}` | List, save a draft, inspect checkpoints |
| `GET /campaigns/{id}/observation` | Compact campaign/trial observations, timestamps and recovery availability |
| `GET/POST /campaigns/{id}/proposal`, `POST /campaigns/{id}/approve` | Review selected servers, effective configuration, dataset and approve its digest |
| `PUT /campaigns/{id}/draft`, `/dataset` | Revise settings or exact dataset membership before launch |
| `GET /objectives` | Ordered objective names and minimize/maximize directions; the dashboard selects the first and loads comparisons automatically |
| `GET /campaigns/{id}/comparison?objective=Cost&ranking=NONE` | Host-matched aggregate tables without curve arrays; explicit optional ranking |
| `GET /campaigns/{id}/trials/{number}/remote-inspection` | Observe remote process/job evidence without reconciling it |
| `POST /campaigns/{id}/recover` | Recover explicitly selected trials; per-trial outcomes |
| `POST /campaigns/{id}/baseline-check` | Baseline build/reproducibility/metadata checks without tuning |
| `POST /campaigns/{id}/start`, `/resume`, `/cancel`, `/report` | Lifecycle actions; reporting uses cached results |
| `GET /campaigns/{id}/targets`, `PUT/DELETE /campaigns/{id}/targets/{targetId}` | Inspect pool, register an immutable server, remove an idle reconciled server |
| `POST /campaigns/{id}/targets/{targetId}/drain`, `/resume` | Stop new assignments or recover a server while the campaign is active |
| `POST /campaigns/{id}/trials/{number}/fail` | End a reconciled blocked trial with an operator reason and release its qualified target |
| `GET /campaigns/{id}/summary` | Effective budgets and estimates, including frozen comparison limits |
| `GET /campaigns/{id}/trials/{number}/progress`, `/comparison` | Active attempt, failure, generation and nested workload progress; saved comparison data |
| `GET /campaigns/{id}/trials/{number}/activity?attempt=N&cursor=0` | Readable generation events in bounded byte pages |
| `GET /campaigns/{id}/artifacts`, `/artifact?path=...` | Evidence index and downloads |
| `GET /campaigns/{id}/log?path=...&cursor=0&maximumBytes=65536` | UTF-8-safe byte cursor; returns `cursor`, `nextCursor`, `text`, `more`, `reset` |
| `GET /campaigns/{id}/preview?path=...&lines=500` | Latest text lines; accepts 1–5,000 lines, capped at 256 KiB. Returns `text`, `lineCount`, `sizeBytes`, `truncated`, `byteLimited`, and a UTF-8-safe `nextCursor` for following. Full downloads remain on `/artifact`. |
| `GET /campaigns/{id}/recovery` | Current campaign/trial/server recovery availability, blocking reasons and effects |
| `POST /campaigns/{id}/trials/{number}/resume` | Resume only the selected blocked/interrupted trial on its original server |
| `POST /campaigns/{id}/targets/{targetId}/requalify` | Requalify an idle, reconciled server before resuming its blocked work |
| `GET /screenings/{id}/preview?path=...&lines=500` | Same bounded tail and UTF-8-safe follow cursor for screening files |
| `GET/POST /screenings`, `GET /screenings/{id}` | Durable dataset preparation jobs; create returns a draft |
| `POST /screenings/{id}/resume`, `/cancel` | Prepare screening evidence or reconcile cancellation; campaign review owns launch approval |
| `GET /screenings/{id}/review`, `/artifact?path=...`, `/log?path=...&cursor=0` | Completed proposal/report/hash, evidence and bounded logs |

### Canonical review, diagnostics and output

`POST /api/autollm/effective-config` returns `configuration`, validation errors, summary and semantic digest. Proposal snapshots are immutable and addressed by digest. Approval identity includes budgets, ordered dataset identities, seeds, source identity and selected worker allocations; free space, heartbeats and lease observations are excluded. A changed proposal returns HTTP 409 with `PROPOSAL_CHANGED` and field-level `path`, `reviewed`, `current` differences. An unknown digest returns `UNKNOWN_PROPOSAL_DIGEST` without an invented comparison.

Failed builds record bounded execution-host compiler, test, dependency or unknown diagnostics before collection/cleanup, with source/report locations. Recognizing a failing baseline test does not establish generated-component causation; attribution remains unknown without stronger evidence.

Response-producing helper commands accept `--output FILE` (`--response-output` when `--output` already names an artifact destination). Large JSON retrievals stream into a temporary file and atomically replace the destination only after success; stdout contains a concise file receipt. Watch output uses newline-delimited JSON with `--json`, otherwise readable lines; log output preserves its documented text/JSON rendering. Interrupted writes preserve the previous destination. `status --screening ID` selects screening explicitly; campaign and screening selectors are mutually exclusive.
