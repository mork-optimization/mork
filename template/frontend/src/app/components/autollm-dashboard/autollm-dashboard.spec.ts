import { Component, input, provideZonelessChangeDetection, signal, WritableSignal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';
import { ChartViewModel } from '../../model/dashboard';
import { AutoLlmStore } from '../../store/autollm-store';
import { AutoLlmDashboard } from './autollm-dashboard';
import { AutoLlmCampaign, AutoLlmTrial } from '../../model/autollm';

@Component({ selector: 'app-dashboard-chart', template: '' })
class ChartStub { readonly model = input.required<ChartViewModel>(); }

describe('AutoLLM dashboard', () => {
  beforeEach(async () => {
    const trial = { number: 1, status: 'SUCCEEDED', tuningSeed: 1234, repairTurns: 1, attempts: [], winner: { $component: 'SimpleAlgorithm', constructive: { $component: 'ConstructiveNew' } } };
    const campaign = { id: 'campaign', status: 'SUCCEEDED', createdAt: '2026-10-07T00:00:00Z', trials: [trial], config: {}, versions: {} };
    const store = { config: signal(null), error: signal(null), connectionError: signal(null), active: signal(''), busy: signal(false), selectedId: signal(campaign.id), selectedTrial: signal(1), campaigns: signal([campaign]), campaign: signal(campaign), trial: signal(trial), completed: signal(1), tuning: signal(null), logPath: signal(''), logLines: signal([]), artifacts: signal([]), comparison: signal({ counts: { SOTA: { SUCCEEDED: 1 } }, curves: [{ instance: 'instance.txt', instanceId: 'instance', algorithm: 'SOTA', iteration: '0', curves: { Cost: [{ instant: 0, value: 10 }, { instant: 10000000, value: 8 }] } }] }), start: vi.fn().mockResolvedValue(undefined) };
    await TestBed.configureTestingModule({ imports: [AutoLlmDashboard], providers: [provideZonelessChangeDetection(), { provide: AutoLlmStore, useValue: store }] })
      .overrideComponent(AutoLlmDashboard, { remove: { imports: [DashboardChart] }, add: { imports: [ChartStub] } }).compileComponents();
  });
  afterEach(() => TestBed.resetTestingModule());
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
