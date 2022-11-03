package es.urjc.etsii.grafo.autoconfig.fakecomponents;

import es.urjc.etsii.grafo.algorithms.scattersearch.SolutionCombinator;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;

public class TestSolutionCombinator extends SolutionCombinator<TestSolution, TestInstance>
{
    @Override
    public TestSolution apply(TestSolution testSolution, TestSolution testSolution2) {
        return testSolution;
    }
}
