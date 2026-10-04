import { provideZonelessChangeDetection, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { provideHighcharts } from 'highcharts-angular';
import { AutoconfigStatus, EliteSnapshot, TuningAlert } from '../../model/autoconfig';
import { ChartViewModel } from '../../model/dashboard';
import { AutoconfigStore } from '../../store/autoconfig-store';
import { PanelPreferences } from '../../store/panel-preferences';
import { TuningDashboard } from './tuning-dashboard';

const emptyChart: ChartViewModel = { options: { series: [] } };
const runningStatus: AutoconfigStatus = {
  runId: 'run-1',
  mode: 'AUTOCONFIG',
  role: 'COORDINATOR',
  state: 'RUNNING',
  phase: 'RACING',
  preparedAt: '2026-10-04T00:00:00Z',
  startedAt: '2026-10-04T00:00:01Z',
  finishedAt: null,
  elapsedMillis: 65_000,
  budget: { maximum: 100, used: 25, remaining: 75 },
  evaluations: { running: 2, succeeded: 20, rejected: 2, failed: 1, slow: 3 },
  latestEvaluationRevision: 50,
  generatedParameterCount: 8,
  trainingInstanceCount: 12,
  metric: {
    objectiveName: 'distance',
    objectiveMode: 'MINIMIZE',
    kind: 'AREA_UNDER_CURVE',
    costMode: 'MINIMIZE',
    negated: false,
    auc: { ignoreInitialMillis: 1_000, intervalDurationMillis: 5_000, logScale: false },
  },
  irace: {
    iteration: 2,
    eliteCount: 4,
    updatedAt: '2026-10-04T00:01:00Z',
    finalSnapshot: false,
    progress: null,
  },
  failure: null,
};

class FakeAutoconfigStore {
  readonly status = signal<AutoconfigStatus | null>(runningStatus);
  readonly syncState = signal<'live'>('live');
  readonly connectionLabel = signal('Live');
  readonly latestError = signal<string | null>(null);
  readonly budgetPercent = signal(25);
  readonly lastUpdated = signal(new Date('2026-10-04T00:01:00Z'));
  readonly evaluationCostChart = signal(emptyChart);
  readonly evaluationActivityChart = signal(emptyChart);
  readonly eliteRankChart = signal(emptyChart);
  readonly evaluations = signal([]);
  readonly instanceSummaries = signal([]);
  readonly searchSpace = signal(null);
  readonly eliteHistory = signal(null);
  readonly artifacts = signal(null);
  readonly alerts = signal<readonly TuningAlert[]>([
    {
      key: 'evaluation:3',
      revision: 3,
      severity: 'warning',
      title: 'Evaluation rejected',
      message: 'Timeout',
      timestamp: '2026-10-04T00:00:30Z',
      evaluationId: 3,
    },
  ]);
  readonly unseenAlertCount = signal(1);
  readonly elites = signal<EliteSnapshot | null>({
    runId: 'run-1',
    iteration: 2,
    updatedAt: '2026-10-04T00:01:00Z',
    finalSnapshot: false,
    elites: [
      { configurationId: '7', position: 1, parameters: { alpha: '1' }, algorithm: null },
      { configurationId: '8', position: 2, parameters: { alpha: '2' }, algorithm: null },
      { configurationId: '9', position: 3, parameters: { alpha: '3' }, algorithm: null },
      { configurationId: '10', position: 4, parameters: { alpha: '4' }, algorithm: null },
    ],
  });
  readonly markAlertsSeen = vi.fn();
  readonly retryNow = vi.fn();
  readonly refreshArtifacts = vi.fn(async () => undefined);
  readonly artifactUrl = vi.fn((id: string) => `/api/autoconfig/artifacts/${id}`);
}

describe('TuningDashboard', () => {
  beforeEach(async () => {
    localStorage.clear();
    await TestBed.configureTestingModule({
      imports: [TuningDashboard],
      providers: [
        provideZonelessChangeDetection(),
        provideHighcharts({
          instance: () => import('highcharts/esm/highcharts').then((module) => module.default),
        }),
        PanelPreferences,
        { provide: AutoconfigStore, useClass: FakeAutoconfigStore },
        { provide: MatDialog, useValue: { open: vi.fn() } },
      ],
    }).compileComponents();
  });

  afterEach(() => TestBed.resetTestingModule());

  it('keeps the actual budget visible and uses distinct top-three elite medals', async () => {
    const fixture = TestBed.createComponent(TuningDashboard);
    fixture.detectChanges();
    await fixture.whenStable();

    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('25 / 100');
    expect(text).toContain('75 remaining');
    expect(text).toContain('Training instances');
    expect(text).not.toContain('Planned iterations');
    expect(fixture.nativeElement.querySelectorAll('.rank-1')).toHaveLength(1);
    expect(fixture.nativeElement.querySelectorAll('.rank-2')).toHaveLength(1);
    expect(fixture.nativeElement.querySelectorAll('.rank-3')).toHaveLength(1);
    expect(fixture.nativeElement.querySelectorAll('.rank-other')).toHaveLength(1);
  });

  it('opens the evaluation issue view from alerts', () => {
    const fixture = TestBed.createComponent(TuningDashboard);
    const component = fixture.componentInstance;

    component.showIssues();

    expect(component.selectedTab()).toBe(1);
    expect(component.forceIssues()).toBe(true);
  });
});
