import {
  ApplicationConfig,
  provideBrowserGlobalErrorListeners,
  provideZonelessChangeDetection,
} from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideHighcharts } from 'highcharts-angular';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideZonelessChangeDetection(),
    provideHttpClient(),
    provideHighcharts({
      instance: () => import('highcharts/esm/highcharts').then((module) => module.default),
      modules: () => [
        import('highcharts/esm/modules/exporting'),
        import('highcharts/esm/modules/offline-exporting'),
        import('highcharts/esm/modules/boost'),
        import('highcharts/esm/modules/accessibility'),
      ],
    }),
  ],
};
