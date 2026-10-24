import { ChangeDetectionStrategy, Component, effect, input } from '@angular/core';
import { HighchartsChartComponent } from 'highcharts-angular';
import type Highcharts from 'highcharts/esm/highcharts';
import { ChartViewModel } from '../../model/dashboard';

@Component({
  selector: 'app-dashboard-chart',
  host: { '[class.resizable]': 'resizable()' },
  imports: [HighchartsChartComponent],
  template: `
    <highcharts-chart
      class="chart highcharts-light"
      [options]="model().options"
      [oneToOne]="true"
      (chartInstance)="chartCreated($event)"
    />
  `,
  styles: `
    :host,
    .chart {
      display: block;
      width: 100%;
      min-width: 0;
    }
    :host(.resizable) {
      height: 32.5rem;
      min-height: 24rem;
      max-height: 75rem;
      resize: vertical;
      overflow: hidden;
      padding-bottom: .5rem;
      box-sizing: border-box;
    }
    :host(.resizable) .chart { height: 100%; }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardChart {
  readonly model = input.required<ChartViewModel>();
  readonly resizable = input(false);
  private chart?: Highcharts.Chart;
  private viewKey?: string;

  constructor() {
    effect(() => {
      const viewKey = this.model().viewKey;
      if (this.chart && viewKey !== this.viewKey) {
        this.viewKey = viewKey;
        this.chart.zoomOut();
      }
    });
  }

  chartCreated(chart: Highcharts.Chart): void {
    this.chart = chart;
    this.viewKey = this.model().viewKey;
  }
}
