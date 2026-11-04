import { Injectable, inject, DestroyRef } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { firstValueFrom, timeout } from 'rxjs';
import { ComparisonCurveRow } from '../util/comparison-chart-util';
import { environment } from '../../environments/environment';
import {
  DatasetEntry,
  DatasetReview,
  ObjectiveModes,
  DatasetSelection,
  ProposalTarget,
  ProposalScreening,
  CampaignReceipt,
  CampaignComparison,
  AutoLlmComparison,
  AutoLlmArtifact,
  AutoLlmCampaign,
  AutoLlmConfig,
  AutoLlmLog,
  AutoLlmLogPreview,
  AutoLlmProgress,
  AutoLlmRecovery,
  AutoLlmServer,
  ScreeningJob,
  ScreeningReview,
} from '../model/autollm';

@Injectable({ providedIn: 'root' })
export class AutoLlmClient {
  private readonly http = inject(HttpClient);
  private readonly destroy = inject(DestroyRef);
  private readonly base = `${environment.apiBaseUrl}/api/autollm`;
  private get<T>(
    path: string,
    params: Record<string, string | number> = {},
    timeoutMillis = 10000,
  ): Promise<T> {
    return firstValueFrom(
      this.http
        .get<T>(this.base + path, { params })
        .pipe(timeout(timeoutMillis), takeUntilDestroyed(this.destroy)),
    );
  }
  private post<T>(path: string, body: unknown = {}): Promise<T> {
    return firstValueFrom(
      this.http.post<T>(this.base + path, body).pipe(takeUntilDestroyed(this.destroy)),
    );
  }
  datasets(config: AutoLlmConfig): Promise<DatasetEntry[]> {
    return this.get('/datasets', {
      repository: config.project.repository,
      artifact: config.project.artifact,
    });
  }
  dataset(config: AutoLlmConfig, id: string): Promise<DatasetEntry> {
    return this.get(
      `/datasets/${encodeURIComponent(id)}`,
      {
        repository: config.project.repository,
        artifact: config.project.artifact,
      },
      120000,
    );
  }
  previewDataset(configuration: AutoLlmConfig, name: string): Promise<DatasetReview> {
    return this.post('/datasets/preview', { configuration, name });
  }
  publishDataset(
    configuration: AutoLlmConfig,
    name: string,
    digest: string,
  ): Promise<DatasetEntry> {
    return this.post('/datasets', { configuration, name, digest });
  }
  defaults(): Promise<AutoLlmConfig> {
    return this.get('/defaults');
  }
  objectives(): Promise<ObjectiveModes> {
    return this.get('/objectives');
  }
  validate(config: AutoLlmConfig): Promise<{ valid: boolean; errors: string[] }> {
    return this.post('/validate', config);
  }
  probeTarget(config: AutoLlmConfig): Promise<Record<string, unknown>> {
    return this.post('/targets/probe', config);
  }
  targetProbe(id: string): Promise<Record<string, unknown>> {
    return this.get(`/targets/probes/${encodeURIComponent(id)}`);
  }
  list(): Promise<{ active: string; campaigns: AutoLlmCampaign[] }> {
    return this.get('/campaigns');
  }
  detail(id: string): Promise<AutoLlmCampaign> {
    return this.get(`/campaigns/${encodeURIComponent(id)}`);
  }
  status(id: string): Promise<AutoLlmCampaign> {
    return this.get(`/campaigns/${encodeURIComponent(id)}/status`);
  }
  campaignComparison(
    id: string,
    objective: string,
    mode: 'MINIMIZE' | 'MAXIMIZE',
  ): Promise<CampaignComparison> {
    return this.get(`/campaigns/${encodeURIComponent(id)}/comparison`, {
      objective,
      mode,
      ranking: 'NONE',
    });
  }
  async create(config: AutoLlmConfig): Promise<AutoLlmCampaign> {
    const receipt = await this.post<{ id: string }>('/campaigns', config);
    return this.get(`/campaigns/${encodeURIComponent(receipt.id)}/status`);
  }
  proposal(
    id: string,
    targets: string[] = [],
  ): Promise<{
    digest: string;
    configuration: AutoLlmConfig;
    generation: import('../model/autollm').GenerationSettings;
    training: string[];
    testing: string[];
    screening: ProposalScreening;
    datasetOrigin?: unknown;
    overlap?: number;
    targets: Record<string, ProposalTarget>;
  }> {
    return this.post(`/campaigns/${encodeURIComponent(id)}/proposal`, { targets });
  }
  async reviseDataset(id: string, selection: DatasetSelection): Promise<AutoLlmCampaign> {
    const receipt = await firstValueFrom(
      this.http
        .put<{ id: string }>(`${this.base}/campaigns/${encodeURIComponent(id)}/dataset`, selection)
        .pipe(takeUntilDestroyed(this.destroy)),
    );
    return this.get(`/campaigns/${encodeURIComponent(receipt.id)}/status`);
  }
  async editDraft(id: string, config: AutoLlmConfig): Promise<AutoLlmCampaign> {
    const receipt = await firstValueFrom(
      this.http
        .put<{ id: string }>(`${this.base}/campaigns/${encodeURIComponent(id)}/draft`, config)
        .pipe(takeUntilDestroyed(this.destroy)),
    );
    return this.get(`/campaigns/${encodeURIComponent(receipt.id)}/status`);
  }
  amendTrials(id: string, trials: number): Promise<Awaited<ReturnType<AutoLlmClient['proposal']>>> {
    return this.post(`/campaigns/${encodeURIComponent(id)}/amend-trials`, { trials });
  }
  approve(id: string, digest: string): Promise<unknown> {
    return this.post(`/campaigns/${encodeURIComponent(id)}/approve`, { digest });
  }
  baselineCheck(id: string): Promise<{
    parameters: number;
    maxParameters: number;
    withinLimit: boolean;
    ready: boolean;
    qualifiedTargets: string[];
    failedTargets: string[];
  }> {
    return this.post(`/campaigns/${encodeURIComponent(id)}/baseline-check`);
  }
  action(
    id: string,
    action: 'start' | 'resume' | 'cancel',
    targets: string[] = [],
  ): Promise<CampaignReceipt> {
    return this.post(
      `/campaigns/${encodeURIComponent(id)}/${action}`,
      action === 'start' ? { targets } : {},
    );
  }
  reportStatus(
    id: string,
    requestId?: string,
  ): Promise<import('../model/autollm').AutoLlmReportStatus> {
    return this.get(`/campaigns/${encodeURIComponent(id)}/report`, requestId ? { requestId } : {});
  }
  generateReport(
    id: string,
    selection: { objective: string; mode: 'MINIMIZE' | 'MAXIMIZE' } | null,
  ): Promise<import('../model/autollm').AutoLlmReportStatus> {
    return this.post(`/campaigns/${encodeURIComponent(id)}/report`, selection ?? {});
  }
  addTarget(id: string, server: AutoLlmServer): Promise<CampaignReceipt> {
    return firstValueFrom(
      this.http
        .put<CampaignReceipt>(
          `${this.base}/campaigns/${encodeURIComponent(id)}/targets/${encodeURIComponent(server.id)}`,
          server,
        )
        .pipe(takeUntilDestroyed(this.destroy)),
    );
  }
  targetAction(
    id: string,
    target: string,
    action: 'drain' | 'resume' | 'requalify' | 'remove',
  ): Promise<CampaignReceipt> {
    const path = `/campaigns/${encodeURIComponent(id)}/targets/${encodeURIComponent(target)}`;
    return action === 'remove'
      ? firstValueFrom(
          this.http
            .delete<CampaignReceipt>(this.base + path)
            .pipe(takeUntilDestroyed(this.destroy)),
        )
      : this.post(`${path}/${action}`);
  }
  progress(id: string, trial: number): Promise<AutoLlmProgress> {
    return this.get(`/campaigns/${encodeURIComponent(id)}/trials/${trial}/progress`);
  }
  recovery(id: string): Promise<AutoLlmRecovery> {
    return this.get(`/campaigns/${encodeURIComponent(id)}/recovery`);
  }
  resumeTrial(id: string, trial: number): Promise<CampaignReceipt> {
    return this.post(`/campaigns/${encodeURIComponent(id)}/trials/${trial}/resume`);
  }
  failTrial(id: string, trial: number, reason: string): Promise<CampaignReceipt> {
    return this.post(`/campaigns/${encodeURIComponent(id)}/trials/${trial}/fail`, { reason });
  }
  effective(config: AutoLlmConfig): Promise<import('../model/autollm').EffectiveConfiguration> {
    return this.post('/effective-config', config);
  }
  cleanupPlan(
    selection: import('../model/autollm').CleanupSelection,
  ): Promise<import('../model/autollm').CleanupPlan> {
    return this.post('/cleanup/plan', selection);
  }
  cleanupApply(digest: string): Promise<import('../model/autollm').CleanupReceipt> {
    return this.post('/cleanup/apply', { digest });
  }
  cleanupStatus(digest: string): Promise<import('../model/autollm').CleanupReceipt> {
    return this.get(`/cleanup/status?digest=${encodeURIComponent(digest)}`);
  }
  summary(id: string): Promise<Record<string, unknown>> {
    return this.get(`/campaigns/${encodeURIComponent(id)}/summary`);
  }
  activity(id: string, trial: number, attempt: number, cursor: number): Promise<AutoLlmLog> {
    return this.get(`/campaigns/${encodeURIComponent(id)}/trials/${trial}/activity`, {
      attempt,
      cursor,
    });
  }
  comparison(id: string, trial: number): Promise<AutoLlmComparison> {
    return this.get(`/campaigns/${encodeURIComponent(id)}/trials/${trial}/comparison`);
  }
  curves(
    id: string,
    trial: number,
    instanceId: string,
  ): Promise<{ curves: ComparisonCurveRow[]; sampled: boolean }> {
    return this.get(`/campaigns/${encodeURIComponent(id)}/trials/${trial}/curves`, { instanceId });
  }
  artifacts(id: string): Promise<AutoLlmArtifact[]> {
    return this.get(`/campaigns/${encodeURIComponent(id)}/artifacts`);
  }
  preview(id: string, path: string, lines: number): Promise<AutoLlmLogPreview> {
    return this.get(`/campaigns/${encodeURIComponent(id)}/preview`, { path, lines });
  }
  artifactUrl(id: string, path: string): string {
    return `${this.base}/campaigns/${encodeURIComponent(id)}/artifact?${new URLSearchParams({ path })}`;
  }
  screenings(): Promise<{ active: string; jobs: ScreeningJob[] }> {
    return this.get('/screenings');
  }
  createScreening(
    config: AutoLlmConfig,
    inputPath: string,
    trainingFraction: number,
  ): Promise<ScreeningJob> {
    return this.post('/screenings', {
      config,
      inputPath,
      trainingFraction,
      settings: {
        sotaFile: config.sotaFile,
        python:
          config.execution.ssh?.servers.find(
            (server) => server.id === config.execution.ssh?.screeningServerId,
          )?.environment?.['MORK_SELECTOR_PYTHON'] ?? 'python3',
      },
    });
  }
  screeningAction(id: string, action: 'resume' | 'cancel'): Promise<ScreeningJob> {
    return this.post(`/screenings/${encodeURIComponent(id)}/${action}`, {});
  }
  screeningReview(id: string): Promise<ScreeningReview> {
    return this.get(`/screenings/${encodeURIComponent(id)}/review`);
  }
  screeningArtifactUrl(id: string, path: string): string {
    return `${this.base}/screenings/${encodeURIComponent(id)}/artifact?${new URLSearchParams({ path })}`;
  }
}
