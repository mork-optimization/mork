import type Highcharts from 'highcharts/esm/highcharts';
import {
  ComponentRelationship,
  ComponentUsageBasis,
  ComponentUsageSnapshot,
  ComponentUsageStats,
} from '../model/autoconfig';
import { ChartViewModel } from '../model/dashboard';

export interface ComponentMatrix {
  readonly columns: readonly ComponentUsageStats[];
  readonly rows: readonly {
    readonly parent: string;
    readonly role: string;
    readonly cells: readonly (ComponentRelationship | null)[];
  }[];
  readonly maximum: number;
}

export class ComponentUsageUtil {
  static count(
    item: { candidatePlacements: number; evaluationPlacements: number },
    basis: ComponentUsageBasis,
  ): number {
    return basis === 'CANDIDATE' ? item.candidatePlacements : item.evaluationPlacements;
  }

  static components(
    snapshot: ComponentUsageSnapshot | null,
    basis: ComponentUsageBasis,
    search: string,
  ): readonly ComponentUsageStats[] {
    const term = search.trim().toLocaleLowerCase();
    return (snapshot?.components ?? [])
      .filter((item) => item.name.toLocaleLowerCase().includes(term))
      .sort((a, b) => this.count(b, basis) - this.count(a, basis) || a.name.localeCompare(b.name));
  }

  static relationships(
    snapshot: ComponentUsageSnapshot | null,
    name: string | undefined,
    direction: 'incoming' | 'outgoing',
    basis: ComponentUsageBasis,
  ): readonly ComponentRelationship[] {
    return (snapshot?.relationships ?? [])
      .filter((edge) => (direction === 'incoming' ? edge.child === name : edge.parent === name))
      .sort(
        (a, b) =>
          this.count(b, basis) - this.count(a, basis) ||
          a.parent.localeCompare(b.parent) ||
          a.role.localeCompare(b.role) ||
          a.child.localeCompare(b.child),
      );
  }

  static treemap(
    components: readonly ComponentUsageStats[],
    basis: ComponentUsageBasis,
    select: (name: string) => void,
  ): ChartViewModel {
    return {
      options: {
        chart: { type: 'treemap', height: 390, animation: false, backgroundColor: 'transparent' },
        title: { text: undefined },
        credits: { enabled: false },
        accessibility: {
          description:
            'Component placements. Select a tile or a component in the list to inspect its parents, children and configurations.',
        },
        tooltip: { pointFormat: '<b>{point.name}</b><br/>Placements: {point.value:,.0f}' },
        plotOptions: { series: { animation: false } },
        series: [
          {
            type: 'treemap',
            name: 'Component placements',
            layoutAlgorithm: 'squarified',
            colorByPoint: true,
            borderWidth: 3,
            borderColor: '#fff',
            colors: ['#2265a7', '#00897b', '#407fab', '#49a69e', '#697cad', '#78b8ad'],
            dataLabels: {
              enabled: true,
              format: '{point.name}<br/>{point.value:,.0f}',
              style: { textOutline: 'none', fontWeight: '600' },
            },
            point: {
              events: {
                click: function (this: Highcharts.Point) {
                  if (this.name) select(this.name);
                },
              },
            },
            data: components
              .filter((item) => this.count(item, basis) > 0)
              .map((item) => ({ name: item.name, value: this.count(item, basis) })),
          },
        ],
      },
    };
  }

  static sankey(
    name: string | undefined,
    incoming: readonly ComponentRelationship[],
    outgoing: readonly ComponentRelationship[],
    basis: ComponentUsageBasis,
    select: (edge: ComponentRelationship) => void,
  ): ChartViewModel {
    const nodes: Highcharts.SeriesSankeyNodesOptionsObject[] = [
      { id: 'focus', name: name ?? 'Component', color: '#2265a7' },
    ];
    const data: Highcharts.SeriesSankeyPointOptionsObject[] = [];
    const append = (edges: readonly ComponentRelationship[], side: 'in' | 'out') => {
      const positive = edges.filter((edge) => this.count(edge, basis) > 0);
      for (const [index, edge] of positive.slice(0, 10).entries()) {
        const id = `${side}:${index}`;
        nodes.push({
          id,
          name: side === 'in' ? `${edge.parent} · ${edge.role}` : `${edge.role} → ${edge.child}`,
          color: '#00897b',
        });
        data.push({
          from: side === 'in' ? id : 'focus',
          to: side === 'in' ? 'focus' : id,
          weight: this.count(edge, basis),
          custom: { edge },
        });
      }
      const remainder = positive.slice(10).reduce((sum, edge) => sum + this.count(edge, basis), 0);
      if (remainder > 0) {
        const id = `${side}:other`;
        nodes.push({
          id,
          name: `Other ${side === 'in' ? 'parents' : 'children'}`,
          color: '#aab7c5',
        });
        data.push({
          from: side === 'in' ? id : 'focus',
          to: side === 'in' ? 'focus' : id,
          weight: remainder,
        });
      }
    };
    append(incoming, 'in');
    append(outgoing, 'out');
    const series: Highcharts.SeriesSankeyOptions = {
      type: 'sankey',
      animation: false,
      name: 'Direct relationships',
      nodes,
      data,
      point: {
        events: {
          click: function (this: Highcharts.Point) {
            const edge = this.options.custom?.['edge'] as ComponentRelationship | undefined;
            if (edge) select(edge);
          },
        },
      },
    };
    return {
      options: {
        chart: {
          height: Math.max(300, Math.min(560, Math.max(incoming.length, outgoing.length) * 32)),
          animation: false,
          backgroundColor: 'transparent',
        },
        title: { text: undefined },
        credits: { enabled: false },
        accessibility: {
          description: `Direct parents and children of ${name ?? 'the selected component'}. The complete role relationships are also listed in the inspector.`,
        },
        series: [series],
      },
    };
  }

  static matrix(
    snapshot: ComponentUsageSnapshot | null,
    basis: ComponentUsageBasis,
    search: string,
    limit: number,
  ): ComponentMatrix {
    const matching = new Set(this.components(snapshot, basis, search).map((item) => item.name));
    const filtered = search.trim().length > 0;
    const edges = (snapshot?.relationships ?? []).filter(
      (edge) => !filtered || matching.has(edge.parent) || matching.has(edge.child),
    );
    const neighborhood = new Set(matching);
    for (const edge of edges) {
      neighborhood.add(edge.parent);
      neighborhood.add(edge.child);
    }
    const columns = this.components(snapshot, basis, '')
      .filter((item) => neighborhood.has(item.name))
      .sort(
        (a, b) =>
          (filtered ? Number(matching.has(b.name)) - Number(matching.has(a.name)) : 0) ||
          this.count(b, basis) - this.count(a, basis) ||
          a.name.localeCompare(b.name),
      )
      .slice(0, limit);
    const names = new Set(columns.map((column) => column.name));
    const groups = new Map<
      string,
      { parent: string; role: string; count: number; edges: Map<string, ComponentRelationship> }
    >();
    let maximum = 0;
    for (const edge of edges) {
      if (!names.has(edge.parent) || !names.has(edge.child)) continue;
      const key = JSON.stringify([edge.parent, edge.role]);
      let group = groups.get(key);
      if (!group) {
        group = { parent: edge.parent, role: edge.role, count: 0, edges: new Map() };
        groups.set(key, group);
      }
      group.edges.set(edge.child, edge);
      group.count += this.count(edge, basis);
      maximum = Math.max(maximum, this.count(edge, basis));
    }
    const rows = [...groups.values()]
      .sort(
        (a, b) =>
          b.count - a.count || a.parent.localeCompare(b.parent) || a.role.localeCompare(b.role),
      )
      .slice(0, limit)
      .map((group) => ({
        parent: group.parent,
        role: group.role,
        cells: columns.map((column) => group.edges.get(column.name) ?? null),
      }));
    return { columns, rows, maximum };
  }

  static parentPercent(
    edge: ComponentRelationship,
    snapshot: ComponentUsageSnapshot | null,
  ): number {
    const parent = snapshot?.components.find((item) => item.name === edge.parent);
    return parent?.configurationCount
      ? (100 * edge.configurationCount) / parent.configurationCount
      : 0;
  }
}
