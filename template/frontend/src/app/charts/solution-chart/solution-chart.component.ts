import {AfterViewInit, Component, ElementRef, Input, OnDestroy, ViewChild} from '@angular/core';
import {ChartComponent} from "../base-chart/chart.component";
import {SolutionGeneratedEvent} from "../../model/Events";
import {ChartConfig} from "../base-chart/ChartConfig";
import {Point, Series} from "highcharts";
import {ChartUtil} from "../base-chart/util";
import {SolutionChartConfig} from "./SolutionChartConfig";

@Component({
  selector: 'app-solution-chart',
  templateUrl: './solution-chart.component.html',
  styleUrls: [
    './solution-chart.component.css'
  ]
})
export class SolutionChartComponent {

  current_solution_chart_data!: Series;
  private _config!: ChartConfig;

  @ViewChild('chart')
  _chart!: ChartComponent;

  constructor() {
  }

  @Input()
  set config(value: SolutionChartConfig){
    this._config = {
      id: 'best-solution-' + value.instance_name,
      options: {
        chart: {
          type: 'xrange',
          zoomType: 'x'
        },
        title: {
          text: 'Best solution'
        },
        subtitle: {
          text: 'For Instance: ' + value.instance_name
        },
        tooltip: {
          pointFormatter: function(this: Point): string {
            const point: any = this;
            const ix = point.index + 1,
              category = point.yCategory,
              from = point.x,
              to = point.x2;
            return `ID: ${point.fid < 0 ? "FAKE" : point.fid}, Width: ${to - from}`;
          }
        },
        xAxis: {
          type: 'linear'
        },
        yAxis: {
          type: 'category',
          title: {
            text: 'Rows'
          },
          //categories: ['Row 0', 'Row 1', 'Row 2'],
          reversed: true
        },
        legend: {
          maxHeight: 60
        },
        series: [],
        credits: {
          enabled: false
        }
      },
      callback: (chart) => this.current_solution_chart_data = chart.addSeries({
        type: 'xrange',
        name: 'Best Solution',
        // pointPadding: 0,
        // groupPadding: 0,
        borderColor: 'gray',
        pointWidth: 20,
        data: [],
        dataLabels: {
          enabled: true
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

  renderSolution(event: SolutionGeneratedEvent){
    // const solution = event.solution;
    // const newData = [];
    // for (let i = 0; i < event.solution!.rows.length; i++) {
    //   const row = event.solution!.rows[i];
    //   const rowSize = event.solution!.rowSizes[i];
    //   let acc = 0;
    //   for (let j = 0; j < rowSize; j++) {
    //     const start = acc;
    //     const f = row[j].facility;
    //     acc += f.width;
    //     const end = acc;
    //     const fid = f.id;
    //     newData.push({
    //       x: start,       // Start pos
    //       x2: end,        // end pos
    //       y: i,           // Row
    //       fid: fid,       // K-PROP Facility id
    //       color: f.fake ? "rgba(255,255,255,0)" : ChartUtil.getRandomColor(fid)     // Color generated from id
    //     })
    //   }
    // }
    //
    // this.current_solution_chart_data.setData(newData);
  }

  get chart(){
    return this._chart?.chart;
  }
}
