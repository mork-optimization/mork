import {AfterViewInit, Component, Input, OnDestroy, OnInit} from '@angular/core';
import * as Highcharts from 'highcharts';

import HighchartsMore from 'highcharts/highcharts-more';
HighchartsMore(Highcharts);

// Load Highcharts modules
import HC_xrange from 'highcharts/modules/xrange';
HC_xrange(Highcharts);
import HC_SolidGauge from 'highcharts/modules/solid-gauge';
HC_SolidGauge(Highcharts);
import HC_Exporting from 'highcharts/modules/exporting';
HC_Exporting(Highcharts);
import HC_offlineExporting from 'highcharts/modules/offline-exporting';
HC_offlineExporting(Highcharts);
import HC_boost from 'highcharts/modules/boost';
import {Chart, ChartCallbackFunction, Options} from "highcharts";
import {ChartConfig} from "./ChartConfig";
HC_boost(Highcharts);

@Component({
  selector: 'app-chart',
  templateUrl: './chart.component.html',
  styleUrls: ['./chart.component.css']
})
export class ChartComponent implements AfterViewInit, OnDestroy {

  private _config!: ChartConfig;
  private _chart!: Chart;

  constructor() { }

  ngAfterViewInit(): void {
    this._chart = new Highcharts.Chart(this._config.id, this._config.options, this._config.callback);
  }

  ngOnDestroy(): void {
    this._chart.destroy();
  }


  get chart(): Chart {
    return this._chart;
  }

  @Input()
  set config(value: ChartConfig) {
    this._config = value;
  }

  get config(): ChartConfig {
    return this._config;
  }
}
