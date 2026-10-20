---
name: mork-autollm
description: Configure, launch, inspect, and resume Mork AutoLLM campaigns when a user wants repeated component generation and autoconfig experiments for a Mork project.
---

# Mork AutoLLM

Turn the user's experimental choices into a reusable AutoLLM configuration and operate the campaign through Mork's local API. Mork owns independent clones, Codex sessions, repairs, tuning, comparison, and checkpoints.

Read the [AutoLLM guide](../../../docs/features/autollm.md) before configuring a campaign. If installed outside this repository, find `docs/features/autollm.md` in the Mork checkout. Use the guide and the running backend's defaults as the configuration reference.

## Choose where workloads run

Before workload preflight, builds or screening for a new campaign, ask: **Where would you like to run the actual workloads: locally on this computer, or configure a remote server over SSH?** Continue local source inspection while waiting, but wait for the answer before launching workloads. Reuse a choice already explicit in the conversation or saved campaign; do not ask again on status, repair or resume.

Keep analysis, Codex component generation/repairs, authentication and native transcripts local in both modes. Set `execution.target` explicitly to `LOCAL` or `SSH`. If SSH is selected, ask only for missing SSH alias/command, remote workspace, environment setup and scheduler/resource restrictions. Use the configured SSH authentication and optional `execution.ssh.configFile`; never copy Codex authentication to the server. The first implementation supports one directly accessible Unix/Linux execution server, not a scheduler or multiple nodes.

Workers default to `-1`: Mork resolves `max(1, availableProcessors / 2)` on the actual execution host. Preserve positive user overrides, including a request for 32 workers. Do not infer the server's worker count from the local computer. Screening always uses one sequential worker. Resolve tuning duration as `ignoreInitialMillis + intervalDurationMillis`; a 10-second request can use 1000 ms ignored and 9000 ms scoring. Keep comparison time limits consistent with the user's request.

For SSH setup and commands, read [remote execution](../../../docs/features/autollm.md#remote-execution-over-ssh). Build the local attached remote-runner JAR before remote preflight. Check Java/build tools, Git, R packages and selector Python on the server, and Codex locally. Use `POST /api/autollm/targets/probe` for remote diagnostics; static validation alone does not check connectivity. No dependencies are installed during measurements.

## Discover the project

- Identify the target example/application, Git repository root, baseline ref, build command, executable JAR, application working directory, and project-specific arguments. In a monorepo, use the Git root as `project.repository`, a build such as `./mvnw -pl <module> -am verify`, and a module-relative artifact path. Discover the available build tool and actual artifact name.
- Check Java/build tools, Git, R with `irace`, `httr`, and `jsonlite` on the execution host, and `codex login status` locally. Confirm that the baseline builds/tests on the selected host, has a solution validator, and supports AutoLLM. Its dependencies or committed reactor source must contain the AutoLLM implementation. Report missing prerequisites and follow the user's scope when installing or updating them.
- Locate the fixed SOTA component specification, optional additional seed specifications, training/comparison inputs, and comparison time policy. Reuse existing specifications. If SOTA exists only in an experiment or algorithm factory, translate that identified algorithm to the component JSON format described in the guide; ask if the reference algorithm is ambiguous.
- Locate the full original input pool separately from any training/testing indexes. Check for a previously approved screening and selection: validate its provenance and input hashes, and verify that testing equals the entire retained pool and includes every training instance. Reuse a valid approved selection unless the user requests replacement.
- Resolve input paths carefully: application defaults may be relative to the application's working directory, while campaign input paths resolve against `project.repository`. Prefer absolute paths for input files and directories. Keep `project.artifact` relative to the clone.
- Check the selected commit and working-tree changes. Trials clone the commit, so uncommitted code is excluded. Existing generated components in that commit are part of the baseline. Establish the intended baseline with the user when unclear; preserve the current checkout and existing proposals.

Generation and repair may adjust the project's existing exploration filter to prune composition branches and meet the actual parameter limit while preserving every supplied seed, including SOTA. They may also add support in Instance, Solution, and Move classes, including necessary initialization, copy, and update wiring, while preserving existing APIs, scoring, feasibility, and behavior.
Keep other existing sources preserved. Use new inventory filters, factories, parameter providers, and *Util classes where needed; use project run arguments for tree depth and derivation repetition. 

## Screen instances and prepare datasets

Before configuring or starting a campaign, complete [instance screening](../../../docs/features/instance-screening.md) unless a valid approved selection already exists for the intended SOTA, executable, inputs and host. The absence of training/testing indexes means preparation is required; do not substitute the raw pool as training or skip screening. This dataset preparation is authorized when the user asks to prepare or run the campaign.

In SSH mode, invoke screening with the same `autollm.project` and `autollm.execution` settings as the planned campaign. The local command delegates builds and screening to the server and prints the actual mirrored proposal directory under the requested output. Review that completed report locally. After agreement, run the approval command with the original output directory, the same SSH settings and the reviewed proposal hash; approval executes on the server. Use the printed mirror's published `train.index`/`test.index`. Preserve its `remote-screening.json`, data and frozen baseline JAR; AutoLLM adopts the exact screened artifact. Local screening evidence cannot qualify a remote campaign.

Use the full original pool and fixed SOTA. The defaults are a 10 ms fast threshold, an observation window equal to the candidate margin rounded up to milliseconds with a one-second minimum, one initial run per input, and 30 fresh confirmations only for inputs with initial TTB at most 30 ms. Both phases therefore use a one-second default window; respect an explicitly configured longer horizon. Exclude an input only when strictly more than 95% of confirmations (at least 29 of 30) reach the common best objective before 10 ms. Preserve inconclusive inputs. Reuse one assertions-disabled worker JVM sequentially across the initial pass and all confirmations, warming it once and resetting the seed, metrics and time limit for each measurement. Inspect existing owned work before starting or resuming screening; do not run duplicate timing jobs or silently reuse incomplete evidence.

The preparation command then runs Mork's property-based instance selector on the retained pool to choose `ceil(15% × retained)` preliminary training instances, unless the user selected another fraction. Final **testing contains ALL retained instances, including those used for training**. Do not subtract training from testing, select preliminary instances by hand, or replace Mork's selector with random sampling.

Show the screening criteria, provenance, retained/excluded/inconclusive counts and detailed lists, proposed training/testing counts and their overlap. Obtain explicit user agreement before `--instance-screening-approve` with the reviewed proposal hash or using these indexes in a campaign. This review concerns the concrete completed selection; do not ask for it before preparing the report. Use only published indexes accompanied by `approved.json` with valid hashes and full-pool testing coverage. Set `trainingPath` to `train.index` and `comparisonPath` to `test.index`, using their actual absolute or repository-relative paths. Freeze both across trials. Screening approval alone does not authorize starting a campaign.

## Configure and validate

Use the user's choices for trials, seed, prompt/model settings, pilot/repair limits, tuning budget, workers, and comparison repetitions/time limits. Keep unspecified operational settings from backend defaults. Ask only for missing inputs or consequential ambiguities; state the effective trial count and budgets before starting.

Launch the matching project's executable JAR from its application working directory using `--autollm`. Set the workspace before startup if the user chose one. Reuse an existing matching AutoLLM backend when available. Verify `GET /api/monitor/status` reports `AUTOLLM`; HTTP availability alone does not establish the execution mode.

Fetch `GET /api/autollm/defaults`, merge the selected settings, and save the complete campaign JSON to a named local file. Use camelCase JSON keys, argument arrays for build/run commands, and milliseconds for durations. The campaign workspace must match the backend's fixed workspace. Preserve the backend's comparison/restart policy and disabled ablation. Generated algorithm components must end in `New`; utilities are exempt. The parameter limit must remain below 300.

Submit the saved JSON to `POST /api/autollm/validate`. Inspect the returned `valid` field and `errors`, including when HTTP status is 200. Resolve configuration errors before submission. Full baseline/component verification happens inside the campaign.

Before launching, measure and report the baseline's actual emitted irace parameter declaration count with a metadata-only search-space check such as `--autollm-check` or `POST /api/autollm/campaigns/{id}/baseline-check` on a saved draft, using the selected baseline, intended campaign configuration, run arguments, and seed files. Read `parameters` from `execution/check.json` and compare it with `maxParameters`; do not infer it from the number of components. If over budget, recommend pruning while preserving every supplied seed, and rebuild/recheck after any filter or search-space setting changes.

Generated SSH-mode conversations receive a controller-owned metadata helper command. Use it for remote build/tests and parameter checks; inspect its local mirrored diagnostics. Do not substitute local builds or launch independent pilot/tuning/comparison jobs. Transport outages do not justify component repairs.

When the user asks only to configure or prepare, stop with the saved configuration, validation outcome, and dashboard URL. A request to run/start authorizes the campaign's Codex generation and tuning; proceed within that scope without asking again for the same authorization.

## Start and inspect

For a new requested run, check `GET /api/autollm/campaigns` for active work, then submit the configuration to `POST /api/autollm/campaigns` unless a draft was already created for the baseline check. Save/reuse the returned campaign ID before calling `POST /api/autollm/campaigns/{id}/start`. For an existing draft, start that ID. Inspect persisted state after an uncertain response rather than creating a replacement campaign.

Keep the backend in an owned, persistent process with redirected logs. Record its PID, working directory, base URL, log path, workspace, and campaign ID alongside the configuration so another conversation can find it. Starting the backend alone does not start a campaign.

For SSH campaigns also record the remote host/workspace, execution-host identity, requested/effective workers and saved job/status paths. A lost SSH connection leaves the remote supervisor and watchdog running. Inspect/rejoin the existing saved job rather than creating another. Show `WAITING_FOR_REMOTE` and `CANCELLATION_PENDING` accurately; cancellation is confirmed only after remote termination and evidence collection. Killing the SSH client does not establish that the workload stopped. Reporting from collected evidence remains local and does not retune.

Use `GET /api/autollm/campaigns/{id}` for campaign/trial outcomes. For detailed progress and comparison results, use `/api/autollm/campaigns/{id}/trials/{number}/progress` and `/comparison`. Use the guide's artifact and paged-log endpoints for diagnostics. Read logs in bounded pages.

For follow-up requests, reuse the saved ID and inspect state first. Cancel with `POST /api/autollm/campaigns/{id}/cancel`; resume interrupted/cancelled work with `/resume` when the workspace is idle. Completed failed trials remain failed. Regenerate reports with `/report` while idle, without retuning. Leave repair turns and interrupted-stage recovery to Mork.

Return the configuration path, campaign ID and actual status, dashboard URL, and evidence location. Distinguish a launched campaign from a completed experiment. When results are available, summarize the winner/components and objective/runtime differences against SOTA using saved comparison data, preserving failed and incomplete trial counts.
