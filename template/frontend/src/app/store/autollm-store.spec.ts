import { TestBed } from '@angular/core/testing';
import { AutoLlmClient } from '../service/autollm-client';
import { AutoLlmCampaign, AutoLlmConfig, AutoLlmLog } from '../model/autollm';
import { AutoLlmStore } from './autollm-store';

describe('AutoLLM campaign lifecycle store', () => {
  const config = {} as AutoLlmConfig;
  const campaign = { id: 'campaign', status: 'RUNNING', config, trials: [{ number: 1, status: 'GENERATION' }, { number: 2, status: 'PENDING' }] } as AutoLlmCampaign;
  let client: { defaults: ReturnType<typeof vi.fn>; list: ReturnType<typeof vi.fn>; progress: ReturnType<typeof vi.fn>; comparison: ReturnType<typeof vi.fn>; validate: ReturnType<typeof vi.fn>; create: ReturnType<typeof vi.fn>; action: ReturnType<typeof vi.fn>; log: ReturnType<typeof vi.fn> };
  let store: AutoLlmStore;
  beforeEach(() => {
    vi.useFakeTimers();
    client = { defaults: vi.fn().mockResolvedValue(config), list: vi.fn().mockResolvedValue({ active: campaign.id, campaigns: [campaign] }), progress: vi.fn().mockResolvedValue({ status: 'GENERATION' }), comparison: vi.fn().mockResolvedValue({ status: 'NOT_STARTED' }), validate: vi.fn().mockResolvedValue({ valid: true, errors: [] }), create: vi.fn().mockResolvedValue(campaign), action: vi.fn().mockResolvedValue(campaign), log: vi.fn().mockResolvedValue({ nextOffset: 0, lines: [] }) };
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
  it('discards a log response when the selected log changes', async () => {
    await store.start(); store.selectLog('old.log');
    let resolveLog!: (log: AutoLlmLog) => void;
    client.log.mockImplementationOnce(() => new Promise<AutoLlmLog>(resolve => { resolveLog = resolve; }));
    const polling = vi.advanceTimersByTimeAsync(2000);
    await vi.waitFor(() => expect(client.log).toHaveBeenCalled());
    store.selectLog('new.log'); resolveLog({ offset: 0, nextOffset: 1, lines: ['old response'] }); await polling;
    expect(store.logLines()).toEqual([]);
  });
});
