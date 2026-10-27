import { Injectable, inject, DestroyRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { firstValueFrom, timeout } from 'rxjs';
import { environment } from '../../environments/environment';
import { AutoLlmArtifact, AutoLlmCampaign, AutoLlmConfig, AutoLlmLog, AutoLlmLogPreview, AutoLlmProgress, AutoLlmRecovery, AutoLlmServer, ScreeningJob, ScreeningReview } from '../model/autollm';

@Injectable({ providedIn: 'root' })
export class AutoLlmClient {
  private readonly http = inject(HttpClient);
  private readonly destroy = inject(DestroyRef);
  private readonly base = `${environment.apiBaseUrl}/api/autollm`;
  private get<T>(path: string, params: Record<string, string | number> = {}): Promise<T> {
    return firstValueFrom(this.http.get<T>(this.base + path, { params }).pipe(timeout(10000), takeUntilDestroyed(this.destroy)));
  }
  private post<T>(path: string, body: unknown = {}): Promise<T> {
    return firstValueFrom(this.http.post<T>(this.base + path, body).pipe(takeUntilDestroyed(this.destroy)));
  }
  defaults(): Promise<AutoLlmConfig> { return this.get('/defaults'); }
  validate(config: AutoLlmConfig): Promise<{ valid: boolean; errors: string[] }> { return this.post('/validate', config); }
  probeTarget(config: AutoLlmConfig): Promise<Record<string, unknown>> { return this.post('/targets/probe', config); }
  targetProbe(id: string): Promise<Record<string, unknown>> { return this.get(`/targets/probes/${encodeURIComponent(id)}`); }
  list(): Promise<{ active: string; campaigns: AutoLlmCampaign[] }> { return this.get('/campaigns'); }
  create(config: AutoLlmConfig): Promise<AutoLlmCampaign> { return this.post('/campaigns', config); }
  baselineCheck(id: string): Promise<{ parameters: number; maxParameters: number; withinLimit: boolean; ready: boolean; qualifiedTargets: string[]; failedTargets: string[] }> { return this.post(`/campaigns/${encodeURIComponent(id)}/baseline-check`); }
  action(id: string, action: 'start' | 'resume' | 'cancel' | 'report', targets: string[] = []): Promise<AutoLlmCampaign> { return this.post(`/campaigns/${encodeURIComponent(id)}/${action}`, action === 'start' ? { targets } : {}); }
  addTarget(id: string, server: AutoLlmServer): Promise<AutoLlmCampaign> {
    return firstValueFrom(this.http.put<AutoLlmCampaign>(`${this.base}/campaigns/${encodeURIComponent(id)}/targets/${encodeURIComponent(server.id)}`, server).pipe(takeUntilDestroyed(this.destroy)));
  }
  targetAction(id: string, target: string, action: 'drain' | 'resume' | 'requalify' | 'remove'): Promise<AutoLlmCampaign> {
    const path = `/campaigns/${encodeURIComponent(id)}/targets/${encodeURIComponent(target)}`;
    return action === 'remove' ? firstValueFrom(this.http.delete<AutoLlmCampaign>(this.base + path).pipe(takeUntilDestroyed(this.destroy))) : this.post(`${path}/${action}`);
  }
  progress(id: string, trial: number): Promise<AutoLlmProgress> { return this.get(`/campaigns/${encodeURIComponent(id)}/trials/${trial}/progress`); }
  recovery(id: string): Promise<AutoLlmRecovery> { return this.get(`/campaigns/${encodeURIComponent(id)}/recovery`); }
  resumeTrial(id: string, trial: number): Promise<AutoLlmCampaign> { return this.post(`/campaigns/${encodeURIComponent(id)}/trials/${trial}/resume`); }
  failTrial(id: string, trial: number, reason: string): Promise<AutoLlmCampaign> { return this.post(`/campaigns/${encodeURIComponent(id)}/trials/${trial}/fail`, { reason }); }
  summary(id: string): Promise<Record<string, unknown>> { return this.get(`/campaigns/${encodeURIComponent(id)}/summary`); }
  activity(id: string, trial: number, attempt: number, cursor: number): Promise<AutoLlmLog> { return this.get(`/campaigns/${encodeURIComponent(id)}/trials/${trial}/activity`, { attempt, cursor }); }
  comparison(id: string, trial: number): Promise<Record<string, unknown>> { return this.get(`/campaigns/${encodeURIComponent(id)}/trials/${trial}/comparison`); }
  curves(id: string, trial: number, cursor: number): Promise<{ curves: unknown[]; nextCursor: number; more: boolean }> { return this.get(`/campaigns/${encodeURIComponent(id)}/trials/${trial}/curves`, { cursor }); }
  artifacts(id: string): Promise<AutoLlmArtifact[]> { return this.get(`/campaigns/${encodeURIComponent(id)}/artifacts`); }
  preview(id: string, path: string, lines: number): Promise<AutoLlmLogPreview> { return this.get(`/campaigns/${encodeURIComponent(id)}/preview`, { path, lines }); }
  artifactUrl(id: string, path: string): string { return `${this.base}/campaigns/${encodeURIComponent(id)}/artifact?${new URLSearchParams({ path })}`; }
  screenings(): Promise<{ active: string; jobs: ScreeningJob[] }> { return this.get('/screenings'); }
  createScreening(config: AutoLlmConfig, inputPath: string, trainingFraction: number): Promise<ScreeningJob> {
    return this.post('/screenings', { config, inputPath, trainingFraction, settings: { sotaFile: config.sotaFile, python: config.execution.ssh?.servers.find(server => server.id === config.execution.ssh?.screeningServerId)?.environment?.['MORK_SELECTOR_PYTHON'] ?? 'python3' } });
  }
  screeningAction(id: string, action: 'resume' | 'cancel' | 'approve', proposalSha256?: string): Promise<ScreeningJob> { return this.post(`/screenings/${encodeURIComponent(id)}/${action}`, { proposalSha256 }); }
  screeningReview(id: string): Promise<ScreeningReview> { return this.get(`/screenings/${encodeURIComponent(id)}/review`); }
  screeningArtifactUrl(id: string, path: string): string { return `${this.base}/screenings/${encodeURIComponent(id)}/artifact?${new URLSearchParams({ path })}`; }
}
