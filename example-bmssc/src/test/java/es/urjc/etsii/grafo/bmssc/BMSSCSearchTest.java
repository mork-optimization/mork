package es.urjc.etsii.grafo.bmssc;

import es.urjc.etsii.grafo.bmssc.create.BMSSCGRASPConstructor;
import es.urjc.etsii.grafo.bmssc.create.RandomConstructor;
import es.urjc.etsii.grafo.bmssc.improve.StrategicOscillation;
import es.urjc.etsii.grafo.bmssc.improve.BestImpLS;
import es.urjc.etsii.grafo.bmssc.improve.FirstImpLS;
import es.urjc.etsii.grafo.bmssc.improve.ShakeImprover;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.*;
import es.urjc.etsii.grafo.bmssc.util.BMSSCUtil;
import es.urjc.etsii.grafo.improve.ls.LocalSearch;
import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchCachedBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchFirstImprovement;
import es.urjc.etsii.grafo.metrics.AbstractMetric;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.shake.Shake;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static es.urjc.etsii.grafo.bmssc.BMSSCTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BMSSCSearchTest {
    @BeforeEach
    void setup() { initialize(1234); }

    @AfterEach
    void teardown() { cleanup(); }

    @Test
    void allStandardLocalSearchesTerminateAtSwapLocalOptima() {
        var neighborhood = new SwapNeighborhood();
        List<LocalSearch<SwapMove, BMSSCSolution, BMSSCInstance>> searches = List.of(
                new LocalSearchFirstImprovement<>(neighborhood),
                new LocalSearchBestImprovement<>(neighborhood),
                new LocalSearchCachedBestImprovement<>(neighborhood, 4));
        for (var search : searches) {
            for (int n : new int[]{1, 2, 7, 12}) {
                for (int k : new int[]{1, n, Math.max(1, n / 3)}) {
                    var solution = new RandomConstructor().construct(new BMSSCSolution(instance(n, k)));
                    double before = solution.getCost();
                    assertTimeout(Duration.ofSeconds(5), () -> search.improve(solution));
                    assertTrue(solution.getCost() <= before + 1e-7);
                    assertFeasible(solution);
                    for (var move : neighborhood.explore(solution).moves().toList()) {
                        assertTrue(move.getCostDelta() >= -1e-6);
                    }
                }
            }
        }
    }

    @Test
    void constructionHandlesIdenticalPointsFullSeedClustersAndCancellation() {
        for (int k : new int[]{1, 3, 7}) {
            var instance = new BMSSCInstance("duplicates", 7, 2, k, new double[7][2]);
            var solution = new BMSSCGRASPConstructor(0.68).construct(new BMSSCSolution(instance));
            assertFeasible(solution);
            assertEquals(0, solution.getCost());
        }
        recordEveryObjective();
        expire();
        var solution = new BMSSCGRASPConstructor(0.68).construct(new BMSSCSolution(instance(9, 4)));
        assertFeasible(solution);
        var values = Metrics.get("Cost").getValues();
        assertEquals(1, values.size());
        assertEquals(solution.getCost(), values.first().value());
        assertTrue(Context.Configurator.isValidationEnabled());
        assertFalse(Context.isObjectiveTrackingSuspended());
    }

    @Test
    void timedConstructionReusesFallbackWhenSeedingOrSelectionIsInterrupted() {
        for (boolean duringSelection : new boolean[]{false, true}) {
            TimeControl.remove();
            recordEveryObjective();
            var instance = instance(9, 3);
            var partial = spy(new BMSSCSolution(instance));
            doAnswer(invocation -> {
                assertFalse(TimeControl.isTimeUp(), "Must not rebuild quotas after cancellation");
                return invocation.callRealMethod();
            }).when(partial).getPointAssignments();
            if (duringSelection) {
                doAnswer(invocation -> {
                    var delta = invocation.callRealMethod();
                    if (partial.getVersion() == instance.k) expire();
                    return delta;
                }).when(partial).assignDelta(anyInt(), anyInt());
            } else {
                doAnswer(invocation -> {
                    invocation.callRealMethod();
                    expire();
                    return null;
                }).when(partial).notifyUpdate();
            }
            TimeControl.setMaxExecutionTime(1, TimeUnit.DAYS);
            TimeControl.start();
            var result = new BMSSCGRASPConstructor(0.68).construct(partial);
            assertTrue(TimeControl.isTimeUp());
            assertFalse(partial.feasibleClusterSizes());
            assertArrayEquals(new int[]{0, 0, 0, 1, 1, 1, 2, 2, 2}, result.getPointAssignments());
            verify(partial, times(1)).getPointAssignments();
            assertFeasible(result);
            var values = Metrics.get("Cost").getValues();
            assertEquals(1, values.size());
            assertEquals(result.getCost(), values.first().value());
            assertTrue(Context.Configurator.isValidationEnabled());
            assertFalse(Context.isObjectiveTrackingSuspended());
        }
    }

    @Test
    void completedTimedConstructionMatchesUntimedConstructionAndPublishesOnlyItsResult() {
        var instance = new BMSSCInstance("ordered", 6, 1, 2,
                new double[][]{{0}, {1}, {2}, {3}, {4}, {5}});
        var constructor = new BMSSCGRASPConstructor(0.75);
        for (long seed = 0; seed < 10; seed++) {
            initialize(seed);
            var expected = constructor.construct(new BMSSCSolution(instance));
            initialize(seed);
            recordEveryObjective();
            TimeControl.setMaxExecutionTime(1, TimeUnit.DAYS);
            TimeControl.start();
            var input = new BMSSCSolution(instance);
            var result = constructor.construct(input);
            assertFalse(TimeControl.isTimeUp());
            assertSame(input, result);
            assertArrayEquals(expected.getPointAssignments(), result.getPointAssignments());
            assertEquals(expected.getCost(), result.getCost());
            assertFeasible(result);
            var values = Metrics.get("Cost").getValues();
            assertEquals(1, values.size());
            assertEquals(result.getCost(), values.first().value());
        }
    }

    @Test
    void cancellationDuringConstructionCompletesWithoutPublishingPartialCosts() {
        recordEveryObjective();
        var partial = spy(new BMSSCSolution(instance(9, 3)));
        doAnswer(invocation -> {
            invocation.callRealMethod();
            assertFalse(Context.Configurator.isValidationEnabled());
            assertTrue(Context.isObjectiveTrackingSuspended());
            Metrics.addCurrentObjectives(partial);
            assertTrue(Metrics.getCurrentThreadMetrics().getMetrics().isEmpty());
            expire();
            return null;
        }).when(partial).notifyUpdate();
        var solution = new BMSSCGRASPConstructor(0.68).construct(partial);
        assertFeasible(solution);
        var values = Metrics.get("Cost").getValues();
        assertEquals(1, values.size());
        assertEquals(solution.getCost(), values.first().value());
    }

    @Test
    void temporaryScopesNestAndRestorePreviouslyDisabledValidationOnExceptions() {
        for (boolean enabled : new boolean[]{true, false}) {
            if (enabled) Context.Configurator.enableValidation();
            else Context.Configurator.disableValidation();
            assertThrows(IllegalStateException.class, () -> BMSSCUtil.withPartialSolution(() -> {
                assertTrue(Context.isObjectiveTrackingSuspended());
                assertFalse(Context.Configurator.isValidationEnabled());
                BMSSCUtil.withPartialSolution(() -> 1);
                assertTrue(Context.isObjectiveTrackingSuspended());
                throw new IllegalStateException("test failure");
            }));
            assertEquals(enabled, Context.Configurator.isValidationEnabled());
            assertFalse(Context.isObjectiveTrackingSuspended());
            var broken = spy(new BMSSCSolution(instance(9, 3)));
            doThrow(new IllegalStateException("construction failure")).when(broken).notifyUpdate();
            assertThrows(IllegalStateException.class,
                    () -> new BMSSCGRASPConstructor(0.68).construct(broken));
            assertEquals(enabled, Context.Configurator.isValidationEnabled());
            assertFalse(Context.isObjectiveTrackingSuspended());
        }
    }

    @Test
    void projectLocalSearchesStopWithinAnInterruptedPairScan() {
        for (boolean best : new boolean[]{false, true}) {
            TimeControl.remove();
            var solution = spy(new RandomConstructor().construct(new BMSSCSolution(instance(9, 1))));
            doAnswer(invocation -> {
                int result = (int) invocation.callRealMethod();
                expire();
                return result;
            }).when(solution).clusterOf(anyInt());
            long version = solution.getVersion();
            assertFalse(best ? new BestImpLS().iteration(solution) : new FirstImpLS().iteration(solution));
            assertEquals(version, solution.getVersion());
            verify(solution, atMost(2)).clusterOf(anyInt());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void shakeImproverDoesNotStartAnotherPhaseAfterTheDeadline() {
        var solution = new RandomConstructor().construct(new BMSSCSolution(instance(9, 3)));
        Improver<BMSSCSolution, BMSSCInstance> improver = mock(Improver.class);
        Shake<BMSSCSolution, BMSSCInstance> shake = mock(Shake.class);
        when(improver.improve(solution)).thenAnswer(invocation -> {
            expire();
            return solution;
        });
        assertSame(solution, new ShakeImprover(improver, shake).improve(solution));
        verifyNoInteractions(shake);
    }

    @Test
    void bestImprovementTerminatesAtALocalOptimumWithoutATimeLimit() {
        var solution = new RandomConstructor().construct(new BMSSCSolution(instance(9, 1)));
        assertFalse(new BestImpLS().iteration(solution));
        assertSame(solution, new BestImpLS().improve(solution));
    }

    @Test
    void oscillationRepairsAfterImprovingThroughAnUnbalancedState() {
        recordEveryObjective();
        var original = oscillationFixture();
        int[] assignment = assignment(original);
        double oldCost = original.getCost();
        var result = new StrategicOscillation(0.75).shake(original, 1);
        assertFeasible(result);
        assertArrayEquals(assignment, assignment(original));
        assertEquals(oldCost, original.getCost());
        assertTrue(result.getVersion() > original.getVersion(), "Expected descent and repair moves");
        var recorded = Metrics.get("Cost").getValues();
        assertEquals(1, recorded.size(), "Only the repaired cost should be published");
        assertEquals(result.getCost(), recorded.first().value());
    }

    @Test
    void interruptedOrFailedOscillationDiscardsTheWorkingCopyAndRestoresContext() {
        for (boolean fail : new boolean[]{true, false}) {
            for (boolean validationEnabled : new boolean[]{true, false}) {
                TimeControl.remove();
                recordEveryObjective();
                if (validationEnabled) Context.Configurator.enableValidation();
                else Context.Configurator.disableValidation();
                var original = spy(oscillationFixture());
                int[] assignment = assignment(original);
                long version = original.getVersion(), timestamp = original.getLastModifiedTime();
                var working = spy(original.cloneSolution());
                doReturn(working).when(original).cloneSolution();
                doAnswer(invocation -> {
                    invocation.callRealMethod();
                    assertFalse(working.feasibleClusterSizes());
                    assertTrue(Context.isObjectiveTrackingSuspended());
                    assertTrue(Metrics.getCurrentThreadMetrics().getMetrics().isEmpty());
                    if (fail) throw new IllegalStateException("shake failure");
                    expire();
                    return null;
                }).when(working).notifyUpdate();
                var shake = new StrategicOscillation(0.75);
                if (fail) assertThrows(IllegalStateException.class, () -> shake.shake(original, 1));
                else assertSame(original, shake.shake(original, 1));
                assertArrayEquals(assignment, assignment(original));
                assertEquals(version, original.getVersion());
                assertEquals(timestamp, original.getLastModifiedTime());
                assertEquals(validationEnabled, Context.Configurator.isValidationEnabled());
                assertFalse(Context.isObjectiveTrackingSuspended());
                assertTrue(Metrics.getCurrentThreadMetrics().getMetrics().isEmpty());
                for (int c = 0; c < original.getInstance().k; c++) {
                    assertEquals(original.getInstance().getClusterSize(c), working.getClusterCapacity(c));
                }
                assertFeasible(original);
            }
        }
    }

    @Test
    void neighborhoodScansRespectAnExpiredDeadline() {
        var solution = oscillationFixture();
        solution.relaxClusterSizeConstraint(1);
        expire();
        assertEquals(0, new SwapNeighborhood().explore(solution).moves().count());
        assertEquals(0, new ReassignNeighborhood().explore(solution).moves().count());
        solution.restoreClusterSizeConstraint();
        assertSame(solution, new StrategicOscillation(0.75).shake(solution, 1));
    }

    private BMSSCSolution oscillationFixture() {
        return assigned(new BMSSCInstance("repair", 6, 1, 2,
                new double[][]{{0}, {0}, {0}, {0}, {0}, {10}}), 0, 0, 1, 1, 1, 0);
    }

    private void recordEveryObjective() {
        Metrics.disableMetrics();
        Metrics.enableMetrics();
        Metrics.register("Cost", reference -> new AbstractMetric(reference) {});
        Metrics.resetMetrics();
    }

    private void expire() {
        TimeControl.setMaxExecutionTime(0, TimeUnit.NANOSECONDS);
        TimeControl.start();
    }
}
