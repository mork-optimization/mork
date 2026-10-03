import { ChangeDetectionStrategy, Component, input } from '@angular/core';
import { HighchartsChartComponent } from 'highcharts-angular';
import { ChartViewModel } from '../../model/dashboard';

@Component({
  selector: 'app-dashboard-chart',
  imports: [HighchartsChartComponent],
  template: ` <highcharts-chart class="chart" [options]="model().options" [oneToOne]="true" /> `,
  styles: `
    :host,
    .chart {
      display: block;
      width: 100%;
      min-width: 0;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class DashboardChart {
  readonly model = input.required<ChartViewModel>();
}
