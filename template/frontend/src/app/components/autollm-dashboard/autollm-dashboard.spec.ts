import { Component, input, provideZonelessChangeDetection, signal, WritableSignal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';
import { ChartViewModel } from '../../model/dashboard';
import { AutoLlmStore } from '../../store/autollm-store';
import { AutoLlmDashboard } from './autollm-dashboard';
import { AutoLlmCampaign, AutoLlmConfig, AutoLlmTrial } from '../../model/autollm';

@Component({ selector: 'app-dashboard-chart', template: '' })
class ChartStub { readonly model = input.required<ChartViewModel>(); }

describe('AutoLLM dashboard', () => {
  beforeEach(async () => {
    const trial = { number: 1, status: 'SUCCEEDED', tuningSeed: 1234, repairTurns: 1, attempts: [], winner: { $component: 'SimpleAlgorithm', constructive: { $component: 'ConstructiveNew' } } };
    const campaign = { id: 'campaign', status: 'SUCCEEDED', createdAt: '2026-10-07T00:00:00Z', trials: [trial], config: {}, versions: {} };
    const store = { config: signal(null), targetStatus: signal(null), error: signal(null), connectionError: signal(null), active: signal(''), busy: signal(false), selectedId: signal(campaign.id), selectedTrial: signal(1), campaigns: signal([campaign]), campaign: signal(campaign), trial: signal(trial), completed: signal(1), tuning: signal(null), logPath: signal(''), logLines: signal([]), artifacts: signal([]), comparison: signal({ counts: { SOTA: { SUCCEEDED: 1 } }, curves: [{ instance: 'instance.txt', instanceId: 'instance', algorithm: 'SOTA', iteration: '0', curves: { Cost: [{ instant: 0, value: 10 }, { instant: 10000000, value: 8 }] } }] }), start: vi.fn().mockResolvedValue(undefined) };
    await TestBed.configureTestingModule({ imports: [AutoLlmDashboard], providers: [provideZonelessChangeDetection(), { provide: AutoLlmStore, useValue: store }] })
      .overrideComponent(AutoLlmDashboard, { remove: { imports: [DashboardChart] }, add: { imports: [ChartStub] } }).compileComponents();
  });
  afterEach(() => TestBed.resetTestingModule());
  function configuration(): AutoLlmConfig {
    return { project: { repository: '/repo', ref: 'HEAD', buildCommand: ['mvn', 'verify'], artifact: 'target/project.jar', runArguments: [] },
      workspace: '/work', trials: 1, seed: 1234, maxParameters: 299, sotaFile: '/sota.json', initialConfigurationsFile: null, trainingPath: '/train.index', comparisonPath: '/test.index',
      codex: { executable: 'codex', promptFile: null, prompt: null, model: null, reasoningEffort: null }, preparation: { pilotDurationMillis: 1000, maxRepairs: 1 },
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
    expect(store.config()?.execution.ssh?.javaExecutable).toBe('java'); expect(store.config()?.execution.workers).toBe(32);
    fixture.componentInstance.workerMode({ target: { value: 'AUTO' } } as unknown as Event); expect(store.config()?.execution.workers).toBe(-1);
  });
  it('merges SSH environment updates without discarding the selected server', () => {
    const store = TestBed.inject(AutoLlmStore); const config = configuration();
    config.execution.ssh = { host: 'server', workspace: '/remote', javaExecutable: 'java', runnerArtifact: null, environment: { PATH: '/tools', JAVA_HOME: '/jdk' } }; store.config.set(config);
    const fixture = TestBed.createComponent(AutoLlmDashboard);
    fixture.componentInstance.advanced({ target: { value: JSON.stringify({ execution: { target: 'SSH', ssh: { environment: { JAVA_HOME: '/jdk2' } } } }) } } as unknown as Event);
    expect(store.config()?.execution.ssh?.host).toBe('server'); expect(store.config()?.execution.ssh?.environment).toEqual({ PATH: '/tools', JAVA_HOME: '/jdk2' }); expect(fixture.componentInstance.executionChoice()).toBe('SSH');
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
  it('shows unavailable usage for older campaigns', async () => {
    const fixture = TestBed.createComponent(AutoLlmDashboard); fixture.detectChanges(); await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Codex token usage · UNAVAILABLE');
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
});
