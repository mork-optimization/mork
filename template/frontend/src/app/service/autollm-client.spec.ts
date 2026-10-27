import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AutoLlmClient } from './autollm-client';

describe('AutoLlmClient file previews', () => {
  afterEach(() => TestBed.resetTestingModule());

  it('bounds status reads and cancels a hung request without replaying mutations', async () => {
    vi.useFakeTimers();
    try {
      TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
      const client = TestBed.inject(AutoLlmClient), http = TestBed.inject(HttpTestingController);
      const pending = client.list(); const rejected = expect(pending).rejects.toThrow('Timeout');
      const request = http.expectOne('http://localhost:8080/api/autollm/campaigns');
      await vi.advanceTimersByTimeAsync(10000); await rejected;
      expect(request.cancelled).toBe(true); http.verify();
    } finally { vi.useRealTimers(); }
  });
  it('requests only the selected tail and keeps downloads on the full-file endpoint', async () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const client = TestBed.inject(AutoLlmClient), http = TestBed.inject(HttpTestingController);
    const pending = client.preview('campaign', 'attempt/std out.log', 100);
    const request = http.expectOne(req => req.url.endsWith('/campaigns/campaign/preview'));
    expect(request.request.params.get('lines')).toBe('100');
    expect(request.request.params.get('path')).toBe('attempt/std out.log');
    expect(request.request.params.has('cursor')).toBe(false);
    request.flush({ text: 'latest', lineCount: 1, sizeBytes: 10000000, truncated: true, byteLimited: false });
    await expect(pending).resolves.toMatchObject({ text: 'latest' });
    const download = new URL(client.artifactUrl('campaign', 'attempt/std out.log'));
    expect(download.pathname).toBe('/api/autollm/campaigns/campaign/artifact');
    expect(download.searchParams.get('path')).toBe('attempt/std out.log');
    expect(download.searchParams.has('lines')).toBe(false);
    http.verify();
  });
  it('uses explicit trial and server recovery endpoints with the operator reason', async () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const client = TestBed.inject(AutoLlmClient), http = TestBed.inject(HttpTestingController);
    let pending = client.recovery('saved');
    http.expectOne('http://localhost:8080/api/autollm/campaigns/saved/recovery').flush({ trials: {}, targets: {} }); await pending;
    const resumed = client.resumeTrial('saved', 3);
    const request = http.expectOne('http://localhost:8080/api/autollm/campaigns/saved/trials/3/resume'); expect(request.request.method).toBe('POST'); request.flush({ id: 'saved' }); await resumed;
    const failed = client.failTrial('saved', 9, 'Sampling exhausted');
    const failRequest = http.expectOne('http://localhost:8080/api/autollm/campaigns/saved/trials/9/fail'); expect(failRequest.request.body).toEqual({ reason: 'Sampling exhausted' }); failRequest.flush({ id: 'saved' }); await failed;
    const qualified = client.targetAction('saved', 'worker/a', 'requalify');
    http.expectOne('http://localhost:8080/api/autollm/campaigns/saved/targets/worker%2Fa/requalify').flush({ id: 'saved' }); await qualified;
    http.verify();
  });
});
