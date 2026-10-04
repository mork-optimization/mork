import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { SearchSpaceSnapshot } from '../../model/autoconfig';

@Component({
  selector: 'app-search-space-dialog',
  imports: [MatButtonModule, MatDialogModule],
  template: `
    <h2 mat-dialog-title>Generated search space</h2>
    <mat-dialog-content>
      <section class="summary">
        <span
          ><strong>{{ data.summary.rootCount }}</strong> roots</span
        >
        <span
          ><strong>{{ data.summary.componentCount }}</strong> components</span
        >
        <span
          ><strong>{{ data.summary.generatedIraceParameterCount }}</strong> IRACE parameters</span
        >
        <span
          ><strong>{{ data.summary.generatedForbiddenConstraintCount }}</strong> constraints</span
        >
      </section>
      <p><strong>Root choices:</strong> {{ data.roots.join(', ') }}</p>
      <section class="components">
        @for (component of data.components; track component.name) {
          <details>
            <summary>{{ component.name }} · {{ component.parameters.length }} parameters</summary>
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Kind</th>
                  <th>Domain</th>
                </tr>
              </thead>
              <tbody>
                @for (parameter of component.parameters; track parameter.name) {
                  <tr>
                    <td>{{ parameter.name }}</td>
                    <td>{{ parameter.kind }}</td>
                    <td>{{ domain(parameter) }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </details>
        }
      </section>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close type="button">Close</button>
    </mat-dialog-actions>
  `,
  styles: `
    mat-dialog-content {
      width: min(60rem, 84vw);
    }

    .summary {
      display: flex;
      flex-wrap: wrap;
      gap: 0.75rem 2rem;
      margin-bottom: 1rem;
    }

    .components {
      display: grid;
      gap: 0.5rem;
    }

    details {
      padding: 0.75rem;
      border: 1px solid var(--mat-sys-outline-variant);
      border-radius: 0.5rem;
    }

    summary {
      cursor: pointer;
      font-weight: 650;
    }

    table {
      width: 100%;
      margin-top: 0.75rem;
      border-collapse: collapse;
    }

    th,
    td {
      padding: 0.45rem;
      border-bottom: 1px solid var(--mat-sys-outline-variant);
      text-align: left;
      overflow-wrap: anywhere;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SearchSpaceDialog {
  readonly data = inject<SearchSpaceSnapshot>(MAT_DIALOG_DATA);

  domain(parameter: SearchSpaceSnapshot['components'][number]['parameters'][number]): string {
    if (parameter.choices.length > 0) {
      return parameter.choices.join(', ');
    }
    if (parameter.values.length > 0) {
      return parameter.values.join(', ');
    }
    if (parameter.minimum !== null || parameter.maximum !== null) {
      return `${String(parameter.minimum)} – ${String(parameter.maximum)}`;
    }
    return 'Provided by Mork';
  }
}
