package es.urjc.etsii.grafo.tsptw.model;

import es.urjc.etsii.grafo.tsptw.improve.*;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.random.RandomType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static es.urjc.etsii.grafo.tsptw.model.TSPTWNewTestUtil.*;
import static es.urjc.etsii.grafo.tsptw.model.TSPTWTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

@Timeout(20)
class TSPTWNewMoveTest {
    @BeforeEach void setUp() { initialize(); }
    @AfterEach void tearDown() { cleanup(); }

    @Test
    void everyRelocationAndExchangeMatchesIndependentFullEvaluation() {
        var random = new Random(19482);
        for (int test = 0; test < 80; test++) {
            var original = generated(random, 4 + random.nextInt(6), test % 2 == 0);
            var route = TSPTWNewMoveUtil.route(original);
            var arrivals = original._makespan.clone();
            for (int length = 1; length <= 3; length++) {
                for (int from = 1; from <= original.n - length; from++) {
                    for (int to = 1; to <= original.n - length; to++) {
                        if (from == to) continue;
                        var expected = new ArrayList<>(original.permutation);
                        var block = new ArrayList<>(expected.subList(from, from + length));
                        expected.subList(from, from + length).clear();
                        expected.addAll(to, block);
                        int[] candidate = new int[route.length];
                        TSPTWNewMoveUtil.relocate(route, candidate, from, to, length);
                        for (int i = 0; i < candidate.length; i++) assertEquals(expected.get(i), candidate[i]);
                        var full = rebuild(original.getInstance(), candidate);
                        double cost = original.cost() + TSPTWNewMoveUtil.relocationDelta(original.getInstance(), route, from, to, length);
                        assertEquals(full.cost(), cost);
                        checkEvaluation(original, candidate, full, cost, Math.min(from, to), Math.max(from, to) + length - 1);
                    }
                }
            }
            for (int first = 1; first < original.n; first++) {
                for (int second = first + 1; second < original.n; second++) {
                    int[] candidate = route.clone();
                    candidate[first] = route[second];
                    candidate[second] = route[first];
                    var full = rebuild(original.getInstance(), candidate);
                    double cost = original.cost() + TSPTWNewMoveUtil.swapDelta(original.getInstance(), route, first, second);
                    assertEquals(full.cost(), cost);
                    checkEvaluation(original, candidate, full, cost, first, second);
                }
            }
            assertArrayEquals(route, TSPTWNewMoveUtil.route(original));
            assertArrayEquals(arrivals, original._makespan);
        }
    }

    private static void checkEvaluation(TSPTWSolution original, int[] candidate, TSPTWSolution full,
                                        double cost, int first, int last) {
        double[] scratch = new double[candidate.length];
        boolean feasible = TSPTWNewMoveUtil.evaluateFeasible(original, candidate, scratch, first, last);
        assertEquals(full.constraint_violations() == 0, feasible);
        if (feasible) {
            assertArrayEquals(full._makespan, scratch);
            var committed = original.cloneSolution();
            TSPTWNewMoveUtil.commit(committed, candidate, scratch, cost);
            assertConsistent(committed);
            assertTrue(new TSPTWSolutionValidator().validate(committed).isValid());
        }
    }

    @Test
    void insertionFindsThePreviouslyMissedFinalCustomerMove() {
        var original = counterexample();
        assertEquals(114, new TSPTWInsertionSearch().improve(original.cloneSolution()).cost());
        for (var selection : TSPTWInsertionSearchNew.Selection.values()) {
            var result = new TSPTWInsertionSearchNew(selection).improve(original.cloneSolution());
            assertTrue(result.cost() <= 108);
            assertConsistent(result);
            assertEquals(0, result.constraint_violations());
        }
    }

    @Test
    void bestInsertionChoosesTheBestFeasibleCandidateBeforeChangingLiveCaches() {
        var random = new Random(912);
        for (int test = 0; test < 40; test++) {
            var solution = generated(random, 8, true);
            double bestCost = bestRelocationCost(solution, 1);
            boolean improved = TSPTWNewMoveUtil.improveRelocation(solution, 1, false);
            assertEquals(bestCost, solution.cost());
            assertConsistent(solution);
            if (improved) assertEquals(0, solution.constraint_violations());
        }
    }

    private static double bestRelocationCost(TSPTWSolution solution, int length) {
        double bestCost = solution.cost();
        for (int from = 1; from <= solution.n - length; from++) {
            for (int to = 1; to <= solution.n - length; to++) {
                if (from == to) continue;
                var candidate = new ArrayList<>(solution.permutation);
                var block = new ArrayList<>(candidate.subList(from, from + length));
                candidate.subList(from, from + length).clear();
                candidate.addAll(to, block);
                int[] route = new int[candidate.size()];
                for (int i = 0; i < route.length; i++) route[i] = candidate.get(i);
                var full = rebuild(solution.getInstance(), route);
                if (full.constraint_violations() == 0) bestCost = Math.min(bestCost, full.cost());
            }
        }
        return bestCost;
    }

    @Test
    void completedSearchesExhaustTheirDeclaredNeighborhoods() {
        var random = new Random(93);
        for (int test = 0; test < 30; test++) {
            var original = generated(random, 8, test % 2 == 0);
            for (int length = 1; length <= 3; length++) {
                TSPTWCostSearch search = length == 1 ? new TSPTWInsertionSearchNew() : new TSPTWOrOptSearchNew(length);
                var result = search.improve(original.cloneSolution());
                assertEquals(result.cost(), bestRelocationCost(result, length));
                assertTrue(result.cost() <= original.cost());
                assertConsistent(result);
            }
            var result = new TSPTWSwapSearchNew().improve(original.cloneSolution());
            for (int first = 1; first < result.n; first++) {
                for (int second = first + 1; second < result.n; second++) {
                    var order = new ArrayList<>(result.permutation);
                    Collections.swap(order, first, second);
                    int[] customers = new int[result.n - 1];
                    for (int i = 1; i < result.n; i++) customers[i - 1] = order.get(i);
                    var candidate = tour(result.getInstance(), customers);
                    assertTrue(candidate.constraint_violations() > 0 || candidate.cost() >= result.cost());
                }
            }
            assertConsistent(result);
        }
    }

    @Test
    void optimizedTwoOptPreservesOriginalTrajectoryAndCacheValues() {
        var random = new Random(417);
        for (int test = 0; test < 100; test++) {
            var original = generated(random, 4 + random.nextInt(9), test % 2 == 0);
            Context.Configurator.resetRandom(RandomType.DEFAULT, test);
            var baseline = new TSPTWTwoOptSearch().improve(original.cloneSolution());
            Context.Configurator.resetRandom(RandomType.DEFAULT, test);
            var variant = new TSPTWTwoOptSearchNew().improve(original.cloneSolution());
            assertEquals(baseline.permutation, variant.permutation);
            assertEquals(baseline.cost(), variant.cost());
            assertArrayEquals(baseline._makespan, variant._makespan);
            assertConsistent(variant);
        }
    }

    @Test
    void deadlineAndFeasibilityGuardsPreserveTheInput() throws Exception {
        var feasible = counterexample();
        var late = tour(instance("prune"), 2, 1, 3);
        for (TSPTWCostSearch search : List.of(new TSPTWInsertionSearchNew(), new TSPTWTwoOptSearchNew(),
                new TSPTWSwapSearchNew(), new TSPTWOrOptSearchNew())) {
            assertThrows(IllegalArgumentException.class, () -> search.improve(late));
            expireTimeLimit();
            var copy = feasible.cloneSolution();
            search.improve(copy);
            assertEquals(feasible.permutation, copy.permutation);
            assertArrayEquals(feasible._makespan, copy._makespan);
        }
        startTimeLimit();
        assertThrows(IllegalArgumentException.class, () -> new TSPTWTwoOptSearchNew().improve(tour(instance("symmetry"), 1, 2, 3)));
    }
}
