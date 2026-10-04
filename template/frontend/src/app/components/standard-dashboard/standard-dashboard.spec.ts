import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { computed, provideZonelessChangeDetection, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MatProgressBarHarness } from '@angular/material/progress-bar/testing';
import { MatSelectHarness } from '@angular/material/select/testing';
import { ObjectiveModel, ProgressModel } from '../../model/dashboard';
import { DashboardStore } from '../../store/dashboard-store';
import { StandardDashboard } from './standard-dashboard';

class FakeDashboardStore {
  readonly connectionState = signal<'live'>('live');
  readonly connectionLabel = computed(() => 'Live');
  readonly currentExperiment = signal<string | null>(null);
  readonly processedEventCount = signal(0);
  readonly lastEventId = signal(-1);
  readonly objectives = signal<readonly ObjectiveModel[]>([]);
  readonly selectedObjective = signal<string | null>(null);
  readonly progress = signal<ProgressModel>({
    instances: { completed: 0, total: 0 },
    algorithms: { completed: 0, total: 0 },
    repetitions: { completed: 0, total: 0 },
  });
  readonly latestError = signal(null);
  readonly instances = signal([]);
  readonly start = vi.fn();
  readonly selectObjective = vi.fn((name: string) => this.selectedObjective.set(name));
}

describe('StandardDashboard', () => {
  let store: FakeDashboardStore;

  beforeEach(async () => {
    store = new FakeDashboardStore();
    await TestBed.configureTestingModule({
      imports: [StandardDashboard],
      providers: [provideZonelessChangeDetection()],
    })
      .overrideComponent(StandardDashboard, {
        set: { providers: [{ provide: DashboardStore, useValue: store }] },
      })
      .compileComponents();
  });

  afterEach(() => TestBed.resetTestingModule());

  it('renders event-driven signal updates without manual change detection', async () => {
    const fixture = TestBed.createComponent(StandardDashboard);
    fixture.detectChanges();
    expect(store.start).toHaveBeenCalledOnce();

    store.currentExperiment.set('HTTP replay experiment');
    store.processedEventCount.set(42);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('HTTP replay experiment');
    expect(fixture.nativeElement.textContent).toContain('42');
  });

  it('forwards objective selection and renders three progress bars', async () => {
    store.objectives.set([
      { name: 'Cost', mode: 'MINIMIZE' },
      { name: 'Quality', mode: 'MAXIMIZE' },
    ]);
    store.selectedObjective.set('Cost');
    store.progress.set({
      instances: { completed: 1, total: 2 },
      algorithms: { completed: 2, total: 4 },
      repetitions: { completed: 3, total: 6 },
    });
    const fixture = TestBed.createComponent(StandardDashboard);
    fixture.detectChanges();
    const loader = TestbedHarnessEnvironment.loader(fixture);

    const select = await loader.getHarness(MatSelectHarness);
    await select.open();
    await select.clickOptions({ text: /Quality/ });
    const bars = await loader.getAllHarnesses(MatProgressBarHarness);

    expect(store.selectObjective).toHaveBeenCalledWith('Quality');
    expect(await Promise.all(bars.map((bar) => bar.getValue()))).toEqual([50, 50, 50]);
  });
});
