import type Highcharts from 'highcharts/esm/highcharts';
import { ChartViewModel } from '../model/dashboard';

export interface ComparisonCurveRow {
  instance: string;
  instanceId: string;
  algorithm: string;
  iteration: string;
  curves: Record<string, { instant: number; value: number }[]>;
}

export type ComparisonYAxisScale = 'linear' | 'logarithmic';

export class ComparisonChartUtil {
  private static readonly palettes = [
    ['#1565c0', '#1976d2', '#1e88e5', '#0277bd', '#039be5', '#0288d1'],
    ['#ef6c00', '#f4511e', '#e64a19', '#e53935', '#d84315', '#c62828'],
    ['#2e7d32', '#388e3c', '#00897b', '#00796b'],
    ['#7b1fa2', '#8e24aa', '#6a1b9a', '#5e35b1'],
  ];

  static canUseLogarithmicScale(rows: readonly ComparisonCurveRow[], objective: string | undefined): boolean {
    if (!objective) return false;
    let hasPoints = false;
    for (const row of rows) {
      for (const point of row.curves[objective] ?? []) {
        if (!Number.isFinite(point.value) || !Number.isFinite(point.instant)) continue;
        if (point.value <= 0) return false;
        hasPoints = true;
      }
    }
    return hasPoints;
  }

  static build(rows: readonly ComparisonCurveRow[], objective: string | undefined, scale: ComparisonYAxisScale): ChartViewModel {
    const algorithms = [...new Set(['SOTA', 'IraceProposal', ...rows.map(row => row.algorithm)])];
    const series: Highcharts.SeriesLineOptions[] = [];
    for (let index = 0; index < algorithms.length; index++) {
      const algorithm = algorithms[index];
      const palette = this.palettes[index % this.palettes.length];
      const parentId = `comparison-${encodeURIComponent(algorithm)}`;
      const repetitions = rows.filter(row => row.algorithm === algorithm).sort((a, b) => a.iteration.localeCompare(b.iteration, undefined, { numeric: true }));
      let hasParent = false;
      for (const row of repetitions) {
        const data = (row.curves[objective ?? ''] ?? [])
          .filter(point => Number.isFinite(point.value) && Number.isFinite(point.instant))
          .map(point => [point.instant / 1_000_000, point.value]);
        if (!data.length) continue;
        const iteration = Number(row.iteration);
        const repetition = Number.isFinite(iteration) ? iteration + 1 : row.iteration;
        const shade = Number.isInteger(iteration) && iteration >= 0 ? iteration : 0;
        series.push({
          type: 'line',
          id: hasParent ? `${parentId}-repetition-${row.iteration}` : parentId,
          linkedTo: hasParent ? parentId : undefined,
          showInLegend: !hasParent,
          name: `${algorithm} · repetition ${repetition}`,
          custom: { algorithm },
          color: palette[shade % palette.length],
          dashStyle: 'Solid',
          opacity: 0.45,
          lineWidth: 1.5,
          marker: { enabled: false },
          states: { hover: { lineWidthPlus: 1.5, opacity: 0.85 }, inactive: { opacity: 0.15 } },
          data,
        });
        hasParent = true;
      }
    }
    const yAxisType = scale === 'logarithmic' && this.canUseLogarithmicScale(rows, objective) ? 'logarithmic' : 'linear';
    return {
      viewKey: JSON.stringify([rows[0]?.instance, objective, yAxisType]),
      options: {
        boost: { enabled: false },
        chart: { type: 'line', animation: false, reflow: true, zooming: { type: 'xy', pinchType: 'xy' }, panning: { enabled: true, type: 'xy' }, panKey: 'shift' },
        title: { text: objective ?? 'Comparison convergence' },
        xAxis: { title: { text: 'Time (milliseconds)' }, minRange: 0 },
        yAxis: { title: { text: 'Objective value' }, type: yAxisType, minRange: 0, startOnTick: false, endOnTick: false },
        legend: {
          maxHeight: 64,
          symbolWidth: 32,
          labelFormatter() {
            const algorithm = this.options.custom?.['algorithm'];
            return typeof algorithm === 'string' ? algorithm : this.name ?? '';
          },
        },
        plotOptions: { series: { animation: false } },
        series,
      },
    };
  }
}
