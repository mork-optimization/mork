import { DestroyRef, Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import { Observable, Subject, firstValueFrom } from 'rxjs';
import { environment } from '../../environments/environment';
import { MorkEventEnvelope } from '../model/Events';

@Injectable({ providedIn: 'root' })
export class EventClient {
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);
  private readonly envelopeSubject = new Subject<MorkEventEnvelope>();
  private readonly connectedSubject = new Subject<void>();
  private readonly disconnectedSubject = new Subject<void>();
  private readonly errorSubject = new Subject<unknown>();

  private client: Client | null = null;
  private eventSubscription: StompSubscription | null = null;

  readonly envelopes$: Observable<MorkEventEnvelope> = this.envelopeSubject.asObservable();
  readonly connected$: Observable<void> = this.connectedSubject.asObservable();
  readonly disconnected$: Observable<void> = this.disconnectedSubject.asObservable();
  readonly errors$: Observable<unknown> = this.errorSubject.asObservable();

  constructor() {
    this.destroyRef.onDestroy(() => {
      void this.stop();
    });
  }

  start(): void {
    if (this.client) {
      return;
    }

    const client = new Client({
      brokerURL: environment.websocketUrl,
      heartbeatIncoming: 4_000,
      heartbeatOutgoing: 4_000,
      reconnectDelay: 500,
      debug: environment.websocketDebug
        ? (message: string) => console.debug(`[STOMP] ${message}`)
        : () => undefined,
    });

    client.onConnect = () => {
      this.eventSubscription?.unsubscribe();
      this.eventSubscription = client.subscribe('/topic/events', (message) => {
        this.receiveMessage(message);
      });
      this.connectedSubject.next();
    };
    client.onWebSocketClose = () => {
      this.eventSubscription = null;
      this.disconnectedSubject.next();
    };
    client.onWebSocketError = (event) => this.errorSubject.next(event);
    client.onStompError = (frame) => this.errorSubject.next(frame);

    this.client = client;
    client.activate();
  }

  async stop(): Promise<void> {
    this.eventSubscription?.unsubscribe();
    this.eventSubscription = null;
    const client = this.client;
    this.client = null;
    if (client) {
      await client.deactivate();
    }
  }

  async getLastEvent(): Promise<MorkEventEnvelope | null> {
    try {
      return await firstValueFrom(this.http.get<MorkEventEnvelope>(this.endpoint('/lastevent')));
    } catch (error: unknown) {
      if (!(error instanceof HttpErrorResponse) || (error.status !== 404 && error.status !== 500)) {
        throw error;
      }

      const firstEvents = await this.getEvents(0, 1);
      if (firstEvents.length === 0) {
        return null;
      }
      throw error;
    }
  }

  getEvents(from: number, to: number): Promise<readonly MorkEventEnvelope[]> {
    return firstValueFrom(
      this.http.get<readonly MorkEventEnvelope[]>(this.endpoint('/events'), {
        params: { from, to },
      }),
    );
  }

  private receiveMessage(message: IMessage): void {
    try {
      const value: unknown = JSON.parse(message.body);
      if (!this.isEnvelope(value)) {
        throw new Error('Received an invalid event envelope');
      }
      this.envelopeSubject.next(value);
    } catch (error: unknown) {
      this.errorSubject.next(error);
    }
  }

  private isEnvelope(value: unknown): value is MorkEventEnvelope {
    if (!value || typeof value !== 'object') {
      return false;
    }
    const envelope = value as Record<string, unknown>;
    return (
      Number.isInteger(envelope['eventId']) &&
      typeof envelope['type'] === 'string' &&
      typeof envelope['timestamp'] === 'number' &&
      typeof envelope['workerName'] === 'string' &&
      !!envelope['payload'] &&
      typeof envelope['payload'] === 'object'
    );
  }

  private endpoint(path: string): string {
    return `${environment.apiBaseUrl}${path}`;
  }
}
