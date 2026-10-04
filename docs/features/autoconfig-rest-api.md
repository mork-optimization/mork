# Autoconfig REST API

Mork exposes a read-only REST API for inspecting the autoconfig process. The API describes the run owned by the
current application process; it does not start, stop, or retain previous runs.

The base path is:

```text
/api/autoconfig
```

## Run Status

`GET /api/autoconfig/status` returns the current lifecycle and budget:

```json
{
  "runId": "cda68d18-37cb-4cc8-8c3e-3a5533017c15",
  "mode": "AUTOCONFIG",
  "role": "COORDINATOR",
  "state": "RUNNING",
  "phase": "RACING",
  "preparedAt": "2026-07-24T10:00:00Z",
  "startedAt": "2026-07-24T10:00:01Z",
  "finishedAt": null,
  "elapsedMillis": 12000,
  "budget": {
    "maximum": 10000,
    "used": 421,
    "remaining": 9579
  },
  "evaluations": {
    "running": 8,
    "succeeded": 400,
    "rejected": 10,
    "failed": 3,
    "slow": 7
  },
  "latestEvaluationRevision": 842,
  "generatedParameterCount": 48,
  "trainingInstanceCount": 32,
  "metric": {
    "objectiveName": "distance",
    "objectiveMode": "MINIMIZE",
    "kind": "AREA_UNDER_CURVE",
    "costMode": "MINIMIZE",
    "negated": false,
    "auc": {
      "ignoreInitialMillis": 10000,
      "intervalDurationMillis": 50000,
      "logScale": true
    }
  },
  "irace": {
    "iteration": 3,
    "eliteCount": 5,
    "updatedAt": "2026-07-24T10:00:10Z",
    "finalSnapshot": false,
    "progress": {
      "nbIterations": 6,
      "maxExperiments": 10000,
      "experimentsUsed": 421,
      "remainingBudget": 9579,
      "remainingBudgetEstimated": false,
      "currentBudget": 120,
      "currentBudgetUsed": 100,
      "maxTime": 0.0,
      "timeUsed": 0.0,
      "remainingTime": null,
      "boundEstimate": null
    }
  },
  "failure": null
}
```

Modes are `DISABLED`, `IRACE`, and `AUTOCONFIG`. Roles are `COORDINATOR`, `WORKER`, and `DISABLED`.
Detailed phases are `NOT_STARTED`, `PREPARING`, `CHECKING_SCENARIO`, `RACING`, `POSTPROCESSING`,
`WAITING_FOR_WORK`, `COMPLETED`, and `FAILED`. The coarser `state` remains available and is derived from the
phase as `NOT_STARTED`, `PREPARING`, `RUNNING`, `COMPLETED`, or `FAILED`.

`metric` describes the cost returned to IRACE. Autoconfig always uses area under the main objective's curve;
manual IRACE uses the raw objective or AUC according to `irace.auc`. `costMode` is always `MINIMIZE`. When the
original objective is maximizing, Mork negates it and reports `negated: true`. `auc` is present only for
`AREA_UNDER_CURVE` metrics.

`trainingInstanceCount` is populated after the coordinator resolves the configured training set. It is `null`
while the run is still preparing and in follower processes.

An experiment consumes budget when Mork accepts it, even if it is still running or is later rejected. Therefore,
`used` equals the sum of running, successful, rejected, and failed evaluations.

The top-level `budget` is Mork's count. `irace.progress` is the latest snapshot reported by
IRACE at an iteration boundary. Mork logs a warning when their overlapping experiment counters differ, but keeps
and exposes both values. With a time budget, `remainingBudget` is IRACE's estimate and is not compared with
Mork's independently configured maximum. `progress` is `null` until the first iteration finishes.

## Search Space

`GET /api/autoconfig/search-space` returns a compact description of the generated space:

- root algorithm components;
- tree-depth and derivation-repetition limits;
- discovered components and their constructor parameters;
- scalar domains and component choices;
- `minItems` and `maxItems` for ordered component combinations;
- the final number of generated IRACE parameters and separate forbidden-constraint count;
- `encodingDiagnostics`: actual counts, the estimated former prefix-encoding counts, avoided declarations,
  and breakdowns by root and collection. Root counts exclude the single shared `ROOT` selector.

The resource becomes available after the coordinator generates the parameters for an automatic `--autoconfig`
run. It returns a `404 Not Found` problem response before generation, in follower processes, and when `--irace`
uses a custom `AlgorithmBuilder`, because the automatic search space does not apply to those execution modes.

The endpoint intentionally does not return the expanded derivation tree. Component identifiers are the same
names used by `$component` in [JSON algorithm descriptions](../concepts/algorithm-components/json-descriptions.md).

## Candidates and Elites

`GET /api/autoconfig/candidates/{configurationId}` returns the canonical description of one IRACE configuration:

```json
{
  "configurationId": "42",
  "parameters": {
    "ROOT": "VND",
    "ROOT_VND.improvers.length": "2"
  },
  "algorithm": {
    "$component": "VND",
    "improvers": [
      {"$component": "LocalSearchBestImprovement"},
      {"$component": "LocalSearchFirstImprovement"}
    ]
  },
  "decodeError": null,
  "evaluations": {
    "running": 2,
    "succeeded": 26,
    "rejected": 1,
    "failed": 1,
    "slow": 3
  }
}
```

A candidate is an algorithm configuration. IRACE may evaluate that candidate many times with different
instances and seeds, so evaluation records refer to it by `configurationId` instead of repeating the algorithm
JSON.

`GET /api/autoconfig/elites` returns the latest elite set reported at an iteration boundary. Elite `position`
is its order in that IRACE snapshot, not a globally comparable quality score. `finalSnapshot` becomes `true`
after the final IRACE result has been validated.

`GET /api/autoconfig/elites/history` returns one immutable snapshot per reported iteration in increasing order.
Each item contains its timestamp, IRACE progress counters, and ordered elite set. A repeated callback replaces
the previous snapshot for that iteration. The final elite set remains available from `/elites`; it is not added
to history as a synthetic iteration.

## Evaluations

`GET /api/autoconfig/evaluations` returns evaluations in increasing ID order. Supported query parameters are:

| Parameter | Meaning |
|-----------|---------|
| `after` | Return IDs greater than this cursor. Defaults to `0`. |
| `limit` | Page size from 1 to 500. Defaults to 100. |
| `state` | Optional evaluation state: `RUNNING`, `SUCCEEDED`, `REJECTED`, or `FAILED`. |

For example, `GET /api/autoconfig/evaluations?state=REJECTED&after=0&limit=500` lists rejected evaluations.

Each item contains the full instance, seed, timing, cost, rejection/error, and slow-overrun details. The page is
a point-in-time snapshot: if it contains a running evaluation, refetch that page to observe its final state.
`nextCursor` navigates later matching IDs; `latestId` is the latest ID assigned, including evaluations outside the
requested state. A state filter reflects the current state at request time. An evaluation with an older ID may later
move from `RUNNING` to `REJECTED` or `FAILED`, so refetch earlier pages when monitoring a filtered state in real time.
Invalid pagination values return status 400.

### Incremental changes

`GET /api/autoconfig/evaluations/changes` provides a mutation-safe cursor for live clients. It accepts an
exclusive `after` revision, defaulting to zero, and a `limit` from 1 to 500:

```json
{
  "runId": "cda68d18-37cb-4cc8-8c3e-3a5533017c15",
  "latestRevision": 842,
  "nextRevision": 500,
  "changes": [
    {
      "revision": 500,
      "evaluation": {
        "id": 250,
        "configurationId": "42",
        "instanceId": "7",
        "instanceName": "instances/example.txt",
        "seed": 123,
        "state": "SUCCEEDED",
        "cost": 12.5,
        "timeSeconds": 0.8
      }
    }
  ]
}
```

Mork appends a complete evaluation view when an evaluation starts and when it reaches a terminal state. Clients
upsert by evaluation ID and continue from `nextRevision`. Revisions reset for a new run, so a changed `runId`
means the client must clear its previous state. `status.latestEvaluationRevision` lets clients avoid requesting
an unchanged feed.

All evaluation records remain available until the application starts a new run or exits. The API keeps them in
memory together with at most two change records per evaluation, so large runs require proportionally more JVM
heap; it does not persist records across restarts.

## Artifacts

`GET /api/autoconfig/artifacts` lists files currently available for the process-owned coordinator run. Each entry
contains a stable ID, filename, media type, byte size, and last-modified time. Download an artifact through
`GET /api/autoconfig/artifacts/{id}`.

| ID | File | Content |
|----|------|---------|
| `final-elites` | `autoconfig-final-elites.json` | Final configurations emitted by IRACE |
| `plots` | `plots.pdf` | IRACE ablation plots |
| `irace-data` | `irace.Rdata` | IRACE R workspace |
| `stdout` | `runner.R.stdout.log` | Captured R standard output and report output |
| `stderr` | `runner.R.stderr.log` | Captured R diagnostics |

Only these fixed files can be downloaded. Missing artifacts and requests outside a coordinator run return 404.
Logs may be visible while they are still being written and remain available after a failed run. Preparing a new
coordinator run deletes every known artifact from the previous run. Artifact metadata is not reconstructed after
an application restart.

## Polling

A REST client can monitor a run without downloading repeated candidate descriptions:

1. Poll `/status`.
2. Fetch `/evaluations/changes` when `latestEvaluationRevision` advances and upsert its evaluation views.
3. Refresh `/elites` and `/elites/history` when the iteration or elite update timestamp changes.
4. Fetch `/candidates/{configurationId}` when the user opens an evaluation or elite.
5. Refresh `/artifacts` during postprocessing and after a terminal state.

The existing generic Mork event API remains separate from these autoconfig snapshots.

## Internal IRACE Endpoints

The R runner uses authenticated, implementation-only endpoints:

- `POST /internal/autoconfig/irace/evaluations` executes an IRACE batch.
- `POST /internal/autoconfig/irace/progress` publishes elites and progress counters at an iteration boundary.
- `POST /internal/autoconfig/irace/phase` publishes `RACING` and `POSTPROCESSING` transitions.

These endpoints are not user-facing and require the generated integration key.

The former `/execute`, `/batchExecute`, and `/auto/debug/**` endpoints no longer exist. Submit a one-element batch
to the internal evaluations endpoint when only one configuration must be evaluated.

Live elite updates require a recent IRACE version that supports the scenario option
`iterationCallback(iteration, elites, progress, ...)`. The bundled runner stops with an upgrade message when the
installed IRACE version does not support this option. Mork receives live updates directly from this callback and
therefore does not poll `irace.Rdata`. Iteration snapshots are retained for the current run; the validated final
elites replace the latest elite view without modifying iteration history.
