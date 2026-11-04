import type Highcharts from 'highcharts/esm/highcharts';
import {
  timedAttempt as attempt,
  timedTrial as trial,
  timingEpoch as epoch,
} from '../testing/autollm-timing-fixture';
import {
  stageDuration,
  timingDuration,
  trialStages,
  trialTimeline,
  trialTiming,
} from './autollm-timing';

describe('AutoLLM timing evidence', () => {
  it('does not mark a failed repair complete because an earlier generation output exists', () => {
    const value = trial([attempt(1, 'GENERATION', 0, 10), attempt(2, 'REPAIR', 20, 30, 'FAILED')], {
      status: 'FAILED',
      outputs: { GENERATION: { directory: 'attempt-1' } },
    });
    expect(trialStages(value, epoch)[0]).toMatchObject({
      state: 'attention',
      current: false,
      statusLabel: 'failed',
    });
  });
  it('keeps repairs separate and sums all attempts for the stage', () => {
    const value = trial([
      attempt(1, 'GENERATION', 0, 10),
      attempt(2, 'PREPARATION', 10, 30, 'FAILED'),
      attempt(3, 'REPAIR', 40, 50),
      attempt(4, 'PREPARATION', 50, 80),
    ]);
    const timing = trialTiming(value, epoch + 100000);
    expect(timing.elapsed).toBe(80000);
    expect(timing.gaps).toEqual([{ start: epoch + 30000, end: epoch + 40000 }]);
    expect(stageDuration(timing, 'GENERATION')).toBe(20000);
    expect(stageDuration(timing, 'PREPARATION')).toBe(50000);
    expect(trialStages(value, epoch)[0].attempts.map((a) => a.number)).toEqual([1, 3]);
  });

  it('uses the last update for running attempts and preserves resumed wall-clock time', () => {
    const value = trial([attempt(1, 'TUNING', 0, null)], { status: 'WAITING_FOR_REMOTE' });
    const timing = trialTiming(value, epoch + 3600000);
    expect(timing.intervals[0]).toMatchObject({
      start: epoch,
      end: epoch + 3600000,
      running: true,
    });
    expect(stageDuration(timing, 'TUNING')).toBe(3600000);
    expect(timing.gaps).toEqual([]);
    expect(trialStages(value, epoch + 3600000)[3]).toMatchObject({
      current: true,
      state: 'waiting',
      statusLabel: 'waiting for remote',
    });
  });

  it('shows a wait after a closed attempt as a gap and keeps completed stages complete', () => {
    const value = trial([attempt(1, 'LOCAL_PREPARATION', 0, 10)], {
      status: 'WAITING_FOR_TARGET',
      nextStage: 'PREPARATION',
    });
    const timing = trialTiming(value, epoch + 30000);
    expect(timing.elapsed).toBe(30000);
    expect(timing.gaps).toEqual([{ start: epoch + 10000, end: epoch + 30000 }]);
    const stages = trialStages(value, epoch + 30000);
    expect(stages[1]).toMatchObject({ current: false, state: 'complete' });
    expect(stages[2]).toMatchObject({ current: true, statusLabel: 'waiting for target' });
  });

  it('does not grow interrupted trials or invent durations for incomplete or malformed evidence', () => {
    const value = trial(
      [
        attempt(1, 'GENERATION', 0, 10),
        attempt(2, 'TUNING', 10, null),
        { ...attempt(3, 'PREPARATION', 20, 30), startedAt: 'invalid' },
        attempt(4, 'COMPARISON', 40, 30),
      ],
      { status: 'INTERRUPTED' },
    );
    const timing = trialTiming(value, epoch + 3600000);
    expect(timing.unknown).toBe(3);
    expect(timing.elapsed).toBe(10000);
    expect(timing.gaps).toEqual([]);
    expect(stageDuration(timing, 'TUNING')).toBeNull();
    expect(stageDuration(timing, 'WINNER_EXPORT')).toBeNull();
    expect(trialStages(value, epoch).some((stage) => stage.current)).toBe(false);
    expect(trialTiming(trial([]), epoch).elapsed).toBeNull();
    expect(timingDuration(null)).toBe('Unavailable');
    expect(timingDuration(0)).toBe('0s');
  });

  it('does not show a partial stage total as complete timing or count overlapping intervals as gaps', () => {
    const timing = trialTiming(
      trial([
        attempt(1, 'GENERATION', 0, 50),
        attempt(2, 'PREPARATION', 10, 20),
        attempt(3, 'REPAIR', 40, 60),
        attempt(4, 'REPAIR', 60, null, 'FAILED'),
      ]),
      epoch,
    );
    expect(timing.gaps).toEqual([]);
    expect(timing.elapsed).toBe(60000);
    expect(stageDuration(timing, 'GENERATION')).toBeNull();
  });

  it('invalidates older stage completion after repair while preserving its timing', () => {
    const value = trial([attempt(1, 'PREPARATION', 0, 20), attempt(2, 'REPAIR', 20, 30)], {
      status: 'PREPARATION',
    });
    const preparation = trialStages(value, epoch + 30000)[2];
    expect(preparation).toMatchObject({ state: 'current', current: true, duration: 20000 });
    expect(trialStages({ ...value, status: 'INTERRUPTED' }, epoch)[2].statusLabel).toBe(
      'Pending after repair',
    );
  });
});

describe('Trial timeline chart', () => {
  const rows = [
    trialTiming(trial([attempt(1, 'GENERATION', 0, 10)]), epoch),
    trialTiming(trial([attempt(2, 'TUNING', 30, 90)], { number: 2 }), epoch),
  ];
  function points(model: ReturnType<typeof trialTimeline>) {
    return (model.options.series![0] as Highcharts.SeriesXrangeOptions)
      .data as Highcharts.XrangePointOptionsObject[];
  }
  it('aligns trial starts in elapsed mode and retains a common scale across pages', () => {
    const model = trialTimeline([rows[1]], rows, 'elapsed', vi.fn());
    expect(points(model)[0]).toMatchObject({ x: 0, x2: 60, y: 0 });
    expect(model.options.xAxis).toMatchObject({ min: 0, max: 60 });
    expect(trialTimeline([rows[0]], rows, 'elapsed', vi.fn()).options.xAxis).toMatchObject({
      max: 60,
    });
  });
  it('preserves concurrency on the campaign clock and links an attempt to its trial and stage', () => {
    const select = vi.fn();
    const model = trialTimeline([rows[1]], rows, 'clock', select);
    const point = points(model)[0];
    expect(model.options.xAxis).toMatchObject({ min: epoch, max: epoch + 90000 });
    expect(point).toMatchObject({ x: epoch + 30000, x2: epoch + 90000 });
    point.events!.click!.call({} as Highcharts.Point, {} as Highcharts.PointClickEventObject);
    expect(select).toHaveBeenCalledWith({ trial: 2, attempt: 2, stage: 'TUNING' });
  });
});
