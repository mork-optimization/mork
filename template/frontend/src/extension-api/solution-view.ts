import { InjectionToken, InputSignal, Provider, Type } from '@angular/core';

/** Stable result metadata supplied to custom visualizers. Durations are nanoseconds. */
export interface SolutionViewResult {
  readonly resultId: string;
  readonly experimentName: string;
  readonly instanceName: string;
  readonly algorithmName: string;
  readonly iteration: string;
  readonly seed: number;
  readonly state: 'PENDING' | 'RUNNING' | 'SUCCEEDED' | 'FAILED' | 'CANCELLED';
  readonly objectives: Readonly<Record<string, number>>;
  readonly properties: Readonly<Record<string, unknown>>;
  readonly executionTime: number | null;
  readonly timeToBest: number | null;
}

/** JSON snapshot produced by the project's Java SolutionViewProvider. */
export type SolutionViewData = Readonly<Record<string, unknown>>;

/** Implement using three unaliased Angular signal inputs with these exact names. */
export interface SolutionView {
  readonly result: InputSignal<SolutionViewResult>;
  readonly selectedObjective: InputSignal<string | null>;
  readonly view: InputSignal<SolutionViewData>;
}

export type SolutionViewRegistry = Readonly<Record<string, Type<SolutionView>>>;

// Framework wiring; intentionally not exported by public-api.ts.
export const SOLUTION_RENDERERS = new InjectionToken<ReadonlyMap<string, Type<SolutionView>>>(
  'Mork solution renderers',
  { providedIn: 'root', factory: () => new Map() },
);

/** Register standalone components by the exact ID returned by SolutionViewProvider.rendererId(). */
export function provideSolutionViews(views: SolutionViewRegistry): Provider {
  return { provide: SOLUTION_RENDERERS, useValue: new Map(Object.entries(views)) };
}
