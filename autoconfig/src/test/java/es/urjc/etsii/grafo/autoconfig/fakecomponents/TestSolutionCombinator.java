package es.urjc.etsii.grafo.autoconfig.fakecomponents;

import es.urjc.etsii.grafo.algorithms.scattersearch.SolutionCombinator;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;

import java.util.ArrayList;
import java.util.List;

public class TestSolutionCombinator extends SolutionCombinator<TestSolution, TestInstance>
{
    @Override
    public List<TestSolution> newSet(List<TestSolution> currentSet) {
        return new ArrayList<>(currentSet);
    }
}
