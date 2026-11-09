import { TestBed } from '@angular/core/testing';
import { SolutionViewData, SolutionViewResult } from '@mork/extension-api';
import { TspSolutionView } from './tsp-solution-view';
import { drawRoute } from './tsp-route';

const data: SolutionViewData = {
  version: 1,
  cities: [
    { x: -2, y: 0 },
    { x: 1, y: 0 },
    { x: 1, y: 4 },
  ],
  tour: [2, 0, 1],
};
const result: SolutionViewResult = {
  resultId: 'test',
  experimentName: 'LocalSearchExperiment',
  instanceName: 'triangle',
  algorithmName: 'Local search',
  iteration: '0',
  seed: 1234,
  state: 'SUCCEEDED',
  objectives: { Distance: 12 },
  properties: {},
  executionTime: 1000,
  timeToBest: 500,
};

describe('TSP solution visualization', () => {
  it('closes the tour and fits negative coordinates without distorting distances', () => {
    const route = drawRoute(data)!;
    const points = route.points.split(' ');
    expect(points).toHaveLength(4);
    expect(points[0]).toBe(points[3]);
    expect(route.cities[0].x).toBeLessThan(route.cities[1].x);
    expect(route.cities[2].y).toBeLessThan(route.cities[1].y);
    expect(
      (route.cities[1].x - route.cities[0].x) / (route.cities[1].y - route.cities[2].y),
    ).toBeCloseTo(3 / 4);
  });

  it('handles coincident and collinear coordinates without non-finite SVG points', () => {
    for (const cities of [
      [{ x: 0, y: 0 }],
      [
        { x: 2, y: 1 },
        { x: 2, y: 1 },
      ],
      [
        { x: 2, y: 1 },
        { x: 2, y: 4 },
      ],
    ]) {
      const route = drawRoute({
        version: 1,
        cities,
        tour: cities.map((_, id) => id),
      })!;
      expect(route).not.toBeNull();
      for (const city of route.cities) {
        expect(Number.isFinite(city.x) && Number.isFinite(city.y)).toBe(true);
      }
    }
  });

  it('rejects malformed data, duplicate visits, unsupported versions, and invalid coordinates', () => {
    for (const invalid of [
      {},
      { ...data, version: 2 },
      { ...data, tour: [0, 0, 1] },
      { ...data, tour: [0, 1, 3] },
      { ...data, tour: [0, 1] },
      { ...data, tour: [0, 1, 1.5] },
      { ...data, cities: [{ x: NaN, y: 0 }], tour: [0] },
      { ...data, cities: [{ x: '0', y: 0 }], tour: [0] },
      { ...data, cities: [{ x: 0, y: Infinity }], tour: [0] },
    ]) {
      expect(drawRoute(invalid)).toBeNull();
    }
  });

  it('updates the SVG and accessible visit order when the retained solution changes', async () => {
    const fixture = TestBed.createComponent(TspSolutionView);
    fixture.componentRef.setInput('result', result);
    fixture.componentRef.setInput('selectedObjective', 'Distance');
    fixture.componentRef.setInput('view', data);
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('svg').getAttribute('aria-label')).toContain(
      '3 cities',
    );
    expect(fixture.nativeElement.querySelectorAll('.city')).toHaveLength(3);
    expect(fixture.nativeElement.querySelector('.visit-order').textContent).toContain(
      '3 → 1 → 2 → 3',
    );
    expect(fixture.nativeElement.textContent).toContain('12.00');
    const previous = fixture.nativeElement.querySelector('polyline').getAttribute('points');
    fixture.componentRef.setInput('view', { ...data, tour: [0, 2, 1] });
    fixture.componentRef.setInput('selectedObjective', 'Missing');
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('polyline').getAttribute('points')).not.toBe(
      previous,
    );
    expect(fixture.nativeElement.textContent).toContain('Unavailable');
    fixture.componentRef.setInput('view', {});
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('svg')).toBeNull();
    expect(fixture.nativeElement.querySelector('[role=status]').textContent).toContain(
      'cannot be displayed',
    );
  });
});
