import { DecimalPipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  effect,
  inject,
  signal,
  untracked,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import {
  ComponentCandidatePage,
  ComponentCandidateQuery,
  ComponentCandidateSelector,
  ComponentRelationship,
  ComponentUsageBasis,
  ComponentUsageStats,
} from '../../model/autoconfig';
import { AutoconfigStore } from '../../store/autoconfig-store';
import { ComponentUsageUtil } from '../../util/component-usage-util';
import { CandidateDialog } from '../candidate-dialog/candidate-dialog';
import { DashboardChart } from '../dashboard-chart/dashboard-chart';

@Component({
  selector: 'app-component-usage-dashboard',
  imports: [
    DecimalPipe,
    MatButtonModule,
    MatButtonToggleModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    DashboardChart,
  ],
  templateUrl: './component-usage-dashboard.html',
  styleUrl: './component-usage-dashboard.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ComponentUsageDashboard {
  readonly store = inject(AutoconfigStore);
  private readonly dialog = inject(MatDialog);
  private readonly destroyRef = inject(DestroyRef);
  readonly basis = signal<ComponentUsageBasis>('CANDIDATE');
  readonly view = signal<'usage' | 'matrix'>('usage');
  readonly search = signal('');
  readonly matrixLimit = signal(20);
  readonly matrixPercent = signal(false);
  readonly selectedName = signal<string | null>(null);
  readonly selectedEdge = signal<ComponentRelationship | null>(null);
  readonly offset = signal(0);
  readonly page = signal<ComponentCandidatePage | null>(null);
  readonly loading = signal(false);
  readonly pageError = signal<string | null>(null);
  private readonly pageRefresh = signal(0);
  private request = 0;
  private pageKey: string | null = null;
  private destroyed = false;

  readonly components = computed(() =>
    ComponentUsageUtil.components(this.store.componentUsage(), this.basis(), this.search()),
  );
  readonly selected = computed<ComponentUsageStats | undefined>(
    () =>
      this.components().find((item) => item.name === this.selectedName()) ?? this.components()[0],
  );
  readonly relationship = computed(() => {
    const selected = this.selectedEdge();
    return (
      this.store
        .componentUsage()
        ?.relationships.find(
          (edge) =>
            selected &&
            edge.parent === selected.parent &&
            edge.role === selected.role &&
            edge.child === selected.child,
        ) ?? null
    );
  });
  readonly incoming = computed(() =>
    ComponentUsageUtil.relationships(
      this.store.componentUsage(),
      this.selected()?.name,
      'incoming',
      this.basis(),
    ),
  );
  readonly outgoing = computed(() =>
    ComponentUsageUtil.relationships(
      this.store.componentUsage(),
      this.selected()?.name,
      'outgoing',
      this.basis(),
    ),
  );
  readonly treemap = computed(() =>
    ComponentUsageUtil.treemap(this.components(), this.basis(), (name) =>
      this.selectComponent(name),
    ),
  );
  readonly sankey = computed(() =>
    ComponentUsageUtil.sankey(
      this.selected()?.name,
      this.incoming(),
      this.outgoing(),
      this.basis(),
      (edge) => this.selectRelationship(edge),
    ),
  );
  readonly matrix = computed(() =>
    ComponentUsageUtil.matrix(
      this.store.componentUsage(),
      this.basis(),
      this.search(),
      this.matrixLimit(),
    ),
  );
  readonly selector = computed<ComponentCandidateSelector | null>(() => {
    const edge = this.relationship();
    if (edge) return { parent: edge.parent, role: edge.role, child: edge.child };
    const name = this.selected()?.name;
    return name ? { component: name } : null;
  });
  readonly selectedCoverage = computed(() => {
    const total = this.store.componentUsage()?.decodedConfigurationCount ?? 0;
    return total ? (100 * (this.selected()?.configurationCount ?? 0)) / total : 0;
  });
  private readonly selectionKey = computed(() => JSON.stringify(this.selector()));

  constructor() {
    this.destroyRef.onDestroy(() => {
      this.destroyed = true;
      this.request++;
    });
    effect(() => {
      this.store.componentScope();
      this.basis();
      this.selectionKey();
      this.store.status()?.runId;
      untracked(() => this.offset.set(0));
    });
    effect(() => {
      const snapshot = this.store.componentUsage();
      const selector = this.selector();
      const scope = this.store.componentScope();
      const basis = this.basis();
      const offset = this.offset();
      this.pageRefresh();
      untracked(() => {
        if (!snapshot || !selector) {
          this.request++;
          this.page.set(null);
          this.loading.set(false);
          this.pageError.set(null);
          return;
        }
        void this.loadPage({ scope, basis, selector, offset, limit: 25 });
      });
    });
  }

  selectComponent(name: string): void {
    this.selectedName.set(name);
    this.selectedEdge.set(null);
  }

  selectRelationship(edge: ComponentRelationship): void {
    this.selectedName.set(edge.child);
    this.selectedEdge.set(edge);
  }

  updateSearch(event: Event): void {
    this.search.set((event.target as HTMLInputElement).value);
    this.selectedEdge.set(null);
  }

  count(item: { candidatePlacements: number; evaluationPlacements: number }): number {
    return ComponentUsageUtil.count(item, this.basis());
  }

  cellLabel(edge: ComponentRelationship): string {
    return this.matrixPercent()
      ? `${ComponentUsageUtil.parentPercent(edge, this.store.componentUsage()).toFixed(1)}%`
      : this.count(edge).toLocaleString();
  }

  cellColor(edge: ComponentRelationship): string {
    const fraction = this.matrixPercent()
      ? ComponentUsageUtil.parentPercent(edge, this.store.componentUsage()) / 100
      : this.count(edge) / (this.matrix().maximum || 1);
    return `rgba(0, 137, 123, ${0.08 + Math.max(0, Math.min(1, fraction)) * 0.65})`;
  }

  cellDescription(edge: ComponentRelationship): string {
    return `${edge.parent}, ${edge.role}, ${edge.child}: ${this.cellLabel(edge)}`;
  }

  inspectCandidate(configurationId: string): void {
    this.dialog.open(CandidateDialog, { data: configurationId, width: '46rem', maxWidth: '94vw' });
  }

  retryPage(): void {
    this.pageRefresh.update((value) => value + 1);
  }

  private async loadPage(query: ComponentCandidateQuery): Promise<void> {
    const request = ++this.request;
    const runId = this.store.componentUsage()?.runId;
    this.loading.set(true);
    this.pageError.set(null);
    const key = JSON.stringify([runId, query]);
    if (key !== this.pageKey) this.page.set(null);
    this.pageKey = key;
    try {
      const page = await this.store.loadComponentCandidates(query);
      if (this.destroyed || request !== this.request) return;
      if (page.runId !== runId || page.scope !== query.scope) {
        throw new Error('Configurations belong to a different run or scope');
      }
      // A live elite replacement can remove a later page while it is being browsed.
      if (query.offset > 0 && page.total <= query.offset) {
        this.offset.set(0);
        return;
      }
      this.page.set(page);
    } catch (error: unknown) {
      if (!this.destroyed && request === this.request) {
        this.pageError.set(
          error instanceof Error ? error.message : 'Unable to load configurations',
        );
      }
    } finally {
      if (!this.destroyed && request === this.request) this.loading.set(false);
    }
  }
}
