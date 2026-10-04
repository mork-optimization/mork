import { HttpErrorResponse } from '@angular/common/http';
import { DestroyRef, Injectable, Signal, computed, inject, signal } from '@angular/core';
import {
  ArtifactManifest,
  AutoconfigStatus,
  CandidateView,
  EliteHistorySnapshot,
  EliteSnapshot,
  EvaluationView,
  InstanceEvaluationSummary,
  SearchSpaceSnapshot,
  TuningAlert,
} from '../model/autoconfig';
import { ChartViewModel } from '../model/dashboard';
import { AutoconfigClient } from '../service/autoconfig-client';
import { AutoconfigChartUtil } from '../util/autoconfig-chart-util';

const CHANGE_PAGE_SIZE = 500;
const ACTIVE_POLL_INTERVAL_MS = 2_000;
const IDLE_POLL_INTERVAL_MS = 10_000;
const ARTIFACT_POLL_INTERVAL_MS = 10_000;
const ALERT_STORAGE_KEY = 'mork.autoconfig.alertSeen.v1';

export type ApplicationView = 'detecting' | 'standard' | 'tuning' | 'worker' | 'unavailable';
export type TuningSyncState = 'detecting' | 'synchronizing' | 'live' | 'retrying';

interface StoredEvaluation {
  readonly revision: number;
  readonly evaluation: EvaluationView;
}

interface AlertSeenState {
  readonly runId: string;
  readonly revision: number;
  readonly runFailureSeen: boolean;
}

@Injectable({ providedIn: 'root' })
export class AutoconfigStore {
  private readonly client = inject(AutoconfigClient);
  private readonly destroyRef = inject(DestroyRef);

  private readonly statusSignal = signal<AutoconfigStatus | null>(null);
  private readonly syncStateSignal = signal<TuningSyncState>('detecting');
  private readonly latestErrorSignal = signal<string | null>(null);
  private readonly evaluationsSignal = signal<readonly EvaluationView[]>([]);
  private readonly elitesSignal = signal<EliteSnapshot | null>(null);
  private readonly eliteHistorySignal = signal<EliteHistorySnapshot | null>(null);
  private readonly searchSpaceSignal = signal<SearchSpaceSnapshot | null>(null);
  private readonly artifactsSignal = signal<ArtifactManifest | null>(null);
  private readonly alertsSignal = signal<readonly TuningAlert[]>([]);
  private readonly seenAlertRevisionSignal = signal(0);
  private readonly runFailureSeenSignal = signal(false);
  private readonly lastUpdatedSignal = signal<Date | null>(null);

  private readonly evaluationMap = new Map<number, StoredEvaluation>();
  private readonly candidateCache = new Map<string, CandidateView>();
  private pollTimer: ReturnType<typeof setTimeout> | null = null;
  private currentRunId: string | null = null;
  private evaluationRevision = 0;
  private eliteMarker: string | null = null;
  private searchSpaceLoaded = false;
  private lastArtifactFetch = 0;
  private lastArtifactPhase: string | null = null;
  private started = false;
  private destroyed = false;

  readonly status = this.statusSignal.asReadonly();
  readonly syncState = this.syncStateSignal.asReadonly();
  readonly latestError = this.latestErrorSignal.asReadonly();
  readonly evaluations = this.evaluationsSignal.asReadonly();
  readonly elites = this.elitesSignal.asReadonly();
  readonly eliteHistory = this.eliteHistorySignal.asReadonly();
  readonly searchSpace = this.searchSpaceSignal.asReadonly();
  readonly artifacts = this.artifactsSignal.asReadonly();
  readonly alerts = this.alertsSignal.asReadonly();
  readonly lastUpdated = this.lastUpdatedSignal.asReadonly();

  readonly viewMode = computed<ApplicationView>(() => {
    const status = this.statusSignal();
    if (!status) {
      return this.syncStateSignal() === 'retrying' ? 'unavailable' : 'detecting';
    }
    if (status.mode === 'DISABLED') {
      return 'standard';
    }
    return status.role === 'WORKER' ? 'worker' : 'tuning';
  });

  readonly connectionLabel = computed(() => {
    switch (this.syncStateSignal()) {
      case 'detecting':
        return 'Detecting run mode';
      case 'synchronizing':
        return 'Synchronizing';
      case 'live':
        return 'Live';
      case 'retrying':
        return 'Reconnecting';
    }
  });

  readonly budgetPercent = computed(() => {
    const budget = this.statusSignal()?.budget;
    if (!budget || budget.maximum <= 0) {
      return 0;
    }
    return Math.min(100, (budget.used / budget.maximum) * 100);
  });

  readonly evaluationCostChart: Signal<ChartViewModel> = computed(() =>
    AutoconfigChartUtil.evaluationCost(this.evaluationsSignal()),
  );
  readonly evaluationActivityChart: Signal<ChartViewModel> = computed(() =>
    AutoconfigChartUtil.evaluationActivity(this.evaluationsSignal()),
  );
  readonly eliteRankChart: Signal<ChartViewModel> = computed(() =>
    AutoconfigChartUtil.eliteRanks(this.eliteHistorySignal()),
  );
  readonly instanceSummaries: Signal<readonly InstanceEvaluationSummary[]> = computed(() =>
    AutoconfigChartUtil.instanceSummaries(this.evaluationsSignal()),
  );
  readonly recentEvaluations = computed(() => this.evaluationsSignal().slice(-100).reverse());
  readonly unseenAlertCount = computed(() => {
    let unseen = 0;
    for (const alert of this.alertsSignal()) {
      if (alert.revision === null) {
        if (!this.runFailureSeenSignal()) {
          unseen++;
        }
      } else if (alert.revision > this.seenAlertRevisionSignal()) {
        unseen++;
      }
    }
    return unseen;
  });

  constructor() {
    this.destroyRef.onDestroy(() => {
      this.destroyed = true;
      this.clearPollTimer();
    });
  }

  start(): void {
    if (this.started) {
      return;
    }
    this.started = true;
    this.schedulePoll(0);
  }

  retryNow(): void {
    this.clearPollTimer();
    this.schedulePoll(0);
  }

  markAlertsSeen(): void {
    this.seenAlertRevisionSignal.set(this.evaluationRevision);
    if (this.statusSignal()?.failure) {
      this.runFailureSeenSignal.set(true);
    }
    this.persistSeenAlerts();
  }

  async loadCandidate(configurationId: string): Promise<CandidateView> {
    const cached = this.candidateCache.get(configurationId);
    if (cached) {
      return cached;
    }
    const candidate = await this.client.getCandidate(configurationId);
    this.candidateCache.set(configurationId, candidate);
    return candidate;
  }

  async refreshArtifacts(): Promise<void> {
    const status = this.statusSignal();
    if (!status?.runId || status.role !== 'COORDINATOR') {
      this.artifactsSignal.set(null);
      return;
    }
    try {
      this.artifactsSignal.set(await this.client.getArtifacts());
      this.lastArtifactFetch = Date.now();
    } catch (error: unknown) {
      if (this.isNotFound(error)) {
        this.artifactsSignal.set({ runId: status.runId, artifacts: [] });
        this.lastArtifactFetch = Date.now();
        return;
      }
      throw error;
    }
  }

  artifactUrl(artifactId: string): string {
    return this.client.artifactUrl(artifactId);
  }

  private schedulePoll(delay: number): void {
    if (this.destroyed) {
      return;
    }
    this.pollTimer = setTimeout(() => void this.poll(), delay);
  }

  private async poll(): Promise<void> {
    this.pollTimer = null;
    this.syncStateSignal.set('synchronizing');
    try {
      const status = await this.client.getStatus();
      this.adoptStatus(status);
      if (status.mode !== 'DISABLED' && status.role === 'COORDINATOR' && status.runId) {
        await this.synchronizeRun(status);
      }
      this.latestErrorSignal.set(null);
      this.lastUpdatedSignal.set(new Date());
      this.syncStateSignal.set('live');
    } catch (error: unknown) {
      this.latestErrorSignal.set(this.errorMessage(error));
      this.syncStateSignal.set('retrying');
    } finally {
      const status = this.statusSignal();
      const active = status?.state === 'PREPARING' || status?.state === 'RUNNING';
      this.schedulePoll(active ? ACTIVE_POLL_INTERVAL_MS : IDLE_POLL_INTERVAL_MS);
    }
  }

  private adoptStatus(status: AutoconfigStatus): void {
    if (status.runId !== this.currentRunId) {
      this.resetRun(status.runId);
    } else if (status.latestEvaluationRevision < this.evaluationRevision) {
      this.resetRun(status.runId);
    }
    this.statusSignal.set(status);
    this.publishAlerts();
  }

  private async synchronizeRun(status: AutoconfigStatus): Promise<void> {
    await this.synchronizeEvaluations(status);
    await this.synchronizeElites(status);
    await this.synchronizeSearchSpace(status);
    await this.synchronizeArtifacts(status);
  }

  private async synchronizeEvaluations(status: AutoconfigStatus): Promise<void> {
    let changed = false;
    while (this.evaluationRevision < status.latestEvaluationRevision) {
      const page = await this.client.getEvaluationChanges(
        this.evaluationRevision,
        CHANGE_PAGE_SIZE,
      );
      if (page.runId !== status.runId) {
        throw new Error('Evaluation changes belong to a different autoconfig run');
      }
      for (const change of page.changes) {
        this.evaluationMap.set(change.evaluation.id, {
          revision: change.revision,
          evaluation: change.evaluation,
        });
        changed = true;
      }
      if (page.nextRevision <= this.evaluationRevision) {
        break;
      }
      this.evaluationRevision = page.nextRevision;
    }
    if (changed) {
      const evaluations = [...this.evaluationMap.values()]
        .sort((left, right) => left.evaluation.id - right.evaluation.id)
        .map((stored) => stored.evaluation);
      this.evaluationsSignal.set(evaluations);
      this.publishAlerts();
    }
  }

  private async synchronizeElites(status: AutoconfigStatus): Promise<void> {
    const marker = `${status.irace.iteration ?? ''}|${status.irace.updatedAt ?? ''}|${status.irace.finalSnapshot}`;
    if (marker === this.eliteMarker) {
      return;
    }
    const [elites, history] = await Promise.all([
      this.client.getElites(),
      this.client.getEliteHistory(),
    ]);
    if (elites.runId !== status.runId || history.runId !== status.runId) {
      throw new Error('Elite snapshots belong to a different autoconfig run');
    }
    this.elitesSignal.set(elites);
    this.eliteHistorySignal.set(history);
    this.eliteMarker = marker;
  }

  private async synchronizeSearchSpace(status: AutoconfigStatus): Promise<void> {
    if (
      status.mode !== 'AUTOCONFIG' ||
      status.generatedParameterCount <= 0 ||
      this.searchSpaceLoaded
    ) {
      return;
    }
    try {
      this.searchSpaceSignal.set(await this.client.getSearchSpace());
      this.searchSpaceLoaded = true;
    } catch (error: unknown) {
      if (!this.isNotFound(error)) {
        throw error;
      }
    }
  }

  private async synchronizeArtifacts(status: AutoconfigStatus): Promise<void> {
    const now = Date.now();
    const phaseChanged = status.phase !== this.lastArtifactPhase;
    if (!phaseChanged && now - this.lastArtifactFetch < ARTIFACT_POLL_INTERVAL_MS) {
      return;
    }
    await this.refreshArtifacts();
    this.lastArtifactPhase = status.phase;
  }

  private publishAlerts(): void {
    const alerts: TuningAlert[] = [];
    const status = this.statusSignal();
    if (status?.failure) {
      alerts.push({
        key: `run:${status.runId}:failure`,
        revision: null,
        severity: 'critical',
        title: 'Tuning run failed',
        message: status.failure.message ?? status.failure.type,
        timestamp: status.finishedAt,
        evaluationId: null,
      });
    }
    for (const stored of this.evaluationMap.values()) {
      const evaluation = stored.evaluation;
      if (evaluation.state !== 'FAILED' && evaluation.state !== 'REJECTED' && !evaluation.slow) {
        continue;
      }
      const failed = evaluation.state === 'FAILED';
      const rejected = evaluation.state === 'REJECTED';
      const title = failed
        ? 'Evaluation failed'
        : rejected
          ? 'Evaluation rejected'
          : 'Slow evaluation';
      const details = evaluation.message ?? evaluation.reasonCode ?? evaluation.instanceName;
      alerts.push({
        key: `evaluation:${evaluation.id}`,
        revision: stored.revision,
        severity: failed ? 'critical' : 'warning',
        title,
        message: evaluation.slow && rejected ? `${details} · also slow` : details,
        timestamp: evaluation.finishedAt,
        evaluationId: evaluation.id,
      });
    }
    alerts.sort((left, right) => {
      if (left.revision === null) {
        return -1;
      }
      if (right.revision === null) {
        return 1;
      }
      return right.revision - left.revision;
    });
    this.alertsSignal.set(alerts);
  }

  private resetRun(runId: string | null): void {
    this.currentRunId = runId;
    this.evaluationRevision = 0;
    this.evaluationMap.clear();
    this.candidateCache.clear();
    this.evaluationsSignal.set([]);
    this.elitesSignal.set(null);
    this.eliteHistorySignal.set(null);
    this.searchSpaceSignal.set(null);
    this.artifactsSignal.set(null);
    this.alertsSignal.set([]);
    this.eliteMarker = null;
    this.searchSpaceLoaded = false;
    this.lastArtifactFetch = 0;
    this.lastArtifactPhase = null;
    this.restoreSeenAlerts(runId);
  }

  private restoreSeenAlerts(runId: string | null): void {
    this.seenAlertRevisionSignal.set(0);
    this.runFailureSeenSignal.set(false);
    if (!runId) {
      return;
    }
    try {
      const raw = localStorage.getItem(ALERT_STORAGE_KEY);
      if (!raw) {
        return;
      }
      const stored = JSON.parse(raw) as Partial<AlertSeenState>;
      if (stored.runId === runId) {
        this.seenAlertRevisionSignal.set(typeof stored.revision === 'number' ? stored.revision : 0);
        this.runFailureSeenSignal.set(stored.runFailureSeen === true);
      }
    } catch {
      localStorage.removeItem(ALERT_STORAGE_KEY);
    }
  }

  private persistSeenAlerts(): void {
    if (!this.currentRunId) {
      return;
    }
    const state: AlertSeenState = {
      runId: this.currentRunId,
      revision: this.seenAlertRevisionSignal(),
      runFailureSeen: this.runFailureSeenSignal(),
    };
    localStorage.setItem(ALERT_STORAGE_KEY, JSON.stringify(state));
  }

  private clearPollTimer(): void {
    if (this.pollTimer) {
      clearTimeout(this.pollTimer);
      this.pollTimer = null;
    }
  }

  private isNotFound(error: unknown): boolean {
    return error instanceof HttpErrorResponse && error.status === 404;
  }

  private errorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      return `Backend request failed (${error.status || 'network error'})`;
    }
    return error instanceof Error ? error.message : String(error);
  }
}
