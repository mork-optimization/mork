import type Highcharts from 'highcharts/esm/highcharts';
import { ComponentRelationship } from '../model/autoconfig';
import { componentUsageFixture as snapshot } from '../testing/component-usage-fixture';
import { ComponentUsageUtil } from './component-usage-util';

describe('ComponentUsageUtil', () => {
  it('sizes treemap tiles by the active basis and sorts or filters component names', () => {
    const components = ComponentUsageUtil.components(snapshot, 'CANDIDATE', '');
    expect(components.map((item) => item.name)).toEqual(['Move', 'Local', 'Root']);
    expect(
      ComponentUsageUtil.components(snapshot, 'CANDIDATE', '  local ').map((item) => item.name),
    ).toEqual(['Local']);
    const chart = ComponentUsageUtil.treemap(components, 'EVALUATION', vi.fn());
    const series = chart.options.series![0] as Highcharts.SeriesTreemapOptions;
    expect(series.data).toEqual([
      { name: 'Move', value: 11 },
      { name: 'Local', value: 6 },
      { name: 'Root', value: 3 },
    ]);
  });

  it('uses only the selected direct neighborhood and preserves weights when grouping large neighborhoods', () => {
    const incoming = ComponentUsageUtil.relationships(snapshot, 'Local', 'incoming', 'CANDIDATE');
    const outgoing = ComponentUsageUtil.relationships(snapshot, 'Local', 'outgoing', 'CANDIDATE');
    expect(incoming.map((edge) => edge.parent)).toEqual(['Root']);
    expect(outgoing.map((edge) => edge.child)).toEqual(['Move']);
    const many: ComponentRelationship[] = Array.from({ length: 12 }, (_, i) => ({
      parent: `Parent${i}`,
      role: 'children[]',
      child: 'Local',
      candidatePlacements: i + 1,
      configurationCount: 1,
      evaluationPlacements: 2 * (i + 1),
    }));
    const chart = ComponentUsageUtil.sankey('Local', many, outgoing, 'EVALUATION', vi.fn());
    const series = chart.options.series![0] as Highcharts.SeriesSankeyOptions;
    const data = series.data as Highcharts.SeriesSankeyPointOptionsObject[];
    expect(series.nodes).toHaveLength(13); // focus, ten parents, other parents, child
    expect(
      data.filter((edge) => edge.to === 'focus').reduce((sum, edge) => sum + (edge.weight ?? 0), 0),
    ).toBe(156);
    expect(data.find((edge) => edge.from === 'in:other')?.custom).toBeUndefined();
    expect(data.find((edge) => edge.from === 'focus')?.to).toBe('out:0');
  });

  it('keeps parameter roles separate and normalizes by distinct parent configurations', () => {
    const matrix = ComponentUsageUtil.matrix(snapshot, 'CANDIDATE', '', 20);
    expect(matrix.columns.map((item) => item.name)).toEqual(['Move', 'Local', 'Root']);
    const row = matrix.rows.find((item) => item.parent === 'Root' && item.role === 'improvers[]')!;
    expect(row.cells[1]?.candidatePlacements).toBe(2);
    expect(ComponentUsageUtil.parentPercent(row.cells[1]!, snapshot)).toBe(100);
    expect(matrix.rows.some((item) => item.parent === 'Root' && item.role === 'fallback')).toBe(
      true,
    );
    expect(ComponentUsageUtil.matrix(snapshot, 'EVALUATION', '', 20).maximum).toBe(6);
    expect(ComponentUsageUtil.matrix(snapshot, 'CANDIDATE', 'missing', 20).rows).toEqual([]);
    const focused = ComponentUsageUtil.matrix(snapshot, 'CANDIDATE', 'local', 20);
    expect(focused.columns[0].name).toBe('Local');
    expect(focused.rows).toHaveLength(2);
    expect(focused.rows.map((row) => row.role)).not.toContain('fallback');
  });
});
