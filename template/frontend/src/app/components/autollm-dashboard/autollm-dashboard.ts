import { JsonPipe, DatePipe, DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, effect, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { AutoLlmStore } from '../../store/autollm-store';
import { AutoLlmConfig, AutoLlmServer } from '../../model/autollm';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';
import { ChartViewModel } from '../../model/dashboard';
import { ComparisonChartUtil, ComparisonCurveRow, ComparisonYAxisScale } from '../../util/comparison-chart-util';
import { InstanceLabelUtil } from '../../util/instance-label-util';
import { FilePreviewUtil } from '../../util/file-preview-util';

@Component({
  selector: 'app-autollm-dashboard',
  imports: [JsonPipe, DatePipe, DecimalPipe, MatButtonModule, MatCardModule, MatProgressBarModule, DashboardChart],
  templateUrl: './autollm-dashboard.html', styleUrl: './autollm-dashboard.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AutoLlmDashboard {
  readonly canPreviewFile = FilePreviewUtil.canPreview;
  readonly store = inject(AutoLlmStore);
  readonly selectedInstance = signal('');
  readonly failureReason = signal('');
  readonly selectedObjective = signal('');
  readonly yAxisScale = signal<ComparisonYAxisScale>('linear');
  readonly chartHeight = signal(520);
  readonly executionChoice = signal('');
  readonly newTarget = signal('{"id":"server-2","host":"user@host","workspace":"/home/user/autollm","javaExecutable":"java","environment":{}}');
  readonly originalPool = signal('');
  readonly trainingFraction = signal(0.15);
  readonly curves = computed(() => (this.store.comparison()?.['curves'] ?? []) as ComparisonCurveRow[]);
  readonly instances = computed(() => [...new Set(this.curves().map(row => row.instance))]);
  readonly objectives = computed(() => [...new Set(this.curves().flatMap(row => Object.keys(row.curves)))]);
  readonly instanceLabels = computed(() => InstanceLabelUtil.relativeLabels(this.instances()));
  readonly activeInstance = computed(() => this.instances().includes(this.selectedInstance()) ? this.selectedInstance() : this.instances()[0]);
  readonly activeObjective = computed(() => this.objectives().includes(this.selectedObjective()) ? this.selectedObjective() : this.objectives()[0]);
  readonly selectedCurves = computed(() => this.curves().filter(row => row.instance === this.activeInstance()));
  readonly canLogYAxis = computed(() => ComparisonChartUtil.canUseLogarithmicScale(this.selectedCurves(), this.activeObjective()));
  readonly effectiveYAxisScale = computed(() => this.yAxisScale() === 'logarithmic' && this.canLogYAxis() ? 'logarithmic' : 'linear');
  readonly comparisonSummary = computed(() => {
    const { curves, ...summary } = this.store.comparison() ?? {};
    return summary;
  });
  readonly curveChart = computed<ChartViewModel>(() => ComparisonChartUtil.build(this.selectedCurves(), this.activeObjective(), this.effectiveYAxisScale()));
  constructor() {
    effect(() => { this.store.selectedId(); this.store.selectedTrial(); this.failureReason.set(''); });
    void this.store.start();
  }
  executionTarget(event: Event): void {
    const value = (event.target as HTMLSelectElement).value as 'LOCAL' | 'SSH';
    this.executionChoice.set(value);
    this.store.config.update(config => config ? { ...config, execution: { ...config.execution, target: value,
      ssh: config.execution.ssh ?? { screeningServerId: 'server-1', runnerArtifact: null, servers: [{ id: 'server-1', host: '', workspace: '', javaExecutable: 'java', environment: {} }] } } } : config);
    this.store.targetStatus.set(null);
  }
  addServer(): void {
    this.store.config.update(config => {
      if (!config?.execution.ssh) return config;
      const servers = [...config.execution.ssh.servers, { id: `server-${config.execution.ssh.servers.length + 1}`, host: '', workspace: '', javaExecutable: 'java', environment: {} }];
      return { ...config, execution: { ...config.execution, ssh: { ...config.execution.ssh, servers } } };
    });
  }
  removeServer(index: number): void {
    this.store.config.update(config => config?.execution.ssh ? { ...config, execution: { ...config.execution, ssh: { ...config.execution.ssh, servers: config.execution.ssh.servers.filter((_, i) => i !== index) } } } : config);
  }
  enrollTarget(): void {
    try {
      const server = JSON.parse(this.newTarget()) as AutoLlmServer;
      if (!server?.id || !server.host || !server.workspace) throw new Error('Provide server id, host, and workspace');
      void this.store.addTarget(server);
    } catch (error) { this.store.error.set((error as Error).message); }
  }
  workerMode(event: Event): void {
    const automatic = (event.target as HTMLSelectElement).value === 'AUTO';
    this.store.config.update(config => config ? { ...config, execution: { ...config.execution, workers: automatic ? -1 : 1 } } : config);
  }
  update(path: string, event: Event, numeric = false): void {
    const current = this.store.config(); if (!current) return;
    const config = structuredClone(current);
    let target = config as unknown as Record<string, unknown>;
    const keys = path.split('.');
    for (const key of keys.slice(0, -1)) {
      if (!target[key] || typeof target[key] !== 'object') target[key] = {};
      target = target[key] as Record<string, unknown>;
    }
    const text = (event.target as HTMLInputElement).value;
    target[keys[keys.length - 1]] = numeric ? (text ? Number(text) : null) : text || null;
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
      const ssh = parsed.execution?.ssh;
      if (ssh != null && (typeof ssh !== 'object' || Array.isArray(ssh))) throw new Error('execution.ssh must be a JSON object');
      this.store.config.set({ ...current, ...parsed, project: { ...current.project, ...parsed.project }, codex: { ...current.codex, ...parsed.codex },
        preparation: { ...current.preparation, ...parsed.preparation }, tuning: { ...current.tuning, ...parsed.tuning }, comparison: { ...current.comparison, ...parsed.comparison },
        execution: { ...current.execution, ...parsed.execution, ssh: ssh === null ? null : current.execution.ssh || ssh ? { screeningServerId: '', runnerArtifact: null, servers: [], ...current.execution.ssh, ...ssh } : null } });
      if (parsed.execution?.target) this.executionChoice.set(parsed.execution.target);
      this.store.error.set(null);
    } catch (error) { this.store.error.set((error as Error).message); }
  }
}
