import type Highcharts from 'highcharts/esm/highcharts';
import { ChartViewModel, InstanceDashboardModel } from '../model/dashboard';
import { InstanceAccumulator, StoredSolution } from '../model/internal-dashboard';
import { ObjectiveMode, SolutionGeneratedEvent } from '../model/Events';

export class ChartUtil {
  static buildInstance(
    instance: InstanceAccumulator,
    objectiveName: string | null,
    objectiveMode: ObjectiveMode | null,
    showReference = true,
  ): InstanceDashboardModel {
    const referenceValue = this.referenceValue(instance.started.refValues, objectiveName);

    return {
      key: instance.key,
      experimentName: instance.started.experimentName,
      instanceName: instance.started.instanceName,
      selectedObjective: objectiveName,
      referenceValue,
      scoreChart: this.buildScoreChart(instance, objectiveName, referenceValue, showReference),
      convergenceChart: this.buildConvergenceChart(
        instance,
        objectiveName,
        objectiveMode,
        referenceValue,
        showReference,
      ),
      bestSolution: this.findBest(instance.solutions, objectiveName, objectiveMode),
    };
  }

  private static buildScoreChart(
    instance: InstanceAccumulator,
    objectiveName: string | null,
    referenceValue: number | null,
    showReference: boolean,
  ): ChartViewModel {
    const points = this.groupSuccessfulPoints(instance, objectiveName, false, null);
    return {
      options: this.chartOptions(
        `${instance.started.instanceName} scores`,
        objectiveName,
        referenceValue,
        points,
        'Raw score for every successful repetition.',
        showReference,
      ),
    };
  }

  private static buildConvergenceChart(
    instance: InstanceAccumulator,
    objectiveName: string | null,
    objectiveMode: ObjectiveMode | null,
    referenceValue: number | null,
    showReference: boolean,
  ): ChartViewModel {
    const points = this.groupSuccessfulPoints(instance, objectiveName, true, objectiveMode);
    return {
      options: this.chartOptions(
        `${instance.started.instanceName} best score across repetitions`,
        objectiveName,
        referenceValue,
        points,
        'Best score reached by each algorithm over completed repetitions.',
        showReference,
      ),
    };
  }

  private static groupSuccessfulPoints(
    instance: InstanceAccumulator,
    objectiveName: string | null,
    convergence: boolean,
    objectiveMode: ObjectiveMode | null,
  ): Highcharts.SeriesOptionsType[] {
    if (!objectiveName) {
      return [];
    }

    const algorithmNames = [...instance.started.algorithms];
    for (const stored of instance.solutions) {
      if (!algorithmNames.includes(stored.event.algorithmName)) {
        algorithmNames.push(stored.event.algorithmName);
      }
    }

    const series: Highcharts.SeriesOptionsType[] = [];
    for (const algorithmName of algorithmNames) {
      const data: Highcharts.PointOptionsObject[] = [];
      let best: number | null = null;

      for (const stored of instance.solutions) {
        const event = stored.event;
        if (event.algorithmName !== algorithmName) {
          continue;
        }
        const value = event.success ? this.objectiveValue(event, objectiveName) : null;
        if (value === null) {
          data.push({ x: stored.ordinal, y: null, custom: { iteration: event.iteration } });
          continue;
        }
        if (convergence) {
          best = best === null || this.isBetter(value, best, objectiveMode) ? value : best;
        }
        data.push({
          x: stored.ordinal,
          y: convergence ? best : value,
          custom: { iteration: event.iteration },
        });
      }

      series.push({
        type: 'line',
        name: algorithmName,
        data,
        lineWidth: convergence ? 2 : 1,
        marker: { enabled: !convergence },
        animation: false,
        turboThreshold: 0,
      });
    }
    return series;
  }

  static referenceValue(
    references: Readonly<Record<string, number>>,
    objectiveName: string | null,
  ): number | null {
    const value = objectiveName ? references[objectiveName] : undefined;
    return typeof value === 'number' && Number.isFinite(value) ? value : null;
  }

  static scoreAxis(
    objectiveName: string | null,
    referenceValue: number | null,
    showReference: boolean,
  ): Highcharts.YAxisOptions {
    const visibleReference = showReference ? referenceValue : null;
    const plotLines: Highcharts.YAxisPlotLinesOptions[] = [];
    if (visibleReference !== null) {
      plotLines.push({
        id: 'reference',
        className: 'reference-line',
        value: visibleReference,
        color: '#d32f2f',
        dashStyle: 'Dash',
        width: 2,
        zIndex: 4,
        label: { text: `Reference: ${visibleReference}` },
      });
    }

    return {
      title: { text: objectiveName ?? 'Score' },
      plotLines,
      softMin: visibleReference ?? undefined,
      softMax: visibleReference ?? undefined,
    };
  }

  private static chartOptions(
    title: string,
    objectiveName: string | null,
    referenceValue: number | null,
    series: Highcharts.SeriesOptionsType[],
    accessibilityDescription: string,
    showReference: boolean,
  ): Highcharts.Options {
    return {
      accessibility: { enabled: true, description: accessibilityDescription },
      boost: { enabled: true, useGPUTranslations: true, seriesThreshold: 25 },
      chart: { animation: false, height: 320, spacing: [12, 12, 12, 12] },
      credits: { enabled: false },
      exporting: { enabled: true },
      legend: { enabled: true },
      title: { text: title, style: { display: 'none' } },
      subtitle: {
        text: objectiveName ? `Objective: ${objectiveName}` : 'Waiting for objective metadata',
      },
      xAxis: {
        allowDecimals: false,
        min: 1,
        title: { text: 'Repetition' },
      },
      yAxis: this.scoreAxis(objectiveName, referenceValue, showReference),
      tooltip: {
        headerFormat: '',
        pointFormat:
          '<b>{series.name}</b><br/>Repetition: {point.x}<br/>Iteration: {point.custom.iteration}<br/>Value: {point.y}',
      },
      plotOptions: {
        series: {
          animation: false,
          connectNulls: false,
        },
      },
      series,
    };
  }

  private static findBest(
    solutions: readonly StoredSolution[],
    objectiveName: string | null,
    objectiveMode: ObjectiveMode | null,
  ): SolutionGeneratedEvent | null {
    if (!objectiveName || !objectiveMode) {
      return null;
    }

    let bestEvent: SolutionGeneratedEvent | null = null;
    let bestValue: number | null = null;
    for (const stored of solutions) {
      if (!stored.event.success) {
        continue;
      }
      const value = this.objectiveValue(stored.event, objectiveName);
      if (value === null) {
        continue;
      }
      if (bestValue === null || this.isBetter(value, bestValue, objectiveMode)) {
        bestValue = value;
        bestEvent = stored.event;
      }
    }
    return bestEvent;
  }

  private static objectiveValue(
    event: SolutionGeneratedEvent,
    objectiveName: string,
  ): number | null {
    const value = event.objectives[objectiveName];
    return typeof value === 'number' && Number.isFinite(value) ? value : null;
  }

  private static isBetter(
    candidate: number,
    current: number,
    objectiveMode: ObjectiveMode | null,
  ): boolean {
    return objectiveMode === 'MAXIMIZE' ? candidate > current : candidate < current;
  }
}
