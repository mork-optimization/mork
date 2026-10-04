import { TestBed } from '@angular/core/testing';
import {
  ArtifactManifest,
  AutoconfigStatus,
  EliteHistorySnapshot,
  EliteSnapshot,
  EvaluationChangePage,
  SearchSpaceSnapshot,
} from '../model/autoconfig';
import { AutoconfigClient } from '../service/autoconfig-client';
import { AutoconfigStore } from './autoconfig-store';

function status(overrides: Partial<AutoconfigStatus> = {}): AutoconfigStatus {
  return {
    runId: 'run-1',
    mode: 'AUTOCONFIG',
    role: 'COORDINATOR',
    state: 'RUNNING',
    phase: 'RACING',
    preparedAt: '2026-10-04T00:00:00Z',
    startedAt: '2026-10-04T00:00:01Z',
    finishedAt: null,
    elapsedMillis: 5_000,
    budget: { maximum: 100, used: 2, remaining: 98 },
    evaluations: { running: 0, succeeded: 1, rejected: 1, failed: 0, slow: 1 },
    latestEvaluationRevision: 2,
    generatedParameterCount: 4,
    trainingInstanceCount: 12,
    metric: {
      objectiveName: 'cost',
      objectiveMode: 'MINIMIZE',
      kind: 'OBJECTIVE',
      costMode: 'MINIMIZE',
      negated: false,
      auc: null,
    },
    irace: {
      iteration: 1,
      eliteCount: 1,
      updatedAt: '2026-10-04T00:00:05Z',
      finalSnapshot: false,
      progress: null,
    },
    failure: null,
    ...overrides,
  };
}

class FakeAutoconfigClient {
  currentStatus = status();
  changePage: EvaluationChangePage = {
    runId: 'run-1',
    latestRevision: 2,
    nextRevision: 2,
    changes: [
      {
        revision: 1,
        evaluation: {
          id: 1,
          configurationId: '7',
          instanceId: 'i-1',
          instanceName: 'instances/one.dat',
          seed: 12,
          state: 'SUCCEEDED',
          cost: 4.5,
          timeSeconds: 0.2,
          startedAt: '2026-10-04T00:00:02Z',
          finishedAt: '2026-10-04T00:00:03Z',
          reasonCode: null,
          message: null,
          slow: false,
          slowOverrunMillis: null,
        },
      },
      {
        revision: 2,
        evaluation: {
          id: 2,
          configurationId: '8',
          instanceId: 'i-2',
          instanceName: 'instances/two.dat',
          seed: 13,
          state: 'REJECTED',
          cost: null,
          timeSeconds: 10,
          startedAt: '2026-10-04T00:00:03Z',
          finishedAt: '2026-10-04T00:00:04Z',
          reasonCode: 'TIMEOUT',
          message: 'Evaluation exceeded its limit',
          slow: true,
          slowOverrunMillis: 500,
        },
      },
    ],
  };
  readonly getStatus = vi.fn(async () => this.currentStatus);
  readonly getEvaluationChanges = vi.fn(async () => this.changePage);
  readonly getElites = vi.fn(async (): Promise<EliteSnapshot> => ({
    runId: this.currentStatus.runId,
    iteration: 1,
    updatedAt: '2026-10-04T00:00:05Z',
    finalSnapshot: false,
    elites: [{ configurationId: '7', position: 1, parameters: { alpha: '1' }, algorithm: null }],
  }));
  readonly getEliteHistory = vi.fn(async (): Promise<EliteHistorySnapshot> => ({
    runId: this.currentStatus.runId,
    iterations: [],
  }));
  readonly getSearchSpace = vi.fn(async (): Promise<SearchSpaceSnapshot> => ({
    limits: { treeDepth: 4, maxDerivationRepetition: 2 },
    summary: {
      rootCount: 1,
      componentCount: 1,
      parameterCount: 1,
      combinationParameterCount: 0,
      generatedIraceParameterCount: 4,
      generatedForbiddenConstraintCount: 0,
    },
    roots: ['Algorithm'],
    components: [],
    encodingDiagnostics: null,
  }));
  readonly getArtifacts = vi.fn(async (): Promise<ArtifactManifest> => ({
    runId: this.currentStatus.runId!,
    artifacts: [],
  }));
  readonly getCandidate = vi.fn();
  readonly artifactUrl = vi.fn((id: string) => `/api/autoconfig/artifacts/${id}`);
}

describe('AutoconfigStore', () => {
  let client: FakeAutoconfigClient;
  let store: AutoconfigStore;

  beforeEach(() => {
    vi.useFakeTimers();
    localStorage.clear();
    client = new FakeAutoconfigClient();
    TestBed.configureTestingModule({
      providers: [AutoconfigStore, { provide: AutoconfigClient, useValue: client }],
    });
    store = TestBed.inject(AutoconfigStore);
  });

  afterEach(() => {
    TestBed.resetTestingModule();
    vi.useRealTimers();
  });

  it('detects tuning mode and incrementally publishes evaluations, elites, and alerts', async () => {
    store.start();
    await vi.advanceTimersByTimeAsync(0);

    expect(store.viewMode()).toBe('tuning');
    expect(store.syncState()).toBe('live');
    expect(client.getEvaluationChanges).toHaveBeenCalledWith(0, 500);
    expect(store.evaluations()).toHaveLength(2);
    expect(store.elites()?.elites[0].configurationId).toBe('7');
    expect(store.searchSpace()?.summary.generatedIraceParameterCount).toBe(4);
    expect(store.unseenAlertCount()).toBe(1);
    expect(store.instanceSummaries()[0]).toMatchObject({ issues: 1, slow: 1 });

    store.markAlertsSeen();
    expect(store.unseenAlertCount()).toBe(0);
  });

  it('switches to the standard dashboard when autoconfig is disabled', async () => {
    client.currentStatus = status({
      runId: null,
      mode: 'DISABLED',
      role: 'DISABLED',
      state: 'NOT_STARTED',
      phase: 'NOT_STARTED',
      latestEvaluationRevision: 0,
    });

    store.start();
    await vi.advanceTimersByTimeAsync(0);

    expect(store.viewMode()).toBe('standard');
    expect(client.getEvaluationChanges).not.toHaveBeenCalled();
  });

  it('clears the previous run when the backend run identifier changes', async () => {
    store.start();
    await vi.advanceTimersByTimeAsync(0);
    expect(store.evaluations()).toHaveLength(2);

    client.currentStatus = status({
      runId: 'run-2',
      latestEvaluationRevision: 0,
      budget: { maximum: 50, used: 0, remaining: 50 },
      irace: {
        iteration: null,
        eliteCount: 0,
        updatedAt: null,
        finalSnapshot: false,
        progress: null,
      },
    });
    await vi.advanceTimersByTimeAsync(2_000);

    expect(store.status()?.runId).toBe('run-2');
    expect(store.evaluations()).toEqual([]);
    expect(store.unseenAlertCount()).toBe(0);
  });
});
