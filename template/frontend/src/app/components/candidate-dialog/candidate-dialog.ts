import { JsonPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { CandidateView } from '../../model/autoconfig';
import { AutoconfigStore } from '../../store/autoconfig-store';

@Component({
  selector: 'app-candidate-dialog',
  imports: [JsonPipe, MatButtonModule, MatDialogModule],
  template: `
    <h2 mat-dialog-title>Configuration {{ configurationId }}</h2>
    <mat-dialog-content>
      @if (candidate(); as value) {
        <section class="counts" aria-label="Candidate evaluation counts">
          <span
            ><strong>{{ value.evaluations.succeeded }}</strong> succeeded</span
          >
          <span
            ><strong>{{ value.evaluations.running }}</strong> running</span
          >
          <span
            ><strong>{{ value.evaluations.rejected }}</strong> rejected</span
          >
          <span
            ><strong>{{ value.evaluations.failed }}</strong> failed</span
          >
          <span
            ><strong>{{ value.evaluations.slow }}</strong> slow</span
          >
        </section>

        <h3>Parameters</h3>
        <dl>
          @for (entry of parameterEntries(value); track entry[0]) {
            <dt>{{ entry[0] }}</dt>
            <dd>{{ entry[1] }}</dd>
          } @empty {
            <dd>No parameters were reported.</dd>
          }
        </dl>

        @if (value.algorithm) {
          <h3>Decoded algorithm</h3>
          <pre>{{ value.algorithm | json }}</pre>
        }
        @if (value.decodeError) {
          <p class="error" role="alert">{{ value.decodeError }}</p>
        }
      } @else if (error()) {
        <p class="error" role="alert">{{ error() }}</p>
      } @else {
        <p>Loading configuration…</p>
      }
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close type="button">Close</button>
    </mat-dialog-actions>
  `,
  styles: `
    mat-dialog-content {
      min-width: min(42rem, 80vw);
    }

    .counts {
      display: flex;
      flex-wrap: wrap;
      gap: 0.5rem 1.25rem;
      margin-bottom: 1.25rem;
    }

    dl {
      display: grid;
      grid-template-columns: minmax(10rem, 1fr) minmax(8rem, 1fr);
      gap: 0.35rem 1rem;
    }

    dt {
      overflow-wrap: anywhere;
      color: var(--mat-sys-on-surface-variant);
    }

    dd {
      margin: 0;
      overflow-wrap: anywhere;
    }

    pre {
      max-height: 22rem;
      overflow: auto;
      padding: 1rem;
      border-radius: 0.5rem;
      background: var(--mat-sys-surface-container);
      white-space: pre-wrap;
    }

    .error {
      color: var(--mat-sys-error);
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class CandidateDialog implements OnInit {
  private readonly store = inject(AutoconfigStore);
  readonly configurationId = inject<string>(MAT_DIALOG_DATA);
  readonly candidate = signal<CandidateView | null>(null);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    void this.store
      .loadCandidate(this.configurationId)
      .then((candidate) => this.candidate.set(candidate))
      .catch((error: unknown) =>
        this.error.set(error instanceof Error ? error.message : String(error)),
      );
  }

  parameterEntries(candidate: CandidateView): readonly [string, string][] {
    return Object.entries(candidate.parameters);
  }
}
