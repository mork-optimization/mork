import { DestroyRef, Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { BehaviorSubject, Observable, Subject, firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';
import { MorkEventEnvelope } from '../model/Events';

export interface EventHead {
  readonly eventLogId: string;
  readonly lastEventId: number;
}
export interface EventPage extends EventHead {
  readonly from: number;
  readonly to: number;
  readonly events: readonly MorkEventEnvelope[];
}
export type EventConnectionState = 'connecting' | 'syncing' | 'live' | 'disconnected' | 'retrying';

/** Ordered notifications; historical presentation data comes from REST snapshots. */
@Injectable({ providedIn: 'root' })
export class EventClient {
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);
  private readonly envelopeSubject = new Subject<MorkEventEnvelope>();
  private readonly connectedSubject = new Subject<void>();
  private readonly disconnectedSubject = new Subject<void>();
  private readonly errorSubject = new Subject<unknown>();
  private readonly stateSubject = new BehaviorSubject<EventConnectionState>('connecting');
  private readonly pending = new Map<string, MorkEventEnvelope>();
  private client: Client | null = null;
  private subscription: StompSubscription | null = null;
  private retryTimer: ReturnType<typeof setTimeout> | null = null;
  private connected = false;
  private synchronization: Promise<void> | null = null;
  private again = false;
  private generation = 0;
  private delay = 500;
  private logId: string | null = null;
  private cursor = -1;
  readonly envelopes$: Observable<MorkEventEnvelope> = this.envelopeSubject.asObservable();
  readonly connected$: Observable<void> = this.connectedSubject.asObservable();
  readonly disconnected$: Observable<void> = this.disconnectedSubject.asObservable();
  readonly errors$: Observable<unknown> = this.errorSubject.asObservable();
  readonly state$: Observable<EventConnectionState> = this.stateSubject.asObservable();
  get lastEventId(): number {
    return this.cursor;
  }
  get eventLogId(): string | null {
    return this.logId;
  }
  constructor() {
    this.destroyRef.onDestroy(() => {
      void this.stop();
    });
  }
  start(): void {
    if (this.client) return;
    const client = new Client({
      brokerURL: environment.websocketUrl,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      reconnectDelay: 1000,
      debug: environment.websocketDebug
        ? (message) => console.debug('[STOMP] ' + message)
        : () => undefined,
    });
    client.onConnect = () => {
      this.subscription?.unsubscribe();
      this.subscription = client.subscribe('/topic/events', (message) =>
        this.receiveMessage(message),
      );
      this.connected = true;
      this.generation++;
      this.connectedSubject.next();
      this.retryNow();
    };
    client.onWebSocketClose = () => {
      this.subscription = null;
      this.connected = false;
      this.generation++;
      this.stateSubject.next('disconnected');
      this.disconnectedSubject.next();
      this.clearTimer();
    };
    client.onWebSocketError = (error) => this.fail(error);
    client.onStompError = (error) => this.fail(error);
    this.client = client;
    client.activate();
  }
  async stop(): Promise<void> {
    this.connected = false;
    this.generation++;
    this.clearTimer();
    this.pending.clear();
    this.subscription?.unsubscribe();
    this.subscription = null;
    const client = this.client;
    this.client = null;
    if (client) await client.deactivate();
  }
  retryNow(): void {
    this.clearTimer();
    this.delay = 500;
    this.requestSync();
  }
  getHead(): Promise<EventHead> {
    return firstValueFrom(
      this.http
        .get<EventHead>(environment.apiBaseUrl + '/lastevent')
        .pipe(takeUntilDestroyed(this.destroyRef)),
    );
  }
  getEvents(eventLogId: string, from: number, to: number): Promise<EventPage> {
    return firstValueFrom(
      this.http
        .get<EventPage>(environment.apiBaseUrl + '/events', { params: { eventLogId, from, to } })
        .pipe(takeUntilDestroyed(this.destroyRef)),
    );
  }
  private receiveMessage(message: IMessage): void {
    try {
      const envelope: MorkEventEnvelope = JSON.parse(message.body);
      this.validate(envelope);
      if (envelope.eventLogId === this.logId && envelope.eventId <= this.cursor) return;
      if (this.pending.size >= 5000) {
        this.pending.clear();
        this.requestSync();
      }
      this.pending.set(this.key(envelope.eventLogId, envelope.eventId), envelope);
      if (this.stateSubject.value === 'live' && envelope.eventLogId === this.logId) this.drain();
      if (envelope.eventLogId !== this.logId || this.hasGap()) this.requestSync();
    } catch (error) {
      this.fail(error);
    }
  }
  private requestSync(): void {
    if (!this.connected) return;
    if (this.synchronization) {
      this.again = true;
      return;
    }
    this.again = false;
    const token = this.generation;
    this.synchronization = this.synchronize(token)
      .catch((error) => {
        if (token === this.generation && this.connected) this.fail(error);
      })
      .finally(() => {
        this.synchronization = null;
        if (this.again && this.connected) this.requestSync();
      });
  }
  private async synchronize(token: number): Promise<void> {
    this.stateSubject.next('syncing');
    const head = await this.getHead();
    if (!this.current(token)) return;
    if (
      typeof head.eventLogId !== 'string' ||
      !Number.isInteger(head.lastEventId) ||
      head.lastEventId < -1
    )
      throw new Error('Invalid event head');
    if (this.logId !== head.eventLogId) {
      // A fresh REST snapshot supplies historical results; establish the notification baseline.
      this.logId = head.eventLogId;
      this.cursor = head.lastEventId;
      for (const [key, event] of this.pending)
        if (event.eventLogId !== this.logId || event.eventId <= this.cursor)
          this.pending.delete(key);
    } else {
      if (head.lastEventId < this.cursor)
        throw new Error('Cursor moved backwards without a new log identity');
      await this.download(this.cursor + 1, head.lastEventId + 1, token);
    }
    if (!this.current(token)) return;
    this.drain();
    for (let attempts = 0; this.hasGap(); attempts++) {
      if (attempts === 10) throw new Error('Live delivery has not caught up');
      const next = this.smallestPending();
      if (next === null) break;
      await this.download(this.cursor + 1, next, token);
      if (!this.current(token)) return;
      this.drain();
    }
    if (this.cursor < head.lastEventId) throw new Error('Incomplete event history');
    if (!this.current(token)) return;
    this.delay = 500;
    this.clearTimer();
    this.stateSubject.next('live');
  }
  private async download(from: number, to: number, token: number): Promise<void> {
    for (let start = from; start < to;) {
      const end = Math.min(to, start + 1000),
        logId = this.logId!;
      const page = await this.getEvents(logId, start, end);
      if (!this.current(token)) return;
      if (
        page.eventLogId !== logId ||
        page.from !== start ||
        page.to !== end ||
        page.events.length !== end - start
      )
        throw new Error('Incomplete or obsolete history');
      let expected = start;
      for (const event of page.events) {
        this.validate(event);
        if (event.eventLogId !== logId || event.eventId !== expected++)
          throw new Error('Non-contiguous history');
        if (event.eventId > this.cursor) this.pending.set(this.key(logId, event.eventId), event);
      }
      this.drain();
      start = end;
    }
  }
  private drain(): void {
    if (!this.logId) return;
    for (
      let event = this.pending.get(this.key(this.logId, this.cursor + 1));
      event;
      event = this.pending.get(this.key(this.logId, this.cursor + 1))
    ) {
      this.pending.delete(this.key(this.logId, event.eventId));
      this.cursor = event.eventId;
      this.envelopeSubject.next(event);
    }
  }
  private smallestPending(): number | null {
    let minimum: number | null = null;
    for (const event of this.pending.values())
      if (
        event.eventLogId === this.logId &&
        event.eventId > this.cursor &&
        (minimum === null || event.eventId < minimum)
      )
        minimum = event.eventId;
    return minimum;
  }
  private hasGap(): boolean {
    const next = this.smallestPending();
    return next !== null && next > this.cursor + 1;
  }
  private current(token: number): boolean {
    return this.connected && token === this.generation;
  }
  private key(log: string, id: number): string {
    return log + ':' + id;
  }
  private clearTimer(): void {
    if (this.retryTimer) clearTimeout(this.retryTimer);
    this.retryTimer = null;
  }
  private fail(error: unknown): void {
    this.errorSubject.next(error);
    this.stateSubject.next('retrying');
    if (!this.connected || this.retryTimer) return;
    this.retryTimer = setTimeout(() => {
      this.retryTimer = null;
      this.requestSync();
    }, this.delay);
    this.delay = Math.min(30000, this.delay * 2);
  }
  private validate(value: MorkEventEnvelope): void {
    if (
      !value ||
      typeof value.eventLogId !== 'string' ||
      !Number.isInteger(value.eventId) ||
      value.eventId < 0 ||
      typeof value.type !== 'string' ||
      !Number.isFinite(value.timestamp) ||
      typeof value.workerName !== 'string' ||
      !value.payload ||
      typeof value.payload !== 'object'
    )
      throw new Error('Invalid envelope');
    const payload = value.payload as Record<string, unknown>;
    if (
      value.type === 'SolutionGeneratedEvent' &&
      (typeof payload['resultId'] !== 'string' ||
        typeof payload['success'] !== 'boolean' ||
        typeof payload['iteration'] !== 'string' ||
        !payload['objectives'] ||
        typeof payload['objectives'] !== 'object')
    )
      throw new Error('Invalid result event');
    if (
      value.type === 'ExecutionStartedEvent' &&
      (!payload['objectives'] ||
        typeof payload['objectives'] !== 'object' ||
        !Array.isArray(payload['experimentNames']))
    )
      throw new Error('Invalid execution event');
    if (value.type === 'ErrorEvent' && typeof payload['exceptionType'] !== 'string')
      throw new Error('Invalid error event');
  }
}
