# Screening trivial instances

The AutoLLM skill requires screening as a preparation step before tuning or final comparison, unless a matching approved selection already exists. It uses a fixed SOTA and proposes a fresh training/testing selection without changing the datasets or existing indexes. Review and explicitly approve the results before using the new indexes.

## Criterion

The defaults are a 10 ms fast threshold and 30 confirmation runs. Both phases use the same observation window: the candidate margin (`threshold-millis * candidate-multiplier`), rounded up to whole milliseconds, with a minimum of one second. This gives a one-second default window. An explicit `horizon-millis` may select a longer window and must cover the candidate margin and be at least one second. First run SOTA once on every input. Only inputs whose first-pass time to best is at most three times the threshold (30 ms) receive the additional 30 runs. Retain the others.

For each candidate, take the best objective observed across the initial and confirmation runs. Exclude the input only when strictly more than 95% of confirmation runs reach this **same observed best** before 10 ms: at least 29 of 30, excluding the initial run from that count. Equality uses the declared objective's comparison rules. A better result discovered late changes the common target and can prevent exclusion.

All runs observe the full horizon, including instances that appear fast initially. TTB is the first attainment time in the recorded objective curve, not the last time the solution object was modified. Failures, missing curves and inconsistent timing are retained as inconclusive and shown in the report. This is an empirical rule for the selected SOTA, host and observation window; it does not establish optimality or rule out improvements beyond that window.

## Prepare

Build the chosen example with its autoconfig dependency. Prepare a Python interpreter containing the existing instance selector's dependencies from `py-tools/instance-selector/requirements.txt`; screening checks these before measurement and does not install packages. Run on the host intended for the subsequent campaign, without competing measurement jobs.

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
