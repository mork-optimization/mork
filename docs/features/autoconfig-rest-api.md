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
  "role": "COORDINATOR",
  "state": "RUNNING",
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
  "generatedParameterCount": 48,
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

Roles are `COORDINATOR`, `WORKER`, and `DISABLED`. Lifecycle states are `NOT_STARTED`, `PREPARING`, `RUNNING`,
`COMPLETED`, and `FAILED`.

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
- the final number of generated IRACE parameters.

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

## Evaluations

`GET /api/autoconfig/evaluations` returns evaluations in increasing ID order. Supported query parameters are:

| Parameter | Meaning |
|-----------|---------|
| `after` | Return IDs greater than this cursor. Defaults to `0`. |
| `limit` | Page size from 1 to 500. Defaults to 100. |

Each item contains the full instance, seed, timing, cost, rejection/error, and slow-overrun details. The page is
a point-in-time snapshot: if it contains a running evaluation, refetch that page to observe its final state.
`nextCursor` navigates later records in the same snapshot and is not an update-event cursor.
`historyTruncated`, `oldestRetainedId`, and `latestId` make bounded retention explicit. Aggregate status and
candidate counters remain exact after old evaluation details have been evicted. Invalid pagination values return
status 400.

## Polling

A REST client can monitor a run without downloading repeated candidate descriptions:

1. Poll `/status`.
2. Fetch or refetch `/evaluations` pages for execution details.
3. Refresh `/elites` when the status iteration or elite update timestamp changes.
4. Fetch `/candidates/{configurationId}` when the user opens an evaluation or elite.

The existing generic Mork event API remains separate from these autoconfig snapshots.

## Internal IRACE Endpoints

The R runner uses authenticated, implementation-only endpoints:

- `POST /internal/autoconfig/irace/evaluations` executes an IRACE batch.
- `POST /internal/autoconfig/irace/progress` publishes elites and progress counters at an iteration boundary.

These endpoints are not user-facing and require the generated integration key.

The former `/execute`, `/batchExecute`, and `/auto/debug/**` endpoints no longer exist. Submit a one-element batch
to the internal evaluations endpoint when only one configuration must be evaluated.

Live elite updates require a recent IRACE version that supports the scenario option
`iterationCallback(iteration, elites, progress, ...)`. The bundled runner stops with an upgrade message when the
installed IRACE version does not support this option. Mork receives live updates directly from this callback and
therefore does not poll `irace.Rdata`. Only the latest callback snapshot is retained; the validated final elites
replace the elite list without discarding that progress snapshot.
