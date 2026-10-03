export type ObjectiveMode = 'MINIMIZE' | 'MAXIMIZE';

export interface ErrorEvent {
  readonly exceptionType: string;
  readonly message: string | null;
}

export type PingEvent = Readonly<Record<string, never>>;

export interface ExecutionStartedEvent {
  readonly objectives: Readonly<Record<string, ObjectiveMode>>;
  readonly experimentNames: readonly string[];
}

export interface ExecutionEndedEvent {
  readonly executionTime: number;
}

export interface ExperimentStartedEvent {
  readonly experimentName: string;
  readonly instanceNames: readonly string[];
}

export interface ExperimentEndedEvent {
  readonly experimentName: string;
  readonly executionTime: number;
  readonly experimentStartTime: number;
}

export interface InstanceProcessingStartedEvent {
  readonly experimentName: string;
  readonly instanceName: string;
  readonly algorithms: readonly string[];
  readonly repetitions: number;
  readonly refValues: Readonly<Record<string, number>>;
}

export interface InstanceProcessingEndedEvent {
  readonly experimentName: string;
  readonly instanceName: string;
  readonly executionTime: number;
  readonly experimentStartTime: number;
}

export interface AlgorithmProcessingStartedEvent {
  readonly experimentName: string;
  readonly instanceName: string;
  readonly algorithmName: string;
  readonly repetitions: number;
}

export interface AlgorithmProcessingEndedEvent {
  readonly experimentName: string;
  readonly instanceName: string;
  readonly algorithmName: string;
  readonly repetitions: number;
}

export interface SolutionGeneratedEvent {
  readonly resultId: string;
  readonly success: boolean;
  readonly experimentName: string;
  readonly instanceName: string;
  readonly algorithmName: string;
  readonly iteration: string;
  readonly objectives: Readonly<Record<string, number>>;
  readonly score: number;
  readonly executionTime: number;
  readonly timeToBest: number;
}

export interface EventPayloadMap {
  readonly ErrorEvent: ErrorEvent;
  readonly PingEvent: PingEvent;
  readonly ExecutionStartedEvent: ExecutionStartedEvent;
  readonly ExecutionEndedEvent: ExecutionEndedEvent;
  readonly ExperimentStartedEvent: ExperimentStartedEvent;
  readonly ExperimentEndedEvent: ExperimentEndedEvent;
  readonly InstanceProcessingStartedEvent: InstanceProcessingStartedEvent;
  readonly InstanceProcessingEndedEvent: InstanceProcessingEndedEvent;
  readonly AlgorithmProcessingStartedEvent: AlgorithmProcessingStartedEvent;
  readonly AlgorithmProcessingEndedEvent: AlgorithmProcessingEndedEvent;
  readonly SolutionGeneratedEvent: SolutionGeneratedEvent;
}

export type KnownEventType = keyof EventPayloadMap;

export interface EventEnvelope<TType extends string, TPayload> {
  readonly eventId: number;
  readonly type: TType;
  readonly timestamp: number;
  readonly workerName: string;
  readonly payload: TPayload;
}

export type KnownEventEnvelope = {
  readonly [TType in KnownEventType]: EventEnvelope<TType, EventPayloadMap[TType]>;
}[KnownEventType];

export type UnknownEventEnvelope = EventEnvelope<string, unknown>;

export type MorkEventEnvelope = KnownEventEnvelope | UnknownEventEnvelope;

export function isEnvelopeType<TType extends KnownEventType>(
  envelope: MorkEventEnvelope,
  type: TType,
): envelope is EventEnvelope<TType, EventPayloadMap[TType]> {
  return envelope.type === type;
}
