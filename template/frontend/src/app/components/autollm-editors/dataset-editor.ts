import { Component, computed, effect, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatPaginatorModule } from '@angular/material/paginator';
import { DatasetSelection } from '../../model/autollm';

type Membership = 'training' | 'testing' | 'both' | 'excluded';
interface DatasetRow {
  path: string;
  membership: Membership;
  reason: string;
}

@Component({
  selector: 'app-autollm-dataset-editor',
  imports: [FormsModule, MatButtonModule, MatFormFieldModule, MatInputModule, MatPaginatorModule],
  template: `
    <p>
      Training and testing may overlap or be disjoint. Every excluded instance needs a reason.
      Saving creates a revised draft for review.
    </p>
    <p>
      {{ selection().training.length }} training · {{ selection().testing.length }} testing ·
      {{ excludedCount() }} excluded
    </p>
    <mat-form-field appearance="outline"
      ><mat-label>Find dataset instances</mat-label>
      <input matInput [ngModel]="search()" (ngModelChange)="search.set($event); page.set(0)" />
    </mat-form-field>
    <div class="table-scroll">
      <table>
        <thead>
          <tr>
            <th>Instance</th>
            <th>Use</th>
            <th>Exclusion reason</th>
          </tr>
        </thead>
        <tbody>
          @for (row of visibleRows(); track row.path) {
            <tr>
              <td class="path">{{ row.path }}</td>
              <td>
                <mat-form-field appearance="outline" subscriptSizing="dynamic">
                  <select
                    matNativeControl
                    [attr.aria-label]="'Use of ' + row.path"
                    [ngModel]="row.membership"
                    (ngModelChange)="update(row.path, { membership: $event })"
                  >
                    <option value="training">Training only</option>
                    <option value="both">Training and testing</option>
                    <option value="testing">Testing only</option>
                    <option value="excluded">Excluded</option>
                  </select>
                </mat-form-field>
              </td>
              <td>
                @if (row.membership === 'excluded') {
                  <mat-form-field appearance="outline" subscriptSizing="dynamic">
                    <textarea
                      matInput
                      rows="2"
                      [attr.aria-label]="'Exclusion reason for ' + row.path"
                      [ngModel]="row.reason"
                      (ngModelChange)="update(row.path, { reason: $event })"
                      required
                    ></textarea>
                  </mat-form-field>
                } @else {
                  <span>—</span>
                }
              </td>
            </tr>
          } @empty {
            <tr>
              <td colspan="3">No matching instances.</td>
            </tr>
          }
        </tbody>
      </table>
    </div>
    <mat-paginator
      [length]="filteredRows().length"
      [pageSize]="25"
      [pageIndex]="page()"
      [hidePageSize]="true"
      (page)="page.set($event.pageIndex)"
      aria-label="Dataset pages"
    />
    <details>
      <summary>Advanced: edit dataset JSON</summary>
      <button mat-button (click)="json.set(asJson())">Copy current selection into editor</button>
      <textarea
        aria-label="Dataset JSON"
        [ngModel]="json()"
        (ngModelChange)="json.set($event)"
        rows="6"
      ></textarea>
      <button mat-button [disabled]="!json().trim()" (click)="importJson()">
        Apply JSON to form
      </button>
      @if (jsonError()) {
        <p role="alert">{{ jsonError() }}</p>
      }
    </details>
    @if (validation()) {
      <p role="status">{{ validation() }}</p>
    }
    <button
      mat-flat-button
      [disabled]="busy() || !!validation() || !changed()"
      (click)="save.emit(selection())"
    >
      Validate revised dataset
    </button>
  `,
  styles: `
    :host {
      display: block;
    }
    .table-scroll {
      overflow-x: auto;
    }
    table {
      width: 100%;
      min-width: 650px;
      table-layout: fixed;
      border-collapse: collapse;
    }
    th:first-child {
      width: 40%;
    }
    th:nth-child(2) {
      width: 25%;
    }
    th:nth-child(3) {
      width: 35%;
    }
    td mat-form-field {
      width: 100%;
    }
    th,
    td {
      text-align: left;
      padding: 10px;
    }
    .path {
      overflow-wrap: anywhere;
    }
    textarea {
      display: block;
      width: 100%;
      box-sizing: border-box;
    }
    details {
      margin: 16px 0;
    }
    summary {
      cursor: pointer;
    }
  `,
})
export class AutoLlmDatasetEditor {
  readonly initial = input.required<DatasetSelection>();
  readonly busy = input(false);
  readonly save = output<DatasetSelection>();
  readonly dirtyChange = output<boolean>();
  readonly rows = signal<DatasetRow[]>([]);
  readonly search = signal('');
  readonly page = signal(0);
  readonly json = signal('');
  readonly jsonError = signal('');
  private readonly order = signal<{ training: string[]; testing: string[] }>({
    training: [],
    testing: [],
  });
  readonly excludedCount = computed(
    () => this.rows().filter((row) => row.membership === 'excluded').length,
  );
  readonly selection = computed<DatasetSelection>(() => {
    const selection: DatasetSelection = { training: [], testing: [], excluded: {} };
    for (const kind of ['training', 'testing'] as const) {
      const members = new Set(
        this.rows()
          .filter((row) => row.membership === kind || row.membership === 'both')
          .map((row) => row.path),
      );
      for (const path of [...this.order()[kind], ...members])
        if (members.delete(path)) selection[kind].push(path);
    }
    for (const row of this.rows())
      if (row.membership === 'excluded') selection.excluded[row.path] = row.reason.trim();
    return selection;
  });
  readonly changed = computed(
    () => JSON.stringify(this.selection()) !== JSON.stringify(this.initial()),
  );
  readonly validation = computed(() => this.validate(this.selection()));
  readonly filteredRows = computed(() =>
    this.rows().filter((row) =>
      `${row.path} ${row.reason}`.toLowerCase().includes(this.search().toLowerCase()),
    ),
  );
  readonly visibleRows = computed(() =>
    this.filteredRows().slice(this.page() * 25, (this.page() + 1) * 25),
  );
  constructor() {
    effect(() => {
      this.setRows(this.initial());
      this.page.set(0);
      this.json.set('');
      this.jsonError.set('');
    });
    effect(() => this.dirtyChange.emit(this.changed()));
  }
  update(path: string, patch: Partial<DatasetRow>): void {
    this.rows.update((rows) => rows.map((row) => (row.path === path ? { ...row, ...patch } : row)));
  }
  private setRows(selection: DatasetSelection): void {
    this.order.set({ training: [...selection.training], testing: [...selection.testing] });
    const training = new Set(selection.training),
      testing = new Set(selection.testing);
    this.rows.set(
      [
        ...new Set([
          ...selection.training,
          ...selection.testing,
          ...Object.keys(selection.excluded),
        ]),
      ].map((path) => ({
        path,
        membership: training.has(path)
          ? testing.has(path)
            ? 'both'
            : 'training'
          : testing.has(path)
            ? 'testing'
            : 'excluded',
        reason: selection.excluded[path] ?? '',
      })),
    );
  }
  private validate(selection: DatasetSelection): string {
    if (
      !selection ||
      !Array.isArray(selection.training) ||
      !Array.isArray(selection.testing) ||
      !selection.excluded ||
      typeof selection.excluded !== 'object' ||
      Array.isArray(selection.excluded)
    )
      return 'Provide training, testing and an exclusions object.';
    const training = new Set(selection.training),
      testing = new Set(selection.testing);
    if (!training.size || !testing.size) return 'Keep at least one training and testing instance.';
    if (training.size !== selection.training.length || testing.size !== selection.testing.length)
      return 'Training and testing must each contain unique instances.';
    const original = new Set([
      ...this.initial().training,
      ...this.initial().testing,
      ...Object.keys(this.initial().excluded),
    ]);
    const accounted = new Set([...training, ...testing]);
    for (const [path, reason] of Object.entries(selection.excluded)) {
      if (typeof reason !== 'string' || !reason.trim())
        return 'Add a reason for every excluded instance.';
      if (accounted.has(path))
        return 'An excluded instance cannot also be used for training or testing.';
      accounted.add(path);
    }
    if (accounted.size !== original.size || [...accounted].some((path) => !original.has(path)))
      return 'Keep every original instance in training, testing or exclusions; new paths cannot be added here.';
    return '';
  }
  asJson(): string {
    return JSON.stringify(this.selection(), null, 2);
  }
  importJson(): void {
    try {
      const selection = JSON.parse(this.json()) as DatasetSelection;
      const error = this.validate(selection);
      if (error) throw new Error(error);
      this.setRows(selection);
      this.page.set(0);
      this.jsonError.set('');
    } catch (error) {
      this.jsonError.set((error as Error).message);
    }
  }
}
