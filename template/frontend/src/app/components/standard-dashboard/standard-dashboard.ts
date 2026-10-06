import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { DatePipe, DecimalPipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { EventClient } from '../../service/event-client';
import { ExperimentClient } from '../../service/experiment-client';
import { DashboardStore } from '../../store/dashboard-store';
import { PanelPreferences, STANDARD_PANELS } from '../../store/panel-preferences';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';
import { MonitorPanel } from '../monitor-panel/monitor-panel';
import { SolutionRenderer } from '../solution-renderer/solution-renderer';
import { WorkState } from '../../model/normal-experiment';

@Component({
  selector: 'app-standard-dashboard',
  imports: [
    DatePipe,
    DecimalPipe,
    DashboardChart,
    MonitorPanel,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatMenuModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatSelectModule,
    MatTabsModule,
    MatToolbarModule,
    SolutionRenderer,
  ],
  providers: [DashboardStore, EventClient, ExperimentClient],
  templateUrl: './standard-dashboard.html',
  styleUrl: './standard-dashboard.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StandardDashboard implements OnInit {
  readonly store = inject(DashboardStore);
  readonly preferences = inject(PanelPreferences);
  readonly panels = STANDARD_PANELS;
  readonly workStates: readonly (WorkState | '')[] = [
    '',
    'PENDING',
    'RUNNING',
    'SUCCEEDED',
    'FAILED',
    'CANCELLED',
  ];
  ngOnInit(): void {
    this.store.start();
  }
  progressValue(completed: number, total: number): number {
    return total > 0 ? Math.min(100, (completed / total) * 100) : 0;
  }
  elapsedLabel(millis: number): string {
    const seconds = Math.floor(millis / 1000);
    return seconds >= 3600
      ? Math.floor(seconds / 3600) + 'h ' + Math.floor((seconds % 3600) / 60) + 'm'
      : Math.floor(seconds / 60) + 'm ' + (seconds % 60) + 's';
  }
  phaseLabel(value: string): string {
    return value.toLowerCase().replaceAll('_', ' ');
  }
}
