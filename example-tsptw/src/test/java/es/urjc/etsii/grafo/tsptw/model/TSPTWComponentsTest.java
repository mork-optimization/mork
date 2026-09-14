package es.urjc.etsii.grafo.tsptw.model;

import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.tsptw.constructives.TSPTWFeasibleConstructive;
import es.urjc.etsii.grafo.tsptw.constructives.TSPTWRandomConstructive;
import es.urjc.etsii.grafo.tsptw.improve.TSPTWCostSearch;
import es.urjc.etsii.grafo.tsptw.improve.TSPTWInsertionSearch;
import es.urjc.etsii.grafo.tsptw.improve.TSPTWTwoOptSearch;
import es.urjc.etsii.grafo.tsptw.improve.TSPTWVND;
import es.urjc.etsii.grafo.tsptw.repair.*;
import es.urjc.etsii.grafo.tsptw.shake.TSPTWFeasibleInsertShake;
import es.urjc.etsii.grafo.tsptw.shake.TSPTWUnrestrictedInsertShake;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.random.RandomGenerator;

import static es.urjc.etsii.grafo.tsptw.model.TSPTWTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Timeout(10)
class TSPTWComponentsTest {
    @BeforeEach void setUp() { initialize(); }
    @AfterEach void tearDown() { cleanup(); }

    @Test
    void classifiesCustomersByTheirOwnDeadlinesAndRepairsReorderedTour() {
        var instance = new TSPTWInstance("positions", 4,
                new double[][]{{0, 1, 1, 1}, {1, 0, 1, 1}, {1, 1, 0, 1}, {1, 1, 1, 0}},
                new int[4], new int[]{100, 1, 100, 100});
        var solution = tour(instance, 2, 1, 3);
        assertFalse(solution.isLateAt(1));
        assertTrue(solution.isLateAt(2));
        assertEquals(List.of(2), TSPTWUtil.shuffledPositions(solution, true));
        new TSPTWBackwardViolated().repair(solution);
        assertEquals(List.of(0, 1, 2, 3, 0), solution.permutation);
        assertEquals(0, solution.constraint_violations());
        assertConsistent(solution);
    }

    @Test
    void repairCanIncreaseCostAndDoesNotPublishInfeasibleObjectiveValues() {
        var instance = new TSPTWInstance("cost-vs-feasibility", 4,
                new double[][]{{0, 10, 1, 1}, {10, 0, 1, 1}, {1, 1, 0, 1}, {1, 1, 1, 0}},
                new int[]{0, 0, 20, 0}, new int[]{1000, 15, 1000, 1000});
        var solution = tour(instance, 2, 1, 3);
        enableMetrics();
        assertEquals(4, solution.cost());
        assertThrows(IllegalArgumentException.class, () -> new TSPTWInsertionSearch().improve(solution));
        assertThrows(IllegalArgumentException.class, () -> new TSPTWVND().improve(solution));
        assertThrows(IllegalArgumentException.class, () -> new TSPTWFeasibleInsertShake().shake(solution, 1));
        new TSPTWFeasibilityRepair().repair(solution);
        assertEquals(13, solution.cost());
        assertEquals(0, solution.constraint_violations());
        assertConsistent(solution);
        assertTrue(Metrics.get("Cost").getValues().isEmpty());
    }

    @Test
    void emptyCustomerSetsAndDepotOnlyViolationAreSafe() {
        var depotOnly = new TSPTWInstance("depot-only", 4,
                new double[][]{{0, 1, 1, 1}, {1, 0, 1, 1}, {1, 1, 0, 1}, {1, 1, 1, 0}},
                new int[4], new int[]{3, 100, 100, 100});
        var solution = tour(depotOnly, 1, 2, 3);
        assertTrue(TSPTWUtil.shuffledPositions(solution, true).isEmpty());
        new TSPTWFeasibilityRepair().repair(solution);
        assertEquals(1, solution.constraint_violations());
        assertConsistent(solution);

        var allLate = new TSPTWInstance("all-late", 4, depotOnly.getDistance(), new int[4], new int[4]);
        solution = tour(allLate, 1, 2, 3);
        assertTrue(TSPTWUtil.shuffledPositions(solution, false).isEmpty());
        new TSPTWFeasibilityRepair().repair(solution);
        assertConsistent(solution);
    }

    @Test
    void expiredDeadlineLeavesPerturbationAndRepairStateUnchanged() throws Exception {
        var solution = tour(instance("best"), 1, 2, 3, 4, 5);
        var before = solution.toString();
        expireTimeLimit();
        new TSPTWFeasibleInsertShake().shake(solution, 8);
        new TSPTWUnrestrictedInsertShake().shake(solution, 8);
        assertEquals(before, solution.toString());
        assertConsistent(solution);
        var infeasible = tour(instance("prune"), 2, 1, 3);
        before = infeasible.toString();
        new TSPTWFeasibilityRepair().repair(infeasible);
        assertEquals(before, infeasible.toString());
        assertConsistent(infeasible);
    }

    @Test
    void interruptedUnrestrictedPerturbationReevaluatesCompletedInsertions() throws Exception {
        var solution = tour(instance("best"), 1, 2, 3, 4, 5);
        var rng = mock(RandomGenerator.JumpableGenerator.class);
        int[] values = {0, 2, 0, 1};
        var calls = new AtomicInteger();
        when(rng.nextInt(anyInt())).thenAnswer(invocation -> {
            int call = calls.getAndIncrement();
            if (call == 2) expireTimeLimit();
            return values[call];
        });
        Context.Configurator.setRandom(rng);
        new TSPTWUnrestrictedInsertShake().shake(solution, 8);
        assertEquals(List.of(0, 2, 3, 1, 4, 5, 0), solution.permutation);
        assertConsistent(solution);
    }

    @Test
    void interruptionDuringFeasiblePerturbationAndRepairLeavesConsistentIncumbents() throws Exception {
        var solution = new InterruptingSolution(tour(instance("best"), 1, 2, 3, 4, 5));
        var rng = mock(RandomGenerator.JumpableGenerator.class);
        // Choose position 4 and shift it back toward position 1.
        when(rng.nextInt(anyInt())).thenReturn(3, 0);
        Context.Configurator.setRandom(rng);
        new TSPTWFeasibleInsertShake().shake(solution, 8);
        assertEquals(1, solution.swaps.get());
        assertEquals(0, solution.constraint_violations());
        assertConsistent(solution);

        startTimeLimit();
        var instance = new TSPTWInstance("repair-interrupt", 4,
                new double[][]{{0, 1, 1, 1}, {1, 0, 1, 1}, {1, 1, 0, 1}, {1, 1, 1, 0}},
                new int[4], new int[]{100, 1, 100, 100});
        solution = new InterruptingSolution(tour(instance, 2, 1, 3));
        new TSPTWBackwardViolated().repair(solution);
        assertEquals(1, solution.swaps.get());
        assertEquals(0, solution.constraint_violations());
        assertConsistent(solution);
    }

    @Test
    void feasibleConstructionRestartsAfterExhaustingItsSizeDependentLevels() throws Exception {
        var instance = instance("prune"); // n / 2 == 2: only level 1 is attempted before restarting.
        var feasible = tour(instance, 1, 2, 3);
        var infeasible = tour(instance, 2, 1, 3);
        var calls = new AtomicInteger();
        var initial = new TSPTWRandomConstructive() {
            @Override public TSPTWSolution construct(TSPTWSolution empty) {
                return (calls.incrementAndGet() == 1 ? infeasible : feasible).cloneSolution();
            }
        };
        var levels = new ArrayList<Integer>();
        var perturbation = new TSPTWUnrestrictedInsertShake() {
            @Override public TSPTWSolution shake(TSPTWSolution solution, int k) {
                levels.add(k);
                return solution;
            }
        };
        var repair = new TSPTWFeasibilityRepair() {
            @Override public void repair(TSPTWSolution solution) {}
        };
        var result = new TSPTWFeasibleConstructive(initial, repair, perturbation).construct(new TSPTWSolution(instance));
        assertEquals(List.of(1), levels);
        assertEquals(2, calls.get());
        assertEquals(0, result.constraint_violations());
    }

    @Test
    void vndRevisitsFirstSearchOnlyWhenTheLaterSearchImproves() throws Exception {
        var instance = instance("best");
        var solution = tour(instance, 1, 2, 3, 4, 5);
        var improved = tour(instance, 1, 2, 5, 3, 4);
        var order = new ArrayList<String>();
        var first = new TSPTWInsertionSearch() {
            @Override public TSPTWSolution improve(TSPTWSolution s) { order.add("insertion"); return s; }
        };
        var second = new TSPTWTwoOptSearch() {
            @Override public TSPTWSolution improve(TSPTWSolution s) { order.add("2opt"); s.copy_from(improved); return s; }
        };
        new TSPTWVND(List.of(first, second)).improve(solution);
        assertEquals(List.of("insertion", "2opt", "insertion", "2opt"), order);
        order.clear();
        new TSPTWVND(List.of(first)).improve(solution);
        assertEquals(List.of("insertion"), order);
    }

    @Test
    void generatedIntegerInstancesPreserveMoveCachesAndComponentContracts() {
        var random = new Random(81251);
        for (int test = 0; test < 100; test++) {
            int n = 4 + random.nextInt(6);
            int[] x = new int[n], y = new int[n], starts = new int[n], ends = new int[n];
            for (int i = 0; i < n; i++) {
                x[i] = random.nextInt(50);
                y[i] = random.nextInt(50);
                starts[i] = i == 0 ? 0 : random.nextInt(100);
            }
            Arrays.fill(ends, 100000);
            double[][] distances = new double[n][n];
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) distances[i][j] = Math.abs(x[i] - x[j]) + Math.abs(y[i] - y[j]);
            }
            var customers = new ArrayList<Integer>();
            for (int i = 1; i < n; i++) customers.add(i);
            for (int i = customers.size() - 1; i > 0; i--) {
                Collections.swap(customers, i, random.nextInt(i + 1));
            }
            int[] permutation = new int[n - 1];
            for (int i = 0; i < n - 1; i++) permutation[i] = customers.get(i);
            var broad = tour(new TSPTWInstance("broad", n, distances, starts, ends), permutation);
            for (int i = 1; i <= n; i++) ends[broad.permutation.get(i)] = (int) broad._makespan[i] + random.nextInt(50);
            var instance = new TSPTWInstance("case" + test, n, distances, starts, ends);
            var initial = tour(instance, permutation);
            assertConsistent(initial);
            for (int k = 1; k < n - 1; k++) {
                var candidate = initial.cloneSolution();
                candidate.swap(k);
                assertConsistent(candidate);
            }
            for (int level = 1; level <= 8; level++) {
                var candidate = initial.cloneSolution();
                new TSPTWFeasibleInsertShake().shake(candidate, level);
                assertEquals(0, candidate.constraint_violations());
                assertConsistent(candidate);
                candidate = initial.cloneSolution();
                new TSPTWUnrestrictedInsertShake().shake(candidate, level);
                assertConsistent(candidate);
                double before = candidate.infeasibility();
                new TSPTWFeasibilityRepair().repair(candidate);
                assertTrue(candidate.infeasibility() <= before);
                assertConsistent(candidate);
            }
            for (TSPTWCostSearch search : List.of(new TSPTWInsertionSearch(), new TSPTWTwoOptSearch())) {
                var candidate = search.improve(initial.cloneSolution());
                assertEquals(0, candidate.constraint_violations());
                assertTrue(candidate.cost() <= initial.cost());
                assertConsistent(candidate);
            }
        }
    }

    private static class InterruptingSolution extends TSPTWSolution {
        final AtomicInteger swaps;
        InterruptingSolution(TSPTWSolution solution) {
            super(solution);
            swaps = solution instanceof InterruptingSolution other ? other.swaps : new AtomicInteger();
        }
        @Override public TSPTWSolution cloneSolution() { return new InterruptingSolution(this); }
        @Override public double do_swap(int k) {
            double gain = super.do_swap(k);
            swaps.incrementAndGet();
            expireTimeLimit();
            return gain;
        }
    }
}
