import { JsonPipe, DatePipe, DecimalPipe, NgTemplateOutlet } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  signal,
  afterNextRender,
  Injector,
  DestroyRef,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { FormsModule } from '@angular/forms';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatStepperModule } from '@angular/material/stepper';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatMenuModule } from '@angular/material/menu';
import { MonitorPanel } from '../monitor-panel/monitor-panel';
import { PanelPreferences, AUTOLLM_PANELS } from '../../store/panel-preferences';
import { AutoLlmStore } from '../../store/autollm-store';
import { AutoLlmConfig, AutoLlmTrial, DatasetSelection } from '../../model/autollm';
import { AutoLlmDatasetEditor } from '../autollm-editors/dataset-editor';
import { AutoLlmServerEditor } from '../autollm-editors/server-editor';
import { AutoLlmTimeline } from '../autollm-timeline/autollm-timeline';
import {
  AttemptSelection,
  TrialStage,
  timingDuration,
  trialStages,
  trialTiming,
  stageOf,
} from '../../util/autollm-timing';
import {
  autoLlmLocation,
  readAutoLlmLocation,
  defaultTimelineSettings,
  matchesTrialFilter,
  TimelineSettings,
  TrialFilter,
} from '../../util/autollm-navigation';
import { trialNotice } from '../../util/autollm-notice';
import { AutoLlmTrialNotice } from '../autollm-trial-notice/autollm-trial-notice';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';
import { ChartViewModel } from '../../model/dashboard';
import { ComparisonChartUtil, ComparisonYAxisScale } from '../../util/comparison-chart-util';
import { InstanceLabelUtil } from '../../util/instance-label-util';
import { FilePreviewUtil } from '../../util/file-preview-util';

@Component({
  selector: 'app-autollm-dashboard',
  imports: [
    AutoLlmTrialNotice,
    AutoLlmTimeline,
    AutoLlmDatasetEditor,
    AutoLlmServerEditor,
    JsonPipe,
    DatePipe,
    DecimalPipe,
    NgTemplateOutlet,
    MatButtonModule,
    MatCardModule,
    MatProgressBarModule,
    DashboardChart,
    MatToolbarModule,
    MatChipsModule,
    MatTabsModule,
    MatInputModule,
    MatSelectModule,
    MatFormFieldModule,
    MatStepperModule,
    MatPaginatorModule,
    MatMenuModule,
    MonitorPanel,
    FormsModule,
  ],
  templateUrl: './autollm-dashboard.html',
  styleUrl: './autollm-dashboard.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AutoLlmDashboard {
  private readonly injector = inject(Injector);
  private navigationReady = false;
  private restoringLocation = false;
  private campaignScrollY: number | null = null;
  readonly timelineSettings = signal(defaultTimelineSettings());
  readonly selectedTarget = signal('');
  readonly decisionSearch = signal('');
  readonly decisionPage = signal(0);
  readonly resultInstanceSearch = signal('');
  readonly resultInstancePage = signal(0);
  readonly preferences = inject(PanelPreferences);
  readonly panels = AUTOLLM_PANELS;
  readonly tab = signal(0);
  readonly detailTab = signal(0);
  readonly settingsOpen = signal(false);
  readonly trialSearch = signal('');
  readonly trialFilter = signal('all');
  readonly trialSort = signal('number');
  readonly trialPage = signal(0);
  readonly artifactSearch = signal('');
  readonly artifactType = signal('all');
  readonly artifactTrial = signal('');
  readonly artifactPage = signal(0);
  readonly datasetSearch = signal('');
  readonly timingDuration = timingDuration;
  readonly selectedStage = signal<TrialStage | null>(null);
  readonly selectedAttempt = signal<number | null>(null);
  readonly timingNow = computed(() => this.store.lastUpdated() ?? this.store.now());
  readonly stageSummaries = computed(() =>
    this.store.trial() ? trialStages(this.store.trial()!, this.timingNow()) : [],
  );
  readonly inspectedStage = computed(() =>
    this.stageSummaries().find((stage) => stage.id === this.selectedStage()),
  );
  readonly attemptTimings = computed(() =>
    this.store.trial() ? trialTiming(this.store.trial()!, this.timingNow()).intervals : [],
  );
  readonly canPreviewFile = FilePreviewUtil.canPreview;
  readonly store = inject(AutoLlmStore);
  readonly notice = computed(() =>
    this.store.trial()
      ? trialNotice(
          this.store.trial()!,
          this.store.active() === this.store.selectedId(),
          this.timingNow(),
          !!this.store.connectionError(),
        )
      : null,
  );
  readonly amendedTrials = signal(10);
  readonly datasetDirty = signal(false);
  readonly failureReason = signal('');
  readonly selectedObjective = signal('');
  readonly yAxisScale = signal<ComparisonYAxisScale>('linear');
  readonly chartHeight = signal(520);
  readonly executionChoice = signal('');
  readonly originalPool = signal('');
  readonly trainingFraction = signal(0.15);
  readonly instances = this.store.comparisonInstances;
  readonly instanceLabels = computed(() =>
    InstanceLabelUtil.relativeLabels(this.instances().map((instance) => instance.instance)),
  );
  readonly activeInstance = computed(() =>
    this.instances().find((instance) => instance.instanceId === this.store.curveInstance()),
  );
  readonly curveCounts = computed(() => {
    const counts = this.activeInstance()?.counts ?? {};
    return [...new Set(['SOTA', 'IraceProposal', ...Object.keys(counts)])].map((algorithm) => ({
      algorithm,
      succeeded: counts[algorithm]?.['SUCCEEDED'] ?? 0,
      failed: counts[algorithm]?.['FAILED'] ?? 0,
    }));
  });
  readonly objectives = computed(() => [
    ...new Set(this.store.curves().flatMap((row) => Object.keys(row.curves))),
  ]);
  readonly activeObjective = computed(() =>
    this.objectives().includes(this.selectedObjective())
      ? this.selectedObjective()
      : this.objectives()[0],
  );
  readonly selectedCurves = this.store.curves;
  readonly canLogYAxis = computed(() =>
    ComparisonChartUtil.canUseLogarithmicScale(this.selectedCurves(), this.activeObjective()),
  );
  readonly effectiveYAxisScale = computed(() =>
    this.yAxisScale() === 'logarithmic' && this.canLogYAxis() ? 'logarithmic' : 'linear',
  );
  readonly curveChart = computed<ChartViewModel>(() =>
    ComparisonChartUtil.build(
      this.selectedCurves(),
      this.activeObjective(),
      this.effectiveYAxisScale(),
    ),
  );
  readonly issueTrials = computed(() =>
    (this.store.campaign()?.trials ?? []).filter((trial) => matchesTrialFilter(trial, 'attention')),
  );
  readonly activeOverviewTrials = computed(() =>
    (this.store.campaign()?.trials ?? []).filter((trial) => matchesTrialFilter(trial, 'active')),
  );
  readonly runningCount = computed(() => this.activeOverviewTrials().length);
  readonly pendingCount = computed(
    () =>
      (this.store.campaign()?.trials ?? []).filter((trial) => trial.status === 'PENDING').length,
  );
  readonly succeededCount = computed(
    () => this.store.campaign()?.trials.filter((t) => t.status === 'SUCCEEDED').length ?? 0,
  );
  readonly failedCount = computed(
    () => this.store.campaign()?.trials.filter((t) => t.status === 'FAILED').length ?? 0,
  );
  readonly filteredTrials = computed(() => {
    const search = this.trialSearch().toLowerCase(),
      filter = this.trialFilter();
    const rows = (this.store.campaign()?.trials ?? []).filter((t) => {
      if (!`${t.number} ${t.targetId ?? ''} ${this.label(t.status)}`.toLowerCase().includes(search))
        return false;
      return matchesTrialFilter(t, filter);
    });
    const sort = this.trialSort();
    return rows.sort((a, b) =>
      sort === 'status'
        ? a.status.localeCompare(b.status) || a.number - b.number
        : sort === 'duration'
          ? this.trialDuration(b) - this.trialDuration(a) || a.number - b.number
          : a.number - b.number,
    );
  });
  readonly effectiveTrialPage = computed(() =>
    Math.min(this.trialPage(), Math.max(0, Math.ceil(this.filteredTrials().length / 25) - 1)),
  );
  readonly trialRows = computed(() =>
    this.filteredTrials().slice(
      this.effectiveTrialPage() * 25,
      (this.effectiveTrialPage() + 1) * 25,
    ),
  );
  readonly filteredArtifacts = computed(() => {
    const search = this.artifactSearch().toLowerCase(),
      type = this.artifactType();
    const trial = this.store
      .campaign()
      ?.trials.find((t) => String(t.number) === this.artifactTrial());
    return this.store
      .artifacts()
      .filter(
        (file) =>
          file.path.toLowerCase().includes(search) &&
          (type === 'all' || this.fileType(file.path) === type) &&
          (!this.artifactTrial() ||
            (!!trial &&
              [trial.directory, ...(trial.attempts ?? []).map((a) => a.directory)].some(
                (path) => path && file.path.startsWith(path + '/'),
              ))),
      );
  });
  readonly effectiveArtifactPage = computed(() =>
    Math.min(this.artifactPage(), Math.max(0, Math.ceil(this.filteredArtifacts().length / 25) - 1)),
  );
  readonly artifactRows = computed(() =>
    this.filteredArtifacts().slice(
      this.effectiveArtifactPage() * 25,
      (this.effectiveArtifactPage() + 1) * 25,
    ),
  );
  readonly proposalInstances = computed(() => {
    const proposal = this.store.proposal();
    if (!proposal) return [];
    const training = new Set(proposal.training),
      testing = new Set(proposal.testing);
    return [...new Set([...proposal.testing, ...proposal.training])]
      .filter((path) => path.toLowerCase().includes(this.datasetSearch().toLowerCase()))
      .slice(0, 100)
      .map((path) => ({ path, training: training.has(path), testing: testing.has(path) }));
  });
  readonly proposalTargets = computed(() => Object.entries(this.store.proposal()?.targets ?? {}));
  readonly targetIds = computed(() => this.store.targets().map((target) => target.id));
  readonly datasetOverlap = computed(() => {
    const proposal = this.store.proposal();
    const testing = new Set(proposal?.testing ?? []);
    return proposal?.training.filter((path) => testing.has(path)).length ?? 0;
  });
  readonly datasetSelection = computed<DatasetSelection>(() => {
    const proposal = this.store.proposal();
    const screening = proposal?.screening;
    const excluded = {
      ...(screening?.selection?.excluded ??
        screening?.screening?.excluded ??
        screening?.excluded ??
        {}),
    };
    const testing = proposal?.testing ?? [];
    for (const path of screening?.originalPool ?? screening?.screening?.originalPool ?? []) {
      if (!testing.includes(path) && !proposal?.training.includes(path) && !(path in excluded))
        excluded[path] = '';
    }
    return { training: proposal?.training ?? [], testing, excluded };
  });
  readonly proposalChanges = computed(() => {
    const campaign = this.store.campaign(),
      proposal = this.store.proposal();
    const rows: { field: string; before: string; after: string }[] = [];
    if (!campaign || !proposal || campaign.status === 'DRAFT') return rows;
    const visit = (before: unknown, after: unknown, path: string) => {
      if (JSON.stringify(before) === JSON.stringify(after)) return;
      if (
        before &&
        after &&
        typeof before === 'object' &&
        typeof after === 'object' &&
        !Array.isArray(before) &&
        !Array.isArray(after)
      ) {
        const a = before as Record<string, unknown>,
          b = after as Record<string, unknown>;
        for (const key of new Set([...Object.keys(a), ...Object.keys(b)]))
          visit(a[key], b[key], path ? `${path}.${key}` : key);
      } else
        rows.push({
          field: path === 'trials' ? 'Total trials' : path,
          before:
            before == null
              ? '—'
              : typeof before === 'object'
                ? JSON.stringify(before)
                : String(before),
          after:
            after == null ? '—' : typeof after === 'object' ? JSON.stringify(after) : String(after),
        });
    };
    visit(campaign.config, proposal.configuration, '');
    const before = [...(campaign.approvedTargets ?? [])].sort();
    const after = this.proposalTargets()
      .filter(([, target]) => target.selected)
      .map(([id]) => id)
      .sort();
    if (JSON.stringify(before) !== JSON.stringify(after))
      rows.push({
        field: 'Approved servers',
        before: before.join(', ') || 'None',
        after: after.join(', ') || 'None',
      });
    return rows;
  });
  readonly trialSequence = computed(() => {
    const filtered = this.filteredTrials();
    return filtered.some((trial) => trial.number === this.store.selectedTrial())
      ? filtered
      : (this.store.campaign()?.trials ?? []);
  });
  readonly trialPosition = computed(() =>
    this.trialSequence().findIndex((trial) => trial.number === this.store.selectedTrial()),
  );
  readonly primaryAction = computed(() => {
    const campaign = this.store.campaign();
    if (!campaign) return { label: '', kind: '', reason: '' };
    if (this.store.proposal())
      return {
        label: this.store.active() === campaign.id ? 'Approve amendment' : 'Launch campaign',
        kind: 'launch',
        reason: this.datasetDirty()
          ? 'Validate the revised dataset before launching.'
          : this.store.proposal()!.generation.ready
            ? ''
            : this.store.proposal()!.generation.approvalLine,
      };
    if (campaign.status === 'DRAFT')
      return {
        label: 'Review campaign',
        kind: 'review',
        reason: this.draftDirty()
          ? 'Save edited configuration in Settings before reviewing this campaign.'
          : this.store.active()
            ? 'Wait for the active campaign to finish before launching another.'
            : '',
      };
    if (['SUCCEEDED', 'COMPLETED_WITH_FAILURES'].includes(campaign.status))
      return { label: 'View results', kind: 'results', reason: '' };
    if (
      ['NEEDS_ATTENTION', 'WAITING_FOR_TARGETS'].includes(campaign.status) ||
      (campaign.trials.some((trial) => ['BLOCKED', 'INTERRUPTED'].includes(trial.status)) &&
        this.store.active() === campaign.id)
    )
      return { label: 'Resolve issue', kind: 'resolve', reason: '' };
    if (['INTERRUPTED', 'CANCELLED'].includes(campaign.status) && !this.store.active())
      return {
        label: 'Resume campaign',
        kind: 'resume',
        reason: this.store.recovery()?.resumeCampaign?.allowed
          ? ''
          : this.store.recovery()?.resumeCampaign?.reason ||
            'Checking whether this campaign can resume…',
      };
    return {
      label: 'View active trial',
      kind: 'active',
      reason: this.store.activeTrials().length ? '' : 'Waiting for a trial to start.',
    };
  });
  readonly proposalExclusions = computed(() => {
    const screening = this.store.proposal()?.screening;
    return Object.entries(
      screening?.selection?.excluded ?? screening?.screening?.excluded ?? screening?.excluded ?? {},
    ).filter(([path, reason]) =>
      `${path} ${reason}`.toLowerCase().includes(this.datasetSearch().toLowerCase()),
    );
  });
  readonly filteredDecisions = computed(() =>
    (this.store.review()?.proposal.instances ?? []).filter((row) =>
      `${row.instance} ${row.classification} ${row.reason}`
        .toLowerCase()
        .includes(this.decisionSearch().toLowerCase()),
    ),
  );
  readonly effectiveDecisionPage = computed(() =>
    Math.min(this.decisionPage(), Math.max(0, Math.ceil(this.filteredDecisions().length / 25) - 1)),
  );
  readonly decisionRows = computed(() =>
    this.filteredDecisions().slice(
      this.effectiveDecisionPage() * 25,
      (this.effectiveDecisionPage() + 1) * 25,
    ),
  );
  readonly draftDirty = computed(
    () =>
      this.store.campaign()?.status === 'DRAFT' &&
      JSON.stringify(this.store.config()) !== JSON.stringify(this.store.campaign()?.config),
  );
  readonly campaignTrialCount = computed(() => this.store.campaign()?.trials.length ?? 10);
  readonly selectedResult = computed(() =>
    this.store.results()?.trials.find((run) => run.trial === this.store.selectedTrial()),
  );
  readonly filteredResultInstances = computed(() =>
    (this.selectedResult()?.instances ?? []).filter((row) =>
      row.instance.toLowerCase().includes(this.resultInstanceSearch().toLowerCase()),
    ),
  );
  readonly effectiveResultPage = computed(() =>
    Math.min(
      this.resultInstancePage(),
      Math.max(0, Math.ceil(this.filteredResultInstances().length / 25) - 1),
    ),
  );
  readonly resultInstanceRows = computed(() =>
    this.filteredResultInstances().slice(
      this.effectiveResultPage() * 25,
      (this.effectiveResultPage() + 1) * 25,
    ),
  );
  readonly resultExclusions = computed(() => Object.entries(this.store.results()?.excluded ?? {}));
  readonly algorithmEntries = computed(() => {
    const entries: { path: string; value: string }[] = [];
    const visit = (value: unknown, path: string) => {
      if (value !== null && typeof value === 'object') {
        for (const [key, child] of Object.entries(value))
          visit(child, path ? `${path}.${key}` : key);
      } else entries.push({ path, value: value === null ? '—' : String(value) });
    };
    visit(this.store.trial()?.winner, '');
    return entries;
  });
  constructor() {
    effect(() => {
      const proposal = this.store.proposal(),
        config = this.store.config();
      if (proposal && config && JSON.stringify(proposal.configuration) !== JSON.stringify(config))
        this.store.proposal.set(null);
    });
    effect(() => {
      const proposal = this.store.proposal();
      this.datasetDirty.set(false);
      if (proposal) {
        this.settingsOpen.set(false);
        this.focusPanel('.proposal');
      }
    });
    effect(() => {
      this.amendedTrials.set(this.campaignTrialCount());
    });
    effect(() => {
      this.store.selectedId();
      this.store.selectedTrial();
      this.failureReason.set('');
    });
    const onLocationChange = () => {
      if (this.navigationReady) this.restoreLocation();
    };
    window.addEventListener('popstate', onLocationChange);
    window.addEventListener('hashchange', onLocationChange);
    const destroyRef = inject(DestroyRef);
    destroyRef.onDestroy(() => {
      window.removeEventListener('popstate', onLocationChange);
      window.removeEventListener('hashchange', onLocationChange);
    });
    void this.store.start().then(() => {
      if (destroyRef.destroyed) return;
      this.navigationReady = true;
      if (window.location.hash) this.restoreLocation();
      else this.saveLocation();
      this.executionChoice.set(this.store.campaign()?.config.execution.target ?? '');
    });
  }
  label(value: string | null | undefined): string {
    if (!value) return '—';
    return value
      .toLowerCase()
      .replace(/_/g, ' ')
      .replace(/^./, (char) => char.toUpperCase());
  }
  percent(done: number, total: number): number {
    return total > 0 ? Math.min(100, Math.max(0, (100 * done) / total)) : 0;
  }
  durationLabel(millis: number): string {
    if (!Number.isFinite(millis) || millis <= 0) return '—';
    const minutes = Math.floor(millis / 60000);
    return minutes >= 60
      ? `${Math.floor(minutes / 60)}h ${minutes % 60}m`
      : `${minutes}m ${Math.floor(millis / 1000) % 60}s`;
  }
  trialDuration(trial: AutoLlmTrial): number {
    return trialTiming(trial, this.timingNow()).elapsed ?? 0;
  }
  campaignDuration(): string {
    const timings = (this.store.campaign()?.trials ?? [])
      .map((trial) => trialTiming(trial, this.timingNow()))
      .filter((timing) => timing.start !== null && timing.end !== null);
    if (!timings.length) return '—';
    const start = Math.min(...timings.map((timing) => timing.start!));
    const end = Math.max(...timings.map((timing) => timing.end!));
    return this.durationLabel(end - start);
  }
  trialProgress(trial: AutoLlmTrial): string {
    const progress = this.store.activeProgress()[trial.number];
    if (progress?.stale) return 'Last update unavailable';
    const workload = progress?.progress?.workload as
      { budget?: { used: number; maximum: number } } | undefined;
    if (workload?.budget) return `${workload.budget.used} / ${workload.budget.maximum} evaluations`;
    return trial.status === 'SUCCEEDED' || trial.status === 'FAILED'
      ? 'Finished'
      : this.label(progress?.progress?.substage);
  }
  comparisonLabel(number: number): string {
    const results = this.store.results(),
      run = results?.trials.find((r) => r.trial === number);
    if (!run) return results?.excluded[number] ? 'Incomplete / excluded' : 'Not compared';
    return run.meanImprovementPercent === null
      ? 'Undefined percentage'
      : `${run.meanImprovementPercent.toFixed(2)}% · ${results!.objective}`;
  }
  attemptDuration(number: number): string {
    const interval = this.attemptTimings().find((interval) => interval.attempt.number === number);
    return timingDuration(interval ? interval.end - interval.start : null);
  }
  openTimedAttempt(selection: AttemptSelection): void {
    this.openTrial(selection.trial, 0, false);
    this.selectedStage.set(selection.stage);
    this.selectedAttempt.set(selection.attempt);
    this.saveLocation(true);
  }
  selectStage(stage: TrialStage): void {
    this.selectedStage.set(stage);
    this.selectedAttempt.set(null);
    this.saveLocation(true);
  }
  selectDetailTab(index: number): void {
    if (index === this.detailTab()) return;
    this.detailTab.set(index);
    this.saveLocation(true);
  }
  generationAgent(agent: 'CODEX' | 'CLAUDE_CODE'): void {
    this.store.config.update(config => config ? { ...config, generation: {
      ...config.generation, agent, executable: agent === 'CODEX' ? 'codex' : 'claude', model: null, reasoningEffort: null
    }} : config);
  }

  inspectAttemptActivity(number: number, generation: boolean): void {
    this.selectedAttempt.set(number);
    if (generation) this.store.watchGeneration(number);
    this.detailTab.set(3);
    this.saveLocation(true);
    this.focusPanel('.trial-inspector');
  }
  fileName(path: string): string {
    return path.split('/').at(-1) ?? path;
  }
  fileSize(bytes: number): string {
    return bytes >= 1048576
      ? `${(bytes / 1048576).toFixed(1)} MiB`
      : bytes >= 1024
        ? `${(bytes / 1024).toFixed(1)} KiB`
        : `${bytes} B`;
  }
  fileType(path: string): string {
    if (/conversation|transcript/.test(path)) return 'Conversations';
    if (/\.(log|jsonl)$/.test(path)) return 'Logs';
    if (/reports\/|\.(xlsx|pdf|html)$/.test(path)) return 'Reports';
    if (/source\/|\.(patch|diff|java|zip|tar|gz)$/.test(path)) return 'Source';
    return 'Data';
  }
  selectCampaign(id: string): void {
    this.store.select(id);
    this.tab.set(0);
    this.settingsOpen.set(false);
    this.trialSearch.set('');
    this.trialFilter.set('all');
    this.trialPage.set(0);
    this.trialSort.set('number');
    this.timelineSettings.set(defaultTimelineSettings());
    this.selectedTarget.set('');
    this.campaignScrollY = null;
    this.artifactTrial.set('');
    this.artifactPage.set(0);
    this.executionChoice.set(this.store.config()?.execution.target ?? '');
    this.saveLocation(true);
  }
  async newCampaign(): Promise<void> {
    await this.store.newCampaign();
    if (!this.store.creating()) return;
    this.executionChoice.set('');
    this.settingsOpen.set(true);
    this.tab.set(0);
    this.saveLocation(true);
  }
  openSettings(): void {
    this.closeTrial();
    this.settingsOpen.set(true);
    void this.store.refreshSummary();
  }
  selectTab(index: number): void {
    if (index === this.tab() && !this.store.inspecting()) return;
    this.store.inspecting.set(false);
    this.tab.set(index);
    if (index === 4 && !this.store.artifactsLoaded()) void this.store.refreshArtifacts();
    this.saveLocation(true);
  }
  openTrial(number: number, detail = 0, navigate = true): void {
    if (!this.store.inspecting()) this.campaignScrollY = window.scrollY;
    this.selectedStage.set(null);
    this.selectedAttempt.set(null);
    this.store.selectTrial(number);
    this.detailTab.set(detail);
    this.settingsOpen.set(false);
    this.resultInstancePage.set(0);
    if (navigate) this.saveLocation(true);
    this.focusPanel('.trial-inspector');
  }
  private focusPanel(selector: string): void {
    afterNextRender(
      () => {
        const panel = document.querySelector<HTMLElement>(selector);
        panel?.scrollIntoView({ block: 'start' });
        panel?.querySelector<HTMLElement>('h1, h2, h3')?.focus({ preventScroll: true });
      },
      { injector: this.injector },
    );
  }
  openLog(path: string): void {
    this.store.selectLog(path);
    this.focusPanel('.log-preview');
  }
  closeTrial(): void {
    this.store.inspecting.set(false);
    this.saveLocation(true);
    if (this.campaignScrollY !== null) this.restoreScroll(this.campaignScrollY);
    else this.focusPanel(this.tab() === 0 ? 'app-autollm-timeline' : '.status-card');
  }
  async reviseDataset(selection: DatasetSelection): Promise<void> {
    await this.store.reviseDataset(selection);
    this.saveLocation();
  }
  adjacentTrial(delta: number): void {
    const trial = this.trialSequence()[this.trialPosition() + delta];
    if (trial) this.openTrial(trial.number, this.detailTab());
  }
  runPrimaryAction(): void {
    const action = this.primaryAction();
    if (action.reason || this.store.busy()) return;
    switch (action.kind) {
      case 'review':
        void this.store.reviewProposal(this.store.startTargets());
        break;
      case 'launch':
        void this.store.approveAndStart();
        break;
      case 'results':
        this.selectTab(2);
        break;
      case 'resume':
        void this.store.action('resume');
        break;
      case 'resolve': {
        const trial = this.store
          .campaign()
          ?.trials.find((trial) => ['BLOCKED', 'INTERRUPTED'].includes(trial.status));
        if (trial) this.openTrial(trial.number);
        else this.selectTab(3);
        break;
      }
      case 'active': {
        const trial = this.store.activeTrials()[0];
        if (trial) this.openTrial(trial.number);
      }
    }
  }
  openCleanup(): void {
    this.selectTab(4);
    afterNextRender(
      () => {
        const details = document.querySelector<HTMLDetailsElement>('#campaign-cleanup');
        if (details) {
          details.open = true;
          details.scrollIntoView({ block: 'start' });
          details.querySelector('summary')?.focus();
        }
      },
      { injector: this.injector },
    );
  }
  setTimelineSettings(settings: TimelineSettings): void {
    this.timelineSettings.set(settings);
    this.saveLocation();
  }
  filterOverview(outcome: TrialFilter): void {
    this.timelineSettings.update((settings) => ({ ...settings, outcome, search: '', page: 0 }));
    this.store.inspecting.set(false);
    this.tab.set(0);
    this.saveLocation(true);
    this.focusPanel('app-autollm-timeline');
  }
  reviewTrialServer(): void {
    this.selectedTarget.set(this.store.trial()?.targetId ?? '');
    this.selectTab(3);
    this.focusPanel('.target-card.selected');
  }
  inspectTrialError(): void {
    const trial = this.store.trial(),
      attempt = trial?.attempts.at(-1);
    if (attempt)
      this.inspectAttemptActivity(attempt.number, ['GENERATION', 'REPAIR'].includes(attempt.stage));
    else this.selectDetailTab(4);
  }
  private restoreScroll(top: number): void {
    afterNextRender(() => window.scrollTo({ top, behavior: 'instant' }), {
      injector: this.injector,
    });
  }
  private restoreLocation(): void {
    this.restoringLocation = true;
    try {
      const view = readAutoLlmLocation(window.location.hash);
      const id = this.store.campaigns().some((c) => c.id === view.campaign)
        ? view.campaign
        : this.store.selectedId();
      if (id !== this.store.selectedId()) this.store.select(id);
      this.settingsOpen.set(false);
      this.tab.set(view.tab);
      this.detailTab.set(view.detail);
      this.timelineSettings.set(view.timeline);
      this.trialSearch.set(view.trialSearch);
      this.trialFilter.set(view.trialFilter);
      this.trialSort.set(view.trialSort);
      this.trialPage.set(view.trialPage);
      this.selectedTarget.set(view.target);
      const trial = this.store.campaign()?.trials.find((trial) => trial.number === view.trial);
      if (trial) {
        if (!this.store.inspecting() || this.store.selectedTrial() !== trial.number)
          this.store.selectTrial(trial.number);
        const attempt = trial.attempts.find((attempt) => attempt.number === view.attempt);
        this.selectedAttempt.set(attempt?.number ?? null);
        this.selectedStage.set(attempt ? (stageOf(attempt.stage) as TrialStage) : view.stage);
        if (view.detail === 3 && attempt && ['GENERATION', 'REPAIR'].includes(attempt.stage))
          this.store.watchGeneration(attempt.number);
      } else {
        this.store.inspecting.set(false);
        this.selectedStage.set(null);
        this.selectedAttempt.set(null);
      }
      if (view.tab === 4 && !this.store.artifactsLoaded()) void this.store.refreshArtifacts();
      const top = window.history.state?.morkScrollY;
      if (typeof top === 'number' && Number.isFinite(top)) this.restoreScroll(top);
    } finally {
      this.restoringLocation = false;
    }
  }
  saveLocation(push = false): void {
    if (!this.navigationReady || this.restoringLocation) return;
    const hash = autoLlmLocation({
      campaign: this.store.selectedId(),
      tab: this.tab(),
      trial: this.store.inspecting() ? this.store.selectedTrial() : null,
      detail: this.detailTab(),
      stage: this.selectedStage(),
      attempt: this.selectedAttempt(),
      target: this.selectedTarget(),
      timeline: this.timelineSettings(),
      trialSearch: this.trialSearch(),
      trialFilter: this.trialFilter(),
      trialSort: this.trialSort(),
      trialPage: this.trialPage(),
    });
    if (hash === window.location.hash) return;
    if (push) {
      window.history.replaceState(
        { ...window.history.state, morkScrollY: window.scrollY },
        '',
        window.location.href,
      );
      window.history.pushState(null, '', hash);
    } else window.history.replaceState(window.history.state, '', hash);
  }
  downloadAlgorithm(): void {
    const trial = this.store.trial();
    if (!trial?.winner) return;
    const url = URL.createObjectURL(
      new Blob([JSON.stringify(trial.winner, null, 2)], { type: 'application/json' }),
    );
    const link = document.createElement('a');
    link.href = url;
    link.download = `trial-${trial.number}-algorithm.json`;
    link.click();
    URL.revokeObjectURL(url);
  }
  showTrialArtifacts(number: number): void {
    this.artifactTrial.set(String(number));
    this.artifactPage.set(0);
    this.selectTab(4);
  }
  duration(path: string, event: Event, factor = 1000): void {
    const value = Number((event.target as HTMLInputElement).value);
    this.update(
      path,
      { target: { value: String(Math.round(value * factor)) } } as unknown as Event,
      true,
    );
  }
  executionTarget(event: Event): void {
    const value = (event.target as HTMLSelectElement).value as 'LOCAL' | 'SSH';
    this.executionChoice.set(value);
    this.store.config.update((config) =>
      config
        ? {
            ...config,
            execution: {
              ...config.execution,
              target: value,
              ssh: config.execution.ssh ?? {
                screeningServerId: 'server-1',
                runnerArtifact: null,
                servers: [
                  {
                    id: 'server-1',
                    host: '',
                    workspace: '',
                    javaExecutable: 'java',
                    environment: {},
                  },
                ],
              },
            },
          }
        : config,
    );
    this.store.targetStatus.set(null);
  }
  addServer(): void {
    this.store.config.update((config) => {
      if (!config?.execution.ssh) return config;
      let number = 1;
      const ids = new Set(config.execution.ssh.servers.map((server) => server.id));
      while (ids.has(`server-${number}`)) number++;
      const servers = [
        ...config.execution.ssh.servers,
        {
          id: `server-${number}`,
          host: '',
          workspace: '',
          javaExecutable: 'java',
          environment: {},
        },
      ];
      return {
        ...config,
        execution: { ...config.execution, ssh: { ...config.execution.ssh, servers } },
      };
    });
  }
  removeServer(index: number): void {
    this.store.config.update((config) => {
      if (!config?.execution.ssh) return config;
      const ssh = config.execution.ssh,
        servers = ssh.servers.filter((_, i) => i !== index);
      return {
        ...config,
        execution: {
          ...config.execution,
          ssh: {
            ...ssh,
            servers,
            screeningServerId: servers.some((server) => server.id === ssh.screeningServerId)
              ? ssh.screeningServerId
              : (servers[0]?.id ?? ''),
          },
        },
      };
    });
  }
  workerMode(event: Event): void {
    const automatic = (event.target as HTMLSelectElement).value === 'AUTO';
    this.store.config.update((config) =>
      config
        ? { ...config, execution: { ...config.execution, workers: automatic ? -1 : 1 } }
        : config,
    );
  }
  update(path: string, event: Event, numeric = false): void {
    const current = this.store.config();
    if (!current) return;
    const config = structuredClone(current);
    let target = config as unknown as Record<string, unknown>;
    const keys = path.split('.');
    for (const key of keys.slice(0, -1)) {
      if (!target[key] || typeof target[key] !== 'object') target[key] = {};
      target = target[key] as Record<string, unknown>;
    }
    const text = (event.target as HTMLInputElement).value;
    target[keys[keys.length - 1]] = numeric ? (text ? Number(text) : null) : text || null;
    if (
      /^execution\.ssh\.servers\.\d+\.id$/.test(path) &&
      config.execution.ssh &&
      current.execution.ssh
    ) {
      const index = Number(keys[3]);
      if (current.execution.ssh.screeningServerId === current.execution.ssh.servers[index]?.id)
        config.execution.ssh.screeningServerId = text;
    }
    this.store.config.set(config);
  }
  buildCommand(event: Event): void {
    try {
      const command: unknown = JSON.parse((event.target as HTMLInputElement).value);
      if (!Array.isArray(command) || command.some((value) => typeof value !== 'string'))
        throw new Error('Build command must be a JSON array of arguments');
      this.store.config.update((c) =>
        c ? { ...c, project: { ...c.project, buildCommand: command as string[] } } : c,
      );
    } catch (e) {
      this.store.error.set((e as Error).message);
    }
  }
  advanced(event: Event): void {
    try {
      const current = this.store.config();
      if (!current) return;
      const parsed = JSON.parse(
        (event.target as HTMLTextAreaElement).value,
      ) as Partial<AutoLlmConfig>;
      if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed))
        throw new Error('Settings must be a JSON object');
      for (const key of [
        'project',
        'generation',
        'preparation',
        'tuning',
        'comparison',
        'execution',
      ] as const) {
        if (parsed[key] != null && (typeof parsed[key] !== 'object' || Array.isArray(parsed[key])))
          throw new Error(`${key} must be a JSON object`);
      }
      const ssh = parsed.execution?.ssh;
      if (ssh != null && (typeof ssh !== 'object' || Array.isArray(ssh)))
        throw new Error('execution.ssh must be a JSON object');
      this.store.config.set({
        ...current,
        ...parsed,
        project: { ...current.project, ...parsed.project },
        generation: { ...current.generation, ...parsed.generation },
        preparation: { ...current.preparation, ...parsed.preparation },
        tuning: { ...current.tuning, ...parsed.tuning },
        comparison: { ...current.comparison, ...parsed.comparison },
        execution: {
          ...current.execution,
          ...parsed.execution,
          ssh:
            ssh === null
              ? null
              : current.execution.ssh || ssh
                ? {
                    screeningServerId: '',
                    runnerArtifact: null,
                    servers: [],
                    ...current.execution.ssh,
                    ...ssh,
                  }
                : null,
        },
      });
      if (parsed.execution?.target) this.executionChoice.set(parsed.execution.target);
      this.store.error.set(null);
    } catch (error) {
      this.store.error.set((error as Error).message);
    }
  }
}
