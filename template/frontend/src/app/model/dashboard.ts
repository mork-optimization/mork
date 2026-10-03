import type Highcharts from 'highcharts/esm/highcharts';
import { ObjectiveMode, SolutionGeneratedEvent } from './Events';

export type ConnectionState =
  'connecting' | 'syncing' | 'replaying' | 'live' | 'disconnected' | 'finished' | 'error';

export interface ObjectiveModel {
  readonly name: string;
  readonly mode: ObjectiveMode;
}

export interface ProgressDimension {
  readonly completed: number;
  readonly total: number;
}

export interface ProgressModel {
  readonly instances: ProgressDimension;
  readonly algorithms: ProgressDimension;
  readonly repetitions: ProgressDimension;
}

export interface ErrorModel {
  readonly exceptionType: string;
  readonly message: string | null;
}

export interface ChartViewModel {
  readonly options: Highcharts.Options;
}

export interface InstanceDashboardModel {
  readonly key: string;
  readonly experimentName: string;
  readonly instanceName: string;
  readonly selectedObjective: string | null;
  readonly referenceValue: number | null;
  readonly scoreChart: ChartViewModel;
  readonly convergenceChart: ChartViewModel;
  readonly bestSolution: SolutionGeneratedEvent | null;
}
