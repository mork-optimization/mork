package es.urjc.etsii.grafo.autoconfig.fakecomponents;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;

import java.util.List;

public class FakeGRASPListManager extends GRASPListManager<TestMove, TestSolution, TestInstance> {

    @AutoconfigConstructor
    public FakeGRASPListManager() {
    }

    @Override
    public List<TestMove> buildInitialCandidateList(TestSolution solution) {
        throw new UnsupportedOperationException("Only for testing");
    }

    @Override
    public List<TestMove> updateCandidateList(TestSolution solution, TestMove move, List<TestMove> candidateList, int index) {
        return buildInitialCandidateList(solution);
    }
}
