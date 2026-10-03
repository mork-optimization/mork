import { computed, provideZonelessChangeDetection, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MatProgressBarHarness } from '@angular/material/progress-bar/testing';
import { MatSelectHarness } from '@angular/material/select/testing';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { App } from './app';
import { ObjectiveModel, ProgressModel } from './model/dashboard';
import { DashboardStore } from './store/dashboard-store';

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

describe('App', () => {
  let store: FakeDashboardStore;

  beforeEach(async () => {
    store = new FakeDashboardStore();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideZonelessChangeDetection(), { provide: DashboardStore, useValue: store }],
    }).compileComponents();
  });

  afterEach(() => TestBed.resetTestingModule());

  it('renders signal updates from event-driven state without manual change detection', async () => {
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    expect(store.start).toHaveBeenCalledOnce();

    store.currentExperiment.set('HTTP replay experiment');
    store.processedEventCount.set(42);
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('HTTP replay experiment');
    expect(fixture.nativeElement.textContent).toContain('42');
  });

  it('forwards Material objective selection to the store', async () => {
    store.objectives.set([
      { name: 'Cost', mode: 'MINIMIZE' },
      { name: 'Quality', mode: 'MAXIMIZE' },
    ]);
    store.selectedObjective.set('Cost');
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const loader = TestbedHarnessEnvironment.loader(fixture);
    const select = await loader.getHarness(MatSelectHarness);
    await select.open();
    await select.clickOptions({ text: /Quality/ });

    expect(store.selectObjective).toHaveBeenCalledWith('Quality');
  });

  it('renders three labeled Material progress bars', async () => {
    store.progress.set({
      instances: { completed: 1, total: 2 },
      algorithms: { completed: 2, total: 4 },
      repetitions: { completed: 3, total: 6 },
    });
    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();

    const loader = TestbedHarnessEnvironment.loader(fixture);
    const bars = await loader.getAllHarnesses(MatProgressBarHarness);
    expect(bars).toHaveLength(3);
    expect(await Promise.all(bars.map((bar) => bar.getValue()))).toEqual([50, 50, 50]);
    expect(fixture.nativeElement.textContent).toContain('Instances');
    expect(fixture.nativeElement.textContent).toContain('Algorithms');
    expect(fixture.nativeElement.textContent).toContain('Repetitions');
  });
});
