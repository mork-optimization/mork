package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.create.grasp.GraspBuilder;
import es.urjc.etsii.grafo.flayouts.Main;
import es.urjc.etsii.grafo.flayouts.constructives.*;
import es.urjc.etsii.grafo.flayouts.constructives.grasp.FLPAddListManager;
import es.urjc.etsii.grafo.flayouts.constructives.grasp.FLPAddListManagerNew;
import es.urjc.etsii.grafo.flayouts.shake.*;
import es.urjc.etsii.grafo.metrics.AbstractMetric;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.shake.Destructive;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static es.urjc.etsii.grafo.flayouts.model.FLPNewTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class FLPNewConstructionTest {
    @BeforeEach void setup() { initialize(1234); }
    @AfterEach void teardown() { cleanup(); }

    @Test
    void constructorsAndRepairsPreserveAssignmentsAndMatchIndependentCost() {
        for (int n : new int[]{1, 2, 9}) for (int rows : new int[]{1, 2, 3}) {
            var instance = instance(n, rows);
            for (var constructor : constructors()) {
                var solution = constructor.construct(new FLPSolution(instance));
                assertState(solution, true);
                var before = solution.cloneSolution();
                constructor.reconstruct(solution);
                assertUnchanged(before, solution);
                var partial = new FLPSolution(instance);
                if (n > 2) FLPNewUtil.add(partial, n - 1, rows - 1, 0, true).execute(partial);
                var repaired = constructor.reconstruct(partial);
                assertSame(partial, repaired);
                assertState(repaired, true);
                if (n > 2) assertEquals(rows - 1, FLPNewUtil.locate(repaired, n - 1).row());
            }
        }
    }

    @Test
    void destroyRepairCombinationsHandleGapsFullDestructionAndPartialInputs() {
        for (var destructor : destructors()) for (var constructor : constructors()) {
            var original = fixture(instance(9, 3), new int[]{0,1,2}, new int[]{3,4,5}, new int[]{6,7,8});
            var before = original.cloneSolution();
            for (int k : new int[]{0, 1, 100}) {
                var partial = destructor.destroy(original, k);
                assertState(partial, false);
                assertTrue(partial.nAssigned() < original.nAssigned());
                assertUnchanged(before, original);
                var survivors = layout(partial);
                var result = constructor.reconstruct(partial);
                assertState(result, true);
                // Every surviving subsequence stays in its row and original relative order.
                for (int row = 0; row < survivors.length; row++) {
                    int index = 0;
                    for (int f : layout(result)[row]) if (index < survivors[row].length && f == survivors[row][index]) index++;
                    assertEquals(survivors[row].length, index);
                }
            }
            var partial = fixture(instance(9, 3), new int[]{8}, new int[]{}, new int[]{});
            assertState(destructor.destroy(partial, Integer.MAX_VALUE), false);
            assertState(destructor.destroy(new FLPSolution(instance(3, 2)), 1), false);
        }
    }

    @Test
    void constructionRepairAndDestructionAreReproducible() {
        var instance = instance(13, 3);
        for (var constructor : constructors()) {
            initialize(45);
            int[][] first = layout(constructor.construct(new FLPSolution(instance)));
            initialize(45);
            assertTrue(Arrays.deepEquals(first, layout(constructor.construct(new FLPSolution(instance)))));
        }
        var original = new FLPRandomConstructiveNew().construct(new FLPSolution(instance));
        for (var destructor : destructors()) {
            initialize(78);
            int[][] first = layout(destructor.destroy(original, 3));
            initialize(78);
            assertTrue(Arrays.deepEquals(first, layout(destructor.destroy(original, 3))));
        }
    }

    @Test
    void expiredDeadlineStillCompletesRepairAndDoesNotPublishPartialObjectives() {
        for (var constructor : constructors()) {
            initialize(987);
            Metrics.enableMetrics();
            Metrics.register("Flow", reference -> new AbstractMetric(reference) {});
            Metrics.resetMetrics();
            TimeControl.setMaxExecutionTime(0, TimeUnit.NANOSECONDS);
            TimeControl.start();
            var solution = constructor.construct(new FLPSolution(instance(12, 3)));
            assertState(solution, true);
            assertTrue(Metrics.getCurrentThreadMetrics().getMetrics().isEmpty());
            Metrics.addCurrentObjectives(solution);
            assertEquals(1, Metrics.get("Flow").getValues().size());
        }
    }

    @Test
    void invalidParametersFailBeforeExecutingAnAlgorithm() {
        assertThrows(IllegalArgumentException.class, () -> new RandomRemoveDestructiveNew(new FLPRemoveNeighNew(), Double.NaN, false));
        assertThrows(IllegalArgumentException.class, () -> new FLPWorstRemoveDestructiveNew(-0.1, 0));
        assertThrows(IllegalArgumentException.class, () -> new FLPRelatedRemoveDestructiveNew(0.2, 2, FLPRelatedRemoveDestructiveNew.Relation.FLOW));
        assertThrows(IllegalArgumentException.class, () -> new FLPRegretConstructiveNew(1, 0));
        assertThrows(IllegalArgumentException.class, () -> new FLPFlowConstructiveNew(FLPFlowConstructiveNew.Order.RANDOM, FLPFlowConstructiveNew.Placement.APPEND, Double.POSITIVE_INFINITY));
    }

    private List<Reconstructive<FLPSolution, FLPInstance>> constructors() {
        var constructors = new ArrayList<Reconstructive<FLPSolution, FLPInstance>>();
        constructors.add(new FLPRandomConstructive());
        constructors.add(new FLPRandomConstructiveNew());
        for (var order : FLPFlowConstructiveNew.Order.values()) for (var placement : FLPFlowConstructiveNew.Placement.values()) {
            constructors.add(new FLPFlowConstructiveNew(order, placement, 0.2));
        }
        constructors.add(new FLPRegretConstructiveNew(2, 0));
        constructors.add(new FLPRegretConstructiveNew(3, 0.3));
        for (boolean originalManager : new boolean[]{false, true})
        for (boolean randomGreedy : new boolean[]{false, true}) for (double alpha : new double[]{0, 1}) {
            var builder = new GraspBuilder<FLPMove, FLPSolution, FLPInstance>().withObjective(Main.FLOW)
                    .withAlphaValue(alpha);
            if (originalManager) builder.withListManager(new FLPAddListManager());
            else builder.withListManager(new FLPAddListManagerNew(new FLPAddNeigh()));
            if (randomGreedy) builder.withStrategyRandomGreedy(); else builder.withStrategyGreedyRandom();
            constructors.add(builder.build());
        }
        return constructors;
    }

    private List<Destructive<FLPSolution, FLPInstance>> destructors() {
        var result = new ArrayList<Destructive<FLPSolution, FLPInstance>>();
        result.add(new RandomRemoveDestructiveNew(new FLPRemoveNeighNew(), 0.25, false));
        result.add(new RandomRemoveDestructiveNew(new FLPRemoveNeighNew(), 0.25, true));
        for (double noise : new double[]{0, 1}) {
            result.add(new FLPWorstRemoveDestructiveNew(0.25, noise));
            for (var relation : FLPRelatedRemoveDestructiveNew.Relation.values()) result.add(new FLPRelatedRemoveDestructiveNew(0.25, noise, relation));
        }
        return result;
    }
}
