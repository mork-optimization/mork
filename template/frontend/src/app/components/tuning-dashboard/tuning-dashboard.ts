import { DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialog } from '@angular/material/dialog';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { ArtifactView, AutoconfigPhase, EliteView } from '../../model/autoconfig';
import { AutoconfigStore } from '../../store/autoconfig-store';
import { PanelPreferences, TUNING_PANELS } from '../../store/panel-preferences';
import { CandidateDialog } from '../candidate-dialog/candidate-dialog';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';
import { EvaluationTable } from '../evaluation-table/evaluation-table';
import { MonitorPanel } from '../monitor-panel/monitor-panel';
import { SearchSpaceDialog } from '../search-space-dialog/search-space-dialog';

@Component({
  selector: 'app-tuning-dashboard',
  imports: [
    DashboardChart,
    DatePipe,
    DecimalPipe,
    EvaluationTable,
    MatBadgeModule,
    MatButtonModule,
    MatCardModule,
    MatChipsModule,
    MatMenuModule,
    MatProgressBarModule,
    MatTabsModule,
    MatToolbarModule,
    MonitorPanel,
  ],
  templateUrl: './tuning-dashboard.html',
  styleUrl: './tuning-dashboard.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TuningDashboard {
  readonly store = inject(AutoconfigStore);
  readonly preferences = inject(PanelPreferences);
  readonly panels = TUNING_PANELS;
  readonly selectedTab = signal(0);
  readonly forceIssues = signal(false);
  private readonly dialog = inject(MatDialog);

  selectTab(index: number): void {
    this.selectedTab.set(index);
    if (index !== 1) {
      this.forceIssues.set(false);
    }
    if (index === 3) {
      void this.store.refreshArtifacts();
    }
  }

  showIssues(): void {
    this.forceIssues.set(true);
    this.selectedTab.set(1);
  }

  inspectCandidate(configurationId: string): void {
    this.dialog.open(CandidateDialog, { data: configurationId, width: '46rem', maxWidth: '94vw' });
  }

  inspectSearchSpace(): void {
    const searchSpace = this.store.searchSpace();
    if (searchSpace) {
      this.dialog.open(SearchSpaceDialog, { data: searchSpace, maxWidth: '96vw' });
    }
  }

  rankClass(position: number): string {
    return position <= 3 ? `rank-${position}` : 'rank-other';
  }

  phaseLabel(phase: AutoconfigPhase): string {
    return phase
      .toLowerCase()
      .split('_')
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  elapsedLabel(elapsedMillis: number | null): string {
    if (elapsedMillis === null) {
      return '—';
    }
    const totalSeconds = Math.floor(elapsedMillis / 1_000);
    const hours = Math.floor(totalSeconds / 3_600);
    const minutes = Math.floor((totalSeconds % 3_600) / 60);
    const seconds = totalSeconds % 60;
    return hours > 0 ? `${hours}h ${minutes}m` : `${minutes}m ${seconds}s`;
  }

  metricLabel(): string {
    const metric = this.store.status()?.metric;
    if (!metric) {
      return 'Waiting for metric details';
    }
    const kind = metric.kind === 'AREA_UNDER_CURVE' ? 'Area under curve' : 'Final objective';
    const direction = metric.negated ? 'maximized, converted to cost' : 'minimized';
    return `${metric.objectiveName} · ${kind} · ${direction}`;
  }

  parameterPreview(elite: EliteView): string {
    const entries = Object.entries(elite.parameters);
    if (entries.length === 0) {
      return 'No parameters';
    }
    return entries
      .slice(0, 3)
      .map(([key, value]) => `${key}=${value}`)
      .join(' · ');
  }

  artifactSize(artifact: ArtifactView): string {
    if (artifact.size < 1_024) {
      return `${artifact.size} B`;
    }
    if (artifact.size < 1_048_576) {
      return `${(artifact.size / 1_024).toFixed(1)} KB`;
    }
    return `${(artifact.size / 1_048_576).toFixed(1)} MB`;
  }
}
