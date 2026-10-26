package probefixtures;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;

/** Isolated from the default component scan; discovered only by the probe regression tests. */
@AlgorithmComponent
public class ProbeNew extends Algorithm<TestSolution, TestInstance> {
    public ProbeNew() { super("probe-fixture"); }
    @Override public TestSolution algorithm(TestInstance instance) { return new TestSolution(instance, 3); }
}
