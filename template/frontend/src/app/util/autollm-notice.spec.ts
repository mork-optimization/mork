import { timedTrial, timingEpoch } from '../testing/autollm-timing-fixture';
import { trialNotice } from './autollm-notice';

describe('AutoLLM waiting and intervention messages', () => {
  const trial = timedTrial([], {
    status: 'WAITING_FOR_QUOTA',
    quotaRetryAt: new Date(timingEpoch + 180000).toISOString(),
  });
  it('shows a timed automatic retry only while the campaign is running with fresh status', () => {
    expect(trialNotice(trial, true, timingEpoch, false)).toMatchObject({
      kind: 'waiting',
      title: 'Automatic retry in 3m 0s',
    });
    expect(trialNotice(trial, false, timingEpoch, false)?.message).toContain(
      'campaign is not running',
    );
    expect(trialNotice(trial, true, timingEpoch, true)?.title).toBe(
      'Last reported: waiting for codex quota',
    );
  });
  it('does not invent retry times or claim an expired retry has happened', () => {
    expect(trialNotice(trial, true, timingEpoch + 200000, false)?.title).toBe(
      'Waiting for retry update',
    );
    expect(
      trialNotice({ ...trial, quotaRetryAt: 'invalid' }, true, timingEpoch, false)?.retryAt,
    ).toBeNull();
    expect(
      trialNotice({ ...trial, status: 'WAITING_FOR_GENERATION' }, true, timingEpoch, false),
    ).toMatchObject({ title: 'Waiting for a generation slot', retryAt: null });
  });
  it('shows capacity retries and preserves the actual problem for blocked and failed trials', () => {
    const capacity = {
      ...trial,
      status: 'WAITING_FOR_CAPACITY' as const,
      capacityWait: {
        firstFailureAt: new Date(timingEpoch).toISOString(),
        nextRetryAt: trial.quotaRetryAt!,
        deadline: new Date(timingEpoch + 3600000).toISOString(),
        retries: 1,
        diagnostic: 'Busy',
      },
    };
    expect(trialNotice(capacity, true, timingEpoch, false)?.title).toBe('Automatic retry in 3m 0s');
    const problem = {
      category: 'INFRASTRUCTURE',
      code: 'SSH',
      message: 'SSH authentication failed',
      recovery: 'Check credentials',
      evidence: [],
      retryable: false,
    };
    expect(
      trialNotice({ ...trial, status: 'BLOCKED', problem }, true, timingEpoch, false),
    ).toMatchObject({ kind: 'attention', title: 'Action required', message: problem.message });
    expect(
      trialNotice({ ...trial, status: 'FAILED', problem }, false, timingEpoch, false)?.title,
    ).toBe('Trial failed');
    expect(trialNotice({ ...trial, status: 'TUNING' }, true, timingEpoch, false)).toBeNull();
  });
});
