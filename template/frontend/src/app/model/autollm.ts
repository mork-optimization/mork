export interface AutoLlmConfig {
  project: { repository: string; ref: string; buildCommand: string[]; artifact: string; runArguments: string[] };
  workspace: string; trials: number; seed: number; maxParameters: number;
  sotaFile: string | null; initialConfigurationsFile: string | null;
  trainingPath: string | null; comparisonPath: string | null;
  codex: { executable: string; promptFile: string | null; prompt: string | null; model: string | null; reasoningEffort: string | null };
  preparation: { pilotDurationMillis: number; maxRepairs: number };
  tuning: { evaluations: number; ignoreInitialMillis: number; intervalDurationMillis: number; logScaleArea: boolean };
  comparison: { repetitions: number; timeLimitMillis: number };
  execution: { workers: number; watchdogGraceMillis: number; codexTimeoutMillis: number; buildTimeoutMillis: number; startupTimeoutMillis: number; searchSpaceTimeoutMillis: number; phaseTimeoutMillis: number; stageTimeoutMillis: number; heap: string };
}
export interface AutoLlmAttempt {
  number: number; stage: string; directory: string; status: string;
  startedAt: string; finishedAt: string | null; failure: string | null;
}
export interface AutoLlmTrial {
  number: number; pilotSeed: number; tuningSeed: number; comparisonSeed: number;
  status: string; failure: string | null; threadId: string | null;
  repairTurns: number; winner: unknown | null; attempts: AutoLlmAttempt[];
}
export interface AutoLlmCampaign {
  id: string; config: AutoLlmConfig; createdAt: string; status: string; failure: string | null;
  baselineCommit: string | null; prompt: string; versions: Record<string, string>; trials: AutoLlmTrial[];
  reportFailure?: string | null; reportUpdatedAt?: string | null;
}
export interface AutoLlmArtifact { path: string; size: number; sha256: string }
export interface AutoLlmLog { offset: number; nextOffset: number; lines: string[] }
