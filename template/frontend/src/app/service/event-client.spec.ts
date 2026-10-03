import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { EventClient } from './event-client';

describe('EventClient history API', () => {
  let client: EventClient;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [EventClient, provideHttpClient(), provideHttpClientTesting()],
    });
    client = TestBed.inject(EventClient);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    try {
      http.verify();
    } finally {
      TestBed.resetTestingModule();
    }
  });

  it('treats the backend empty-log response as no last event', async () => {
    const result = client.getLastEvent();
    http
      .expectOne('http://localhost:8080/lastevent')
      .flush(
        { message: 'No events have been published yet' },
        { status: 500, statusText: 'Internal Server Error' },
      );
    await Promise.resolve();
    http.expectOne('http://localhost:8080/events?from=0&to=1').flush([]);

    await expect(result).resolves.toBeNull();
  });

  it('requests half-open history ranges', async () => {
    const result = client.getEvents(1_000, 2_000);
    const request = http.expectOne('http://localhost:8080/events?from=1000&to=2000');
    request.flush([
      {
        eventId: 1_000,
        type: 'PingEvent',
        timestamp: 1,
        workerName: 'worker',
        payload: {},
      },
    ]);

    await expect(result).resolves.toHaveLength(1);
  });
});
