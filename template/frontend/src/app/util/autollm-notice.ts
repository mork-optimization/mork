import { AutoLlmTrial } from '../model/autollm';
import { timingDuration } from './autollm-timing';

export interface TrialNotice {
  kind: 'waiting' | 'attention';
  title: string;
  message: string;
  retryAt: string | null;
}
export function trialNotice(
  trial: AutoLlmTrial,
  active: boolean,
  now: number,
  stale: boolean,
): TrialNotice | null {
  if (['FAILED', 'BLOCKED', 'INTERRUPTED'].includes(trial.status)) {
    return {
      kind: 'attention',
      title: trial.status === 'FAILED' ? 'Trial failed' : 'Action required',
      message:
        trial.problem?.message ||
        (trial.status === 'INTERRUPTED'
          ? 'This trial was interrupted. Review recovery options to continue.'
          : 'Review the saved evidence and recovery options.'),
      retryAt: null,
    };
  }
  if (!trial.status.startsWith('WAITING_FOR_')) return null;
  const labels: Record<string, string> = {
    WAITING_FOR_QUOTA: 'Waiting for Codex quota',
    WAITING_FOR_CAPACITY: 'Waiting for model capacity',
    WAITING_FOR_REMOTE: 'Waiting for remote connection',
    WAITING_FOR_TARGET: 'Waiting for an execution server',
    WAITING_FOR_WORKSPACE: 'Waiting for workspace access',
    WAITING_FOR_GENERATION: 'Waiting for a generation slot',
  };
  const title = labels[trial.status] ?? 'Waiting';
  const timestamp =
    trial.status === 'WAITING_FOR_QUOTA'
      ? trial.quotaRetryAt
      : trial.status === 'WAITING_FOR_CAPACITY'
        ? trial.capacityWait?.nextRetryAt
        : null;
  const retryAt = timestamp && Number.isFinite(Date.parse(timestamp)) ? timestamp : null;
  if (stale)
    return {
      kind: 'waiting',
      title: 'Last reported: ' + title.toLowerCase(),
      message:
        'Status updates are unavailable. Retry timing will refresh when the connection recovers.',
      retryAt,
    };
  if (!active)
    return {
      kind: 'waiting',
      title,
      message:
        'The campaign is not running. Automatic retries can continue after the campaign resumes.',
      retryAt,
    };
  if (retryAt) {
    const remaining = Date.parse(retryAt) - now;
    return {
      kind: 'waiting',
      title:
        remaining > 0
          ? `Automatic retry in ${timingDuration(remaining)}`
          : 'Waiting for retry update',
      message:
        remaining > 0
          ? `${title}. No action needed; the controller will retry automatically.`
          : 'The scheduled retry time has passed. Waiting for the controller to report its next state.',
      retryAt,
    };
  }
  return {
    kind: 'waiting',
    title,
    message:
      'The controller is waiting for this resource. Details will update as it becomes available.',
    retryAt: null,
  };
}
