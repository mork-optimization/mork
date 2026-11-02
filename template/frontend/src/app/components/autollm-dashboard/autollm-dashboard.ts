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
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { FormsModule } from '@angular/forms';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
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
import { DashboardChart } from '../dashboard-chart/dashboard-chart';
import { ChartViewModel } from '../../model/dashboard';
import { ComparisonChartUtil, ComparisonYAxisScale } from '../../util/comparison-chart-util';
import { InstanceLabelUtil } from '../../util/instance-label-util';
import { FilePreviewUtil } from '../../util/file-preview-util';

@Component({
  selector: 'app-autollm-dashboard',
  imports: [
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
  readonly stages = [
    'GENERATION',
    'LOCAL_PREPARATION',
    'PREPARATION',
    'TUNING',
    'WINNER_EXPORT',
    'COMPARISON',
  ];
  readonly canPreviewFile = FilePreviewUtil.canPreview;
  readonly store = inject(AutoLlmStore);
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
    (this.store.campaign()?.trials ?? []).filter((t) =>
      ['BLOCKED', 'INTERRUPTED', 'FAILED'].includes(t.status),
    ),
  );
  readonly runningCount = computed(
    () =>
      this.store.activeTrials().filter((t) => !['BLOCKED', 'INTERRUPTED'].includes(t.status))
        .length,
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
      if (filter === 'active') return !['PENDING', 'SUCCEEDED', 'FAILED'].includes(t.status);
      if (filter === 'attention') return ['BLOCKED', 'INTERRUPTED', 'FAILED'].includes(t.status);
      return filter === 'all' || filter === t.status;
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
      if (!testing.includes(path) && !(path in excluded)) excluded[path] = '';
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
    const route = new URLSearchParams(window.location.hash.slice(1));
    void this.store.start().then(() => {
      const campaign = route.get('campaign');
      if (campaign && this.store.campaigns().some((c) => c.id === campaign))
        this.store.select(campaign);
      if (this.store.campaign())
        this.executionChoice.set(this.store.campaign()!.config.execution.target ?? '');
      const tab = Number(route.get('tab') ?? 0);
      if (route.has('tab') && Number.isInteger(tab) && tab >= 0 && tab <= 4) this.selectTab(tab);
      const trial = Number(route.get('trial'));
      if (trial && this.store.campaign()?.trials.some((t) => t.number === trial)) {
        const detail = Number(route.get('detail') ?? 0);
        this.openTrial(trial, Number.isInteger(detail) && detail >= 0 && detail <= 4 ? detail : 0);
      }
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
    const attempts = trial.attempts ?? [];
    if (!attempts.length) return 0;
    const start = Math.min(...attempts.map((a) => Date.parse(a.startedAt)));
    const end = Math.max(
      ...attempts.map((a) => (a.finishedAt ? Date.parse(a.finishedAt) : this.store.now())),
    );
    return Math.max(0, end - start);
  }
  campaignDuration(): string {
    const attempts = (this.store.campaign()?.trials ?? []).flatMap((t) => t.attempts ?? []);
    if (!attempts.length) return '—';
    const start = Math.min(...attempts.map((a) => Date.parse(a.startedAt)));
    const end = Math.max(
      ...attempts.map((a) => (a.finishedAt ? Date.parse(a.finishedAt) : this.store.now())),
    );
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
  trialStage(trial: AutoLlmTrial): string {
    const stage = trial.attempts?.at(-1)?.stage ?? trial.status;
    return stage === 'REPAIR' ? 'GENERATION' : stage;
  }
  stageState(trial: AutoLlmTrial, stage: string): string {
    const attempt = trial.attempts
      ?.filter((a) => a.stage === stage || (stage === 'GENERATION' && a.stage === 'REPAIR'))
      .at(-1);
    return attempt ? this.label(attempt.status) : 'Not started';
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
    this.artifactTrial.set('');
    this.artifactPage.set(0);
    this.executionChoice.set(this.store.config()?.execution.target ?? '');
    this.saveLocation();
  }
  async newCampaign(): Promise<void> {
    await this.store.newCampaign();
    if (!this.store.creating()) return;
    this.executionChoice.set('');
    this.settingsOpen.set(true);
    this.tab.set(0);
    this.saveLocation();
  }
  openSettings(): void {
    this.closeTrial();
    this.settingsOpen.set(true);
    void this.store.refreshSummary();
  }
  selectTab(index: number): void {
    this.store.inspecting.set(false);
    this.tab.set(index);
    if (index === 4 && !this.store.artifactsLoaded()) void this.store.refreshArtifacts();
    this.saveLocation();
  }
  openTrial(number: number, detail = 0): void {
    this.store.selectTrial(number);
    this.detailTab.set(detail);
    this.settingsOpen.set(false);
    this.resultInstancePage.set(0);
    this.saveLocation();
    this.focusPanel('.trial-inspector');
  }
  private focusPanel(selector: string): void {
    afterNextRender(
      () => {
        const panel = document.querySelector<HTMLElement>(selector);
        panel?.scrollIntoView({ block: 'start' });
        panel?.querySelector<HTMLElement>('h1, h2')?.focus({ preventScroll: true });
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
    this.saveLocation();
    this.focusPanel('.status-card');
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
  saveLocation(): void {
    const params = new URLSearchParams();
    if (this.store.selectedId()) params.set('campaign', this.store.selectedId());
    params.set('tab', String(this.tab()));
    if (this.store.inspecting()) {
      params.set('trial', String(this.store.selectedTrial()));
      params.set('detail', String(this.detailTab()));
    }
    window.history.replaceState(null, '', '#' + params.toString());
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
        'codex',
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
        codex: { ...current.codex, ...parsed.codex },
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
