import { DestroyRef, Injectable, Signal, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  ConnectionState,
  ErrorModel,
  InstanceDashboardModel,
  ObjectiveModel,
  ProgressModel,
} from '../model/dashboard';
import { InstanceAccumulator } from '../model/internal-dashboard';
import {
  type ExecutionStartedEvent,
  type InstanceProcessingStartedEvent,
  type MorkEventEnvelope,
  type ObjectiveMode,
  type SolutionGeneratedEvent,
  isEnvelopeType,
} from '../model/Events';
import { EventClient } from '../service/event-client';
import { ChartUtil } from '../util/chart-util';

const EVENT_BATCH_SIZE = 1_000;
const CHART_PUBLICATION_INTERVAL_MS = 2_000;
const MAX_VISIBLE_INSTANCES = 10;

const EMPTY_PROGRESS: ProgressModel = {
  instances: { completed: 0, total: 0 },
  algorithms: { completed: 0, total: 0 },
  repetitions: { completed: 0, total: 0 },
};

@Injectable({ providedIn: 'root' })
export class DashboardStore {
  private readonly eventClient = inject(EventClient);
  private readonly destroyRef = inject(DestroyRef);

  private readonly connectionStateSignal = signal<ConnectionState>('connecting');
  private readonly currentExperimentSignal = signal<string | null>(null);
  private readonly processedEventCountSignal = signal(0);
  private readonly lastEventIdSignal = signal(-1);
  private readonly objectivesSignal = signal<readonly ObjectiveModel[]>([]);
  private readonly selectedObjectiveSignal = signal<string | null>(null);
  private readonly progressSignal = signal<ProgressModel>(EMPTY_PROGRESS);
  private readonly latestErrorSignal = signal<ErrorModel | null>(null);
  private readonly chartRevision = signal(0);

  private readonly pendingEnvelopes = new Map<number, MorkEventEnvelope>();
  private readonly instanceAccumulators: InstanceAccumulator[] = [];
  private synchronization: Promise<void> | null = null;
  private chartTimer: ReturnType<typeof setTimeout> | null = null;
  private currentInstance: InstanceAccumulator | null = null;
  private lastChartPublication = 0;
  private started = false;
  private socketConnected = false;
  private synchronizeAgain = false;
  private executionFinished = false;

  readonly connectionState = this.connectionStateSignal.asReadonly();
  readonly connectionLabel = computed(() => this.labelFor(this.connectionStateSignal()));
  readonly currentExperiment = this.currentExperimentSignal.asReadonly();
  readonly processedEventCount = this.processedEventCountSignal.asReadonly();
  readonly lastEventId = this.lastEventIdSignal.asReadonly();
  readonly objectives = this.objectivesSignal.asReadonly();
  readonly selectedObjective = this.selectedObjectiveSignal.asReadonly();
  readonly progress = this.progressSignal.asReadonly();
  readonly latestError = this.latestErrorSignal.asReadonly();
  readonly instances: Signal<readonly InstanceDashboardModel[]> = computed(() => {
    this.chartRevision();
    const objectiveName = this.selectedObjectiveSignal();
    const objectiveMode = this.objectiveMode(objectiveName);
    const models: InstanceDashboardModel[] = [];
    for (const instance of this.instanceAccumulators) {
      models.push(ChartUtil.buildInstance(instance, objectiveName, objectiveMode));
    }
    return models;
  });

  constructor() {
    this.destroyRef.onDestroy(() => this.cancelChartPublication());
  }

  start(): void {
    if (this.started) {
      return;
    }
    this.started = true;
    this.connectionStateSignal.set('connecting');

    this.eventClient.envelopes$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((envelope) => this.receiveEnvelope(envelope));
    this.eventClient.connected$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.handleConnected());
    this.eventClient.disconnected$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.handleDisconnected());
    this.eventClient.errors$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((error) => this.handleTransportError(error));

    this.eventClient.start();
  }

  selectObjective(name: string): void {
    let available = false;
    for (const objective of this.objectivesSignal()) {
      if (objective.name === name) {
        available = true;
        break;
      }
    }
    if (!available || this.selectedObjectiveSignal() === name) {
      return;
    }
    this.selectedObjectiveSignal.set(name);
    this.publishCharts();
  }

  private handleConnected(): void {
    this.socketConnected = true;
    this.requestSynchronization();
  }

  private handleDisconnected(): void {
    this.socketConnected = false;
    if (!this.executionFinished) {
      this.connectionStateSignal.set('disconnected');
    }
  }

  private handleTransportError(error: unknown): void {
    this.latestErrorSignal.set({
      exceptionType: 'ConnectionError',
      message: error instanceof Error ? error.message : String(error),
    });
    this.connectionStateSignal.set('error');
  }

  private receiveEnvelope(envelope: MorkEventEnvelope): void {
    if (!Number.isInteger(envelope.eventId) || envelope.eventId < 0) {
      return;
    }

    if (envelope.eventId === 0 && this.lastEventIdSignal() >= 0) {
      this.resetAfterBackendRestart();
    }
    if (envelope.eventId <= this.lastEventIdSignal()) {
      return;
    }

    this.pendingEnvelopes.set(envelope.eventId, envelope);
    if (this.connectionStateSignal() === 'live' || this.connectionStateSignal() === 'finished') {
      this.drainContiguousEnvelopes();
      if (this.hasDeliveryGap()) {
        this.requestSynchronization();
      }
    }
  }

  private requestSynchronization(): void {
    if (!this.socketConnected) {
      return;
    }
    if (this.synchronization) {
      this.synchronizeAgain = true;
      return;
    }

    this.synchronizeAgain = false;
    this.synchronization = this.synchronize()
      .catch((error: unknown) => this.handleTransportError(error))
      .finally(() => {
        this.synchronization = null;
        if (this.synchronizeAgain && this.socketConnected) {
          this.requestSynchronization();
        }
      });
  }

  private async synchronize(): Promise<void> {
    this.connectionStateSignal.set('syncing');
    const latest = await this.eventClient.getLastEvent();
    if (!this.socketConnected) {
      return;
    }

    if (!latest && this.lastEventIdSignal() >= 0) {
      this.resetAfterBackendRestart();
    } else if (latest && latest.eventId < this.lastEventIdSignal()) {
      this.resetAfterBackendRestart();
    }

    if (latest) {
      await this.downloadRange(this.lastEventIdSignal() + 1, latest.eventId + 1);
    }
    if (!this.socketConnected) {
      return;
    }

    this.connectionStateSignal.set('replaying');
    this.drainContiguousEnvelopes();
    await this.downloadPendingGaps();
    this.drainContiguousEnvelopes();

    if (!this.socketConnected) {
      return;
    }
    this.connectionStateSignal.set(this.executionFinished ? 'finished' : 'live');
    this.publishCharts();
  }

  private async downloadRange(from: number, to: number): Promise<void> {
    let batchStart = from;
    while (batchStart < to) {
      const batchEnd = Math.min(to, batchStart + EVENT_BATCH_SIZE);
      const envelopes = await this.eventClient.getEvents(batchStart, batchEnd);
      for (const envelope of envelopes) {
        if (envelope.eventId > this.lastEventIdSignal()) {
          this.pendingEnvelopes.set(envelope.eventId, envelope);
        }
      }
      batchStart = batchEnd;
    }
  }

  private async downloadPendingGaps(): Promise<void> {
    while (true) {
      this.drainContiguousEnvelopes();
      const nextBufferedId = this.smallestPendingId();
      const expectedId = this.lastEventIdSignal() + 1;
      if (nextBufferedId === null || nextBufferedId <= expectedId) {
        return;
      }

      const previousSize = this.pendingEnvelopes.size;
      await this.downloadRange(expectedId, nextBufferedId);
      if (this.pendingEnvelopes.size === previousSize) {
        return;
      }
    }
  }

  private drainContiguousEnvelopes(): void {
    let nextId = this.lastEventIdSignal() + 1;
    let envelope = this.pendingEnvelopes.get(nextId);
    while (envelope) {
      this.pendingEnvelopes.delete(nextId);
      this.processEnvelope(envelope);
      nextId = this.lastEventIdSignal() + 1;
      envelope = this.pendingEnvelopes.get(nextId);
    }
  }

  private processEnvelope(envelope: MorkEventEnvelope): void {
    this.lastEventIdSignal.set(envelope.eventId);
    this.processedEventCountSignal.update((count) => count + 1);

    if (isEnvelopeType(envelope, 'ExecutionStartedEvent')) {
      this.handleExecutionStarted(envelope.payload);
    } else if (isEnvelopeType(envelope, 'ExecutionEndedEvent')) {
      this.handleExecutionEnded();
    } else if (isEnvelopeType(envelope, 'ExperimentStartedEvent')) {
      this.currentExperimentSignal.set(envelope.payload.experimentName);
      this.progressSignal.set({
        instances: { completed: 0, total: envelope.payload.instanceNames.length },
        algorithms: { completed: 0, total: 0 },
        repetitions: { completed: 0, total: 0 },
      });
    } else if (isEnvelopeType(envelope, 'ExperimentEndedEvent')) {
      this.currentExperimentSignal.set(null);
    } else if (isEnvelopeType(envelope, 'InstanceProcessingStartedEvent')) {
      this.handleInstanceStarted(envelope.eventId, envelope.payload);
    } else if (isEnvelopeType(envelope, 'InstanceProcessingEndedEvent')) {
      this.handleInstanceEnded();
    } else if (isEnvelopeType(envelope, 'AlgorithmProcessingStartedEvent')) {
      const progress = this.progressSignal();
      this.progressSignal.set({
        ...progress,
        repetitions: { completed: 0, total: envelope.payload.repetitions },
      });
    } else if (isEnvelopeType(envelope, 'AlgorithmProcessingEndedEvent')) {
      this.handleAlgorithmEnded();
    } else if (isEnvelopeType(envelope, 'SolutionGeneratedEvent')) {
      this.handleSolution(envelope.payload);
    } else if (isEnvelopeType(envelope, 'ErrorEvent')) {
      this.latestErrorSignal.set({
        exceptionType: envelope.payload.exceptionType,
        message: envelope.payload.message,
      });
    }
  }

  private handleExecutionStarted(payload: ExecutionStartedEvent): void {
    this.clearPresentationState();
    this.executionFinished = false;
    const objectives: ObjectiveModel[] = [];
    for (const [name, mode] of Object.entries(payload.objectives)) {
      objectives.push({ name, mode });
    }
    this.objectivesSignal.set(objectives);
    this.selectedObjectiveSignal.set(objectives[0]?.name ?? null);
    this.publishCharts();
  }

  private handleExecutionEnded(): void {
    this.executionFinished = true;
    const progress = this.progressSignal();
    this.progressSignal.set({
      instances: { completed: progress.instances.total, total: progress.instances.total },
      algorithms: { completed: progress.algorithms.total, total: progress.algorithms.total },
      repetitions: { completed: progress.repetitions.total, total: progress.repetitions.total },
    });
    this.connectionStateSignal.set('finished');
    this.publishCharts();
  }

  private handleInstanceStarted(eventId: number, payload: InstanceProcessingStartedEvent): void {
    const accumulator: InstanceAccumulator = {
      key: `${payload.experimentName}:${payload.instanceName}:${eventId}`,
      started: payload,
      solutions: [],
    };
    this.instanceAccumulators.unshift(accumulator);
    if (this.instanceAccumulators.length > MAX_VISIBLE_INSTANCES) {
      this.instanceAccumulators.length = MAX_VISIBLE_INSTANCES;
    }
    this.currentInstance = accumulator;

    const progress = this.progressSignal();
    this.progressSignal.set({
      instances: progress.instances,
      algorithms: { completed: 0, total: payload.algorithms.length },
      repetitions: { completed: 0, total: payload.repetitions },
    });
    this.publishCharts();
  }

  private handleInstanceEnded(): void {
    const progress = this.progressSignal();
    this.progressSignal.set({
      instances: {
        completed: Math.min(progress.instances.total, progress.instances.completed + 1),
        total: progress.instances.total,
      },
      algorithms: { completed: progress.algorithms.total, total: progress.algorithms.total },
      repetitions: { completed: progress.repetitions.total, total: progress.repetitions.total },
    });
    this.currentInstance = null;
    this.publishCharts();
  }

  private handleAlgorithmEnded(): void {
    const progress = this.progressSignal();
    this.progressSignal.set({
      ...progress,
      algorithms: {
        completed: Math.min(progress.algorithms.total, progress.algorithms.completed + 1),
        total: progress.algorithms.total,
      },
      repetitions: { completed: progress.repetitions.total, total: progress.repetitions.total },
    });
  }

  private handleSolution(payload: SolutionGeneratedEvent): void {
    const progress = this.progressSignal();
    this.progressSignal.set({
      ...progress,
      repetitions: {
        completed: progress.repetitions.completed + 1,
        total: progress.repetitions.total,
      },
    });

    const instance = this.findInstance(payload.experimentName, payload.instanceName);
    if (!instance) {
      return;
    }
    let ordinal = 1;
    for (const stored of instance.solutions) {
      if (stored.event.algorithmName === payload.algorithmName) {
        ordinal++;
      }
    }
    instance.solutions.push({ event: payload, ordinal });
    this.scheduleChartPublication();
  }

  private findInstance(experimentName: string, instanceName: string): InstanceAccumulator | null {
    if (
      this.currentInstance?.started.experimentName === experimentName &&
      this.currentInstance.started.instanceName === instanceName
    ) {
      return this.currentInstance;
    }
    for (const instance of this.instanceAccumulators) {
      if (
        instance.started.experimentName === experimentName &&
        instance.started.instanceName === instanceName
      ) {
        return instance;
      }
    }
    return null;
  }

  private scheduleChartPublication(): void {
    if (this.chartTimer) {
      return;
    }
    const elapsed = Date.now() - this.lastChartPublication;
    const delay = Math.max(0, CHART_PUBLICATION_INTERVAL_MS - elapsed);
    this.chartTimer = setTimeout(() => this.publishCharts(), delay);
  }

  private publishCharts(): void {
    this.cancelChartPublication();
    this.lastChartPublication = Date.now();
    this.chartRevision.update((revision) => revision + 1);
  }

  private cancelChartPublication(): void {
    if (this.chartTimer) {
      clearTimeout(this.chartTimer);
      this.chartTimer = null;
    }
  }

  private resetAfterBackendRestart(): void {
    this.pendingEnvelopes.clear();
    this.lastEventIdSignal.set(-1);
    this.processedEventCountSignal.set(0);
    this.clearPresentationState();
    this.executionFinished = false;
  }

  private clearPresentationState(): void {
    this.cancelChartPublication();
    this.instanceAccumulators.length = 0;
    this.currentInstance = null;
    this.currentExperimentSignal.set(null);
    this.objectivesSignal.set([]);
    this.selectedObjectiveSignal.set(null);
    this.progressSignal.set(EMPTY_PROGRESS);
    this.latestErrorSignal.set(null);
    this.chartRevision.update((revision) => revision + 1);
  }

  private hasDeliveryGap(): boolean {
    const smallestId = this.smallestPendingId();
    return smallestId !== null && smallestId > this.lastEventIdSignal() + 1;
  }

  private smallestPendingId(): number | null {
    let smallest: number | null = null;
    for (const eventId of this.pendingEnvelopes.keys()) {
      if (smallest === null || eventId < smallest) {
        smallest = eventId;
      }
    }
    return smallest;
  }

  private objectiveMode(objectiveName: string | null): ObjectiveMode | null {
    if (!objectiveName) {
      return null;
    }
    for (const objective of this.objectivesSignal()) {
      if (objective.name === objectiveName) {
        return objective.mode;
      }
    }
    return null;
  }

  private labelFor(state: ConnectionState): string {
    switch (state) {
      case 'connecting':
        return 'Connecting';
      case 'syncing':
        return 'Synchronizing history';
      case 'replaying':
        return 'Replaying events';
      case 'live':
        return 'Live';
      case 'disconnected':
        return 'Disconnected — retrying';
      case 'finished':
        return 'Finished';
      case 'error':
        return 'Connection error';
    }
  }
}
