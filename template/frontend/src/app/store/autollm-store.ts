import { DestroyRef, Injectable, computed, inject, signal } from '@angular/core';
import { AutoLlmClient } from '../service/autollm-client';
import { AutoLlmActivity, AutoLlmArtifact, AutoLlmCampaign, AutoLlmConfig, AutoLlmProgress, ScreeningJob, ScreeningReview } from '../model/autollm';
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
  readonly logText = signal('');
  readonly error = signal<string | null>(null);
  readonly connectionError = signal<string | null>(null);
  readonly busy = signal(false);
  readonly targetStatus = signal<Record<string, unknown> | null>(null);
  private timer: ReturnType<typeof setTimeout> | null = null;
  private started = false;
  private destroyed = false;
  private logOffset = 0;
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
      if (this.trial()) {
        const id = this.selectedId(), trial = this.selectedTrial();
        const [progress, comparison] = await Promise.all([this.client.progress(id, trial), this.client.comparison(id, trial)]);
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
      if (this.logPath()) {
        const id = this.selectedId(), path = this.logPath(), offset = this.logOffset;
        const log = await this.client.log(id, path, offset);
        if (id === this.selectedId() && path === this.logPath() && offset === this.logOffset && !this.destroyed) {
          this.logOffset = log.nextCursor;
          this.logText.update(text => ((log.reset ? '' : text) + log.text).slice(-262144));
        }
      }
    } catch (e) { if (!this.destroyed) this.connectionError.set(this.message(e)); }
    finally { if (!this.destroyed) this.timer = setTimeout(() => void this.poll(), 2000); }
  }
  select(id: string): void { this.selectedId.set(id); this.selectedTrial.set(1); this.artifacts.set([]); this.selectLog(''); this.progress.set(null); this.comparison.set(null); this.watchGeneration(null); }
  selectTrial(number: number): void { this.selectedTrial.set(number); this.progress.set(null); this.comparison.set(null); this.selectLog(''); this.watchGeneration(null); }
  watchGeneration(attempt: number | null): void { this.generationAttempt.set(attempt); this.activityKey = ''; this.activity.set([]); }
  selectLog(path: string): void { this.logPath.set(path); this.logOffset = 0; this.logText.set(''); }
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
    await this.operate(async () => { await this.client.action(this.selectedId(), action); if (action === 'start' || action === 'resume') this.active.set(this.selectedId()); });
  }
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
