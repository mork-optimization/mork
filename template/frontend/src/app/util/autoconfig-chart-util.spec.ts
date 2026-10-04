import type Highcharts from 'highcharts/esm/highcharts';
import { EliteHistorySnapshot, EvaluationView } from '../model/autoconfig';
import { AutoconfigChartUtil } from './autoconfig-chart-util';

function evaluation(overrides: Partial<EvaluationView>): EvaluationView {
  return {
    id: 1,
    configurationId: '7',
    instanceId: 'instance-1',
    instanceName: 'one.dat',
    seed: 1,
    state: 'SUCCEEDED',
    cost: 10,
    timeSeconds: 1,
    startedAt: '2026-10-04T00:00:00Z',
    finishedAt: '2026-10-04T00:00:01Z',
    reasonCode: null,
    message: null,
    slow: false,
    slowOverrunMillis: null,
    ...overrides,
  };
}

describe('AutoconfigChartUtil', () => {
  it('charts only successful costs and separates slow evaluations', () => {
    const model = AutoconfigChartUtil.evaluationCost([
      evaluation({ id: 1, cost: 5 }),
      evaluation({ id: 2, cost: 4, slow: true }),
      evaluation({ id: 3, state: 'REJECTED', cost: null }),
      evaluation({ id: 4, state: 'FAILED', cost: null }),
    ]);
    const series = model.options.series as Highcharts.SeriesScatterOptions[];

    expect(series[0].data).toHaveLength(1);
    expect(series[1].data).toHaveLength(1);
    expect(series[0].data?.[0]).toMatchObject({ x: 1, y: 5 });
    expect(series[1].data?.[0]).toMatchObject({ x: 2, y: 4 });
  });

  it('uses actual race checkpoint identifiers for elite rank history', () => {
    const history: EliteHistorySnapshot = {
      runId: 'run-1',
      iterations: [
        {
          iteration: 2,
          updatedAt: '2026-10-04T00:00:02Z',
          progress: {
            nbIterations: 100,
            maxExperiments: 50,
            experimentsUsed: 10,
            remainingBudget: 40,
            remainingBudgetEstimated: false,
            currentBudget: 10,
            currentBudgetUsed: 10,
            maxTime: 0,
            timeUsed: 0,
            remainingTime: null,
            boundEstimate: null,
          },
          elites: [{ configurationId: '7', position: 2, parameters: {}, algorithm: null }],
        },
        {
          iteration: 5,
          updatedAt: '2026-10-04T00:00:05Z',
          progress: {
            nbIterations: 100,
            maxExperiments: 50,
            experimentsUsed: 30,
            remainingBudget: 20,
            remainingBudgetEstimated: false,
            currentBudget: 20,
            currentBudgetUsed: 20,
            maxTime: 0,
            timeUsed: 0,
            remainingTime: null,
            boundEstimate: null,
          },
          elites: [{ configurationId: '7', position: 1, parameters: {}, algorithm: null }],
        },
      ],
    };

    const model = AutoconfigChartUtil.eliteRanks(history);
    const series = model.options.series as Highcharts.SeriesLineOptions[];

    expect(series[0].data).toEqual([
      [2, 2],
      [5, 1],
    ]);
  });

  it('counts all attempts while excluding unsuccessful costs from instance bests', () => {
    const summaries = AutoconfigChartUtil.instanceSummaries([
      evaluation({ id: 1, cost: 8 }),
      evaluation({ id: 2, cost: 5, slow: true }),
      evaluation({ id: 3, state: 'REJECTED', cost: null }),
      evaluation({ id: 4, state: 'FAILED', cost: null }),
    ]);

    expect(summaries).toEqual([
      { instanceName: 'one.dat', evaluations: 4, bestCost: 5, issues: 2, slow: 1 },
    ]);
  });
});
