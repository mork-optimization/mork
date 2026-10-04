import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ArtifactManifest,
  AutoconfigStatus,
  CandidateView,
  EliteHistorySnapshot,
  EliteSnapshot,
  EvaluationChangePage,
  SearchSpaceSnapshot,
} from '../model/autoconfig';

@Injectable({ providedIn: 'root' })
export class AutoconfigClient {
  private readonly http = inject(HttpClient);

  getStatus(): Promise<AutoconfigStatus> {
    return firstValueFrom(this.http.get<AutoconfigStatus>(this.endpoint('/status')));
  }

  getEvaluationChanges(after: number, limit: number): Promise<EvaluationChangePage> {
    return firstValueFrom(
      this.http.get<EvaluationChangePage>(this.endpoint('/evaluations/changes'), {
        params: { after, limit },
      }),
    );
  }

  getElites(): Promise<EliteSnapshot> {
    return firstValueFrom(this.http.get<EliteSnapshot>(this.endpoint('/elites')));
  }

  getEliteHistory(): Promise<EliteHistorySnapshot> {
    return firstValueFrom(this.http.get<EliteHistorySnapshot>(this.endpoint('/elites/history')));
  }

  getCandidate(configurationId: string): Promise<CandidateView> {
    return firstValueFrom(
      this.http.get<CandidateView>(
        this.endpoint(`/candidates/${encodeURIComponent(configurationId)}`),
      ),
    );
  }

  getSearchSpace(): Promise<SearchSpaceSnapshot> {
    return firstValueFrom(this.http.get<SearchSpaceSnapshot>(this.endpoint('/search-space')));
  }

  getArtifacts(): Promise<ArtifactManifest> {
    return firstValueFrom(this.http.get<ArtifactManifest>(this.endpoint('/artifacts')));
  }

  artifactUrl(artifactId: string): string {
    return this.endpoint(`/artifacts/${encodeURIComponent(artifactId)}`);
  }

  private endpoint(path: string): string {
    return `${environment.apiBaseUrl}/api/autoconfig${path}`;
  }
}
