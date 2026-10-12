import { JsonPipe, DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { AutoLlmStore } from '../../store/autollm-store';
import { AutoLlmConfig } from '../../model/autollm';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';
import { ChartViewModel } from '../../model/dashboard';
type CurveRow = { instance: string; instanceId: string; algorithm: string; iteration: string; curves: Record<string, { instant: number; value: number }[]> };

@Component({
  selector: 'app-autollm-dashboard',
  imports: [JsonPipe, DatePipe, DecimalPipe, MatButtonModule, MatCardModule, MatProgressBarModule, DashboardChart],
  templateUrl: './autollm-dashboard.html', styleUrl: './autollm-dashboard.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AutoLlmDashboard {
  readonly store = inject(AutoLlmStore);
  readonly selectedInstance = signal('');
  readonly selectedObjective = signal('');
  readonly curves = computed(() => (this.store.comparison()?.['curves'] ?? []) as CurveRow[]);
  readonly instances = computed(() => [...new Set(this.curves().map(row => row.instance))]);
  readonly objectives = computed(() => [...new Set(this.curves().flatMap(row => Object.keys(row.curves)))]);
  readonly comparisonSummary = computed(() => {
    const { curves, ...summary } = this.store.comparison() ?? {};
    return summary;
  });
  readonly curveChart = computed<ChartViewModel>(() => {
    const instance = this.instances().includes(this.selectedInstance()) ? this.selectedInstance() : this.instances()[0];
    const objective = this.objectives().includes(this.selectedObjective()) ? this.selectedObjective() : this.objectives()[0];
    return { options: { chart: { type: 'line', height: 360 }, title: { text: objective ?? 'Comparison convergence' }, xAxis: { title: { text: 'Time (milliseconds)' } }, yAxis: { title: { text: 'Objective value' } },
      series: this.curves().filter(row => row.instance === instance).map(row => ({ type: 'line' as const, name: `${row.algorithm} · repetition ${Number(row.iteration) + 1}`, data: (row.curves[objective] ?? []).filter(point => Number.isFinite(point.value)).map(point => [point.instant / 1_000_000, point.value]) })) } };
  });
  constructor() { void this.store.start(); }
  update(path: string, event: Event, numeric = false): void {
    const current = this.store.config(); if (!current) return;
    const config = structuredClone(current);
    let target = config as unknown as Record<string, unknown>;
    const keys = path.split('.');
    for (const key of keys.slice(0, -1)) target = target[key] as Record<string, unknown>;
    const text = (event.target as HTMLInputElement).value;
    target[keys[keys.length - 1]] = numeric ? Number(text) : text || null;
    this.store.config.set(config);
  }
  buildCommand(event: Event): void {
    try {
      const command: unknown = JSON.parse((event.target as HTMLInputElement).value);
      if (!Array.isArray(command) || command.some(value => typeof value !== 'string')) throw new Error('Build command must be a JSON array of arguments');
      this.store.config.update(c => c ? { ...c, project: { ...c.project, buildCommand: command as string[] } } : c);
    } catch (e) { this.store.error.set((e as Error).message); }
  }
  advanced(event: Event): void {
    try {
      const current = this.store.config(); if (!current) return;
      const parsed = JSON.parse((event.target as HTMLTextAreaElement).value) as Partial<AutoLlmConfig>;
      if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) throw new Error('Settings must be a JSON object');
      for (const key of ['project', 'codex', 'preparation', 'tuning', 'comparison', 'execution'] as const) {
        if (parsed[key] != null && (typeof parsed[key] !== 'object' || Array.isArray(parsed[key]))) throw new Error(`${key} must be a JSON object`);
      }
      this.store.config.set({ ...current, ...parsed, project: { ...current.project, ...parsed.project }, codex: { ...current.codex, ...parsed.codex },
        preparation: { ...current.preparation, ...parsed.preparation }, tuning: { ...current.tuning, ...parsed.tuning }, comparison: { ...current.comparison, ...parsed.comparison }, execution: { ...current.execution, ...parsed.execution } });
      this.store.error.set(null);
    } catch (error) { this.store.error.set((error as Error).message); }
  }
}
