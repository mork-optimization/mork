import { normalDetail, normalResult } from '../testing/normal-experiment-fixture';
import { NormalChartUtil } from './normal-chart-util';
import type Highcharts from 'highcharts/esm/highcharts';

describe('Normal chart views', () => {
  it('retains string iteration labels, stable ordinals and explicit failure gaps', () => {
    const chart = NormalChartUtil.instance(normalDetail(), 'MINIMIZE');
    const series = chart.scoreChart.options.series![0] as Highcharts.SeriesLineOptions;
    expect(series.data).toEqual([
      expect.objectContaining({ x: 1, y: 5, custom: { iteration: 'repetition-one' } }),
      expect.objectContaining({ x: 2, y: null }),
    ]);
    expect(chart.bestSolution?.resultId).toBe('result-0');
  });
  it('recomputes best results and cumulative scores in both directions', () => {
    const detail = normalDetail('instance-0', 'Quality', {
      results: [
        normalResult(),
        normalResult({ resultId: 'better', ordinal: 2, objectives: { Cost: 2, Quality: 10 } }),
      ],
    });
    const max = NormalChartUtil.instance(detail, 'MAXIMIZE');
    expect(max.bestSolution?.resultId).toBe('better');
    expect((max.convergenceChart.options.series![0] as Highcharts.SeriesLineOptions).data).toEqual([
      expect.objectContaining({ y: 5 }),
      expect.objectContaining({ y: 10 }),
    ]);
    expect(
      NormalChartUtil.instance({ ...detail, objective: 'Cost' }, 'MINIMIZE').bestSolution?.resultId,
    ).toBe('better');
  });
  it('builds distribution and runtime views without failed score samples', () => {
    expect(
      (
        NormalChartUtil.distribution(normalDetail()).options
          .series![0] as Highcharts.SeriesBoxplotOptions
      ).type,
    ).toBe('boxplot');
    expect(
      (
        NormalChartUtil.qualityRuntime(normalDetail()).options
          .series![0] as Highcharts.SeriesScatterOptions
      ).data,
    ).toHaveLength(1);
  });
});
