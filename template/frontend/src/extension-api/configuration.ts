import { SolutionViewRegistry } from './solution-view';

/** Supported project customization, consumed at application bootstrap. */
export interface FrontendExtensions {
  readonly solutionViews: SolutionViewRegistry;
}
