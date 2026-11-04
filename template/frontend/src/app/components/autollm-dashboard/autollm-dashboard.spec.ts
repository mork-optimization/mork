import {
  computed,
  Component,
  input,
  provideZonelessChangeDetection,
  signal,
  WritableSignal,
} from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';
import { ChartViewModel } from '../../model/dashboard';
import { AutoLlmStore } from '../../store/autollm-store';
import { AutoLlmDashboard } from './autollm-dashboard';
import { AutoLlmTimeline } from '../autollm-timeline/autollm-timeline';
import { timedAttempt, timedTrial, timingEpoch } from '../../testing/autollm-timing-fixture';
import {
  AutoLlmComparison,
  ComparisonInstance,
  AutoLlmActivity,
  AutoLlmCampaign,
  AutoLlmConfig,
  AutoLlmTrial,
  CodexProgress,
  ScreeningJob,
  ScreeningReview,
} from '../../model/autollm';

@Component({ selector: 'app-dashboard-chart', template: '' })
class ChartStub {
  readonly model = input.required<ChartViewModel>();
  readonly resizable = input(false);
}

describe('AutoLLM dashboard', () => {
  let originalScrollIntoView: PropertyDescriptor | undefined;
  let scrollTo: ReturnType<typeof vi.spyOn>;
  beforeEach(async () => {
    scrollTo = vi.spyOn(window, 'scrollTo').mockImplementation(() => {});
    // jsdom has no layout or scrolling implementation.
    originalScrollIntoView = Object.getOwnPropertyDescriptor(
      HTMLElement.prototype,
      'scrollIntoView',
    );
    Object.defineProperty(HTMLElement.prototype, 'scrollIntoView', {
      configurable: true,
      writable: true,
      value: vi.fn(),
    });
    window.history.replaceState(null, '', window.location.pathname);
    localStorage.clear();
    const trial = {
      number: 1,
      status: 'SUCCEEDED',
      tuningSeed: 1234,
      repairTurns: 1,
      attempts: [],
      winner: { $component: 'SimpleAlgorithm', constructive: { $component: 'ConstructiveNew' } },
    };
    const campaign = {
      id: 'campaign',
      status: 'SUCCEEDED',
      createdAt: '2026-10-07T00:00:00Z',
      trials: [trial],
      config: { execution: { target: 'LOCAL' }, comparison: { repetitions: 30 } },
      versions: {},
      poolRevision: 1,
      targets: {},
    };
    const store = {
      now: signal(Date.now()),
      inspecting: signal(false),
      detail: signal(null),
      detailLoading: signal(false),
      detailError: signal(null),
      lastUpdated: signal(Date.now()),
      progressError: signal(null),
      comparisonError: signal(null),
      comparisonUpdated: signal(null),
      results: signal(null),
      resultsLoading: signal(false),
      resultsError: signal(null),
      resultObjective: signal(''),
      resultMode: signal('MINIMIZE'),
      objectiveNames: signal(['Cost']),
      objectivesError: signal(null),
      artifactsLoaded: signal(false),
      artifactsLoading: signal(false),
      artifactsError: signal(null),
      refreshDetail: vi.fn(),
      refreshSummary: vi.fn(),
      refreshArtifacts: vi.fn(),
      selectTrial: vi.fn((number: number) => {
        store.selectedTrial.set(number);
        store.inspecting.set(true);
      }),
      select: vi.fn(),
      watchGeneration: vi.fn(),
      cleanupPlan: signal(null),
      cleanupReceipt: signal(null),
      approvalDifferences: signal([]),
      reviewCleanup: vi.fn(),
      applyCleanup: vi.fn(),
      proposal: signal(null),
      startTargets: signal([]),
      targets: signal([]),
      activeTrials: signal([]),
      activeProgress: signal({}),
      screenings: signal([]),
      activeScreening: signal(''),
      selectedScreening: signal(''),
      screening: signal(null),
      review: signal(null),
      summary: signal(null),
      progress: signal(null),
      generation: signal(null),
      conversationAttempt: signal(null),
      activity: signal([]),
      config: signal(null),
      targetStatus: signal(null),
      recovery: signal(null),
      recoveryError: signal(null),
      trialRecovery: signal(null),
      error: signal(null),
      connectionError: signal(null),
      active: signal(''),
      busy: signal(false),
      selectedId: signal(campaign.id),
      selectedTrial: signal(1),
      campaigns: signal([campaign]),
      campaign: signal(campaign),
      trial: signal(trial),
      completed: signal(1),
      tuning: signal(null),
      logPath: signal(''),
      logLines: signal(500),
      logFollow: signal(true),
      logPreview: signal(null),
      logLoading: signal(false),
      logError: signal(null),
      artifacts: signal([]),
      comparison: signal<AutoLlmComparison>({
        counts: { SOTA: { SUCCEEDED: 1 } },
        instances: [
          { instance: 'instance.txt', instanceId: 'instance', counts: { SOTA: { SUCCEEDED: 1 } } },
        ],
      }),
      comparisonInstances: computed((): ComparisonInstance[] => store.comparison().instances ?? []),
      curveInstance: signal('instance'),
      curvesLoading: signal(false),
      curvesError: signal(null),
      curves: signal([
        {
          instance: 'instance.txt',
          instanceId: 'instance',
          algorithm: 'SOTA',
          iteration: '0',
          curves: {
            Cost: [
              { instant: 0, value: 10 },
              { instant: 10000000, value: 8 },
            ],
          },
        },
      ]),
      start: vi.fn().mockResolvedValue(undefined),
    };
    await TestBed.configureTestingModule({
      imports: [AutoLlmDashboard],
      providers: [provideZonelessChangeDetection(), { provide: AutoLlmStore, useValue: store }],
    })
      .overrideComponent(AutoLlmDashboard, {
        remove: { imports: [DashboardChart] },
        add: { imports: [ChartStub] },
      })
      .overrideComponent(AutoLlmTimeline, {
        remove: { imports: [DashboardChart] },
        add: { imports: [ChartStub] },
      })
      .compileComponents();
  });
  afterEach(() => {
    try {
      TestBed.resetTestingModule();
    } finally {
      scrollTo.mockRestore();
      if (originalScrollIntoView)
        Object.defineProperty(HTMLElement.prototype, 'scrollIntoView', originalScrollIntoView);
      else Reflect.deleteProperty(HTMLElement.prototype, 'scrollIntoView');
    }
  });
  it('opens the selected timeline attempt in stage evidence and links to its activity', async () => {
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    const store = TestBed.inject(AutoLlmStore);
    const trial = timedTrial([
      timedAttempt(1, 'GENERATION', 0, 10),
      timedAttempt(2, 'REPAIR', 20, 30),
    ]);
    (store.trial as WritableSignal<AutoLlmTrial | null>).set(trial);
    store.now.set(timingEpoch + 30000);
    store.inspecting.set(true);
    fixture.componentInstance.openTimedAttempt({ trial: 1, attempt: 2, stage: 'GENERATION' });
    await fixture.whenStable();
    expect(store.selectTrial).toHaveBeenCalledWith(1);
    expect(
      fixture.nativeElement.querySelector('.stage-evidence .selected-row').textContent,
    ).toContain('Repair');
    expect(fixture.nativeElement.querySelector('.stage-timeline').textContent).toContain('20s');
    fixture.nativeElement.querySelector('.stage-evidence .selected-row button').click();
    await fixture.whenStable();
    expect(store.watchGeneration).toHaveBeenCalledWith(2);
    expect(fixture.componentInstance.detailTab()).toBe(3);
    expect(fixture.nativeElement.querySelector('.attempt.selected-row').textContent).toContain(
      'REPAIR',
    );
  });
  it('freezes stage durations at the last successful status update when polling is stale', () => {
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    const store = TestBed.inject(AutoLlmStore);
    (store.trial as WritableSignal<AutoLlmTrial | null>).set(
      timedTrial([timedAttempt(1, 'TUNING', 0, null)], { status: 'TUNING' }),
    );
    store.lastUpdated.set(timingEpoch + 10000);
    store.now.set(timingEpoch + 60000);
    expect(fixture.componentInstance.stageSummaries()[3].duration).toBe(10000);
  });
  it('preserves timeline controls through inspection, history navigation and reloading a selected attempt', async () => {
    const store = TestBed.inject(AutoLlmStore);
    const trials = Array.from({ length: 60 }, (_, i) =>
      timedTrial([timedAttempt(1, 'GENERATION', 0, 10), timedAttempt(2, 'TUNING', 10, 20)], {
        number: i + 1,
        targetId: 'worker-b',
      }),
    );
    const campaign = { ...store.campaign()!, trials };
    (store.campaign as WritableSignal<AutoLlmCampaign | null>).set(campaign);
    store.campaigns.set([campaign]);
    store.selectTrial = vi.fn((number) => {
      store.selectedTrial.set(number);
      store.inspecting.set(true);
      (store.trial as WritableSignal<AutoLlmTrial | null>).set(
        trials.find((trial) => trial.number === number)!,
      );
    });
    const fixture = TestBed.createComponent(AutoLlmDashboard),
      dashboard = fixture.componentInstance;
    await fixture.whenStable();
    const settings = {
      mode: 'clock' as const,
      sort: 'TUNING',
      search: 'worker-b',
      page: 1,
      outcome: 'all' as const,
    };
    dashboard.setTimelineSettings(settings);
    const timelineUrl = window.location.hash;
    dashboard.openTimedAttempt({ trial: 38, stage: 'TUNING', attempt: 2 });
    await fixture.whenStable();
    const attemptUrl = window.location.hash;
    expect(attemptUrl).toContain('attempt=2');
    expect(attemptUrl).toContain('stage=TUNING');
    dashboard.closeTrial();
    await fixture.whenStable();
    expect(dashboard.timelineSettings()).toEqual(settings);
    expect(fixture.nativeElement.querySelector('app-autollm-timeline select').value).toBe('clock');
    expect(fixture.nativeElement.querySelector('app-autollm-timeline input').value).toBe(
      'worker-b',
    );
    for (const hash of [attemptUrl, timelineUrl, attemptUrl]) {
      window.history.replaceState(null, '', hash);
      window.dispatchEvent(new PopStateEvent('popstate'));
      await fixture.whenStable();
      expect(store.inspecting()).toBe(hash === attemptUrl);
      expect(dashboard.timelineSettings()).toEqual(settings);
    }
    expect(dashboard.selectedStage()).toBe('TUNING');
    expect(dashboard.selectedAttempt()).toBe(2);
    fixture.destroy();
    const reloaded = TestBed.createComponent(AutoLlmDashboard);
    await reloaded.whenStable();
    expect(reloaded.componentInstance.timelineSettings()).toEqual(settings);
    expect(reloaded.componentInstance.selectedAttempt()).toBe(2);
    expect(
      reloaded.nativeElement.querySelector('.stage-evidence .selected-row').textContent,
    ).toContain('20');
  });
  it('uses overview counts to filter the timeline and keeps it ahead of collapsed details', async () => {
    const store = TestBed.inject(AutoLlmStore);
    (store.campaign as WritableSignal<AutoLlmCampaign | null>).update((c) =>
      c
        ? {
            ...c,
            trials: [
              timedTrial([], { number: 1, status: 'WAITING_FOR_GENERATION' }),
              timedTrial([], { number: 2, status: 'BLOCKED' }),
              timedTrial([], { number: 3, status: 'SUCCEEDED' }),
            ],
          }
        : c,
    );
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    await fixture.whenStable();
    const dashboard = fixture.componentInstance;
    expect(dashboard.runningCount()).toBe(1);
    const buttons = [
      ...fixture.nativeElement.querySelectorAll('.summary-counts button'),
    ] as HTMLButtonElement[];
    buttons.find((button) => button.textContent!.trim().startsWith('Needs attention'))!.click();
    await fixture.whenStable();
    expect(dashboard.timelineSettings().outcome).toBe('attention');
    expect(fixture.nativeElement.querySelector('app-autollm-timeline tbody').textContent).toContain(
      'Open trial 2',
    );
    expect(
      fixture.nativeElement.querySelector('app-autollm-timeline tbody').textContent,
    ).not.toContain('Open trial 1');
    expect(fixture.nativeElement.querySelector('.overview-details').open).toBe(false);
    const timeline = fixture.nativeElement.querySelector('app-autollm-timeline');
    expect(
      timeline.compareDocumentPosition(fixture.nativeElement.querySelector('.overview-details')) &
        Node.DOCUMENT_POSITION_FOLLOWING,
    ).toBeTruthy();
  });
  it('distinguishes a quota wait from a blocked trial and exposes server review without mutation', async () => {
    const store = TestBed.inject(AutoLlmStore);
    store.active.set('campaign');
    store.lastUpdated.set(timingEpoch);
    const trial = timedTrial([timedAttempt(1, 'GENERATION', -10, 0, 'QUOTA_LIMIT')], {
      status: 'WAITING_FOR_QUOTA',
      quotaRetryAt: new Date(timingEpoch + 180000).toISOString(),
    });
    (store.trial as WritableSignal<AutoLlmTrial | null>).set(trial);
    store.inspecting.set(true);
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.trial-notice.waiting').textContent).toContain(
      'Automatic retry in 3m',
    );
    expect(fixture.nativeElement.querySelector('.stage-timeline li.waiting')).not.toBeNull();
    expect(fixture.nativeElement.querySelector('.trial-recovery')).toBeNull();
    (store.trial as WritableSignal<AutoLlmTrial | null>).set({
      ...trial,
      status: 'BLOCKED',
      targetId: 'worker-b',
      problem: {
        category: 'INFRASTRUCTURE',
        code: 'SSH',
        message: 'SSH authentication failed',
        recovery: 'Check credentials',
        retryable: false,
        evidence: [],
      },
    });
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.trial-notice.attention').textContent).toContain(
      'SSH authentication failed',
    );
    expect(fixture.nativeElement.querySelector('.trial-recovery').open).toBe(false);
    const review = [...fixture.nativeElement.querySelectorAll('.trial-notice button')].find(
      (button: any) => button.textContent.includes('Review server'),
    ) as HTMLButtonElement;
    review.click();
    await fixture.whenStable();
    expect(fixture.componentInstance.tab()).toBe(3);
    expect(fixture.componentInstance.selectedTarget()).toBe('worker-b');
    expect(store.inspecting()).toBe(false);
  });
  function configuration(): AutoLlmConfig {
    return {
      project: {
        repository: '/repo',
        ref: 'HEAD',
        buildCommand: ['mvn', 'verify'],
        artifact: 'target/project.jar',
        runArguments: [],
      },
      workspace: '/work',
      trials: 1,
      seed: 1234,
      maxParameters: 299,
      sotaFile: '/sota.json',
      initialConfigurationsFile: null,
      trainingPath: '/train.index',
      comparisonPath: '/test.index',
      codex: {
        executable: 'codex',
        promptFile: null,
        prompt: null,
        model: null,
        reasoningEffort: null,
        maxConcurrentSessions: 1,
      },
      preparation: { probeDurationMillis: 1000, pilotDurationMillis: 1000, maxRepairs: 1 },
      tuning: {
        evaluations: 100,
        ignoreInitialMillis: 1000,
        intervalDurationMillis: 9000,
        logScaleArea: true,
      },
      comparison: { repetitions: 30, timeLimitMillis: 10000 },
      execution: {
        target: 'LOCAL',
        ssh: null,
        workers: -1,
        watchdogGraceMillis: 1000,
        codexTimeoutMillis: 10000,
        buildTimeoutMillis: 10000,
        startupTimeoutMillis: 10000,
        searchSpaceTimeoutMillis: 10000,
        phaseTimeoutMillis: 10000,
        stageTimeoutMillis: 10000,
        heap: '2g',
      },
    };
  }
  it('shows generation selection before approval and blocks an unselected model', async () => {
    const store = TestBed.inject(AutoLlmStore);
    const proposal = {
      digest: 'reviewed',
      configuration: configuration(),
      training: [],
      testing: [],
      targets: {},
      screening: {},
      generation: {
        model: null,
        reasoningEffort: null,
        ready: false,
        approvalLine:
          'Generation and repairs: model NOT SELECTED; set codex.model before approval; reasoning effort Codex default (not pinned).',
      },
    };
    store.proposal.set(proposal);
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    fixture.detectChanges();
    await fixture.whenStable();
    const approval = () =>
      [...fixture.nativeElement.querySelectorAll('button')].find((element: unknown) =>
        (element as HTMLButtonElement).textContent?.includes('Approve this campaign'),
      ) as HTMLButtonElement;
    expect(fixture.nativeElement.textContent).toContain(proposal.generation.approvalLine);
    expect(approval().disabled).toBe(true);
    store.proposal.set({
      ...proposal,
      generation: {
        model: 'chosen-model',
        reasoningEffort: 'high',
        ready: true,
        approvalLine:
          'Generation and repairs: model chosen-model (campaign setting); reasoning effort high (campaign setting).',
      },
    });
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('model chosen-model (campaign setting)');
    expect(approval().disabled).toBe(false);
  });
  it('requires a location choice before creating a campaign', async () => {
    TestBed.inject(AutoLlmStore).config.set(configuration());
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    fixture.componentInstance.tab.set(0);
    fixture.componentInstance.detailTab.set(0);
    fixture.componentInstance.settingsOpen.set(true);
    TestBed.inject(AutoLlmStore).inspecting.set(false);
    fixture.detectChanges();
    await fixture.whenStable();
    const button = [...fixture.nativeElement.querySelectorAll('button')].find((element: unknown) =>
      (element as HTMLButtonElement).textContent?.includes('Prepare campaign for review'),
    ) as HTMLButtonElement;
    expect(button.disabled).toBe(true);
    fixture.componentInstance.executionTarget({ target: { value: 'LOCAL' } } as unknown as Event);
    fixture.detectChanges();
    expect(button.disabled).toBe(false);
    expect(TestBed.inject(AutoLlmStore).config()?.execution.target).toBe('LOCAL');
  });
  it('configures SSH only when selected and preserves an explicit worker override', () => {
    const store = TestBed.inject(AutoLlmStore);
    const config = configuration();
    config.execution.workers = 32;
    store.config.set(config);
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    fixture.componentInstance.executionTarget({ target: { value: 'SSH' } } as unknown as Event);
    expect(store.config()?.execution.ssh?.servers[0].javaExecutable).toBe('java');
    expect(store.config()?.execution.workers).toBe(32);
    fixture.componentInstance.workerMode({ target: { value: 'AUTO' } } as unknown as Event);
    expect(store.config()?.execution.workers).toBe(-1);
  });
  it('edits a server pool and preserves its selected screening server', () => {
    const store = TestBed.inject(AutoLlmStore);
    store.config.set(configuration());
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    fixture.componentInstance.executionTarget({ target: { value: 'SSH' } } as unknown as Event);
    fixture.componentInstance.addServer();
    expect(store.config()?.execution.ssh?.servers).toHaveLength(2);
    fixture.componentInstance.update('execution.ssh.servers.1.host', {
      target: { value: 'second' },
    } as unknown as Event);
    expect(store.config()?.execution.ssh?.servers[1].host).toBe('second');
    fixture.componentInstance.advanced({
      target: { value: JSON.stringify({ execution: { ssh: { runnerArtifact: '/runner.jar' } } }) },
    } as unknown as Event);
    expect(store.config()?.execution.ssh?.screeningServerId).toBe('server-1');
    expect(store.config()?.execution.ssh?.servers).toHaveLength(2);
    fixture.componentInstance.removeServer(1);
    expect(store.config()?.execution.ssh?.servers).toHaveLength(1);
  });
  it('renders saved trial outcomes, winner components, and convergence', async () => {
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    fixture.componentInstance.tab.set(1);
    fixture.componentInstance.detailTab.set(2);
    fixture.componentInstance.settingsOpen.set(false);
    TestBed.inject(AutoLlmStore).inspecting.set(true);
    fixture.detectChanges();
    await fixture.whenStable();
    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('Back to campaign');
    expect(fixture.nativeElement.querySelector('.status-card')).toBeNull();
    expect(fixture.nativeElement.querySelector('.trial-table')).toBeNull();
    expect(text).toContain('ConstructiveNew');
    expect(text).toContain('First-ranked proposal');
    fixture.componentInstance.detailTab.set(1);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('app-dashboard-chart')).not.toBeNull();
    expect(fixture.nativeElement.textContent).toContain('SOTA: 1/30 repetitions completed');
    expect(text).not.toContain('Load next comparison curves');
  });
  it('uses relative milliseconds for saved nanosecond progress curves', () => {
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    expect(fixture.componentInstance.curveChart().options.series).toEqual([
      expect.objectContaining({
        name: 'SOTA · repetition 1',
        data: [
          [0, 10],
          [10, 8],
        ],
      }),
    ]);
  });
  it('shows relative instance labels and retains full paths when switching the chart', async () => {
    const store = TestBed.inject(AutoLlmStore);
    const paths = [
      '/remote/campaign/data/00000000/one.txt',
      '/remote/campaign/data/00000001/two.txt',
    ];
    store.comparison.set({
      instances: paths.map((instance, index) => ({
        instance,
        instanceId: `id-${index}`,
        counts: { SOTA: { SUCCEEDED: 1 } },
      })),
    });
    store.curveInstance.set('id-0');
    store.selectCurveInstance = vi.fn(async (id) => {
      store.curveInstance.set(id);
      const index = id === 'id-1' ? 1 : 0;
      store.curves.set([
        {
          instance: paths[index],
          instanceId: id,
          algorithm: 'SOTA',
          iteration: '0',
          curves: { Cost: [{ instant: 0, value: index + 10 }] },
        },
      ]);
    });
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    fixture.componentInstance.tab.set(1);
    fixture.componentInstance.detailTab.set(1);
    fixture.componentInstance.settingsOpen.set(false);
    TestBed.inject(AutoLlmStore).inspecting.set(true);
    fixture.detectChanges();
    await fixture.whenStable();
    const select = fixture.nativeElement.querySelector(
      '.comparison-controls select',
    ) as HTMLSelectElement;
    expect([...select.options].map((option) => option.textContent?.trim())).toEqual([
      '00000000/one.txt',
      '00000001/two.txt',
    ]);
    expect(select.options[1].value).toBe('id-1');
    expect(select.options[1].title).toBe(paths[1]);
    select.value = 'id-1';
    select.dispatchEvent(new Event('change'));
    fixture.detectChanges();
    expect(store.selectCurveInstance).toHaveBeenCalledWith('id-1');
    expect(fixture.componentInstance.activeInstance()?.instance).toBe(paths[1]);
    expect(fixture.componentInstance.curveChart().options.series![0]).toEqual(
      expect.objectContaining({ data: [[0, 11]] }),
    );
    expect(select.title).toBe(paths[1]);
  });
  it('shows unavailable usage for older campaigns', async () => {
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    fixture.componentInstance.tab.set(0);
    fixture.componentInstance.detailTab.set(0);
    fixture.componentInstance.settingsOpen.set(true);
    TestBed.inject(AutoLlmStore).inspecting.set(false);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Codex token usage · UNAVAILABLE');
  });
  it('uses backend recovery eligibility and requires a recorded reason to end a trial', async () => {
    const store = TestBed.inject(AutoLlmStore);
    (store.trial as WritableSignal<AutoLlmTrial | null>).set({
      ...store.trial()!,
      status: 'BLOCKED',
    });
    const recovery = {
      resume: { allowed: false, reason: 'Requalify the assigned server first', effect: '' },
      fail: { allowed: true, reason: null, effect: 'Preserves evidence and releases the server' },
    };
    (store.trialRecovery as WritableSignal<unknown>).set(recovery);
    store.failTrial = vi.fn();
    store.resumeTrial = vi.fn();
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    fixture.componentInstance.tab.set(1);
    fixture.componentInstance.detailTab.set(0);
    fixture.componentInstance.settingsOpen.set(false);
    TestBed.inject(AutoLlmStore).inspecting.set(true);
    fixture.detectChanges();
    await fixture.whenStable();
    const panel = fixture.nativeElement.querySelector('.trial-recovery') as HTMLElement;
    const buttons = panel.querySelectorAll('button');
    expect(panel.textContent).toContain('Requalify the assigned server first');
    expect(buttons[0].disabled).toBe(true);
    expect(buttons[1].disabled).toBe(true);
    const reason = panel.querySelector('textarea')!;
    reason.value = 'Sampler failed';
    reason.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    expect(buttons[1].disabled).toBe(false);
    buttons[1].click();
    expect(store.failTrial).toHaveBeenCalledWith('Sampler failed');
    expect(store.resumeTrial).not.toHaveBeenCalled();
  });
  it('shows server requalification eligibility and scopes the action to that server', async () => {
    const store = TestBed.inject(AutoLlmStore);
    (store.campaign as WritableSignal<AutoLlmCampaign | null>).update((c) =>
      c
        ? { ...c, config: { ...c.config, execution: { ...c.config.execution, target: 'SSH' } } }
        : c,
    );
    (store.targets as WritableSignal<unknown>).set([
      { id: 'worker-a', status: 'NEEDS_ATTENTION', definition: { host: 'user@worker-a' } },
    ]);
    const allowed = {
      allowed: true,
      reason: null,
      effect: 'Run qualification before resuming blocked work',
    };
    store.recovery.set({
      resumeCampaign: allowed,
      trials: {},
      targets: { 'worker-a': { resume: allowed, requalify: allowed } },
    });
    store.targetAction = vi.fn();
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    fixture.componentInstance.tab.set(3);
    fixture.componentInstance.detailTab.set(0);
    fixture.componentInstance.settingsOpen.set(false);
    TestBed.inject(AutoLlmStore).inspecting.set(false);
    fixture.detectChanges();
    await fixture.whenStable();
    const button = [...fixture.nativeElement.querySelectorAll('.target-card button')].find(
      (element: unknown) =>
        (element as HTMLButtonElement).textContent?.includes('Requalify server'),
    ) as HTMLButtonElement;
    expect(button.disabled).toBe(false);
    button.click();
    expect(store.targetAction).toHaveBeenCalledWith('worker-a', 'requalify');
    store.recovery.set({
      resumeCampaign: allowed,
      trials: {},
      targets: {
        'worker-a': {
          resume: allowed,
          requalify: { ...allowed, allowed: false, reason: 'Reconcile remote jobs first' },
        },
      },
    });
    fixture.detectChanges();
    expect(button.disabled).toBe(true);
    expect(fixture.nativeElement.textContent).toContain('Reconcile remote jobs first');
  });
  it('offers a bounded tail preview and downloads the full file directly', async () => {
    const store = TestBed.inject(AutoLlmStore);
    store.logPath.set('attempt/stdout.log');
    store.logPreview.set({
      text: 'latest line\n',
      lineCount: 1,
      sizeBytes: 1000000,
      truncated: true,
      byteLimited: true,
    });
    store.setLogLines = vi.fn();
    store.selectLog = vi.fn();
    store.artifactUrl = (path) => '/artifact?path=' + path;
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    fixture.componentInstance.tab.set(4);
    fixture.componentInstance.detailTab.set(0);
    fixture.componentInstance.settingsOpen.set(false);
    TestBed.inject(AutoLlmStore).inspecting.set(false);
    fixture.detectChanges();
    await fixture.whenStable();
    const preview = fixture.nativeElement.querySelector('.log-preview') as HTMLElement;
    expect(preview.querySelector('pre')?.textContent).toBe('latest line\n');
    expect(preview.textContent).toContain('last 500 lines');
    expect(preview.textContent).toContain('256 KiB byte limit');
    const download = preview.querySelector('a') as HTMLAnchorElement;
    expect(download.textContent).toContain('Download full file');
    expect(download.getAttribute('href')).toBe('/artifact?path=attempt/stdout.log');
    expect(download.hasAttribute('download')).toBe(true);
    const select = preview.querySelector('select')!;
    select.value = '100';
    select.dispatchEvent(new Event('change'));
    expect(store.setLogLines).toHaveBeenCalledWith(100);
  });
  it('shows partial known totals, unknown fields, and conversation downloads', async () => {
    const store = TestBed.inject(AutoLlmStore);
    const usage = {
      status: 'PARTIAL' as const,
      tokens: {
        inputTokens: 1234,
        cachedInputTokens: null,
        cacheWriteInputTokens: null,
        outputTokens: 0,
        reasoningOutputTokens: null,
        totalTokens: null,
      },
      attempts: 2,
      completeAttempts: 0,
      partialAttempts: 1,
      unavailableAttempts: 1,
    };
    (store.campaign as WritableSignal<AutoLlmCampaign | null>).update((c) =>
      c ? { ...c, codexUsage: usage } : c,
    );
    (store.trial as WritableSignal<AutoLlmTrial | null>).set({
      ...store.trial()!,
      codexUsage: usage,
      attempts: [
        {
          number: 1,
          stage: 'GENERATION',
          status: 'FAILED',
          directory: 'attempt',
          startedAt: '2026-10-12T00:00:00Z',
          finishedAt: null,
          failure: null,
          codexEvidence: {
            status: 'ARCHIVED',
            threadId: 'thread',
            failure: null,
            usage,
            conversations: [
              {
                threadId: 'thread',
                parentThreadId: null,
                path: 'conversations/thread.jsonl',
                sha256: 'hash',
                cliVersion: 'fixture',
                models: ['fixture-model'],
              },
            ],
          },
        },
      ],
    });
    (store as unknown as { artifactUrl: (path: string) => string }).artifactUrl = (path) =>
      '/artifact?path=' + path;
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    fixture.componentInstance.tab.set(1);
    fixture.componentInstance.detailTab.set(3);
    fixture.componentInstance.settingsOpen.set(true);
    TestBed.inject(AutoLlmStore).inspecting.set(true);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Input: 1,234');
    expect(fixture.nativeElement.textContent).toContain('Output: 0');
    expect(fixture.nativeElement.textContent).toContain('Total: Unknown');
    expect(fixture.nativeElement.textContent).toContain('Known totals may be incomplete');
    expect(
      fixture.nativeElement.querySelector('a[href*=conversations]').getAttribute('href'),
    ).toContain('attempt/conversations/thread.jsonl');
  });
  it('renders readable live generation with observable counters and session identity', async () => {
    const store = TestBed.inject(AutoLlmStore);
    (store.generation as WritableSignal<CodexProgress | null>).set({
      state: 'ACTIVE',
      threadId: 'live-thread',
      lastActivityAt: '2026-10-20T10:00:00Z',
      turns: 1,
      commandsStarted: 3,
      commandsCompleted: 2,
      edits: 1,
      messages: 2,
      usage: null,
    });
    (store.conversationAttempt as WritableSignal<unknown>).set({
      number: 1,
      stage: 'GENERATION',
      status: 'RUNNING',
      directory: 'attempt',
    });
    store.activity.set([
      {
        timestamp: '2026-10-20T10:00:00Z',
        type: 'item.completed',
        text: 'Building a new neighborhood',
      },
    ]);
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    fixture.componentInstance.tab.set(1);
    fixture.componentInstance.detailTab.set(4);
    fixture.componentInstance.settingsOpen.set(false);
    TestBed.inject(AutoLlmStore).inspecting.set(true);
    fixture.detectChanges();
    await fixture.whenStable();
    const text = fixture.nativeElement.textContent as string;
    expect(text).toContain('live-thread');
    expect(text).toContain('2 completed / 3 started');
    fixture.componentInstance.detailTab.set(3);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Building a new neighborhood');
    expect(text).toContain('model iteration totals are unavailable');
  });
  it('shows screening evidence before campaign review', async () => {
    const store = TestBed.inject(AutoLlmStore);
    const job = {
      id: 'screening',
      createdAt: '2026-10-20T00:00:00Z',
      status: 'READY_FOR_REVIEW',
      mirror: '/evidence',
      problem: null,
      activity: null,
      progress: {
        phase: 'READY_FOR_REVIEW',
        initialCompleted: 415,
        initialTotal: 415,
        confirmationsCompleted: 5850,
        confirmationsTotal: 5850,
        updatedAt: '2026-10-20T10:00:00Z',
      },
    } as ScreeningJob;
    store.screenings.set([job]);
    (store.screening as WritableSignal<ScreeningJob | null>).set(job);
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    fixture.componentInstance.tab.set(0);
    fixture.componentInstance.detailTab.set(0);
    fixture.componentInstance.settingsOpen.set(true);
    TestBed.inject(AutoLlmStore).inspecting.set(false);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('415/415');
    expect(fixture.nativeElement.textContent).toContain('5850/5850');
    expect(fixture.nativeElement.textContent).not.toContain('Approve this reviewed selection');
    store.review.set({
      jobId: job.id,
      report: 'Completed report',
      proposalSha256: 'reviewed-hash',
      proposalDirectory: '/evidence/proposal',
      artifactPrefix: 'proposal',
      proposal: { trainingCount: 50, testingCount: 333, instances: [], splitFailure: null },
    } as ScreeningReview);
    (store as unknown as { screeningArtifactUrl: (path: string) => string }).screeningArtifactUrl =
      (path) => '/artifact?path=' + path;
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).not.toContain('Approve this reviewed selection');
    expect(fixture.nativeElement.textContent).toContain('reviewed-hash');
  });
  it('keeps seeds out of the trial table and exposes them in diagnostics', async () => {
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    fixture.componentInstance.tab.set(1);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.trial-table').textContent).not.toContain('1234');
    TestBed.inject(AutoLlmStore).inspecting.set(true);
    fixture.componentInstance.detailTab.set(4);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.trial-inspector').textContent).toContain(
      'Tuning seed',
    );
    expect(fixture.nativeElement.querySelector('.trial-inspector').textContent).toContain('1234');
  });
  it('uses unique server IDs and repairs the screening-server selection after removal and rename', () => {
    const store = TestBed.inject(AutoLlmStore);
    store.config.set(configuration());
    const fixture = TestBed.createComponent(AutoLlmDashboard),
      component = fixture.componentInstance;
    component.executionTarget({ target: { value: 'SSH' } } as unknown as Event);
    component.addServer();
    component.removeServer(0);
    component.addServer();
    expect(store.config()?.execution.ssh?.servers.map((s) => s.id)).toEqual([
      'server-2',
      'server-1',
    ]);
    expect(store.config()?.execution.ssh?.screeningServerId).toBe('server-2');
    component.update('execution.ssh.servers.0.id', {
      target: { value: 'renamed' },
    } as unknown as Event);
    expect(store.config()?.execution.ssh?.screeningServerId).toBe('renamed');
  });
  it('links local preparation to its actual build log', async () => {
    const store = TestBed.inject(AutoLlmStore);
    (store.trial as WritableSignal<AutoLlmTrial | null>).set({
      ...store.trial()!,
      attempts: [
        {
          number: 1,
          stage: 'LOCAL_PREPARATION',
          status: 'SUCCEEDED',
          directory: 'attempt',
          startedAt: '2026-11-01',
          finishedAt: null,
          failure: null,
        },
      ],
    });
    store.selectLog = vi.fn();
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    fixture.componentInstance.tab.set(1);
    fixture.componentInstance.detailTab.set(3);
    store.inspecting.set(true);
    fixture.detectChanges();
    await fixture.whenStable();
    (fixture.nativeElement.querySelector('.attempt button') as HTMLButtonElement).click();
    expect(store.selectLog).toHaveBeenCalledWith('attempt/build/stdout.log');
  });
  it('requires saving edited draft settings before reviewing a saved proposal', async () => {
    const store = TestBed.inject(AutoLlmStore),
      original = configuration();
    (store.campaign as WritableSignal<AutoLlmCampaign | null>).update((c) =>
      c ? { ...c, status: 'DRAFT', config: original } : c,
    );
    store.config.set({ ...original, trials: original.trials + 1 });
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    fixture.detectChanges();
    await fixture.whenStable();
    const button = Array.from(
      fixture.nativeElement.querySelectorAll('button') as NodeListOf<HTMLButtonElement>,
    ).find((b) => b.textContent?.includes('Review campaign'))!;
    expect(button.disabled).toBe(true);
    store.config.set(structuredClone(original));
    fixture.detectChanges();
    expect(button.disabled).toBe(false);
  });
  it('returns from a dedicated trial view with filters, sorting and page intact', async () => {
    const store = TestBed.inject(AutoLlmStore);
    const trials = Array.from({ length: 60 }, (_, i) => ({ ...store.trial()!, number: i + 1 }));
    (store.campaign as WritableSignal<AutoLlmCampaign | null>).update((c) =>
      c ? { ...c, trials } : c,
    );
    store.selectTrial = vi.fn((number) => {
      store.selectedTrial.set(number);
      store.inspecting.set(true);
    });
    const fixture = TestBed.createComponent(AutoLlmDashboard),
      dashboard = fixture.componentInstance;
    await fixture.whenStable();
    dashboard.selectTab(1);
    dashboard.trialFilter.set('SUCCEEDED');
    dashboard.trialSort.set('duration');
    dashboard.trialPage.set(1);
    dashboard.openTrial(26);
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.trial-table')).toBeNull();
    dashboard.adjacentTrial(1);
    expect(store.selectedTrial()).toBe(27);
    dashboard.closeTrial();
    await fixture.whenStable();
    expect(dashboard.tab()).toBe(1);
    expect(dashboard.trialFilter()).toBe('SUCCEEDED');
    expect(dashboard.trialSort()).toBe('duration');
    expect(dashboard.trialPage()).toBe(1);
    expect(fixture.nativeElement.querySelector('.trial-table')).not.toBeNull();
    expect(window.location.hash).not.toContain('trial=');
  });
  it('chooses the next campaign action and explains unavailable resume inline', async () => {
    const store = TestBed.inject(AutoLlmStore);
    const fixture = TestBed.createComponent(AutoLlmDashboard),
      dashboard = fixture.componentInstance;
    await fixture.whenStable();
    expect(dashboard.primaryAction().label).toBe('View results');
    (store.campaign as WritableSignal<AutoLlmCampaign | null>).update((c) =>
      c ? { ...c, status: 'INTERRUPTED' } : c,
    );
    store.recovery.set({
      resumeCampaign: { allowed: false, reason: 'Worker reconciliation is pending', effect: '' },
      trials: {},
      targets: {},
    });
    await fixture.whenStable();
    expect(dashboard.primaryAction().label).toBe('Resume campaign');
    expect(fixture.nativeElement.textContent).toContain('Worker reconciliation is pending');
    (store.campaign as WritableSignal<AutoLlmCampaign | null>).update((c) =>
      c ? { ...c, status: 'NEEDS_ATTENTION' } : c,
    );
    expect(dashboard.primaryAction().label).toBe('Resolve issue');
  });
  it('summarizes amended trial counts and server selection against the saved campaign', async () => {
    const store = TestBed.inject(AutoLlmStore),
      original = configuration();
    (store.campaign as WritableSignal<AutoLlmCampaign | null>).update((c) =>
      c
        ? {
            ...c,
            status: 'RUNNING',
            config: original,
            approvedTargets: ['server-1'],
          }
        : c,
    );
    const updated = { ...original, trials: original.trials + 4 };
    store.config.set(updated);
    store.proposal.set({
      digest: 'amended',
      configuration: updated,
      training: ['a'],
      testing: ['a'],
      screening: {},
      targets: {
        'server-1': { selected: true, ready: true },
        'server-2': { selected: true, ready: true },
      },
      generation: {
        model: 'model',
        reasoningEffort: null,
        ready: true,
        approvalLine: 'Model: model',
      },
    });
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    await fixture.whenStable();
    expect(fixture.componentInstance.proposalChanges()).toEqual([
      { field: 'Total trials', before: String(original.trials), after: String(updated.trials) },
      { field: 'Approved servers', before: 'server-1', after: 'server-1, server-2' },
    ]);
    expect(fixture.nativeElement.textContent).toContain('Changes to approve');
  });
});
