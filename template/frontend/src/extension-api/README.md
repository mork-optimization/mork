# Supported frontend extension API

The exports of [`public-api.ts`](public-api.ts) and the behavior documented here are Mork's supported
extension contract. All files remain editable. Changes to this contract must be recorded in
[`MIGRATIONS.md`](../../MIGRATIONS.md); breaking changes require affected versions, before/after examples,
and any corresponding Java changes. Additive changes must also be documented. Framework internals
outside this contract can change in any version without an extension migration guarantee.

Import from `public-api.ts`. The internal dependency-injection token and other files are implementation
details, even when TypeScript allows direct imports. Angular itself and the frontend build tools have
their own compatibility requirements; this contract does not freeze their versions.

## Implement a visualizer

Create `src/custom/solution-views/my-solution-view.ts`:

```ts
import { Component, input } from '@angular/core';
import { JsonPipe } from '@angular/common';
import {
  SolutionView,
  SolutionViewData,
  SolutionViewResult,
} from '../../extension-api/public-api';

@Component({
  selector: 'custom-my-solution-view',
  standalone: true,
  imports: [JsonPipe],
  template: `
    <h3>{{ result().instanceName }} — {{ result().algorithmName }}</h3>
    <p>Selected objective: {{ selectedObjective() ?? 'None' }}</p>
    <pre>{{ view() | json }}</pre>
  `,
})
export class MySolutionView implements SolutionView {
  readonly result = input.required<SolutionViewResult>();
  readonly selectedObjective = input.required<string | null>();
  readonly view = input.required<SolutionViewData>();
}
```

Replace the JSON preview with your problem-specific drawing. `SolutionViewData` contains unknown values:
validate/narrow them before using them as coordinates or other domain data. Your project owns the schema
returned by its Java provider and is responsible for keeping its component consistent with that schema.
Treat inputs as immutable snapshots, including nested objects. Never inject the payload as HTML.

Register it in `src/custom/configuration.ts`:

```ts
import { FrontendExtensions } from '../extension-api/public-api';
import { MySolutionView } from './solution-views/my-solution-view';

export const customConfiguration: FrontendExtensions = {
  solutionViews: { 'my-problem': MySolutionView },
};
```

The key must exactly match `SolutionViewProvider.rendererId()` in Java; matching is case-sensitive.
Use one standalone component per ID. The registration is fixed at bootstrap. `src/main.ts` calls the
public `provideSolutionViews()` helper with this configuration; framework code does not import custom
components. For your own host application or tests, the helper can also be used directly in Angular's
`providers` array. Register it once per application injector; multiple calls replace rather than merge
registries.

## Inputs and lifecycle

Components implement `SolutionView` with three required, unaliased Angular signal inputs named
`result`, `selectedObjective`, and `view`. The interface checks their TypeScript types; Angular still
requires the actual `input.required(...)` declarations. An ordinary field or an aliased input does not
fulfil the runtime contract.

- `result` supplies the fields in `SolutionViewResult`: result ID, experiment/instance/algorithm names,
  iteration label, seed, outcome, all objectives, custom properties, runtime, and time to best.
  `executionTime` and `timeToBest` are nanoseconds, or `null` when unavailable. These are result metadata,
  not the retained Java solution. Additional framework response fields are not part of this contract.
- `selectedObjective` is the selected objective name or `null`. It can change independently of the result.
- `view` is the retained JSON snapshot returned by `SolutionViewProvider.describe(solution)`.

Use signals/computed values or Angular input lifecycle handling to react to changes. A component may
receive a new result or payload without being recreated. Its inputs are available for template rendering
and initialization hooks, not for reading in the constructor. Components can be destroyed and recreated
when selection, availability, or dashboard visibility changes; release subscriptions/resources on destroy.
Instance identity and exact refresh timing are not guaranteed.

The framework creates a visualizer only when data is `AVAILABLE` and its ID is registered. An unknown ID
falls back to a JSON preview. `UNSUPPORTED` (no provider) and `UNAVAILABLE` (no retained view) show metadata
and availability information without constructing a custom visualizer. Closing the inspector removes it.

The Java provider is called for successful results retained as best for an objective, before solution
release; it is not a stream of intermediate search steps. Current retention is bounded to 128 views of
at most 256,000 UTF-8 bytes each, with superseded best views removed. These limits are implementation
details, not guarantees that a particular result will remain available. Implementations must handle the
documented availability states. No Java API or payload format change is required for this frontend split.
