---
name: mork-autollm
description: Prepare, run, inspect, diagnose, and resume Mork AutoLLM component-generation and autoconfig campaigns, locally or over SSH.
---

# Mork AutoLLM

Operate through Mork's API. Mork owns clones, generation, repairs, screening, tuning, comparison, retries, and checkpoints. Requests such as “improve SOTA” authorize campaign work within the established scope; configuration-only requests stop at a validated draft. Preserve prior choices and authorization across follow-ups.

Use [the helper](scripts/mork_autollm.py) for backend discovery/startup, configuration, `campaign`/`screening` lifecycle actions, `watch`, logs, and diagnostics. Reuse saved IDs; `watch` discovers active work or the latest backend session. Read command `--help` for arguments and the relevant parts of the [AutoLLM guide](../../../docs/features/autollm.md) for setup or recovery, or the [screening guide](../../../docs/features/instance-screening.md) for dataset preparation. If installed separately, resolve these guides in the Mork checkout.

## New campaign

1. Resolve workload location before any workload: **locally or on a remote server over SSH?** Reuse an explicit answer; ask only for missing host, dedicated workspace, environment, and resource constraints. Keep Codex, authentication, transcripts, and orchestration local. Remote supervisors survive disconnection; local generation and stage advancement wait while the controller is unavailable.
2. Discover the committed baseline, application working directory, build argument array, relative executable artifact, fixed SOTA specification, and full original instance pool. Trials exclude uncommitted edits. Preserve the user's checkout. In a monorepo use the Git root and appropriate module/reactor build. Resolve campaign input paths against the repository.
3. Use capabilities, artifact identity, and workspace to match a backend, rather than HTTP availability or mode alone. Use a fresh workspace for checkpoint version 2; historical campaigns remain with their original controller. Preserve unrelated backends. Run target preflight and inspect every prerequisite result; static validation does not establish connectivity. Prepare missing dependencies before measurements using an isolated selector environment.
4. Prepare or reuse a provenance-matching approved dataset. Inspect saved screening IDs before resuming. Follow the review requirements below; never substitute the raw pool for a missing training index.
5. Merge user choices with backend defaults and save a complete configuration using the helper. Preserve positive worker overrides; automatic workers resolve on the execution host. Screening uses one sequential worker. Tuning duration is `ignoreInitialMillis + intervalDurationMillis`; honor requested comparison limits too.
6. Inspect `/validate`'s `valid` and `errors`. Create and save a draft ID, run its baseline check, and read the actual emitted parameter count. The cap must be below 300; prune while preserving all supplied seeds if needed. Baseline verification includes a rebuild in another checkout path.
7. Show effective trials, repair/pilot limits, tuning and comparison budgets, workers, and workload estimates, including unresolved application time limits. Start the same draft when running is authorized. Dataset approval and start authorization are separate; reuse existing run authorization instead of asking again.

## Dataset review

Use [screening jobs and review artifacts](../../../docs/features/instance-screening.md) on the full original pool with fixed SOTA. Preserve the sequential warmed-worker protocol, inconclusive inputs, and property-based selector. Testing contains **every retained instance, including training**.

Show the completed report, provenance, criteria, exclusions, retained/inconclusive inputs, training/testing membership, and overlap. Obtain explicit agreement before approving the exact reviewed proposal SHA-256. Ask after the concrete report exists. Use published indexes with valid approval and payload hashes. Changed source/executable, SOTA, inputs, or host can invalidate approval.

## Status, diagnosis, and recovery

Inspect persisted IDs first. The helper's status and diagnostic commands expose attempts, progress, failure categories, remote identity, and evidence without creating experiments. Backend-maintained session files replace handwritten “latest status” notes.

Distinguish process launch, session readiness, observed generation activity, preparation, and tuning. Report observable turns, commands, edits, messages, and available tokens; these are not hidden model iteration totals. Use the readable timeline or bounded cursor logs; native transcripts are archived after attempts finish.

Infrastructure retries are bounded and spend zero component repairs. Inspect `NEEDS_ATTENTION` and correct its cause before resuming; unknown failures also pause. Preserve framework tests, objectives, validators, dependencies, and frozen evidence. Do not make a trial agent repair the framework. SSH conversations use the controller metadata helper and must not launch independent pilots, tuning, or comparison.

Rejoin saved SSH job identities after uncertain responses. `WAITING_FOR_REMOTE` does not prove failure; `CANCELLATION_PENDING` does not prove termination. Confirm cancellation and evidence collection before conflicting workloads. Resume through Mork; completed failed trials stay failed. Regenerate reports without retuning.

Return configuration path, actual status, campaign/screening ID, dashboard URL, and evidence location. Performance claims require saved comparisons, accounting for failed and incomplete trials.
