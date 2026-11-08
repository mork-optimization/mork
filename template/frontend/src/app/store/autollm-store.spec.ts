import { TestBed } from '@angular/core/testing';
import { AutoLlmClient } from '../service/autollm-client';
import {
  AutoLlmCampaign,
  AutoLlmConfig,
  AutoLlmLogPreview,
  ScreeningReview,
} from '../model/autollm';
import { AutoLlmStore } from './autollm-store';

describe('AutoLLM campaign lifecycle store', () => {
  const config = {} as AutoLlmConfig;
  const campaign = {
    id: 'campaign',
    status: 'RUNNING',
    config,
    trials: [
      { number: 1, status: 'GENERATION' },
      { number: 2, status: 'PENDING' },
    ],
  } as AutoLlmCampaign;
  let client: {
    datasets: ReturnType<typeof vi.fn>;
    dataset: ReturnType<typeof vi.fn>;
    previewDataset: ReturnType<typeof vi.fn>;
    publishDataset: ReturnType<typeof vi.fn>;
    reportStatus: ReturnType<typeof vi.fn>;
    generateReport: ReturnType<typeof vi.fn>;
    objectives: ReturnType<typeof vi.fn>;
    detail: ReturnType<typeof vi.fn>;
    status: ReturnType<typeof vi.fn>;
    campaignComparison: ReturnType<typeof vi.fn>;
    artifacts: ReturnType<typeof vi.fn>;
    addTarget: ReturnType<typeof vi.fn>;
    cleanupPlan: ReturnType<typeof vi.fn>;
    cleanupApply: ReturnType<typeof vi.fn>;
    proposal: ReturnType<typeof vi.fn>;
    approve: ReturnType<typeof vi.fn>;
    curves: ReturnType<typeof vi.fn>;
    probeTarget: ReturnType<typeof vi.fn>;
    targetProbe: ReturnType<typeof vi.fn>;
    recovery: ReturnType<typeof vi.fn>;
    resumeTrial: ReturnType<typeof vi.fn>;
    failTrial: ReturnType<typeof vi.fn>;
    targetAction: ReturnType<typeof vi.fn>;
    screeningAction: ReturnType<typeof vi.fn>;
    screeningReview: ReturnType<typeof vi.fn>;
    screenings: ReturnType<typeof vi.fn>;
    summary: ReturnType<typeof vi.fn>;
    activity: ReturnType<typeof vi.fn>;
    defaults: ReturnType<typeof vi.fn>;
    list: ReturnType<typeof vi.fn>;
    progress: ReturnType<typeof vi.fn>;
    comparison: ReturnType<typeof vi.fn>;
    validate: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    baselineCheck: ReturnType<typeof vi.fn>;
    action: ReturnType<typeof vi.fn>;
    preview: ReturnType<typeof vi.fn>;
  };
  let store: AutoLlmStore;
  it('publishes only after review and passes current paths with the reviewed digest', async () => {
    const draft = {
      project: { repository: '/repo', artifact: 'target/app.jar' },
      trainingPath: 'train.index',
      comparisonPath: 'test.index',
    } as AutoLlmConfig;
    store.config.set(draft);
    store.datasetName.set('Reusable');
    await store.publishDataset();
    expect(client.publishDataset).not.toHaveBeenCalled();
    client.previewDataset.mockResolvedValue({
      name: 'Reusable',
      digest: 'reviewed',
      training: ['a'],
      comparison: ['b'],
    });
    await store.previewDataset();
    const changed = { ...draft, trainingPath: 'changed.index' };
    store.config.set(changed);
    client.publishDataset.mockRejectedValue(new Error('Dataset selection changed'));
    await store.publishDataset();
    expect(client.publishDataset).toHaveBeenCalledWith(changed, 'Reusable', 'reviewed');
    expect(store.error()).toContain('Dataset selection changed');
    expect(client.approve).not.toHaveBeenCalled();
    expect(client.create).not.toHaveBeenCalled();
  });
  it('verifies a saved dataset before filling the configuration', async () => {
    store.config.set({ trainingPath: 'old', comparisonPath: 'old' } as AutoLlmConfig);
    client.dataset.mockResolvedValue({
      trainingPath: '/library/train.index',
      comparisonPath: '/library/test.index',
    });
    await store.useDataset('saved');
    expect(store.config()?.trainingPath).toBe('/library/train.index');
    expect(store.config()?.comparisonPath).toBe('/library/test.index');
    expect(client.create).not.toHaveBeenCalled();
  });
  beforeEach(() => {
    vi.useFakeTimers();
    client = {
      datasets: vi.fn().mockResolvedValue([]),
      dataset: vi.fn(),
      previewDataset: vi.fn(),
      publishDataset: vi.fn(),
      reportStatus: vi
        .fn()
        .mockResolvedValue({
          state: 'NOT_STARTED',
          selection: { objective: 'Cost', mode: 'MINIMIZE' },
          objectives: { Cost: 'MINIMIZE' },
          path: null,
        }),
      generateReport: vi.fn(),
      objectives: vi.fn().mockResolvedValue({ Cost: 'MINIMIZE', Quality: 'MAXIMIZE' }),
      detail: vi.fn().mockResolvedValue(campaign),
      status: vi.fn().mockResolvedValue(campaign),
      campaignComparison: vi.fn().mockResolvedValue({ trials: [], excluded: {} }),
      artifacts: vi.fn().mockResolvedValue([]),
      addTarget: vi.fn().mockResolvedValue({
        id: campaign.id,
        status: 'RUNNING',
        operation: 'addTarget',
        accepted: true,
      }),
      cleanupPlan: vi.fn(),
      cleanupApply: vi.fn(),
      proposal: vi.fn().mockResolvedValue({
        digest: 'reviewed',
        generation: { agent: 'CODEX', ready: true },
        testing: [],
        training: [],
      }),
      approve: vi.fn().mockResolvedValue({}),
      curves: vi.fn().mockResolvedValue({ curves: [], sampled: true }),
      probeTarget: vi.fn().mockResolvedValue({ id: 'probe', status: 'RUNNING', targets: {} }),
      targetProbe: vi.fn().mockResolvedValue({
        id: 'probe',
        status: 'SUCCEEDED',
        targets: { local: { status: 'SUCCEEDED' } },
      }),
      recovery: vi.fn().mockResolvedValue({
        trials: {},
        targets: {},
        resumeCampaign: { allowed: false, reason: 'Already running', effect: '' },
      }),
      resumeTrial: vi.fn().mockResolvedValue({
        id: campaign.id,
        status: campaign.status,
        operation: 'resumeTrial',
        accepted: true,
      }),
      failTrial: vi.fn().mockResolvedValue({
        id: campaign.id,
        status: campaign.status,
        operation: 'failTrial',
        accepted: true,
      }),
      targetAction: vi.fn().mockResolvedValue({
        id: campaign.id,
        status: campaign.status,
        operation: 'targetAction',
        accepted: true,
      }),
      screeningAction: vi.fn(),
      screeningReview: vi.fn(),
      screenings: vi.fn().mockResolvedValue({ active: '', jobs: [] }),
      summary: vi.fn().mockResolvedValue({}),
      activity: vi.fn(),
      defaults: vi.fn().mockResolvedValue(config),
      list: vi.fn().mockResolvedValue({ active: campaign.id, campaigns: [campaign] }),
      progress: vi.fn().mockResolvedValue({ status: 'GENERATION' }),
      comparison: vi.fn().mockResolvedValue({ status: 'NOT_STARTED' }),
      validate: vi.fn().mockResolvedValue({ valid: true, errors: [] }),
      create: vi.fn().mockResolvedValue(campaign),
      baselineCheck: vi.fn().mockResolvedValue({
        parameters: 10,
        maxParameters: 299,
        withinLimit: true,
        ready: true,
        qualifiedTargets: ['a'],
        failedTargets: [],
      }),
      action: vi.fn().mockResolvedValue({
        id: campaign.id,
        status: campaign.status,
        operation: 'action',
        accepted: true,
      }),
      preview: vi.fn().mockResolvedValue({
        text: '',
        lineCount: 0,
        sizeBytes: 0,
        truncated: false,
        byteLimited: false,
      }),
    };
    TestBed.configureTestingModule({
      providers: [AutoLlmStore, { provide: AutoLlmClient, useValue: client }],
    });
    store = TestBed.inject(AutoLlmStore);
  });
  it('loads saved report settings and ignores a late response after switching campaigns', async () => {
    store.select(campaign.id);
    await store.refreshReport();
    expect(store.reportObjective()).toBe('Cost');
    let resolve!: (value: unknown) => void;
    client.reportStatus.mockImplementationOnce(
      () =>
        new Promise((r) => {
          resolve = r;
        }),
    );
    const pending = store.refreshReport();
    store.select('another');
    resolve({ state: 'SUCCEEDED', selection: { objective: 'Old' }, path: 'reports/report.html' });
    await pending;
    expect(store.report()).toBeNull();
    expect(store.reportObjective()).toBe('');
  });
  it('submits one report request and retains its receipt while polling', async () => {
    store.select(campaign.id);
    store.selectReportObjective('Cost');
    client.generateReport.mockResolvedValue({
      requestId: 'request',
      state: 'QUEUED',
      selection: { objective: 'Cost', mode: 'MINIMIZE' },
      objectives: {},
      path: null,
    });
    vi.spyOn(store, 'retryNow').mockImplementation(() => {});
    await store.generateReport();
    await store.generateReport();
    expect(client.generateReport).toHaveBeenCalledTimes(1);
    expect(client.generateReport).toHaveBeenCalledWith(campaign.id, {
      objective: 'Cost',
      mode: 'MINIMIZE',
    });
    client.reportStatus.mockResolvedValue({
      requestId: 'request',
      state: 'SUCCEEDED',
      selection: { objective: 'Cost', mode: 'MINIMIZE' },
      objectives: {},
      path: 'reports/report.html',
    });
    await store.refreshReport();
    expect(client.reportStatus).toHaveBeenLastCalledWith(campaign.id, 'request');
    expect(store.report()?.path).toBe('reports/report.html');
  });

  it('does not approve a proposal with an unselected generation model', async () => {
    store.proposal.set({
      digest: 'reviewed',
      generation: { agent: 'CODEX', ready: false, approvalLine: 'Set generation.model before approval' },
    } as never);
    await store.approveAndStart();
    expect(client.approve).not.toHaveBeenCalled();
    expect(client.action).not.toHaveBeenCalled();
    expect(store.error()).toContain('Set generation.model');
  });
  afterEach(() => {
    TestBed.resetTestingModule();
    vi.useRealTimers();
  });
  const instance = (id: string, repetitions = 1) => ({
    instanceId: id,
    instance: `/remote/${id}`,
    counts: { SOTA: { SUCCEEDED: repetitions }, IraceProposal: { SUCCEEDED: repetitions } },
  });
  const curves = (id: string) =>
    ['SOTA', 'IraceProposal'].map((algorithm) => ({
      instance: `/remote/${id}`,
      instanceId: id,
      algorithm,
      iteration: '0',
      curves: { Cost: [{ instant: 0, value: 10 }] },
    }));
  it('applies only the reviewed cleanup digest and preserves pending receipts', async () => {
    store.selectedId.set(campaign.id);
    client.cleanupPlan.mockResolvedValue({
      digest: 'reviewed',
      selection: { kind: 'campaign', id: campaign.id },
    });
    client.cleanupApply.mockResolvedValue({
      digest: 'reviewed',
      status: 'PENDING',
      completed: [],
      pending: { worker: 'offline' },
    });
    await store.reviewCleanup();
    await store.applyCleanup();
    expect(client.cleanupApply).toHaveBeenCalledExactlyOnceWith('reviewed');
    expect(store.cleanupReceipt()?.status).toBe('PENDING');
    store.selectedId.set('different');
    await store.applyCleanup();
    expect(client.cleanupApply).toHaveBeenCalledTimes(1);
  });
  it('keeps a newly selected campaign when an earlier cleanup completes', async () => {
    store.selectedId.set(campaign.id);
    store.campaigns.set([campaign, { ...campaign, id: 'different' }]);
    store.cleanupPlan.set({
      digest: 'reviewed',
      selection: { kind: 'campaign', id: campaign.id },
    } as never);
    let resolve!: (value: unknown) => void;
    client.cleanupApply.mockImplementation(
      () =>
        new Promise((done) => {
          resolve = done;
        }),
    );
    const pending = store.applyCleanup();
    store.selectedId.set('different');
    resolve({ digest: 'reviewed', status: 'SUCCEEDED', completed: [], pending: {} });
    await pending;
    expect(store.selectedId()).toBe('different');
    expect(store.campaigns().map((value) => value.id)).toEqual(['different']);
  });
  it('shows approval field differences without starting a changed proposal', async () => {
    store.selectedId.set(campaign.id);
    store.proposal.set({ digest: 'reviewed', generation: { agent: 'CODEX', ready: true } } as never);
    const differences = [{ path: 'configuration.seed', reviewed: 1, current: 2 }];
    client.approve.mockRejectedValue({ error: { message: 'PROPOSAL_CHANGED', differences } });
    await store.approveAndStart();
    expect(store.approvalDifferences()).toEqual(differences);
    expect(client.action).not.toHaveBeenCalled();
  });
  it('loads both algorithms for the first instance and refreshes only when its counts change', async () => {
    client.comparison.mockResolvedValue({ instances: [instance('one'), instance('two')] });
    client.curves.mockResolvedValue({ curves: curves('one'), sampled: true });
    await store.start();
    expect(client.curves).toHaveBeenCalledExactlyOnceWith(campaign.id, 1, 'one');
    expect(store.curves()).toEqual(curves('one'));
    await vi.advanceTimersByTimeAsync(2000);
    expect(client.curves).toHaveBeenCalledTimes(1);
    client.comparison.mockResolvedValue({ instances: [instance('one'), instance('two', 2)] });
    await vi.advanceTimersByTimeAsync(2000);
    expect(client.curves).toHaveBeenCalledTimes(1);
    client.comparison.mockResolvedValue({ instances: [instance('one', 2), instance('two', 2)] });
    await vi.advanceTimersByTimeAsync(2000);
    expect(client.curves).toHaveBeenCalledTimes(2);
    expect(store.comparison()).not.toHaveProperty('curves');
  });
  it('discards late curves when switching instances and trials, including switching away and back', async () => {
    client.comparison.mockResolvedValue({ instances: [instance('one'), instance('two')] });
    await store.start();
    let resolve!: (value: unknown) => void;
    client.curves.mockImplementationOnce(
      () =>
        new Promise((done) => {
          resolve = done;
        }),
    );
    const pending = store.selectCurveInstance('two');
    expect(store.curvesLoading()).toBe(true);
    expect(store.curves()).toEqual([]);
    client.curves.mockResolvedValue({ curves: curves('one'), sampled: true });
    await store.selectCurveInstance('one');
    resolve({ curves: curves('two') });
    await pending;
    expect(store.curves()).toEqual(curves('one'));
    expect(store.curvesLoading()).toBe(false);
    client.curves.mockImplementationOnce(
      () =>
        new Promise((done) => {
          resolve = done;
        }),
    );
    const oldTrial = store.selectCurveInstance('two');
    store.selectTrial(2);
    store.selectTrial(1);
    resolve({ curves: curves('two') });
    await oldTrial;
    expect(store.curves()).toEqual([]);
    expect(store.curveInstance()).toBe('');
    expect(store.curvesLoading()).toBe(false);
  });
  it('retries failed curve reads without losing summary results', async () => {
    client.comparison.mockResolvedValue({ instances: [instance('one')] });
    client.curves
      .mockRejectedValueOnce(new Error('offline'))
      .mockResolvedValue({ curves: curves('one'), sampled: true });
    await store.start();
    expect(store.curvesError()).toBe('offline');
    expect(store.comparisonInstances()).toHaveLength(1);
    await vi.advanceTimersByTimeAsync(2000);
    expect(store.curvesError()).toBeNull();
    expect(store.curves()).toEqual(curves('one'));
    client.comparison.mockResolvedValue({ instances: [] });
    await vi.advanceTimersByTimeAsync(2000);
    expect(store.curves()).toEqual([]);
    expect(store.curveInstance()).toBe('');
  });
  it('shows the saved preflight id immediately and polls its results independently', async () => {
    await store.start();
    store.config.set(config);
    await store.probeTarget();
    expect(store.targetStatus()?.['id']).toBe('probe');
    expect(store.targetStatus()?.['status']).toBe('RUNNING');
    await vi.advanceTimersByTimeAsync(2000);
    expect(client.probeTarget).toHaveBeenCalledTimes(1);
    expect(client.targetProbe).toHaveBeenCalledWith('probe');
    expect(store.targetStatus()?.['status']).toBe('SUCCEEDED');
    expect(store.activeProgress()[1].stale).toBe(false);
  });
  it('retains the saved probe and healthy campaign progress after a failed preflight read', async () => {
    await store.start();
    store.config.set(config);
    await store.probeTarget();
    client.targetProbe.mockRejectedValue(new Error('offline'));
    await vi.advanceTimersByTimeAsync(2000);
    expect(store.targetStatus()?.['id']).toBe('probe');
    expect(store.targetStatus()?.['status']).toBe('RUNNING');
    expect(store.activeProgress()[1].stale).toBe(false);
    expect(store.targetStatus()?.['readError']).toBe('offline');
    await vi.advanceTimersByTimeAsync(2000);
    expect(client.probeTarget).toHaveBeenCalledTimes(1);
  });
  it('restores an active campaign after a temporary disconnection and preserves trial selection', async () => {
    client.list.mockRejectedValueOnce(new Error('offline'));
    await store.start();
    expect(store.connectionError()).toBe('offline');
    await vi.advanceTimersByTimeAsync(2000);
    expect(store.active()).toBe(campaign.id);
    expect(store.connectionError()).toBeNull();
    store.selectTrial(2);
    await vi.advanceTimersByTimeAsync(2000);
    expect(store.selectedTrial()).toBe(2);
    expect(client.progress).toHaveBeenLastCalledWith(campaign.id, 2);
    const requests = client.list.mock.calls.length;
    TestBed.resetTestingModule();
    await vi.advanceTimersByTimeAsync(10000);
    expect(client.list).toHaveBeenCalledTimes(requests);
  });
  it('keeps healthy progress while another active trial is unavailable', async () => {
    const parallel = {
      ...campaign,
      trials: [
        { number: 1, status: 'GENERATION' },
        { number: 2, status: 'TUNING' },
      ],
    } as AutoLlmCampaign;
    client.list.mockResolvedValue({ active: campaign.id, campaigns: [parallel] });
    client.progress.mockImplementation((_id: string, trial: number) =>
      trial === 2
        ? Promise.reject(new Error('unavailable'))
        : Promise.resolve({ status: 'GENERATION' }),
    );
    await store.start();
    expect(store.activeProgress()[1]).toEqual({ progress: { status: 'GENERATION' }, stale: false });
    expect(store.activeProgress()[2].stale).toBe(true);
    expect(store.connectionError()).toBeNull();
  });
  it('validates before creating and never starts an invalid configuration', async () => {
    store.config.set(config);
    client.validate.mockResolvedValue({ valid: false, errors: ['Missing SOTA'] });
    await store.prepareCampaign();
    expect(store.error()).toBe('Missing SOTA');
    expect(client.create).not.toHaveBeenCalled();
    expect(client.action).not.toHaveBeenCalled();
    expect(store.busy()).toBe(false);
  });
  it('creates, starts, cancels, and resumes through explicit lifecycle actions', async () => {
    store.config.set(config);
    await store.prepareCampaign();
    expect(client.action).not.toHaveBeenCalled();
    await store.approveAndStart();
    expect(client.approve).toHaveBeenCalledWith(campaign.id, 'reviewed');
    expect(client.action).toHaveBeenLastCalledWith(campaign.id, 'start');
    await store.action('cancel');
    expect(client.action).toHaveBeenLastCalledWith(campaign.id, 'cancel', []);
    await store.action('resume');
    expect(client.action).toHaveBeenLastCalledWith(campaign.id, 'resume', []);
  });
  it('does not start when a target failed qualification even if parameter limits passed', async () => {
    store.config.set(config);
    client.baselineCheck.mockResolvedValue({
      parameters: 10,
      maxParameters: 299,
      withinLimit: true,
      ready: false,
      qualifiedTargets: ['a'],
      failedTargets: ['b'],
    });
    await store.prepareCampaign();
    expect(client.action).not.toHaveBeenCalled();
    expect(store.error()).toContain('Failed targets: b');
    await store.action('start', ['a']);
    expect(client.action).toHaveBeenLastCalledWith(campaign.id, 'start', ['a']);
  });
  it('keeps campaign progress when screening status is unavailable', async () => {
    client.screenings.mockRejectedValue(new Error('screening unavailable'));
    await store.start();
    expect(store.active()).toBe(campaign.id);
    expect(store.activeProgress()[1].stale).toBe(false);
    expect(store.connectionError()).toBe('screening unavailable');
  });
  it('keeps an over-budget baseline as a draft and does not start it', async () => {
    store.config.set(config);
    client.baselineCheck.mockResolvedValue({
      parameters: 350,
      maxParameters: 299,
      withinLimit: false,
      ready: false,
      qualifiedTargets: [],
      failedTargets: ['a'],
    });
    await store.prepareCampaign();
    expect(store.selectedId()).toBe(campaign.id);
    expect(store.error()).toContain('350 parameters');
    expect(client.action).not.toHaveBeenCalled();
  });
  it('keeps the preparation busy while a cancellation request completes', async () => {
    store.config.set(config);
    let finish!: (value: Awaited<ReturnType<AutoLlmClient['baselineCheck']>>) => void;
    client.baselineCheck.mockImplementationOnce(
      () =>
        new Promise((resolve) => {
          finish = resolve;
        }),
    );
    const preparing = store.prepareCampaign();
    await vi.waitFor(() => expect(client.baselineCheck).toHaveBeenCalled());
    await store.action('cancel');
    expect(store.busy()).toBe(true);
    finish({
      parameters: 350,
      maxParameters: 299,
      withinLimit: false,
      ready: false,
      qualifiedTargets: [],
      failedTargets: ['a'],
    });
    await preparing;
    expect(store.busy()).toBe(false);
  });
  it('loads the newest 500 lines immediately and replaces the preview on each poll', async () => {
    await store.start();
    client.preview.mockResolvedValueOnce({
      text: 'recent\n',
      lineCount: 1,
      sizeBytes: 1000000,
      truncated: true,
      byteLimited: false,
    });
    store.selectLog('large.log');
    await vi.waitFor(() => expect(store.logPreview()?.text).toBe('recent\n'));
    expect(client.preview).toHaveBeenLastCalledWith(campaign.id, 'large.log', 500);
    client.preview.mockResolvedValue({
      text: 'newest\n',
      lineCount: 1,
      sizeBytes: 1000010,
      truncated: true,
      byteLimited: false,
    });
    await vi.advanceTimersByTimeAsync(2000);
    expect(store.logPreview()?.text).toBe('newest\n');
    const unchanged = store.logPreview();
    await vi.advanceTimersByTimeAsync(2000);
    expect(store.logPreview()).toBe(unchanged);
  });
  it('discards an old response even when the same file is selected again', async () => {
    await store.start();
    let resolveLog!: (log: AutoLlmLogPreview) => void;
    client.preview.mockImplementationOnce(
      () =>
        new Promise<AutoLlmLogPreview>((resolve) => {
          resolveLog = resolve;
        }),
    );
    store.selectLog('same.log');
    store.selectLog('other.log');
    store.selectLog('same.log');
    await vi.waitFor(() => expect(store.logLoading()).toBe(false));
    resolveLog({
      text: 'old response',
      lineCount: 1,
      sizeBytes: 12,
      truncated: false,
      byteLimited: false,
    });
    await Promise.resolve();
    expect(store.logPreview()?.text).toBe('');
  });
  it('refreshes with the chosen line limit and stops polling a paused or closed preview', async () => {
    await store.start();
    store.selectLog('large.log');
    await vi.waitFor(() => expect(store.logLoading()).toBe(false));
    store.setLogLines(100);
    await vi.waitFor(() => expect(store.logLoading()).toBe(false));
    expect(client.preview).toHaveBeenLastCalledWith(campaign.id, 'large.log', 100);
    store.logFollow.set(false);
    client.preview.mockClear();
    await vi.advanceTimersByTimeAsync(2000);
    expect(client.preview).not.toHaveBeenCalled();
    await store.refreshLog();
    expect(client.preview).toHaveBeenCalledTimes(1);
    store.selectLog('');
    store.logFollow.set(true);
    await vi.advanceTimersByTimeAsync(2000);
    expect(client.preview).toHaveBeenCalledTimes(1);
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
    client.preview.mockResolvedValue({
      text: 'recovered',
      lineCount: 1,
      sizeBytes: 9,
      truncated: false,
      byteLimited: false,
    });
    await store.refreshLog();
    expect(store.logError()).toBeNull();
    expect(store.logPreview()?.text).toBe('recovered');
  });
  it('assembles split activity events and resumes from their byte cursor', async () => {
    const running = {
      ...campaign,
      trials: [
        {
          ...campaign.trials[0],
          attempts: [{ number: 1, stage: 'GENERATION', status: 'RUNNING' }],
        },
      ],
    } as AutoLlmCampaign;
    client.list.mockResolvedValue({ active: running.id, campaigns: [running] });
    client.activity
      .mockResolvedValueOnce({
        text: '{"timestamp":"now","type":"item.completed","text":"hel',
        nextCursor: 53,
        more: false,
        reset: false,
      })
      .mockResolvedValue({ text: 'lo"}\n', nextCursor: 58, more: false, reset: false });
    await store.start();
    expect(store.activity()).toEqual([]);
    await vi.advanceTimersByTimeAsync(2000);
    expect(client.activity).toHaveBeenLastCalledWith(campaign.id, 1, 1, 53);
    expect(store.activity()).toEqual([{ timestamp: 'now', type: 'item.completed', text: 'hello' }]);
  });
  it('uses proposed indexes without a separate screening approval', () => {
    store.config.set(config);
    store.selectedScreening.set('second');
    store.screenings.set([
      {
        id: 'second',
        status: 'READY_FOR_REVIEW',
        mirror: '/screening',
      } as import('../model/autollm').ScreeningJob,
    ]);
    store.useSelection();
    expect(store.config()?.trainingPath).toBe('/screening/proposal/proposed-train.index');
    expect(store.config()?.comparisonPath).toBe('/screening/proposal/proposed-test.index');
    expect(client.screeningAction).not.toHaveBeenCalled();
  });
  it('recovers only the selected trial, records its failure reason, and refreshes availability', async () => {
    await store.start();
    store.selectTrial(2);
    await store.resumeTrial();
    expect(client.resumeTrial).toHaveBeenCalledWith(campaign.id, 2);
    await store.failTrial('   ');
    expect(client.failTrial).not.toHaveBeenCalled();
    await store.failTrial(' Sampling exhausted ');
    expect(client.failTrial).toHaveBeenCalledWith(campaign.id, 2, 'Sampling exhausted');
    await store.targetAction('server-a', 'requalify');
    expect(client.targetAction).toHaveBeenCalledWith(campaign.id, 'server-a', 'requalify');
    expect(client.recovery).toHaveBeenCalledTimes(4);
  });
  it('isolates recovery endpoint failures and discards late availability from another campaign', async () => {
    client.recovery.mockRejectedValueOnce(new Error('recovery offline'));
    await store.start();
    expect(store.recoveryError()).toBe('recovery offline');
    expect(store.connectionError()).toBeNull();
    let resolve!: (value: unknown) => void;
    client.recovery.mockImplementationOnce(
      () =>
        new Promise((done) => {
          resolve = done;
        }),
    );
    const pending = store.refreshRecovery();
    store.select('another');
    resolve({ trials: {}, targets: {} });
    await pending;
    expect(store.recovery()).toBeNull();
    expect(store.recoveryError()).toBeNull();
  });
  it('does not replace the selected review with a late response', async () => {
    store.selectScreening('first');
    let resolve!: (value: ScreeningReview) => void;
    client.screeningReview.mockImplementation(
      () =>
        new Promise<ScreeningReview>((done) => {
          resolve = done;
        }),
    );
    const pending = store.reviewScreening();
    store.selectScreening('second');
    resolve({ proposalSha256: 'first-review' } as ScreeningReview);
    await pending;
    expect(store.review()).toBeNull();
  });
  it('loads the selected campaign into the editor and clears campaign-scoped evidence', () => {
    const other = { ...campaign, id: 'other', config: { trials: 9 } as AutoLlmConfig };
    store.campaigns.set([campaign, other]);
    store.config.set({ trials: 2 } as AutoLlmConfig);
    store.summary.set({ campaign: 'old' });
    store.activeProgress.set({ 1: { progress: { status: 'TUNING' }, stale: false } });
    store.select('other');
    expect(store.config()).toEqual(other.config);
    expect(store.config()).not.toBe(other.config);
    expect(store.summary()).toBeNull();
    expect(store.activeProgress()).toEqual({});
  });
  it('hydrates a server-enrollment receipt without losing the campaign while status is loading', async () => {
    store.campaigns.set([campaign]);
    store.select(campaign.id);
    let resolve!: (value: AutoLlmCampaign) => void;
    client.status.mockImplementationOnce(
      () =>
        new Promise((done) => {
          resolve = done;
        }),
    );
    const pending = store.addTarget({
      id: 'worker',
      host: 'host',
      workspace: '/work',
      javaExecutable: 'java',
      environment: {},
    });
    await Promise.resolve();
    await Promise.resolve();
    expect(store.campaign()?.trials).toEqual(campaign.trials);
    expect(store.completed()).toBe(0);
    resolve(campaign);
    await pending;
    expect(client.status).toHaveBeenCalledWith(campaign.id);
    expect(store.error()).toBeNull();
  });
  it('loads full evidence on inspection and preserves it across compact status polling', async () => {
    const attempt = { number: 1, stage: 'GENERATION', status: 'SUCCEEDED', directory: 'attempt' };
    const compact = {
      ...campaign,
      trials: [{ ...campaign.trials[0], attempts: [attempt], winner: null }],
    } as AutoLlmCampaign;
    const full = {
      ...compact,
      trials: [
        {
          ...compact.trials[0],
          winner: { component: 'New' },
          failure: 'saved failure',
          attempts: [{ ...attempt, generationEvidence: { status: 'ARCHIVED', conversations: [] } }],
        },
      ],
    } as unknown as AutoLlmCampaign;
    client.list.mockResolvedValue({ active: campaign.id, campaigns: [compact] });
    client.detail.mockResolvedValue(full);
    client.activity.mockResolvedValue({ text: '', nextCursor: 0 });
    await store.start();
    expect(client.detail).not.toHaveBeenCalled();
    store.selectTrial(1);
    await Promise.resolve();
    await Promise.resolve();
    expect(store.trial()?.winner).toEqual({ component: 'New' });
    await vi.advanceTimersByTimeAsync(2000);
    expect(store.trial()?.attempts[0].generationEvidence?.status).toBe('ARCHIVED');
    expect(client.detail).toHaveBeenCalledTimes(1);
  });
  it('discards full detail and result reads after switching away and back', async () => {
    store.campaigns.set([campaign]);
    store.select(campaign.id);
    let detail!: (value: AutoLlmCampaign) => void, result!: (value: unknown) => void;
    client.detail.mockImplementationOnce(
      () =>
        new Promise((done) => {
          detail = done;
        }),
    );
    client.campaignComparison.mockImplementationOnce(
      () =>
        new Promise((done) => {
          result = done;
        }),
    );
    store.objectiveModes.set({ Cost: 'MINIMIZE' });
    const a = store.refreshDetail(),
      b = store.loadResults('Cost');
    store.select('other');
    store.objectiveModes.set({});
    store.select(campaign.id);
    detail(campaign);
    result({ trials: [{ trial: 99 }] });
    await a;
    await b;
    expect(store.detail()).toBeNull();
    expect(store.results()).toBeNull();
  });
  it('keeps a new-campaign editor independent from background campaign polling', async () => {
    await store.start();
    await store.newCampaign();
    store.config.set({ trials: 42 } as AutoLlmConfig);
    await vi.advanceTimersByTimeAsync(2000);
    expect(store.selectedId()).toBe('');
    expect(store.config()?.trials).toBe(42);
  });
  it('reports failed comparison reads without discarding last known results', async () => {
    await store.start();
    const previous = store.comparison();
    client.comparison.mockRejectedValue(new Error('summary unavailable'));
    await vi.advanceTimersByTimeAsync(2000);
    expect(store.comparison()).toEqual(previous);
    expect(store.comparisonError()).toContain('summary unavailable');
  });
  it('requests host-matched comparison statistics with an explicit objective direction', async () => {
    await store.start();
    expect(client.campaignComparison).toHaveBeenCalledWith(campaign.id, 'Cost', 'MINIMIZE');
    client.campaignComparison.mockClear();
    await store.loadResults('Quality');
    expect(client.campaignComparison).toHaveBeenCalledWith(campaign.id, 'Quality', 'MAXIMIZE');
    await store.refreshResults();
    expect(client.campaignComparison).toHaveBeenCalledTimes(1);
  });
  it('recovers unavailable objective metadata without blocking the campaign monitor', async () => {
    client.objectives.mockRejectedValueOnce(new Error('metadata offline'));
    await store.start();
    expect(store.campaign()?.id).toBe(campaign.id);
    expect(store.objectivesError()).toContain('metadata offline');
    expect(client.campaignComparison).not.toHaveBeenCalled();
    await vi.advanceTimersByTimeAsync(2000);
    expect(store.objectivesError()).toBeNull();
    expect(client.campaignComparison).toHaveBeenCalledWith(campaign.id, 'Cost', 'MINIMIZE');
  });
  it('loads the default comparison when switching campaigns and ignores unknown objectives', async () => {
    await store.start();
    store.campaigns.update((campaigns) => [...campaigns, { ...campaign, id: 'other' }]);
    store.select('other');
    await Promise.resolve();
    expect(client.campaignComparison).toHaveBeenLastCalledWith('other', 'Cost', 'MINIMIZE');
    client.campaignComparison.mockClear();
    await store.loadResults('missing');
    expect(client.campaignComparison).not.toHaveBeenCalled();
    expect(store.resultObjective()).toBe('Cost');
  });
  it('keeps unsaved setup and its probe when the workspace has no campaigns', async () => {
    client.list.mockResolvedValue({ active: '', campaigns: [] });
    await store.start();
    store.config.set({ trials: 17 } as AutoLlmConfig);
    await store.probeTarget();
    await vi.advanceTimersByTimeAsync(2000);
    expect(store.config()?.trials).toBe(17);
    expect(store.targetStatus()?.['status']).toBe('SUCCEEDED');
  });
  it('keeps the accepted mutation and reports a failed status refresh without replaying it', async () => {
    await store.start();
    client.status.mockRejectedValue(new Error('offline'));
    await store.addTarget({
      id: 'worker',
      host: 'host',
      workspace: '/work',
      javaExecutable: 'java',
      environment: {},
    });
    expect(store.campaign()?.trials).toEqual(campaign.trials);
    expect(store.error()).toContain('Operation accepted');
    expect(client.addTarget).toHaveBeenCalledTimes(1);
    await vi.advanceTimersByTimeAsync(2000);
    expect(client.addTarget).toHaveBeenCalledTimes(1);
  });
});
