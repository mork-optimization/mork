# Frontend extension migrations

Read this file when upgrading Mork. Preserve `src/custom/` and project assets; merge your modifications
to framework and build files. All source remains editable. Internal framework changes may happen in any
version and are the user's merge responsibility.

Every supported extension API change must add an entry here identifying the affected Mork versions.
Breaking changes must include before/after code and migration steps covering registration, types,
behavior, and Java providers when affected. Additive changes must explain the new API and whether action
is needed. Update the extension contract tests with behavioral changes. Toolchain changes must state
their requirements; a stable extension contract does not imply a permanently fixed Angular version.

## 0.23 development: explicit extension ownership

Applies when upgrading from the frontend that registered visualizers directly in `app/app.config.ts`
to the frontend containing `src/extension-api/` and `src/custom/`. This is a source-level migration;
the backend renderer ID and JSON payload are unchanged.

### 1. Move your components and update imports

Move custom components to `src/custom/solution-views/`. Previously, a component imported internal models:

```ts
import { ResultView } from '../../model/normal-experiment';

export class MySolutionView {
  readonly result = input.required<ResultView>();
  readonly selectedObjective = input.required<string | null>();
  readonly view = input.required<Readonly<Record<string, unknown>>>();
}
```

Use the public contract instead (retain your existing standalone component decorator/template):

```ts
import {
  SolutionView,
  SolutionViewData,
  SolutionViewResult,
} from '../../extension-api/public-api';

export class MySolutionView implements SolutionView {
  readonly result = input.required<SolutionViewResult>();
  readonly selectedObjective = input.required<string | null>();
  readonly view = input.required<SolutionViewData>();
}
```

The result metadata contract includes names, ID, seed, iteration, outcome, objectives, properties, runtime,
and time to best. Internal fields `instanceId`, `ordinal`, `startedAt`, and `endedAt` are not supported
extension inputs. If your visualization needs extra problem data, return it from the Java provider's
`describe()` map and read it from `view()`; keep framework-specific integrations under your own maintenance.

### 2. Move registration out of framework configuration

Before, `app/app.config.ts` imported the token from the host component:

```ts
import { SOLUTION_RENDERERS } from './components/solution-renderer/solution-renderer';

// Inside appConfig.providers:
{ provide: SOLUTION_RENDERERS, useValue: { 'my-problem': MySolutionView } }
```

Remove that import and provider. In `src/custom/configuration.ts`:

```ts
import { FrontendExtensions } from '../extension-api/public-api';
import { MySolutionView } from './solution-views/my-solution-view';

export const customConfiguration: FrontendExtensions = {
  solutionViews: { 'my-problem': MySolutionView },
};
```

Adopt the updated `src/main.ts`, which combines framework providers with this registration. For tests
or a custom bootstrap, replace direct token providers with `provideSolutionViews({ 'my-problem': MySolutionView })`
imported from `extension-api/public-api`. No compatibility export remains at the old component path.

### 3. Preserve styles and rebuild

Move project-wide style additions to `src/custom/styles.scss`; retain component styles with their
components. Adopt the `angular.json` entry that loads custom styles after `src/styles.scss`. Preserve any
other local changes when merging build configuration. `public/custom/` is a suggested location for
project assets; existing asset paths do not have to change.

Run `npm ci` if dependencies changed, then `npm run typecheck`, `npm test`, `npm run build:static`,
`npm run verify:static`, and the browser checks. Replace generated assets as a complete build; do not merge
hashed JavaScript bundles or store custom source inside `src/main/resources/static`.
