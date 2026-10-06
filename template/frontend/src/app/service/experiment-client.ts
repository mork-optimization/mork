import { DestroyRef, Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ArtifactManifest,
  InstanceDetail,
  InstanceQuery,
  InstanceSummary,
  Issue,
  MonitorStatus,
  NormalStatus,
  Page,
  ResultDetail,
  ResultQuery,
  ResultView,
} from '../model/normal-experiment';

@Injectable({ providedIn: 'root' })
export class ExperimentClient {
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);
  private get<T>(path: string, params: Record<string, string | number> = {}): Promise<T> {
    return firstValueFrom(
      this.http
        .get<T>(`${environment.apiBaseUrl}${path}`, { params })
        .pipe(takeUntilDestroyed(this.destroyRef)),
    );
  }
  getMode(): Promise<MonitorStatus> {
    return this.get('/api/monitor/status');
  }
  getStatus(): Promise<NormalStatus> {
    return this.get('/api/experiments/status');
  }
  getInstances(runId: string, query: InstanceQuery): Promise<Page<InstanceSummary>> {
    return this.get('/api/experiments/instances', { runId, ...query });
  }
  getInstance(runId: string, id: string, objective: string): Promise<InstanceDetail> {
    return this.get(`/api/experiments/instances/${encodeURIComponent(id)}`, { runId, objective });
  }
  getResults(runId: string, query: ResultQuery): Promise<Page<ResultView>> {
    const params: Record<string, string | number> = {
      runId,
      ...query,
      order: query.order ?? 'PLANNED',
    };
    if (!query.state) delete params['state'];
    return this.get('/api/experiments/results', params);
  }
  getResult(runId: string, id: string): Promise<ResultDetail> {
    return this.get(`/api/experiments/results/${encodeURIComponent(id)}`, { runId });
  }
  getIssues(runId: string, severity: string, offset: number): Promise<Page<Issue>> {
    return this.get('/api/experiments/issues', { runId, severity, offset, limit: 25 });
  }
  getArtifacts(runId: string): Promise<ArtifactManifest> {
    return this.get('/api/experiments/artifacts', { runId });
  }
  artifactUrl(runId: string, id: string): string {
    return `${environment.apiBaseUrl}/api/experiments/artifacts/${encodeURIComponent(id)}?${new URLSearchParams({ runId })}`;
  }
  exportUrl(runId: string, query: ResultQuery, format: 'csv' | 'json'): string {
    const params = new URLSearchParams({
      runId,
      format,
      instanceId: query.instanceId,
      algorithm: query.algorithm,
      search: query.search,
      order: query.order ?? 'PLANNED',
    });
    if (query.state) params.set('state', query.state);
    return `${environment.apiBaseUrl}/api/experiments/results/export?${params}`;
  }
}
