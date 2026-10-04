import { ComponentUsageSnapshot } from '../model/autoconfig';

/** Two distinct trees: Root(Local(Move), Local(Move), Move), evaluated three times;
 *  and a root Move evaluated twice. */
export const componentUsageFixture: ComponentUsageSnapshot = {
  runId: 'run-1',
  scope: 'ALL',
  latestEvaluationRevision: 5,
  eliteUpdatedAt: null,
  configurationCount: 2,
  decodedConfigurationCount: 2,
  unavailableConfigurationCount: 0,
  evaluationCount: 5,
  candidatePlacementCount: 7,
  evaluationPlacementCount: 20,
  components: [
    {
      name: 'Local',
      candidatePlacements: 2,
      configurationCount: 1,
      evaluationPlacements: 6,
      rootCandidatePlacements: 0,
      rootConfigurationCount: 0,
      rootEvaluationPlacements: 0,
    },
    {
      name: 'Move',
      candidatePlacements: 4,
      configurationCount: 2,
      evaluationPlacements: 11,
      rootCandidatePlacements: 1,
      rootConfigurationCount: 1,
      rootEvaluationPlacements: 2,
    },
    {
      name: 'Root',
      candidatePlacements: 1,
      configurationCount: 1,
      evaluationPlacements: 3,
      rootCandidatePlacements: 1,
      rootConfigurationCount: 1,
      rootEvaluationPlacements: 3,
    },
  ],
  relationships: [
    {
      parent: 'Root',
      role: 'improvers[]',
      child: 'Local',
      candidatePlacements: 2,
      configurationCount: 1,
      evaluationPlacements: 6,
    },
    {
      parent: 'Local',
      role: 'neighborhood',
      child: 'Move',
      candidatePlacements: 2,
      configurationCount: 1,
      evaluationPlacements: 6,
    },
    {
      parent: 'Root',
      role: 'fallback',
      child: 'Move',
      candidatePlacements: 1,
      configurationCount: 1,
      evaluationPlacements: 3,
    },
  ],
};
