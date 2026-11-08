import { DestroyRef, Injectable, computed, inject, signal } from '@angular/core';
import { AutoLlmClient } from '../service/autollm-client';
import {
  DatasetEntry,
  DatasetReview,
  ObjectiveModes,
  DatasetSelection,
  CampaignReceipt,
  CampaignComparison,
  AutoLlmComparison,
  AutoLlmActivity,
  AutoLlmArtifact,
  AutoLlmCampaign,
  AutoLlmConfig,
  AutoLlmLogPreview,
  AutoLlmProgress,
  AutoLlmRecovery,
  AutoLlmServer,
  ScreeningJob,
  ScreeningReview,
} from '../model/autollm';
import { ComparisonCurveRow } from '../util/comparison-chart-util';
import { AutoconfigStatus } from '../model/autoconfig';

@Injectable({ providedIn: 'root' })
export class AutoLlmStore {
  private readonly client = inject(AutoLlmClient);
  private readonly destroy = inject(DestroyRef);
  readonly config = signal<AutoLlmConfig | null>(null);
  readonly campaigns = signal<AutoLlmCampaign[]>([]);
  readonly active = signal('');
  readonly selectedId = signal('');
  readonly selectedTrial = signal(1);
  readonly cleanupPlan = signal<import('../model/autollm').CleanupPlan | null>(null);
  readonly cleanupReceipt = signal<import('../model/autollm').CleanupReceipt | null>(null);
  readonly approvalDifferences = signal<import('../model/autollm').ApprovalDifference[]>([]);
  readonly proposal = signal<Awaited<ReturnType<AutoLlmClient['proposal']>> | null>(null);
  readonly startTargets = signal<string[]>([]);
  readonly campaign = computed(
    () => this.campaigns().find((c) => c.id === this.selectedId()) ?? null,
  );
  readonly detail = signal<AutoLlmCampaign | null>(null);
  readonly detailError = signal<string | null>(null);
  readonly detailLoading = signal(false);
  readonly inspecting = signal(false);
  readonly creating = signal(false);
  readonly lastUpdated = signal<number | null>(null);
  readonly progressError = signal<string | null>(null);
  readonly comparisonError = signal<string | null>(null);
  readonly comparisonUpdated = signal<number | null>(null);
  readonly results = signal<CampaignComparison | null>(null);
  readonly resultsError = signal<string | null>(null);
  readonly resultsLoading = signal(false);
  readonly resultObjective = signal('');
  readonly objectiveModes = signal<ObjectiveModes>({});
  readonly objectiveNames = computed(() => Object.keys(this.objectiveModes()));
  readonly objectivesError = signal<string | null>(null);
  readonly resultMode = signal<'MINIMIZE' | 'MAXIMIZE'>('MINIMIZE');
  readonly report = signal<import('../model/autollm').AutoLlmReportStatus | null>(null);
  readonly reportError = signal<string | null>(null);
  readonly reportObjective = signal('');
  readonly reportMode = signal<'MINIMIZE' | 'MAXIMIZE'>('MINIMIZE');
  readonly reportObjectives = computed(() => Object.keys(this.report()?.objectives ?? {}));
  readonly reportBusy = computed(() => ['QUEUED', 'RUNNING'].includes(this.report()?.state ?? ''));
  private reportRequest: string | undefined;
  private reportInitialized = false;
  readonly artifactsLoading = signal(false);
  readonly artifactsLoaded = signal(false);
  readonly artifactsError = signal<string | null>(null);
  private detailKey = '';
  private detailRequest = 0;
  private resultsKey = '';
  private resultsRequest = 0;
  private selection = 0;
  readonly trial = computed(() => {
    const current = this.campaign()?.trials.find((t) => t.number === this.selectedTrial());
    if (!current) return null;
    const saved = this.detail()?.trials.find((t) => t.number === current.number);
    if (!saved) return current;
    return {
      ...current,
      winner: saved.winner,
      failure: saved.failure,
      attempts: (current.attempts ?? []).map((attempt) => ({
        ...attempt,
        failure: saved.attempts.find((a) => a.number === attempt.number)?.failure ?? null,
        generationEvidence:
          saved.attempts.find((a) => a.number === attempt.number)?.generationEvidence ?? null,
      })),
    };
  });
  readonly completed = computed(
    () =>
      this.campaign()?.trials.filter((t) => ['SUCCEEDED', 'FAILED'].includes(t.status)).length ?? 0,
  );
  readonly progress = signal<AutoLlmProgress | null>(null);
  readonly activeProgress = signal<Record<number, { progress?: AutoLlmProgress; stale: boolean }>>(
    {},
  );
  readonly targets = computed(() =>
    Object.entries(this.campaign()?.targets ?? {}).map(([id, target]) => ({ id, ...target })),
  );
  readonly activeTrials = computed(
    () =>
      this.campaign()?.trials.filter(
        (trial) => !['PENDING', 'SUCCEEDED', 'FAILED'].includes(trial.status),
      ) ?? [],
  );
  readonly now = signal(Date.now());
  readonly tuning = computed(() =>
    this.progress()?.workload?.['budget']
      ? (this.progress()!.workload as unknown as AutoconfigStatus)
      : null,
  );
  readonly generation = computed(() => this.progress()?.generation ?? null);
  readonly generationAttempt = signal<number | null>(null);
  readonly conversationAttempt = computed(() => {
    const attempts =
      this.trial()?.attempts?.filter((attempt) =>
        ['GENERATION', 'REPAIR'].includes(attempt.stage),
      ) ?? [];
    return (
      attempts.find((attempt) => attempt.number === this.generationAttempt()) ??
      attempts.at(-1) ??
      null
    );
  });
  readonly activity = signal<AutoLlmActivity[]>([]);
  readonly datasets = signal<DatasetEntry[]>([]);
  readonly datasetReview = signal<DatasetReview | null>(null);
  readonly datasetName = signal('');
  readonly screenings = signal<ScreeningJob[]>([]);
  readonly activeScreening = signal('');
  readonly selectedScreening = signal('');
  readonly screening = computed(
    () => this.screenings().find((job) => job.id === this.selectedScreening()) ?? null,
  );
  readonly review = signal<ScreeningReview | null>(null);
  readonly summary = signal<Record<string, unknown> | null>(null);
  readonly comparison = signal<AutoLlmComparison | null>(null);
  readonly comparisonInstances = computed(() => this.comparison()?.instances ?? []);
  readonly curveInstance = signal('');
  readonly curves = signal<ComparisonCurveRow[]>([]);
  readonly curvesLoading = signal(false);
  readonly curvesError = signal<string | null>(null);
  private comparisonSelection = 0;
  private curveRequest = 0;
  private curveCounts = '';
  readonly artifacts = signal<AutoLlmArtifact[]>([]);
  readonly logPath = signal('');
  readonly logLines = signal(500);
  readonly logFollow = signal(true);
  readonly logPreview = signal<AutoLlmLogPreview | null>(null);
  readonly logLoading = signal(false);
  readonly logError = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly connectionError = signal<string | null>(null);
  readonly busy = signal(false);
  readonly targetStatus = signal<Record<string, unknown> | null>(null);
  readonly recovery = signal<AutoLlmRecovery | null>(null);
  readonly recoveryError = signal<string | null>(null);
  readonly trialRecovery = computed(() => this.recovery()?.trials[this.selectedTrial()]);
  private timer: ReturnType<typeof setTimeout> | null = null;
  private started = false;
  private polling = false;
  private retryRequested = false;
  private destroyed = false;
  private logRequest = 0;
  private activityKey = '';
  private activityCursor = 0;
  private activityPending = '';
  private operations = 0;
  constructor() {
    this.destroy.onDestroy(() => {
      this.destroyed = true;
      if (this.timer) clearTimeout(this.timer);
    });
  }
  async start(): Promise<void> {
    if (this.started) return;
    this.started = true;
    try {
      this.config.set(await this.client.defaults());
    } catch (e) {
      this.connectionError.set(this.message(e));
    }
    await this.poll();
  }
  retryNow(): void {
    if (this.timer) clearTimeout(this.timer);
    if (this.polling) this.retryRequested = true;
    else void this.poll();
  }
  private async poll(): Promise<void> {
    if (this.destroyed || this.polling) return;
    this.polling = true;
    try {
      if (!this.config()) this.config.set(await this.client.defaults());
      const probe = this.targetStatus();
      const [campaignResult, screeningResult, probeResult, objectivesResult] =
        await Promise.allSettled([
          this.client.list(),
          this.client.screenings(),
          probe?.['status'] === 'RUNNING' && typeof probe['id'] === 'string'
            ? this.client.targetProbe(probe['id'])
            : Promise.resolve(null),
          this.objectiveNames().length
            ? Promise.resolve(this.objectiveModes())
            : this.client.objectives(),
        ]);
      if (this.destroyed) return;
      if (objectivesResult.status === 'fulfilled') {
        this.objectiveModes.set(objectivesResult.value);
        this.objectivesError.set(
          this.objectiveNames().length ? null : 'No objective definitions are available.',
        );
      } else this.objectivesError.set(this.message(objectivesResult.reason));
      this.connectionError.set(null);
      if (
        probeResult.status === 'fulfilled' &&
        probeResult.value &&
        this.targetStatus()?.['id'] === probe?.['id']
      )
        this.targetStatus.set(probeResult.value);
      if (probeResult.status === 'rejected' && this.targetStatus()?.['id'] === probe?.['id'])
        this.targetStatus.update((previous) => ({
          ...previous,
          readError: this.message(probeResult.reason),
        }));
      if (campaignResult.status === 'fulfilled') {
        const data = campaignResult.value;
        this.campaigns.set(data.campaigns);
        this.active.set(data.active);
        if (!this.selectedId() && !this.creating()) {
          const id = data.active || data.campaigns[0]?.id;
          if (id) this.select(id);
        }
        this.lastUpdated.set(Date.now());
      } else this.connectionError.set(this.message(campaignResult.reason));
      if (screeningResult.status === 'fulfilled') {
        const data = screeningResult.value;
        this.screenings.set(data.jobs);
        this.activeScreening.set(data.active);
        if (!this.selectedScreening())
          this.selectedScreening.set(data.active || data.jobs[0]?.id || '');
      } else this.connectionError.set(this.message(screeningResult.reason));
      this.now.set(Date.now());
      const campaignId = this.selectedId(),
        selected = this.selection,
        activeTrials = this.activeTrials();
      const [updates] = await Promise.all([
        Promise.allSettled(
          activeTrials.map((trial) => this.client.progress(campaignId, trial.number)),
        ),
        this.refreshRecovery(),
        this.refreshReport(),
      ]);
      if (selected === this.selection && this.selectedId() === campaignId && !this.destroyed) {
        const next: Record<number, { progress?: AutoLlmProgress; stale: boolean }> = {};
        updates.forEach((update, index) => {
          const number = activeTrials[index].number;
          next[number] =
            update.status === 'fulfilled'
              ? { progress: update.value, stale: false }
              : { progress: this.activeProgress()[number]?.progress, stale: true };
        });
        this.activeProgress.set(next);
      }
      if (this.trial()) {
        const id = this.selectedId(),
          trial = this.selectedTrial(),
          selection = this.comparisonSelection;
        const [progress, comparison] = await Promise.allSettled([
          this.activeProgress()[trial]?.progress ?? this.client.progress(id, trial),
          this.client.comparison(id, trial),
        ]);
        if (selection === this.comparisonSelection && !this.destroyed) {
          if (progress.status === 'fulfilled') {
            this.progress.set(progress.value);
            this.progressError.set(
              this.activeProgress()[trial]?.stale
                ? 'Progress unavailable; showing last update'
                : null,
            );
          } else this.progressError.set(this.message(progress.reason));
          if (comparison.status === 'fulfilled') {
            this.comparison.set(comparison.value);
            this.comparisonError.set(null);
            this.comparisonUpdated.set(Date.now());
            if (
              !this.comparisonInstances().some(
                (instance) => instance.instanceId === this.curveInstance(),
              )
            ) {
              await this.selectCurveInstance(this.comparisonInstances()[0]?.instanceId ?? '');
            } else await this.refreshCurves();
          } else this.comparisonError.set(this.message(comparison.reason));
        }
      }
      if (this.inspecting()) await this.refreshDetail();
      if (!this.resultObjective() && this.objectiveNames().length && this.campaign())
        await this.loadResults(this.objectiveNames()[0]);
      else if (this.resultObjective()) await this.refreshResults();
      const attempt = this.conversationAttempt();
      if (attempt) {
        const id = this.selectedId(),
          trial = this.selectedTrial(),
          key = `${id}/${trial}/${attempt.number}`;
        if (this.activityKey !== key) {
          this.activityKey = key;
          this.activityCursor = 0;
          this.activityPending = '';
          this.activity.set([]);
        }
        const cursor = this.activityCursor;
        const page = await this.client.activity(id, trial, attempt.number, cursor);
        if (
          this.activityKey === key &&
          this.selectedId() === id &&
          this.selectedTrial() === trial &&
          this.conversationAttempt()?.number === attempt.number &&
          !this.destroyed
        ) {
          if (page.reset) {
            this.activity.set([]);
            this.activityPending = '';
          }
          const lines = (this.activityPending + page.text).split('\n');
          this.activityPending = lines.pop() ?? '';
          const events: AutoLlmActivity[] = [];
          for (const line of lines)
            if (line.trim()) {
              try {
                events.push(JSON.parse(line) as AutoLlmActivity);
              } catch {
                /* Keep raw logs available for malformed events. */
              }
            }
          this.activity.update((values) => [...values, ...events].slice(-200));
          this.activityCursor = page.nextCursor;
        }
      }
      if (this.logFollow()) await this.refreshLog();
    } catch (e) {
      if (!this.destroyed) this.connectionError.set(this.message(e));
    } finally {
      this.polling = false;
      if (!this.destroyed)
        this.timer = setTimeout(() => void this.poll(), this.retryRequested ? 0 : 2000);
      this.retryRequested = false;
    }
  }
  select(id: string): void {
    this.selection++;
    this.detailRequest++;
    this.resultsRequest++;
    this.creating.set(false);
    this.proposal.set(null);
    this.cleanupPlan.set(null);
    this.cleanupReceipt.set(null);
    this.approvalDifferences.set([]);
    this.selectedId.set(id);
    this.report.set(null);
    this.reportError.set(null);
    this.reportObjective.set('');
    this.reportMode.set('MINIMIZE');
    this.reportRequest = undefined;
    this.reportInitialized = false;
    this.startTargets.set([]);
    this.resetComparison();
    this.activeProgress.set({});
    this.summary.set(null);
    this.targetStatus.set(null);
    this.detail.set(null);
    this.detailKey = '';
    this.detailError.set(null);
    this.detailLoading.set(false);
    this.results.set(null);
    this.resultObjective.set('');
    this.resultsKey = '';
    this.resultsError.set(null);
    this.resultsLoading.set(false);
    this.artifacts.set([]);
    this.artifactsLoaded.set(false);
    this.artifactsLoading.set(false);
    this.artifactsError.set(null);
    this.recovery.set(null);
    this.recoveryError.set(null);
    this.selectLog('');
    this.progress.set(null);
    this.progressError.set(null);
    this.watchGeneration(null);
    this.inspecting.set(false);
    const campaign = this.campaign();
    if (campaign) this.config.set(structuredClone(campaign.config));
    const trials = campaign?.trials ?? [];
    this.selectedTrial.set(
      trials.find((t) => ['BLOCKED', 'INTERRUPTED'].includes(t.status))?.number ??
        trials.find((t) => !['PENDING', 'SUCCEEDED', 'FAILED'].includes(t.status))?.number ??
        trials[0]?.number ??
        1,
    );
    if (campaign && this.objectiveNames().length) void this.loadResults(this.objectiveNames()[0]);
  }
  async newCampaign(): Promise<void> {
    this.select('');
    this.creating.set(true);
    this.config.set(null);
    const selection = this.selection;
    try {
      const defaults = await this.client.defaults();
      if (selection === this.selection) this.config.set(defaults);
    } catch (e) {
      if (selection === this.selection) this.error.set(this.message(e));
    }
  }
  selectTrial(number: number): void {
    this.selectedTrial.set(number);
    this.resetComparison();
    this.progress.set(null);
    this.progressError.set(null);
    this.selectLog('');
    this.watchGeneration(null);
    this.inspecting.set(true);
    void this.refreshDetail();
  }
  async refreshDetail(force = false): Promise<void> {
    const campaign = this.campaign();
    if (!campaign) return;
    const key = JSON.stringify([
      campaign.id,
      campaign.status,
      campaign.trials.map((t) => [t.number, t.status, t.attempts]),
    ]);
    if (!force && (key === this.detailKey || this.detailLoading())) return;
    const request = ++this.detailRequest;
    this.detailLoading.set(true);
    try {
      const detail = await this.client.detail(campaign.id);
      if (request === this.detailRequest && !this.destroyed) {
        this.detail.set(detail);
        this.detailKey = key;
        this.detailError.set(null);
      }
    } catch (e) {
      if (request === this.detailRequest) this.detailError.set(this.message(e));
    } finally {
      if (request === this.detailRequest) this.detailLoading.set(false);
    }
  }
  async loadResults(objective: string): Promise<void> {
    const mode = this.objectiveModes()[objective];
    if (!mode) return;
    this.resultsRequest++;
    this.resultsKey = '';
    this.results.set(null);
    this.resultsLoading.set(false);
    this.resultObjective.set(objective.trim());
    this.resultMode.set(mode);
    this.resultsError.set(null);
    await this.refreshResults();
  }
  async refreshResults(force = false): Promise<void> {
    const campaign = this.campaign(),
      objective = this.resultObjective();
    if (!campaign || !objective || this.resultsLoading()) return;
    const key = JSON.stringify([
      campaign.id,
      objective,
      this.resultMode(),
      campaign.trials.map((t) => [t.number, t.status, t.attempts?.length]),
    ]);
    if (!force && key === this.resultsKey) return;
    const request = ++this.resultsRequest;
    this.resultsLoading.set(true);
    try {
      const results = await this.client.campaignComparison(
        campaign.id,
        objective,
        this.resultMode(),
      );
      if (request === this.resultsRequest && !this.destroyed) {
        this.results.set(results);
        this.resultsKey = key;
        this.resultsError.set(null);
      }
    } catch (e) {
      if (request === this.resultsRequest) this.resultsError.set(this.message(e));
    } finally {
      if (request === this.resultsRequest) this.resultsLoading.set(false);
    }
  }
  async refreshReport(): Promise<void> {
    const id = this.selectedId(),
      selected = this.selection;
    if (!id) return;
    const requestId = this.reportRequest;
    try {
      const status = await this.client.reportStatus(id, requestId);
      if (selected !== this.selection || requestId !== this.reportRequest || this.destroyed) return;
      this.report.set(status);
      this.reportError.set(null);
      if (!this.reportInitialized) {
        this.reportInitialized = true;
        this.reportObjective.set(status.selection.objective ?? '');
        this.reportMode.set(status.selection.mode ?? 'MINIMIZE');
      }
      if (status.state !== 'RUNNING' && status.state !== 'QUEUED') this.reportRequest = undefined;
    } catch (e) {
      if (selected === this.selection && !this.destroyed) this.reportError.set(this.message(e));
    }
  }
  selectReportObjective(objective: string): void {
    this.reportInitialized = true;
    this.reportObjective.set(objective);
    const mode = this.report()?.objectives[objective];
    if (mode) this.reportMode.set(mode);
  }
  async generateReport(): Promise<void> {
    const id = this.selectedId(),
      selected = this.selection;
    if (!id || this.reportBusy()) return;
    await this.operate(async () => {
      const objective = this.reportObjective().trim();
      const status = await this.client.generateReport(
        id,
        objective ? { objective, mode: this.reportMode() } : null,
      );
      if (selected !== this.selection || this.destroyed) return;
      this.reportRequest = status.requestId ?? undefined;
      this.report.set(status);
      this.reportError.set(null);
      this.retryNow();
    });
  }
  async refreshSummary(): Promise<void> {
    const id = this.selectedId(),
      selection = this.selection;
    if (!id) return;
    try {
      const summary = await this.client.summary(id);
      if (selection === this.selection) this.summary.set(summary);
    } catch (e) {
      if (selection === this.selection) this.error.set(this.message(e));
    }
  }
  watchGeneration(attempt: number | null): void {
    this.generationAttempt.set(attempt);
    this.activityKey = '';
    this.activity.set([]);
  }
  selectLog(path: string): void {
    this.logRequest++;
    this.logPath.set(path);
    this.logPreview.set(null);
    this.logError.set(null);
    this.logLoading.set(false);
    if (path) void this.refreshLog();
  }
  setLogLines(lines: number): void {
    if (!Number.isInteger(lines) || lines < 1 || lines > 5000) return;
    this.logLines.set(lines);
    this.selectLog(this.logPath());
  }
  async refreshLog(): Promise<void> {
    const id = this.selectedId(),
      path = this.logPath(),
      lines = this.logLines();
    if (!id || !path || this.logLoading() || this.destroyed) return;
    const request = ++this.logRequest;
    this.logLoading.set(true);
    this.logError.set(null);
    try {
      const preview = await this.client.preview(id, path, lines);
      if (request === this.logRequest && id === this.selectedId() && !this.destroyed) {
        const current = this.logPreview();
        if (current?.text !== preview.text || current?.sizeBytes !== preview.sizeBytes)
          this.logPreview.set(preview);
      }
    } catch (e) {
      if (request === this.logRequest && !this.destroyed) this.logError.set(this.message(e));
    } finally {
      if (request === this.logRequest && !this.destroyed) this.logLoading.set(false);
    }
  }
  async loadDatasets(): Promise<void> {
    const config = this.config();
    if (!config) return;
    await this.operate(async () => {
      this.datasets.set(await this.client.datasets(config));
    });
  }
  async useDataset(id: string): Promise<void> {
    const config = this.config();
    if (!config || !id) return;
    await this.operate(async () => {
      const entry = await this.client.dataset(config, id);
      this.config.set({
        ...config,
        trainingPath: entry.trainingPath,
        comparisonPath: entry.comparisonPath,
      });
      this.datasetReview.set(null);
    });
  }
  async previewDataset(): Promise<void> {
    const config = this.config();
    if (!config) return;
    this.datasetReview.set(null);
    await this.operate(async () => {
      const review = await this.client.previewDataset(config, this.datasetName());
      this.datasetReview.set(review);
    });
  }
  async publishDataset(): Promise<void> {
    const review = this.datasetReview(),
      config = this.config();
    if (!review || !config) return;
    await this.operate(async () => {
      const entry = await this.client.publishDataset(config, review.name, review.digest);
      this.datasets.set(await this.client.datasets(config));
      this.config.set({
        ...config,
        trainingPath: entry.trainingPath,
        comparisonPath: entry.comparisonPath,
      });
      this.datasetReview.set(null);
    });
  }
  selectScreening(id: string): void {
    this.selectedScreening.set(id);
    this.review.set(null);
  }
  async createScreening(inputPath: string, fraction: number): Promise<void> {
    const config = this.config();
    if (!config) return;
    await this.operate(async () => {
      const job = await this.client.createScreening(config, inputPath, fraction);
      this.screenings.update((values) => [job, ...values]);
      this.selectScreening(job.id);
      await this.client.screeningAction(job.id, 'resume');
      this.activeScreening.set(job.id);
    });
  }
  async reviewScreening(): Promise<void> {
    const id = this.selectedScreening();
    await this.operate(async () => {
      const review = await this.client.screeningReview(id);
      if (this.selectedScreening() === id) this.review.set({ ...review, jobId: id });
    });
  }
  async screeningAction(action: 'resume' | 'cancel'): Promise<void> {
    const id = this.selectedScreening();
    await this.operate(async () => {
      await this.client.screeningAction(id, action);
    });
  }
  useSelection(): void {
    const job = this.screening();
    if (!job || !['READY_FOR_REVIEW'].includes(job.status) || !job.mirror) return;
    this.config.update((config) =>
      config
        ? {
            ...config,
            trainingPath: `${job.mirror}/proposal/proposed-train.index`,
            comparisonPath: `${job.mirror}/proposal/proposed-test.index`,
          }
        : config,
    );
  }
  screeningArtifactUrl(path: string): string {
    return this.client.screeningArtifactUrl(this.selectedScreening(), path);
  }
  async refreshArtifacts(): Promise<void> {
    const id = this.selectedId(),
      selection = this.selection;
    if (!id || this.artifactsLoading()) return;
    this.artifactsLoading.set(true);
    this.artifactsError.set(null);
    try {
      const artifacts = await this.client.artifacts(id);
      if (selection === this.selection && !this.destroyed) {
        this.artifacts.set(artifacts);
        this.artifactsLoaded.set(true);
      }
    } catch (e) {
      if (selection === this.selection) this.artifactsError.set(this.message(e));
    } finally {
      if (selection === this.selection) this.artifactsLoading.set(false);
    }
  }
  async prepareCampaign(): Promise<void> {
    const config = this.config();
    if (!config) return;
    await this.operate(async () => {
      const validation = await this.client.validate(config);
      if (!validation.valid) throw new Error(validation.errors.join('; '));
      const campaign = await this.client.create(config);
      this.campaigns.update((values) => [campaign, ...values]);
      this.select(campaign.id);
      const check = await this.client.baselineCheck(campaign.id);
      this.summary.set(await this.client.summary(campaign.id));
      if (!check.withinLimit)
        throw new Error(
          `Baseline emits ${check.parameters} parameters; limit is ${check.maxParameters}. Prune the search space while preserving every seed before starting this draft.`,
        );
      if (!check.ready)
        throw new Error(
          `Baseline is not ready. Failed targets: ${check.failedTargets.join(', ')}. Inspect qualification results before selecting servers to start.`,
        );
      const proposal = await this.client.proposal(campaign.id);
      this.config.set(structuredClone(proposal.configuration));
      this.proposal.set(proposal);
    });
  }
  async reviseDataset(selection: DatasetSelection): Promise<void> {
    await this.operate(async () => {
      const campaign = await this.client.reviseDataset(this.selectedId(), selection);
      this.campaigns.update((values) => [
        campaign,
        ...values.filter((value) => value.id !== campaign.id),
      ]);
      this.select(campaign.id);
      await this.client.baselineCheck(campaign.id);
      const proposal = await this.client.proposal(campaign.id);
      this.config.set(structuredClone(proposal.configuration));
      this.proposal.set(proposal);
    });
  }
  async saveDraftChanges(): Promise<void> {
    const config = this.config();
    if (!config) return;
    await this.operate(async () => {
      const campaign = await this.client.editDraft(this.selectedId(), config);
      this.campaigns.update((values) => [
        campaign,
        ...values.filter((value) => value.id !== campaign.id),
      ]);
      this.select(campaign.id);
      await this.client.baselineCheck(campaign.id);
      const proposal = await this.client.proposal(campaign.id);
      this.config.set(structuredClone(proposal.configuration));
      this.proposal.set(proposal);
    });
  }
  async reviewProposal(targets: string[] = []): Promise<void> {
    await this.operate(async () => {
      const id = this.selectedId();
      const proposal = await this.client.proposal(id, targets);
      if (id === this.selectedId()) {
        this.config.set(structuredClone(proposal.configuration));
        this.proposal.set(proposal);
      }
    });
  }
  async amendTrials(count: number): Promise<void> {
    await this.operate(async () => {
      const proposal = await this.client.amendTrials(this.selectedId(), count);
      this.config.set(structuredClone(proposal.configuration));
      this.proposal.set(proposal);
    });
  }
  async reviewCleanup(): Promise<void> {
    const id = this.selectedId();
    await this.operate(async () => {
      const plan = await this.client.cleanupPlan({ kind: 'campaign', id });
      if (this.selectedId() === id) {
        this.cleanupPlan.set(plan);
        this.cleanupReceipt.set(null);
      }
    });
  }
  async applyCleanup(): Promise<void> {
    const plan = this.cleanupPlan();
    if (!plan || plan.selection.id !== this.selectedId()) return;
    await this.operate(async () => {
      const receipt = await this.client.cleanupApply(plan.digest);
      if (this.selectedId() === plan.selection.id) this.cleanupReceipt.set(receipt);
      if (receipt.status === 'SUCCEEDED') {
        this.campaigns.update((values) => values.filter((value) => value.id !== plan.selection.id));
        if (this.selectedId() === plan.selection.id) {
          this.selectedId.set('');
          this.cleanupPlan.set(null);
        }
      }
    });
  }
  async approveAndStart(): Promise<void> {
    const proposal = this.proposal();
    if (!proposal) return;
    await this.operate(async () => {
      if (!proposal.generation.ready) throw new Error(proposal.generation.approvalLine);
      const id = this.selectedId();
      await this.client.approve(id, proposal.digest);
      if (this.active() !== id) {
        await this.acceptReceipt(await this.client.action(id, 'start'));
        this.active.set(id);
      }
      this.proposal.set(null);
    });
  }
  async probeTarget(): Promise<void> {
    const config = this.config();
    if (!config) return;
    await this.operate(async () => {
      this.targetStatus.set(await this.client.probeTarget(config));
    });
  }
  async action(action: 'start' | 'resume' | 'cancel', targets: string[] = []): Promise<void> {
    await this.operate(async () => {
      const id = this.selectedId();
      const campaign = await this.client.action(id, action, targets);
      await this.acceptReceipt(campaign);
      if (action === 'start' || action === 'resume') this.active.set(id);
      await this.refreshRecovery();
    });
  }
  toggleStartTarget(id: string, selected: boolean): void {
    this.proposal.set(null);
    this.startTargets.update((values) =>
      selected ? [...values, id] : values.filter((value) => value !== id),
    );
  }
  private resetComparison(): void {
    this.comparisonSelection++;
    this.curveRequest++;
    this.curveCounts = '';
    this.comparison.set(null);
    this.comparisonError.set(null);
    this.comparisonUpdated.set(null);
    this.curveInstance.set('');
    this.curves.set([]);
    this.curvesLoading.set(false);
    this.curvesError.set(null);
  }
  async selectCurveInstance(instanceId: string): Promise<void> {
    this.curveRequest++;
    this.curveCounts = '';
    this.curveInstance.set(instanceId);
    this.curves.set([]);
    this.curvesLoading.set(false);
    this.curvesError.set(null);
    await this.refreshCurves();
  }
  async refreshCurves(): Promise<void> {
    const instance = this.comparisonInstances().find(
      (value) => value.instanceId === this.curveInstance(),
    );
    if (!instance || this.curvesLoading() || this.destroyed) return;
    const counts = JSON.stringify(instance.counts);
    if (counts === this.curveCounts) return;
    const request = ++this.curveRequest;
    this.curvesLoading.set(true);
    this.curvesError.set(null);
    try {
      const result = await this.client.curves(
        this.selectedId(),
        this.selectedTrial(),
        instance.instanceId,
      );
      if (request === this.curveRequest && !this.destroyed) {
        this.curves.set(result.curves);
        this.curveCounts = counts;
      }
    } catch (e) {
      if (request === this.curveRequest && !this.destroyed) this.curvesError.set(this.message(e));
    } finally {
      if (request === this.curveRequest && !this.destroyed) this.curvesLoading.set(false);
    }
  }
  async addTarget(server: AutoLlmServer): Promise<void> {
    const id = this.selectedId();
    await this.operate(async () => {
      const c = await this.client.addTarget(id, server);
      await this.acceptReceipt(c);
    });
  }
  async targetAction(
    target: string,
    action: 'drain' | 'resume' | 'requalify' | 'remove',
  ): Promise<void> {
    const id = this.selectedId();
    await this.operate(async () => {
      await this.acceptReceipt(await this.client.targetAction(id, target, action));
      await this.refreshRecovery();
    });
  }
  async refreshRecovery(): Promise<void> {
    const id = this.selectedId();
    if (!id) return;
    try {
      const recovery = await this.client.recovery(id);
      if (id === this.selectedId() && !this.destroyed) {
        this.recovery.set(recovery);
        this.recoveryError.set(null);
      }
    } catch (e) {
      if (id === this.selectedId() && !this.destroyed) {
        this.recovery.set(null);
        this.recoveryError.set(this.message(e));
      }
    }
  }
  async resumeTrial(): Promise<void> {
    const id = this.selectedId(),
      number = this.selectedTrial();
    await this.operate(async () => {
      await this.acceptReceipt(await this.client.resumeTrial(id, number));
      await this.refreshRecovery();
    });
  }
  async failTrial(reason: string): Promise<void> {
    if (!reason.trim()) return;
    const id = this.selectedId(),
      number = this.selectedTrial();
    await this.operate(async () => {
      await this.acceptReceipt(await this.client.failTrial(id, number, reason.trim()));
      await this.refreshRecovery();
    });
  }
  private async acceptReceipt(receipt: CampaignReceipt): Promise<void> {
    this.campaigns.update((values) =>
      values.map((value) =>
        value.id === receipt.id ? { ...value, status: receipt.status } : value,
      ),
    );
    let campaign: AutoLlmCampaign;
    try {
      campaign = await this.client.status(receipt.id);
    } catch {
      throw new Error(
        'Operation accepted, but the updated status is unavailable. Monitoring will retry; do not repeat the operation.',
      );
    }
    if (this.destroyed) return;
    this.campaigns.update((values) =>
      values.map((value) => (value.id === campaign.id ? campaign : value)),
    );
    if (this.inspecting() && this.selectedId() === campaign.id) await this.refreshDetail(true);
  }
  targetStale(lastSync: string | null | undefined): boolean {
    return (
      !lastSync ||
      !Number.isFinite(Date.parse(lastSync)) ||
      this.now() - Date.parse(lastSync) > 30000
    );
  }
  private async operate(callback: () => Promise<void>): Promise<void> {
    this.operations++;
    this.busy.set(true);
    this.error.set(null);
    this.approvalDifferences.set([]);
    try {
      await callback();
    } catch (e) {
      const response = (
        e as { error?: { differences?: import('../model/autollm').ApprovalDifference[] } }
      ).error;
      this.approvalDifferences.set(response?.differences ?? []);
      this.error.set(this.message(e));
    } finally {
      this.operations--;
      this.busy.set(this.operations > 0);
    }
  }
  artifactUrl(path: string): string {
    return this.client.artifactUrl(this.selectedId(), path);
  }
  private message(error: unknown): string {
    const value = error as { error?: { message?: string }; message?: string };
    return value.error?.message ?? value.message ?? 'AutoLLM request failed';
  }
}
