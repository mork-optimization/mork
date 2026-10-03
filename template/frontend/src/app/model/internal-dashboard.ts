import { InstanceProcessingStartedEvent, SolutionGeneratedEvent } from './Events';

export interface StoredSolution {
  readonly event: SolutionGeneratedEvent;
  readonly ordinal: number;
}

export interface InstanceAccumulator {
  readonly key: string;
  readonly started: InstanceProcessingStartedEvent;
  readonly solutions: StoredSolution[];
}
