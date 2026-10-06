import { JsonPipe, NgComponentOutlet, DecimalPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  InjectionToken,
  Type,
  computed,
  inject,
  input,
} from '@angular/core';
import { ResultDetail } from '../../model/normal-experiment';

/** Registered renderers receive signal inputs named result, selectedObjective and view. */
export const SOLUTION_RENDERERS = new InjectionToken<Readonly<Record<string, Type<unknown>>>>(
  'Mork solution renderers',
  { providedIn: 'root', factory: () => ({}) },
);
@Component({
  selector: 'app-solution-renderer',
  imports: [JsonPipe, NgComponentOutlet, DecimalPipe],
  template: `
    @let result = detail().result;
    <dl>
      <div>
        <dt>Instance / experiment</dt>
        <dd>{{ result.instanceName }} / {{ result.experimentName }}</dd>
      </div>
      <div>
        <dt>Algorithm</dt>
        <dd>{{ result.algorithmName }}</dd>
      </div>
      <div>
        <dt>Iteration / seed</dt>
        <dd>{{ result.iteration }} / {{ result.seed }}</dd>
      </div>
      <div>
        <dt>Outcome</dt>
        <dd>{{ result.state }}</dd>
      </div>
      <div>
        <dt>{{ selectedObjective() ?? 'Objective' }}</dt>
        <dd>
          {{ (result.objectives[selectedObjective() ?? ''] | number: '1.0-4') ?? 'Unavailable' }}
        </dd>
      </div>
      <div>
        <dt>Runtime</dt>
        <dd>
          {{
            result.executionTime === null
              ? 'Unavailable'
              : (result.executionTime / 1000000 | number: '1.0-2') + ' ms'
          }}
        </dd>
      </div>
      <div>
        <dt>Time to best</dt>
        <dd>
          {{
            result.timeToBest === null
              ? 'Unavailable'
              : (result.timeToBest / 1000000 | number: '1.0-2') + ' ms'
          }}
        </dd>
      </div>
      <div>
        <dt>Result ID</dt>
        <dd>{{ result.resultId }}</dd>
      </div>
    </dl>
    <details>
      <summary>All objective values and custom properties</summary>
      <pre>{{ result.objectives | json }}</pre>
      <pre>{{ result.properties | json }}</pre>
    </details>
    @for (issue of detail().issues; track issue.id) {
      <p class="note">
        {{ issue.severity }} · {{ issue.scope }}: {{ issue.exceptionType }} — {{ issue.message }}
      </p>
    }
    @if (renderer(); as component) {
      <ng-container *ngComponentOutlet="component; inputs: rendererInputs()" />
    } @else if (detail().rendering.state === 'AVAILABLE') {
      <details open>
        <summary>Solution view data</summary>
        <pre>{{ detail().rendering.data | json }}</pre>
      </details>
    } @else {
      <p class="note">
        {{
          detail().rendering.state === 'UNSUPPORTED'
            ? 'A problem-specific solution view is not provided for this project.'
            : 'This solution view is no longer retained.'
        }}
        Metadata remains available; exported solution files are listed below or in Artifacts.
      </p>
    }
  `,
  styles: `
    dl {
      display: grid;
      grid-template-columns: repeat(2, minmax(0, 1fr));
      gap: 0.75rem;
    }
    dl > div {
      display: grid;
      gap: 0.25rem;
    }
    dt,
    .note {
      color: var(--mat-sys-on-surface-variant);
    }
    dd {
      margin: 0;
      overflow-wrap: anywhere;
    }
    pre {
      overflow: auto;
      max-height: 24rem;
      white-space: pre-wrap;
    }
    @media (max-width: 42rem) {
      dl {
        grid-template-columns: 1fr;
      }
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SolutionRenderer {
  readonly detail = input.required<ResultDetail>();
  readonly selectedObjective = input.required<string | null>();
  private readonly registered = inject(SOLUTION_RENDERERS);
  readonly renderer = computed(() =>
    this.detail().rendering.state === 'AVAILABLE'
      ? (this.registered[this.detail().rendering.rendererId ?? ''] ?? null)
      : null,
  );
  readonly rendererInputs = computed(() => ({
    result: this.detail().result,
    selectedObjective: this.selectedObjective(),
    view: this.detail().rendering.data,
  }));
}
