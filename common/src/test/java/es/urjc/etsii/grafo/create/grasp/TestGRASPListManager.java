package es.urjc.etsii.grafo.create.grasp;

import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;

import java.util.ArrayList;
import java.util.List;

public class TestGRASPListManager extends GRASPListManager<TestMove, TestSolution, TestInstance> {

    public List<TestMove> moves;

    public TestGRASPListManager(List<TestMove> moves) {
        this.moves = moves;
    }

    public boolean calledBefore = false, calledAfter = false;
    public int nCalls = 0;

    @Override
    public void beforeGRASP(TestSolution solution) {
        super.beforeGRASP(solution);
        calledBefore = true;
    }

    @Override
    public void afterGRASP(TestSolution solution) {
        super.afterGRASP(solution);
        calledAfter = true;
    }

    @Override
    public List<TestMove> buildInitialCandidateList(TestSolution solution) {
        return moves;
    }

    @Override
    public List<TestMove> updateCandidateList(TestSolution solution, TestMove move, List<TestMove> candidateList, int index) {
        List<TestMove> newCandidates = new ArrayList<>();
        for (int i = 0; i < candidateList.size(); i++) {
            if (i == index) continue;
            TestMove m = candidateList.get(i);
            newCandidates.add(new TestMove(solution, m.getValue(), m.isMaximizing()));
        }
        nCalls++;
        return newCandidates;
    }
}