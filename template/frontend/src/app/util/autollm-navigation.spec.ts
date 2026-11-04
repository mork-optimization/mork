import { autoLlmLocation, matchesTrialFilter, readAutoLlmLocation } from './autollm-navigation';
import { timedTrial } from '../testing/autollm-timing-fixture';

describe('AutoLLM navigation', () => {
  it('round-trips timeline context, trial filters and a selected attempt in a shareable URL', () => {
    const view = readAutoLlmLocation(
      '#campaign=campaign&tab=1&trial=38&detail=3&stage=TUNING&attempt=6&axis=clock&sort=TUNING&search=worker%26b&page=2&outcome=active&trialFilter=finished&trialSearch=server%20a&trialSort=duration&trialPage=1&target=worker-b',
    );
    expect(readAutoLlmLocation(autoLlmLocation(view))).toEqual(view);
    expect(view.timeline).toEqual({
      mode: 'clock',
      sort: 'TUNING',
      search: 'worker&b',
      page: 2,
      outcome: 'active',
    });
    expect(view).toMatchObject({ trial: 38, stage: 'TUNING', attempt: 6, target: 'worker-b' });
  });
  it('rejects invalid view parameters and preserves existing campaign/trial links', () => {
    const view = readAutoLlmLocation(
      '#campaign=c&trial=2&tab=99&detail=-1&stage=UNKNOWN&attempt=NaN&axis=bad&page=Infinity&sort=bad&outcome=bad&trialPage=1.5',
    );
    expect(view).toMatchObject({
      campaign: 'c',
      trial: 2,
      tab: 0,
      detail: 0,
      stage: null,
      attempt: null,
      trialPage: 0,
    });
    expect(view.timeline).toEqual({
      mode: 'elapsed',
      sort: 'number',
      search: '',
      page: 0,
      outcome: 'all',
    });
  });
  it('keeps automatic waits active and separates blocked trials from active counts', () => {
    for (const status of ['BLOCKED', 'INTERRUPTED', 'FAILED'] as const) {
      expect(matchesTrialFilter(timedTrial([], { status }), 'attention')).toBe(true);
      expect(matchesTrialFilter(timedTrial([], { status }), 'active')).toBe(false);
    }
    expect(matchesTrialFilter(timedTrial([], { status: 'WAITING_FOR_QUOTA' }), 'active')).toBe(
      true,
    );
    expect(matchesTrialFilter(timedTrial([], { status: 'FAILED' }), 'finished')).toBe(true);
    expect(matchesTrialFilter(timedTrial([], { status: 'PENDING' }), 'finished')).toBe(false);
  });
});
