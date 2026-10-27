import { TestBed } from '@angular/core/testing';
import { AutoLlmClient } from '../service/autollm-client';
import { AutoLlmCampaign, AutoLlmConfig, AutoLlmLogPreview, ScreeningReview } from '../model/autollm';
import { AutoLlmStore } from './autollm-store';

describe('AutoLLM campaign lifecycle store', () => {
  const config = {} as AutoLlmConfig;
  const campaign = { id: 'campaign', status: 'RUNNING', config, trials: [{ number: 1, status: 'GENERATION' }, { number: 2, status: 'PENDING' }] } as AutoLlmCampaign;
  let client: { probeTarget: ReturnType<typeof vi.fn>; targetProbe: ReturnType<typeof vi.fn>; recovery: ReturnType<typeof vi.fn>; resumeTrial: ReturnType<typeof vi.fn>; failTrial: ReturnType<typeof vi.fn>; targetAction: ReturnType<typeof vi.fn>; screeningAction: ReturnType<typeof vi.fn>; screeningReview: ReturnType<typeof vi.fn>; screenings: ReturnType<typeof vi.fn>; summary: ReturnType<typeof vi.fn>; activity: ReturnType<typeof vi.fn>; defaults: ReturnType<typeof vi.fn>; list: ReturnType<typeof vi.fn>; progress: ReturnType<typeof vi.fn>; comparison: ReturnType<typeof vi.fn>; validate: ReturnType<typeof vi.fn>; create: ReturnType<typeof vi.fn>; baselineCheck: ReturnType<typeof vi.fn>; action: ReturnType<typeof vi.fn>; preview: ReturnType<typeof vi.fn> };
  let store: AutoLlmStore;
  beforeEach(() => {
    vi.useFakeTimers();
    client = { probeTarget: vi.fn().mockResolvedValue({ id: 'probe', status: 'RUNNING', targets: {} }), targetProbe: vi.fn().mockResolvedValue({ id: 'probe', status: 'SUCCEEDED', targets: { local: { status: 'SUCCEEDED' } } }), recovery: vi.fn().mockResolvedValue({ trials: {}, targets: {}, resumeCampaign: { allowed: false, reason: 'Already running', effect: '' } }), resumeTrial: vi.fn().mockResolvedValue(campaign), failTrial: vi.fn().mockResolvedValue(campaign), targetAction: vi.fn().mockResolvedValue(campaign), screeningAction: vi.fn(), screeningReview: vi.fn(), screenings: vi.fn().mockResolvedValue({ active: '', jobs: [] }), summary: vi.fn().mockResolvedValue({}), activity: vi.fn(), defaults: vi.fn().mockResolvedValue(config), list: vi.fn().mockResolvedValue({ active: campaign.id, campaigns: [campaign] }), progress: vi.fn().mockResolvedValue({ status: 'GENERATION' }), comparison: vi.fn().mockResolvedValue({ status: 'NOT_STARTED' }), validate: vi.fn().mockResolvedValue({ valid: true, errors: [] }), create: vi.fn().mockResolvedValue(campaign), baselineCheck: vi.fn().mockResolvedValue({ parameters: 10, maxParameters: 299, withinLimit: true, ready: true, qualifiedTargets: ['a'], failedTargets: [] }), action: vi.fn().mockResolvedValue(campaign), preview: vi.fn().mockResolvedValue({ text: '', lineCount: 0, sizeBytes: 0, truncated: false, byteLimited: false }) };
    TestBed.configureTestingModule({ providers: [AutoLlmStore, { provide: AutoLlmClient, useValue: client }] });
    store = TestBed.inject(AutoLlmStore);
  });
  afterEach(() => { TestBed.resetTestingModule(); vi.useRealTimers(); });
  it('shows the saved preflight id immediately and polls its results independently', async () => {
    store.config.set(config); await store.probeTarget();
    expect(store.targetStatus()?.['id']).toBe('probe'); expect(store.targetStatus()?.['status']).toBe('RUNNING');
    await store.start();
    expect(client.probeTarget).toHaveBeenCalledTimes(1); expect(client.targetProbe).toHaveBeenCalledWith('probe');
    expect(store.targetStatus()?.['status']).toBe('SUCCEEDED'); expect(store.activeProgress()[1].stale).toBe(false);
  });
  it('retains the saved probe and healthy campaign progress after a failed preflight read', async () => {
    store.config.set(config); await store.probeTarget(); client.targetProbe.mockRejectedValue(new Error('offline'));
    await store.start();
    expect(store.targetStatus()?.['id']).toBe('probe'); expect(store.targetStatus()?.['status']).toBe('RUNNING');
    expect(store.activeProgress()[1].stale).toBe(false); expect(store.targetStatus()?.['readError']).toBe('offline');
    await vi.advanceTimersByTimeAsync(2000); expect(client.probeTarget).toHaveBeenCalledTimes(1);
  });
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
    await store.action('cancel'); expect(client.action).toHaveBeenLastCalledWith(campaign.id, 'cancel', []);
    await store.action('resume'); expect(client.action).toHaveBeenLastCalledWith(campaign.id, 'resume', []);
  });
  it('does not start when a target failed qualification even if parameter limits passed', async () => {
    store.config.set(config);
    client.baselineCheck.mockResolvedValue({ parameters: 10, maxParameters: 299, withinLimit: true, ready: false, qualifiedTargets: ['a'], failedTargets: ['b'] });
    await store.createAndStart();
    expect(client.action).not.toHaveBeenCalled(); expect(store.error()).toContain('Failed targets: b');
    await store.action('start', ['a']); expect(client.action).toHaveBeenLastCalledWith(campaign.id, 'start', ['a']);
  });
  it('keeps campaign progress when screening status is unavailable', async () => {
    client.screenings.mockRejectedValue(new Error('screening unavailable'));
    await store.start();
    expect(store.active()).toBe(campaign.id); expect(store.activeProgress()[1].stale).toBe(false);
    expect(store.connectionError()).toBe('screening unavailable');
  });
  it('keeps an over-budget baseline as a draft and does not start it', async () => {
    store.config.set(config); client.baselineCheck.mockResolvedValue({ parameters: 350, maxParameters: 299, withinLimit: false, ready: false, qualifiedTargets: [], failedTargets: ['a'] });
    await store.createAndStart();
    expect(store.selectedId()).toBe(campaign.id); expect(store.error()).toContain('350 parameters'); expect(client.action).not.toHaveBeenCalled();
  });
  it('keeps the preparation busy while a cancellation request completes', async () => {
    store.config.set(config);
    let finish!: (value: Awaited<ReturnType<AutoLlmClient['baselineCheck']>>) => void;
    client.baselineCheck.mockImplementationOnce(() => new Promise(resolve => { finish = resolve; }));
    const preparing = store.createAndStart(); await vi.waitFor(() => expect(client.baselineCheck).toHaveBeenCalled());
    await store.action('cancel'); expect(store.busy()).toBe(true);
    finish({ parameters: 350, maxParameters: 299, withinLimit: false, ready: false, qualifiedTargets: [], failedTargets: ['a'] }); await preparing; expect(store.busy()).toBe(false);
  });
  it('loads the newest 500 lines immediately and replaces the preview on each poll', async () => {
    await store.start();
    client.preview.mockResolvedValueOnce({ text: 'recent\n', lineCount: 1, sizeBytes: 1000000, truncated: true, byteLimited: false });
    store.selectLog('large.log');
    await vi.waitFor(() => expect(store.logPreview()?.text).toBe('recent\n'));
    expect(client.preview).toHaveBeenLastCalledWith(campaign.id, 'large.log', 500);
    client.preview.mockResolvedValue({ text: 'newest\n', lineCount: 1, sizeBytes: 1000010, truncated: true, byteLimited: false });
    await vi.advanceTimersByTimeAsync(2000);
    expect(store.logPreview()?.text).toBe('newest\n');
    const unchanged = store.logPreview();
    await vi.advanceTimersByTimeAsync(2000);
    expect(store.logPreview()).toBe(unchanged);
  });
  it('discards an old response even when the same file is selected again', async () => {
    await store.start();
    let resolveLog!: (log: AutoLlmLogPreview) => void;
    client.preview.mockImplementationOnce(() => new Promise<AutoLlmLogPreview>(resolve => { resolveLog = resolve; }));
    store.selectLog('same.log'); store.selectLog('other.log'); store.selectLog('same.log');
    await vi.waitFor(() => expect(store.logLoading()).toBe(false));
    resolveLog({ text: 'old response', lineCount: 1, sizeBytes: 12, truncated: false, byteLimited: false });
    await Promise.resolve();
    expect(store.logPreview()?.text).toBe('');
  });
  it('refreshes with the chosen line limit and stops polling a paused or closed preview', async () => {
    await store.start(); store.selectLog('large.log');
    await vi.waitFor(() => expect(store.logLoading()).toBe(false));
    store.setLogLines(100);
    await vi.waitFor(() => expect(store.logLoading()).toBe(false));
    expect(client.preview).toHaveBeenLastCalledWith(campaign.id, 'large.log', 100);
    store.logFollow.set(false); client.preview.mockClear();
    await vi.advanceTimersByTimeAsync(2000); expect(client.preview).not.toHaveBeenCalled();
    await store.refreshLog(); expect(client.preview).toHaveBeenCalledTimes(1);
    store.selectLog(''); store.logFollow.set(true);
    await vi.advanceTimersByTimeAsync(2000); expect(client.preview).toHaveBeenCalledTimes(1);
    expect(store.logPreview()).toBeNull();
  });
  it('shows preview errors separately and keeps campaign polling healthy', async () => {
    await store.start();
    client.preview.mockRejectedValue(new Error('Preview unavailable'));
    store.selectLog('missing.log');
    await vi.waitFor(() => expect(store.logError()).toBe('Preview unavailable'));
    expect(store.logLoading()).toBe(false);
    await vi.advanceTimersByTimeAsync(2000);
    expect(store.connectionError()).toBeNull();
    client.preview.mockResolvedValue({ text: 'recovered', lineCount: 1, sizeBytes: 9, truncated: false, byteLimited: false });
    await store.refreshLog(); expect(store.logError()).toBeNull(); expect(store.logPreview()?.text).toBe('recovered');
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
  it('recovers only the selected trial, records its failure reason, and refreshes availability', async () => {
    await store.start(); store.selectTrial(2);
    await store.resumeTrial(); expect(client.resumeTrial).toHaveBeenCalledWith(campaign.id, 2);
    await store.failTrial('   '); expect(client.failTrial).not.toHaveBeenCalled();
    await store.failTrial(' Sampling exhausted ');
    expect(client.failTrial).toHaveBeenCalledWith(campaign.id, 2, 'Sampling exhausted');
    await store.targetAction('server-a', 'requalify');
    expect(client.targetAction).toHaveBeenCalledWith(campaign.id, 'server-a', 'requalify');
    expect(client.recovery).toHaveBeenCalledTimes(4);
  });
  it('isolates recovery endpoint failures and discards late availability from another campaign', async () => {
    client.recovery.mockRejectedValueOnce(new Error('recovery offline'));
    await store.start(); expect(store.recoveryError()).toBe('recovery offline'); expect(store.connectionError()).toBeNull();
    let resolve!: (value: unknown) => void;
    client.recovery.mockImplementationOnce(() => new Promise(done => { resolve = done; }));
    const pending = store.refreshRecovery(); store.select('another'); resolve({ trials: {}, targets: {} }); await pending;
    expect(store.recovery()).toBeNull(); expect(store.recoveryError()).toBeNull();
  });
  it('does not replace the selected review with a late response', async () => {
    store.selectScreening('first'); let resolve!: (value: ScreeningReview) => void;
    client.screeningReview.mockImplementation(() => new Promise<ScreeningReview>(done => { resolve = done; }));
    const pending = store.reviewScreening(); store.selectScreening('second');
    resolve({ proposalSha256: 'first-review' } as ScreeningReview); await pending; expect(store.review()).toBeNull();
  });
});
