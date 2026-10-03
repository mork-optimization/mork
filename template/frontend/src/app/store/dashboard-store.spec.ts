import { TestBed } from '@angular/core/testing';
import { Subject } from 'rxjs';
import { EventClient } from '../service/event-client';
import { MorkEventEnvelope } from '../model/Events';
import { DashboardStore } from './dashboard-store';

class FakeEventClient {
  readonly envelopesSubject = new Subject<MorkEventEnvelope>();
  readonly connectedSubject = new Subject<void>();
  readonly disconnectedSubject = new Subject<void>();
  readonly errorsSubject = new Subject<unknown>();

  readonly envelopes$ = this.envelopesSubject.asObservable();
  readonly connected$ = this.connectedSubject.asObservable();
  readonly disconnected$ = this.disconnectedSubject.asObservable();
  readonly errors$ = this.errorsSubject.asObservable();

  history: MorkEventEnvelope[] = [];
  readonly start = vi.fn();
  readonly getLastEvent = vi.fn(async (): Promise<MorkEventEnvelope | null> => {
    return this.history.at(-1) ?? null;
  });
  readonly getEvents = vi.fn(
    async (from: number, to: number): Promise<readonly MorkEventEnvelope[]> => {
      return this.history.filter((event) => event.eventId >= from && event.eventId < to);
    },
  );
}

function envelope(eventId: number, type: string, payload: object = {}): MorkEventEnvelope {
  return {
    eventId,
    type,
    timestamp: eventId + 1_000,
    workerName: 'test-worker',
    payload,
  } as MorkEventEnvelope;
}

function executionStarted(
  eventId: number,
  objectives: Readonly<Record<string, 'MINIMIZE' | 'MAXIMIZE'>> = {
    Cost: 'MINIMIZE',
    Quality: 'MAXIMIZE',
  },
): MorkEventEnvelope {
  return envelope(eventId, 'ExecutionStartedEvent', {
    objectives,
    experimentNames: ['Benchmark'],
  });
}

function experimentStarted(eventId: number, instanceNames = ['alpha.dat']): MorkEventEnvelope {
  return envelope(eventId, 'ExperimentStartedEvent', {
    experimentName: 'Benchmark',
    instanceNames,
  });
}

function instanceStarted(eventId: number, instanceName = 'alpha.dat'): MorkEventEnvelope {
  return envelope(eventId, 'InstanceProcessingStartedEvent', {
    experimentName: 'Benchmark',
    instanceName,
    algorithms: ['Greedy'],
    repetitions: 3,
    refValues: { Cost: 6, Quality: 6 },
  });
}

function algorithmStarted(eventId: number, instanceName = 'alpha.dat'): MorkEventEnvelope {
  return envelope(eventId, 'AlgorithmProcessingStartedEvent', {
    experimentName: 'Benchmark',
    instanceName,
    algorithmName: 'Greedy',
    repetitions: 3,
  });
}

function solution(
  eventId: number,
  resultId: string,
  success: boolean,
  cost: number,
  quality: number,
  iteration: string,
): MorkEventEnvelope {
  return envelope(eventId, 'SolutionGeneratedEvent', {
    resultId,
    success,
    experimentName: 'Benchmark',
    instanceName: 'alpha.dat',
    algorithmName: 'Greedy',
    iteration,
    objectives: { Cost: cost, Quality: quality },
    score: cost,
    executionTime: 10,
    timeToBest: 5,
  });
}

async function connect(store: DashboardStore, client: FakeEventClient): Promise<void> {
  store.start();
  client.connectedSubject.next();
  await vi.waitFor(() => {
    expect(['live', 'finished']).toContain(store.connectionState());
  });
}

describe('DashboardStore', () => {
  let client: FakeEventClient;
  let store: DashboardStore;

  beforeEach(() => {
    client = new FakeEventClient();
    TestBed.configureTestingModule({
      providers: [DashboardStore, { provide: EventClient, useValue: client }],
    });
    store = TestBed.inject(DashboardStore);
  });

  afterEach(() => TestBed.resetTestingModule());

  it('reduces the full lifecycle, counts failures, and recomputes charts for each objective', async () => {
    client.history = [
      executionStarted(0),
      experimentStarted(1),
      instanceStarted(2),
      algorithmStarted(3),
      solution(4, '00000000-0000-0000-0000-000000000004', false, 1, 100, 'failed-iteration'),
      solution(5, '00000000-0000-0000-0000-000000000005', true, 10, 5, 'phase-A'),
      solution(6, '00000000-0000-0000-0000-000000000006', true, 7, 3, 'phase-B'),
      envelope(7, 'AlgorithmProcessingEndedEvent', {
        experimentName: 'Benchmark',
        instanceName: 'alpha.dat',
        algorithmName: 'Greedy',
        repetitions: 3,
      }),
      envelope(8, 'InstanceProcessingEndedEvent', {
        experimentName: 'Benchmark',
        instanceName: 'alpha.dat',
        executionTime: 20,
        experimentStartTime: 1,
      }),
      envelope(9, 'ExperimentEndedEvent', {
        experimentName: 'Benchmark',
        executionTime: 30,
        experimentStartTime: 1,
      }),
      envelope(10, 'ExecutionEndedEvent', { executionTime: 40 }),
    ];

    await connect(store, client);

    expect(store.connectionState()).toBe('finished');
    expect(store.processedEventCount()).toBe(11);
    expect(store.progress()).toEqual({
      instances: { completed: 1, total: 1 },
      algorithms: { completed: 1, total: 1 },
      repetitions: { completed: 3, total: 3 },
    });
    expect(store.selectedObjective()).toBe('Cost');
    expect(store.instances()[0]?.bestSolution?.resultId).toBe(
      '00000000-0000-0000-0000-000000000006',
    );

    const costSeries = store.instances()[0]?.convergenceChart.options.series?.[0] as {
      data: Array<{ x: number; y: number; custom: { iteration: string } }>;
    };
    expect(costSeries.data).toEqual([
      { x: 2, y: 10, custom: { iteration: 'phase-A' } },
      { x: 3, y: 7, custom: { iteration: 'phase-B' } },
    ]);

    store.selectObjective('Quality');
    expect(store.instances()[0]?.bestSolution?.resultId).toBe(
      '00000000-0000-0000-0000-000000000005',
    );
    const qualitySeries = store.instances()[0]?.convergenceChart.options.series?.[0] as {
      data: Array<{ x: number; y: number; custom: { iteration: string } }>;
    };
    expect(qualitySeries.data).toEqual([
      { x: 2, y: 5, custom: { iteration: 'phase-A' } },
      { x: 3, y: 5, custom: { iteration: 'phase-B' } },
    ]);
  });

  it('advances repetition progress for a failed result but excludes it from charts', async () => {
    await connect(store, client);
    client.envelopesSubject.next(executionStarted(0));
    client.envelopesSubject.next(experimentStarted(1));
    client.envelopesSubject.next(instanceStarted(2));
    client.envelopesSubject.next(algorithmStarted(3));
    client.envelopesSubject.next(
      solution(4, '00000000-0000-0000-0000-000000000004', false, 1, 100, 'failed'),
    );

    expect(store.progress().repetitions).toEqual({ completed: 1, total: 3 });
    store.selectObjective('Quality');
    expect(store.instances()[0]?.bestSolution).toBeNull();
    const series = store.instances()[0]?.scoreChart.options.series?.[0] as { data: unknown[] };
    expect(series.data).toEqual([]);
  });

  it('throttles chart publication and flushes immediately when an instance completes', async () => {
    await connect(store, client);
    client.envelopesSubject.next(executionStarted(0));
    client.envelopesSubject.next(experimentStarted(1));
    client.envelopesSubject.next(instanceStarted(2));
    client.envelopesSubject.next(algorithmStarted(3));
    expect(store.instances()[0]?.scoreChart.options.series?.[0]).toMatchObject({ data: [] });

    vi.useFakeTimers();
    try {
      client.envelopesSubject.next(
        solution(4, '00000000-0000-0000-0000-000000000004', true, 10, 5, 'first'),
      );
      expect(store.instances()[0]?.scoreChart.options.series?.[0]).toMatchObject({ data: [] });

      vi.advanceTimersByTime(2_000);
      expect(store.instances()[0]?.scoreChart.options.series?.[0]).toMatchObject({
        data: [{ x: 1, y: 10, custom: { iteration: 'first' } }],
      });

      client.envelopesSubject.next(
        solution(5, '00000000-0000-0000-0000-000000000005', true, 7, 3, 'second'),
      );
      client.envelopesSubject.next(
        envelope(6, 'InstanceProcessingEndedEvent', {
          experimentName: 'Benchmark',
          instanceName: 'alpha.dat',
          executionTime: 20,
          experimentStartTime: 1,
        }),
      );
      expect(store.instances()[0]?.scoreChart.options.series?.[0]).toMatchObject({
        data: [
          { x: 1, y: 10, custom: { iteration: 'first' } },
          { x: 2, y: 7, custom: { iteration: 'second' } },
        ],
      });
    } finally {
      vi.useRealTimers();
    }
  });

  it('downloads history in exact 1,000-event batches', async () => {
    for (let eventId = 0; eventId <= 2_000; eventId++) {
      client.history.push(envelope(eventId, 'PingEvent'));
    }

    await connect(store, client);

    expect(client.getEvents.mock.calls).toEqual([
      [0, 1_000],
      [1_000, 2_000],
      [2_000, 2_001],
    ]);
    expect(store.processedEventCount()).toBe(2_001);
    expect(store.lastEventId()).toBe(2_000);
  });

  it('merges events that arrive while history is downloading', async () => {
    client.history = [envelope(0, 'PingEvent'), envelope(1, 'PingEvent'), envelope(2, 'PingEvent')];
    let emittedLiveEvent = false;
    client.getEvents.mockImplementation(async (from: number, to: number) => {
      if (!emittedLiveEvent) {
        emittedLiveEvent = true;
        client.envelopesSubject.next(envelope(3, 'CustomEvent', { value: 42 }));
      }
      return client.history.filter((event) => event.eventId >= from && event.eventId < to);
    });

    await connect(store, client);

    expect(store.lastEventId()).toBe(3);
    expect(store.processedEventCount()).toBe(4);
  });

  it('deduplicates, reorders, and fills a live delivery gap', async () => {
    await connect(store, client);
    client.history = [envelope(0, 'PingEvent')];
    client.envelopesSubject.next(client.history[0]!);

    client.history.push(envelope(1, 'PingEvent'), envelope(2, 'PingEvent'));
    client.envelopesSubject.next(client.history[2]!);

    await vi.waitFor(() => expect(store.lastEventId()).toBe(2));
    client.envelopesSubject.next(client.history[1]!);

    expect(store.processedEventCount()).toBe(3);
  });

  it('catches up after reconnect and resets state when backend event IDs restart', async () => {
    client.history = [executionStarted(0), envelope(1, 'PingEvent')];
    await connect(store, client);
    expect(store.lastEventId()).toBe(1);

    client.disconnectedSubject.next();
    expect(store.connectionState()).toBe('disconnected');
    client.history = [executionStarted(0, { Distance: 'MINIMIZE' })];
    client.connectedSubject.next();

    await vi.waitFor(() => expect(store.connectionState()).toBe('live'));
    expect(store.lastEventId()).toBe(0);
    expect(store.processedEventCount()).toBe(1);
    expect(store.objectives()).toEqual([{ name: 'Distance', mode: 'MINIMIZE' }]);
  });

  it('clears stale execution state when reconnecting to an empty restarted backend', async () => {
    client.history = [executionStarted(0), envelope(1, 'PingEvent')];
    await connect(store, client);

    client.disconnectedSubject.next();
    client.history = [];
    client.connectedSubject.next();

    await vi.waitFor(() => expect(store.connectionState()).toBe('live'));
    expect(store.lastEventId()).toBe(-1);
    expect(store.processedEventCount()).toBe(0);
    expect(store.objectives()).toEqual([]);
  });

  it('retains exactly the ten newest instances', async () => {
    client.history.push(executionStarted(0));
    const names: string[] = [];
    for (let index = 0; index < 11; index++) {
      names.push(`instance-${index}`);
    }
    client.history.push(experimentStarted(1, names));
    for (let index = 0; index < 11; index++) {
      client.history.push(instanceStarted(index + 2, names[index]));
    }

    await connect(store, client);

    expect(store.instances()).toHaveLength(10);
    expect(store.instances()[0]?.instanceName).toBe('instance-10');
    expect(store.instances()[9]?.instanceName).toBe('instance-1');
  });

  it('advances accounting for pings and unknown events and surfaces ErrorEvent', async () => {
    client.history = [
      envelope(0, 'PingEvent'),
      envelope(1, 'PluginSpecificEvent', { plugin: true }),
      envelope(2, 'ErrorEvent', { exceptionType: 'IllegalStateException', message: 'boom' }),
    ];

    await connect(store, client);

    expect(store.processedEventCount()).toBe(3);
    expect(store.latestError()).toEqual({
      exceptionType: 'IllegalStateException',
      message: 'boom',
    });
  });
});
