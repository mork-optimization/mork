import { Component, input, OnDestroy } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { normalResult } from '../../testing/normal-experiment-fixture';
import { ResultDetail } from '../../model/normal-experiment';
import { SolutionRenderer } from './solution-renderer';
import {
  provideSolutionViews,
  SolutionView,
  SolutionViewData,
  SolutionViewResult,
} from '../../../extension-api/public-api';

@Component({
  selector: 'test-problem-view',
  template: '{{ result().instanceName }} · {{ selectedObjective() }} · {{ view()["nodes"] }}',
})
class ProblemView implements SolutionView, OnDestroy {
  static destroyed = 0;
  readonly result = input.required<SolutionViewResult>();
  readonly selectedObjective = input.required<string | null>();
  readonly view = input.required<SolutionViewData>();

  ngOnDestroy(): void {
    ProblemView.destroyed++;
  }
}
describe('solution renderer extension', () => {
  const detail: ResultDetail = {
    runId: 'run',
    revision: 1,
    result: normalResult(),
    issues: [],
    rendering: { state: 'AVAILABLE', rendererId: 'problem', data: { nodes: 5 } },
  };
  it('passes typed result, objective and view inputs to a registered component', async () => {
    TestBed.configureTestingModule({
      providers: [provideSolutionViews({ problem: ProblemView })],
    });
    const fixture = TestBed.createComponent(SolutionRenderer);
    fixture.componentRef.setInput('detail', detail);
    fixture.componentRef.setInput('selectedObjective', 'Cost');
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('test-problem-view').textContent).toBe(
      'Instance 0 · Cost · 5',
    );
    fixture.componentRef.setInput('selectedObjective', 'Quality');
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('test-problem-view').textContent).toContain(
      'Quality',
    );
  });
  it('updates result and payload inputs and destroys a view when its data becomes unavailable', async () => {
    TestBed.configureTestingModule({ providers: [provideSolutionViews({ problem: ProblemView })] });
    const fixture = TestBed.createComponent(SolutionRenderer);
    fixture.componentRef.setInput('detail', detail);
    fixture.componentRef.setInput('selectedObjective', null);
    await fixture.whenStable();

    fixture.componentRef.setInput('detail', {
      ...detail,
      result: normalResult({ instanceName: 'Another instance' }),
      rendering: { state: 'AVAILABLE', rendererId: 'problem', data: { nodes: 12 } },
    });
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('test-problem-view').textContent).toBe(
      'Another instance ·  · 12',
    );

    const destroyedBefore = ProblemView.destroyed;
    fixture.componentRef.setInput('detail', {
      ...detail,
      rendering: { state: 'UNAVAILABLE', rendererId: 'problem', data: {} },
    });
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('test-problem-view')).toBeNull();
    expect(ProblemView.destroyed).toBe(destroyedBefore + 1);
    expect(fixture.nativeElement.textContent).toContain('no longer retained');
  });

  it('uses exact registered IDs and falls back to JSON for unknown renderers', async () => {
    TestBed.configureTestingModule({ providers: [provideSolutionViews({ problem: ProblemView })] });
    const fixture = TestBed.createComponent(SolutionRenderer);
    fixture.componentRef.setInput('selectedObjective', 'Cost');
    for (const rendererId of ['Problem', 'constructor', 'toString']) {
      fixture.componentRef.setInput('detail', {
        ...detail,
        rendering: { ...detail.rendering, rendererId },
      });
      await fixture.whenStable();
      expect(fixture.nativeElement.querySelector('test-problem-view')).toBeNull();
      expect(fixture.nativeElement.textContent).toContain('Solution view data');
      expect(fixture.nativeElement.textContent).toContain('"nodes": 5');
    }
  });

  it('remains useful without a provider and never interprets property text as HTML', async () => {
    const fixture = TestBed.createComponent(SolutionRenderer);
    fixture.componentRef.setInput('detail', {
      ...detail,
      result: normalResult({ properties: { text: '<script>alert(1)</script>' } }),
      rendering: { state: 'UNSUPPORTED', rendererId: null, data: {} },
    });
    fixture.componentRef.setInput('selectedObjective', 'Cost');
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('not provided');
    expect(fixture.nativeElement.textContent).toContain('<script>alert(1)</script>');
    expect(fixture.nativeElement.querySelector('script')).toBeNull();
  });
});
