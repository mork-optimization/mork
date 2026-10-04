import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatToolbarModule } from '@angular/material/toolbar';
import { EventClient } from '../../service/event-client';
import { DashboardStore } from '../../store/dashboard-store';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';
import { SolutionRenderer } from '../solution-renderer/solution-renderer';

@Component({
  selector: 'app-standard-dashboard',
  imports: [
    DashboardChart,
    MatCardModule,
    MatChipsModule,
    MatFormFieldModule,
    MatProgressBarModule,
    MatSelectModule,
    MatToolbarModule,
    SolutionRenderer,
  ],
  providers: [DashboardStore, EventClient],
  templateUrl: './standard-dashboard.html',
  styleUrl: './standard-dashboard.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class StandardDashboard implements OnInit {
  readonly store = inject(DashboardStore);

  ngOnInit(): void {
    this.store.start();
  }

  selectObjective(name: string): void {
    this.store.selectObjective(name);
  }

  progressValue(completed: number, total: number): number {
    return total > 0 ? Math.min(100, (completed / total) * 100) : 0;
  }
}
