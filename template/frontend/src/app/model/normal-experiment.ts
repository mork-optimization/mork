import { ObjectiveMode } from './Events';

export type ExecutionPhase =
  | 'IDLE'
  | 'PLANNING'
  | 'WARMING_UP'
  | 'RUNNING'
  | 'FINALIZING'
  | 'SUCCEEDED'
  | 'COMPLETED_WITH_ERRORS'
  | 'FAILED'
  | 'CANCELLED';
export type WorkState = 'PENDING' | 'RUNNING' | 'SUCCEEDED' | 'FAILED' | 'CANCELLED';
export interface Counts {
  readonly planned: number;
  readonly pending: number;
  readonly running: number;
  readonly succeeded: number;
  readonly failed: number;
  readonly cancelled: number;
}
export interface ExperimentSummary {
  readonly name: string;
  readonly counts: Counts;
  readonly instances: number;
  readonly completedInstances: number;
}
export interface NormalStatus {
  readonly runId: string | null;
  readonly revision: number;
  readonly phase: ExecutionPhase;
  readonly startedAt: number;
  readonly endedAt: number | null;
  readonly elapsedMillis: number;
  readonly currentExperiment: string | null;
  readonly objectives: Readonly<Record<string, ObjectiveMode>>;
  readonly counts: Counts;
  readonly instances: number;
  readonly completedInstances: number;
  readonly runningInstances: number;
  readonly algorithms: number;
  readonly completedAlgorithms: number;
  readonly runningAlgorithms: number;
  readonly experiments: readonly ExperimentSummary[];
  readonly issueCount: number;
}
export interface InstanceSummary {
  readonly id: string;
  readonly experimentName: string;
  readonly instanceName: string;
  readonly counts: Counts;
  readonly references: Readonly<Record<string, number>>;
  readonly algorithms: number;
  readonly completedAlgorithms: number;
  readonly finalized: boolean;
  readonly revision: number;
}
export interface Statistics {
  readonly count: number;
  readonly best: number | null;
  readonly mean: number | null;
  readonly median: number | null;
  readonly standardDeviation: number | null;
  readonly lowerQuartile: number | null;
  readonly upperQuartile: number | null;
  readonly worst: number | null;
  readonly absoluteGap: number | null;
  readonly percentageGap: number | null;
}
export interface AlgorithmSummary {
  readonly name: string;
  readonly counts: Counts;
  readonly objective: Statistics;
  readonly runtimeMillis: Statistics;
  readonly timeToBestMillis: Statistics;
  readonly bestResultId: string | null;
}
export interface ResultView {
  readonly resultId: string;
  readonly instanceId: string;
  readonly experimentName: string;
  readonly instanceName: string;
  readonly algorithmName: string;
  readonly ordinal: number;
  readonly iteration: string;
  readonly seed: number;
  readonly state: WorkState;
  readonly objectives: Readonly<Record<string, number>>;
  readonly properties: Readonly<Record<string, unknown>>;
  readonly executionTime: number | null;
  readonly timeToBest: number | null;
  readonly startedAt: number | null;
  readonly endedAt: number | null;
}
export interface ResultDetail {
  readonly runId: string;
  readonly revision: number;
  readonly result: ResultView;
  readonly issues: readonly Issue[];
  readonly rendering: {
    readonly state: 'AVAILABLE' | 'UNSUPPORTED' | 'UNAVAILABLE';
    readonly rendererId: string | null;
    readonly data: Readonly<Record<string, unknown>>;
  };
}
export interface InstanceDetail {
  readonly runId: string;
  readonly revision: number;
  readonly instance: InstanceSummary;
  readonly objective: string;
  readonly algorithms: readonly AlgorithmSummary[];
  readonly results: readonly ResultView[];
  readonly resultCount: number;
  readonly sampled: boolean;
}
export interface Page<T> {
  readonly runId: string;
  readonly revision: number;
  readonly total: number;
  readonly offset: number;
  readonly limit: number;
  readonly items: readonly T[];
}
export interface Issue {
  readonly id: number;
  readonly timestamp: number;
  readonly severity: string;
  readonly scope: string;
  readonly exceptionType: string;
  readonly message: string | null;
  readonly resultId: string | null;
  readonly experimentName: string | null;
  readonly instanceName: string | null;
  readonly algorithmName: string | null;
  readonly iteration: string | null;
  readonly seed: number | null;
}
export interface Artifact {
  readonly id: string;
  readonly experimentName: string;
  readonly resultId: string | null;
  readonly filename: string;
  readonly size: number;
  readonly modifiedAt: number;
}
export interface ArtifactManifest {
  readonly runId: string;
  readonly artifacts: readonly Artifact[];
}
export interface InstanceQuery {
  readonly experiment: string;
  readonly search: string;
  readonly offset: number;
  readonly limit: number;
}
export type ResultOrder = 'PLANNED' | 'INSTANCE' | 'ALGORITHM' | 'STATE' | 'RUNTIME';
export interface ResultQuery {
  readonly instanceId: string;
  readonly algorithm: string;
  readonly state: WorkState | '';
  readonly search: string;
  readonly offset: number;
  readonly limit: number;
  readonly order?: ResultOrder;
}
export interface MonitorStatus {
  readonly mode: 'DETECTING' | 'STANDARD' | 'TUNING' | 'WORKER' | 'UTILITY' | 'AUTOLLM';
  readonly eventLogId: string;
}
