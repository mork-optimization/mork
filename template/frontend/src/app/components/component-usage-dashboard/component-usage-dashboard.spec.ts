import { Component, input, provideZonelessChangeDetection, signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import {
  ComponentCandidatePage,
  ComponentCandidateQuery,
  ComponentUsageScope,
  ComponentUsageSnapshot,
} from '../../model/autoconfig';
import { ChartViewModel } from '../../model/dashboard';
import { AutoconfigStore } from '../../store/autoconfig-store';
import { componentUsageFixture } from '../../testing/component-usage-fixture';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';
import { ComponentUsageDashboard } from './component-usage-dashboard';

@Component({ selector: 'app-dashboard-chart', template: '' })
class ChartStub {
  readonly model = input.required<ChartViewModel>();
}

class FakeStore {
  readonly status = signal({ runId: 'run-1' });
  readonly componentScope = signal<ComponentUsageScope>('ALL');
  readonly componentUsage = signal<ComponentUsageSnapshot | null>(componentUsageFixture);
  readonly componentError = signal<string | null>(null);
  readonly refreshComponentUsage = vi.fn();
  readonly selectComponentScope = vi.fn((scope: ComponentUsageScope) => {
    this.componentScope.set(scope);
    this.componentUsage.set({ ...componentUsageFixture, scope });
  });
  readonly loadComponentCandidates = vi.fn(
    async (query: ComponentCandidateQuery): Promise<ComponentCandidatePage> => ({
      runId: 'run-1',
      scope: query.scope,
      total: 60,
      offset: query.offset,
      nextOffset: query.offset + 25 < 60 ? query.offset + 25 : null,
      candidates: [
        {
          configurationId: `${query.offset + 1}`,
          occurrences: 3,
          evaluationPlacements: 9,
          elitePosition: 1,
          evaluations: { running: 0, succeeded: 1, rejected: 1, failed: 1, slow: 0 },
        },
      ],
    }),
  );
}

describe('ComponentUsageDashboard', () => {
  let store: FakeStore;
  const dialog = { open: vi.fn() };

  beforeEach(async () => {
    store = new FakeStore();
    dialog.open.mockClear();
    await TestBed.configureTestingModule({
      imports: [ComponentUsageDashboard],
      providers: [
        provideZonelessChangeDetection(),
        { provide: AutoconfigStore, useValue: store },
        { provide: MatDialog, useValue: dialog },
      ],
    })
      .overrideComponent(ComponentUsageDashboard, {
        remove: { imports: [DashboardChart] },
        add: { imports: [ChartStub] },
      })
      .compileComponents();
  });
  afterEach(() => TestBed.resetTestingModule());

  it('renders live usage and opens matching configurations without manual change detection', async () => {
    const fixture = TestBed.createComponent(ComponentUsageDashboard);
    await fixture.whenStable();
    const component = fixture.componentInstance;
    expect(component.selected()?.name).toBe('Move');
    expect(store.loadComponentCandidates).toHaveBeenLastCalledWith(
      expect.objectContaining({ selector: { component: 'Move' } }),
    );
    const button = fixture.nativeElement.querySelector(
      '.candidate-table button',
    ) as HTMLButtonElement;
    button.click();
    expect(dialog.open).toHaveBeenCalledWith(
      expect.anything(),
      expect.objectContaining({ data: '1' }),
    );
    store.componentUsage.set({ ...componentUsageFixture, candidatePlacementCount: 123 });
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('.totals').textContent).toContain('123');
  });

  it('selects exact relationships from the matrix and resets pagination only when filters change', async () => {
    const fixture = TestBed.createComponent(ComponentUsageDashboard);
    await fixture.whenStable();
    const component = fixture.componentInstance;
    component.offset.set(25);
    await fixture.whenStable();
    expect(component.page()?.offset).toBe(25);
    store.componentUsage.set({ ...componentUsageFixture, latestEvaluationRevision: 6 });
    await fixture.whenStable();
    expect(component.offset()).toBe(25);
    component.view.set('matrix');
    component.matrixPercent.set(true);
    await fixture.whenStable();
    const button = fixture.nativeElement.querySelector('.matrix button') as HTMLButtonElement;
    expect(button.textContent).toContain('100.0%');
    button.click();
    await fixture.whenStable();
    expect(component.offset()).toBe(0);
    expect(store.loadComponentCandidates).toHaveBeenLastCalledWith(
      expect.objectContaining({
        selector: { parent: 'Local', role: 'neighborhood', child: 'Move' },
      }),
    );
    component.basis.set('EVALUATION');
    await fixture.whenStable();
    expect(store.loadComponentCandidates).toHaveBeenLastCalledWith(
      expect.objectContaining({ basis: 'EVALUATION' }),
    );
  });

  it('retains the component while it remains available and explains empty and unavailable data', async () => {
    const fixture = TestBed.createComponent(ComponentUsageDashboard);
    await fixture.whenStable();
    const component = fixture.componentInstance;
    component.selectComponent('Local');
    store.componentUsage.set({ ...componentUsageFixture, unavailableConfigurationCount: 2 });
    await fixture.whenStable();
    expect(component.selected()?.name).toBe('Local');
    expect(fixture.nativeElement.textContent).toContain(
      '2 configurations have unavailable component trees',
    );
    component.search.set('missing');
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('No components match');
    store.selectComponentScope('CURRENT_ELITES');
    store.componentUsage.set({
      ...componentUsageFixture,
      scope: 'CURRENT_ELITES',
      components: [],
      relationships: [],
    });
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain(
      'Elites appear after the first race checkpoint',
    );
  });

  it('ignores candidate pages which arrive after the selection changes', async () => {
    const fixture = TestBed.createComponent(ComponentUsageDashboard);
    await fixture.whenStable();
    let resolve!: (page: ComponentCandidatePage) => void;
    store.loadComponentCandidates.mockImplementationOnce(
      () =>
        new Promise((done) => {
          resolve = done;
        }),
    );
    fixture.componentInstance.selectComponent('Root');
    await fixture.whenStable();
    fixture.componentInstance.selectComponent('Local');
    await fixture.whenStable();
    resolve({
      runId: 'run-1',
      scope: 'ALL',
      total: 1,
      offset: 0,
      nextOffset: null,
      candidates: [
        {
          configurationId: 'obsolete',
          occurrences: 1,
          evaluationPlacements: 1,
          elitePosition: null,
          evaluations: { running: 0, succeeded: 1, rejected: 0, failed: 0, slow: 0 },
        },
      ],
    });
    await fixture.whenStable();
    expect(fixture.componentInstance.page()?.candidates[0].configurationId).toBe('1');
  });
});
