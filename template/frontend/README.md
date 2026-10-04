# Mork dashboard

Mork frontend, Angular 22.2 standalone application. It automatically selects the appropriate monitor for the
backend's execution mode:

- Standard experiments use the REST/STOMP event dashboard.
- Coordinator `--irace` and `--autoconfig` runs an specific panel.
- Worker processes show a lightweight role notice because the coordinator owns the run-wide monitoring state.

## Requirements

- Node.js 26
- npm 11 (the exact package-manager version is recorded in `package.json`)
- A Mork backend on `http://localhost:8080` for local development

Install the locked dependencies with `npm ci`, then use:

| Command                 | Purpose                                                           |
| ----------------------- | ----------------------------------------------------------------- |
| `npm start`             | Run the development server on `http://localhost:4200`             |
| `npm run typecheck`     | Check application and test TypeScript                             |
| `npm test`              | Run Vitest once, without watch mode                               |
| `npm run build`         | Build production files into `dist/`                               |
| `npm run build:static`  | Build into `../src/main/resources/static` for Spring packaging    |
| `npm run verify:static` | Verify every local asset referenced by the committed static index |

`build:static` deletes and recreates `../src/main/resources/static` before building. This prevents stale hashed bundles from accumulating. Put every asset that must be served from that directory in this frontend's `public/` directory so Angular copies it into each build, then commit the complete generated output.

## Standard experiment architecture

`EventClient` owns HTTP/STOMP transport only. `DashboardStore` subscribes before requesting history, buffers live envelopes, downloads history in ranges of 1,000, deduplicates by event ID, and applies only contiguous IDs. It retrieves gaps through REST, catches up after reconnect, and clears execution state if a restarted backend begins again at event ID zero.

The store exposes readonly Angular signals and the `start()` and `selectObjective()` commands. High-volume solution events accumulate in ordinary data structures. A revision signal publishes chart view models at most once every two seconds, with immediate publication at synchronization, objective changes, instance completion, and execution completion.

All successful result values are retained for the ten newest instances. Failed results advance repetition progress but do not contribute to raw scores, convergence, or best-solution selection. The selector defaults to the first objective in `ExecutionStartedEvent` and respects `MINIMIZE` or `MAXIMIZE` when recomputing views.

## Custom solution rendering

`src/app/components/solution-renderer/solution-renderer.ts` is the problem-specific extension point. Its signal inputs provide:

- The current `InstanceDashboardModel`
- The selected objective name
- The best successful `SolutionGeneratedEvent` for that objective

The default component explains the extension and shows event metadata. Extend the backend event contract first if a visualization requires fields that are not present in `SolutionGeneratedEvent`; do not add transport metadata to event payloads.

## Implementation

`AutoconfigStore` polls `GET /api/autoconfig/status` to detect the mode and drives the tuning dashboard from
readonly signals. It follows the mutation-safe `/evaluations/changes` revision cursor, upserts evaluations by ID,
and only refreshes elite snapshots, search-space metadata, and artifacts when their server-side markers change.
The active run ID scopes all state: a new ID or a reset revision clears the previous run before replaying changes.

The budget ribbon remains visible on every tuning tab. The dashboard deliberately shows actual evaluation budget
and completed IRACE checkpoints; it does not infer a linear iteration percentage. Its tabs provide:

- **Overview:** evaluation health, successful costs, elite rank evolution, current elites, and run information.
- **Evaluations:** cumulative activity, per-instance summaries, searchable/filterable evaluation ledger, slow-run
  markers, and rejected/failed details.
- **Elites:** current ranked configurations and immutable checkpoint history. Selecting a configuration loads its
  decoded algorithm and parameters on demand.
- **Artifacts:** the fixed, backend-whitelisted logs, plots, R workspace, and final elite output.

Evaluation failures, rejections, slow runs, and terminal run failures appear in the alert menu. Alert acknowledgement
and overview panel hide/collapse preferences are stored locally per browser. Hidden panels can be restored from the
Overview **Panels** menu. Automatic mode exposes a generated search-space dialog once the backend publishes it.

The tuning monitor is read-only. It intentionally provides no start, stop, or experiment-control actions and does
not collect worker telemetry. See `docs/features/autoconfig-rest-api.md` for the exact polling contract.
