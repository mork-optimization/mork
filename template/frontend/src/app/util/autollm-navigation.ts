import { AutoLlmTrial } from '../model/autollm';
import { TimelineMode, TRIAL_STAGES, TrialStage } from './autollm-timing';

export type TrialFilter =
  'all' | 'finished' | 'active' | 'attention' | 'SUCCEEDED' | 'FAILED' | 'PENDING';
export interface TimelineSettings {
  mode: TimelineMode;
  sort: string;
  search: string;
  page: number;
  outcome: TrialFilter;
}
export function defaultTimelineSettings(): TimelineSettings {
  return { mode: 'elapsed', sort: 'number', search: '', page: 0, outcome: 'all' };
}
export function matchesTrialFilter(trial: AutoLlmTrial, filter: string): boolean {
  if (filter === 'active')
    return !['PENDING', 'SUCCEEDED', 'FAILED', 'BLOCKED', 'INTERRUPTED'].includes(trial.status);
  if (filter === 'attention') return ['BLOCKED', 'INTERRUPTED', 'FAILED'].includes(trial.status);
  if (filter === 'finished') return ['SUCCEEDED', 'FAILED'].includes(trial.status);
  return filter === 'all' || trial.status === filter;
}
export interface AutoLlmNavigation {
  campaign: string;
  tab: number;
  trial: number | null;
  detail: number;
  stage: TrialStage | null;
  attempt: number | null;
  target: string;
  timeline: TimelineSettings;
  trialSearch: string;
  trialFilter: string;
  trialSort: string;
  trialPage: number;
}
const filters: TrialFilter[] = [
  'all',
  'finished',
  'active',
  'attention',
  'SUCCEEDED',
  'FAILED',
  'PENDING',
];
function choice<T extends string>(value: string | null, choices: readonly T[], fallback: T): T {
  return choices.includes(value as T) ? (value as T) : fallback;
}
function integer(
  value: string | null,
  fallback: number,
  maximum = Number.MAX_SAFE_INTEGER,
): number {
  if (!value?.trim()) return fallback;
  const parsed = Number(value);
  return Number.isSafeInteger(parsed) && parsed >= 0 && parsed <= maximum ? parsed : fallback;
}
export function readAutoLlmLocation(hash: string): AutoLlmNavigation {
  const params = new URLSearchParams(hash.replace(/^#/, ''));
  const stages = TRIAL_STAGES.map((stage) => stage.id);
  return {
    campaign: params.get('campaign') ?? '',
    tab: integer(params.get('tab'), 0, 4),
    trial: integer(params.get('trial'), 0) || null,
    detail: integer(params.get('detail'), 0, 4),
    stage: stages.includes(params.get('stage') as TrialStage)
      ? (params.get('stage') as TrialStage)
      : null,
    attempt: integer(params.get('attempt'), 0) || null,
    target: params.get('target') ?? '',
    timeline: {
      mode: choice(params.get('axis'), ['elapsed', 'clock'], 'elapsed'),
      sort: choice(params.get('sort'), ['number', 'total', ...stages], 'number'),
      search: params.get('search') ?? '',
      page: integer(params.get('page'), 0),
      outcome: choice(params.get('outcome'), filters, 'all'),
    },
    trialSearch: params.get('trialSearch') ?? '',
    trialFilter: choice(params.get('trialFilter'), filters, 'all'),
    trialSort: choice(params.get('trialSort'), ['number', 'status', 'duration'], 'number'),
    trialPage: integer(params.get('trialPage'), 0),
  };
}
export function autoLlmLocation(view: AutoLlmNavigation): string {
  const params = new URLSearchParams();
  if (view.campaign) params.set('campaign', view.campaign);
  params.set('tab', String(view.tab));
  if (view.trial !== null) {
    params.set('trial', String(view.trial));
    params.set('detail', String(view.detail));
    if (view.stage) params.set('stage', view.stage);
    if (view.attempt !== null) params.set('attempt', String(view.attempt));
  }
  if (view.target) params.set('target', view.target);
  const fields = [
    ['axis', view.timeline.mode, 'elapsed'],
    ['sort', view.timeline.sort, 'number'],
    ['search', view.timeline.search, ''],
    ['page', String(view.timeline.page), '0'],
    ['outcome', view.timeline.outcome, 'all'],
    ['trialSearch', view.trialSearch, ''],
    ['trialFilter', view.trialFilter, 'all'],
    ['trialSort', view.trialSort, 'number'],
    ['trialPage', String(view.trialPage), '0'],
  ];
  for (const [key, value, fallback] of fields) if (value !== fallback) params.set(key, value);
  return '#' + params.toString();
}
