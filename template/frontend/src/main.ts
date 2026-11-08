import { bootstrapApplication } from '@angular/platform-browser';
import { mergeApplicationConfig } from '@angular/core';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import { customConfiguration } from './custom/configuration';
import { provideSolutionViews } from './extension-api/public-api';

// Framework-owned integration: custom registration belongs in custom/configuration.ts.
const config = mergeApplicationConfig(appConfig, {
  providers: [provideSolutionViews(customConfiguration.solutionViews)],
});
bootstrapApplication(App, config).catch((err) => console.error(err));
