import { TestBed } from '@angular/core/testing';
import { AutoLlmClient } from '../service/autollm-client';
import { AutoLlmCampaign, AutoLlmConfig, AutoLlmLog, ScreeningReview } from '../model/autollm';
import { AutoLlmStore } from './autollm-store';

describe('AutoLLM campaign lifecycle store', () => {
  const config = {} as AutoLlmConfig;
  const campaign = { id: 'campaign', status: 'RUNNING', config, trials: [{ number: 1, status: 'GENERATION' }, { number: 2, status: 'PENDING' }] } as AutoLlmCampaign;
  let client: { screeningAction: ReturnType<typeof vi.fn>; screeningReview: ReturnType<typeof vi.fn>; screenings: ReturnType<typeof vi.fn>; summary: ReturnType<typeof vi.fn>; activity: ReturnType<typeof vi.fn>; defaults: ReturnType<typeof vi.fn>; list: ReturnType<typeof vi.fn>; progress: ReturnType<typeof vi.fn>; comparison: ReturnType<typeof vi.fn>; validate: ReturnType<typeof vi.fn>; create: ReturnType<typeof vi.fn>; baselineCheck: ReturnType<typeof vi.fn>; action: ReturnType<typeof vi.fn>; log: ReturnType<typeof vi.fn> };
  let store: AutoLlmStore;
  beforeEach(() => {
    vi.useFakeTimers();
    client = { screeningAction: vi.fn(), screeningReview: vi.fn(), screenings: vi.fn().mockResolvedValue({ active: '', jobs: [] }), summary: vi.fn().mockResolvedValue({}), activity: vi.fn(), defaults: vi.fn().mockResolvedValue(config), list: vi.fn().mockResolvedValue({ active: campaign.id, campaigns: [campaign] }), progress: vi.fn().mockResolvedValue({ status: 'GENERATION' }), comparison: vi.fn().mockResolvedValue({ status: 'NOT_STARTED' }), validate: vi.fn().mockResolvedValue({ valid: true, errors: [] }), create: vi.fn().mockResolvedValue(campaign), baselineCheck: vi.fn().mockResolvedValue({ parameters: 10, maxParameters: 299, withinLimit: true }), action: vi.fn().mockResolvedValue(campaign), log: vi.fn().mockResolvedValue({ cursor: 0, nextCursor: 0, text: '', more: false, reset: false }) };
    TestBed.configureTestingModule({ providers: [AutoLlmStore, { provide: AutoLlmClient, useValue: client }] });
    store = TestBed.inject(AutoLlmStore);
  });
  afterEach(() => { TestBed.resetTestingModule(); vi.useRealTimers(); });
  it('restores an active campaign after a temporary disconnection and preserves trial selection', async () => {
    client.list.mockRejectedValueOnce(new Error('offline'));
    await store.start(); expect(store.connectionError()).toBe('offline');
    await vi.advanceTimersByTimeAsync(2000);
    expect(store.active()).toBe(campaign.id); expect(store.connectionError()).toBeNull();
    store.selectTrial(2); await vi.advanceTimersByTimeAsync(2000);
    expect(store.selectedTrial()).toBe(2); expect(client.progress).toHaveBeenLastCalledWith(campaign.id, 2);
    const requests = client.list.mock.calls.length; TestBed.resetTestingModule(); await vi.advanceTimersByTimeAsync(10000); expect(client.list).toHaveBeenCalledTimes(requests);
  });
  it('keeps healthy progress while another active trial is unavailable', async () => {
    const parallel = { ...campaign, trials: [{ number: 1, status: 'GENERATION' }, { number: 2, status: 'TUNING' }] } as AutoLlmCampaign;
    client.list.mockResolvedValue({ active: campaign.id, campaigns: [parallel] });
    client.progress.mockImplementation((_id: string, trial: number) => trial === 2 ? Promise.reject(new Error('unavailable')) : Promise.resolve({ status: 'GENERATION' }));
    await store.start();
    expect(store.activeProgress()[1]).toEqual({ progress: { status: 'GENERATION' }, stale: false });
    expect(store.activeProgress()[2].stale).toBe(true);
    expect(store.connectionError()).toBeNull();
  });
  it('validates before creating and never starts an invalid configuration', async () => {
    store.config.set(config); client.validate.mockResolvedValue({ valid: false, errors: ['Missing SOTA'] });
    await store.createAndStart();
    expect(store.error()).toBe('Missing SOTA'); expect(client.create).not.toHaveBeenCalled(); expect(client.action).not.toHaveBeenCalled(); expect(store.busy()).toBe(false);
  });
  it('creates, starts, cancels, and resumes through explicit lifecycle actions', async () => {
    store.config.set(config); await store.createAndStart();
    expect(client.action).toHaveBeenLastCalledWith(campaign.id, 'start');
    await store.action('cancel'); expect(client.action).toHaveBeenLastCalledWith(campaign.id, 'cancel');
    await store.action('resume'); expect(client.action).toHaveBeenLastCalledWith(campaign.id, 'resume');
  });
  it('keeps an over-budget baseline as a draft and does not start it', async () => {
    store.config.set(config); client.baselineCheck.mockResolvedValue({ parameters: 350, maxParameters: 299, withinLimit: false });
    await store.createAndStart();
    expect(store.selectedId()).toBe(campaign.id); expect(store.error()).toContain('350 parameters'); expect(client.action).not.toHaveBeenCalled();
  });
  it('keeps the preparation busy while a cancellation request completes', async () => {
    store.config.set(config);
    let finish!: (value: { parameters: number; maxParameters: number; withinLimit: boolean }) => void;
    client.baselineCheck.mockImplementationOnce(() => new Promise(resolve => { finish = resolve; }));
    const preparing = store.createAndStart(); await vi.waitFor(() => expect(client.baselineCheck).toHaveBeenCalled());
    await store.action('cancel'); expect(store.busy()).toBe(true);
    finish({ parameters: 350, maxParameters: 299, withinLimit: false }); await preparing; expect(store.busy()).toBe(false);
  });
  it('discards a log response when the selected log changes', async () => {
    await store.start(); store.selectLog('old.log');
    let resolveLog!: (log: AutoLlmLog) => void;
    client.log.mockImplementationOnce(() => new Promise<AutoLlmLog>(resolve => { resolveLog = resolve; }));
    const polling = vi.advanceTimersByTimeAsync(2000);
    await vi.waitFor(() => expect(client.log).toHaveBeenCalled());
    store.selectLog('new.log'); resolveLog({ cursor: 0, nextCursor: 12, text: 'old response', more: false, reset: false }); await polling;
    expect(store.logText()).toBe('');
  });
  it('assembles split activity events and resumes from their byte cursor', async () => {
    const running = { ...campaign, trials: [{ ...campaign.trials[0], attempts: [{ number: 1, stage: 'GENERATION', status: 'RUNNING' }] }] } as AutoLlmCampaign;
    client.list.mockResolvedValue({ active: running.id, campaigns: [running] });
    client.activity.mockResolvedValueOnce({ text: '{"timestamp":"now","type":"item.completed","text":"hel', nextCursor: 53, more: false, reset: false })
      .mockResolvedValue({ text: 'lo"}\n', nextCursor: 58, more: false, reset: false });
    await store.start(); expect(store.activity()).toEqual([]);
    await vi.advanceTimersByTimeAsync(2000);
    expect(client.activity).toHaveBeenLastCalledWith(campaign.id, 1, 1, 53);
    expect(store.activity()).toEqual([{ timestamp: 'now', type: 'item.completed', text: 'hello' }]);
  });
  it('never approves a review belonging to a different screening', async () => {
    store.selectedScreening.set('second'); store.review.set({ jobId: 'first', proposalSha256: 'reviewed' } as ScreeningReview);
    await store.screeningAction('approve'); expect(client.screeningAction).not.toHaveBeenCalled();
    store.review.set({ jobId: 'second', proposalSha256: 'reviewed' } as ScreeningReview);
    await store.screeningAction('approve'); expect(client.screeningAction).toHaveBeenCalledWith('second', 'approve', 'reviewed');
  });
  it('does not replace the selected review with a late response', async () => {
    store.selectScreening('first'); let resolve!: (value: ScreeningReview) => void;
    client.screeningReview.mockImplementation(() => new Promise<ScreeningReview>(done => { resolve = done; }));
    const pending = store.reviewScreening(); store.selectScreening('second');
    resolve({ proposalSha256: 'first-review' } as ScreeningReview); await pending; expect(store.review()).toBeNull();
  });
});
