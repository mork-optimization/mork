import { TestBed } from '@angular/core/testing';
import { BehaviorSubject, Subject } from 'rxjs';
import { DashboardStore } from './dashboard-store';
import { ExperimentClient } from '../service/experiment-client';
import { EventClient, EventConnectionState } from '../service/event-client';
import { MorkEventEnvelope } from '../model/Events';
import { normalDetail, normalStatus, normalResult } from '../testing/normal-experiment-fixture';
import { InstanceQuery, ResultQuery, WorkState } from '../model/normal-experiment';

class FakeClient {
  status = normalStatus();
  getStatus = vi.fn(async () => this.status);
  getInstances = vi.fn(async (runId: string, query: InstanceQuery) => ({
    runId,
    revision: this.status.revision,
    total: 12,
    offset: query.offset,
    limit: query.limit,
    items: Array.from({ length: Math.min(query.limit, 12 - query.offset) }, (_, index) => ({
      ...normalDetail('instance-' + (query.offset + index)).instance,
      revision: this.status.revision,
    })),
  }));
  getInstance = vi.fn(async (runId: string, id: string, objective: string) =>
    normalDetail(id, objective, { runId, revision: this.status.revision }),
  );
  getResults = vi.fn(async (runId: string, query: ResultQuery) => ({
    runId,
    revision: this.status.revision,
    total: 1,
    offset: query.offset,
    limit: query.limit,
    items: [normalResult()],
  }));
  getIssues = vi.fn(async (runId: string) => ({
    runId,
    revision: this.status.revision,
    total: 0,
    offset: 0,
    limit: 25,
    items: [],
  }));
  getArtifacts = vi.fn(async (runId: string) => ({ runId, artifacts: [] }));
  getResult = vi.fn(async (runId: string) => ({
    runId,
    revision: 1,
    result: normalResult(),
    issues: [],
    rendering: { state: 'UNSUPPORTED' as const, rendererId: null, data: {} },
  }));
}
class FakeEvents {
  state$ = new BehaviorSubject<EventConnectionState>('live');
  envelopes$ = new Subject<MorkEventEnvelope>();
  connected$ = new Subject<void>();
  errors$ = new Subject<unknown>();
  lastEventId = 0;
  start = vi.fn();
  retryNow = vi.fn();
}

describe('DashboardStore backend snapshots', () => {
  let client: FakeClient, events: FakeEvents, store: DashboardStore;
  beforeEach(() => {
    vi.useFakeTimers();
    localStorage.clear();
    client = new FakeClient();
    events = new FakeEvents();
    TestBed.configureTestingModule({
      providers: [
        DashboardStore,
        { provide: ExperimentClient, useValue: client },
        { provide: EventClient, useValue: events },
      ],
    });
    store = TestBed.inject(DashboardStore);
    store.start();
  });
  afterEach(() => {
    TestBed.resetTestingModule();
    vi.useRealTimers();
  });
  async function flush(): Promise<void> {
    await vi.advanceTimersByTimeAsync(0);
  }
  it('uses authoritative totals, including failures, and defaults to the first objective', async () => {
    await flush();
    expect(store.selectedObjective()).toBe('Cost');
    expect(store.progress().repetitions).toEqual({ completed: 2, total: 2 });
    expect(store.charts()?.bestSolution?.resultId).toBe('result-0');
  });
  it('does not manufacture completion or erase the final experiment', async () => {
    await flush();
    client.status = normalStatus({
      revision: 2,
      phase: 'FAILED',
      completedInstances: 0,
      counts: { planned: 10, pending: 0, running: 0, succeeded: 1, failed: 1, cancelled: 8 },
    });
    store.refreshNow();
    await flush();
    expect(store.status()?.counts.succeeded).toBe(1);
    expect(store.progress().instances.completed).toBe(0);
    expect(store.status()?.currentExperiment).toBe('Experiment A');
    expect(store.terminal()).toBe(true);
  });
  it('recovers from a REST failure while the socket stays live', async () => {
    await flush();
    client.getStatus.mockRejectedValueOnce(new Error('temporary'));
    store.refreshNow();
    await flush();
    expect(store.syncState()).toBe('retrying');
    expect(store.detail()).not.toBeNull();
    await vi.advanceTimersByTimeAsync(500);
    expect(store.syncState()).toBe('live');
    expect(store.latestError()).toBeNull();
    expect(store.transportState()).toBe('live');
  });
  it('keeps transport failures separate from experiment state', async () => {
    await flush();
    events.errors$.next(new Error('socket'));
    events.state$.next('disconnected');
    expect(store.transportError()).toBe('socket');
    expect(store.status()?.phase).toBe('RUNNING');
    expect(store.connectionLabel()).toBe('Live via HTTP');
    events.state$.next('live');
    expect(store.transportError()).toBeNull();
  });
  it('switches objectives and loads a fresh immutable view immediately', async () => {
    await flush();
    store.selectObjective('Quality');
    await flush();
    expect(client.getInstance).toHaveBeenLastCalledWith('run-a', 'instance-0', 'Quality');
    expect(store.detail()?.objective).toBe('Quality');
    expect(store.charts()?.selectedObjective).toBe('Quality');
  });
  it('bounds cached details to exactly ten without losing older instances', async () => {
    await flush();
    for (let index = 1; index < 12; index++) {
      store.selectInstance('instance-' + index);
      await flush();
    }
    expect(store.instancePage()?.total).toBe(12);
    const before = client.getInstance.mock.calls.length;
    store.selectInstance('instance-0');
    await flush();
    expect(client.getInstance).toHaveBeenCalledTimes(before + 1);
    expect(store.detail()?.instance.id).toBe('instance-0');
  });
  it('invalidates old responses when objective selection changes', async () => {
    await flush();
    let resolve!: (value: ReturnType<typeof normalDetail>) => void;
    client.status = normalStatus({ revision: 2 });
    client.getInstance.mockImplementationOnce(() => new Promise((r) => (resolve = r)));
    store.selectInstance('instance-1');
    await flush();
    store.selectObjective('Quality');
    resolve(normalDetail('instance-1', 'Cost'));
    await flush();
    expect(store.detail()?.objective).toBe('Quality');
  });
  it('resets run-specific state on a new execution identity', async () => {
    await flush();
    store.markIssuesSeen();
    client.status = normalStatus({ runId: 'run-b', revision: 500 });
    store.refreshNow();
    await flush();
    expect(store.status()?.runId).toBe('run-b');
    expect(store.detail()?.runId).toBe('run-b');
    expect(store.seenIssueId()).toBe(0);
  });
  it('throttles chart refreshes and forces the terminal flush', async () => {
    await flush();
    client.status = normalStatus({ revision: 2 });
    store.refreshNow();
    await flush();
    expect(client.getInstance).toHaveBeenCalledTimes(1);
    client.status = normalStatus({ revision: 3, phase: 'SUCCEEDED' });
    store.refreshNow();
    await flush();
    expect(client.getInstance).toHaveBeenCalledTimes(2);
  });
  it('keeps unchanged completed chart models across status polls', async () => {
    await flush();
    const charts = store.charts();
    client.status = normalStatus({ elapsedMillis: 2000 });
    store.refreshNow();
    await flush();
    expect(store.charts()).toBe(charts);
    store.filterResults('', 'unknown algorithm', '', '');
    await flush();
    expect(store.progress().repetitionScope).toBe('selected instance');
  });
  it('passes server-side filters and pagination through without truncating the run', async () => {
    await flush();
    store.selectTab(1);
    store.filterResults('', 'Algorithm A', 'FAILED' as WorkState, 'instance');
    await flush();
    expect(client.getResults).toHaveBeenLastCalledWith(
      'run-a',
      expect.objectContaining({ algorithm: 'Algorithm A', state: 'FAILED', search: 'instance' }),
    );
    store.pageResults(25);
    await flush();
    expect(client.getResults).toHaveBeenLastCalledWith(
      'run-a',
      expect.objectContaining({ offset: 25 }),
    );
    store.sortResults('RUNTIME');
    await flush();
    expect(client.getResults).toHaveBeenLastCalledWith(
      'run-a',
      expect.objectContaining({ order: 'RUNTIME', offset: 0 }),
    );
  });
  it('does not schedule more requests after destruction', async () => {
    await flush();
    const calls = client.getStatus.mock.calls.length;
    TestBed.resetTestingModule();
    await vi.advanceTimersByTimeAsync(60000);
    expect(client.getStatus).toHaveBeenCalledTimes(calls);
  });
});
