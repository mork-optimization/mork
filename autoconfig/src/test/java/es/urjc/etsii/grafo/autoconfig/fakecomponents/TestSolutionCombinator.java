package es.urjc.etsii.grafo.autoconfig.fakecomponents;

import es.urjc.etsii.grafo.algorithms.scattersearch.SolutionCombinator;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;

import java.util.List;

public class TestSolutionCombinator extends SolutionCombinator<TestSolution, TestInstance>
{
    @Override
    protected List<TestSolution> apply(TestSolution left, TestSolution right) {
        return List.of(left);
    }
}
