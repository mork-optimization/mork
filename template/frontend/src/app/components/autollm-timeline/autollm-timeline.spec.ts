import { TestBed } from '@angular/core/testing';
import { appConfig } from '../../app.config';
import { timedAttempt, timedTrial, timingEpoch } from '../../testing/autollm-timing-fixture';
import { AutoLlmTimeline } from './autollm-timeline';

describe('AutoLLM timeline', () => {
  afterEach(() => TestBed.resetTestingModule());

  it('renders X-range with production modules and provides sorting, paging and accessible inspection', async () => {
    await TestBed.configureTestingModule({
      imports: [AutoLlmTimeline],
      providers: appConfig.providers,
    }).compileComponents();
    const fixture = TestBed.createComponent(AutoLlmTimeline);
    const trials = Array.from({ length: 26 }, (_, i) =>
      timedTrial(
        [timedAttempt(1, 'GENERATION', 0, i + 1), timedAttempt(2, 'TUNING', i + 2, null)],
        { number: i + 1, targetId: `worker-${i + 1}`, status: 'TUNING' },
      ),
    );
    fixture.componentRef.setInput('trials', trials);
    fixture.componentRef.setInput('now', timingEpoch + 60000);
    await fixture.whenStable();
    expect(
      fixture.nativeElement.querySelectorAll(
        '.highcharts-xrange-series .highcharts-partfill-original',
      ),
    ).toHaveLength(75);
    const component = fixture.componentInstance;
    component.updateSettings({ mode: 'clock' });
    await fixture.whenStable();
    expect(
      fixture.nativeElement.querySelector('.highcharts-xaxis .highcharts-axis-title').textContent,
    ).toBe('Campaign clock (UTC)');
    component.updateSettings({ mode: 'elapsed' });
    await fixture.whenStable();
    expect(
      fixture.nativeElement.querySelector('.highcharts-xaxis .highcharts-axis-title').textContent,
    ).toContain('Elapsed');
    expect(component.rows()).toHaveLength(25);
    component.updateSettings({ page: 1 });
    await fixture.whenStable();
    expect(component.rows().map((row) => row.trial.number)).toEqual([26]);
    expect(
      fixture.nativeElement.querySelectorAll(
        '.highcharts-xrange-series .highcharts-partfill-original',
      ),
    ).toHaveLength(3);
    component.updateSettings({ sort: 'GENERATION' });
    component.updateSettings({ page: 0 });
    expect(component.rows()[0].trial.number).toBe(26);
    component.updateSettings({ search: 'worker-26' });
    await fixture.whenStable();
    const inspect = vi.fn();
    component.inspectTrial.subscribe(inspect);
    fixture.nativeElement.querySelector('tbody button').click();
    expect(inspect).toHaveBeenCalledWith(26);
    fixture.componentRef.setInput('trials', [timedTrial([], { status: 'PENDING' })]);
    component.updateSettings({ search: '' });
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('No valid attempt intervals');
    expect(fixture.nativeElement.textContent).toContain('Unavailable');
    expect(fixture.nativeElement.querySelector('app-dashboard-chart')).toBeNull();
  });
});
