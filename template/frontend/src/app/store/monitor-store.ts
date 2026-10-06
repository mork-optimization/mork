import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { ExperimentClient } from '../service/experiment-client';

export type ApplicationView =
  'detecting' | 'standard' | 'tuning' | 'worker' | 'utility' | 'unavailable';
@Injectable({ providedIn: 'root' })
export class MonitorStore {
  private readonly client = inject(ExperimentClient);
  private readonly destroyRef = inject(DestroyRef);
  readonly viewMode = signal<ApplicationView>('detecting');
  readonly latestError = signal<string | null>(null);
  private timer: ReturnType<typeof setTimeout> | null = null;
  private started = false;
  private destroyed = false;
  private polling = false;
  private retry = false;
  private delay = 500;
  constructor() {
    this.destroyRef.onDestroy(() => {
      this.destroyed = true;
      if (this.timer) clearTimeout(this.timer);
    });
  }
  start(): void {
    if (!this.started) {
      this.started = true;
      void this.poll();
    }
  }
  retryNow(): void {
    if (this.timer) clearTimeout(this.timer);
    if (this.polling) this.retry = true;
    else void this.poll();
  }
  private async poll(): Promise<void> {
    if (this.destroyed) return;
    this.polling = true;
    let delay = 10_000;
    try {
      const mode = await this.client.getMode();
      if (this.destroyed) return;
      this.viewMode.set(mode.mode.toLowerCase() as ApplicationView);
      this.latestError.set(null);
      this.delay = 500;
      if (mode.mode === 'DETECTING') delay = 500;
    } catch (error) {
      if (this.destroyed) return;
      this.latestError.set(
        error instanceof Error ? error.message : 'Could not contact the backend',
      );
      if (this.viewMode() === 'detecting') this.viewMode.set('unavailable');
      delay = this.delay;
      this.delay = Math.min(30_000, this.delay * 2);
    } finally {
      this.polling = false;
      if (!this.destroyed) {
        this.timer = setTimeout(() => void this.poll(), this.retry ? 0 : delay);
        this.retry = false;
      }
    }
  }
}
