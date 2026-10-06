import { InstanceDetail, NormalStatus, ResultView, Statistics } from '../model/normal-experiment';

export const normalCounts = {
  planned: 2,
  pending: 0,
  running: 0,
  succeeded: 1,
  failed: 1,
  cancelled: 0,
};
export function normalStatus(overrides: Partial<NormalStatus> = {}): NormalStatus {
  return {
    runId: 'run-a',
    revision: 1,
    phase: 'RUNNING',
    startedAt: 1,
    endedAt: null,
    elapsedMillis: 1000,
    currentExperiment: 'Experiment A',
    objectives: { Cost: 'MINIMIZE', Quality: 'MAXIMIZE' },
    counts: normalCounts,
    instances: 12,
    completedInstances: 1,
    runningInstances: 1,
    algorithms: 12,
    completedAlgorithms: 1,
    runningAlgorithms: 1,
    experiments: [
      { name: 'Experiment A', counts: normalCounts, instances: 12, completedInstances: 1 },
    ],
    issueCount: 0,
    ...overrides,
  };
}
export function normalResult(overrides: Partial<ResultView> = {}): ResultView {
  return {
    resultId: 'result-0',
    instanceId: 'instance-0',
    experimentName: 'Experiment A',
    instanceName: 'Instance 0',
    algorithmName: 'Algorithm A',
    ordinal: 1,
    iteration: 'repetition-one',
    seed: 11,
    state: 'SUCCEEDED',
    objectives: { Cost: 5, Quality: 5 },
    properties: {},
    executionTime: 2000000,
    timeToBest: 1000000,
    startedAt: 1,
    endedAt: 3,
    ...overrides,
  };
}
export function normalDetail(
  id = 'instance-0',
  objective = 'Cost',
  overrides: Partial<InstanceDetail> = {},
): InstanceDetail {
  const stats: Statistics = {
    count: 1,
    best: 5,
    mean: 5,
    median: 5,
    standardDeviation: null,
    lowerQuartile: 5,
    upperQuartile: 5,
    worst: 5,
    absoluteGap: 1,
    percentageGap: 25,
  };
  return {
    runId: 'run-a',
    revision: 1,
    objective,
    instance: {
      id,
      instanceName: id,
      experimentName: 'Experiment A',
      counts: normalCounts,
      references: { Cost: 4, Quality: 6 },
      algorithms: 1,
      completedAlgorithms: 1,
      finalized: true,
      revision: 1,
    },
    algorithms: [
      {
        name: 'Algorithm A',
        counts: normalCounts,
        objective: stats,
        runtimeMillis: stats,
        timeToBestMillis: stats,
        bestResultId: 'result-0',
      },
    ],
    results: [
      normalResult({ instanceId: id }),
      normalResult({
        resultId: 'result-1',
        instanceId: id,
        ordinal: 2,
        iteration: 'failed-two',
        state: 'FAILED',
        objectives: {},
      }),
    ],
    resultCount: 2,
    sampled: false,
    ...overrides,
  };
}
