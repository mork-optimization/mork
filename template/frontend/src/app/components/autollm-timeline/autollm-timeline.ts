import { ChangeDetectionStrategy, Component, computed, input, model, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule } from '@angular/material/paginator';
import { AutoLlmTrial } from '../../model/autollm';
import {
  defaultTimelineSettings,
  matchesTrialFilter,
  TimelineSettings,
} from '../../util/autollm-navigation';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';
import {
  AttemptSelection,
  TRIAL_STAGES,
  stageDuration,
  timingDuration,
  trialTimeline,
  trialTiming,
} from '../../util/autollm-timing';

@Component({
  selector: 'app-autollm-timeline',
  imports: [
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatPaginatorModule,
    DashboardChart,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <mat-card appearance="outlined"
      ><mat-card-content>
        <h2 tabindex="-1">Trial duration timeline</h2>
        <p>
          One row per trial; each colored segment is a recorded attempt. Repairs and retries remain
          separate.
        </p>
        <div class="controls">
          <mat-form-field appearance="outline" subscriptSizing="dynamic"
            ><mat-label>Time axis</mat-label>
            <select
              matNativeControl
              [ngModel]="settings().mode"
              (ngModelChange)="updateSettings({ mode: $event })"
            >
              <option value="elapsed">Elapsed since trial start</option>
              <option value="clock">Campaign clock (UTC)</option>
            </select>
          </mat-form-field>
          <mat-form-field appearance="outline" subscriptSizing="dynamic"
            ><mat-label>Sort trials by</mat-label>
            <select
              matNativeControl
              [ngModel]="settings().sort"
              (ngModelChange)="updateSettings({ sort: $event, page: 0 })"
            >
              <option value="number">Trial number</option>
              <option value="total">Longest elapsed time</option>
              @for (stage of stages; track stage.id) {
                <option [value]="stage.id">Longest {{ stage.label.toLowerCase() }}</option>
              }
            </select>
          </mat-form-field>
          <mat-form-field appearance="outline" subscriptSizing="dynamic"
            ><mat-label>Find trial or server</mat-label>
            <input
              matInput
              [ngModel]="settings().search"
              (ngModelChange)="updateSettings({ search: $event, page: 0 })"
            />
          </mat-form-field>
        </div>
        @if (settings().outcome !== 'all') {
          <p class="outcome-filter">
            Showing {{ outcomeLabel() }}
            <button mat-button (click)="updateSettings({ outcome: 'all', page: 0 })">
              Show all trials
            </button>
          </p>
        }
        <div class="legend" aria-label="Stage colors">
          @for (stage of stages; track stage.id) {
            <span><i [style.background]="stage.color"></i>{{ stage.label }}</span>
          }
          <span><i class="gap"></i>Unclassified gap</span>
        </div>
        @if (rows().length) {
          @if (hasIntervals()) {
            <app-dashboard-chart [model]="chart()" />
          } @else {
            <p>No valid attempt intervals are available for these trials yet.</p>
          }
          <p class="note">
            Click a segment to inspect its attempt. Outlined segments are running at the last
            update. Durations include downtime.
          </p>
          <details>
            <summary>Duration breakdown and trial details</summary>
            <div class="table-scroll">
              <table>
                <thead>
                  <tr>
                    <th>Trial</th>
                    <th>Server</th>
                    <th>Elapsed</th>
                    @for (stage of stages; track stage.id) {
                      <th>{{ stage.label }}</th>
                    }
                    <th>Gaps</th>
                    <th>Evidence</th>
                    <th>Inspect</th>
                  </tr>
                </thead>
                <tbody>
                  @for (row of rows(); track row.trial.number) {
                    <tr>
                      <td>{{ row.trial.number }}</td>
                      <td>{{ row.trial.targetId || 'Unassigned' }}</td>
                      <td>{{ duration(row.elapsed) }}</td>
                      @for (stage of stages; track stage.id) {
                        <td>{{ duration(stageTime(row, stage.id)) }}</td>
                      }
                      <td>{{ duration(gapTime(row)) }}</td>
                      <td>
                        {{
                          row.unknown
                            ? row.unknown + ' attempts missing valid timing'
                            : row.start === null
                              ? 'No attempts recorded'
                              : 'Available intervals'
                        }}
                      </td>
                      <td>
                        <button mat-button (click)="inspectTrial.emit(row.trial.number)">
                          Open trial {{ row.trial.number }}
                        </button>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
            <p>
              These are wall-clock intervals, not CPU time, and may include remote downtime or
              reconnection. Gaps have no valid attempt timing; their cause is unknown. The time
              scale stays the same across pages. Stage totals include all attempts, including
              failures and repairs. Unavailable means no complete timing evidence; it does not mean
              zero time. Elapsed starts at the first valid attempt timestamp and includes gaps;
              unfinished trials extend to the last update. Time before the first attempt is
              unavailable.
            </p>
          </details>
        } @else {
          <p>No matching trials.</p>
        }
        <mat-paginator
          [length]="filtered().length"
          [pageSize]="25"
          [hidePageSize]="true"
          [pageIndex]="effectivePage()"
          (page)="updateSettings({ page: $event.pageIndex })"
          aria-label="Timeline trial pages"
        /> </mat-card-content
    ></mat-card>
  `,
  styles: `
    :host {
      display: block;
      min-width: 0;
      margin-top: 1rem;
    }
    mat-card-content {
      min-width: 0;
      padding: 1.25rem;
    }
    h2 {
      font: var(--mat-sys-title-large);
    }
    p {
      line-height: 1.6;
    }
    .controls {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(min(100%, 15rem), 1fr));
      gap: 1rem;
      margin: 1rem 0;
    }
    .legend {
      display: flex;
      flex-wrap: wrap;
      gap: 0.5rem 1rem;
      font-size: 0.875rem;
    }
    .legend span {
      display: flex;
      align-items: center;
      gap: 0.4rem;
    }
    .legend i {
      width: 0.8rem;
      height: 0.8rem;
      border-radius: 2px;
    }
    .gap {
      background: #e0e0e0;
    }
    .note {
      color: var(--mat-sys-on-surface-variant);
      font-size: 0.875rem;
    }
    summary {
      cursor: pointer;
      padding-block: 0.5rem;
    }
    .table-scroll {
      overflow-x: auto;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.875rem;
    }
    th,
    td {
      text-align: left;
      padding: 0.75rem;
      border-bottom: 1px solid var(--mat-sys-outline-variant);
    }
  `,
})
export class AutoLlmTimeline {
  readonly trials = input.required<AutoLlmTrial[]>();
  readonly now = input.required<number>();
  readonly inspectAttempt = output<AttemptSelection>();
  readonly inspectTrial = output<number>();
  readonly stages = TRIAL_STAGES;
  readonly duration = timingDuration;
  readonly stageTime = stageDuration;
  readonly settings = model<TimelineSettings>(defaultTimelineSettings());
  readonly outcomeLabel = computed(
    () =>
      ({
        all: 'all trials',
        finished: 'finished trials',
        active: 'active trials',
        attention: 'trials needing attention',
        SUCCEEDED: 'succeeded trials',
        FAILED: 'failed trials',
        PENDING: 'pending trials',
      })[this.settings().outcome],
  );
  updateSettings(patch: Partial<TimelineSettings>): void {
    this.settings.update((value) => ({ ...value, ...patch }));
  }
  readonly timings = computed(() => this.trials().map((trial) => trialTiming(trial, this.now())));
  readonly filtered = computed(() => {
    const search = this.settings().search.toLowerCase(),
      sort = this.settings().sort;
    return this.timings()
      .filter(
        (row) =>
          matchesTrialFilter(row.trial, this.settings().outcome) &&
          `${row.trial.number} ${row.trial.targetId ?? ''}`.toLowerCase().includes(search),
      )
      .sort((a, b) =>
        sort === 'number'
          ? a.trial.number - b.trial.number
          : ((sort === 'total' ? b.elapsed : stageDuration(b, sort)) ?? -1) -
              ((sort === 'total' ? a.elapsed : stageDuration(a, sort)) ?? -1) ||
            a.trial.number - b.trial.number,
      );
  });
  readonly effectivePage = computed(() =>
    Math.min(this.settings().page, Math.max(0, Math.ceil(this.filtered().length / 25) - 1)),
  );
  readonly rows = computed(() =>
    this.filtered().slice(this.effectivePage() * 25, (this.effectivePage() + 1) * 25),
  );
  readonly hasIntervals = computed(() => this.rows().some((row) => row.intervals.length));
  readonly chart = computed(() =>
    trialTimeline(this.rows(), this.filtered(), this.settings().mode, (value) =>
      this.inspectAttempt.emit(value),
    ),
  );
  gapTime(row: ReturnType<typeof trialTiming>): number | null {
    return row.start === null ? null : row.gaps.reduce((sum, gap) => sum + gap.end - gap.start, 0);
  }
}
