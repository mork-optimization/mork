import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { SolutionView, SolutionViewData, SolutionViewResult } from '@mork/extension-api';
import { drawRoute } from './tsp-route';

@Component({
  selector: 'custom-tsp-solution-view',
  standalone: true,
  templateUrl: './tsp-solution-view.html',
  styleUrl: './tsp-solution-view.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TspSolutionView implements SolutionView {
  readonly result = input.required<SolutionViewResult>();
  readonly selectedObjective = input.required<string | null>();
  readonly view = input.required<SolutionViewData>();
  readonly drawing = computed(() => drawRoute(this.view()));
  readonly score = computed(() => {
    const objective = this.selectedObjective();
    const value = objective === null ? undefined : this.result().objectives[objective];
    return typeof value === 'number' && Number.isFinite(value) ? value.toFixed(2) : 'Unavailable';
  });
  readonly routeLabel = computed(() => {
    const tour = this.drawing()?.tour ?? [];
    const labels: number[] = [];
    for (const id of tour) labels.push(id + 1);
    if (labels.length) labels.push(labels[0]);
    return labels.join(' → ');
  });
}
