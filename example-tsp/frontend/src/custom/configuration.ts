import { FrontendExtensions } from '@mork/extension-api';
import { TspSolutionView } from './solution-views/tsp-solution-view';

export const customConfiguration: FrontendExtensions = {
  solutionViews: { tsp: TspSolutionView },
};
