import { AutoLlmAttempt, AutoLlmTrial } from '../model/autollm';

export const timingEpoch = Date.parse('2026-11-03T10:00:00Z');
export function timedAttempt(
  number: number,
  stage: AutoLlmAttempt['stage'],
  start: number,
  end: number | null,
  status: AutoLlmAttempt['status'] = end === null ? 'RUNNING' : 'SUCCEEDED',
): AutoLlmAttempt {
  return {
    number,
    stage,
    status,
    startedAt: new Date(timingEpoch + start * 1000).toISOString(),
    finishedAt: end === null ? null : new Date(timingEpoch + end * 1000).toISOString(),
    directory: `attempt-${number}`,
    failure: null,
  };
}
export function timedTrial(
  attempts: AutoLlmAttempt[],
  overrides: Partial<AutoLlmTrial> = {},
): AutoLlmTrial {
  return {
    number: 1,
    status: 'SUCCEEDED',
    pilotSeed: 1,
    tuningSeed: 2,
    comparisonSeed: 3,
    failure: null,
    threadId: null,
    repairTurns: 0,
    winner: null,
    attempts,
    ...overrides,
  };
}
