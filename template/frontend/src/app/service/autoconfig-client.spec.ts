import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AutoconfigClient } from './autoconfig-client';

describe('AutoconfigClient', () => {
  let client: AutoconfigClient;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [AutoconfigClient, provideHttpClient(), provideHttpClientTesting()],
    });
    client = TestBed.inject(AutoconfigClient);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    try {
      http.verify();
    } finally {
      TestBed.resetTestingModule();
    }
  });

  it('uses the revision cursor and maximum supported page size', async () => {
    const result = client.getEvaluationChanges(500, 500);
    const request = http.expectOne(
      'http://localhost:8080/api/autoconfig/evaluations/changes?after=500&limit=500',
    );
    request.flush({ runId: 'run-1', latestRevision: 500, nextRevision: 500, changes: [] });

    await expect(result).resolves.toMatchObject({ nextRevision: 500 });
  });

  it('encodes candidate and artifact identifiers in read-only URLs', async () => {
    const result = client.getCandidate('candidate/a b');
    http.expectOne('http://localhost:8080/api/autoconfig/candidates/candidate%2Fa%20b').flush({
      configurationId: 'candidate/a b',
      parameters: {},
      algorithm: null,
      decodeError: null,
      evaluations: { running: 0, succeeded: 0, rejected: 0, failed: 0, slow: 0 },
    });

    await expect(result).resolves.toMatchObject({ configurationId: 'candidate/a b' });
    expect(client.artifactUrl('log/a')).toBe(
      'http://localhost:8080/api/autoconfig/artifacts/log%2Fa',
    );
  });
});
