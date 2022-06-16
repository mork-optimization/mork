import {Solution} from "./Solution";

export type EventHandler = (event: MorkEvent) => void;

export class MorkEvent {
  type: EventType = EventType.Undefined;
  eventId: number = -1;
  workerName: string = "";
}

export class ErrorEvent extends MorkEvent {
  override type = EventType.ErrorEvent;
  throwable: string = "";
}

export class PingEvent extends MorkEvent {
  override type = EventType.PingEvent;
  message: string = "";
}

export class ExecutionStartedEvent extends MorkEvent {
  override type = EventType.ExecutionStartedEvent;
  experimentNames!: string[];
  maximizing!: boolean;
}

export class ExecutionEndedEvent extends MorkEvent {
  override type = EventType.ExecutionEndedEvent;
  executionTime!: number;
}

export class ExperimentStartedEvent extends MorkEvent {
  override type = EventType.ExperimentStartedEvent;
  experimentName!: string;
  instanceNames!: string[];
}

export class ExperimentEndedEvent extends MorkEvent {
  override type = EventType.ExperimentEndedEvent;
  experimentName!: string;

  /**
   * Execution time in nanoseconds
   */
  executionTime!: number;

  /**
   * Experiment start timestamp
   */
  experimentStartTime!: number;
}

export class InstanceProcessingStartedEvent extends MorkEvent {
  override type = EventType.InstanceProcessingStartedEvent;
  experimentName!: string;
  instanceName!: string;
  algorithms!: any[];
  repetitions!: number;
  referenceValue?: number;
}

export class InstanceProcessingEndedEvent extends MorkEvent {
  override type = EventType.InstanceProcessingEndedEvent;
  experimentName!: string;
  instanceName!: string;
  executionTime!: number;
  experimentStartTime!: number;
}

export class AlgorithmProcessingStartedEvent extends MorkEvent {
  override type = EventType.AlgorithmProcessingStartedEvent;
  experimentName!: string;
  instanceName!: string;
  algorithm!: any;
  repetitions!: number;
}

export class AlgorithmProcessingEndedEvent extends MorkEvent {
  override type = EventType.AlgorithmProcessingEndedEvent;
  experimentName!: string;
  instanceName!: string;
  algorithm!: any;
  repetitions!: number;
}

export class SolutionGeneratedEvent extends MorkEvent {
  override type = EventType.SolutionGeneratedEvent;
  experimentName!: string;
  instanceName!: string;
  algorithmName!: string;
  iteration!: number;
  score!: number;
  executionTime!: number;
  timeToBest!: number;
  algorithm!: any;
  solution?: Solution;
}

export enum EventType {
  Undefined = "Undefined",
  ErrorEvent = "ErrorEvent",
  PingEvent = "PingEvent",
  ExecutionStartedEvent = "ExecutionStartedEvent",
  ExecutionEndedEvent = "ExecutionEndedEvent",
  ExperimentStartedEvent = "ExperimentStartedEvent",
  ExperimentEndedEvent = "ExperimentEndedEvent",
  InstanceProcessingStartedEvent = "InstanceProcessingStartedEvent",
  InstanceProcessingEndedEvent = "InstanceProcessingEndedEvent",
  AlgorithmProcessingStartedEvent = "AlgorithmProcessingStartedEvent",
  AlgorithmProcessingEndedEvent = "AlgorithmProcessingEndedEvent",
  SolutionGeneratedEvent = "SolutionGeneratedEvent"
}

