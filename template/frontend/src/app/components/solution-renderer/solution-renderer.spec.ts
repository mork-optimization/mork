import { Component, input } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { normalResult } from '../../testing/normal-experiment-fixture';
import { ResultDetail, ResultView } from '../../model/normal-experiment';
import { SOLUTION_RENDERERS, SolutionRenderer } from './solution-renderer';

@Component({
  selector: 'test-problem-view',
  template: '{{ result().instanceName }} · {{ selectedObjective() }} · {{ view()["nodes"] }}',
})
class ProblemView {
  readonly result = input.required<ResultView>();
  readonly selectedObjective = input.required<string | null>();
  readonly view = input.required<Readonly<Record<string, unknown>>>();
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
      providers: [{ provide: SOLUTION_RENDERERS, useValue: { problem: ProblemView } }],
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
