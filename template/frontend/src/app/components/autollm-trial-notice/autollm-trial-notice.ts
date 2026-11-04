import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { AutoLlmTrial } from '../../model/autollm';
import { TrialNotice } from '../../util/autollm-notice';

@Component({
  selector: 'app-autollm-trial-notice',
  imports: [DatePipe, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    @let state = notice();
    @let value = trial();
    <section
      class="trial-notice"
      [class.waiting]="state.kind === 'waiting'"
      [class.attention]="state.kind === 'attention'"
      [attr.aria-label]="
        state.kind === 'waiting' ? 'Trial waiting status' : 'Trial needs attention'
      "
    >
      <span class="notice-icon" aria-hidden="true">{{ state.kind === 'waiting' ? '◷' : '!' }}</span>
      <div>
        <h3>{{ state.title }}</h3>
        <p>{{ state.message }}</p>
        @if (state.retryAt) {
          <p class="muted">Scheduled retry: {{ state.retryAt | date: 'medium' }}</p>
        }
        @if (state.kind === 'attention') {
          @if (value.problem?.recovery) {
            <p>{{ value.problem.recovery }}</p>
          }
          <div class="actions">
            @if (value.targetId && value.problem?.category?.includes('INFRASTRUCTURE')) {
              <button mat-flat-button (click)="reviewServer.emit()">Review server</button>
            }
            <button mat-stroked-button (click)="inspectError.emit()">View error details</button>
          </div>
        }
      </div>
    </section>
  `,
  styles: `
    :host {
      display: block;
    }
    .trial-notice {
      display: flex;
      gap: 1rem;
      padding: 1rem;
      border-radius: 0.5rem;
      margin: 1rem 0;
    }
    .trial-notice > div {
      min-width: 0;
    }
    h3 {
      font: var(--mat-sys-title-medium);
      margin: 0;
    }
    p {
      margin: 0.35rem 0;
      line-height: 1.6;
    }
    .actions {
      display: flex;
      gap: 0.75rem;
      flex-wrap: wrap;
      margin-top: 0.75rem;
    }
    .notice-icon {
      font-size: 1.5rem;
      font-weight: 700;
    }
    .waiting {
      background: light-dark(#fff5db, #3b311b);
      color: light-dark(#4c390d, #f5dfaa);
      border-left: 4px solid light-dark(#916400, #e7bd5c);
    }
    .attention {
      background: var(--mat-sys-error-container);
      color: var(--mat-sys-on-error-container);
      border-left: 4px solid var(--mat-sys-error);
    }
    .muted {
      font-size: 0.875rem;
    }
  `,
})
export class AutoLlmTrialNotice {
  readonly notice = input.required<TrialNotice>();
  readonly trial = input.required<AutoLlmTrial>();
  readonly reviewServer = output<void>();
  readonly inspectError = output<void>();
}
