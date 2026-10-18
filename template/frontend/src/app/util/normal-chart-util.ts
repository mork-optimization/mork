import { ObjectiveMode, SolutionGeneratedEvent } from '../model/Events';
import type {} from 'highcharts/options/boxplot';
import { ChartViewModel, InstanceDashboardModel } from '../model/dashboard';
import { InstanceDetail } from '../model/normal-experiment';
import { ChartUtil } from './chart-util';

export class NormalChartUtil {
  static instance(
    detail: InstanceDetail,
    mode: ObjectiveMode,
    showReference = true,
  ): InstanceDashboardModel {
    return ChartUtil.buildInstance(
      {
        key: detail.instance.id,
        started: {
          experimentName: detail.instance.experimentName,
          instanceName: detail.instance.instanceName,
          algorithms: detail.algorithms.map((a) => a.name),
          repetitions: detail.instance.counts.planned,
          refValues: detail.instance.references,
        },
        solutions: detail.results.map((result) => ({
          ordinal: result.ordinal,
          event: {
            resultId: result.resultId,
            success: result.state === 'SUCCEEDED',
            experimentName: result.experimentName,
            instanceName: result.instanceName,
            algorithmName: result.algorithmName,
            iteration: result.iteration,
            objectives: result.objectives,
            score: result.objectives[detail.objective] ?? Number.NaN,
            executionTime: result.executionTime ?? -1,
            timeToBest: result.timeToBest ?? -1,
          } satisfies SolutionGeneratedEvent,
        })),
      },
      detail.objective,
      mode,
      showReference,
    );
  }
  static distribution(detail: InstanceDetail, showReference = true): ChartViewModel {
    return {
      options: {
        chart: { type: 'boxplot', height: 320, animation: false },
        plotOptions: { series: { animation: false } },
        title: { text: 'Score distribution' },
        credits: { enabled: false },
        exporting: { enabled: true },
        accessibility: { enabled: true },
        xAxis: { categories: detail.algorithms.map((a) => a.name) },
        yAxis: ChartUtil.scoreAxis(
          detail.objective,
          ChartUtil.referenceValue(detail.instance.references, detail.objective),
          showReference,
        ),
        series: [
          {
            type: 'boxplot',
            name: detail.objective,
            data: detail.algorithms.flatMap((a, x) => {
              const stats = a.objective;
              return stats.best === null || stats.worst === null
                ? []
                : [
                    {
                      x,
                      low: Math.min(stats.best, stats.worst),
                      q1: stats.lowerQuartile!,
                      median: stats.median!,
                      q3: stats.upperQuartile!,
                      high: Math.max(stats.best, stats.worst),
                    },
                  ];
            }),
          },
        ],
      },
    };
  }
  static qualityRuntime(detail: InstanceDetail, showReference = true): ChartViewModel {
    return {
      options: {
        chart: { type: 'scatter', height: 320, animation: false },
        plotOptions: { series: { animation: false } },
        title: { text: 'Score versus runtime' },
        credits: { enabled: false },
        exporting: { enabled: true },
        accessibility: { enabled: true },
        xAxis: { title: { text: 'Runtime (ms)' } },
        yAxis: ChartUtil.scoreAxis(
          detail.objective,
          ChartUtil.referenceValue(detail.instance.references, detail.objective),
          showReference,
        ),
        tooltip: {
          pointFormat:
            'Runtime: {point.x} ms<br/>Score: {point.y}<br/>Iteration: {point.custom.iteration}',
        },
        series: detail.algorithms.map((a) => ({
          type: 'scatter',
          name: a.name,
          data: detail.results
            .filter(
              (r) =>
                r.algorithmName === a.name &&
                r.state === 'SUCCEEDED' &&
                r.executionTime !== null &&
                Number.isFinite(r.objectives[detail.objective]),
            )
            .map((r) => ({
              x: r.executionTime! / 1000000,
              y: r.objectives[detail.objective],
              custom: { iteration: r.iteration },
            })),
        })),
      },
    };
  }
}
