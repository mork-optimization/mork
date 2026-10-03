import {Component, Input} from '@angular/core';
import {SolutionGeneratedEvent} from "../../model/Events";
import {SolutionChartConfig} from "./SolutionChartConfig";

@Component({
  selector: 'app-solution-chart',
  templateUrl: './solution-chart.component.html',
  styleUrls: [
    './solution-chart.component.css'
  ]
})
export class SolutionChartComponent {
  @Input()
  config!: SolutionChartConfig;

  renderSolution(_event: SolutionGeneratedEvent) {
    // Application templates can implement problem-specific solution rendering here.
  }
}
