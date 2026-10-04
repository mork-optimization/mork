import type Highcharts from 'highcharts/esm/highcharts';
import {
  EliteHistorySnapshot,
  EvaluationView,
  InstanceEvaluationSummary,
} from '../model/autoconfig';
import { ChartViewModel } from '../model/dashboard';

export class AutoconfigChartUtil {
  static evaluationCost(evaluations: readonly EvaluationView[]): ChartViewModel {
    const regular: Highcharts.PointOptionsObject[] = [];
    const slow: Highcharts.PointOptionsObject[] = [];
    for (const evaluation of evaluations) {
      if (
        evaluation.state !== 'SUCCEEDED' ||
        typeof evaluation.cost !== 'number' ||
        !Number.isFinite(evaluation.cost)
      ) {
        continue;
      }
      const point: Highcharts.PointOptionsObject = {
        x: evaluation.id,
        y: evaluation.cost,
        custom: {
          configurationId: evaluation.configurationId,
          instanceName: evaluation.instanceName,
          timeSeconds: evaluation.timeSeconds,
        },
      };
      (evaluation.slow ? slow : regular).push(point);
    }

    return {
      options: this.baseOptions(
        'Evaluation cost',
        'Successful IRACE costs. Lower is better.',
        {
          allowDecimals: false,
          title: { text: 'Evaluation ID' },
        },
        { title: { text: 'IRACE cost' } },
        [
          {
            type: 'scatter',
            name: 'Succeeded',
            data: regular,
            color: '#00aab5',
            marker: { radius: 2.5 },
            turboThreshold: 0,
          },
          {
            type: 'scatter',
            name: 'Slow',
            data: slow,
            color: '#ed8e00',
            marker: { radius: 3.5, symbol: 'diamond' },
            turboThreshold: 0,
          },
        ],
        '<b>{series.name}</b><br/>Evaluation: {point.x}<br/>Cost: {point.y}<br/>' +
          'Configuration: {point.custom.configurationId}<br/>Instance: {point.custom.instanceName}',
      ),
    };
  }

  static evaluationActivity(evaluations: readonly EvaluationView[]): ChartViewModel {
    const terminal = evaluations
      .filter((evaluation) => evaluation.finishedAt && evaluation.state !== 'RUNNING')
      .slice()
      .sort((left, right) => {
        const timeDifference = Date.parse(left.finishedAt!) - Date.parse(right.finishedAt!);
        return timeDifference === 0 ? left.id - right.id : timeDifference;
      });
    const counts = { SUCCEEDED: 0, REJECTED: 0, FAILED: 0 };
    const succeeded: Highcharts.PointOptionsObject[] = [];
    const rejected: Highcharts.PointOptionsObject[] = [];
    const failed: Highcharts.PointOptionsObject[] = [];
    for (const evaluation of terminal) {
      const state = evaluation.state;
      if (state !== 'SUCCEEDED' && state !== 'REJECTED' && state !== 'FAILED') {
        continue;
      }
      counts[state]++;
      const point = [Date.parse(evaluation.finishedAt!), counts[state]] as [number, number];
      if (state === 'SUCCEEDED') {
        succeeded.push(point);
      } else if (state === 'REJECTED') {
        rejected.push(point);
      } else {
        failed.push(point);
      }
    }

    return {
      options: this.baseOptions(
        'Evaluation activity',
        'Cumulative terminal evaluations over wall-clock time.',
        { type: 'datetime', title: { text: 'Completion time' } },
        { allowDecimals: false, min: 0, title: { text: 'Evaluations' } },
        [
          {
            type: 'line',
            name: 'Succeeded',
            data: succeeded,
            color: '#079447',
            marker: { enabled: false },
          },
          {
            type: 'line',
            name: 'Rejected',
            data: rejected,
            color: '#ed8e00',
            marker: { enabled: false },
          },
          {
            type: 'line',
            name: 'Failed',
            data: failed,
            color: '#d32f2f',
            marker: { enabled: false },
          },
        ],
        '<b>{series.name}</b><br/>{point.x:%Y-%m-%d %H:%M:%S}<br/>Count: {point.y}',
      ),
    };
  }

  static eliteRanks(history: EliteHistorySnapshot | null): ChartViewModel {
    const snapshots = history?.iterations ?? [];
    const configurationIds: string[] = [];
    for (const snapshot of snapshots) {
      for (const elite of snapshot.elites) {
        if (!configurationIds.includes(elite.configurationId)) {
          configurationIds.push(elite.configurationId);
        }
      }
    }

    const series: Highcharts.SeriesOptionsType[] = [];
    for (const configurationId of configurationIds) {
      const data: Array<[number, number | null]> = [];
      for (const snapshot of snapshots) {
        let rank: number | null = null;
        for (const elite of snapshot.elites) {
          if (elite.configurationId === configurationId) {
            rank = elite.position;
            break;
          }
        }
        data.push([snapshot.iteration, rank]);
      }
      series.push({
        type: 'line',
        name: configurationId,
        data,
        connectNulls: false,
        marker: { enabled: true, radius: 2.5 },
        step: 'left',
      });
    }

    return {
      options: this.baseOptions(
        'Elite rank evolution',
        'Elite positions at completed IRACE race checkpoints.',
        { allowDecimals: false, title: { text: 'Race checkpoint' } },
        {
          allowDecimals: false,
          min: 1,
          reversed: true,
          title: { text: 'Elite position' },
        },
        series,
        '<b>{series.name}</b><br/>Checkpoint: {point.x}<br/>Position: {point.y}',
      ),
    };
  }

  static instanceSummaries(
    evaluations: readonly EvaluationView[],
  ): readonly InstanceEvaluationSummary[] {
    const summaries = new Map<string, InstanceEvaluationSummary>();
    for (const evaluation of evaluations) {
      const current = summaries.get(evaluation.instanceName) ?? {
        instanceName: evaluation.instanceName,
        evaluations: 0,
        bestCost: null,
        issues: 0,
        slow: 0,
      };
      let bestCost = current.bestCost;
      if (
        evaluation.state === 'SUCCEEDED' &&
        typeof evaluation.cost === 'number' &&
        Number.isFinite(evaluation.cost) &&
        (bestCost === null || evaluation.cost < bestCost)
      ) {
        bestCost = evaluation.cost;
      }
      summaries.set(evaluation.instanceName, {
        instanceName: current.instanceName,
        evaluations: current.evaluations + 1,
        bestCost,
        issues:
          current.issues +
          (evaluation.state === 'REJECTED' || evaluation.state === 'FAILED' ? 1 : 0),
        slow: current.slow + (evaluation.slow ? 1 : 0),
      });
    }
    return [...summaries.values()].sort(
      (left, right) =>
        right.issues - left.issues || left.instanceName.localeCompare(right.instanceName),
    );
  }

  private static baseOptions(
    title: string,
    description: string,
    xAxis: Highcharts.XAxisOptions,
    yAxis: Highcharts.YAxisOptions,
    series: Highcharts.SeriesOptionsType[],
    pointFormat: string,
  ): Highcharts.Options {
    return {
      accessibility: { enabled: true, description },
      boost: { enabled: true, useGPUTranslations: true, seriesThreshold: 20 },
      chart: { animation: false, height: 330, spacing: [12, 12, 12, 12] },
      credits: { enabled: false },
      exporting: { enabled: true },
      legend: { enabled: true, maxHeight: 72 },
      title: { text: title, style: { display: 'none' } },
      xAxis,
      yAxis,
      tooltip: { headerFormat: '', pointFormat },
      plotOptions: { series: { animation: false, connectNulls: false } },
      series,
    };
  }
}
