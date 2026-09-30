package es.urjc.etsii.grafo.tsptw.model;

import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.tsptw.constructives.TSPTWRandomConstructiveNew;
import es.urjc.etsii.grafo.tsptw.improve.*;
import es.urjc.etsii.grafo.tsptw.repair.TSPTWBackwardViolated;
import es.urjc.etsii.grafo.tsptw.repair.TSPTWFeasibilityRepairNew;
import es.urjc.etsii.grafo.tsptw.shake.TSPTWFeasibleInsertShakeNew;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.random.RandomGenerator;

import static es.urjc.etsii.grafo.tsptw.model.TSPTWNewTestUtil.*;
import static es.urjc.etsii.grafo.tsptw.model.TSPTWTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@Timeout(20)
class TSPTWNewComponentsTest {
    @BeforeEach void setUp() { initialize(); }
    @AfterEach void tearDown() { cleanup(); }

    @Test
    void constructiveTradesTravelAgainstUrgencyAndCompletesExpiredOrImpossibleTours() {
        var instance = new TSPTWInstance("priorities", 4,
                new double[][]{{0, 1, 4, 7}, {1, 0, 3, 6}, {4, 3, 0, 3}, {7, 6, 3, 0}},
                new int[4], new int[]{100, 100, 5, 100});
        var nearest = new TSPTWRandomConstructiveNew(0, 1).construct(new TSPTWSolution(instance));
        var urgent = new TSPTWRandomConstructiveNew(1, 1).construct(new TSPTWSolution(instance));
        assertEquals(1, nearest.permutation.get(1));
        assertEquals(2, urgent.permutation.get(1));
        assertConsistent(nearest);
        assertConsistent(urgent);
        var impossible = new TSPTWInstance("impossible", 4, instance.getDistance(), new int[4], new int[4]);
        assertConsistent(new TSPTWRandomConstructiveNew(0.5, 8).construct(new TSPTWSolution(impossible)));
        expireTimeLimit();
        assertConsistent(new TSPTWRandomConstructiveNew().construct(new TSPTWSolution(instance)));
    }

    @Test
    void constructiveHandlesTiesAndInterruptionDuringSelection() {
        var instance = new TSPTWInstance("ties", 6, new double[6][6], new int[6], new int[6]);
        var random = mock(RandomGenerator.JumpableGenerator.class);
        var calls = new AtomicInteger();
        when(random.nextInt(anyInt())).thenAnswer(invocation -> {
            if (calls.incrementAndGet() == 2) expireTimeLimit();
            return 0;
        });
        Context.Configurator.setRandom(random);
        var result = new TSPTWRandomConstructiveNew(0.5, 8).construct(new TSPTWSolution(instance));
        assertEquals(2, calls.get());
        assertConsistent(result);
    }

    @Test
    void feasibleShakeMovesTheFinalCustomerAndKeepsCompletedMovesOnInterruption() throws Exception {
        var solution = tour(instance("best"), 1, 2, 3, 4, 5);
        var random = mock(RandomGenerator.JumpableGenerator.class);
        var calls = new AtomicInteger();
        when(random.nextInt(anyInt())).thenAnswer(invocation -> {
            int call = calls.incrementAndGet();
            if (call == 3) expireTimeLimit();
            return call == 1 ? 4 : 0;
        });
        Context.Configurator.setRandom(random);
        new TSPTWFeasibleInsertShakeNew(2).shake(solution, 2);
        assertEquals(List.of(0, 5, 1, 2, 3, 4, 0), solution.permutation);
        assertConsistent(solution);
        assertEquals(0, solution.constraint_violations());
    }

    @Test
    void shakeBoundsFailedSamplingAndPreservesStateWhenNoMoveIsFeasible() throws Exception {
        var solution = tour(instance("prune"), 1, 2, 3);
        var before = solution.cloneSolution();
        var random = mock(RandomGenerator.JumpableGenerator.class);
        when(random.nextInt(anyInt())).thenReturn(0); // Repeatedly attempts moving customer 1 after customer 2.
        Context.Configurator.setRandom(random);
        new TSPTWFeasibleInsertShakeNew(2).shake(solution, 1);
        verify(random, times(4)).nextInt(anyInt());
        assertEquals(before.permutation, solution.permutation);
        assertArrayEquals(before._makespan, solution._makespan);
        expireTimeLimit();
        new TSPTWFeasibleInsertShakeNew().shake(solution, 5);
        verifyNoMoreInteractions(random);
    }

    @Test
    void repairPassLimitChangesEffortWithoutPublishingInfeasibleMetrics() {
        var instance = new TSPTWInstance("passes", 5,
                new double[][]{{0, 1, 1, 1, 1}, {1, 0, 1, 1, 1}, {1, 1, 0, 1, 1}, {1, 1, 1, 0, 1}, {1, 1, 1, 1, 0}},
                new int[5], new int[]{100, 1, 100, 100, 100});
        var calls = new AtomicInteger();
        var oneStep = new TSPTWBackwardViolated() {
            @Override public void repair(TSPTWSolution solution) {
                calls.incrementAndGet();
                solution.swap(solution.permutation.indexOf(1) - 1);
            }
        };
        enableMetrics();
        var limited = tour(instance, 2, 3, 4, 1);
        new TSPTWFeasibilityRepairNew(List.of(oneStep), 1).repair(limited);
        assertEquals(1, calls.get());
        assertEquals(2, limited.infeasibility());
        assertConsistent(limited);
        calls.set(0);
        var unlimited = tour(instance, 2, 3, 4, 1);
        new TSPTWFeasibilityRepairNew(List.of(oneStep), 0).repair(unlimited);
        assertEquals(3, calls.get());
        assertEquals(0, unlimited.constraint_violations());
        assertConsistent(unlimited);
        assertTrue(Metrics.get("Cost").getValues().isEmpty());
    }

    @Test
    void vndRestartsEarlierNeighborhoodsImmediatelyAfterLaterImprovement() throws Exception {
        var solution = tour(instance("best"), 1, 2, 3, 4, 5);
        var better = tour(solution.getInstance(), 1, 2, 5, 3, 4);
        var order = new ArrayList<String>();
        var first = new TSPTWInsertionSearchNew() {
            @Override public TSPTWSolution improve(TSPTWSolution s) { order.add("first"); return s; }
        };
        var second = new TSPTWTwoOptSearchNew() {
            @Override public TSPTWSolution improve(TSPTWSolution s) { order.add("second"); return better.cloneSolution(); }
        };
        var third = new TSPTWSwapSearchNew() {
            @Override public TSPTWSolution improve(TSPTWSolution s) { order.add("third"); return s; }
        };
        var result = new TSPTWVNDNew(List.of(first, second, third)).improve(solution);
        assertEquals(List.of("first", "second", "first", "second", "third"), order);
        assertEquals(better.cost(), result.cost());
    }

    @Test
    void newConstructionRepairShakeAndVndKeepTheirContractsOnGeneratedInstances() {
        var random = new Random(817);
        for (int test = 0; test < 50; test++) {
            var original = generated(random, 4 + random.nextInt(7), test % 2 == 0);
            var built = new TSPTWRandomConstructiveNew(test % 3 / 2.0, 1 + test % 8).construct(new TSPTWSolution(original.getInstance()));
            assertConsistent(built);
            double lateness = built.infeasibility();
            new TSPTWFeasibilityRepairNew().repair(built);
            assertTrue(built.infeasibility() <= lateness);
            assertConsistent(built);
            for (int level = 1; level <= 8; level++) {
                var copy = original.cloneSolution();
                new TSPTWFeasibleInsertShakeNew().shake(copy, level);
                assertEquals(0, copy.constraint_violations());
                assertConsistent(copy);
            }
            double cost = original.cost();
            new TSPTWVNDNew().improve(original);
            assertTrue(original.cost() <= cost);
            assertConsistent(original);
        }
    }

    @Test
    void newCostSearchPublishesFeasibleImprovingObjectives() {
        enableMetrics();
        var solution = counterexample();
        new TSPTWInsertionSearchNew().improve(solution);
        var values = Metrics.get("Cost").getValues();
        assertFalse(values.isEmpty());
        assertEquals(solution.cost(), values.last().value());
        assertEquals(0, solution.constraint_violations());
    }

    @Test
    void constructorsRejectInvalidParametersAndCompositions() {
        assertThrows(IllegalArgumentException.class, () -> new TSPTWRandomConstructiveNew(Double.NaN, 1));
        assertThrows(IllegalArgumentException.class, () -> new TSPTWRandomConstructiveNew(0.5, 0));
        assertThrows(IllegalArgumentException.class, () -> new TSPTWFeasibleInsertShakeNew(1));
        assertThrows(IllegalArgumentException.class, () -> new TSPTWOrOptSearchNew(1));
        assertThrows(IllegalArgumentException.class, () -> new TSPTWFeasibilityRepairNew(List.of(), 1));
        assertThrows(IllegalArgumentException.class, () -> new TSPTWFeasibilityRepairNew(List.of(new TSPTWBackwardViolated()), -1));
        assertThrows(IllegalArgumentException.class, () -> new TSPTWVNDNew(List.of(new TSPTWInsertionSearchNew())));
        assertThrows(IllegalArgumentException.class, () -> new TSPTWVNDNew(List.of(
                new TSPTWInsertionSearchNew(), new TSPTWInsertionSearchNew(), new TSPTWSwapSearchNew())));
    }
}
