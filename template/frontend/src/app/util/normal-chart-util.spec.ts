import { normalDetail, normalResult } from '../testing/normal-experiment-fixture';
import { NormalChartUtil } from './normal-chart-util';
import type Highcharts from 'highcharts/esm/highcharts';
import { ObjectiveMode } from '../model/Events';
import { InstanceDetail } from '../model/normal-experiment';

describe('Normal chart views', () => {
  function charts(detail: InstanceDetail, mode: ObjectiveMode, showReference = true) {
    const instance = NormalChartUtil.instance(detail, mode, showReference);
    return [
      instance.scoreChart,
      instance.convergenceChart,
      NormalChartUtil.distribution(detail, showReference),
      NormalChartUtil.qualityRuntime(detail, showReference),
    ];
  }

  it.each([
    ['Cost', 'MINIMIZE', 4],
    ['Quality', 'MAXIMIZE', 6],
  ] as const)('uses the %s reference in all four charts', (objective, mode, reference) => {
    for (const chart of charts(normalDetail('instance-0', objective), mode)) {
      const axis = chart.options.yAxis as Highcharts.YAxisOptions;
      expect(axis.plotLines).toEqual([
        expect.objectContaining({ value: reference, color: '#d32f2f', dashStyle: 'Dash' }),
      ]);
      expect(axis.softMin).toBe(reference);
      expect(axis.softMax).toBe(reference);
    }
  });

  it('hides the reference and releases the axis range without changing result series', () => {
    const detail = normalDetail();
    const visible = charts(detail, 'MINIMIZE');
    const hidden = charts(detail, 'MINIMIZE', false);
    for (let i = 0; i < hidden.length; i++) {
      const axis = hidden[i].options.yAxis as Highcharts.YAxisOptions;
      expect(axis.plotLines).toEqual([]);
      expect(axis.softMin).toBeUndefined();
      expect(axis.softMax).toBeUndefined();
      expect(hidden[i].options.series).toEqual(visible[i].options.series);
    }
    expect(NormalChartUtil.instance(detail, 'MINIMIZE', false).referenceValue).toBe(4);
  });

  it.each([undefined, Number.NaN, Number.POSITIVE_INFINITY, Number.NEGATIVE_INFINITY])(
    'omits missing or non-finite references (%s)',
    (reference) => {
      const detail = normalDetail();
      const withoutReference: InstanceDetail = {
        ...detail,
        instance: {
          ...detail.instance,
          references: reference === undefined ? { Quality: 6 } : { Cost: reference },
        },
      };
      expect(NormalChartUtil.instance(withoutReference, 'MINIMIZE').referenceValue).toBeNull();
      for (const chart of charts(withoutReference, 'MINIMIZE')) {
        const axis = chart.options.yAxis as Highcharts.YAxisOptions;
        expect(axis.plotLines).toEqual([]);
        expect(axis.softMin).toBeUndefined();
        expect(axis.softMax).toBeUndefined();
      }
    },
  );

  it.each([0, -3])('retains a reference of %s', (reference) => {
    const detail = normalDetail();
    const withReference = {
      ...detail,
      instance: { ...detail.instance, references: { Cost: reference } },
    };
    expect(NormalChartUtil.instance(withReference, 'MINIMIZE').referenceValue).toBe(reference);
    for (const chart of charts(withReference, 'MINIMIZE')) {
      const axis = chart.options.yAxis as Highcharts.YAxisOptions;
      expect(axis.plotLines?.[0].value).toBe(reference);
    }
  });

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
