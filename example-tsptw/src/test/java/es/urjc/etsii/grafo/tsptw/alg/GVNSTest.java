package es.urjc.etsii.grafo.tsptw.alg;

import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.metrics.MetricUtil;
import es.urjc.etsii.grafo.tsptw.Main;
import es.urjc.etsii.grafo.tsptw.constructives.TSPTWFeasibleConstructive;
import es.urjc.etsii.grafo.tsptw.improve.TSPTWVND;
import es.urjc.etsii.grafo.tsptw.model.TSPTWInstance;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolutionValidator;
import es.urjc.etsii.grafo.tsptw.shake.TSPTWFeasibleInsertShake;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
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
        var constructive = new ScriptedConstructive(List.of(tour(instance, 1, 2, 3, 4, 5),
                tour(instance, 1, 2, 5, 3, 4), tour(instance, 5, 1, 2, 3, 4)));
        var shake = new RecordingShake();
        var algorithm = new GVNS("test", 2, 1, constructive, shake, new NoSearch());
        startTimeLimit();
        var result = algorithm.algorithm(instance);
        assertEquals(3, constructive.calls);
        assertEquals(List.of(1, 1), shake.levels);
        assertEquals(113, result.cost());
        assertConsistent(result);
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
        var constructive = new ScriptedConstructive(List.of(feasible, infeasible));
        var algorithm = new GVNS("test", 2, 1, constructive, new RecordingShake(), new NoSearch());
        startTimeLimit();
        var result = algorithm.algorithm(instance);
        assertEquals(feasible.cost(), result.cost());
        assertEquals(0, result.constraint_violations());
    }

    @Test
    void rejectsTinyInstancesAndRequiresFrameworkTimeBudget() throws Exception {
        for (int n = 1; n <= 3; n++) {
            var instance = new TSPTWInstance("tiny", n, new double[n][n], new int[n], new int[n]);
            assertThrows(IllegalArgumentException.class, () -> new GVNS().algorithm(instance));
        }
        var instance = instance("best");
        assertThrows(IllegalStateException.class, () -> new GVNS().algorithm(instance));
    }

    @Test
    void realSearchReturnsFeasibleTourWhenBudgetExpires() throws Exception {
        var instance = instance("best");
        TimeControl.setMaxExecutionTime(50, TimeUnit.MILLISECONDS);
        TimeControl.start();
        var result = new GVNS().algorithm(instance);
        assertTrue(new TSPTWSolutionValidator().validate(result).isValid());
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
        assertConsistent(result);
        assertFalse(new TSPTWSolutionValidator().validate(result).isValid());
    }

    @Test
    void resetsLevelsAndAttemptsAfterImprovementAndRestoresRejectedCandidates() throws Exception {
        var instance = instance("best");
        var original = tour(instance, 1, 2, 3, 4, 5);
        var improved = tour(instance, 1, 2, 5, 3, 4);
        var worse = tour(instance, 5, 1, 2, 3, 4);
        var constructive = new ScriptedConstructive(List.of(original, original));
        var levels = new ArrayList<Integer>();
        var shake = new TSPTWFeasibleInsertShake() {
            @Override public TSPTWSolution shake(TSPTWSolution solution, int k) {
                levels.add(k);
                assertEquals(levels.size() <= 3 ? original.cost() : improved.cost(), solution.cost());
                solution.copy_from(worse);
                return solution;
            }
        };
        var search = new TSPTWVND() {
            int calls;
            @Override public TSPTWSolution improve(TSPTWSolution solution) {
                calls++;
                if (calls == 3) solution.copy_from(improved);
                if (calls == 7) expireTimeLimit();
                return solution;
            }
        };
        startTimeLimit();
        var result = new GVNS("test", 3, 2, constructive, shake, search).algorithm(instance);
        assertEquals(List.of(1, 1, 2, 1, 1, 2, 2), levels);
        assertEquals(improved.cost(), result.cost());
        assertEquals(1, constructive.calls);
    }

    @Test
    void originalScheduleMakes31AttemptsAtEachOfSevenLevels() throws Exception {
        var instance = instance("best");
        var initial = tour(instance, 1, 2, 3, 4, 5);
        var constructive = new ScriptedConstructive(List.of(initial, initial));
        var shake = new RecordingShake();
        startTimeLimit();
        new GVNS("test", 8, 31, constructive, shake, new NoSearch()).algorithm(instance);
        assertEquals(217, shake.levels.size());
        for (int i = 0; i < 217; i++) assertEquals(1 + i / 31, shake.levels.get(i));
    }

    @Test
    void reportsFirstFeasibleSolutionBeforeRefinementAndKeepsInfeasibleScoresOutOfMetrics() throws Exception {
        enableMetrics();
        var instance = instance("best");
        var initial = tour(instance, 1, 2, 3, 4, 5);
        var improved = tour(instance, 1, 2, 5, 3, 4);
        var constructive = new ScriptedConstructive(List.of(initial, initial));
        var search = new TSPTWVND() {
            @Override public TSPTWSolution improve(TSPTWSolution solution) {
                var values = Metrics.get("Cost").getValues();
                assertEquals(1, values.size());
                assertEquals(initial.cost(), values.first().value());
                solution.copy_from(improved);
                expireTimeLimit();
                return solution;
            }
        };
        startTimeLimit();
        new GVNS("test", 2, 1, constructive, new RecordingShake(), search).algorithm(instance);
        var values = Metrics.get("Cost").getValues();
        assertEquals(2, values.size());
        assertEquals(improved.cost(), values.last().value());
        assertTrue(Double.isFinite(MetricUtil.areaUnderCurve(Main.OBJECTIVE, values.first().instant(),
                values.last().instant() - values.first().instant() + 1, true)));

        Metrics.resetMetrics();
        var impossible = new TSPTWInstance("impossible", 4,
                new double[][]{{0, 1, 1, 1}, {1, 0, 1, 1}, {1, 1, 0, 1}, {1, 1, 1, 0}},
                new int[4], new int[4]);
        new GVNS().algorithm(impossible);
        assertTrue(Metrics.get("Cost").getValues().isEmpty());
    }

    private static class ScriptedConstructive extends TSPTWFeasibleConstructive {
        private final List<TSPTWSolution> candidates;
        int calls;
        ScriptedConstructive(List<TSPTWSolution> candidates) { this.candidates = candidates; }
        @Override public TSPTWSolution construct(TSPTWSolution empty) {
            var candidate = candidates.get(calls++).cloneSolution();
            if (calls == candidates.size()) expireTimeLimit();
            return candidate;
        }
    }

    private static class RecordingShake extends TSPTWFeasibleInsertShake {
        final List<Integer> levels = new ArrayList<>();
        @Override public TSPTWSolution shake(TSPTWSolution solution, int k) {
            levels.add(k);
            return solution;
        }
    }

    private static class NoSearch extends TSPTWVND {
        @Override public TSPTWSolution improve(TSPTWSolution solution) { return solution; }
    }
}
