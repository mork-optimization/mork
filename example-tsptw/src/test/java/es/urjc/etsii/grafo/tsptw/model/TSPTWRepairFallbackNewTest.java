package es.urjc.etsii.grafo.tsptw.model;

import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.tsptw.repair.TSPTWFeasibilityRepairFallbackNew;
import es.urjc.etsii.grafo.tsptw.repair.TSPTWFeasibilityRepairNew;
import es.urjc.etsii.grafo.tsptw.repair.TSPTWForwardViolated;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static es.urjc.etsii.grafo.tsptw.model.TSPTWTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class TSPTWRepairFallbackNewTest {
    @BeforeEach void setUp() { initialize(); }
    @AfterEach void tearDown() { cleanup(); }

    @Test
    void fallbackRepairsTourThatForwardViolatedAloneCannotRepair() {
        // Customer 1 must be first, but is currently last: forward moves cannot repair it.
        var original = tour(urgentFirstInstance(), 2, 3, 4, 1);
        var repaired = original.cloneSolution();
        enableMetrics();

        new TSPTWFeasibilityRepairNew(List.of(new TSPTWForwardViolated()), 2).repair(original);
        assertEquals(3, original.infeasibility());
        assertFalse(new TSPTWSolutionValidator().validate(original).isValid());

        new TSPTWFeasibilityRepairFallbackNew(List.of(new TSPTWForwardViolated()), 2).repair(repaired);
        assertEquals(1, repaired.permutation.get(1));
        assertTrue(new TSPTWSolutionValidator().validate(repaired).isValid());
        assertConsistent(original);
        assertConsistent(repaired);
        assertTrue(Metrics.get("Cost").getValues().isEmpty());
    }

    @Test
    void deadlineDuringPrimaryRepairPreventsFallbackAndPreservesConsistentTour() {
        var solution = tour(urgentFirstInstance(), 2, 3, 4, 1);
        var before = solution.cloneSolution();
        var interrupted = new TSPTWForwardViolated() {
            @Override public void repair(TSPTWSolution s) { expireTimeLimit(); }
        };
        new TSPTWFeasibilityRepairFallbackNew(List.of(interrupted), 2).repair(solution);
        assertEquals(before.permutation, solution.permutation);
        assertEquals(before.infeasibility(), solution.infeasibility());
        assertConsistent(solution);
    }

    @Test
    void feasibleInputDoesNotInvokePrimaryOrFallback() {
        var solution = tour(urgentFirstInstance(), 1, 2, 3, 4);
        var before = solution.cloneSolution();
        var unexpected = new TSPTWForwardViolated() {
            @Override public void repair(TSPTWSolution s) { fail("Feasible input needs no repair"); }
        };
        new TSPTWFeasibilityRepairFallbackNew(List.of(unexpected), 2).repair(solution);
        assertEquals(before.permutation, solution.permutation);
        assertConsistent(solution);
    }

    private static TSPTWInstance urgentFirstInstance() {
        return new TSPTWInstance("urgent-first", 5,
                new double[][]{{0, 1, 1, 1, 1}, {1, 0, 1, 1, 1}, {1, 1, 0, 1, 1},
                        {1, 1, 1, 0, 1}, {1, 1, 1, 1, 0}},
                new int[5], new int[]{100, 1, 100, 100, 100});
    }
}
