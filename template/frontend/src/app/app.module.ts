import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';

import { HighchartsChartModule } from 'highcharts-angular';

import { ChartComponent } from './charts/base-chart/chart.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import {RxStompService} from "./service/rx-stomp.service";
import {rxStompServiceFactory} from "./service/rx-stomp-service-factory";
import {HttpClientModule} from "@angular/common/http";
import { SolutionChartComponent } from './charts/solution-chart/solution-chart.component';
import { ConvergenceChartComponent } from './charts/convergence-chart/convergence-chart.component';
import { ScoresChartComponent } from './charts/scores-chart/scores-chart.component';

@NgModule({
  declarations: [
    AppComponent,
    ChartComponent,
    SolutionChartComponent,
    ConvergenceChartComponent,
    ScoresChartComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    AppRoutingModule,
    HighchartsChartModule,
    NgbModule
  ],
  providers: [
    {
      provide: RxStompService,
      useFactory: rxStompServiceFactory,
    },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
