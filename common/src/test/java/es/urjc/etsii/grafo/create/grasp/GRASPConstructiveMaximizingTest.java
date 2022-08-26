package es.urjc.etsii.grafo.create.grasp;

import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.random.RandomManager;
import es.urjc.etsii.grafo.util.random.RandomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GRASPConstructiveMaximizingTest {

    TestGRASPListManager listManager;
    List<TestMove> moves;
    TestSolution solution;
    private GreedyRandomGRASPConstructive<TestMove, TestSolution, TestInstance> gr;
    private RandomGreedyGRASPConstructive<TestMove, TestSolution, TestInstance> rg;
    private TestInstance instance;

    @BeforeEach
    void setUp(){
        RandomManager.globalConfiguration(RandomType.DEFAULT, 0, 1);
        RandomManager.localConfiguration(RandomType.DEFAULT, 0);
        this.instance = new TestInstance("testinstance");
        this.solution = new TestSolution(this.instance);
        this.moves = new ArrayList<>(Arrays.asList(
                new TestMove(this.solution, -10, true),
                new TestMove(this.solution, 0, true),
                new TestMove(this.solution, 1, true),
                new TestMove(this.solution, 3, true),
                new TestMove(this.solution, 5, true),
                new TestMove(this.solution, 7, true)
        ));
        this.listManager = new TestGRASPListManager(this.moves);
        this.gr = new GreedyRandomGRASPConstructive<>(true, listManager, TestMove::getValue, ()  -> 0, "Fixed{0}");
        this.rg = new RandomGreedyGRASPConstructive<>(true, listManager, TestMove::getValue, ()  -> 0, "Fixed{0}");

    }

    @Test
    void doRunGrMaximize(){
        var solution = new TestSolution(this.instance);
        assertFalse(this.listManager.calledBefore);
        assertFalse(this.listManager.calledAfter);
        var builtSolution = this.gr.construct(solution);
        assertTrue(this.listManager.calledBefore);
        assertTrue(this.listManager.calledAfter);
        assertEquals(this.moves.size(), this.listManager.nCalls);
        var moves = builtSolution.lastExecutesMoves();
        // Verify that moves are executed in decreasing score order, as grasp is run with alpha 0 = greedy
        descending(moves);
    }

    @Test
    void doRunRgMaximize(){
        var solution = new TestSolution(this.instance);
        assertFalse(this.listManager.calledBefore);
        assertFalse(this.listManager.calledAfter);
        var builtSolution = this.rg.construct(solution);
        assertTrue(this.listManager.calledBefore);
        assertTrue(this.listManager.calledAfter);
        assertEquals(this.moves.size(), this.listManager.nCalls);
        var moves = builtSolution.lastExecutesMoves();
        // Verify that moves are executed in decreasing score order, as grasp is run with alpha 0 = greedy
        descending(moves);
    }

    private void descending(List<Move<? extends Solution<TestSolution, TestInstance>, TestInstance>> moves){
        double last = moves.get(0).getValue();
        for (int i = 1; i < moves.size(); i++) {
            double current = moves.get(i).getValue();
            assertTrue(DoubleComparator.isLessOrEquals(current, last));
            last = current;
        }
    }
}