import type Highcharts from 'highcharts/esm/highcharts';
import { ComparisonChartUtil, ComparisonCurveRow } from './comparison-chart-util';

function row(algorithm = 'SOTA', iteration = '0', value = 865): ComparisonCurveRow {
  return { instance: '/work/instance.txt', instanceId: 'instance', algorithm, iteration, curves: { Cost: [{ instant: 1_000_000, value }] } };
}

describe('ComparisonChartUtil', () => {
  it('retains all 60 repetitions with translucent solid lines and two linked legend groups', () => {
    const rows: ComparisonCurveRow[] = [];
    for (let iteration = 29; iteration >= 0; iteration--) {
      rows.push(row('IraceProposal', String(iteration)), row('SOTA', String(iteration)));
    }
    const options = ComparisonChartUtil.build(rows, 'Cost', 'linear').options;
    expect(options.boost?.enabled).toBe(false);
    const series = options.series as Highcharts.SeriesLineOptions[];
    expect(series).toHaveLength(60);
    const parents = series.filter(item => item.showInLegend);
    expect(parents).toHaveLength(2);
    for (const algorithm of ['SOTA', 'IraceProposal']) {
      const group = series.filter(item => item.custom?.['algorithm'] === algorithm);
      expect(group[0].showInLegend).toBe(true);
      expect(group[0].name).toBe(`${algorithm} · repetition 1`);
      for (const child of group.slice(1)) expect(child.linkedTo).toBe(group[0].id);
      expect(new Set(group.map(item => item.color)).size).toBeGreaterThan(1);
      for (const item of group) {
        const color = item.color as string;
        const red = parseInt(color.slice(1, 3), 16), blue = parseInt(color.slice(5, 7), 16);
        expect(algorithm === 'SOTA' ? blue > red : red > blue).toBe(true);
        expect(item.dashStyle).toBe('Solid');
        expect(item.opacity).toBeGreaterThan(0); expect(item.opacity).toBeLessThan(1);
      }
    }
  });

  it('keeps repetition colors stable as results arrive in a different order', () => {
    const a = row('SOTA', '3'), b = row('SOTA', '1');
    const first = ComparisonChartUtil.build([a, b], 'Cost', 'linear').options.series;
    const second = ComparisonChartUtil.build([b, a], 'Cost', 'linear').options.series;
    expect(first).toEqual(second);
  });

  it('uses a logarithmic axis for positive values and preserves relative milliseconds', () => {
    const options = ComparisonChartUtil.build([row()], 'Cost', 'logarithmic').options;
    expect((options.yAxis as Highcharts.YAxisOptions).type).toBe('logarithmic');
    expect((options.series![0] as Highcharts.SeriesLineOptions).data).toEqual([[1, 865]]);
  });

  it.each([0, -10])('uses a linear axis without losing a nonpositive score: %s', value => {
    const rows = [row('SOTA', '0', value), row('IraceProposal')];
    const options = ComparisonChartUtil.build(rows, 'Cost', 'logarithmic').options;
    expect(ComparisonChartUtil.canUseLogarithmicScale(rows, 'Cost')).toBe(false);
    expect((options.yAxis as Highcharts.YAxisOptions).type).toBe('linear');
    expect((options.series![0] as Highcharts.SeriesLineOptions).data).toEqual([[1, value]]);
  });

  it('does not offer logarithmic scale when no finite points are available', () => {
    expect(ComparisonChartUtil.canUseLogarithmicScale([row('SOTA', '0', Infinity)], 'Cost')).toBe(false);
    expect(ComparisonChartUtil.canUseLogarithmicScale([row()], 'Unknown')).toBe(false);
  });
});
