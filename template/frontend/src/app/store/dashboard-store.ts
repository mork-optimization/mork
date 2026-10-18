import { DestroyRef, Injectable, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  ArtifactManifest,
  InstanceDetail,
  InstanceQuery,
  InstanceSummary,
  Issue,
  NormalStatus,
  Page,
  ResultDetail,
  ResultQuery,
  ResultOrder,
  ResultView,
  WorkState,
} from '../model/normal-experiment';
import { EventClient, EventConnectionState } from '../service/event-client';
import { ExperimentClient } from '../service/experiment-client';
import { NormalChartUtil } from '../util/normal-chart-util';

@Injectable({ providedIn: 'root' })
export class DashboardStore {
  private readonly client = inject(ExperimentClient);
  private readonly events = inject(EventClient);
  private readonly destroyRef = inject(DestroyRef);
  private readonly statusState = signal<NormalStatus | null>(null);
  readonly status = this.statusState.asReadonly();
  private readonly syncStateState = signal<'connecting' | 'live' | 'retrying'>('connecting');
  readonly syncState = this.syncStateState.asReadonly();
  private readonly transportStateState = signal<EventConnectionState>('connecting');
  readonly transportState = this.transportStateState.asReadonly();
  private readonly latestErrorState = signal<string | null>(null);
  readonly latestError = this.latestErrorState.asReadonly();
  private readonly transportErrorState = signal<string | null>(null);
  readonly transportError = this.transportErrorState.asReadonly();
  private readonly lastUpdatedState = signal<Date | null>(null);
  readonly lastUpdated = this.lastUpdatedState.asReadonly();
  private readonly selectedObjectiveState = signal<string | null>(null);
  readonly selectedObjective = this.selectedObjectiveState.asReadonly();
  private readonly showReferenceState = signal(true);
  readonly showReference = this.showReferenceState.asReadonly();
  private readonly selectedInstanceIdState = signal<string | null>(null);
  readonly selectedInstanceId = this.selectedInstanceIdState.asReadonly();
  private readonly instancePageState = signal<Page<InstanceSummary> | null>(null);
  readonly instancePage = this.instancePageState.asReadonly();
  private readonly resultPageState = signal<Page<ResultView> | null>(null);
  readonly resultPage = this.resultPageState.asReadonly();
  private readonly issuePageState = signal<Page<Issue> | null>(null);
  readonly issuePage = this.issuePageState.asReadonly();
  private readonly artifactsState = signal<ArtifactManifest | null>(null);
  readonly artifacts = this.artifactsState.asReadonly();
  private readonly detailState = signal<InstanceDetail | null>(null);
  readonly detail = this.detailState.asReadonly();
  private readonly inspectedResultState = signal<ResultDetail | null>(null);
  readonly inspectedResult = this.inspectedResultState.asReadonly();
  private readonly resultErrorState = signal<string | null>(null);
  readonly resultError = this.resultErrorState.asReadonly();
  private readonly tabState = signal(0);
  readonly tab = this.tabState.asReadonly();
  private readonly instanceQueryState = signal<InstanceQuery>({
    experiment: '',
    search: '',
    offset: 0,
    limit: 25,
  });
  readonly instanceQuery = this.instanceQueryState.asReadonly();
  private readonly resultQueryState = signal<ResultQuery>({
    instanceId: '',
    algorithm: '',
    state: '',
    search: '',
    offset: 0,
    limit: 25,
  });
  readonly resultQuery = this.resultQueryState.asReadonly();
  private readonly issueSeverityState = signal('');
  readonly issueSeverity = this.issueSeverityState.asReadonly();
  private readonly issueOffsetState = signal(0);
  readonly issueOffset = this.issueOffsetState.asReadonly();
  private readonly seenIssueIdState = signal(0);
  readonly seenIssueId = this.seenIssueIdState.asReadonly();
  readonly objectives = computed(() =>
    Object.entries(this.status()?.objectives ?? {}).map(([name, mode]) => ({ name, mode })),
  );
  readonly terminal = computed(() =>
    ['SUCCEEDED', 'COMPLETED_WITH_ERRORS', 'FAILED', 'CANCELLED'].includes(
      this.status()?.phase ?? '',
    ),
  );
  readonly connectionLabel = computed(() =>
    this.syncState() === 'retrying'
      ? 'Retrying · data may be stale'
      : this.syncState() === 'connecting'
        ? 'Connecting'
        : this.transportState() === 'live'
          ? 'Live'
          : 'Live via HTTP',
  );
  readonly unseenIssues = computed(() =>
    Math.max(0, (this.status()?.issueCount ?? 0) - this.seenIssueId()),
  );
  readonly estimatedRemainingMillis = computed(() => {
    const status = this.status();
    if (!status || status.phase !== 'RUNNING' || status.elapsedMillis < 10000) return null;
    const completed = status.counts.succeeded + status.counts.failed;
    return completed < 5 || status.counts.cancelled > 0
      ? null
      : (status.elapsedMillis / completed) * (status.counts.pending + status.counts.running);
  });
  readonly charts = computed(() => {
    const detail = this.detail();
    const objective = this.selectedObjective();
    const mode = this.selectedObjectiveMode();
    return detail && objective && mode && detail.objective === objective
      ? NormalChartUtil.instance(detail, mode, this.showReference())
      : null;
  });
  private readonly selectedObjectiveMode = computed(
    () => this.status()?.objectives[this.selectedObjective() ?? ''] ?? null,
  );
  readonly distribution = computed(() =>
    this.detail() ? NormalChartUtil.distribution(this.detail()!, this.showReference()) : null,
  );
  readonly qualityRuntime = computed(() =>
    this.detail() ? NormalChartUtil.qualityRuntime(this.detail()!, this.showReference()) : null,
  );
  readonly lastEventId = computed(() => {
    this.transportState();
    this.lastUpdated();
    return this.events.lastEventId;
  });
  readonly progress = computed(() => {
    const status = this.status(),
      detail = this.detail(),
      algorithm = detail?.algorithms.find((a) => a.name === this.resultQuery().algorithm);
    const counts = algorithm?.counts ?? detail?.instance.counts ?? status?.counts;
    return {
      instances: { completed: status?.completedInstances ?? 0, total: status?.instances ?? 0 },
      algorithms: {
        completed: detail?.instance.completedAlgorithms ?? 0,
        total: detail?.instance.algorithms ?? 0,
      },
      repetitions: {
        completed: (counts?.succeeded ?? 0) + (counts?.failed ?? 0),
        total: counts?.planned ?? 0,
      },
      repetitionScope: algorithm
        ? 'selected algorithm'
        : detail
          ? 'selected instance'
          : 'execution',
    };
  });
  private readonly cache = new Map<string, InstanceDetail>();
  private timer: ReturnType<typeof setTimeout> | null = null;
  private polling = false;
  private pending = false;
  private started = false;
  private destroyed = false;
  private generation = 0;
  private delay = 500;
  private forceDetail = true;
  private lastDetailFetch = 0;
  private resultRequest = 0;

  constructor() {
    this.destroyRef.onDestroy(() => {
      this.destroyed = true;
      this.generation++;
      this.resultRequest++;
      this.clearTimer();
    });
  }
  start(): void {
    if (this.started) return;
    this.started = true;
    this.events.state$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((state) => {
      this.transportStateState.set(state);
      if (state === 'live') this.transportErrorState.set(null);
    });
    this.events.errors$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((error) => this.transportErrorState.set(this.message(error)));
    this.events.connected$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.generation++;
      this.forceDetail = true;
      this.refreshNow();
    });
    this.events.envelopes$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((event) => {
      if (event.type === 'PingEvent') return;
      if (event.type === 'ExecutionEndedEvent' || event.type === 'InstanceProcessingEndedEvent') {
        this.forceDetail = true;
        this.refreshNow();
      } else if (!this.polling)
        this.schedule(Math.max(100, 500 - (Date.now() - (this.lastUpdated()?.getTime() ?? 0))));
    });
    this.events.start();
    this.refreshNow();
  }
  retryNow(): void {
    this.delay = 500;
    this.events.retryNow();
    this.refreshNow();
  }
  setReferenceVisible(visible: boolean): void {
    this.showReferenceState.set(visible);
  }
  selectObjective(name: string): void {
    if (!this.status()?.objectives[name] || this.selectedObjective() === name) return;
    this.selectedObjectiveState.set(name);
    this.generation++;
    this.forceDetail = true;
    this.detailState.set(null);
    this.refreshNow();
  }
  selectInstance(id: string): void {
    this.selectedInstanceIdState.set(id);
    this.generation++;
    this.forceDetail = true;
    const cached = this.cache.get(id);
    this.detailState.set(cached?.objective === this.selectedObjective() ? cached : null);
    this.refreshNow();
  }
  selectTab(tab: number): void {
    this.tabState.set(tab);
    this.refreshNow();
  }
  filterInstances(experiment: string, search: string): void {
    this.instanceQueryState.set({ ...this.instanceQuery(), experiment, search, offset: 0 });
    this.invalidate();
  }
  pageInstances(offset: number): void {
    this.instanceQueryState.update((q) => ({ ...q, offset }));
    this.invalidate();
  }
  filterResults(
    instanceId: string,
    algorithm: string,
    state: WorkState | '',
    search: string,
  ): void {
    this.resultQueryState.set({
      ...this.resultQuery(),
      instanceId,
      algorithm,
      state,
      search,
      offset: 0,
    });
    this.invalidate();
  }
  pageResults(offset: number): void {
    this.resultQueryState.update((q) => ({ ...q, offset }));
    this.invalidate();
  }
  sortResults(order: ResultOrder): void {
    this.resultQueryState.update((q) => ({ ...q, order, offset: 0 }));
    this.invalidate();
  }
  filterIssues(severity: string): void {
    this.issueSeverityState.set(severity);
    this.issueOffsetState.set(0);
    this.invalidate();
  }
  pageIssues(offset: number): void {
    this.issueOffsetState.set(offset);
    this.invalidate();
  }
  markIssuesSeen(): void {
    this.seenIssueIdState.set(this.status()?.issueCount ?? 0);
    try {
      localStorage.setItem(
        'mork.normal.issues.v1',
        JSON.stringify({ runId: this.status()?.runId, id: this.seenIssueId() }),
      );
    } catch {
      /* Storage may be disabled. */
    }
  }
  async inspectResult(id: string): Promise<void> {
    const runId = this.status()?.runId;
    if (!runId) return;
    const request = ++this.resultRequest;
    this.inspectedResultState.set(null);
    this.resultErrorState.set(null);
    try {
      const [result, artifacts] = await Promise.all([
        this.client.getResult(runId, id),
        this.client.getArtifacts(runId),
      ]);
      if (
        !this.destroyed &&
        request === this.resultRequest &&
        runId === this.status()?.runId &&
        result.runId === runId
      ) {
        this.inspectedResultState.set(result);
        if (artifacts.runId === runId) this.artifactsState.set(artifacts);
      }
    } catch (error) {
      if (!this.destroyed && request === this.resultRequest && runId === this.status()?.runId)
        this.resultErrorState.set(this.message(error));
    }
  }
  closeResult(): void {
    this.resultRequest++;
    this.inspectedResultState.set(null);
    this.resultErrorState.set(null);
  }
  artifactUrl(id: string): string {
    return this.client.artifactUrl(this.status()!.runId!, id);
  }
  exportUrl(format: 'csv' | 'json'): string {
    return this.client.exportUrl(this.status()!.runId!, this.resultQuery(), format);
  }
  refreshNow(): void {
    this.clearTimer();
    if (this.polling) this.pending = true;
    else void this.poll();
  }
  private invalidate(): void {
    this.generation++;
    this.refreshNow();
  }
  private async poll(): Promise<void> {
    if (this.destroyed) return;
    this.polling = true;
    this.pending = false;
    let token = this.generation,
      nextDelay = 2000;
    try {
      const status = await this.client.getStatus();
      if (!this.current(token)) return;
      const previous = this.status();
      if (status.runId !== previous?.runId) {
        this.cache.clear();
        this.detailState.set(null);
        this.inspectedResultState.set(null);
        this.resultRequest++;
        this.instancePageState.set(null);
        this.resultPageState.set(null);
        this.issuePageState.set(null);
        this.artifactsState.set(null);
        this.selectedInstanceIdState.set(null);
        this.selectedObjectiveState.set(Object.keys(status.objectives)[0] ?? null);
        this.instanceQueryState.set({ experiment: '', search: '', offset: 0, limit: 25 });
        this.resultQueryState.set({
          instanceId: '',
          algorithm: '',
          state: '',
          search: '',
          offset: 0,
          limit: 25,
        });
        this.seenIssueIdState.set(0);
        this.forceDetail = true;
        try {
          const seen = JSON.parse(localStorage.getItem('mork.normal.issues.v1') ?? 'null');
          if (seen?.runId === status.runId) this.seenIssueIdState.set(seen.id);
        } catch {
          /* Invalid or unavailable storage. */
        }
      }
      if (previous?.runId === status.runId && status.revision < previous.revision)
        throw new Error('Monitoring revision moved backwards');
      this.statusState.set(status);
      if (status.runId) {
        const runId = status.runId;
        const page = await this.client.getInstances(runId, this.instanceQuery());
        if (!this.current(token) || page.runId !== runId) return;
        this.instancePageState.set(page);
        if (!this.selectedInstanceId() && page.items.length)
          this.selectedInstanceIdState.set(
            page.items.find((i) => i.counts.running > 0)?.id ?? page.items[0].id,
          );
        const id = this.selectedInstanceId(),
          objective = this.selectedObjective(),
          cached = id ? this.cache.get(id) : null;
        const summary = page.items.find((i) => i.id === id);
        const changed =
          !cached ||
          cached.objective !== objective ||
          !summary ||
          cached.revision !== summary.revision;
        const finalFlush =
          ['SUCCEEDED', 'COMPLETED_WITH_ERRORS', 'FAILED', 'CANCELLED'].includes(status.phase) &&
          status.revision !== previous?.revision;
        if (
          id &&
          objective &&
          changed &&
          (this.forceDetail || finalFlush || Date.now() - this.lastDetailFetch >= 2000)
        ) {
          const detail = await this.client.getInstance(runId, id, objective);
          if (
            !this.current(token) ||
            detail.runId !== runId ||
            detail.objective !== this.selectedObjective()
          )
            return;
          this.cache.delete(id);
          this.cache.set(id, detail);
          while (this.cache.size > 10) this.cache.delete(this.cache.keys().next().value!);
          this.detailState.set(detail);
          this.lastDetailFetch = Date.now();
          this.forceDetail = false;
        } else if (cached && cached.objective === objective) this.detailState.set(cached);
        if (this.tab() === 1) {
          const results = await this.client.getResults(runId, this.resultQuery());
          if (!this.current(token) || results.runId !== runId) return;
          this.resultPageState.set(results);
        }
        if (this.tab() === 2 || status.issueCount !== previous?.issueCount || !this.issuePage()) {
          const issues = await this.client.getIssues(
            runId,
            this.issueSeverity(),
            this.issueOffset(),
          );
          if (!this.current(token) || issues.runId !== runId) return;
          this.issuePageState.set(issues);
        }
        if (this.tab() === 3) {
          const artifacts = await this.client.getArtifacts(runId);
          if (!this.current(token) || artifacts.runId !== runId) return;
          this.artifactsState.set(artifacts);
        }
      }
      this.latestErrorState.set(null);
      this.lastUpdatedState.set(new Date());
      this.syncStateState.set('live');
      this.delay = 500;
      nextDelay = this.terminal() || status.phase === 'IDLE' ? 10000 : 2000;
    } catch (error) {
      if (!this.current(token)) return;
      this.latestErrorState.set(this.message(error));
      this.syncStateState.set('retrying');
      nextDelay = this.delay;
      this.delay = Math.min(30000, this.delay * 2);
    } finally {
      this.polling = false;
      if (!this.destroyed) this.schedule(this.pending || token !== this.generation ? 0 : nextDelay);
    }
  }
  private current(token: number): boolean {
    return !this.destroyed && token === this.generation;
  }
  private message(error: unknown): string {
    return error instanceof Error ? error.message : String(error);
  }
  private schedule(delay: number): void {
    if (this.destroyed) return;
    this.clearTimer();
    this.timer = setTimeout(() => void this.poll(), delay);
  }
  private clearTimer(): void {
    if (this.timer) clearTimeout(this.timer);
    this.timer = null;
  }
}
