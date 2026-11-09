# TSP dashboard customization

This example uses the editable framework source in `template/frontend` and keeps its own visualizer and
registration in `src/custom/`. There is no duplicate dashboard source or separate npm installation here.
The public extension API is imported as `@mork/extension-api`, configured in the shared TSP TypeScript build.

From `template/frontend`:

```sh
npm ci
npm run typecheck:tsp
npm run test:tsp
npm run build:tsp
npm run verify:tsp
```

`build:tsp` selects the example registration and styles, and rebuilds
`example-tsp/src/main/resources/static/`. The default template output is untouched. Commit the complete
generated output with frontend changes so Maven builds and packaged examples work without Node.js.
Then build the backend from the repository root with `./mvnw -pl example-tsp -am package`.

The Java `TSPSolutionViewProvider` publishes `tsp` snapshots with payload version 1: `cities` contains
coordinates and `tour` contains zero-based indices in visit order. `tsp-route.ts` validates and projects
the data; `TspSolutionView` renders a closed SVG route, objective value, and accessible visit order.
It receives only the documented public inputs. It has no dependency on framework stores or result models.

All frontend source remains editable. Preserve `src/custom/` on upgrades, merge your framework changes,
and follow [`template/frontend/MIGRATIONS.md`](../../template/frontend/MIGRATIONS.md) for supported API changes.
The tutorial includes [provider, registration, run, and migration instructions](../../docs/examples/TSP/index.md).

## Browser checks and screenshot reproduction

The shared browser test can check the compiled TSP bundle against its deterministic fixture:

```sh
# From template/frontend; requires Chromium.
MORK_STATIC_ROOT=../../example-tsp/src/main/resources/static MORK_TSP_VIEW=1 npm run test:browser
```

For documentation screenshots, run the Berlin52 command in the tutorial, then point the same test at
that real backend:

```sh
MORK_MONITOR_URL=http://localhost:8080 MORK_TSP_VIEW=1 npm run test:browser
```

It verifies the route against the backend snapshot, checks closure and mobile layout, and writes
`tsp-dashboard.png`, `tsp-route.png`, and `tsp-route-mobile.png` to the temporary directory printed on
completion. Use the real-run images for documentation, not the triangle fixture images. Copy the first
two to `docs/examples/TSP/img/dashboard.png` and `docs/examples/TSP/img/route.png`; the homepage copies are
`web/assets/dashboard.png` and `web/assets/tsp-route.png`. Record the actual run configuration when refreshing
them. The chart named “Best score across repetitions” is not an intra-run convergence curve.
