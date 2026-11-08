# Mork dashboard

Mork frontend, Angular 22.2 standalone application. It automatically selects the appropriate monitor for the
backend's execution mode:

- Standard experiments use the backend-authoritative REST/STOMP dashboard.
- Coordinator `--irace` and `--autoconfig` runs an specific panel.
- AutoLLM campaigns use a campaign summary, task-focused tabs, and a trial inspector.
- Worker processes show a lightweight role notice because the coordinator owns the run-wide monitoring state.
- Instance-property/selection utilities show a utility notice, not an experiment monitor.

## Ownership and upgrades

The entire frontend is editable. Directory ownership describes compatibility expectations, not edit permissions.

| Location | Ownership and upgrade expectations |
| --- | --- |
| [`src/app/`](src/app/README.md) | Framework implementation. Internals may change in any version; users merge their modifications. |
| [`src/extension-api/`](src/extension-api/README.md) | Supported public contracts exported by `public-api.ts`. All API changes are documented; breaking changes include migration examples. |
| [`src/custom/`](src/custom/README.md) | Project-owned visualizers, registration, and styles. Preserve this directory during upgrades and apply documented contract migrations when needed. |
| `src/main.ts`, `src/environments/`, `src/styles.scss`, `src/index.html`, build configuration, and scripts | Editable framework/integration infrastructure. Merge local changes and review toolchain requirements on upgrades. |
| `public/` | Source assets. Use `public/custom/` for project assets to distinguish them from framework assets such as `favicon.svg`. |
| `dist/` and `../src/main/resources/static/` | Generated output; rebuild and replace, never merge compiled bundles. |

For ordinary customization, import only `src/extension-api/public-api.ts`. Importing internal framework
code remains possible, but accepts the same upgrade responsibility as editing it. Only `src/main.ts`
wires project configuration into the framework; the framework does not import user components.
This is a source-ownership boundary, not a separate package or automatic updater.

Read [MIGRATIONS.md](MIGRATIONS.md) before upgrading. It includes the migration from direct renderer
registration in `app.config.ts` to `custom/configuration.ts`. Angular and build-tool upgrades may still
require dependency changes even when the Mork extension contract is unchanged.

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
| `npm run test:browser`  | Exercise a production build in installed Chromium                 |
| `npm run build`         | Build production files into `dist/`                               |
| `npm run build:static`  | Build into `../src/main/resources/static` for Spring packaging    |
| `npm run verify:static` | Verify every local asset referenced by the committed static index |

`build:static` deletes and recreates `../src/main/resources/static` before building. This prevents stale hashed bundles from accumulating. Put every asset that must be served from that directory in this frontend's `public/` directory so Angular copies it into each build, then commit the complete generated output.

## Standard experiment architecture

`MonitorStore` reads the core-owned `/api/monitor/status`, so normal mode detection does not depend on the autoconfig module. `DashboardStore` reads `/api/experiments` snapshots; events only notify it that a refresh is due. Normal progress is recorded when work starts/completes, before ordered export consumption, so parallel results are visible even while an earlier repetition is running.

`EventClient` subscribes before reading the event head, buffers live envelopes, and establishes a notification baseline on first connection. It catches up contiguous IDs in ranges of 1,000 after reconnect, deduplicates duplicates, and retrieves gaps through REST. Every head, page and envelope carries an `eventLogId`; changed identity detects a backend restart even when its new event IDs are already higher than the old cursor. Snapshot responses are independently bound to `runId` and revision.

The store exposes readonly signals and explicit commands. Active snapshots refresh every two seconds (ten seconds when idle/terminal), with event-driven summary refreshes and immediate detail refreshes at selection, reconnect, instance completion and execution completion. HTTP failures preserve last known data and retry with bounded exponential backoff; the user can retry immediately. Transport health is separate from execution outcome. Destroying the dashboard cancels its subscriptions, HTTP requests and scheduled work.

Overview provides execution/selected-instance/selected-algorithm progress, all-instance browsing, exact per-objective algorithm statistics, score/best-across-repetitions charts, boxplots and score-versus-runtime. Results provides a filtered, paginated ledger, result inspection and matching CSV/JSON exports. Issues retains computation, export and finalization failures with iteration/seed correlation. Artifacts exposes only registered files. Diagnostics describes transport health without implying experiment failure.

All instances remain available through server pagination; only ten visited detail models are cached in the browser. Large chart responses are explicitly sampled, while statistics, results and exports remain exact. Failed repetitions advance completed counts but have chart gaps and no best score. Cancelled work remains incomplete. The first ordered status objective is the default; all published objectives respect `MINIMIZE`/`MAXIMIZE`. Best-across-repetitions is not an intra-run optimization trajectory. Raw scores from different instances are not averaged.

Browser tests use the production `dist/` bundle and an HTTP/STOMP fixture, with no additional npm dependencies. Set `MORK_BROWSER` if Chromium is not on a known path. `MORK_STATIC_ROOT=../src/main/resources/static npm run test:browser` tests the packaged bundle; `MORK_MONITOR_URL=http://localhost:8080 npm run test:browser` checks a real completed standard run with exported artifacts. Desktop/mobile screenshots are written to the reported temporary directory.

## Custom solution rendering

Metadata and all objective values remain inspectable after backend solutions are released. An optional Spring `@Component` implementing `SolutionViewProvider<S,I>` returns a renderer ID and JSON-safe `Map<String,Object>`. Best views are captured before release, capped at 256 KB each and 128 retained views; superseded best views are removed. Non-best/evicted views report `UNAVAILABLE`; no provider reports `UNSUPPORTED`.

Create standalone visualizers under `src/custom/solution-views/`, implement `SolutionView` from
`src/extension-api/public-api.ts`, and register them in `src/custom/configuration.ts`:

```ts
export const customConfiguration: FrontendExtensions = {
  solutionViews: { 'my-problem': MySolutionView },
};
```

The framework supplies `result`, `selectedObjective`, and `view` signal inputs. See the
[public extension contract](src/extension-api/README.md) for a complete example, metadata types,
registration, input updates, lifecycle, and availability behavior. Unregistered renderer IDs show JSON;
unsupported/evicted views keep metadata, related issues, and artifact links. No framework edits are needed
for registration. The full frontend remains available for deeper, user-maintained modifications.

## Implementation

Once `MonitorStore` selects tuning mode, `AutoconfigStore` polls `GET /api/autoconfig/status` and drives the tuning dashboard from
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
- **Components** (automatic mode): a treemap of component frequency, a focused parent/child Sankey, a role-aware
  relationship matrix, and a server-paginated list of matching configurations.
- **Artifacts:** the fixed, backend-whitelisted logs, plots, R workspace, and final elite output.

Evaluation failures, rejections, slow runs, and terminal run failures appear in the alert menu. Alert acknowledgement
and overview panel hide/collapse preferences are stored locally per browser. Hidden panels can be restored from the
Overview **Panels** menu. Automatic mode exposes a generated search-space dialog once the backend publishes it.

The Components tab defaults to all evaluated candidates and counts each unique tree once, preserving repeated
placements within the tree. Switch to **Evaluation exposure** to weight placements by accepted evaluations
(including running, rejected, and failed evaluations), or to **Current elites** to inspect the latest elite set.
The inspector shows direct parent roles, direct children, root usage, and distinct-configuration coverage. Selecting
a relationship limits the configuration list to that exact parent/role/child edge. The focused flow shows the ten
largest relationships on each side and groups the rest as Other; the inspector lists every relationship. The matrix
offers top 10/20/50 components and normalizes percentages by distinct parent configurations.

The backend computes these statistics; the frontend does not download and recount every candidate tree. Snapshot
refreshes follow evaluation revisions and elite markers. Scope/selection changes discard obsolete responses,
and configuration detail remains available through the existing candidate dialog. Manual irace runs do not have
canonical automatic component trees, so their monitor omits this tab.

The tuning monitor is read-only. It intentionally provides no start, stop, or experiment-control actions and does
not collect worker telemetry. See `docs/features/autoconfig-rest-api.md` for the exact polling contract.

## AutoLLM campaign dashboard

AutoLLM uses the same Material toolbar, cards, tabs and persistent panel preferences as the other monitors. Overview shows active trials, issues and comparison coverage. Trials supports search, outcome filters, sorting and pagination. Opening a trial replaces the campaign dashboard with a dedicated view, breadcrumbs and previous/next navigation through the current filtered order. Returning preserves the campaign tab, filters, sorting and page. Trial detail exposes Summary, Results, Algorithm, Activity and Diagnostics; seeds and raw evidence live in Diagnostics. Campaign/trial/tab selections can be shared through the URL fragment.

Results automatically uses the first ordered objective from `GET /api/autollm/objectives` and its declared minimize/maximize direction. The objective selector loads another comparison immediately; users do not need to type names or guess direction. Comparisons also populate trial rows automatically. It shows host-matched SOTA comparisons, per-instance wins/ties/losses, coverage, exclusions and mean improvement. It does not rank trials across hosts. Zero references retain undefined percentages, and descriptive wins do not imply statistical significance. The inspector combines paginated per-instance statistics with existing sampled convergence curves.

Infrastructure retains qualification, availability, health, worker reservations and backend-controlled recovery actions. Artifacts load on entry and support filename/type/trial filters, pagination, bounded previews and complete downloads. The contextual primary action offers review, launch, issue resolution, resume or results as appropriate. Infrequent report and cleanup actions live in More actions; unavailable primary actions have inline explanations. Cleanup opens the reviewed plan in Artifacts and keeps its backend digest and blocker checks.

New campaign and Settings & dataset use Project, Dataset, Budget, Execution and Review steps. Common durations are edited in seconds and converted to the API's milliseconds; advanced JSON retains every setting. Draft review includes the generation-model requirement, server readiness, membership and exclusions before explicit approval. Training membership remains included in testing and is identified as such. The searchable dataset editor assigns each original instance to training and testing, testing only, or exclusions with a required reason; pagination never drops hidden instances. Server enrollment uses validated fields and environment variable rows. Both editors keep JSON as an explicit advanced import. Amendments show current and proposed settings and server selections before approval.

Status polling stays lightweight. Opening an inspector separately retrieves the full campaign evidence, cached until the status/attempt signature changes; compact polls do not erase winners, failures or archived conversation links. Mutations return typed receipts followed by an authoritative status read. Selection changes invalidate in-flight evidence, results and artifact reads, and each detail section exposes read errors without discarding its last known data.
