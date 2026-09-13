package es.urjc.etsii.grafo.bmssc;

import es.urjc.etsii.grafo.bmssc.create.*;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.bmssc.shake.RandomRemoval;
import es.urjc.etsii.grafo.bmssc.shake.RelatedRemoval;
import es.urjc.etsii.grafo.bmssc.shake.WorstRemoval;
import es.urjc.etsii.grafo.bmssc.util.BMSSCNewUtil.SeedStrategy;
import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.metrics.AbstractMetric;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.shake.Destructive;
import es.urjc.etsii.grafo.shake.DestroyRebuild;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static es.urjc.etsii.grafo.bmssc.BMSSCTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BMSSCNewConstructionTest {
    @BeforeEach
    void setup() { initialize(1234); }

    @AfterEach
    void teardown() { cleanup(); }

    @Test
    void constructorsHandleQuotaEdgesAndIdenticalPointsReproducibly() {
        for (var constructor : constructors()) {
            for (var instance : List.of(instance(1, 1), instance(2, 1), instance(2, 2), instance(7, 3),
                    instance(9, 3), new BMSSCInstance("duplicates", 7, 2, 3, new double[7][2]))) {
                initialize(42);
                var first = constructor.construct(new BMSSCSolution(instance));
                assertFeasible(first);
                initialize(42);
                var second = constructor.construct(new BMSSCSolution(instance));
                assertArrayEquals(assignment(first), assignment(second));
                assertThrows(IllegalArgumentException.class, () -> constructor.construct(first));
                assertTrue(Context.Configurator.isValidationEnabled());
                assertFalse(Context.isObjectiveTrackingSuspended());
            }
        }
    }

    @Test
    void everyDestructorReconstructorCombinationPreservesSurvivorsAndOriginal() {
        for (var constructor : constructors()) {
            for (var destructor : destructors()) {
                for (int strength : new int[]{0, 1, 100}) {
                    var original = new RandomConstructor().construct(new BMSSCSolution(instance(10, 3)));
                    int[] originalAssignments = assignment(original);
                    var partial = destructor.destroy(original, strength);
                    assertEquals(strength == 0 ? 0 : strength == 1 ? 3 : 10, partial.getNotAssignedPoints().size());
                    assertCostAndCaches(partial);
                    int[] retained = assignment(partial);
                    var complete = constructor.reconstruct(partial);
                    assertFeasible(complete);
                    for (int p = 0; p < retained.length; p++) {
                        if (retained[p] >= 0) assertEquals(retained[p], complete.clusterOf(p));
                    }
                    assertArrayEquals(originalAssignments, assignment(original));
                    assertFeasible(original);
                    var shake = new DestroyRebuild<>(constructor, destructor);
                    initialize(789);
                    var first = shake.shake(original.cloneSolution(), strength);
                    initialize(789);
                    var second = shake.shake(original.cloneSolution(), strength);
                    assertFeasible(first);
                    assertArrayEquals(assignment(first), assignment(second));
                }
            }
        }
    }

    @Test
    void worstRemovalMatchesSuccessiveIndependentCostCalculations() {
        var original = new RandomConstructor().construct(new BMSSCSolution(instance(13, 3)));
        int[] expected = assignment(original);
        for (int removal = 0; removal < 4; removal++) {
            int chosen = -1;
            double best = Double.POSITIVE_INFINITY;
            for (int p = 0; p < expected.length; p++) {
                if (expected[p] < 0) continue;
                int cluster = expected[p];
                expected[p] = -1;
                double after = cost(original.getInstance(), expected);
                expected[p] = cluster;
                if (after < best) {
                    best = after;
                    chosen = p;
                }
            }
            expected[chosen] = -1;
        }
        assertArrayEquals(expected, assignment(new WorstRemoval(0.3).destroy(original, 1)));
    }

    @Test
    void relatedRemovalIncludesTheSeedEvenWhenAllDistancesTie() {
        var instance = new BMSSCInstance("ties", 10, 1, 2, new double[10][1]);
        var original = new RandomConstructor().construct(new BMSSCSolution(instance));
        initialize(1234);
        int seed = RandomManager.getRandom().nextInt(instance.n);
        initialize(1234);
        var partial = new RelatedRemoval(0.1).destroy(original, 1);
        assertEquals(1, partial.getNotAssignedPoints().size());
        assertFalse(partial.isAssigned(seed));
    }

    @Test
    void cancellationDuringConstructionCompletesWithoutPublishingPartialStates() {
        for (var constructor : constructors()) {
            TimeControl.remove();
            recordEveryObjective();
            var solution = spy(new BMSSCSolution(instance(7, 3)));
            doAnswer(invocation -> {
                invocation.callRealMethod();
                assertTrue(Context.isObjectiveTrackingSuspended());
                assertTrue(Metrics.getCurrentThreadMetrics().getMetrics().isEmpty());
                expire();
                return null;
            }).when(solution).notifyUpdate();
            constructor.construct(solution);
            assertFeasible(solution);
            assertEquals(1, Metrics.get("Cost").getValues().size());
            assertEquals(solution.getCost(), Metrics.get("Cost").getValues().first().value());
            assertTrue(Context.Configurator.isValidationEnabled());
            assertFalse(Context.isObjectiveTrackingSuspended());
        }
    }

    @Test
    void destructionDoesNotPublishPartialObjectivesAndExpiredReconstructionCompletes() {
        for (var constructor : constructors()) {
            for (var destructor : destructors()) {
                TimeControl.remove();
                var original = new RandomConstructor().construct(new BMSSCSolution(instance(7, 3)));
                recordEveryObjective();
                var partial = destructor.destroy(original, 100);
                assertTrue(Metrics.getCurrentThreadMetrics().getMetrics().isEmpty());
                expire();
                assertSame(original, destructor.destroy(original, 1));
                assertFeasible(constructor.reconstruct(partial));
                assertEquals(1, Metrics.get("Cost").getValues().size());
                assertFalse(Context.isObjectiveTrackingSuspended());
            }
        }
    }

    @Test
    void invalidParametersAndFailedConstructionRestoreContext() {
        for (double invalid : new double[]{-1, 2, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> new RegretConstructor(invalid));
            assertThrows(IllegalArgumentException.class, () -> new BMSSCGRASPConstructorNew(invalid,
                    BMSSCGRASPConstructorNew.Strategy.GREEDY_RANDOM, SeedStrategy.RANDOM));
            assertThrows(IllegalArgumentException.class, () -> new RandomRemoval(invalid));
            assertThrows(IllegalArgumentException.class, () -> new WorstRemoval(invalid));
            assertThrows(IllegalArgumentException.class, () -> new RelatedRemoval(invalid));
        }
        for (var constructor : constructors()) {
            TimeControl.remove();
            var solution = spy(new BMSSCSolution(instance(7, 3)));
            doThrow(new IllegalStateException("injected failure")).when(solution).notifyUpdate();
            for (boolean enabled : new boolean[]{true, false}) {
                if (enabled) Context.Configurator.enableValidation();
                else Context.Configurator.disableValidation();
                assertThrows(IllegalStateException.class, () -> constructor.reconstruct(solution));
                assertEquals(enabled, Context.Configurator.isValidationEnabled());
                assertFalse(Context.isObjectiveTrackingSuspended());
            }
        }
    }

    private List<Reconstructive<BMSSCSolution, BMSSCInstance>> constructors() {
        var constructors = new ArrayList<Reconstructive<BMSSCSolution, BMSSCInstance>>();
        constructors.add(new RandomConstructorNew());
        for (double alpha : new double[]{0, 0.4, 1}) {
            constructors.add(new RegretConstructor(alpha));
            for (var strategy : BMSSCGRASPConstructorNew.Strategy.values()) {
                for (var seed : SeedStrategy.values()) constructors.add(new BMSSCGRASPConstructorNew(alpha, strategy, seed));
            }
        }
        return constructors;
    }

    private List<Destructive<BMSSCSolution, BMSSCInstance>> destructors() {
        return List.of(new RandomRemoval(0.3), new WorstRemoval(0.3), new RelatedRemoval(0.3));
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
