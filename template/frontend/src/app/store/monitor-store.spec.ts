import { TestBed } from '@angular/core/testing';
import { ExperimentClient } from '../service/experiment-client';
import { MonitorStore } from './monitor-store';

describe('core monitor mode selection', () => {
  let getMode: ReturnType<typeof vi.fn>, store: MonitorStore;
  beforeEach(() => {
    vi.useFakeTimers();
    getMode = vi.fn().mockResolvedValue({ mode: 'STANDARD', eventLogId: 'log' });
    TestBed.configureTestingModule({
      providers: [MonitorStore, { provide: ExperimentClient, useValue: { getMode } }],
    });
    store = TestBed.inject(MonitorStore);
  });
  afterEach(() => {
    TestBed.resetTestingModule();
    vi.useRealTimers();
  });
  it('selects standard mode without calling autoconfig', async () => {
    store.start();
    await vi.advanceTimersByTimeAsync(0);
    expect(store.viewMode()).toBe('standard');
    store.start();
    expect(getMode).toHaveBeenCalledTimes(1);
  });
  it('selects the AutoLLM campaign dashboard', async () => {
    getMode.mockResolvedValue({ mode: 'AUTOLLM' });
    store.start();
    await vi.advanceTimersByTimeAsync(0);
    expect(store.viewMode()).toBe('autollm');
  });
  it('recovers from unavailable mode detection with bounded retry', async () => {
    getMode.mockRejectedValueOnce(new Error('offline'));
    store.start();
    await vi.advanceTimersByTimeAsync(0);
    expect(store.viewMode()).toBe('unavailable');
    await vi.advanceTimersByTimeAsync(500);
    expect(store.viewMode()).toBe('standard');
    expect(store.latestError()).toBeNull();
  });
  it('detects utility and worker roles and stops polling on destroy', async () => {
    getMode.mockResolvedValueOnce({ mode: 'UTILITY' });
    store.start();
    await vi.advanceTimersByTimeAsync(0);
    expect(store.viewMode()).toBe('utility');
    getMode.mockResolvedValueOnce({ mode: 'WORKER' });
    store.retryNow();
    await vi.advanceTimersByTimeAsync(0);
    expect(store.viewMode()).toBe('worker');
    TestBed.resetTestingModule();
    await vi.advanceTimersByTimeAsync(60000);
    expect(getMode).toHaveBeenCalledTimes(2);
  });
});
