import { Injectable, inject, DestroyRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';
import { AutoLlmArtifact, AutoLlmCampaign, AutoLlmConfig, AutoLlmLog } from '../model/autollm';

@Injectable({ providedIn: 'root' })
export class AutoLlmClient {
  private readonly http = inject(HttpClient);
  private readonly destroy = inject(DestroyRef);
  private readonly base = `${environment.apiBaseUrl}/api/autollm`;
  private get<T>(path: string, params: Record<string, string | number> = {}): Promise<T> {
    return firstValueFrom(this.http.get<T>(this.base + path, { params }).pipe(takeUntilDestroyed(this.destroy)));
  }
  private post<T>(path: string, body: unknown = {}): Promise<T> {
    return firstValueFrom(this.http.post<T>(this.base + path, body).pipe(takeUntilDestroyed(this.destroy)));
  }
  defaults(): Promise<AutoLlmConfig> { return this.get('/defaults'); }
  validate(config: AutoLlmConfig): Promise<{ valid: boolean; errors: string[] }> { return this.post('/validate', config); }
  probeTarget(config: AutoLlmConfig): Promise<Record<string, unknown>> { return this.post('/targets/probe', config); }
  list(): Promise<{ active: string; campaigns: AutoLlmCampaign[] }> { return this.get('/campaigns'); }
  create(config: AutoLlmConfig): Promise<AutoLlmCampaign> { return this.post('/campaigns', config); }
  baselineCheck(id: string): Promise<{ parameters: number; maxParameters: number; withinLimit: boolean }> { return this.post(`/campaigns/${encodeURIComponent(id)}/baseline-check`); }
  action(id: string, action: 'start' | 'resume' | 'cancel' | 'report'): Promise<AutoLlmCampaign> { return this.post(`/campaigns/${encodeURIComponent(id)}/${action}`); }
  progress(id: string, trial: number): Promise<Record<string, unknown>> { return this.get(`/campaigns/${encodeURIComponent(id)}/trials/${trial}/progress`); }
  comparison(id: string, trial: number): Promise<Record<string, unknown>> { return this.get(`/campaigns/${encodeURIComponent(id)}/trials/${trial}/comparison`); }
  artifacts(id: string): Promise<AutoLlmArtifact[]> { return this.get(`/campaigns/${encodeURIComponent(id)}/artifacts`); }
  log(id: string, path: string, offset: number): Promise<AutoLlmLog> { return this.get(`/campaigns/${encodeURIComponent(id)}/log`, { path, offset, limit: 200 }); }
  artifactUrl(id: string, path: string): string { return `${this.base}/campaigns/${encodeURIComponent(id)}/artifact?${new URLSearchParams({ path })}`; }
}
