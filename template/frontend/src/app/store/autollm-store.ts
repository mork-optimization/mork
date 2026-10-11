import { DestroyRef, Injectable, computed, inject, signal } from '@angular/core';
import { AutoLlmClient } from '../service/autollm-client';
import { AutoLlmArtifact, AutoLlmCampaign, AutoLlmConfig } from '../model/autollm';
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
  readonly progress = signal<Record<string, unknown> | null>(null);
  readonly tuning = computed(() => this.progress()?.['budget'] ? this.progress() as unknown as AutoconfigStatus : null);
  readonly comparison = signal<Record<string, unknown> | null>(null);
  readonly artifacts = signal<AutoLlmArtifact[]>([]);
  readonly logPath = signal('');
  readonly logLines = signal<string[]>([]);
  readonly error = signal<string | null>(null);
  readonly connectionError = signal<string | null>(null);
  readonly busy = signal(false);
  private timer: ReturnType<typeof setTimeout> | null = null;
  private started = false;
  private destroyed = false;
  private logOffset = 0;
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
      const data = await this.client.list();
      if (this.destroyed) return;
      this.connectionError.set(null);
      this.campaigns.set(data.campaigns); this.active.set(data.active);
      if (!this.selectedId()) this.selectedId.set(data.active || data.campaigns[0]?.id || '');
      if (this.trial()) {
        const id = this.selectedId(), trial = this.selectedTrial();
        const [progress, comparison] = await Promise.all([this.client.progress(id, trial), this.client.comparison(id, trial)]);
        if (this.selectedId() === id && this.selectedTrial() === trial && !this.destroyed) {
          this.progress.set(progress); this.comparison.set(comparison);
        }
      }
      if (this.logPath()) {
        const id = this.selectedId(), path = this.logPath(), offset = this.logOffset;
        const log = await this.client.log(id, path, offset);
        if (id === this.selectedId() && path === this.logPath() && offset === this.logOffset && !this.destroyed) {
          this.logOffset = log.nextOffset;
          this.logLines.update(lines => [...lines, ...log.lines].slice(-1000));
        }
      }
    } catch (e) { if (!this.destroyed) this.connectionError.set(this.message(e)); }
    finally { if (!this.destroyed) this.timer = setTimeout(() => void this.poll(), 2000); }
  }
  select(id: string): void { this.selectedId.set(id); this.selectedTrial.set(1); this.artifacts.set([]); this.selectLog(''); this.progress.set(null); this.comparison.set(null); }
  selectTrial(number: number): void { this.selectedTrial.set(number); this.progress.set(null); this.comparison.set(null); this.selectLog(''); }
  selectLog(path: string): void { this.logPath.set(path); this.logOffset = 0; this.logLines.set([]); }
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
      await this.client.action(campaign.id, 'start'); this.active.set(campaign.id);
    });
  }
  async action(action: 'start' | 'resume' | 'cancel' | 'report'): Promise<void> {
    await this.operate(async () => { await this.client.action(this.selectedId(), action); if (action === 'start' || action === 'resume') this.active.set(this.selectedId()); });
  }
  private async operate(callback: () => Promise<void>): Promise<void> {
    this.busy.set(true); this.error.set(null);
    try { await callback(); } catch (e) { this.error.set(this.message(e)); } finally { this.busy.set(false); }
  }
  artifactUrl(path: string): string { return this.client.artifactUrl(this.selectedId(), path); }
  private message(error: unknown): string {
    const value = error as { error?: { message?: string }; message?: string };
    return value.error?.message ?? value.message ?? 'AutoLLM request failed';
  }
}
