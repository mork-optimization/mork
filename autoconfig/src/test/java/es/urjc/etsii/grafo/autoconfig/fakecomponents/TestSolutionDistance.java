package es.urjc.etsii.grafo.autoconfig.fakecomponents;

import es.urjc.etsii.grafo.algorithms.scattersearch.SolutionDistance;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;

public class TestSolutionDistance extends SolutionDistance<TestSolution, TestInstance> {
    @Override
    public double distances(TestSolution sa, TestSolution sb) {
        return 1;
    }
}
