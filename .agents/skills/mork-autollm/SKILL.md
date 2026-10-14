---
name: mork-autollm
description: Configure, launch, inspect, and resume Mork AutoLLM campaigns when a user wants repeated component generation and autoconfig experiments for a Mork project.
---

# Mork AutoLLM

Turn the user's experimental choices into a reusable AutoLLM configuration and operate the campaign through Mork's local API. Mork owns independent clones, Codex sessions, repairs, tuning, comparison, and checkpoints.

Read the [AutoLLM guide](../../../docs/features/autollm.md) before configuring a campaign. If installed outside this repository, find `docs/features/autollm.md` in the Mork checkout. Use the guide and the running backend's defaults as the configuration reference.

## Discover the project

- Identify the target example/application, Git repository root, baseline ref, build command, executable JAR, application working directory, and project-specific arguments. In a monorepo, use the Git root as `project.repository`, a build such as `./mvnw -pl <module> -am verify`, and a module-relative artifact path. Discover the available build tool and actual artifact name.
- Check Java/build tools, Git, R with `irace`, `httr`, and `jsonlite`, and `codex login status`. Confirm that the baseline builds/tests, has a solution validator, and supports AutoLLM. Its dependencies or committed reactor source must contain the AutoLLM implementation. Report missing prerequisites and follow the user's scope when installing or updating them.
- Locate the fixed SOTA component specification, optional additional seed specifications, training/comparison inputs, and comparison time policy. Reuse existing specifications. If SOTA exists only in an experiment or algorithm factory, translate that identified algorithm to the component JSON format described in the guide; ask if the reference algorithm is ambiguous.
- Resolve input paths carefully: application defaults may be relative to the application's working directory, while campaign input paths resolve against `project.repository`. Prefer absolute paths for input files and directories. Keep `project.artifact` relative to the clone.
- Check the selected commit and working-tree changes. Trials clone the commit, so uncommitted code is excluded. Existing generated components in that commit are part of the baseline. Establish the intended baseline with the user when unclear; preserve the current checkout and existing proposals.

Generation and repair may adjust the project's existing exploration filter to prune composition branches and meet the actual parameter limit while preserving every supplied seed, including SOTA. They may also add support in Instance, Solution, and Move classes, including necessary initialization, copy, and update wiring, while preserving existing APIs, scoring, feasibility, and behavior.
Keep other existing sources preserved. Use new inventory filters, factories, parameter providers, and *Util classes where needed; use project run arguments for tree depth and derivation repetition. 

## Configure and validate

Use the user's choices for trials, seed, prompt/model settings, pilot/repair limits, tuning budget, workers, and comparison repetitions/time limits. Keep unspecified operational settings from backend defaults. Ask only for missing inputs or consequential ambiguities; state the effective trial count and budgets before starting.

Launch the matching project's executable JAR from its application working directory using `--autollm`. Set the workspace before startup if the user chose one. Reuse an existing matching AutoLLM backend when available. Verify `GET /api/monitor/status` reports `AUTOLLM`; HTTP availability alone does not establish the execution mode.

Fetch `GET /api/autollm/defaults`, merge the selected settings, and save the complete campaign JSON to a named local file. Use camelCase JSON keys, argument arrays for build/run commands, and milliseconds for durations. The campaign workspace must match the backend's fixed workspace. Preserve the backend's comparison/restart policy and disabled ablation. Generated algorithm components must end in `New`; utilities are exempt. The parameter limit must remain below 300.

Submit the saved JSON to `POST /api/autollm/validate`. Inspect the returned `valid` field and `errors`, including when HTTP status is 200. Resolve configuration errors before submission. Full baseline/component verification happens inside the campaign.

When the user asks only to configure or prepare, stop with the saved configuration, validation outcome, and dashboard URL. A request to run/start authorizes the campaign's Codex generation and tuning; proceed within that scope without asking again for the same authorization.

## Start and inspect

For a new requested run, check `GET /api/autollm/campaigns` for active work, then submit the configuration to `POST /api/autollm/campaigns`. Save the returned campaign ID before calling `POST /api/autollm/campaigns/{id}/start`. For an existing draft, start that ID. Inspect persisted state after an uncertain response rather than creating a replacement campaign.

Keep the backend in an owned, persistent process with redirected logs. Record its PID, working directory, base URL, log path, workspace, and campaign ID alongside the configuration so another conversation can find it. Starting the backend alone does not start a campaign.

Use `GET /api/autollm/campaigns/{id}` for campaign/trial outcomes. For detailed progress and comparison results, use `/api/autollm/campaigns/{id}/trials/{number}/progress` and `/comparison`. Use the guide's artifact and paged-log endpoints for diagnostics. Read logs in bounded pages.

For follow-up requests, reuse the saved ID and inspect state first. Cancel with `POST /api/autollm/campaigns/{id}/cancel`; resume interrupted/cancelled work with `/resume` when the workspace is idle. Completed failed trials remain failed. Regenerate reports with `/report` while idle, without retuning. Leave repair turns and interrupted-stage recovery to Mork.

Return the configuration path, campaign ID and actual status, dashboard URL, and evidence location. Distinguish a launched campaign from a completed experiment. When results are available, summarize the winner/components and objective/runtime differences against SOTA using saved comparison data, preserving failed and incomplete trial counts.
