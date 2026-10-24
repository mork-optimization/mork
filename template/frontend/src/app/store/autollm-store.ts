import { DestroyRef, Injectable, computed, inject, signal } from '@angular/core';
import { AutoLlmClient } from '../service/autollm-client';
import { AutoLlmActivity, AutoLlmArtifact, AutoLlmCampaign, AutoLlmConfig, AutoLlmLogPreview, AutoLlmProgress, AutoLlmRecovery, AutoLlmServer, ScreeningJob, ScreeningReview } from '../model/autollm';
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
  readonly campaign = computed(() => this.campaigns().find(c => c.id === this.selectedId()) ?? null);
  readonly trial = computed(() => this.campaign()?.trials.find(t => t.number === this.selectedTrial()) ?? null);
  readonly completed = computed(() => this.campaign()?.trials.filter(t => ['SUCCEEDED', 'FAILED'].includes(t.status)).length ?? 0);
  readonly progress = signal<AutoLlmProgress | null>(null);
  readonly activeProgress = signal<Record<number, { progress?: AutoLlmProgress; stale: boolean }>>({});
  readonly targets = computed(() => Object.entries(this.campaign()?.targets ?? {}).map(([id, target]) => ({ id, ...target })));
  readonly activeTrials = computed(() => this.campaign()?.trials.filter(trial => !['PENDING', 'SUCCEEDED', 'FAILED'].includes(trial.status)) ?? []);
  readonly now = signal(Date.now());
  readonly tuning = computed(() => this.progress()?.workload?.['budget'] ? this.progress()!.workload as unknown as AutoconfigStatus : null);
  readonly generation = computed(() => this.progress()?.generation ?? null);
  readonly generationAttempt = signal<number | null>(null);
  readonly conversationAttempt = computed(() => {
    const attempts = this.trial()?.attempts?.filter(attempt => ['GENERATION', 'REPAIR'].includes(attempt.stage)) ?? [];
    return attempts.find(attempt => attempt.number === this.generationAttempt()) ?? attempts.at(-1) ?? null;
  });
  readonly activity = signal<AutoLlmActivity[]>([]);
  readonly screenings = signal<ScreeningJob[]>([]);
  readonly activeScreening = signal('');
  readonly selectedScreening = signal('');
  readonly screening = computed(() => this.screenings().find(job => job.id === this.selectedScreening()) ?? null);
  readonly review = signal<ScreeningReview | null>(null);
  readonly summary = signal<Record<string, unknown> | null>(null);
  readonly comparison = signal<Record<string, unknown> | null>(null);
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
  private destroyed = false;
  private logRequest = 0;
  private activityKey = '';
  private activityCursor = 0;
  private activityPending = '';
  private operations = 0;
  constructor() { this.destroy.onDestroy(() => { this.destroyed = true; if (this.timer) clearTimeout(this.timer); }); }
  async start(): Promise<void> {
    if (this.started) return; this.started = true;
    try { this.config.set(await this.client.defaults()); } catch (e) { this.connectionError.set(this.message(e)); }
    await this.poll();
  }
  private async poll(): Promise<void> {
    if (this.destroyed) return;
    try {
      if (!this.config()) this.config.set(await this.client.defaults());
      const [data, screenings] = await Promise.all([this.client.list(), this.client.screenings()]);
      if (this.destroyed) return;
      this.connectionError.set(null);
      this.campaigns.set(data.campaigns); this.active.set(data.active);
      this.screenings.set(screenings.jobs); this.activeScreening.set(screenings.active);
      if (!this.selectedScreening()) this.selectedScreening.set(screenings.active || screenings.jobs[0]?.id || '');
      if (!this.selectedId()) this.selectedId.set(data.active || data.campaigns[0]?.id || '');
      this.now.set(Date.now());
      const campaignId = this.selectedId(), activeTrials = this.activeTrials();
      const [updates] = await Promise.all([Promise.allSettled(activeTrials.map(trial => this.client.progress(campaignId, trial.number))), this.refreshRecovery()]);
      if (this.selectedId() === campaignId && !this.destroyed) {
        const next: Record<number, { progress?: AutoLlmProgress; stale: boolean }> = {};
        updates.forEach((update, index) => {
          const number = activeTrials[index].number;
          next[number] = update.status === 'fulfilled' ? { progress: update.value, stale: false } : { progress: this.activeProgress()[number]?.progress, stale: true };
        });
        this.activeProgress.set(next);
      }
      if (this.trial()) {
        const id = this.selectedId(), trial = this.selectedTrial();
        const [progress, comparison] = await Promise.all([this.activeProgress()[trial]?.progress ?? this.client.progress(id, trial), this.client.comparison(id, trial)]);
        if (this.selectedId() === id && this.selectedTrial() === trial && !this.destroyed) {
          this.progress.set(progress); this.comparison.set(comparison);
        }
      }
      const attempt = this.conversationAttempt();
      if (attempt) {
        const id = this.selectedId(), trial = this.selectedTrial(), key = `${id}/${trial}/${attempt.number}`;
        if (this.activityKey !== key) { this.activityKey = key; this.activityCursor = 0; this.activityPending = ''; this.activity.set([]); }
        const cursor = this.activityCursor;
        const page = await this.client.activity(id, trial, attempt.number, cursor);
        if (this.activityKey === key && this.selectedId() === id && this.selectedTrial() === trial && this.conversationAttempt()?.number === attempt.number && !this.destroyed) {
          if (page.reset) { this.activity.set([]); this.activityPending = ''; }
          const lines = (this.activityPending + page.text).split('\n'); this.activityPending = lines.pop() ?? '';
          const events: AutoLlmActivity[] = [];
          for (const line of lines) if (line.trim()) { try { events.push(JSON.parse(line) as AutoLlmActivity); } catch { /* Keep raw logs available for malformed events. */ } }
          this.activity.update(values => [...values, ...events].slice(-200)); this.activityCursor = page.nextCursor;
        }
      }
      if (this.logFollow()) await this.refreshLog();
    } catch (e) { if (!this.destroyed) this.connectionError.set(this.message(e)); }
    finally { if (!this.destroyed) this.timer = setTimeout(() => void this.poll(), 2000); }
  }
  select(id: string): void { this.selectedId.set(id); this.selectedTrial.set(1); this.artifacts.set([]); this.recovery.set(null); this.recoveryError.set(null); this.selectLog(''); this.progress.set(null); this.comparison.set(null); this.watchGeneration(null); }
  selectTrial(number: number): void { this.selectedTrial.set(number); this.progress.set(null); this.comparison.set(null); this.selectLog(''); this.watchGeneration(null); }
  watchGeneration(attempt: number | null): void { this.generationAttempt.set(attempt); this.activityKey = ''; this.activity.set([]); }
  selectLog(path: string): void {
    this.logRequest++; this.logPath.set(path); this.logPreview.set(null); this.logError.set(null); this.logLoading.set(false);
    if (path) void this.refreshLog();
  }
  setLogLines(lines: number): void {
    if (!Number.isInteger(lines) || lines < 1 || lines > 5000) return;
    this.logLines.set(lines); this.selectLog(this.logPath());
  }
  async refreshLog(): Promise<void> {
    const id = this.selectedId(), path = this.logPath(), lines = this.logLines();
    if (!id || !path || this.logLoading() || this.destroyed) return;
    const request = ++this.logRequest;
    this.logLoading.set(true); this.logError.set(null);
    try {
      const preview = await this.client.preview(id, path, lines);
      if (request === this.logRequest && id === this.selectedId() && !this.destroyed) {
        const current = this.logPreview();
        if (current?.text !== preview.text || current?.sizeBytes !== preview.sizeBytes) this.logPreview.set(preview);
      }
    } catch (e) {
      if (request === this.logRequest && !this.destroyed) this.logError.set(this.message(e));
    } finally {
      if (request === this.logRequest && !this.destroyed) this.logLoading.set(false);
    }
  }
  selectScreening(id: string): void { this.selectedScreening.set(id); this.review.set(null); }
  async createScreening(inputPath: string, fraction: number): Promise<void> {
    const config = this.config(); if (!config) return;
    await this.operate(async () => {
      const job = await this.client.createScreening(config, inputPath, fraction); this.screenings.update(values => [job, ...values]); this.selectScreening(job.id);
      await this.client.screeningAction(job.id, 'resume'); this.activeScreening.set(job.id);
    });
  }
  async reviewScreening(): Promise<void> {
    const id = this.selectedScreening();
    await this.operate(async () => { const review = await this.client.screeningReview(id); if (this.selectedScreening() === id) this.review.set({ ...review, jobId: id }); });
  }
  async screeningAction(action: 'resume' | 'cancel' | 'approve'): Promise<void> {
    const id = this.selectedScreening(), review = this.review();
    if (action === 'approve' && review?.jobId !== id) return;
    await this.operate(async () => { await this.client.screeningAction(id, action, review?.proposalSha256); });
  }
  useSelection(): void {
    const job = this.screening(); if (job?.status !== 'APPROVED' || !job.mirror) return;
    this.config.update(config => config ? { ...config, trainingPath: `${job.mirror}/proposal/train.index`, comparisonPath: `${job.mirror}/proposal/test.index` } : config);
  }
  screeningArtifactUrl(path: string): string { return this.client.screeningArtifactUrl(this.selectedScreening(), path); }
  async refreshArtifacts(): Promise<void> {
    if (!this.selectedId()) return;
    const id = this.selectedId();
    try { const artifacts = await this.client.artifacts(id); if (id === this.selectedId() && !this.destroyed) this.artifacts.set(artifacts); } catch (e) { this.error.set(this.message(e)); }
  }
  async createAndStart(): Promise<void> {
    const config = this.config(); if (!config) return;
    await this.operate(async () => {
      const validation = await this.client.validate(config);
      if (!validation.valid) throw new Error(validation.errors.join('; '));
      const campaign = await this.client.create(config); this.campaigns.update(values => [campaign, ...values]); this.select(campaign.id);
      const check = await this.client.baselineCheck(campaign.id);
      this.summary.set(await this.client.summary(campaign.id));
      if (!check.withinLimit) throw new Error(`Baseline emits ${check.parameters} parameters; limit is ${check.maxParameters}. Prune the search space while preserving every seed before starting this draft.`);
      await this.client.action(campaign.id, 'start'); this.active.set(campaign.id);
    });
  }
  async probeTarget(): Promise<void> {
    const config = this.config(); if (!config) return;
    await this.operate(async () => { this.targetStatus.set(await this.client.probeTarget(config)); });
  }
  async action(action: 'start' | 'resume' | 'cancel' | 'report'): Promise<void> {
    await this.operate(async () => { const id = this.selectedId(); const campaign = await this.client.action(id, action); this.updateCampaign(campaign); if (action === 'start' || action === 'resume') this.active.set(id); await this.refreshRecovery(); });
  }
  async addTarget(server: AutoLlmServer): Promise<void> {
    const id = this.selectedId();
    await this.operate(async () => { const c = await this.client.addTarget(id, server); this.campaigns.update(values => values.map(value => value.id === id ? c : value)); });
  }
  async targetAction(target: string, action: 'drain' | 'resume' | 'requalify' | 'remove'): Promise<void> {
    const id = this.selectedId();
    await this.operate(async () => { this.updateCampaign(await this.client.targetAction(id, target, action)); await this.refreshRecovery(); });
  }
  async refreshRecovery(): Promise<void> {
    const id = this.selectedId(); if (!id) return;
    try {
      const recovery = await this.client.recovery(id);
      if (id === this.selectedId() && !this.destroyed) { this.recovery.set(recovery); this.recoveryError.set(null); }
    } catch (e) {
      if (id === this.selectedId() && !this.destroyed) { this.recovery.set(null); this.recoveryError.set(this.message(e)); }
    }
  }
  async resumeTrial(): Promise<void> {
    const id = this.selectedId(), number = this.selectedTrial();
    await this.operate(async () => { this.updateCampaign(await this.client.resumeTrial(id, number)); await this.refreshRecovery(); });
  }
  async failTrial(reason: string): Promise<void> {
    if (!reason.trim()) return;
    const id = this.selectedId(), number = this.selectedTrial();
    await this.operate(async () => { this.updateCampaign(await this.client.failTrial(id, number, reason.trim())); await this.refreshRecovery(); });
  }
  private updateCampaign(campaign: AutoLlmCampaign): void {
    this.campaigns.update(values => values.map(value => value.id === campaign.id ? campaign : value));
  }
  targetStale(lastSync: string | null | undefined): boolean { return !lastSync || this.now() - Date.parse(lastSync) > 30000; }
  private async operate(callback: () => Promise<void>): Promise<void> {
    this.operations++; this.busy.set(true); this.error.set(null);
    try { await callback(); } catch (e) { this.error.set(this.message(e)); } finally { this.operations--; this.busy.set(this.operations > 0); }
  }
  artifactUrl(path: string): string { return this.client.artifactUrl(this.selectedId(), path); }
  private message(error: unknown): string {
    const value = error as { error?: { message?: string }; message?: string };
    return value.error?.message ?? value.message ?? 'AutoLLM request failed';
  }
}
