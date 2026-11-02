import { Component, computed, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { AutoLlmServer } from '../../model/autollm';

@Component({
  selector: 'app-autollm-server-editor',
  imports: [FormsModule, MatButtonModule, MatFormFieldModule, MatInputModule],
  template: `
    <div class="fields">
      @for (field of fields; track field.key) {
        <mat-form-field appearance="outline"
          ><mat-label>{{ field.label }}</mat-label>
          <input
            matInput
            [ngModel]="server()[field.key]"
            (ngModelChange)="update(field.key, $event)"
            [placeholder]="field.placeholder"
            [required]="field.required"
          />
        </mat-form-field>
      }
    </div>
    <details>
      <summary>Advanced server settings</summary>
      <div class="fields">
        <mat-form-field appearance="outline"
          ><mat-label>Workers (empty for automatic)</mat-label>
          <input
            matInput
            type="number"
            min="1"
            step="1"
            [ngModel]="server().workers"
            (ngModelChange)="update('workers', $event === '' ? null : $event)"
          /> </mat-form-field
        ><mat-form-field appearance="outline"
          ><mat-label>Java heap</mat-label>
          <input
            matInput
            placeholder="8g"
            [ngModel]="server().heap"
            (ngModelChange)="update('heap', $event)"
          /> </mat-form-field
        ><mat-form-field appearance="outline"
          ><mat-label>SSH configuration file</mat-label>
          <input
            matInput
            [ngModel]="server().configFile"
            (ngModelChange)="update('configFile', $event)"
          />
        </mat-form-field>
      </div>
      <p>Environment variables</p>
      @for (entry of environment(); track $index; let i = $index) {
        <div class="fields">
          <mat-form-field appearance="outline"
            ><mat-label>Variable name</mat-label>
            <input
              matInput
              [ngModel]="entry.key"
              (ngModelChange)="updateEnvironment(i, 'key', $event)"
            /> </mat-form-field
          ><mat-form-field appearance="outline"
            ><mat-label>Variable value</mat-label>
            <input
              matInput
              [ngModel]="entry.value"
              (ngModelChange)="updateEnvironment(i, 'value', $event)"
            /> </mat-form-field
          ><button mat-button (click)="removeEnvironment(i)">Remove variable</button>
        </div>
      }
      <button mat-button (click)="addEnvironment()">Add variable</button>
      <details>
        <summary>Import server JSON</summary>
        <textarea
          aria-label="Server JSON"
          rows="5"
          [ngModel]="json()"
          (ngModelChange)="json.set($event)"
        ></textarea>
        <button mat-button [disabled]="!json().trim()" (click)="importJson()">
          Apply JSON to form
        </button>
        @if (jsonError()) {
          <p role="alert">{{ jsonError() }}</p>
        }
      </details>
    </details>
    @if (validation()) {
      <p role="status">{{ validation() }}</p>
    }
    <button mat-flat-button [disabled]="busy() || !!validation()" (click)="save.emit(value())">
      Register server
    </button>
  `,
  styles: `
    :host {
      display: block;
    }
    .fields {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      align-items: start;
    }
    mat-form-field {
      flex: 1 1 200px;
    }
    details {
      margin: 16px 0;
    }
    summary {
      cursor: pointer;
    }
    textarea {
      display: block;
      width: 100%;
      box-sizing: border-box;
    }
  `,
})
export class AutoLlmServerEditor {
  readonly existingIds = input<string[]>([]);
  readonly busy = input(false);
  readonly save = output<AutoLlmServer>();
  readonly server = signal<AutoLlmServer>({
    id: '',
    host: '',
    workspace: '',
    javaExecutable: 'java',
    environment: {},
  });
  readonly environment = signal<{ key: string; value: string }[]>([]);
  readonly json = signal('');
  readonly jsonError = signal('');
  readonly fields = [
    { key: 'id', label: 'Server ID', placeholder: 'server-2', required: true },
    { key: 'host', label: 'SSH host', placeholder: 'user@host', required: true },
    {
      key: 'workspace',
      label: 'Remote workspace',
      placeholder: '/home/user/autollm',
      required: true,
    },
    { key: 'javaExecutable', label: 'Java executable', placeholder: 'java', required: true },
  ] as const;
  readonly value = computed<AutoLlmServer>(() => ({
    ...this.server(),
    id: this.server().id.trim(),
    host: this.server().host.trim(),
    workspace: this.server().workspace.trim(),
    javaExecutable: this.server().javaExecutable.trim(),
    environment: Object.fromEntries(
      this.environment().map((entry) => [entry.key.trim(), entry.value]),
    ),
  }));
  readonly validation = computed(() => {
    const server = this.value();
    if (!server.id || !server.host || !server.workspace || !server.javaExecutable)
      return 'Enter a server ID, SSH host, remote workspace and Java executable.';
    if (!/^[a-zA-Z0-9][a-zA-Z0-9._-]*$/.test(server.id))
      return 'Use letters, numbers, dots, underscores or hyphens for the server ID.';
    if (this.existingIds().includes(server.id))
      return 'This server ID is already enrolled. Choose a unique ID.';
    if (server.workers != null && (!Number.isInteger(server.workers) || server.workers < 1))
      return 'Workers must be a positive whole number.';
    const keys = this.environment().map((entry) => entry.key.trim());
    if (
      keys.some((key) => !/^[a-zA-Z_][a-zA-Z0-9_]*$/.test(key)) ||
      new Set(keys).size !== keys.length
    )
      return 'Environment variable names must be valid and unique.';
    return '';
  });
  update(key: keyof AutoLlmServer, value: string | number | null): void {
    this.server.update((server) => ({ ...server, [key]: value }));
  }
  addEnvironment(): void {
    this.environment.update((rows) => [...rows, { key: '', value: '' }]);
  }
  removeEnvironment(index: number): void {
    this.environment.update((rows) => rows.filter((_, i) => i !== index));
  }
  updateEnvironment(index: number, key: 'key' | 'value', value: string): void {
    this.environment.update((rows) =>
      rows.map((row, i) => (i === index ? { ...row, [key]: value } : row)),
    );
  }
  importJson(): void {
    try {
      const server = JSON.parse(this.json()) as AutoLlmServer;
      if (
        !server ||
        ['id', 'host', 'workspace', 'javaExecutable'].some(
          (key) => typeof server[key as keyof AutoLlmServer] !== 'string',
        ) ||
        !server.environment ||
        typeof server.environment !== 'object' ||
        Array.isArray(server.environment) ||
        Object.values(server.environment).some((value) => typeof value !== 'string')
      )
        throw new Error(
          'Provide string fields for id, host, workspace and javaExecutable, and an environment object of string values.',
        );
      this.server.set(server);
      this.environment.set(
        Object.entries(server.environment).map(([key, value]) => ({ key, value })),
      );
      this.jsonError.set('');
    } catch (error) {
      this.jsonError.set((error as Error).message);
    }
  }
}
