export type AutoLlmCampaignStatus =
  | 'DRAFT'
  | 'RUNNING'
  | 'WAITING_FOR_REMOTE'
  | 'WAITING_FOR_TARGETS'
  | 'CANCELLATION_PENDING'
  | 'INTERRUPTED'
  | 'NEEDS_ATTENTION'
  | 'CANCELLED'
  | 'SUCCEEDED'
  | 'COMPLETED_WITH_FAILURES';
export type AutoLlmTargetStatus =
  | 'REGISTERED'
  | 'VALIDATED'
  | 'QUALIFYING'
  | 'READY'
  | 'BUSY'
  | 'DRAINING'
  | 'NEEDS_ATTENTION'
  | 'REMOVED';
export type AutoLlmTrialStatus =
  | 'PENDING'
  | 'WAITING_FOR_TARGET'
  | 'WAITING_FOR_WORKER'
  | 'LOCAL_PREPARATION'
  | 'STARTING'
  | 'WAITING_FOR_GENERATION'
  | 'GENERATION'
  | 'REPAIR'
  | 'METADATA_RECONCILIATION'
  | 'PREPARATION'
  | 'TUNING'
  | 'WINNER_EXPORT'
  | 'COMPARISON'
  | 'WAITING_FOR_QUOTA'
  | 'WAITING_FOR_CAPACITY'
  | 'WAITING_FOR_REMOTE'
  | 'INTERRUPTED'
  | 'BLOCKED'
  | 'SUCCEEDED'
  | 'FAILED';
export type AutoLlmAttemptStatus =
  'RUNNING' | 'SUCCEEDED' | 'FAILED' | 'INTERRUPTED' | 'QUOTA_LIMIT' | 'CAPACITY_LIMIT';
export type AutoLlmAttemptStage =
  | 'GENERATION'
  | 'REPAIR'
  | 'LOCAL_PREPARATION'
  | 'PREPARATION'
  | 'TUNING'
  | 'WINNER_EXPORT'
  | 'COMPARISON';
export type AutoLlmPreparationStage = 'BUILD' | 'DEPENDENCIES' | 'METADATA' | 'PROBES' | 'PILOT';
export type ScreeningStatus =
  | 'DRAFT'
  | 'RUNNING'
  | 'WAITING_FOR_REMOTE'
  | 'CANCELLATION_PENDING'
  | 'READY_FOR_REVIEW'
  | 'NEEDS_ATTENTION'
  | 'INTERRUPTED'
  | 'CANCELLED';

export interface AutoLlmServer {
  id: string;
  host: string;
  workspace: string;
  javaExecutable: string;
  environment: Record<string, string>;
  configFile?: string | null;
  workers?: number | null;
  heap?: string | null;
}
export type ObjectiveModes = Record<string, 'MINIMIZE' | 'MAXIMIZE'>;
export interface DatasetSelection {
  training: string[];
  testing: string[];
  excluded: Record<string, string>;
}
export interface AutoLlmTarget {
  definition: AutoLlmServer;
  status: AutoLlmTargetStatus;
  directory: string;
  revision: number;
  draining: boolean;
  qualification?: 'QUALIFIED' | 'QUALIFYING' | 'UNQUALIFIED';
  availability?: 'RESERVED' | 'UNKNOWN';
  health?: 'HEALTHY' | 'UNHEALTHY' | 'UNKNOWN';
  assignedTrial: number | null;
  baselineReady: boolean;
  problem?: AutoLlmFailure | null;
  host?: AutoLlmCampaign['executionHost'];
  remote?: AutoLlmCampaign['remote'];
}
export interface AutoLlmConfig {
  project: {
    repository: string;
    ref: string;
    buildCommand: string[];
    artifact: string;
    runArguments: string[];
  };
  workspace: string;
  trials: number;
  seed: number;
  maxParameters: number;
  sotaFile: string | null;
  initialConfigurationsFile: string | null;
  trainingPath: string | null;
  comparisonPath: string | null;
  codex: {
    executable: string;
    promptFile: string | null;
    prompt: string | null;
    model: string | null;
    reasoningEffort: string | null;
    maxConcurrentSessions: number;
  };
  preparation: {
    probeDurationMillis: number;
    pilotDurationMillis: number;
    maxRepairs: number;
    localBuilds?: number;
    readyQueueSize?: number;
    localWorkers?: number;
    localHeap?: string;
  };
  tuning: {
    evaluations: number;
    ignoreInitialMillis: number;
    intervalDurationMillis: number;
    logScaleArea: boolean;
  };
  comparison: { repetitions: number; timeLimitMillis: number };
  execution: {
    workers: number;
    target?: 'LOCAL' | 'SSH';
    ssh?: {
      screeningServerId: string;
      runnerArtifact: string | null;
      servers: AutoLlmServer[];
      reconnect?: { initialDelayMillis: number; maximumDelayMillis: number; budgetMillis: number };
    } | null;
    watchdogGraceMillis: number;
    codexTimeoutMillis: number;
    buildTimeoutMillis: number;
    startupTimeoutMillis: number;
    searchSpaceTimeoutMillis: number;
    phaseTimeoutMillis: number;
    stageTimeoutMillis: number;
    heap: string;
  };
}
export interface AutoLlmAttempt {
  number: number;
  stage: AutoLlmAttemptStage;
  directory: string;
  status: AutoLlmAttemptStatus;
  startedAt: string;
  finishedAt: string | null;
  failure: string | null;
  problem?: AutoLlmFailure | null;
  substage?: AutoLlmPreparationStage | null;
  codexEvidence?: {
    status: string;
    threadId: string | null;
    failure: string | null;
    usage: CodexUsage;
    conversations: {
      threadId: string;
      parentThreadId: string | null;
      path: string;
      sha256: string;
      cliVersion: string | null;
      models: string[];
    }[];
  } | null;
}
export interface CodexUsage {
  status: 'NOT_STARTED' | 'COMPLETE' | 'PARTIAL' | 'UNAVAILABLE';
  tokens: {
    inputTokens: number | null;
    cachedInputTokens: number | null;
    cacheWriteInputTokens: number | null;
    outputTokens: number | null;
    reasoningOutputTokens: number | null;
    totalTokens: number | null;
  };
  attempts: number;
  completeAttempts: number;
  partialAttempts: number;
  unavailableAttempts: number;
}
export interface AutoLlmTrial {
  number: number;
  targetId?: string | null;
  targetRevision?: number;
  directory?: string | null;
  pilotSeed: number;
  tuningSeed: number;
  comparisonSeed: number;
  status: AutoLlmTrialStatus;
  failure: string | null;
  threadId: string | null;
  repairTurns: number;
  winner: unknown | null;
  attempts: AutoLlmAttempt[];
  codexUsage?: CodexUsage | null;
  problem?: AutoLlmFailure | null;
  infrastructureRetries?: number;
  quotaRetryAt?: string | null;
  capacityWait?: {
    firstFailureAt: string;
    deadline: string;
    nextRetryAt: string;
    retries: number;
    diagnostic: string;
  } | null;
}
export interface AutoLlmCampaign {
  approvedTargets?: string[];
  id: string;
  config: AutoLlmConfig;
  createdAt: string;
  status: AutoLlmCampaignStatus;
  failure: string | null;
  baselineCommit: string | null;
  prompt: string;
  versions: Record<string, string>;
  trials: AutoLlmTrial[];
  poolRevision: number;
  targets: Record<string, AutoLlmTarget>;
  reportFailure?: string | null;
  reportUpdatedAt?: string | null;
  codexUsage?: CodexUsage | null;
  problem?: AutoLlmFailure | null;
  executionHost?: {
    requestedWorkers: number;
    effectiveWorkers: number;
    identity: Record<string, string>;
    maxMemoryBytes: number;
  } | null;
  remote?: {
    host: string;
    jobId: string;
    status: string;
    stageDirectory?: string;
    updatedAt: string;
    lastSynchronizedAt?: string | null;
    lastContactAt?: string | null;
    workerId?: string;
    assignmentId?: string | null;
    lease?: WorkerLease;
    failure: string | null;
  } | null;
}
export interface AutoLlmArtifact {
  path: string;
  size: number;
  sha256: string | null;
}
export interface AutoLlmRecoveryAction {
  allowed: boolean;
  reason: string | null;
  effect: string;
}
export interface AutoLlmRecovery {
  resumeCampaign: AutoLlmRecoveryAction;
  trials: Record<number, { resume: AutoLlmRecoveryAction; fail: AutoLlmRecoveryAction }>;
  targets: Record<string, { resume: AutoLlmRecoveryAction; requalify: AutoLlmRecoveryAction }>;
}
export interface AutoLlmLog {
  cursor: number;
  nextCursor: number;
  text: string;
  more: boolean;
  reset: boolean;
}
export interface AutoLlmLogPreview {
  text: string;
  lineCount: number;
  sizeBytes: number;
  truncated: boolean;
  byteLimited: boolean;
}
export interface AutoLlmFailure {
  category: string;
  code: string;
  message: string;
  evidence: string[];
  retryable: boolean;
  recovery: string;
}
export interface CodexProgress {
  state: string;
  threadId: string | null;
  lastActivityAt: string | null;
  turns: number;
  commandsStarted: number;
  commandsCompleted: number;
  edits: number;
  messages: number;
  usage: unknown;
}
export interface AutoLlmProgress {
  modelSelection?: {
    requestedModel: string | null;
    resolvedModel: string | null;
    resolvedProvider: string | null;
    observation: string;
  };
  status: AutoLlmTrialStatus;
  attempt?: AutoLlmAttempt;
  substage?: AutoLlmPreparationStage | null;
  elapsedMillis?: number;
  problem?: AutoLlmFailure | null;
  repairTurns?: number;
  infrastructureRetries?: number;
  quotaRetryAt?: string | null;
  capacityWait?: {
    firstFailureAt: string;
    deadline: string;
    nextRetryAt: string;
    retries: number;
    diagnostic: string;
  } | null;
  generation?: CodexProgress;
  workload?: Record<string, unknown>;
  remote?: AutoLlmCampaign['remote'];
}
export interface AutoLlmActivity {
  timestamp: string;
  type: string;
  text: string;
}
export interface ScreeningJob {
  progressObservation?: {
    sequence: number;
    lastMeaningfulChange: string | null;
    counters: Record<string, unknown>;
  };
  id: string;
  createdAt: string;
  status: ScreeningStatus;
  mirror: string | null;
  problem: AutoLlmFailure | null;
  activity: { phase: string; remote?: AutoLlmCampaign['remote'] } | null;
  progress: {
    phase: string;
    initialCompleted: number;
    initialTotal: number;
    confirmationsCompleted: number;
    confirmationsTotal: number;
    updatedAt: string;
    training?: number;
    testing?: number;
    excluded?: number;
    retained?: number;
    inconclusive?: number;
  } | null;
}
export interface ScreeningReview {
  jobId: string;
  proposalSha256: string;
  report: string;
  proposalDirectory: string;
  artifactPrefix: string;
  proposal: {
    trainingCount: number;
    testingCount: number;
    splitFailure: string | null;
    instances: {
      instance: string;
      classification: string;
      reason: string;
      fastHits: number;
      confirmations: number;
    }[];
  };
}

export interface ComparisonInstance {
  instanceId: string;
  instance: string;
  counts: Record<string, Record<string, number>>;
}
export interface AutoLlmComparison {
  instances?: ComparisonInstance[];
  [key: string]: unknown;
}

export interface WorkerLease {
  workerId: string;
  controller: string;
  operation: string;
  assignment: string | null;
  token: string;
  generation: number;
}
export interface EffectiveConfiguration {
  configuration: AutoLlmConfig;
  errors: string[];
  summary: Record<string, unknown>;
  digest: string;
}
export interface ApprovalDifference {
  path: string;
  reviewed: unknown;
  current: unknown;
}
export interface GenerationSettings {
  model: string | null;
  reasoningEffort: string | null;
  ready: boolean;
  approvalLine: string;
}
export interface CleanupSelection {
  kind: 'campaign' | 'screening' | 'probe' | 'workspace';
  id?: string;
  export?: string;
  shutdown?: boolean;
  retainReceipt?: boolean;
}
export interface CleanupPlan {
  digest: string;
  workspace: string;
  selection: CleanupSelection;
  delete: unknown[];
  retain: Record<string, string>;
  remote: unknown[];
  blockers: string[];
  processes: { evidence: string; identity: { pid: number; start: string } }[];
}
export interface CleanupReceipt {
  digest: string;
  status: 'UNKNOWN' | 'PLANNED' | 'PENDING' | 'SUCCEEDED';
  completed: string[];
  pending: Record<string, string>;
}

export interface CampaignReceipt {
  id: string;
  status: AutoLlmCampaignStatus;
  operation: string;
  trial?: number | null;
  accepted: boolean;
}
export interface ComparisonStatistics {
  count: number;
  mean: number | null;
  median: number | null;
  best: number | null;
  standardDeviation: number | null;
}
export interface ComparisonMeasurement {
  objective: ComparisonStatistics;
  executionTimeMillis: ComparisonStatistics;
  timeToOwnBestMillis: ComparisonStatistics;
}
export interface CampaignComparisonRow {
  instanceId: string;
  instance: string;
  sota: ComparisonMeasurement;
  proposal: ComparisonMeasurement;
  difference: number;
  improvementPercent: number | null;
  outcome: string;
}
export interface CampaignComparisonRun {
  trial: number;
  targetId: string | null;
  repetitions: number;
  journal: string;
  instances: CampaignComparisonRow[];
  wins: number;
  ties: number;
  losses: number;
  undefinedPercentages: number;
  meanImprovementPercent: number | null;
}
export interface CampaignComparison {
  campaignId: string;
  objective: string;
  mode: 'MINIMIZE' | 'MAXIMIZE';
  checkedAt: string;
  reference: string;
  trials: CampaignComparisonRun[];
  excluded: Record<string, string>;
  ranking: { trial: number; meanImprovementPercent: number }[];
  rankingMethod: string;
}

export interface ProposalTarget {
  definition?: AutoLlmServer;
  selected: boolean;
  ready: boolean;
  draining?: boolean;
}
export interface ProposalScreening {
  originalPool?: string[];
  excluded?: Record<string, string>;
  decisions?: { instance: string; classification: string; reason: string }[];
  screening?: ProposalScreening;
  selection?: { excluded?: Record<string, string> };
}
