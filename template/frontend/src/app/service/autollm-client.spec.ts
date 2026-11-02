import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AutoLlmClient } from './autollm-client';

describe('AutoLlmClient file previews', () => {
  afterEach(() => TestBed.resetTestingModule());

  it('reads the replacement draft after a compact edit receipt', async () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const client = TestBed.inject(AutoLlmClient),
      http = TestBed.inject(HttpTestingController);
    const config = { trials: 4 } as import('../model/autollm').AutoLlmConfig;
    const pending = client.editDraft('original', config);
    const edit = http.expectOne('http://localhost:8080/api/autollm/campaigns/original/draft');
    expect(edit.request.method).toBe('PUT');
    expect(edit.request.body).toEqual(config);
    edit.flush({ id: 'replacement', status: 'DRAFT', accepted: true });
    await Promise.resolve();
    http
      .expectOne('http://localhost:8080/api/autollm/campaigns/replacement/status')
      .flush({ id: 'replacement', status: 'DRAFT', config });
    await expect(pending).resolves.toMatchObject({ id: 'replacement', config });
    http.verify();
  });

  it('bounds status reads and cancels a hung request without replaying mutations', async () => {
    vi.useFakeTimers();
    try {
      TestBed.configureTestingModule({
        providers: [provideHttpClient(), provideHttpClientTesting()],
      });
      const client = TestBed.inject(AutoLlmClient),
        http = TestBed.inject(HttpTestingController);
      const pending = client.list();
      const rejected = expect(pending).rejects.toThrow('Timeout');
      const request = http.expectOne('http://localhost:8080/api/autollm/campaigns');
      await vi.advanceTimersByTimeAsync(10000);
      await rejected;
      expect(request.cancelled).toBe(true);
      http.verify();
    } finally {
      vi.useRealTimers();
    }
  });
  it('requests only the selected tail and keeps downloads on the full-file endpoint', async () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const client = TestBed.inject(AutoLlmClient),
      http = TestBed.inject(HttpTestingController);
    const pending = client.preview('campaign', 'attempt/std out.log', 100);
    const request = http.expectOne((req) => req.url.endsWith('/campaigns/campaign/preview'));
    expect(request.request.params.get('lines')).toBe('100');
    expect(request.request.params.get('path')).toBe('attempt/std out.log');
    expect(request.request.params.has('cursor')).toBe(false);
    request.flush({
      text: 'latest',
      lineCount: 1,
      sizeBytes: 10000000,
      truncated: true,
      byteLimited: false,
    });
    await expect(pending).resolves.toMatchObject({ text: 'latest' });
    const download = new URL(client.artifactUrl('campaign', 'attempt/std out.log'));
    expect(download.pathname).toBe('/api/autollm/campaigns/campaign/artifact');
    expect(download.searchParams.get('path')).toBe('attempt/std out.log');
    expect(download.searchParams.has('lines')).toBe(false);
    http.verify();
  });
  it('uses explicit trial and server recovery endpoints with the operator reason', async () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const client = TestBed.inject(AutoLlmClient),
      http = TestBed.inject(HttpTestingController);
    let pending = client.recovery('saved');
    http
      .expectOne('http://localhost:8080/api/autollm/campaigns/saved/recovery')
      .flush({ trials: {}, targets: {} });
    await pending;
    const resumed = client.resumeTrial('saved', 3);
    const request = http.expectOne(
      'http://localhost:8080/api/autollm/campaigns/saved/trials/3/resume',
    );
    expect(request.request.method).toBe('POST');
    request.flush({ id: 'saved' });
    await resumed;
    const failed = client.failTrial('saved', 9, 'Sampling exhausted');
    const failRequest = http.expectOne(
      'http://localhost:8080/api/autollm/campaigns/saved/trials/9/fail',
    );
    expect(failRequest.request.body).toEqual({ reason: 'Sampling exhausted' });
    failRequest.flush({ id: 'saved' });
    await failed;
    const qualified = client.targetAction('saved', 'worker/a', 'requalify');
    http
      .expectOne('http://localhost:8080/api/autollm/campaigns/saved/targets/worker%2Fa/requalify')
      .flush({ id: 'saved' });
    await qualified;
    http.verify();
  });
  it('reads full evidence independently of status and requests an explicit comparison without ranking', async () => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    const client = TestBed.inject(AutoLlmClient),
      http = TestBed.inject(HttpTestingController);
    const details = client.detail('saved');
    http
      .expectOne('http://localhost:8080/api/autollm/campaigns/saved')
      .flush({ id: 'saved', trials: [{ winner: { component: 'New' } }] });
    expect((await details).trials[0].winner).toEqual({ component: 'New' });
    const comparison = client.campaignComparison('saved', 'Quality score', 'MAXIMIZE');
    const request = http.expectOne((req) => req.url.endsWith('/campaigns/saved/comparison'));
    expect(request.request.params.get('objective')).toBe('Quality score');
    expect(request.request.params.get('mode')).toBe('MAXIMIZE');
    expect(request.request.params.get('ranking')).toBe('NONE');
    request.flush({ trials: [], excluded: { '2': 'Comparison incomplete' } });
    expect((await comparison).excluded['2']).toBe('Comparison incomplete');
    http.verify();
  });
});
