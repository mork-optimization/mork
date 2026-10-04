import { Component } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { appConfig } from '../../app.config';
import { componentUsageFixture } from '../../testing/component-usage-fixture';
import { ComponentUsageUtil } from '../../util/component-usage-util';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';

@Component({
  imports: [DashboardChart],
  template: '<app-dashboard-chart [model]="treemap" /><app-dashboard-chart [model]="sankey" />',
})
class ChartHost {
  readonly treemap = ComponentUsageUtil.treemap(
    componentUsageFixture.components,
    'CANDIDATE',
    () => undefined,
  );
  readonly sankey = ComponentUsageUtil.sankey(
    'Local',
    ComponentUsageUtil.relationships(componentUsageFixture, 'Local', 'incoming', 'CANDIDATE'),
    ComponentUsageUtil.relationships(componentUsageFixture, 'Local', 'outgoing', 'CANDIDATE'),
    'CANDIDATE',
    () => undefined,
  );
}

describe('Component chart integration', () => {
  afterEach(() => TestBed.resetTestingModule());

  it('loads production modules in dependency order and renders both new chart types', async () => {
    await TestBed.configureTestingModule({
      imports: [ChartHost],
      providers: appConfig.providers,
    }).compileComponents();
    const errors = vi.spyOn(console, 'error');
    try {
      const fixture = TestBed.createComponent(ChartHost);
      await fixture.whenStable();
      expect(errors).not.toHaveBeenCalled();
      expect(
        fixture.nativeElement.querySelectorAll('.highcharts-treemap-series .highcharts-point'),
      ).toHaveLength(3);
      expect(
        fixture.nativeElement.querySelectorAll('.highcharts-sankey-series .highcharts-link'),
      ).toHaveLength(2);
    } finally {
      errors.mockRestore();
    }
  });
});
