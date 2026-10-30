---
name: mork-autollm
description: Prepare, run, inspect, diagnose, and resume Mork AutoLLM campaigns locally or over SSH.
---

# Mork AutoLLM

Use [the helper](scripts/mork_autollm.py); Mork owns generation, repairs, execution and checkpoints. Read this entrypoint once, then use command help or relevant sections of the [AutoLLM guide](../../../docs/features/autollm.md) and [screening guide](../../../docs/features/instance-screening.md). Resolve guides in the checkout when installed separately.

## Prepare and approve

Reuse established hosts, workspaces, resource limits and authorization. Preserve the user's checkout; trials use committed source. Keep Codex, credentials and transcripts local. Discover the repository/module, build argument array, executable, fixed SOTA, seeds and full original input pool.

Use `backend status` to distinguish the saved address from live health; `backend start` discovers or launches a compatible controller. Restart through saved metadata with `backend restart`. Preserve unrelated controllers and use the matching operator for historical checkpoint versions.

Screen the original pool using Mork's warmed-worker protocol. Keep inconclusive inputs; testing includes training. Screening evidence can feed a campaign draft without separate selection approval. Use proposed training/testing indexes and preserve provenance; never substitute the raw pool for a missing index.

Validate the configuration, create the draft, and run its baseline check. Preserve SOTA and all supplied seeds when pruning. Review `campaign proposal`: exact testing/training membership, trivial exclusions with reasons, selected servers/readiness and complete resolved settings. Present this concrete proposal to the user and obtain explicit campaign approval before launch, even for a broad “improve SOTA” request. Edit preferences before approval; select a healthy subset explicitly. Approve the reviewed digest and launch that revision. Changes require a revised review; unchanged resume/reconnect retains authorization.

Leave concurrency at its configured default unless requested otherwise. Generation and local build/metadata preparation can precede worker assignment; probes, pilot and measurements run on the selected worker. Detailed component timing is opt-in.

## Observe and recover

Start with compact `status --campaign ID`. Use explicit `--json` or `campaign get` for needed detail, bounded `logs`/`diagnose` for evidence, and `watch` for generation activity. Keep unavailable progress unknown. Distinguish tuning finished, evidence collected and comparison finished; process existence alone is not progress.

Use `remote --campaign ID --trial N` for stale, blocked or contradictory remote observations. Use backend recovery eligibility and scoped `recover --campaign ID --trial N` (repeat for selected trials); resume a stopped campaign first. Rejoin saved jobs and retain seeds/assignments. Infrastructure failures do not consume component repairs. Never mark a blocked trial failed merely for slowness.

Do not replay a mutation after an uncertain response: inspect saved state first. WAITING_FOR_REMOTE does not establish failure; CANCELLATION_PENDING does not confirm termination. Reconcile and collect evidence before conflicting work. Repair collection/reporting without repeating completed experiments.

Use `compare` for saved journal results; keep host-matched SOTA comparisons and exclusion/completeness details. Choose an explicit ranking method before claiming a single best proposal. Do not parse spreadsheets or load curves for routine comparison tables.

Return observed status, IDs, configuration/proposal location, dashboard URL and relevant evidence. Token usage and progress must come from available observations.
