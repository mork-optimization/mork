import { Injectable, signal } from '@angular/core';

export type TuningPanelId =
  | 'evaluation-health'
  | 'evaluation-cost'
  | 'elite-evolution'
  | 'current-elites'
  | 'run-information';

export const TUNING_PANELS: readonly { readonly id: TuningPanelId; readonly label: string }[] = [
  { id: 'evaluation-health', label: 'Evaluation health' },
  { id: 'evaluation-cost', label: 'Evaluation cost' },
  { id: 'elite-evolution', label: 'Elite rank evolution' },
  { id: 'current-elites', label: 'Current elites' },
  { id: 'run-information', label: 'Run information' },
];

const STORAGE_KEY = 'mork.autoconfig.panelPreferences.v1';

interface StoredPreferences {
  readonly hidden: readonly TuningPanelId[];
  readonly collapsed: readonly TuningPanelId[];
}

@Injectable({ providedIn: 'root' })
export class PanelPreferences {
  private readonly hiddenSignal = signal<ReadonlySet<TuningPanelId>>(new Set());
  private readonly collapsedSignal = signal<ReadonlySet<TuningPanelId>>(new Set());

  readonly hidden = this.hiddenSignal.asReadonly();
  readonly collapsed = this.collapsedSignal.asReadonly();

  constructor() {
    this.restore();
  }

  isHidden(panelId: TuningPanelId): boolean {
    return this.hiddenSignal().has(panelId);
  }

  isCollapsed(panelId: TuningPanelId): boolean {
    return this.collapsedSignal().has(panelId);
  }

  show(panelId: TuningPanelId): void {
    this.hiddenSignal.update((current) => this.without(current, panelId));
    this.persist();
  }

  hide(panelId: TuningPanelId): void {
    this.hiddenSignal.update((current) => this.with(current, panelId));
    this.persist();
  }

  toggleCollapsed(panelId: TuningPanelId): void {
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
      const validIds = new Set(TUNING_PANELS.map((panel) => panel.id));
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
    values: ReadonlySet<TuningPanelId>,
    panelId: TuningPanelId,
  ): ReadonlySet<TuningPanelId> {
    const updated = new Set(values);
    updated.add(panelId);
    return updated;
  }

  private without(
    values: ReadonlySet<TuningPanelId>,
    panelId: TuningPanelId,
  ): ReadonlySet<TuningPanelId> {
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
