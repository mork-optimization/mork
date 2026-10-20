# Screening trivial instances

The AutoLLM skill requires screening as a preparation step before tuning or final comparison, unless a matching approved selection already exists. It uses a fixed SOTA and proposes a fresh training/testing selection without changing the datasets or existing indexes. Review and explicitly approve the results before using the new indexes.

## Dashboard and durable jobs

The AutoLLM dashboard can prepare datasets locally or over SSH before a campaign exists. Choose the execution host, committed project/build/artifact and fixed SOTA, then provide the full original pool and training fraction. The dashboard shows build, warmup, initial measurements, confirmations, selection and approval phases, including completed/total measurement counts and last synchronization. It provides the report, every instance decision and downloadable membership lists before the explicit approval button.

For programmatic use, `POST /api/autollm/screenings` accepts:

```json
{
  "config": { "project": { "repository": "/absolute/repository", "ref": "COMMIT", "buildCommand": ["./mvnw", "verify"], "artifact": "target/project.jar" }, "workspace": "/absolute/controller-workspace", "sotaFile": "/absolute/sota.json", "execution": { "target": "LOCAL", "workers": 32 } },
  "settings": { "python": "/absolute/selector-venv/bin/python3" },
  "inputPath": "/absolute/full-pool.index",
  "trainingFraction": 0.15
}
```

Creation saves a `DRAFT` and returns its ID. Save that ID before `POST /screenings/{id}/resume`. Inspect `GET /screenings` or `/screenings/{id}` after an uncertain response; reuse that job instead of submitting a duplicate. Job settings, mirrors, remote IDs and progress persist under the controller workspace. The same executor and workspace reservation are shared with campaigns. Persistent infrastructure faults pause in `NEEDS_ATTENTION`; resume reattaches saved jobs, and cancellation reconciles their termination and evidence before releasing the reservation.

`GET /screenings/{id}/review` returns the completed report, full decisions, proposed counts, evidence paths and `proposalSha256`. After explicit agreement, send that hash in `POST /screenings/{id}/approve` as `{"proposalSha256":"THE_REVIEWED_HASH"}`. Wait for `APPROVED`, then use the mirror's published `proposal/train.index` and `proposal/test.index`. Approval verifies the existing provenance and evidence rules; it does not start a campaign. Standalone CLI preparation remains available below.

The [operator helper](autollm.md#backend-discovery-and-operator-helper) wraps these APIs. Append the following to `python3 .agents/skills/mork-autollm/scripts/mork_autollm.py --workspace /absolute/controller-workspace`:

```text
screening create --config /absolute/campaign.json --settings /absolute/screening-settings.json --input /absolute/full-pool.index --training-fraction 0.15
screening resume --id SAVED_ID
watch --screening SAVED_ID
screening review --id SAVED_ID
screening approve --id SAVED_ID --proposal-sha256 THE_EXACT_REVIEWED_HASH
```

`--settings` accepts a JSON object such as `{"python":"/absolute/selector-venv/bin/python3"}` and may be omitted to use backend defaults. Creating a job saves a draft. Watching stops at `AWAITING_APPROVAL`; approval requires the explicitly supplied proposal SHA-256 and is a separate command after review. `screening get`, `list` and `cancel` inspect or manage the same saved jobs.

## Remote screening

Choose local or remote workload execution before any measurements. Local preparation and approval continue to use the commands below. For SSH, use the same `autollm.project` and `autollm.execution` settings as the intended campaign, as described in [remote execution](autollm.md#remote-execution-over-ssh).

Run the local application JAR with `--instance-screening`, the additional SSH configuration, an absolute local SOTA file, the full local input pool and an output directory outside that pool. Set `instance-screening.python` to the prepared **remote** Python interpreter. Use `--server.port=0` when the local dashboard already occupies its usual port.

The controller clones the selected committed source locally, transfers Git/submodule snapshots and original archive containers, and builds/tests the baseline remotely. It freezes that executable, then runs the unchanged sequential screening/selection protocol on the server. Remote jobs retain their watchdogs and can be rejoined after connectivity loss. Resume the same command and output directory; do not duplicate a running job.

The command prints the actual local mirror directory, under `<output>/remote-<identity>/proposal`, with the completed report, full lists, proposed indexes and reviewed proposal hash. Its sibling `remote-screening.json` identifies the source commit, frozen JAR, SOTA and execution settings. Preserve this directory, its data and build evidence together; relative published indexes work in the local mirror without editing the reviewed bytes.

Review these completed artifacts locally. Only after explicit user agreement, rerun the local application with `--instance-screening-approve`, the **original output directory**, the same SSH configuration and `--instance-screening.review-sha256=<reviewed hash>`. Approval is delegated to the same server, which checks its host, exact executable and input hashes. The resulting `approved.json` and indexes are mirrored and verified locally.

Set campaign training/comparison paths to the mirror's published `train.index` and `test.index`. The campaign adopts the exact tested baseline artifact, checks host and source identity, and freezes its own copies of the approved inputs. Moving to different hardware or changing the baseline/SOTA/inputs requires a new preparation and review. Locally measured screening evidence cannot qualify a remote campaign. Screening still uses one sequential JVM even when tuning requests 32 workers.

## Criterion

The defaults are a 10 ms fast threshold and 30 confirmation runs. Both phases use the same observation window: the candidate margin (`threshold-millis * candidate-multiplier`), rounded up to whole milliseconds, with a minimum of one second. This gives a one-second default window. An explicit `horizon-millis` may select a longer window and must cover the candidate margin and be at least one second. First run SOTA once on every input. Only inputs whose first-pass time to best is at most three times the threshold (30 ms) receive the additional 30 runs. Retain the others.

For each candidate, take the best objective observed across the initial and confirmation runs. Exclude the input only when strictly more than 95% of confirmation runs reach this **same observed best** before 10 ms: at least 29 of 30, excluding the initial run from that count. Equality uses the declared objective's comparison rules. A better result discovered late changes the common target and can prevent exclusion.

All runs observe the full horizon, including instances that appear fast initially. TTB is the first attainment time in the recorded objective curve, not the last time the solution object was modified. Failures, missing curves and inconsistent timing are retained as inconclusive and shown in the report. This is an empirical rule for the selected SOTA, host and observation window; it does not establish optimality or rule out improvements beyond that window.

## Prepare

Build the chosen example with its autoconfig dependency. Prepare a Python interpreter containing the existing instance selector's dependencies from `py-tools/instance-selector/requirements.txt`; screening checks these before measurement and does not install packages. Run on the host intended for the subsequent campaign, without competing measurement jobs.

For a fresh isolated interpreter, run this on the chosen execution host, substituting the checkout path:

```bash
python3 -m venv /absolute/selector-venv
/absolute/selector-venv/bin/python3 -m pip install -r /absolute/mork/py-tools/instance-selector/requirements.txt
/absolute/selector-venv/bin/python3 -c 'import numpy,pandas,sklearn,yellowbrick,matplotlib,seaborn'
/absolute/selector-venv/bin/python3 -m pip freeze > /absolute/selector-requirements-frozen.txt
```

Setuptools is pinned because Yellowbrick imports distutils on Python 3.12. Complete setup and record the environment before timing measurements; setup is never performed by a measurement job.

```bash
java -da -jar target/project.jar --instance-screening \
  --instance-screening.sota-file=/absolute/path/sota.json \
  --instances.for-selection=/absolute/path/full-pool.index \
  --instance-screening.output=/absolute/path/screening/run-01 \
  --instance-screening.python=/absolute/path/venv/bin/python3 \
  --event.webserver.stop-on-execution-end=true
```

`sota-file` contains one component JSON object, not a seed array. For these examples, use the previously identified frozen SOTA specification; do not substitute a generated proposal. Input selection defaults to `instances.path.default` when `instances.for-selection` is absent. Files, directories, indexes and archive entries use normal Mork load semantics. Keep output outside the input dataset directory.

One sequential, assertions-disabled worker JVM handles the complete initial pass and all subsequent confirmations. It performs five one-second warm-up runs once, before the first uncached measurement, and reuses the warmed JVM across instances and both phases. Every run builds a fresh algorithm and resets its random seed, metrics and time limit. Loading, algorithm construction, validation and reporting are outside measured time. The SOTA has the same outer restart policy used by AutoLLM comparisons. The worker has the existing AutoLLM process watchdog and heap settings. A process crash or watchdog termination stops preparation without publishing a partial selection; resume the same command to reuse completed measurements in a newly warmed worker.

| Property | Default |
| --- | ---: |
| `instance-screening.threshold-millis` | 10 |
| `instance-screening.candidate-multiplier` | 3 |
| `instance-screening.horizon-millis` | `max(1000, ceil(threshold-millis * candidate-multiplier))` |
| `instance-screening.confirmation-runs` | 30 |
| `instance-screening.fast-fraction` | 0.95 |
| `instance-screening.warmup-repetitions` | 5 |
| `instance-screening.warmup-millis` | 1000 |
| `instances.preliminar-percentage` | 0.15 |
| `solver.seed` | 1234 |

The initial run uses the base seed. Confirmations use base+1 through base+30; warm-up seeds are separate. Resume the same command and output directory to reuse completed runs. Changed inputs, SOTA, executable, host or settings require a new directory. The fingerprint includes the actual input contents, including archive-entry payloads. A directory lock prevents simultaneous writers. Do not rebuild the executable between screening and approval.

After filtering, the existing property-based selector chooses `ceil(15% × retained inputs)` for training. Final testing contains **all retained inputs, including the training inputs**. Training is a subset of testing; overlap is intentional. Small or constant-feature pools use a deterministic bounded-clustering fallback. An empty retained pool or a failed selector produces a report without an approvable selection. A single retained input can belong to both sets.

## Review and approve

The preparation command prints the summary and writes:

- `report.md` and `instances.csv`: criteria, host and SOTA identity, counts, every retained/excluded/inconclusive input, fast-hit counts and reasons. Median, p95 and maximum confirmation TTB summarize only runs attaining the common target; the attainment count identifies unreached targets separately.
- `runs/`: per-run seeds, objectives, runtime and objective curves; `jobs/` retains worker logs and ownership/deadline evidence.
- `manifest.json` and `proposal.json`: frozen provenance and hashes of the reviewed artifacts.
- `proposed-retained.index`, `proposed-train.index`, `proposed-test.index`: portable proposed selections. Original files and indexes remain untouched.

Show the user the summary and detailed lists, obtain explicit agreement, and use the proposal SHA-256 printed by preparation:

```bash
java -da -jar target/project.jar --instance-screening-approve \
  --instance-screening.output=/absolute/path/screening/run-01 \
  --instance-screening.review-sha256=THE_REVIEWED_PROPOSAL_SHA256 \
  --event.webserver.stop-on-execution-end=true
```

Approval verifies the reviewed hashes, original inputs, SOTA, executable and host, checks that testing covers every retained input and includes training, then publishes `retained.index`, `train.index`, `test.index` and `approved.json`. An approval command is an explicit acceptance action; agents must not invoke it without the user's agreement. Changes after review invalidate that acceptance and require review again.

After approval, configure AutoLLM `trainingPath` with `train.index` and `comparisonPath` with `test.index`. Standalone AutoConfig uses `--instances.path.irace=/absolute/path/screening/run-01/train.index`; normal final experiments use their instance path setting with the approved testing index. Index entries are relative to their index files and preserve archive-entry names.

Freeze this selection across all campaign trials. Approving a selection does not authorize starting a campaign, and screening is never rerun in response to generated algorithm performance. Screening timings are host-specific; screen again when moving the experiment to different hardware.
