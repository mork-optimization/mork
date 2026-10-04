import { ChangeDetectionStrategy, Component, inject, input } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatMenuModule } from '@angular/material/menu';
import { PanelPreferences, TuningPanelId } from '../../store/panel-preferences';

@Component({
  selector: 'app-monitor-panel',
  imports: [MatButtonModule, MatCardModule, MatMenuModule],
  template: `
    @if (!preferences.isHidden(panelId())) {
      <mat-card appearance="outlined">
        <mat-card-header>
          <mat-card-title>{{ title() }}</mat-card-title>
          <span class="spacer"></span>
          <button
            mat-button
            class="collapse"
            type="button"
            [attr.aria-label]="
              preferences.isCollapsed(panelId()) ? 'Expand ' + title() : 'Collapse ' + title()
            "
            (click)="preferences.toggleCollapsed(panelId())"
          >
            {{ preferences.isCollapsed(panelId()) ? '+' : '−' }}
          </button>
          <button
            mat-button
            class="menu"
            type="button"
            [matMenuTriggerFor]="panelMenu"
            [attr.aria-label]="title() + ' options'"
          >
            ⋮
          </button>
          <mat-menu #panelMenu="matMenu">
            <button mat-menu-item type="button" (click)="preferences.toggleCollapsed(panelId())">
              {{ preferences.isCollapsed(panelId()) ? 'Expand panel' : 'Collapse panel' }}
            </button>
            <button mat-menu-item type="button" (click)="preferences.hide(panelId())">
              Hide panel
            </button>
          </mat-menu>
        </mat-card-header>
        @if (!preferences.isCollapsed(panelId())) {
          <mat-card-content>
            <ng-content />
          </mat-card-content>
        }
      </mat-card>
    }
  `,
  styles: `
    :host {
      display: block;
      min-width: 0;
    }

    mat-card {
      height: 100%;
    }

    mat-card-header {
      min-height: 3.5rem;
      align-items: center;
      padding-bottom: 0.5rem;
    }

    mat-card-title {
      font: var(--mat-sys-title-medium);
      font-weight: 650;
    }

    mat-card-content {
      min-width: 0;
    }

    .spacer {
      flex: 1;
    }

    .collapse,
    .menu {
      min-width: 2.25rem;
      padding: 0;
      font-size: 1.25rem;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MonitorPanel {
  readonly preferences = inject(PanelPreferences);
  readonly panelId = input.required<TuningPanelId>();
  readonly title = input.required<string>();
}
