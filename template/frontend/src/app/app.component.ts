import {Component, QueryList, ViewChildren} from '@angular/core';
import {RxStompService} from "./service/rx-stomp.service";
import {Message} from "@stomp/stompjs";
import {MorkEvent, ExecutionStartedEvent, ExecutionEndedEvent, ExperimentStartedEvent, ExperimentEndedEvent, InstanceProcessingStartedEvent, InstanceProcessingEndedEvent, AlgorithmProcessingStartedEvent, AlgorithmProcessingEndedEvent, SolutionGeneratedEvent, EventType} from "./model/Events";
import {HttpClient} from "@angular/common/http";

import * as Highcharts from 'highcharts';

import {Chart, Point} from "highcharts";
import {environment} from "../environments/environment";
import {ChartConfig} from "./charts/base-chart/ChartConfig";
import {ConvergenceChartComponent} from "./charts/convergence-chart/convergence-chart.component";
import {SolutionChartComponent} from "./charts/solution-chart/solution-chart.component";
import {ScoresChartComponent} from "./charts/scores-chart/scores-chart.component";
import {ConvergenceChartConfig} from "./charts/convergence-chart/ConvergenceChartConfig";
import {ScoresChartConfig} from "./charts/scores-chart/ScoresChartConfig";
import {SolutionChartConfig} from "./charts/solution-chart/SolutionChartConfig";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'frontend';
  status = "Connecting...";
  experimentName = "";
  eventCount: number = 0;


  /**
   * Max number of charts rendered. Oldest charts get removed when limit is reached.
   */
  static readonly max_charts = 10;

  /**
   * Redraw cooldown in milliseconds.
   * Example: If 100 changes are done to a chart in a second,
   * and cooldown is 500, charts are only redrawn twice instead of 100 times.
   */
  static readonly redraw_cooldown = 2000;

  /**
   * Event batch download size
   */
  static readonly event_batch_size = 1000;


//// Start Chart data
  private convergence_chart_series!: any;
  private minimum_algorithm: any = {};

  private current_chart_series!: any;

  private bestValue: number = NaN;
  private alreadyConnected = false;
  private maximizing!: boolean;
  private last_redraw = new Date().valueOf() - 1000;
  private lastEventId = -1;

  private progress_chart!: Chart;
  private nInstances!: number;
  private currentInstances!: number;
  private nAlgorithms!: number;
  private currentAlgorithms!: number;
  private nRepetitions!: number;
  private currentRepetitions!: number;

  // Angular migration specific
  chartConfigs: {convergence: ConvergenceChartConfig, scores: ScoresChartConfig, best: SolutionChartConfig}[] = [];
  @ViewChildren(ConvergenceChartComponent)
  convergenceCharts!: QueryList<ConvergenceChartComponent>

  @ViewChildren(ScoresChartComponent)
  chartCurrents!: QueryList<ScoresChartComponent>

  @ViewChildren(SolutionChartComponent)
  bestSolutions!: QueryList<SolutionChartComponent>

//// End chart data

  //// SYNC data
  private event_queue:MorkEvent[] = [];
  private downloaded_events:MorkEvent[] = [];
  private isUpToDate:boolean = false;
  //// END SYNC data

  constructor(private rxStompService: RxStompService, private http: HttpClient) {}

  ngOnInit() {
    this.rxStompService.watch('/topic/events').subscribe((message) => this.eventHandler(message, this));
    this.rxStompService.connected$.subscribe(() => {
      if(this.alreadyConnected){
        console.log("Backend probably restarted, reloading...");
        window.location.reload();
      } else {
        console.log("First STOMP connection");
        this.alreadyConnected = true;
        // Download event data
        this.http.get<MorkEvent>(environment.APIPath + "lastevent").subscribe((event) => {
          console.log("Recieved first event with id: " + event.eventId);
          this.downloadOldEventData(0, event.eventId + 1); // [0, eventId]
        });
      }
    });
  }

  downloadOldEventData(from: number, to: number){
    this.status = 'RUNNING, SYNCING';
    const limit = Math.min(to, from + AppComponent.event_batch_size);
    this.http.get<MorkEvent[]>(environment.APIPath + `events?from=${from}&to=${limit}`).subscribe(events => {
      console.log(`API /events returned  [${from}, ${limit}): ` + events.length);
      this.downloaded_events = this.downloaded_events.concat(events);
      if (events.length < AppComponent.event_batch_size) {
        console.log(this.event_queue);
        this.event_queue = this.downloaded_events.concat(this.event_queue);
        console.log(this.event_queue);
        this.downloaded_events = [];
        const catchUp = (i:number) => {
          if (i === this.event_queue.length) {
            // force redraw
            // Each time an instance finishes executing redraw its charts one last time and delete oldest ones
            this.convergenceCharts.get(0)?.chart.redraw();
            this.chartCurrents.get(0)?.chart.redraw();
            this.bestSolutions.get(0)?.chart?.redraw();

            this.event_queue = [];
            this.isUpToDate = true;
            this.status = 'RUNNING, REAL TIME';
            console.log("Up to date!");
          } else {
            this.status = 'RUNNING, REPLAYING';
            this.onEvent(this.event_queue[i]);
            setTimeout(() => catchUp(i + 1), 0);
          }
        };
        setTimeout(() => catchUp(0), 0);
      } else {
        setTimeout(() => this.downloadOldEventData(from + AppComponent.event_batch_size, to), 0);
      }
    });
  }

  eventHandler(message: Message, that: AppComponent){
    const payload = JSON.parse(message.body) as MorkEvent;
    if(that.isUpToDate){
      this.onEvent(payload);
    } else {
      that.event_queue.push(payload);
    }
  }

  onEvent(event: MorkEvent) {
    if(environment.eventDebug) console.log(`onEvent ${event.eventId}:${event.type}`);
    // Dispatch to event handlers
    if(event.eventId <= this.lastEventId){
      console.log("Skipping event handlers for the following event, already processed or out of order");
      console.log(event);
      return;
    }
    this.lastEventId++;
    if(event.eventId !== this.lastEventId){
      console.log(`Wrong event order, expected ${this.lastEventId}, for id ${event.eventId}`)
    }
    switch (event.type) {
      case EventType.ExecutionStartedEvent:
        this.onExecutionStart(event as ExecutionStartedEvent);
        break;

      case EventType.ExecutionEndedEvent:
        this.onExecutionEnd(event as ExecutionEndedEvent);
        break;

      case EventType.ExperimentStartedEvent:
        this.onExperimentStart(event as ExperimentStartedEvent);
        break;

      case EventType.ExperimentEndedEvent:
        this.onExperimentEnd(event as ExperimentEndedEvent);
        break;

      case EventType.InstanceProcessingStartedEvent:
        this.onInstanceProcessingStart(event as InstanceProcessingStartedEvent);
        break;

      case EventType.InstanceProcessingEndedEvent:
        this.onInstanceProcessingEnd(event as InstanceProcessingEndedEvent);
        break;

      case EventType.SolutionGeneratedEvent:
        this.onSolutionGenerated(event as SolutionGeneratedEvent);
        break;
    }
    this.onAnyEvent(event);
  }
  onExecutionStart(event: ExecutionStartedEvent) {
    this.createProgressChart();
    this.maximizing = event.maximizing;
  }

  onExecutionEnd(event: ExecutionEndedEvent) {
    this.status = 'FINISHED';
  }

  onExperimentStart(event: ExperimentStartedEvent) {
    this.nInstances = event.instanceNames.length;
    this.experimentName = event.experimentName;
    this.currentInstances = 0;
  }


  onExperimentEnd(event: ExperimentEndedEvent) {
    this.experimentName = '[Waiting]';
  }

  onInstanceProcessingStart(event: InstanceProcessingStartedEvent) {
    this.currentAlgorithms = 0;
    this.nAlgorithms = event.algorithms.length;
    this.currentRepetitions = 0;
    this.nRepetitions = event.repetitions;
    this.updateStatusChart();

    const instanceName = event.instanceName.replace(/\..+/g, "");

    // Charts configuration
    this.chartConfigs.unshift({
      convergence: {
        instance_name: event.instanceName,
        reference_value:event.referenceValue
      },
      scores: {
        instance_name: event.instanceName,
        reference_value: event.referenceValue
      },
      best: {instance_name: instanceName}
    });

    this.convergence_chart_series = {};
    this.minimum_algorithm = {};
    this.current_chart_series = {};
    // old first argument: 'chart-current-' + instanceName,

    // Draw best solution found
    this.bestValue = NaN;

    // END Define how the current solution should be drawn.


  }

  onInstanceProcessingEnd(event: InstanceProcessingEndedEvent) {
    // Each time an instance finishes executing redraw its charts one last time and delete oldest ones
    this.convergenceCharts.get(0)?.chart.redraw();
    this.chartCurrents.get(0)?.chart.redraw();
    this.bestSolutions.get(0)?.chart?.redraw();

    this.removeExtraCharts(this.chartConfigs);

    this.currentInstances++;
    this.currentAlgorithms = this.nAlgorithms;
    this.currentRepetitions = this.nRepetitions;
    this.updateStatusChart(true);

    this.convergence_chart_series = {}
    this.minimum_algorithm = {}
  }

  onSolutionGenerated(event: SolutionGeneratedEvent) {
    if (this.currentRepetitions >= this.nRepetitions) {
      this.currentRepetitions = 0;
      this.currentAlgorithms++;
    }
    this.currentRepetitions++;
    this.updateStatusChart();

    // if (!this.convergence_chart || !this.current_chart || !this.current_solution_chart) {
    //   console.log("Skipping onSolutionGenerated due to missing charts, probably a bug!!");
    //   return;
    // }

    const currentTime = new Date();
    const ellapsedTime = currentTime.valueOf() - this.last_redraw.valueOf();
    let redraw = false;
    if (ellapsedTime > AppComponent.redraw_cooldown) {
      this.last_redraw = currentTime.valueOf();
      redraw = true;
      if(environment.eventDebug) console.log("Redrawing: " + ellapsedTime + ", eventId: " + event.eventId);
    } else {
      if(environment.eventDebug) console.log("Skipping redraw: " + ellapsedTime + ", eventId: " + event.eventId);
    }

    if (!this.convergence_chart_series.hasOwnProperty(event.algorithmName)) {
      this.convergence_chart_series[event.algorithmName] = this.convergenceCharts.get(0)?.chart.addSeries({ // TODO get(0)
        name: event.algorithmName,
        type: 'line',
        data: [],
        lineWidth: 1,
        animation: false
      });
      this.minimum_algorithm[event.algorithmName] = event.score;
    }
    this.minimum_algorithm[event.algorithmName] = Math.min(this.minimum_algorithm[event.algorithmName], event.score);
    // BUGFIX: Reorder iterations for convergence chart as they arrive
    this.convergence_chart_series[event.algorithmName].addPoint([this.currentRepetitions, this.minimum_algorithm[event.algorithmName]], redraw)


    // Update current value graph
    // if (!this.current_chart) {
    //   //console.log("Skipping due to missing char");
    //   return;
    // }
    if (!this.current_chart_series.hasOwnProperty(event.algorithmName)) {
      this.current_chart_series[event.algorithmName] = this.chartCurrents.get(0)?.chart.addSeries({  // TODO get(0)
        name: event.algorithmName,
        type: 'line',
        data: [],
        lineWidth: 1,
        animation: false
      });
    }
    this.current_chart_series[event.algorithmName].addPoint([event.iteration, event.score], redraw);

    if (isNaN(this.bestValue) || this.isBetter(event.score, this.bestValue, this.maximizing)) {
      this.bestValue = event.score;
      this.bestSolutions.get(0)?.renderSolution(event);
    }
  }

  isBetter(current: number, best: number, maximizing: boolean){
    if(maximizing){
      return current > best;
    } else {
      return current < best;
    }
  }

  onAnyEvent(event: MorkEvent) {
    // Update last event id
    this.eventCount = event.eventId;
  }

  createProgressChart() {
    if (this.progress_chart) {
      this.progress_chart.destroy();
    }
    // @ts-ignore
    this.progress_chart = Highcharts.chart('progress-chart',{
      chart: {
        height: '110%'
      },
      title: {
        text: '',
        style: {
          fontSize: '12px'
        }
      },

      exporting: {
        enabled: false
      },

      credits: {
        enabled: false
      },
      tooltip: {
        borderWidth: 0,
        backgroundColor: 'none',
        shadow: false,
        style: {
          fontSize: '10px'
        },
        valueSuffix: '%',
        pointFormat: '{series.name}<br><span style="font-size:2em; color: {point.color}; font-weight: bold">{point.y}</span>',
        positioner: function (labelWidth) {
          return {
            x: (this.chart.chartWidth - labelWidth) / 2,
            y: (this.chart.plotHeight / 2) - 15
          };
        }
      },

      pane: {
        startAngle: 0,
        endAngle: 360,
        background: [{ // Track for Iterations
          outerRadius: '112%',
          innerRadius: '88%',
          backgroundColor: Highcharts.color(Highcharts.getOptions().colors![0])
            .setOpacity(0.3)
            .get(),
          borderWidth: 0
        }, { // Track for Algorithms
          outerRadius: '87%',
          innerRadius: '63%',
          backgroundColor: Highcharts.color(Highcharts.getOptions().colors![1])
            .setOpacity(0.3)
            .get(),
          borderWidth: 0
        }, { // Track for Instances
          outerRadius: '62%',
          innerRadius: '38%',
          backgroundColor: Highcharts.color(Highcharts.getOptions().colors![2])
            .setOpacity(0.3)
            .get(),
          borderWidth: 0
        }]
      },

      yAxis: {
        min: 0,
        max: 100,
        lineWidth: 0,
        tickPositions: []
      },

      plotOptions: {
        solidgauge: {
          dataLabels: {
            enabled: false
          },
          linecap: 'round',
          stickyTracking: false,
          rounded: true
        }
      },

      series: [{
        type: 'solidgauge',
        name: 'Iteration',
        data: [{
          id: 'Iteration',
          color: Highcharts.getOptions().colors![0],
          radius: '112%',
          innerRadius: '88%',
          y: 80
        }]
      }, {
        type: 'solidgauge',
        name: 'Algorithm',
        data: [{
          id: 'Algorithm',
          color: Highcharts.getOptions().colors![1],
          radius: '87%',
          innerRadius: '63%',
          y: 65
        }]
      }, {
        type: 'solidgauge',
        name: 'Instance',
        data: [{
          id: 'Instance',
          color: Highcharts.getOptions().colors![2],
          radius: '62%',
          innerRadius: '38%',
          y: 50
        }]
      }]
    }, undefined);
  }

  updateStatusChart(force = false) {
    const ellapsedTime = new Date().valueOf() - this.last_redraw.valueOf();
    let redraw = force || ellapsedTime > AppComponent.redraw_cooldown;
    const iteration = this.progress_chart.get('Iteration'),
      algorithm = this.progress_chart.get('Algorithm'),
      instance = this.progress_chart.get('Instance');


    // TODO: WTF why ts-ignore
    // @ts-ignore
    iteration!.update(Math.round(this.currentRepetitions / this.nRepetitions * 100), redraw);
    // @ts-ignore
    algorithm!.update(Math.round(this.currentAlgorithms / this.nAlgorithms * 100), redraw);
    // @ts-ignore
    instance!.update(Math.round(this.currentInstances / this.nInstances * 100), redraw);
  }

  // removeExtraCharts(selector) {
  //   $(selector).children().slice(max_charts - 1).each(function () {
  //     const c = $(this);
  //     try {
  //       console.log("Deleting chart:");
  //       console.log(c);
  //       c.highcharts().destroy();
  //       c.remove();
  //     } catch (e) {
  //       console.log(e);
  //     }
  //   });
  // }

  removeExtraCharts(data: any[]) {
    for (let i = 0; i < (data.length - AppComponent.max_charts - 1); i++) {
      data.pop();
    }
  }
}



