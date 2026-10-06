import { Injectable, signal } from '@angular/core';

export type MonitorPanelId =
  | 'evaluation-health'
  | 'evaluation-cost'
  | 'elite-evolution'
  | 'current-elites'
  | 'run-information'
  | 'normal-comparison'
  | 'normal-scores'
  | 'normal-best'
  | 'normal-distribution'
  | 'normal-runtime';

export const TUNING_PANELS: readonly { readonly id: MonitorPanelId; readonly label: string }[] = [
  { id: 'evaluation-health', label: 'Evaluation health' },
  { id: 'evaluation-cost', label: 'Evaluation cost' },
  { id: 'elite-evolution', label: 'Elite rank evolution' },
  { id: 'current-elites', label: 'Current elites' },
  { id: 'run-information', label: 'Run information' },
];

export const STANDARD_PANELS: readonly { readonly id: MonitorPanelId; readonly label: string }[] = [
  { id: 'normal-comparison', label: 'Algorithm comparison' },
  { id: 'normal-scores', label: 'Scores' },
  { id: 'normal-best', label: 'Best score across repetitions' },
  { id: 'normal-distribution', label: 'Score distribution' },
  { id: 'normal-runtime', label: 'Score versus runtime' },
];

const STORAGE_KEY = 'mork.autoconfig.panelPreferences.v1';

interface StoredPreferences {
  readonly hidden: readonly MonitorPanelId[];
  readonly collapsed: readonly MonitorPanelId[];
}

@Injectable({ providedIn: 'root' })
export class PanelPreferences {
  private readonly hiddenSignal = signal<ReadonlySet<MonitorPanelId>>(new Set());
  private readonly collapsedSignal = signal<ReadonlySet<MonitorPanelId>>(new Set());

  readonly hidden = this.hiddenSignal.asReadonly();
  readonly collapsed = this.collapsedSignal.asReadonly();

  constructor() {
    this.restore();
  }

  isHidden(panelId: MonitorPanelId): boolean {
    return this.hiddenSignal().has(panelId);
  }

  isCollapsed(panelId: MonitorPanelId): boolean {
    return this.collapsedSignal().has(panelId);
  }

  show(panelId: MonitorPanelId): void {
    this.hiddenSignal.update((current) => this.without(current, panelId));
    this.persist();
  }

  hide(panelId: MonitorPanelId): void {
    this.hiddenSignal.update((current) => this.with(current, panelId));
    this.persist();
  }

  toggleCollapsed(panelId: MonitorPanelId): void {
    this.collapsedSignal.update((current) =>
      current.has(panelId) ? this.without(current, panelId) : this.with(current, panelId),
    );
    this.persist();
  }

  reset(): void {
    this.hiddenSignal.set(new Set());
    this.collapsedSignal.set(new Set());
    localStorage.removeItem(STORAGE_KEY);
  }

  private restore(): void {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) {
        return;
      }
      const stored = JSON.parse(raw) as Partial<StoredPreferences>;
      const validIds = new Set([...TUNING_PANELS, ...STANDARD_PANELS].map((panel) => panel.id));
      this.hiddenSignal.set(
        new Set((stored.hidden ?? []).filter((panelId) => validIds.has(panelId))),
      );
      this.collapsedSignal.set(
        new Set((stored.collapsed ?? []).filter((panelId) => validIds.has(panelId))),
      );
    } catch {
      localStorage.removeItem(STORAGE_KEY);
    }
  }

  private with(
    values: ReadonlySet<MonitorPanelId>,
    panelId: MonitorPanelId,
  ): ReadonlySet<MonitorPanelId> {
    const updated = new Set(values);
    updated.add(panelId);
    return updated;
  }

  private without(
    values: ReadonlySet<MonitorPanelId>,
    panelId: MonitorPanelId,
  ): ReadonlySet<MonitorPanelId> {
    const updated = new Set(values);
    updated.delete(panelId);
    return updated;
  }

  private persist(): void {
    const preferences: StoredPreferences = {
      hidden: [...this.hiddenSignal()],
      collapsed: [...this.collapsedSignal()],
    };
    localStorage.setItem(STORAGE_KEY, JSON.stringify(preferences));
  }
}
