import { SolutionViewData } from '@mork/extension-api';

export interface City {
  readonly x: number;
  readonly y: number;
}

export interface RouteDrawing {
  readonly cities: readonly City[];
  readonly tour: readonly number[];
  readonly points: string;
}

/** Validate the project's JSON contract and fit coordinates without distorting their aspect ratio. */
export function drawRoute(data: SolutionViewData): RouteDrawing | null {
  const cities = data['cities'];
  const tour = data['tour'];
  if (data['version'] !== 1 || !Array.isArray(cities) || !Array.isArray(tour)) return null;
  if (cities.length === 0 || tour.length !== cities.length) return null;

  let minX = Infinity,
    minY = Infinity,
    maxX = -Infinity,
    maxY = -Infinity;
  for (const city of cities) {
    if (!city || typeof city !== 'object' || !Number.isFinite(city.x) || !Number.isFinite(city.y))
      return null;
    minX = Math.min(minX, city.x);
    maxX = Math.max(maxX, city.x);
    minY = Math.min(minY, city.y);
    maxY = Math.max(maxY, city.y);
  }
  const seen = new Set<number>();
  for (const id of tour) {
    if (!Number.isInteger(id) || id < 0 || id >= cities.length || seen.has(id)) return null;
    seen.add(id);
  }

  const spanX = maxX - minX,
    spanY = maxY - minY;
  if (!Number.isFinite(spanX) || !Number.isFinite(spanY)) return null;
  const span = Math.max(spanX / 720, spanY / 420);
  const scale = span === 0 ? 1 : 1 / span;
  if (!Number.isFinite(scale)) return null;
  const left = (800 - spanX * scale) / 2;
  const top = (500 - spanY * scale) / 2;
  const projected: City[] = [];
  for (const city of cities) {
    projected.push({
      x: left + (city.x - minX) * scale,
      y: top + (maxY - city.y) * scale,
    });
  }
  const points: string[] = [];
  for (const id of tour) points.push(`${projected[id].x},${projected[id].y}`);
  const first = projected[tour[0]];
  points.push(`${first.x},${first.y}`);
  return { cities: projected, tour: [...tour], points: points.join(' ') };
}
