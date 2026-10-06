import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { EventClient, EventHead, EventPage } from './event-client';
import { MorkEventEnvelope } from '../model/Events';

const socket = vi.hoisted(() => ({
  connect: () => {},
  close: () => {},
  receive: (_message: { body: string }) => {},
}));
vi.mock('@stomp/stompjs', () => ({
  Client: class {
    set onConnect(callback: () => void) {
      socket.connect = callback;
    }
    set onWebSocketClose(callback: () => void) {
      socket.close = callback;
    }
    subscribe(_topic: string, callback: (message: { body: string }) => void) {
      socket.receive = callback;
      return { unsubscribe: vi.fn() };
    }
    activate() {}
    async deactivate() {}
  },
}));
function event(id: number, log = 'log-a', type = 'PingEvent'): MorkEventEnvelope {
  return { eventLogId: log, eventId: id, type, timestamp: 1, workerName: 'producer', payload: {} };
}
describe('EventClient synchronization', () => {
  let client: EventClient, http: HttpTestingController;
  beforeEach(() => {
    vi.useFakeTimers();
    TestBed.configureTestingModule({
      providers: [EventClient, provideHttpClient(), provideHttpClientTesting()],
    });
    client = TestBed.inject(EventClient);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => {
    http.verify();
    TestBed.resetTestingModule();
    vi.useRealTimers();
  });
  async function flush(): Promise<void> {
    await vi.advanceTimersByTimeAsync(0);
  }
  function setup(
    head: { eventLogId: string; lastEventId: number } = { eventLogId: 'log-a', lastEventId: -1 },
  ) {
    const getHead = vi.spyOn(client, 'getHead').mockImplementation(async () => head);
    const getEvents = vi
      .spyOn(client, 'getEvents')
      .mockImplementation(async (log, from, to): Promise<EventPage> => ({
        eventLogId: log,
        lastEventId: head.lastEventId,
        from,
        to,
        events: Array.from({ length: to - from }, (_, i) => event(from + i, log)),
      }));
    client.start();
    socket.connect();
    return { head, getHead, getEvents };
  }
  it('represents empty logs explicitly, without interpreting HTTP 500 as empty', async () => {
    const result = client.getHead();
    http
      .expectOne('http://localhost:8080/lastevent')
      .flush({ eventLogId: 'empty', lastEventId: -1 });
    await expect(result).resolves.toEqual({ eventLogId: 'empty', lastEventId: -1 });
  });
  it('scopes half-open history requests to the log identity', async () => {
    const result = client.getEvents('log-a', 1000, 2000);
    http
      .expectOne(
        (r) =>
          r.url.endsWith('/events') &&
          r.params.get('eventLogId') === 'log-a' &&
          r.params.get('from') === '1000' &&
          r.params.get('to') === '2000',
      )
      .flush({ eventLogId: 'log-a', lastEventId: 1999, from: 1000, to: 2000, events: [] });
    expect((await result).eventLogId).toBe('log-a');
  });
  it('deduplicates event zero without resetting valid state', async () => {
    setup();
    const delivered: number[] = [];
    client.envelopes$.subscribe((e) => delivered.push(e.eventId));
    await flush();
    socket.receive({ body: JSON.stringify(event(0)) });
    socket.receive({ body: JSON.stringify(event(0)) });
    expect(client.lastEventId).toBe(0);
    expect(delivered).toEqual([0]);
  });
  it('downloads out-of-order gaps and drains only contiguous IDs', async () => {
    const { head } = setup();
    const delivered: number[] = [];
    client.envelopes$.subscribe((e) => delivered.push(e.eventId));
    await flush();
    socket.receive({ body: JSON.stringify(event(0)) });
    head.lastEventId = 2;
    socket.receive({ body: JSON.stringify(event(2)) });
    await flush();
    expect(delivered).toEqual([0, 1, 2]);
    expect(client.lastEventId).toBe(2);
  });
  it('reconnects through multiple exact-boundary batches', async () => {
    const { head, getEvents } = setup();
    await flush();
    socket.close();
    head.lastEventId = 1999;
    socket.connect();
    await flush();
    expect(getEvents.mock.calls).toEqual([
      ['log-a', 0, 1000],
      ['log-a', 1000, 2000],
    ]);
    expect(client.lastEventId).toBe(1999);
  });
  it('does not declare live when history is incomplete and retries while connected', async () => {
    const { head, getEvents } = setup();
    await flush();
    head.lastEventId = 2;
    getEvents.mockResolvedValueOnce({
      eventLogId: 'log-a',
      lastEventId: 2,
      from: 0,
      to: 3,
      events: [event(0)],
    });
    let state = '';
    client.state$.subscribe((value) => (state = value));
    socket.receive({ body: JSON.stringify(event(2)) });
    await flush();
    expect(state).toBe('retrying');
    expect(client.lastEventId).toBe(-1);
    await vi.advanceTimersByTimeAsync(500);
    expect(state).toBe('live');
    expect(client.lastEventId).toBe(2);
  });
  it('recovers a transient REST error without closing the socket', async () => {
    const { getHead } = setup();
    await flush();
    getHead.mockRejectedValueOnce(new Error('temporary'));
    client.retryNow();
    await flush();
    await vi.advanceTimersByTimeAsync(500);
    expect(getHead).toHaveBeenCalledTimes(3);
  });
  it('detects restart by identity even when the new head is larger', async () => {
    const { head } = setup({ eventLogId: 'log-a', lastEventId: 100 });
    await flush();
    socket.close();
    head.eventLogId = 'log-b';
    head.lastEventId = 500;
    socket.connect();
    await flush();
    expect(client.eventLogId).toBe('log-b');
    expect(client.lastEventId).toBe(500);
  });
  it('advances unknown and ping notifications without presentation mutation', async () => {
    setup();
    await flush();
    socket.receive({ body: JSON.stringify(event(0, 'log-a', 'CustomEvent')) });
    socket.receive({ body: JSON.stringify(event(1)) });
    expect(client.lastEventId).toBe(1);
  });
  it('ignores an obsolete in-flight head after disconnect', async () => {
    const { getHead } = setup();
    await flush();
    let resolve!: (head: EventHead) => void;
    getHead.mockImplementationOnce(() => new Promise((r) => (resolve = r)));
    client.retryNow();
    socket.close();
    resolve({ eventLogId: 'obsolete', lastEventId: 999 });
    await flush();
    expect(client.eventLogId).toBe('log-a');
    expect(client.lastEventId).toBe(-1);
  });
});
