package es.urjc.etsii.grafo.tsptw.model;

import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.tsptw.constructives.TSPTWFeasibleConstructive;
import es.urjc.etsii.grafo.tsptw.constructives.TSPTWFeasibleConstructiveBestNew;
import es.urjc.etsii.grafo.tsptw.constructives.TSPTWRandomConstructive;
import es.urjc.etsii.grafo.tsptw.repair.TSPTWFeasibilityRepair;
import es.urjc.etsii.grafo.tsptw.repair.TSPTWFeasibilityRepairFullNew;
import es.urjc.etsii.grafo.tsptw.shake.TSPTWUnrestrictedInsertShake;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static es.urjc.etsii.grafo.tsptw.model.TSPTWTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(20)
class TSPTWFullRepairConstructionTest {
    @BeforeEach void setUp() { initialize(); }
    @AfterEach void tearDown() { cleanup(); }

    @Test
    void fixedBundleMatchesOriginalFullRepairForIdenticalSeeds() {
        var random = new Random(704);
        for (int test = 0; test < 40; test++) {
            var instance = TSPTWNewTestUtil.generated(random, 4 + test % 6, true).getInstance();
            var original = new TSPTWRandomConstructive().construct(new TSPTWSolution(instance));
            var bundled = original.cloneSolution();
            Context.Configurator.resetRandom(RandomType.DEFAULT, test);
            new TSPTWFeasibilityRepair().repair(original);
            Context.Configurator.resetRandom(RandomType.DEFAULT, test);
            new TSPTWFeasibilityRepairFullNew().repair(bundled);
            assertEquals(original.permutation, bundled.permutation);
            assertEquals(original.infeasibility(), bundled.infeasibility());
            assertConsistent(original);
            assertConsistent(bundled);
        }
    }

    @Test
    void retainsEarlierRestartWhileOriginalReturnsLastRestart() {
        var instance = urgentFirstInstance();
        var best = tour(instance, 2, 1, 3, 4);
        var worse = tour(instance, 2, 3, 4, 1);
        enableMetrics();
        var original = new TSPTWFeasibleConstructive(scriptedInitial(List.of(best, worse)), noRepair(), noShake());
        startTimeLimit();
        assertEquals(3, original.construct(new TSPTWSolution(instance)).infeasibility());

        var variant = new TSPTWFeasibleConstructiveBestNew(scriptedInitial(List.of(best, worse)), noRepair(), noShake());
        startTimeLimit();
        var result = variant.construct(new TSPTWSolution(instance));
        assertEquals(1, result.infeasibility());
        assertEquals(best.permutation, result.permutation);
        assertConsistent(result);
        assertTrue(Metrics.get("Cost").getValues().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void ranksRepairedCandidatesByLatenessThenViolationsThenCostAndKeepsExactTies(int scenario) {
        var instance = rankingInstance();
        // The first two cases deliberately trade off criteria: lower lateness despite more
        // violations, then fewer violations despite higher cost.
        int[][] initialTours = {{1,2,4,3,5}, {1,5,2,3,4}, {1,2,4,3,5}, {1,3,4,2,5}};
        int[][] candidateTours = {{1,3,2,4,5}, {1,3,5,2,4}, {1,2,5,4,3}, {1,3,4,5,2}};
        var initial = tour(instance, initialTours[scenario]);
        var candidate = tour(instance, candidateTours[scenario]);
        var repairInput = new AtomicReference<TSPTWSolution>();
        var repair = new TSPTWFeasibilityRepair() {
            int calls;
            @Override public void repair(TSPTWSolution solution) {
                if (++calls == 2) {
                    solution.copy_from(candidate);
                    repairInput.set(solution);
                    expireTimeLimit();
                }
            }
        };
        var initialConstructor = new TSPTWRandomConstructive() {
            @Override public TSPTWSolution construct(TSPTWSolution solution) {
                solution.copy_from(initial);
                return solution;
            }
        };
        var constructive = new TSPTWFeasibleConstructiveBestNew(initialConstructor, repair, noShake());
        startTimeLimit();
        var result = constructive.construct(new TSPTWSolution(instance));
        var expected = scenario == 3 ? initial : candidate;
        assertEquals(expected.permutation, result.permutation);
        assertConsistent(result);
        // These improvements include equal-lateness candidates rejected by the original acceptance rule.
        // Overwriting the live candidate must not alter the saved incumbent.
        repairInput.get().copy_from(tour(instance, 5, 4, 3, 2, 1));
        assertEquals(expected.permutation, result.permutation);
        assertConsistent(result);
    }

    @Test
    void retainedInitialTourIsIndependentOfReusedConstructionStorage() {
        var instance = urgentFirstInstance();
        var best = tour(instance, 2, 1, 3, 4);
        var worse = tour(instance, 2, 3, 4, 1);
        var shared = best.cloneSolution();
        var initial = new TSPTWRandomConstructive() {
            int calls;
            @Override public TSPTWSolution construct(TSPTWSolution empty) {
                if (++calls == 2) {
                    shared.copy_from(worse);
                    expireTimeLimit();
                }
                return shared;
            }
        };
        startTimeLimit();
        var result = new TSPTWFeasibleConstructiveBestNew(initial, noRepair(), noShake())
                .construct(new TSPTWSolution(instance));
        assertEquals(best.permutation, result.permutation);
        assertNotSame(shared, result);
        assertConsistent(result);
    }

    @Test
    void feasibleConstructionStopsWithoutRestartingOrPerturbing() {
        var instance = urgentFirstInstance();
        var calls = new AtomicInteger();
        var initial = new TSPTWRandomConstructive() {
            @Override public TSPTWSolution construct(TSPTWSolution solution) {
                calls.incrementAndGet();
                solution.add(new int[]{2,3,4,1});
                solution.notifyUpdate();
                return solution;
            }
        };
        var unexpectedShake = new TSPTWUnrestrictedInsertShake() {
            @Override public TSPTWSolution shake(TSPTWSolution solution, int k) {
                throw new AssertionError("Full repair should make this tour feasible");
            }
        };
        startTimeLimit();
        var result = new TSPTWFeasibleConstructiveBestNew(initial, new TSPTWFeasibilityRepairFullNew(), unexpectedShake)
                .construct(new TSPTWSolution(instance));
        assertEquals(1, calls.get());
        assertTrue(new TSPTWSolutionValidator().validate(result).isValid());
        assertConsistent(result);
    }

    @Test
    void expiredDeadlineReturnsCompleteConsistentTour() {
        var instance = urgentFirstInstance();
        var initial = tour(instance, 2, 3, 4, 1);
        expireTimeLimit();
        var result = new TSPTWFeasibleConstructiveBestNew(scriptedInitial(List.of(initial)),
                new TSPTWFeasibilityRepairFullNew(), noShake()).construct(new TSPTWSolution(instance));
        assertEquals(initial.permutation, result.permutation);
        assertConsistent(result);
    }

    @Test
    void impossibleInstanceStopsWithInvalidResultAndNoFeasibleMetrics() {
        var instance = new TSPTWInstance("impossible", 5, urgentFirstInstance().getDistance(), new int[5], new int[5]);
        enableMetrics();
        TimeControl.setMaxExecutionTime(10, TimeUnit.MILLISECONDS);
        TimeControl.start();
        var result = new TSPTWFeasibleConstructiveBestNew().construct(new TSPTWSolution(instance));
        assertTrue(TimeControl.isTimeUp());
        assertFalse(new TSPTWSolutionValidator().validate(result).isValid());
        assertConsistent(result);
        assertTrue(Metrics.get("Cost").getValues().isEmpty());
    }

    private static TSPTWRandomConstructive scriptedInitial(List<TSPTWSolution> starts) {
        return new TSPTWRandomConstructive() {
            int calls;
            @Override public TSPTWSolution construct(TSPTWSolution solution) {
                solution.copy_from(starts.get(calls++));
                if (calls == starts.size()) expireTimeLimit();
                return solution;
            }
        };
    }

    private static TSPTWFeasibilityRepair noRepair() {
        return new TSPTWFeasibilityRepair() { @Override public void repair(TSPTWSolution solution) {} };
    }

    private static TSPTWUnrestrictedInsertShake noShake() {
        return new TSPTWUnrestrictedInsertShake() {
            @Override public TSPTWSolution shake(TSPTWSolution solution, int k) { return solution; }
        };
    }

    private static TSPTWInstance urgentFirstInstance() {
        return new TSPTWInstance("urgent-first", 5,
                new double[][]{{0,1,1,1,1}, {1,0,1,1,1}, {1,1,0,1,1}, {1,1,1,0,1}, {1,1,1,1,0}},
                new int[5], new int[]{100,1,100,100,100});
    }

    private static TSPTWInstance rankingInstance() {
        var source = TSPTWNewTestUtil.counterexample().getInstance();
        return new TSPTWInstance("ranking", 6, source.getDistance(), new int[6], new int[]{1000,25,47,91,121,117});
    }
}
