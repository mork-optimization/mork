package es.urjc.etsii.grafo.bmssc;

import es.urjc.etsii.grafo.bmssc.create.BMSSCGRASPConstructor;
import es.urjc.etsii.grafo.bmssc.create.BMSSCListManager;
import es.urjc.etsii.grafo.bmssc.create.RandomConstructor;
import es.urjc.etsii.grafo.bmssc.improve.StrategicOscillation;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.*;
import es.urjc.etsii.grafo.bmssc.util.BMSSCUtil;
import es.urjc.etsii.grafo.improve.ls.LocalSearch;
import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchCachedBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchFirstImprovement;
import es.urjc.etsii.grafo.metrics.AbstractMetric;
import es.urjc.etsii.grafo.metrics.Metrics;
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
        expire();
        var solution = new BMSSCGRASPConstructor(0.68).construct(new BMSSCSolution(instance(9, 4)));
        assertFeasible(solution);
        assertTrue(Context.Configurator.isValidationEnabled());
        assertFalse(Context.isObjectiveTrackingSuspended());
    }

    @Test
    void cancellationDuringConstructionCompletesWithoutPublishingPartialCosts() {
        recordEveryObjective();
        var manager = new BMSSCListManager() {
            @Override
            public void beforeGRASP(BMSSCSolution solution) {
                super.beforeGRASP(solution);
                assertFalse(solution.feasibleClusterSizes());
                assertFalse(Context.Configurator.isValidationEnabled());
                assertTrue(Context.isObjectiveTrackingSuspended());
                Metrics.addCurrentObjectives(solution);
                assertTrue(Metrics.getCurrentThreadMetrics().getMetrics().isEmpty());
                expire();
            }
        };
        var solution = new BMSSCGRASPConstructor(0.68, manager).construct(new BMSSCSolution(instance(9, 3)));
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
            var badManager = new BMSSCListManager() {
                @Override
                public void beforeGRASP(BMSSCSolution solution) {
                    super.beforeGRASP(solution);
                    throw new IllegalStateException("construction failure");
                }
            };
            assertThrows(IllegalStateException.class,
                    () -> new BMSSCGRASPConstructor(0.68, badManager).construct(new BMSSCSolution(instance(9, 3))));
            assertEquals(enabled, Context.Configurator.isValidationEnabled());
            assertFalse(Context.isObjectiveTrackingSuspended());
        }
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
