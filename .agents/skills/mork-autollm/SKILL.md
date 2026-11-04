---
name: mork-autollm
description: Prepare, run, inspect, diagnose, and resume Mork AutoLLM campaigns locally or over SSH.
---

# Mork AutoLLM

Use [the helper](scripts/mork_autollm.py); Mork owns generation, repairs, execution and checkpoints. Read this entrypoint once, then use command help or relevant sections of the [AutoLLM guide](../../../docs/features/autollm.md) and [screening guide](../../../docs/features/instance-screening.md). Resolve guides in the checkout when installed separately.

## Prepare and approve

Reuse established hosts, workspaces, resource limits and authorization. Preserve the user's checkout; trials use committed source. Keep Codex, credentials and transcripts local. Discover the repository/module, build argument array, executable, fixed SOTA, seeds and full original input pool.

Use `backend status` to distinguish the saved address from live health; `backend start` discovers or launches a compatible controller. Restart through saved metadata with `backend restart`. Preserve unrelated controllers and use the matching operator for historical checkpoint versions.

Check the explicitly configured training and comparison datasets first (`autollm.training-path` / `comparison-path`, or named `instances.path.irace` / `autollm-comparison`). When both are valid, preserve their exact membership and skip screening and splitting, for LOCAL and SSH. Sets may be disjoint or overlapping. Do not infer a selection from the default raw pool or silently replace missing indexes.

Use `dataset list --config FILE` to discover approved project selections across workspaces. `dataset get --config FILE --id ID` verifies a saved entry and returns reusable paths. To save a supplied or screened selection, run `dataset preview --config FILE --name NAME`, show the exact membership, provenance and digest, obtain explicit user approval, then `dataset publish --config FILE --name NAME --digest DIGEST`. Library payloads survive campaign cleanup; saving a selection does not approve a campaign. Baseline/SOTA/host changes permit dataset reuse, while each campaign still builds and validates its own baseline. Historical screening timings retain their original context.

Only when a new selection is needed, explicitly screen the original pool using Mork's warmed-worker protocol. Keep inconclusive inputs; newly screened testing includes training by default. Screening evidence can feed a campaign draft without separate library publication. Use proposed training/testing indexes and preserve provenance.

Require a successful pristine build of the selected committed baseline before screening. Put baseline corrections on a reviewable project branch and select its commit; never rely on cached build output or fixes made only in a disposable campaign checkout.

Write the server-returned canonical effective configuration; do not substitute a locally merged draft. Validate the configuration, create the draft, and run its baseline check. Preserve SOTA and all supplied seeds when pruning. Review `campaign proposal`: exact testing/training membership, trivial exclusions with reasons, selected servers/readiness and complete resolved settings. Present this concrete proposal to the user and obtain explicit campaign approval before launch, even for a broad “improve SOTA” request. Edit preferences before approval; select a healthy subset explicitly. Approve the reviewed digest and launch that revision. Changes require a revised review; unchanged resume/reconnect retains authorization.

Include the proposal’s generation approval line prominently: the explicit model for generation and repairs, reasoning effort (or its unpinned Codex default), inherited provider policy and no automatic model fallback. Never choose a model for the user silently. A missing `codex.model` blocks approval and launch; ask the user to choose it, update the draft and review the new digest. Model or reasoning changes require renewed approval.

Include one line in the campaign approval showing estimated additional peak storage and available space on the controller and each worker, with a warning if the campaign may not fit.

Leave concurrency at its configured default unless requested otherwise. Generation and local build/metadata preparation can precede worker assignment; probes, pilot and measurements run on the selected worker. Detailed component timing is opt-in.

## Observe and recover

Start with compact `status --campaign ID`. Use explicit `--json` or `campaign get` for needed detail, bounded `logs`/`diagnose` for evidence, and `watch` for generation activity. Watch starts at a recent tail and bounds activity to 4 KiB per event/256 KiB per invocation; use `--history` or `--unlimited-output` explicitly. Use `--output FILE` for large responses. Keep unavailable progress unknown; remote contact timestamps are not evaluation progress. Distinguish tuning finished, evidence collected and comparison finished; process existence alone is not progress.

Use `remote --campaign ID --trial N` for stale, blocked or contradictory remote observations; add `--assignment ID` or `--all-assignments` for recorded historical owners. Use backend recovery eligibility and scoped `recover --campaign ID --trial N` (repeat for selected trials); resume a stopped campaign first. Rejoin saved jobs and retain seeds/assignments. Infrastructure failures do not consume component repairs. Never mark a blocked trial failed merely for slowness.

Do not replay a mutation after an uncertain response: inspect saved state first. WAITING_FOR_REMOTE does not establish failure; CANCELLATION_PENDING does not confirm termination. Reconcile and collect evidence before conflicting work. Repair collection/reporting without repeating completed experiments.

Run under the existing user/SSH account. Workspaces create their own `.mork` ownership metadata automatically; no system registry, provisioning or administrator access is required. Remote preflight and execution hold the configured workspace's `execution.lock`; collection does not. Separate workspaces can run concurrently even on the same machine. SSH aliases pointing at the same workspace share its lock and persistent workspace ID. WAITING_FOR_WORKSPACE is retryable contention, not host failure or a component repair. A crashed supervisor's uncertain child processes block reuse until reconciled. Use version-8 controllers/runners for new work; retain matching controllers for historical campaigns.

For complete deletion, use `cleanup plan --kind campaign|screening|probe|workspace`, review its scope/digest, then `cleanup apply --digest DIGEST` within the user's authorized scope. The plan exposes retained shared assets and unresolved ownership. Cleanup cancels and reconciles selected work without requiring disposable archives to be downloaded. Export diagnostics only when requested (`--export`); native transcripts require exact exclusive ownership. Offline hosts remain pending; resume apply and inspect status. `--shutdown` is for a complete workspace only. Keep `storage --maintain` as conservative compaction. When reusing workers, confirm termination/collection and preserve per-host resource/toolchain settings.

Use `compare` for saved journal results; keep host-matched SOTA comparisons and exclusion/completeness details. Choose an explicit ranking method before claiming a single best proposal. Do not parse spreadsheets or load curves for routine comparison tables.

Return observed status, IDs, configuration/proposal location, dashboard URL and relevant evidence. Token usage and progress must come from available observations.
