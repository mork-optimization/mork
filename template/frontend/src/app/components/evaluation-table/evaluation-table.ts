import { DecimalPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSelectModule } from '@angular/material/select';
import { EvaluationState, EvaluationView } from '../../model/autoconfig';

type SortColumn = 'id' | 'configurationId' | 'instanceName' | 'state' | 'cost' | 'timeSeconds';

@Component({
  selector: 'app-evaluation-table',
  imports: [
    DatePipe,
    DecimalPipe,
    FormsModule,
    MatButtonModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatInputModule,
    MatPaginatorModule,
    MatSelectModule,
  ],
  templateUrl: './evaluation-table.html',
  styleUrl: './evaluation-table.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EvaluationTable {
  readonly evaluations = input.required<readonly EvaluationView[]>();
  readonly forceIssues = input(false);
  readonly candidateSelected = output<string>();

  readonly search = signal('');
  readonly state = signal<EvaluationState | 'ALL'>('ALL');
  readonly slowOnly = signal(false);
  readonly issueOnly = signal(false);
  readonly pageIndex = signal(0);
  readonly pageSize = signal(25);
  readonly sortColumn = signal<SortColumn>('id');
  readonly sortAscending = signal(false);

  readonly filtered = computed(() => {
    const query = this.search().trim().toLowerCase();
    const state = this.state();
    const slowOnly = this.slowOnly();
    const issueOnly = this.issueOnly() || this.forceIssues();
    const values: EvaluationView[] = [];
    for (const evaluation of this.evaluations()) {
      if (state !== 'ALL' && evaluation.state !== state) {
        continue;
      }
      if (slowOnly && !evaluation.slow) {
        continue;
      }
      if (
        issueOnly &&
        evaluation.state !== 'FAILED' &&
        evaluation.state !== 'REJECTED' &&
        !evaluation.slow
      ) {
        continue;
      }
      if (
        query &&
        !evaluation.configurationId.toLowerCase().includes(query) &&
        !evaluation.instanceName.toLowerCase().includes(query) &&
        !String(evaluation.id).includes(query)
      ) {
        continue;
      }
      values.push(evaluation);
    }
    const column = this.sortColumn();
    const direction = this.sortAscending() ? 1 : -1;
    values.sort((left, right) => this.compare(left[column], right[column]) * direction);
    return values;
  });

  readonly page = computed(() => {
    const start = this.pageIndex() * this.pageSize();
    return this.filtered().slice(start, start + this.pageSize());
  });

  updateSearch(value: string): void {
    this.search.set(value);
    this.pageIndex.set(0);
  }

  updateState(value: EvaluationState | 'ALL'): void {
    this.state.set(value);
    this.pageIndex.set(0);
  }

  updateSlowOnly(value: boolean): void {
    this.slowOnly.set(value);
    this.pageIndex.set(0);
  }

  updateIssueOnly(value: boolean): void {
    this.issueOnly.set(value);
    this.pageIndex.set(0);
  }

  updatePage(event: PageEvent): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
  }

  sortBy(column: SortColumn): void {
    if (this.sortColumn() === column) {
      this.sortAscending.update((ascending) => !ascending);
    } else {
      this.sortColumn.set(column);
      this.sortAscending.set(true);
    }
    this.pageIndex.set(0);
  }

  sortIndicator(column: SortColumn): string {
    if (this.sortColumn() !== column) {
      return '';
    }
    return this.sortAscending() ? ' ↑' : ' ↓';
  }

  stateClass(state: EvaluationState): string {
    return `state-${state.toLowerCase()}`;
  }

  private compare(left: string | number | null, right: string | number | null): number {
    if (left === right) {
      return 0;
    }
    if (left === null) {
      return 1;
    }
    if (right === null) {
      return -1;
    }
    if (typeof left === 'number' && typeof right === 'number') {
      return left - right;
    }
    return String(left).localeCompare(String(right), undefined, { numeric: true });
  }
}
