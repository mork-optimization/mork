package es.urjc.etsii.grafo.tsptw.alg;

import es.urjc.etsii.grafo.tsptw.experiments.TSPTWTimeLimit;
import es.urjc.etsii.grafo.tsptw.model.TSPTWInstance;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolutionValidator;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static es.urjc.etsii.grafo.tsptw.model.TSPTWTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(5)
class GVNSTest {
    @BeforeEach void setUp() { initialize(); }
    @AfterEach void tearDown() { cleanup(); }

    @Test
    void restartsUntilDeadlineAndRetainsBestEarlierRun() throws Exception {
        var instance = instance("best");
        var candidates = List.of(tour(instance, 1, 2, 3, 4, 5),
                tour(instance, 1, 2, 5, 3, 4), tour(instance, 5, 1, 2, 3, 4));
        var algorithm = new ScriptedGVNS(candidates);
        TimeControl.setMaxExecutionTime(new TSPTWTimeLimit().timeLimitInMillis(instance, algorithm), TimeUnit.MILLISECONDS);
        TimeControl.start();
        var result = algorithm.algorithm(instance);
        assertEquals(3, algorithm.restarts);
        assertEquals(2, algorithm.searches);
        assertEquals(113, result.cost());
        assertTrue(new TSPTWSolutionValidator().validate(result).isValid());
    }

    @Test
    void infeasibleFinalRestartCannotReplaceMoreExpensiveFeasibleIncumbent() {
        var instance = new TSPTWInstance("feasibility-first", 4,
                new double[][]{{0, 10, 1, 1}, {10, 0, 1, 1}, {1, 1, 0, 1}, {1, 1, 1, 0}},
                new int[]{0, 0, 20, 0}, new int[]{1000, 15, 1000, 1000});
        var feasible = tour(instance, 1, 2, 3);
        var infeasible = tour(instance, 2, 1, 3);
        assertTrue(infeasible.cost() < feasible.cost());
        assertTrue(infeasible.constraint_violations() > 0);
        var algorithm = new ScriptedGVNS(List.of(feasible, infeasible));
        TimeControl.setMaxExecutionTime(1, TimeUnit.DAYS);
        TimeControl.start();
        var result = algorithm.algorithm(instance);
        assertEquals(feasible.cost(), result.cost());
        assertEquals(0, result.constraint_violations());
    }

    @Test
    void requiresFrameworkTimeBudget() throws Exception {
        var instance = instance("best");
        assertThrows(IllegalStateException.class, () -> new GVNS().algorithm(instance));
    }

    @Test
    void realSearchReturnsFeasibleTourWhenBudgetExpires() throws Exception {
        var instance = instance("best");
        TimeControl.setMaxExecutionTime(50, TimeUnit.MILLISECONDS);
        TimeControl.start();
        var result = new GVNS().algorithm(instance);
        var validation = new TSPTWSolutionValidator().validate(result);
        assertTrue(validation.isValid(), validation.getReasonsFailed().toString());
    }

    @Test
    void impossibleInstanceStopsAndFailsFinalValidation() {
        var instance = new TSPTWInstance("impossible", 4,
                new double[][]{{0, 1, 1, 1}, {1, 0, 1, 1}, {1, 1, 0, 1}, {1, 1, 1, 0}},
                new int[4], new int[4]);
        TimeControl.setMaxExecutionTime(20, TimeUnit.MILLISECONDS);
        TimeControl.start();
        var result = new GVNS().algorithm(instance);
        assertTrue(TimeControl.isTimeUp());
        assertFalse(new TSPTWSolutionValidator().validate(result).isValid());
    }

    private static class ScriptedGVNS extends GVNS {
        private final List<TSPTWSolution> candidates;
        private int restarts;
        private int searches;

        private ScriptedGVNS(List<TSPTWSolution> candidates) {
            this.candidates = candidates;
        }

        @Override public TSPTWSolution vns_feasible(TSPTWInstance instance) {
            var candidate = candidates.get(restarts++);
            if (restarts == candidates.size()) expireTimeLimit();
            return candidate;
        }

        @Override public void gvns(TSPTWSolution solution) {
            searches++;
        }
    }
}
