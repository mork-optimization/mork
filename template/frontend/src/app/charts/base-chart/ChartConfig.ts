import {ChartCallbackFunction, Options} from "highcharts";

export type ChartConfig = {
  id: string;
  options: Options;
  callback?: ChartCallbackFunction;
}
