# Mork dashboard

Mork frontend, Angular 22.2 standalone application. It monitors the existing REST and STOMP event API.

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

## Architecture

`EventClient` owns HTTP/STOMP transport only. `DashboardStore` subscribes before requesting history, buffers live envelopes, downloads history in ranges of 1,000, deduplicates by event ID, and applies only contiguous IDs. It retrieves gaps through REST, catches up after reconnect, and clears execution state if a restarted backend begins again at event ID zero.

The store exposes readonly Angular signals and the `start()` and `selectObjective()` commands. High-volume solution events accumulate in ordinary data structures. A revision signal publishes chart view models at most once every two seconds, with immediate publication at synchronization, objective changes, instance completion, and execution completion.

All successful result values are retained for the ten newest instances. Failed results advance repetition progress but do not contribute to raw scores, convergence, or best-solution selection. The selector defaults to the first objective in `ExecutionStartedEvent` and respects `MINIMIZE` or `MAXIMIZE` when recomputing views.

## Custom solution rendering

`src/app/components/solution-renderer/solution-renderer.ts` is the problem-specific extension point. Its signal inputs provide:

- The current `InstanceDashboardModel`
- The selected objective name
- The best successful `SolutionGeneratedEvent` for that objective

The default component explains the extension and shows event metadata. Extend the backend event contract first if a visualization requires fields that are not present in `SolutionGeneratedEvent`; do not add transport metadata to event payloads.
