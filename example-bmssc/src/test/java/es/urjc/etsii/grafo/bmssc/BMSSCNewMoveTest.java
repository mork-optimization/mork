package es.urjc.etsii.grafo.bmssc;

import es.urjc.etsii.grafo.bmssc.create.RandomConstructorNew;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.*;
import es.urjc.etsii.grafo.improve.ls.LocalSearch;
import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchCachedBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchFirstImprovement;
import es.urjc.etsii.grafo.metrics.AbstractMetric;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.shake.RandomMoveShake;
import es.urjc.etsii.grafo.solution.RefreshableMove;
import es.urjc.etsii.grafo.solution.neighborhood.RandomizableNeighborhood;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static es.urjc.etsii.grafo.bmssc.BMSSCTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BMSSCNewMoveTest {
    @BeforeEach
    void setup() { initialize(1234); }

    @AfterEach
    void teardown() { cleanup(); }

    @Test
    void exhaustiveCompoundDeltasMatchIndependentCostsAndPreserveOriginals() {
        for (int k : new int[]{1, 2, 3, 4, 7}) {
            var original = new RandomConstructorNew().construct(new BMSSCSolution(instance(7, k)));
            int expectedCycles = 0;
            var cycles = new ThreeCycleNeighborhood().explore(original).moves().toList();
            for (int p = 0; p < 7; p++) {
                for (int q = p + 1; q < 7; q++) {
                    for (int r = q + 1; r < 7; r++) {
                        if (original.canSwap(p, q) && original.canSwap(p, r) && original.canSwap(q, r)) expectedCycles += 2;
                    }
                }
            }
            assertEquals(expectedCycles, cycles.size());
            assertEquals(cycles.size(), new HashSet<>(cycles).size());
            for (var move : cycles) checkMove(original, move);
            for (int p = 0; p < 7; p++) {
                for (int q = p + 1; q < 7; q++) {
                    if (original.clusterOf(p) != original.clusterOf(q)) continue;
                    for (int r = 0; r < 7; r++) {
                        for (int s = r + 1; s < 7; s++) {
                            if (!original.canSwap(p, r) || original.clusterOf(r) != original.clusterOf(s)) continue;
                            var move = new TwoForTwoMove(original, p, q, r, s);
                            assertEquals(move, new TwoForTwoMove(original, s, r, q, p));
                            checkMove(original, move);
                        }
                    }
                }
            }
        }
    }

    @Test
    void compoundMovesApplyTheSpecifiedPermutationWithOnePublishedUpdate() {
        var original = assigned(instance(7, 3), 0, 0, 0, 1, 1, 2, 2);
        for (boolean reverse : new boolean[]{false, true}) {
            var solution = spy(original.cloneSolution());
            int[] expected = assignment(original);
            expected[0] = reverse ? 2 : 1;
            expected[3] = reverse ? 0 : 2;
            expected[5] = reverse ? 1 : 0;
            recordEveryObjective();
            new ThreeCycleMove(solution, 0, 3, 5, reverse).execute(solution);
            assertArrayEquals(expected, assignment(solution));
            assertEquals(original.getVersion() + 1, solution.getVersion());
            verify(solution, times(1)).notifyUpdate();
            assertTrue(Metrics.getCurrentThreadMetrics().getMetrics().isEmpty());
            Metrics.addCurrentObjectives(solution);
            assertEquals(1, Metrics.get("Cost").getValues().size());
            assertEquals(solution.getCost(), Metrics.get("Cost").getValues().first().value());
        }
        var solution = spy(original.cloneSolution());
        recordEveryObjective();
        new TwoForTwoMove(solution, 0, 1, 3, 4).execute(solution);
        assertArrayEquals(new int[]{1, 1, 0, 0, 0, 2, 2}, assignment(solution));
        assertEquals(original.getVersion() + 1, solution.getVersion());
        verify(solution, times(1)).notifyUpdate();
        assertTrue(Metrics.getCurrentThreadMetrics().getMetrics().isEmpty());
        Metrics.addCurrentObjectives(solution);
        assertEquals(1, Metrics.get("Cost").getValues().size());
        assertEquals(solution.getCost(), Metrics.get("Cost").getValues().first().value());
    }

    @Test
    void refreshRecomputesValidMovesAndRejectsInvalidatedGroups() {
        var original = assigned(instance(9, 3), 0, 0, 0, 1, 1, 1, 2, 2, 2);
        var cycle = new ThreeCycleMove(original, 0, 3, 6, false);
        var exchange = new TwoForTwoMove(original, 0, 1, 3, 4);
        var solution = original.cloneSolution();
        new SwapMove(solution, 2, 5).execute(solution);
        assertThrows(AssertionError.class, () -> cycle.execute(solution));
        assertThrows(AssertionError.class, () -> exchange.execute(solution));
        checkMove(solution, cycle.refresh(solution).orElseThrow());
        checkMove(solution, exchange.refresh(solution).orElseThrow());
        new SwapMove(solution, 0, 4).execute(solution);
        assertTrue(cycle.refresh(solution).isEmpty());
        assertTrue(exchange.refresh(solution).isEmpty());
    }

    @Test
    void shuffledExplorationAndRandomShakesPreserveNeighborhoodContracts() {
        var original = new RandomConstructorNew().construct(new BMSSCSolution(instance(13, 3)));
        var expected = new HashSet<>(new SwapNeighborhood().explore(original).moves().toList());
        assertEquals(expected, new HashSet<>(new SwapNeighborhoodNew().explore(original).moves().toList()));
        var shuffled = new ShuffledSwapNeighborhood().explore(original).moves().toList();
        assertEquals(expected, new HashSet<>(shuffled));
        assertEquals(expected.size(), shuffled.size());
        var sampled = new SampledTwoForTwoNeighborhood(16).explore(original).moves().toList();
        assertTrue(sampled.size() <= 16);
        assertEquals(sampled.size(), new HashSet<>(sampled).size());
        for (var neighborhood : neighborhoods()) {
            var solution = original.cloneSolution();
            for (int i = 0; i < 30; i++) {
                var move = neighborhood.getRandomMove(solution).orElseThrow();
                double before = solution.getCost();
                move.execute(solution);
                assertEquals(before + move.getCostDelta(), solution.getCost(), 1e-7);
                assertFeasible(solution);
            }
            initialize(9876);
            int[] first = assignment(new RandomMoveShake<>(3, neighborhood).shake(original.cloneSolution(), 4));
            initialize(9876);
            int[] second = assignment(new RandomMoveShake<>(3, neighborhood).shake(original.cloneSolution(), 4));
            assertArrayEquals(first, second);
        }
    }

    @Test
    void existingLocalSearchesAcceptEveryNewMoveType() {
        checkSearches(new SwapNeighborhoodNew(), true);
        checkSearches(new ShuffledSwapNeighborhood(), true);
        checkSearches(new ThreeCycleNeighborhood(), true);
        checkSearches(new SampledTwoForTwoNeighborhood(32), false);
    }

    @Test
    void impossibleMovesAndExpiredDeadlinesTerminate() {
        var singleCluster = new RandomConstructorNew().construct(new BMSSCSolution(instance(8, 1)));
        for (var neighborhood : neighborhoods()) {
            assertTrue(neighborhood.getRandomMove(singleCluster).isEmpty());
            assertEquals(0, neighborhood.explore(singleCluster).moves().count());
        }
        var singletonClusters = new RandomConstructorNew().construct(new BMSSCSolution(instance(8, 8)));
        assertTrue(new SampledTwoForTwoNeighborhood(16).getRandomMove(singletonClusters).isEmpty());
        var twoClusters = new RandomConstructorNew().construct(new BMSSCSolution(instance(8, 2)));
        assertTrue(new ThreeCycleNeighborhood().getRandomMove(twoClusters).isEmpty());
        var solution = new RandomConstructorNew().construct(new BMSSCSolution(instance(9, 3)));
        TimeControl.setMaxExecutionTime(0, TimeUnit.NANOSECONDS);
        TimeControl.start();
        for (var neighborhood : neighborhoods()) {
            assertTrue(neighborhood.getRandomMove(solution).isEmpty());
            assertEquals(0, neighborhood.explore(solution).moves().count());
        }
    }

    private <M extends BMSSCMove & RefreshableMove<M, BMSSCSolution, BMSSCInstance>> void checkSearches(
            RandomizableNeighborhood<M, BMSSCSolution, BMSSCInstance> neighborhood, boolean exhaustive) {
        List<LocalSearch<M, BMSSCSolution, BMSSCInstance>> searches = List.of(
                new LocalSearchFirstImprovement<>(neighborhood), new LocalSearchBestImprovement<>(neighborhood),
                new LocalSearchCachedBestImprovement<>(neighborhood, 4));
        for (var search : searches) {
            for (int k : new int[]{1, 2, 3, 9}) {
                var solution = new RandomConstructorNew().construct(new BMSSCSolution(instance(9, k)));
                double before = solution.getCost();
                assertTimeout(Duration.ofSeconds(5), () -> search.improve(solution));
                assertTrue(solution.getCost() <= before + 1e-7);
                assertFeasible(solution);
                if (exhaustive) {
                    for (var move : neighborhood.explore(solution).moves().toList()) assertTrue(move.getCostDelta() >= -1e-7);
                }
            }
        }
    }

    private void checkMove(BMSSCSolution original, BMSSCMove move) {
        int[] before = assignment(original);
        var copy = original.cloneSolution();
        move.execute(copy);
        assertFeasible(copy);
        assertEquals(cost(original.getInstance(), assignment(copy)) - cost(original.getInstance(), before), move.getCostDelta(), 1e-7);
        assertEquals(original.getVersion() + 1, copy.getVersion());
        assertArrayEquals(before, assignment(original));
        assertFeasible(original);
    }

    private List<RandomizableNeighborhood<? extends BMSSCMove, BMSSCSolution, BMSSCInstance>> neighborhoods() {
        return List.of(new SwapNeighborhoodNew(), new ShuffledSwapNeighborhood(), new ThreeCycleNeighborhood(), new SampledTwoForTwoNeighborhood(16));
    }

    private void recordEveryObjective() {
        Metrics.disableMetrics();
        Metrics.enableMetrics();
        Metrics.register("Cost", reference -> new AbstractMetric(reference) {});
        Metrics.resetMetrics();
    }
}
