package es.urjc.etsii.grafo.create.grasp;

import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestCommonUtils;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.random.RandomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static es.urjc.etsii.grafo.algorithms.FMode.MAXIMIZE;
import static org.junit.jupiter.api.Assertions.*;

class GRASPConstructiveMaximizingTest {

    private final Objective<TestMove,TestSolution,TestInstance> maxObj = Objective.ofMaximizing("TestMax", TestSolution::getScore, TestMove::getScoreChange);

    TestGRASPListManager listManager;
    List<TestMove> moves;
    TestSolution solution;
    private GreedyRandomGRASPConstructive<TestMove, TestSolution, TestInstance> gr;
    private RandomGreedyGRASPConstructive<TestMove, TestSolution, TestInstance> rg;
    private TestInstance instance;

    @BeforeEach
    void setUp(){
        var config = TestCommonUtils.solverConfig(RandomType.DEFAULT, 0, 1);
        TestCommonUtils.initRandom(config);
        this.instance = new TestInstance("testinstance");
        this.solution = new TestSolution(this.instance);
        this.moves = new ArrayList<>(Arrays.asList(
                new TestMove(this.solution, -10, MAXIMIZE),
                new TestMove(this.solution, 0, MAXIMIZE),
                new TestMove(this.solution, 1, MAXIMIZE),
                new TestMove(this.solution, 3, MAXIMIZE),
                new TestMove(this.solution, 5, MAXIMIZE),
                new TestMove(this.solution, 7, MAXIMIZE)
        ));
        this.listManager = new TestGRASPListManager(this.moves);
        this.gr = new GreedyRandomGRASPConstructive<>(maxObj, listManager, ()  -> 0, "Fixed{0}");
        this.rg = new RandomGreedyGRASPConstructive<>(maxObj, listManager, ()  -> 0, "Fixed{0}");
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
        var moves = (List<TestMove>) (Object) builtSolution.lastExecutesMoves();
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
        var moves = (List<TestMove>) (Object) builtSolution.lastExecutesMoves();
        // Verify that moves are executed in decreasing score order, as grasp is run with alpha 0 = greedy
        descending(moves);
    }

    private void descending(List<TestMove> moves){
        double last = maxObj.evalMove(moves.getFirst());
        for (int i = 1; i < moves.size(); i++) {
            double current = maxObj.evalMove(moves.get(i));
            assertTrue(DoubleComparator.isLessOrEquals(current, last));
            last = current;
        }
    }
}