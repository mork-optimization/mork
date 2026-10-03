import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { InstanceDashboardModel } from '../../model/dashboard';
import { SolutionGeneratedEvent } from '../../model/Events';

@Component({
  selector: 'app-solution-renderer',
  template: `
    <section class="solution" aria-label="Best solution details">
      @if (bestSolution(); as best) {
        <dl>
          <div>
            <dt>Algorithm</dt>
            <dd>{{ best.algorithmName }}</dd>
          </div>
          <div>
            <dt>Iteration</dt>
            <dd>{{ best.iteration }}</dd>
          </div>
          <div>
            <dt>{{ selectedObjective() ?? 'Score' }}</dt>
            <dd>{{ objectiveValue(best) }}</dd>
          </div>
        </dl>
        <p>
          Replace <code>SolutionRenderer</code> with a problem-specific visualization when the event
          contract exposes the required solution data.
        </p>
      } @else {
        <p class="empty">No successful solution has been reported for this objective yet.</p>
      }
    </section>
  `,
  styles: `
    .solution {
      min-height: 15rem;
      padding: 1rem;
      border-radius: 0.75rem;
      background: var(--mat-sys-surface-container-low);
      display: grid;
      align-content: center;
    }

    dl {
      margin: 0 0 1rem;
      display: grid;
      gap: 0.7rem;
    }

    dl div {
      display: flex;
      justify-content: space-between;
      gap: 1rem;
    }

    dt,
    p {
      color: var(--mat-sys-on-surface-variant);
    }

    dd {
      margin: 0;
      font-weight: 600;
      overflow-wrap: anywhere;
    }

    p {
      margin: 0;
      line-height: 1.5;
    }

    .empty {
      text-align: center;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SolutionRenderer {
  readonly instance = input.required<InstanceDashboardModel>();
  readonly selectedObjective = input.required<string | null>();
  readonly bestSolution = input.required<SolutionGeneratedEvent | null>();

  objectiveValue(solution: SolutionGeneratedEvent): number | string {
    const objective = this.selectedObjective();
    return objective ? (solution.objectives[objective] ?? 'Unavailable') : solution.score;
  }
}
