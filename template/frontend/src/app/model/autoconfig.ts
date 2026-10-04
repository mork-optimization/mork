export type AutoconfigMode = 'DISABLED' | 'IRACE' | 'AUTOCONFIG';
export type AutoconfigRole = 'COORDINATOR' | 'WORKER' | 'DISABLED';
export type AutoconfigState = 'NOT_STARTED' | 'PREPARING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
export type AutoconfigPhase =
  | 'NOT_STARTED'
  | 'PREPARING'
  | 'CHECKING_SCENARIO'
  | 'RACING'
  | 'POSTPROCESSING'
  | 'WAITING_FOR_WORK'
  | 'COMPLETED'
  | 'FAILED';
export type EvaluationState = 'RUNNING' | 'SUCCEEDED' | 'REJECTED' | 'FAILED';
export type CostMetricKind = 'OBJECTIVE' | 'AREA_UNDER_CURVE';

export interface AutoconfigBudget {
  readonly maximum: number;
  readonly used: number;
  readonly remaining: number;
}

export interface EvaluationCounts {
  readonly running: number;
  readonly succeeded: number;
  readonly rejected: number;
  readonly failed: number;
  readonly slow: number;
}

export interface AucSettings {
  readonly ignoreInitialMillis: number;
  readonly intervalDurationMillis: number;
  readonly logScale: boolean;
}

export interface CostMetric {
  readonly objectiveName: string;
  readonly objectiveMode: 'MINIMIZE' | 'MAXIMIZE';
  readonly kind: CostMetricKind;
  readonly costMode: 'MINIMIZE';
  readonly negated: boolean;
  readonly auc: AucSettings | null;
}

export interface IraceProgressDetails {
  readonly nbIterations: number;
  readonly maxExperiments: number;
  readonly experimentsUsed: number;
  readonly remainingBudget: number;
  readonly remainingBudgetEstimated: boolean;
  readonly currentBudget: number;
  readonly currentBudgetUsed: number;
  readonly maxTime: number;
  readonly timeUsed: number;
  readonly remainingTime: number | null;
  readonly boundEstimate: number | null;
}

export interface AutoconfigStatus {
  readonly runId: string | null;
  readonly mode: AutoconfigMode;
  readonly role: AutoconfigRole;
  readonly state: AutoconfigState;
  readonly phase: AutoconfigPhase;
  readonly preparedAt: string | null;
  readonly startedAt: string | null;
  readonly finishedAt: string | null;
  readonly elapsedMillis: number | null;
  readonly budget: AutoconfigBudget;
  readonly evaluations: EvaluationCounts;
  readonly latestEvaluationRevision: number;
  readonly generatedParameterCount: number;
  readonly trainingInstanceCount: number | null;
  readonly metric: CostMetric | null;
  readonly irace: {
    readonly iteration: number | null;
    readonly eliteCount: number;
    readonly updatedAt: string | null;
    readonly finalSnapshot: boolean;
    readonly progress: IraceProgressDetails | null;
  };
  readonly failure: { readonly type: string; readonly message: string | null } | null;
}

export interface EvaluationView {
  readonly id: number;
  readonly configurationId: string;
  readonly instanceId: string;
  readonly instanceName: string;
  readonly seed: number;
  readonly state: EvaluationState;
  readonly cost: number | null;
  readonly timeSeconds: number | null;
  readonly startedAt: string;
  readonly finishedAt: string | null;
  readonly reasonCode: string | null;
  readonly message: string | null;
  readonly slow: boolean;
  readonly slowOverrunMillis: number | null;
}

export interface EvaluationChange {
  readonly revision: number;
  readonly evaluation: EvaluationView;
}

export interface EvaluationChangePage {
  readonly runId: string | null;
  readonly latestRevision: number;
  readonly nextRevision: number;
  readonly changes: readonly EvaluationChange[];
}

export interface EliteView {
  readonly configurationId: string;
  readonly position: number;
  readonly parameters: Readonly<Record<string, string>>;
  readonly algorithm: unknown | null;
}

export interface EliteSnapshot {
  readonly runId: string | null;
  readonly iteration: number | null;
  readonly updatedAt: string | null;
  readonly finalSnapshot: boolean;
  readonly elites: readonly EliteView[];
}

export interface EliteIterationSnapshot {
  readonly iteration: number;
  readonly updatedAt: string;
  readonly progress: IraceProgressDetails;
  readonly elites: readonly EliteView[];
}

export interface EliteHistorySnapshot {
  readonly runId: string | null;
  readonly iterations: readonly EliteIterationSnapshot[];
}

export interface CandidateView {
  readonly configurationId: string;
  readonly parameters: Readonly<Record<string, string>>;
  readonly algorithm: unknown | null;
  readonly decodeError: string | null;
  readonly evaluations: EvaluationCounts;
}

export type SearchParameterKind =
  'INTEGER' | 'REAL' | 'CATEGORICAL' | 'ORDINAL' | 'PROVIDED' | 'COMPONENT' | 'COMBINATION';

export interface SearchParameter {
  readonly name: string;
  readonly kind: SearchParameterKind;
  readonly values: readonly unknown[];
  readonly minimum: unknown | null;
  readonly maximum: unknown | null;
  readonly minItems: number | null;
  readonly maxItems: number | null;
  readonly choices: readonly string[];
}

export interface SearchSpaceSnapshot {
  readonly limits: {
    readonly treeDepth: number;
    readonly maxDerivationRepetition: number;
  };
  readonly summary: {
    readonly rootCount: number;
    readonly componentCount: number;
    readonly parameterCount: number;
    readonly combinationParameterCount: number;
    readonly generatedIraceParameterCount: number;
    readonly generatedForbiddenConstraintCount: number;
  };
  readonly roots: readonly string[];
  readonly components: readonly {
    readonly name: string;
    readonly parameters: readonly SearchParameter[];
  }[];
  readonly encodingDiagnostics: unknown;
}

export interface ArtifactView {
  readonly id: 'final-elites' | 'plots' | 'irace-data' | 'stdout' | 'stderr';
  readonly filename: string;
  readonly mediaType: string;
  readonly size: number;
  readonly lastModified: string;
}

export interface ArtifactManifest {
  readonly runId: string;
  readonly artifacts: readonly ArtifactView[];
}

export interface InstanceEvaluationSummary {
  readonly instanceName: string;
  readonly evaluations: number;
  readonly bestCost: number | null;
  readonly issues: number;
  readonly slow: number;
}

export interface TuningAlert {
  readonly key: string;
  readonly revision: number | null;
  readonly severity: 'critical' | 'warning';
  readonly title: string;
  readonly message: string;
  readonly timestamp: string | null;
  readonly evaluationId: number | null;
}
