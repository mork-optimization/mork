---
name: mork-autollm
description: Prepare, run, inspect, diagnose, and resume Mork AutoLLM component-generation and autoconfig campaigns, locally or over SSH.
---

# Mork AutoLLM

Operate through Mork's API using [the helper](scripts/mork_autollm.py). Mork owns generation, repairs, screening, execution and checkpoints. Reuse saved IDs and established user choices. “Improve SOTA” authorizes running within that scope; a configuration-only request ends at a validated draft. Dataset approval and permission to run are separate; preserve existing authorization across follow-ups.

Use command `--help` for arguments, the [AutoLLM guide](../../../docs/features/autollm.md) for configuration and execution contracts, and the [screening guide](../../../docs/features/instance-screening.md) for dataset preparation. If installed separately, resolve these guides in the Mork checkout. Load only the relevant sections.

## Prepare and start

- Reuse the established local/SSH workload location. Ask only for missing hosts, dedicated workspaces, environments or resource limits. Keep Codex, authentication, transcripts and orchestration local. Preserve explicit worker overrides and leave Codex concurrency at its default unless the user requests otherwise.
- Discover the committed baseline, application working directory, build argument array, executable artifact, fixed SOTA and full original input pool. Preserve the user's checkout; campaign trials use committed source. For monorepos, resolve repository/module paths and build the required reactor.
- Use the helper to find or start a compatible backend for the intended artifact and workspace. Preserve unrelated backends. Run target preflight and inspect per-target results; a saved probe may remain `RUNNING`, in which case use `campaign probe-status --id ID`.
- Prepare or reuse an approved dataset with matching provenance. Save a complete configuration, validate it, create a draft, and run its baseline check. Use the backend's parameter counts, readiness and failure details. If pruning is needed, preserve SOTA and every supplied seed configuration. Starting a healthy subset requires explicit server selection through repeated `campaign start --target ID`; omitted targets are drained.
- Present the effective trials, resources, repair limits, tuning/comparison budgets and unresolved time limits. Start the saved draft when authorized. Do not replace a missing approved training index with the raw pool.

## Review the dataset

Screen the full original pool with fixed SOTA using Mork's sequential warmed-worker protocol. Preserve inconclusive inputs. Testing must contain every retained instance, including training; the overlap is intentional.

Before approval, show the concrete report, provenance, criteria, exclusions, retained/inconclusive inputs, and training/testing membership. Obtain explicit agreement on the exact reviewed proposal SHA-256, then approve that proposal through Mork. Changed source, executable, SOTA or inputs invalidate approval. Reuse frozen indexes across qualified servers only under the proposal's reuse policy; screening timing remains evidence from its original host.

## Inspect and recover

Use `status --campaign ID --table`, `watch`, bounded `logs`, and `diagnose` to inspect saved work. Report observed CLI activity and available token usage; keep unavailable progress unknown. Performance claims need saved comparisons, accounting for failed/incomplete trials and keeping results grouped by host.

Use backend recovery actions (`campaign recovery` or `trial actions`) to determine eligibility and effects, then apply the authorized scoped trial/target action. Preserve trial assignments, seeds, checkpoints and remote job identities. Resume a stopped campaign before recovering active work. Mark a blocked trial failed only when the user directs it; slowness alone is insufficient.

Infrastructure failures must not consume component repairs or prompt a trial agent to change framework tests, objectives, validators or dependencies. Respect the backend's failure classification and correct the underlying cause before resuming. Missing conversation/result collection can be retried through normal recovery without repeating completed work.

Never automatically replay a mutation after an uncertain response. Rejoin saved remote jobs: `WAITING_FOR_REMOTE` does not establish failure, and `CANCELLATION_PENDING` does not confirm termination. Confirm cancellation and evidence collection before conflicting workloads. Regenerate reports without retuning when only reporting needs repair.

Return the actual status, campaign/screening/probe ID, configuration path, dashboard URL and evidence location.
