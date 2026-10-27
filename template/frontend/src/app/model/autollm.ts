export type AutoLlmCampaignStatus = 'DRAFT' | 'RUNNING' | 'WAITING_FOR_REMOTE' | 'WAITING_FOR_TARGETS' | 'CANCELLATION_PENDING' | 'INTERRUPTED' | 'NEEDS_ATTENTION' | 'CANCELLED' | 'SUCCEEDED' | 'COMPLETED_WITH_FAILURES';
export type AutoLlmTargetStatus = 'REGISTERED' | 'VALIDATED' | 'QUALIFYING' | 'READY' | 'BUSY' | 'DRAINING' | 'NEEDS_ATTENTION' | 'REMOVED';
export type AutoLlmTrialStatus = 'PENDING' | 'STARTING' | 'WAITING_FOR_GENERATION' | 'GENERATION' | 'REPAIR' | 'METADATA_RECONCILIATION' | 'PREPARATION' | 'TUNING' | 'WINNER_EXPORT' | 'COMPARISON' | 'WAITING_FOR_QUOTA' | 'WAITING_FOR_REMOTE' | 'INTERRUPTED' | 'BLOCKED' | 'SUCCEEDED' | 'FAILED';
export type AutoLlmAttemptStatus = 'RUNNING' | 'SUCCEEDED' | 'FAILED' | 'INTERRUPTED' | 'QUOTA_LIMIT';
export type AutoLlmAttemptStage = 'GENERATION' | 'REPAIR' | 'PREPARATION' | 'TUNING' | 'WINNER_EXPORT' | 'COMPARISON';
export type AutoLlmPreparationStage = 'BUILD' | 'DEPENDENCIES' | 'METADATA' | 'PROBES' | 'PILOT';
export type ScreeningStatus = 'DRAFT' | 'RUNNING' | 'WAITING_FOR_REMOTE' | 'CANCELLATION_PENDING' | 'AWAITING_APPROVAL' | 'APPROVED' | 'NEEDS_ATTENTION' | 'INTERRUPTED' | 'CANCELLED';

export interface AutoLlmServer {
  id: string; host: string; workspace: string; javaExecutable: string; environment: Record<string, string>;
  configFile?: string | null; workers?: number | null; heap?: string | null;
}
export interface AutoLlmTarget {
  definition: AutoLlmServer; status: AutoLlmTargetStatus; directory: string; revision: number; draining: boolean;
  assignedTrial: number | null; baselineReady: boolean; problem?: AutoLlmFailure | null;
  host?: AutoLlmCampaign['executionHost']; remote?: AutoLlmCampaign['remote'];
}
export interface AutoLlmConfig {
  project: { repository: string; ref: string; buildCommand: string[]; artifact: string; runArguments: string[] };
  workspace: string; trials: number; seed: number; maxParameters: number;
  sotaFile: string | null; initialConfigurationsFile: string | null;
  trainingPath: string | null; comparisonPath: string | null;
  codex: { executable: string; promptFile: string | null; prompt: string | null; model: string | null; reasoningEffort: string | null; maxConcurrentSessions: number };
  preparation: { probeDurationMillis: number; pilotDurationMillis: number; maxRepairs: number };
  tuning: { evaluations: number; ignoreInitialMillis: number; intervalDurationMillis: number; logScaleArea: boolean };
  comparison: { repetitions: number; timeLimitMillis: number };
  execution: { workers: number; target?: 'LOCAL' | 'SSH'; ssh?: { screeningServerId: string; runnerArtifact: string | null; servers: AutoLlmServer[] } | null; watchdogGraceMillis: number; codexTimeoutMillis: number; buildTimeoutMillis: number; startupTimeoutMillis: number; searchSpaceTimeoutMillis: number; phaseTimeoutMillis: number; stageTimeoutMillis: number; heap: string };
}
export interface AutoLlmAttempt {
  number: number; stage: AutoLlmAttemptStage; directory: string; status: AutoLlmAttemptStatus;
  startedAt: string; finishedAt: string | null; failure: string | null;
  problem?: AutoLlmFailure | null; substage?: AutoLlmPreparationStage | null;
  codexEvidence?: { status: string; threadId: string | null; failure: string | null; usage: CodexUsage; conversations: { threadId: string; parentThreadId: string | null; path: string; sha256: string; cliVersion: string | null; models: string[] }[] } | null;
}
export interface CodexUsage {
  status: 'NOT_STARTED' | 'COMPLETE' | 'PARTIAL' | 'UNAVAILABLE';
  tokens: { inputTokens: number | null; cachedInputTokens: number | null; cacheWriteInputTokens: number | null; outputTokens: number | null; reasoningOutputTokens: number | null; totalTokens: number | null };
  attempts: number; completeAttempts: number; partialAttempts: number; unavailableAttempts: number;
}
export interface AutoLlmTrial {
  number: number; targetId?: string | null; targetRevision?: number; directory?: string | null; pilotSeed: number; tuningSeed: number; comparisonSeed: number;
  status: AutoLlmTrialStatus; failure: string | null; threadId: string | null;
  repairTurns: number; winner: unknown | null; attempts: AutoLlmAttempt[];
  codexUsage?: CodexUsage | null;
  problem?: AutoLlmFailure | null; infrastructureRetries?: number;
  quotaRetryAt?: string | null;
}
export interface AutoLlmCampaign {
  id: string; config: AutoLlmConfig; createdAt: string; status: AutoLlmCampaignStatus; failure: string | null;
  baselineCommit: string | null; prompt: string; versions: Record<string, string>; trials: AutoLlmTrial[];
  poolRevision: number; targets: Record<string, AutoLlmTarget>;
  reportFailure?: string | null; reportUpdatedAt?: string | null;
  codexUsage?: CodexUsage | null;
  problem?: AutoLlmFailure | null;
  executionHost?: { requestedWorkers: number; effectiveWorkers: number; identity: Record<string, string>; maxMemoryBytes: number } | null;
  remote?: { host: string; jobId: string; status: string; stageDirectory?: string; updatedAt: string; lastSynchronizedAt?: string | null; failure: string | null } | null;
}
export interface AutoLlmArtifact { path: string; size: number; sha256: string }
export interface AutoLlmRecoveryAction { allowed: boolean; reason: string | null; effect: string }
export interface AutoLlmRecovery {
  resumeCampaign: AutoLlmRecoveryAction;
  trials: Record<number, { resume: AutoLlmRecoveryAction; fail: AutoLlmRecoveryAction }>;
  targets: Record<string, { resume: AutoLlmRecoveryAction; requalify: AutoLlmRecoveryAction }>;
}
export interface AutoLlmLog { cursor: number; nextCursor: number; text: string; more: boolean; reset: boolean }
export interface AutoLlmLogPreview { text: string; lineCount: number; sizeBytes: number; truncated: boolean; byteLimited: boolean }
export interface AutoLlmFailure { category: string; code: string; message: string; evidence: string[]; retryable: boolean; recovery: string }
export interface CodexProgress { state: string; threadId: string | null; lastActivityAt: string | null; turns: number; commandsStarted: number; commandsCompleted: number; edits: number; messages: number; usage: unknown }
export interface AutoLlmProgress {
  status: AutoLlmTrialStatus; attempt?: AutoLlmAttempt; substage?: AutoLlmPreparationStage | null; elapsedMillis?: number;
  problem?: AutoLlmFailure | null; repairTurns?: number; infrastructureRetries?: number;
  quotaRetryAt?: string | null;
  generation?: CodexProgress; workload?: Record<string, unknown>; remote?: AutoLlmCampaign['remote'];
}
export interface AutoLlmActivity { timestamp: string; type: string; text: string }
export interface ScreeningJob {
  id: string; createdAt: string; status: ScreeningStatus; mirror: string | null; problem: AutoLlmFailure | null;
  activity: { phase: string; remote?: AutoLlmCampaign['remote'] } | null;
  progress: { phase: string; initialCompleted: number; initialTotal: number; confirmationsCompleted: number; confirmationsTotal: number; updatedAt: string; training?: number; testing?: number; excluded?: number; retained?: number; inconclusive?: number } | null;
}
export interface ScreeningReview {
  jobId: string; proposalSha256: string; report: string; proposalDirectory: string; artifactPrefix: string;
  proposal: { trainingCount: number; testingCount: number; splitFailure: string | null; instances: { instance: string; classification: string; reason: string; fastHits: number; confirmations: number }[] };
}
