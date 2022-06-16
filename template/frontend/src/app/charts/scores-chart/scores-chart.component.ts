import {AfterViewInit, Component, ElementRef, Input, OnDestroy, ViewChild} from '@angular/core';
import {ChartComponent} from "../base-chart/chart.component";
import {ChartConfig} from "../base-chart/ChartConfig";
import {ScoresChartConfig} from "./ScoresChartConfig";

@Component({
  selector: 'app-scores-chart',
  templateUrl: './scores-chart.component.html',
  styleUrls: [
    './scores-chart.component.css'
  ]
})
export class ScoresChartComponent {
  private _config!: ChartConfig;

  @ViewChild('chart')
  _chart!: ChartComponent;

  constructor() {
  }

  @Input()
  set config(value: ScoresChartConfig) {
    this._config = {
      id: 'chart-current-' + value.instance_name,
      options: {
        chart: {
          zoomType: 'x'
        },
        title: {
          text: 'Actual value chart'
        },
        subtitle: {
          text: 'Instance: ' + value.instance_name
        },
        tooltip: {
          valueDecimals: 2
        },
        xAxis: {
          type: 'linear',
          title: {text: 'Iteration'}
        },
        yAxis: {
          type: 'linear',
          title: {text: 'Score'}
        },
        legend: {
          maxHeight: 60
        },
        credits: {
          enabled: false
        }
      }, callback: (chart) => chart.yAxis[0].addPlotLine({
        // Add reference value plot line
        // Style of the plot line. Default to solid. See https://jsfiddle.net/gh/get/library/pure/highcharts/highcharts/tree/master/samples/highcharts/plotoptions/series-dashstyle-all/
        color: 'red',
        dashStyle: 'LongDash',
        value: value.reference_value,
        width: 2,
        label: {
          align: 'left',
          y: 16
        }
      })
    };
  }

  set internalConfig(value: ChartConfig) {
    this._config = value;
  }

  get internalConfig(): ChartConfig {
    return this._config;
  }

  get chart(){
    return this._chart.chart;
  }

}
