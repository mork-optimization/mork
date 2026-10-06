import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { Component, input, provideZonelessChangeDetection } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MatProgressBarHarness } from '@angular/material/progress-bar/testing';
import { MatSelectHarness } from '@angular/material/select/testing';
import { BehaviorSubject, Subject } from 'rxjs';
import { ChartViewModel } from '../../model/dashboard';
import { normalDetail, normalStatus } from '../../testing/normal-experiment-fixture';
import { DashboardStore } from '../../store/dashboard-store';
import { EventClient } from '../../service/event-client';
import { ExperimentClient } from '../../service/experiment-client';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';
import { StandardDashboard } from './standard-dashboard';

@Component({ selector: 'app-dashboard-chart', template: '' })
class ChartStub {
  readonly model = input.required<ChartViewModel>();
}

describe('StandardDashboard with the real snapshot store', () => {
  let status = normalStatus();
  beforeEach(async () => {
    status = normalStatus();
    localStorage.clear();
    const detail = normalDetail();
    await TestBed.configureTestingModule({
      imports: [StandardDashboard],
      providers: [provideZonelessChangeDetection()],
    })
      .overrideComponent(StandardDashboard, {
        remove: { imports: [DashboardChart] },
        add: { imports: [ChartStub] },
      })
      .overrideComponent(StandardDashboard, {
        set: {
          providers: [
            DashboardStore,
            {
              provide: ExperimentClient,
              useValue: {
                getStatus: async () => status,
                getInstances: async () => ({
                  runId: status.runId,
                  revision: status.revision,
                  total: 1,
                  offset: 0,
                  limit: 25,
                  items: [detail.instance],
                }),
                getInstance: async (_run: string, _id: string, objective: string) => ({
                  ...detail,
                  objective,
                }),
                getIssues: async () => ({
                  runId: status.runId,
                  revision: 1,
                  total: 0,
                  offset: 0,
                  limit: 25,
                  items: [],
                }),
              },
            },
            {
              provide: EventClient,
              useValue: {
                state$: new BehaviorSubject('live'),
                envelopes$: new Subject(),
                connected$: new Subject(),
                errors$: new Subject(),
                start: vi.fn(),
                retryNow: vi.fn(),
                lastEventId: 0,
              },
            },
          ],
        },
      })
      .compileComponents();
  });
  afterEach(() => TestBed.resetTestingModule());
  async function settle(fixture: ReturnType<typeof TestBed.createComponent<StandardDashboard>>) {
    for (let i = 0; i < 20; i++) await Promise.resolve();
    await fixture.whenStable();
  }
  it('renders HTTP-driven signal changes without manual change detection', async () => {
    const fixture = TestBed.createComponent(StandardDashboard);
    fixture.detectChanges();
    await settle(fixture);
    expect(fixture.nativeElement.textContent).toContain('Experiment A');
    status = normalStatus({
      revision: 2,
      phase: 'FAILED',
      currentExperiment: 'Final experiment',
      completedInstances: 0,
    });
    fixture.componentInstance.store.refreshNow();
    await settle(fixture);
    expect(fixture.nativeElement.textContent).toContain('Final experiment');
    expect(fixture.nativeElement.textContent).toContain('failed');
    expect(fixture.nativeElement.textContent).not.toContain('Waiting for execution');
  });
  it('selects objectives and displays three explicitly scoped progress bars', async () => {
    const fixture = TestBed.createComponent(StandardDashboard);
    fixture.detectChanges();
    await settle(fixture);
    const loader = TestbedHarnessEnvironment.loader(fixture);
    const select = await loader.getHarness(
      MatSelectHarness.with({ selector: '.summary mat-select' }),
    );
    await select.open();
    await select.clickOptions({ text: /Quality/ });
    await settle(fixture);
    expect(fixture.componentInstance.store.selectedObjective()).toBe('Quality');
    const bars = await loader.getAllHarnesses(MatProgressBarHarness);
    expect(bars).toHaveLength(3);
    const values = await Promise.all(bars.map((bar) => bar.getValue()));
    expect(values[0]).toBeCloseTo(100 / 12);
    expect(values.slice(1)).toEqual([100, 100]);
    expect(fixture.nativeElement.textContent).toContain('Best score across repetitions');
  });
});
