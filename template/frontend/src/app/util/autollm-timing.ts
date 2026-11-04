import type Highcharts from 'highcharts/esm/highcharts';
import type {} from 'highcharts/esm/modules/xrange';
import { AutoLlmAttempt, AutoLlmTrial } from '../model/autollm';
import { ChartViewModel } from '../model/dashboard';

export const TRIAL_STAGES = [
  { id: 'GENERATION', label: 'Generation', color: '#1565c0' },
  { id: 'LOCAL_PREPARATION', label: 'Local preparation', color: '#6a1b9a' },
  { id: 'PREPARATION', label: 'Preparation', color: '#00838f' },
  { id: 'TUNING', label: 'Tuning', color: '#e65100' },
  { id: 'WINNER_EXPORT', label: 'Winner export', color: '#546e7a' },
  { id: 'COMPARISON', label: 'Comparison', color: '#2e7d32' },
] as const;
export type TrialStage = (typeof TRIAL_STAGES)[number]['id'];
export type TimelineMode = 'elapsed' | 'clock';
export interface AttemptInterval {
  attempt: AutoLlmAttempt;
  start: number;
  end: number;
  running: boolean;
}
export interface TrialTiming {
  trial: AutoLlmTrial;
  intervals: AttemptInterval[];
  gaps: { start: number; end: number }[];
  start: number | null;
  end: number | null;
  elapsed: number | null;
  unknown: number;
}
export interface AttemptSelection {
  trial: number;
  attempt: number;
  stage: TrialStage;
}

export function stageOf(stage: string): string {
  return stage === 'REPAIR' ? 'GENERATION' : stage;
}
export function timingDuration(ms: number | null): string {
  if (ms === null || !Number.isFinite(ms) || ms < 0) return 'Unavailable';
  if (ms < 1000) return ms === 0 ? '0s' : '<1s';
  const seconds = Math.floor(ms / 1000);
  if (seconds < 60) return `${seconds}s`;
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m ${seconds % 60}s`;
  return `${Math.floor(seconds / 3600)}h ${Math.floor(seconds / 60) % 60}m`;
}
export function trialTiming(trial: AutoLlmTrial, now: number): TrialTiming {
  const intervals: AttemptInterval[] = [];
  let unknown = 0;
  const terminal = ['SUCCEEDED', 'FAILED', 'INTERRUPTED'].includes(trial.status);
  for (const attempt of trial.attempts ?? []) {
    const start = Date.parse(attempt.startedAt);
    const running = !attempt.finishedAt && attempt.status === 'RUNNING' && !terminal;
    const end = attempt.finishedAt ? Date.parse(attempt.finishedAt) : running ? now : NaN;
    if (!Number.isFinite(start) || !Number.isFinite(end) || end < start) {
      unknown++;
      continue;
    }
    intervals.push({ attempt, start, end, running });
  }
  intervals.sort((a, b) => a.start - b.start || a.attempt.number - b.attempt.number);
  const gaps: TrialTiming['gaps'] = [];
  let end = intervals[0]?.start ?? null;
  for (const interval of intervals) {
    if (end !== null && interval.start > end) gaps.push({ start: end, end: interval.start });
    end = Math.max(end ?? interval.end, interval.end);
  }
  if (!terminal && trial.status !== 'PENDING' && end !== null && now > end) {
    gaps.push({ start: end, end: now });
    end = now;
  }
  const start = intervals[0]?.start ?? null;
  return {
    trial,
    intervals,
    gaps,
    start,
    end,
    elapsed: start === null || end === null ? null : end - start,
    unknown,
  };
}
export function stageDuration(timing: TrialTiming, stage: string): number | null {
  const attempts = (timing.trial.attempts ?? []).filter((a) => stageOf(a.stage) === stage);
  const intervals = timing.intervals.filter((a) => stageOf(a.attempt.stage) === stage);
  if (!attempts.length || intervals.length !== attempts.length) return null;
  return intervals.reduce((sum, a) => sum + a.end - a.start, 0);
}
export function trialStages(trial: AutoLlmTrial, now: number) {
  const timing = trialTiming(trial, now);
  const latest = trial.attempts?.at(-1);
  const active = !['PENDING', 'SUCCEEDED', 'FAILED', 'INTERRUPTED'].includes(trial.status);
  const currentStage = TRIAL_STAGES.some((stage) => stage.id === stageOf(trial.status))
    ? stageOf(trial.status)
    : trial.status === 'WAITING_FOR_GENERATION'
      ? 'GENERATION'
      : latest && latest.status !== 'SUCCEEDED'
        ? stageOf(latest.stage)
        : trial.nextStage;
  const generation = [...(trial.attempts ?? [])]
    .reverse()
    .find((a) => stageOf(a.stage) === 'GENERATION');
  return TRIAL_STAGES.map((stage) => {
    const attempts = (trial.attempts ?? []).filter((a) => stageOf(a.stage) === stage.id);
    const last = attempts.at(-1);
    const current = active && currentStage === stage.id;
    const invalidated =
      last && stage.id !== 'GENERATION' && generation && last.number < generation.number;
    const output = trial.outputs?.[stage.id];
    const committed =
      !invalidated &&
      (trial.outputs
        ? !!output &&
          (!last || (last.status === 'SUCCEEDED' && output.directory === last.directory))
        : last?.status === 'SUCCEEDED');
    let state = 'pending',
      label = 'Not started';
    if (committed) {
      state = 'complete';
      label = 'Completed';
    } else if (invalidated) label = 'Pending after repair';
    else if (last) {
      state = last.status === 'SUCCEEDED' ? 'pending' : 'attention';
      label =
        last.status === 'SUCCEEDED'
          ? 'Awaiting checkpoint'
          : last.status.toLowerCase().replaceAll('_', ' ');
    } else if (
      trial.status === 'SUCCEEDED' ||
      (trial.nextStage &&
        [...TRIAL_STAGES.map((s) => s.id), 'COMPLETE'].indexOf(trial.nextStage) >
          TRIAL_STAGES.indexOf(stage))
    )
      label = 'No recorded attempt';
    if (current) {
      state = trial.status.startsWith('WAITING_FOR_')
        ? 'waiting'
        : trial.status === 'BLOCKED'
          ? 'attention'
          : 'current';
      label =
        state === 'attention' || state === 'waiting'
          ? trial.status.toLowerCase().replaceAll('_', ' ')
          : 'In progress';
    }
    return {
      ...stage,
      attempts,
      duration: stageDuration(timing, stage.id),
      current,
      state,
      statusLabel: label,
    };
  });
}

export function trialTimeline(
  rows: TrialTiming[],
  allRows: TrialTiming[],
  mode: TimelineMode,
  select: (value: AttemptSelection) => void,
): ChartViewModel {
  const starts = allRows.flatMap((row) => (row.start === null ? [] : [row.start]));
  const origin = starts.length ? Math.min(...starts) : 0;
  const position = (value: number, row: TrialTiming) =>
    mode === 'clock' ? value : (value - row.start!) / 1000;
  const data: Highcharts.XrangePointOptionsObject[] = [];
  const running: Highcharts.XrangePointOptionsObject[] = [];
  for (const [y, row] of rows.entries()) {
    for (const gap of row.gaps)
      data.push({
        x: position(gap.start, row),
        x2: position(gap.end, row),
        y,
        color: '#e0e0e0',
        name: 'Unclassified gap',
        custom: {
          description: `Unclassified gap · ${timingDuration(gap.end - gap.start)}. No valid attempt timing covers this interval.`,
        },
      });
    for (const interval of row.intervals) {
      const stage = TRIAL_STAGES.find((stage) => stage.id === stageOf(interval.attempt.stage));
      if (!stage) continue;
      const status = interval.running
        ? 'Running at last update'
        : interval.attempt.status.toLowerCase().replaceAll('_', ' ');
      const selection = {
        trial: row.trial.number,
        attempt: interval.attempt.number,
        stage: stage.id,
      };
      (interval.running ? running : data).push({
        x: position(interval.start, row),
        x2: position(interval.end, row),
        y,
        name: `${stage.label} · attempt ${interval.attempt.number}`,
        color: stage.color,
        custom: {
          description: `${stage.label}${interval.attempt.stage === 'REPAIR' ? ' (repair)' : ''} · attempt ${interval.attempt.number} · ${status} · ${timingDuration(interval.end - interval.start)}<br>${new Date(interval.start).toISOString()} → ${new Date(interval.end).toISOString()} (UTC)`,
        },
        events: { click: () => select(selection) },
      });
    }
  }
  const maximum = Math.max(
    mode === 'clock' ? origin + 1000 : 1,
    ...allRows.map((row) => (row.end === null ? 0 : position(row.end, row))),
  );
  return {
    viewKey: JSON.stringify([mode, rows.map((row) => row.trial.number)]),
    options: {
      chart: {
        type: 'xrange',
        animation: false,
        height: Math.max(220, rows.length * 44 + 105),
        backgroundColor: 'transparent',
        zooming: { type: 'x' },
      },
      title: { text: undefined },
      credits: { enabled: false },
      legend: { enabled: false },
      boost: { enabled: false },
      accessibility: {
        description:
          'Trial attempt intervals. A text table with stage durations and inspection buttons follows the chart.',
      },
      xAxis: {
        type: mode === 'clock' ? 'datetime' : 'linear',
        min: mode === 'clock' ? origin : 0,
        max: maximum,
        title: {
          text:
            mode === 'clock'
              ? 'Campaign clock (UTC)'
              : 'Elapsed since first valid attempt timestamp',
        },
        labels: {
          formatter() {
            return mode === 'elapsed'
              ? timingDuration(Number(this.value) * 1000)
              : this.axis.defaultLabelFormatter.call(this);
          },
        },
      },
      time: { timezone: 'UTC' },
      yAxis: {
        categories: rows.map(
          (row) => `Trial ${row.trial.number}${row.start === null ? ' · no timing' : ''}`,
        ),
        reversed: true,
        title: { text: undefined },
        gridLineWidth: 0,
      },
      tooltip: {
        pointFormatter() {
          return String(this.options.custom?.['description'] ?? '');
        },
      },
      plotOptions: {
        series: { animation: false, cursor: 'pointer', states: { inactive: { opacity: 1 } } },
        xrange: {
          grouping: false,
          pointWidth: 22,
          borderRadius: 2,
          colorByPoint: false,
          dataLabels: { enabled: false },
        },
      },
      series: [
        { type: 'xrange', name: 'Recorded attempts and gaps', data, borderWidth: 0 },
        {
          type: 'xrange',
          name: 'Running attempts',
          data: running,
          borderWidth: 2,
          borderColor: '#202124',
        },
      ],
    },
  };
}
