import { Component, input, provideZonelessChangeDetection, signal, WritableSignal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';
import { ChartViewModel } from '../../model/dashboard';
import { AutoLlmStore } from '../../store/autollm-store';
import { AutoLlmDashboard } from './autollm-dashboard';
import { AutoLlmActivity, AutoLlmCampaign, AutoLlmConfig, AutoLlmTrial, CodexProgress, ScreeningJob, ScreeningReview } from '../../model/autollm';

@Component({ selector: 'app-dashboard-chart', template: '' })
class ChartStub { readonly model = input.required<ChartViewModel>(); readonly resizable = input(false); }

describe('AutoLLM dashboard', () => {
  beforeEach(async () => {
    const trial = { number: 1, status: 'SUCCEEDED', tuningSeed: 1234, repairTurns: 1, attempts: [], winner: { $component: 'SimpleAlgorithm', constructive: { $component: 'ConstructiveNew' } } };
    const campaign = { id: 'campaign', status: 'SUCCEEDED', createdAt: '2026-10-07T00:00:00Z', trials: [trial], config: { execution: { target: 'LOCAL' } }, versions: {}, poolRevision: 1, targets: {} };
    const store = { targets: signal([]), activeTrials: signal([]), activeProgress: signal({}), screenings: signal([]), activeScreening: signal(''), selectedScreening: signal(''), screening: signal(null), review: signal(null), summary: signal(null), progress: signal(null), generation: signal(null), conversationAttempt: signal(null), activity: signal([]), config: signal(null), targetStatus: signal(null), recovery: signal(null), recoveryError: signal(null), trialRecovery: signal(null), error: signal(null), connectionError: signal(null), active: signal(''), busy: signal(false), selectedId: signal(campaign.id), selectedTrial: signal(1), campaigns: signal([campaign]), campaign: signal(campaign), trial: signal(trial), completed: signal(1), tuning: signal(null), logPath: signal(''), logLines: signal(500), logFollow: signal(true), logPreview: signal(null), logLoading: signal(false), logError: signal(null), artifacts: signal([]), comparison: signal({ counts: { SOTA: { SUCCEEDED: 1 } }, curves: [{ instance: 'instance.txt', instanceId: 'instance', algorithm: 'SOTA', iteration: '0', curves: { Cost: [{ instant: 0, value: 10 }, { instant: 10000000, value: 8 }] } }] }), start: vi.fn().mockResolvedValue(undefined) };
    await TestBed.configureTestingModule({ imports: [AutoLlmDashboard], providers: [provideZonelessChangeDetection(), { provide: AutoLlmStore, useValue: store }] })
      .overrideComponent(AutoLlmDashboard, { remove: { imports: [DashboardChart] }, add: { imports: [ChartStub] } }).compileComponents();
  });
  afterEach(() => TestBed.resetTestingModule());
  function configuration(): AutoLlmConfig {
    return { project: { repository: '/repo', ref: 'HEAD', buildCommand: ['mvn', 'verify'], artifact: 'target/project.jar', runArguments: [] },
      workspace: '/work', trials: 1, seed: 1234, maxParameters: 299, sotaFile: '/sota.json', initialConfigurationsFile: null, trainingPath: '/train.index', comparisonPath: '/test.index',
      codex: { executable: 'codex', promptFile: null, prompt: null, model: null, reasoningEffort: null, maxConcurrentSessions: 1 }, preparation: { pilotDurationMillis: 1000, maxRepairs: 1 },
      tuning: { evaluations: 100, ignoreInitialMillis: 1000, intervalDurationMillis: 9000, logScaleArea: true }, comparison: { repetitions: 30, timeLimitMillis: 10000 },
      execution: { target: 'LOCAL', ssh: null, workers: -1, watchdogGraceMillis: 1000, codexTimeoutMillis: 10000, buildTimeoutMillis: 10000, startupTimeoutMillis: 10000, searchSpaceTimeoutMillis: 10000, phaseTimeoutMillis: 10000, stageTimeoutMillis: 10000, heap: '2g' } };
  }
  it('requires a location choice before creating a campaign', async () => {
    TestBed.inject(AutoLlmStore).config.set(configuration());
    const fixture = TestBed.createComponent(AutoLlmDashboard); fixture.detectChanges(); await fixture.whenStable();
    const button = [...fixture.nativeElement.querySelectorAll('button')].find((element: unknown) => (element as HTMLButtonElement).textContent?.includes('Create and start')) as HTMLButtonElement;
    expect(button.disabled).toBe(true);
    fixture.componentInstance.executionTarget({ target: { value: 'LOCAL' } } as unknown as Event); fixture.detectChanges();
    expect(button.disabled).toBe(false); expect(TestBed.inject(AutoLlmStore).config()?.execution.target).toBe('LOCAL');
  });
  it('configures SSH only when selected and preserves an explicit worker override', () => {
    const store = TestBed.inject(AutoLlmStore); const config = configuration(); config.execution.workers = 32; store.config.set(config);
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    fixture.componentInstance.executionTarget({ target: { value: 'SSH' } } as unknown as Event);
    expect(store.config()?.execution.ssh?.servers[0].javaExecutable).toBe('java'); expect(store.config()?.execution.workers).toBe(32);
    fixture.componentInstance.workerMode({ target: { value: 'AUTO' } } as unknown as Event); expect(store.config()?.execution.workers).toBe(-1);
  });
  it('edits a server pool and preserves its selected screening server', () => {
    const store = TestBed.inject(AutoLlmStore); store.config.set(configuration());
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    fixture.componentInstance.executionTarget({ target: { value: 'SSH' } } as unknown as Event);
    fixture.componentInstance.addServer();
    expect(store.config()?.execution.ssh?.servers).toHaveLength(2);
    fixture.componentInstance.update('execution.ssh.servers.1.host', { target: { value: 'second' } } as unknown as Event);
    expect(store.config()?.execution.ssh?.servers[1].host).toBe('second');
    fixture.componentInstance.advanced({ target: { value: JSON.stringify({ execution: { ssh: { runnerArtifact: '/runner.jar' } } }) } } as unknown as Event);
    expect(store.config()?.execution.ssh?.screeningServerId).toBe('server-1');
    expect(store.config()?.execution.ssh?.servers).toHaveLength(2);
    fixture.componentInstance.removeServer(1); expect(store.config()?.execution.ssh?.servers).toHaveLength(1);
  });
  it('renders saved trial outcomes, winner components, and convergence', async () => {
    const fixture = TestBed.createComponent(AutoLlmDashboard); fixture.detectChanges(); await fixture.whenStable();
    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('1/1 trials finished'); expect(text).toContain('ConstructiveNew'); expect(text).toContain('Comparison results');
    expect(fixture.nativeElement.querySelector('app-dashboard-chart')).not.toBeNull();
    expect(fixture.componentInstance.comparisonSummary()).not.toHaveProperty('curves');
  });
  it('uses relative milliseconds for saved nanosecond progress curves', () => {
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    expect(fixture.componentInstance.curveChart().options.series).toEqual([expect.objectContaining({ name: 'SOTA · repetition 1', data: [[0, 10], [10, 8]] })]);
  });
  it('shows relative instance labels and retains full paths when switching the chart', async () => {
    const store = TestBed.inject(AutoLlmStore);
    const paths = ['/remote/campaign/data/00000000/one.txt', '/remote/campaign/data/00000001/two.txt'];
    store.comparison.set({ curves: paths.map((instance, index) => ({ instance, instanceId: instance, algorithm: 'SOTA', iteration: '0', curves: { Cost: [{ instant: 0, value: index + 10 }] } })) });
    const fixture = TestBed.createComponent(AutoLlmDashboard); fixture.detectChanges(); await fixture.whenStable();
    const select = fixture.nativeElement.querySelector('.comparison-controls select') as HTMLSelectElement;
    expect([...select.options].map(option => option.textContent?.trim())).toEqual(['00000000/one.txt', '00000001/two.txt']);
    expect(select.options[1].value).toBe(paths[1]); expect(select.options[1].title).toBe(paths[1]);
    select.value = paths[1]; select.dispatchEvent(new Event('change')); fixture.detectChanges();
    expect(fixture.componentInstance.activeInstance()).toBe(paths[1]);
    expect(fixture.componentInstance.curveChart().options.series![0]).toEqual(expect.objectContaining({ data: [[0, 11]] }));
    expect(select.title).toBe(paths[1]);
  });
  it('shows unavailable usage for older campaigns', async () => {
    const fixture = TestBed.createComponent(AutoLlmDashboard); fixture.detectChanges(); await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Codex token usage · UNAVAILABLE');
  });
  it('uses backend recovery eligibility and requires a recorded reason to end a trial', async () => {
    const store = TestBed.inject(AutoLlmStore);
    (store.trial as WritableSignal<AutoLlmTrial | null>).set({ ...store.trial()!, status: 'BLOCKED' });
    const recovery = { resume: { allowed: false, reason: 'Requalify the assigned server first', effect: '' }, fail: { allowed: true, reason: null, effect: 'Preserves evidence and releases the server' } };
    (store.trialRecovery as WritableSignal<unknown>).set(recovery);
    store.failTrial = vi.fn(); store.resumeTrial = vi.fn();
    const fixture = TestBed.createComponent(AutoLlmDashboard); fixture.detectChanges(); await fixture.whenStable();
    const panel = fixture.nativeElement.querySelector('.trial-recovery') as HTMLElement;
    const buttons = panel.querySelectorAll('button');
    expect(panel.textContent).toContain('Requalify the assigned server first');
    expect(buttons[0].disabled).toBe(true); expect(buttons[1].disabled).toBe(true);
    const reason = panel.querySelector('textarea')!; reason.value = 'Sampler failed'; reason.dispatchEvent(new Event('input')); fixture.detectChanges();
    expect(buttons[1].disabled).toBe(false); buttons[1].click();
    expect(store.failTrial).toHaveBeenCalledWith('Sampler failed'); expect(store.resumeTrial).not.toHaveBeenCalled();
  });
  it('shows server requalification eligibility and scopes the action to that server', async () => {
    const store = TestBed.inject(AutoLlmStore);
    (store.campaign as WritableSignal<AutoLlmCampaign | null>).update(c => c ? { ...c, config: { ...c.config, execution: { ...c.config.execution, target: 'SSH' } } } : c);
    (store.targets as WritableSignal<unknown>).set([{ id: 'worker-a', status: 'NEEDS_ATTENTION', definition: { host: 'user@worker-a' } }]);
    const allowed = { allowed: true, reason: null, effect: 'Run qualification before resuming blocked work' };
    store.recovery.set({ resumeCampaign: allowed, trials: {}, targets: { 'worker-a': { resume: allowed, requalify: allowed } } });
    store.targetAction = vi.fn();
    const fixture = TestBed.createComponent(AutoLlmDashboard); fixture.detectChanges(); await fixture.whenStable();
    const button = [...fixture.nativeElement.querySelectorAll('.target-card button')].find((element: unknown) => (element as HTMLButtonElement).textContent?.includes('Requalify server')) as HTMLButtonElement;
    expect(button.disabled).toBe(false); button.click();
    expect(store.targetAction).toHaveBeenCalledWith('worker-a', 'requalify');
    store.recovery.set({ resumeCampaign: allowed, trials: {}, targets: { 'worker-a': { resume: allowed, requalify: { ...allowed, allowed: false, reason: 'Reconcile remote jobs first' } } } });
    fixture.detectChanges(); expect(button.disabled).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Reconcile remote jobs first');
  });
  it('offers a bounded tail preview and downloads the full file directly', async () => {
    const store = TestBed.inject(AutoLlmStore);
    store.logPath.set('attempt/stdout.log');
    store.logPreview.set({ text: 'latest line\n', lineCount: 1, sizeBytes: 1000000, truncated: true, byteLimited: true });
    store.setLogLines = vi.fn(); store.selectLog = vi.fn();
    store.artifactUrl = path => '/artifact?path=' + path;
    const fixture = TestBed.createComponent(AutoLlmDashboard); fixture.detectChanges(); await fixture.whenStable();
    const preview = fixture.nativeElement.querySelector('.log-preview') as HTMLElement;
    expect(preview.querySelector('pre')?.textContent).toBe('latest line\n');
    expect(preview.textContent).toContain('last 500 lines');
    expect(preview.textContent).toContain('256 KiB byte limit');
    const download = preview.querySelector('a') as HTMLAnchorElement;
    expect(download.textContent).toContain('Download full file');
    expect(download.getAttribute('href')).toBe('/artifact?path=attempt/stdout.log');
    expect(download.hasAttribute('download')).toBe(true);
    const select = preview.querySelector('select')!; select.value = '100'; select.dispatchEvent(new Event('change'));
    expect(store.setLogLines).toHaveBeenCalledWith(100);
  });
  it('shows partial known totals, unknown fields, and conversation downloads', async () => {
    const store = TestBed.inject(AutoLlmStore);
    const usage = { status: 'PARTIAL' as const, tokens: { inputTokens: 1234, cachedInputTokens: null, cacheWriteInputTokens: null, outputTokens: 0, reasoningOutputTokens: null, totalTokens: null }, attempts: 2, completeAttempts: 0, partialAttempts: 1, unavailableAttempts: 1 };
    (store.campaign as WritableSignal<AutoLlmCampaign | null>).update(c => c ? { ...c, codexUsage: usage } : c);
    (store.trial as WritableSignal<AutoLlmTrial | null>).set({ ...store.trial()!, codexUsage: usage, attempts: [{ number: 1, stage: 'GENERATION', status: 'FAILED', directory: 'attempt', startedAt: '2026-10-12T00:00:00Z', finishedAt: null, failure: null, codexEvidence: { status: 'ARCHIVED', threadId: 'thread', failure: null, usage, conversations: [{ threadId: 'thread', parentThreadId: null, path: 'conversations/thread.jsonl', sha256: 'hash', cliVersion: 'fixture', models: ['fixture-model'] }] } }] });
    (store as unknown as { artifactUrl: (path: string) => string }).artifactUrl = path => '/artifact?path=' + path;
    const fixture = TestBed.createComponent(AutoLlmDashboard); fixture.detectChanges(); await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Input: 1,234'); expect(fixture.nativeElement.textContent).toContain('Output: 0'); expect(fixture.nativeElement.textContent).toContain('Total: Unknown');
    expect(fixture.nativeElement.textContent).toContain('Known totals may be incomplete');
    expect(fixture.nativeElement.querySelector('a').getAttribute('href')).toContain('attempt/conversations/thread.jsonl');
  });
  it('renders readable live generation with observable counters and session identity', async () => {
    const store = TestBed.inject(AutoLlmStore);
    (store.generation as WritableSignal<CodexProgress | null>).set({ state: 'ACTIVE', threadId: 'live-thread', lastActivityAt: '2026-10-20T10:00:00Z', turns: 1, commandsStarted: 3, commandsCompleted: 2, edits: 1, messages: 2, usage: null });
    (store.conversationAttempt as WritableSignal<unknown>).set({ number: 1, stage: 'GENERATION', status: 'RUNNING', directory: 'attempt' });
    store.activity.set([{ timestamp: '2026-10-20T10:00:00Z', type: 'item.completed', text: 'Building a new neighborhood' }]);
    const fixture = TestBed.createComponent(AutoLlmDashboard); fixture.detectChanges(); await fixture.whenStable();
    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('live-thread'); expect(text).toContain('2 completed / 3 started');
    expect(text).toContain('Building a new neighborhood'); expect(text).toContain('model iteration totals are unavailable');
  });
  it('shows screening progress and requires a completed review for approval', async () => {
    const store = TestBed.inject(AutoLlmStore);
    const job = { id: 'screening', createdAt: '2026-10-20T00:00:00Z', status: 'AWAITING_APPROVAL', mirror: '/evidence', problem: null, activity: null,
      progress: { phase: 'AWAITING_APPROVAL', initialCompleted: 415, initialTotal: 415, confirmationsCompleted: 5850, confirmationsTotal: 5850, updatedAt: '2026-10-20T10:00:00Z' } } as ScreeningJob;
    store.screenings.set([job]); (store.screening as WritableSignal<ScreeningJob | null>).set(job);
    const fixture = TestBed.createComponent(AutoLlmDashboard); fixture.detectChanges(); await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('415/415'); expect(fixture.nativeElement.textContent).toContain('5850/5850');
    expect(fixture.nativeElement.textContent).not.toContain('Approve this reviewed selection');
    store.review.set({ jobId: job.id, report: 'Completed report', proposalSha256: 'reviewed-hash', proposalDirectory: '/evidence/proposal', artifactPrefix: 'proposal', proposal: { trainingCount: 50, testingCount: 333, instances: [], splitFailure: null } } as ScreeningReview);
    (store as unknown as { screeningArtifactUrl: (path: string) => string }).screeningArtifactUrl = path => '/artifact?path=' + path;
    fixture.detectChanges(); await fixture.whenStable(); expect(fixture.nativeElement.textContent).toContain('Approve this reviewed selection');
    expect(fixture.nativeElement.textContent).toContain('reviewed-hash');
  });
});
