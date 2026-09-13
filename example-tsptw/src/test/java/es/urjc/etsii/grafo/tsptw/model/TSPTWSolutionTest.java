package es.urjc.etsii.grafo.tsptw.model;

import es.urjc.etsii.grafo.tsptw.alg.GVNS;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static es.urjc.etsii.grafo.tsptw.model.TSPTWTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class TSPTWSolutionTest {
    private final TSPTWSolutionValidator validator = new TSPTWSolutionValidator();

    @BeforeEach void setUp() { initialize(); }
    @AfterEach void tearDown() { cleanup(); }

    @Test
    void prunesStronglyInfeasibleArcsInOriginalAndCopiedSolutions() throws Exception {
        var instance = instance("prune");
        assertTrue(instance.isTimeWindowInfeasible(2, 1));
        assertFalse(instance.isTimeWindowInfeasible(1, 2));
        var original = tour(instance, 1, 2, 3);
        var copy = original.clone_solution();
        var assigned = new TSPTWSolution(instance);
        assigned.copy_from(original);
        for (var solution : List.of(original, copy, assigned)) {
            var candidates = new ArrayList<Integer>();
            solution.shuffle_1shift_feasible_nodes(candidates);
            assertEquals(List.of(2), candidates);
            assertSame(instance, solution.getInstance());
        }
        copy.swap(1);
        assertEquals(List.of(0, 1, 2, 3, 0), original.permutation);
        assertEquals(0, original.constraint_violations());
        assertTrue(copy.constraint_violations() > 0);
    }

    @Test
    void bestInsertionPreservesBestCandidateWhileExploringLaterMoves() throws Exception {
        var original = tour(instance("best"), 1, 2, 3, 4, 5);
        var best = original.localsearch_insertion(false);
        assertEquals(136, original.cost());
        assertEquals(113, best.cost());
        assertEquals(List.of(0, 1, 2, 5, 3, 4, 0), best.permutation);
        assertTrue(validator.validate(best).isValid());
    }

    @Test
    void fractionalTwoOptRejectsLateArrivalWithoutChangingCaches() throws Exception {
        var solution = tour(instance("fraction"), 1, 2, 3);
        double[] arrivalTimes = solution._makespan.clone();
        assertEquals(0, solution.constraint_violations());
        assertEquals(2, solution.two_opt_is_infeasible(0, 2));
        assertArrayEquals(arrivalTimes, solution._makespan);
        assertTrue(validator.validate(solution).isValid());
    }

    @Test
    void approximatelySymmetricMatrixCannotUseSymmetricTwoOpt() throws Exception {
        var instance = instance("symmetry");
        assertFalse(instance.isSymmetric());
        var solution = tour(instance, 1, 2, 3);
        assertThrows(IllegalArgumentException.class, solution::two_opt_first);
        assertThrows(IllegalArgumentException.class, () -> new GVNS().algorithm(instance));
    }

    @Test
    void validatorChecksTimeWindowsIndependentlyOfViolationCaches() throws Exception {
        var solution = tour(instance("prune"), 2, 1, 3);
        assertTrue(solution.constraint_violations() > 0);
        solution._constraint_violations = 0;
        solution._infeasibility = 0;
        var result = validator.validate(solution);
        assertFalse(result.isValid());
        assertTrue(result.getReasonFailed().contains("Tour violates"));
    }

    @Test
    void validatorChecksDepotDeadline() {
        var instance = new TSPTWInstance("depot", 4,
                new double[][]{{0, 1, 1, 1}, {1, 0, 1, 1}, {1, 1, 0, 1}, {1, 1, 1, 0}},
                new int[4], new int[]{3, 100, 100, 100});
        var solution = tour(instance, 1, 2, 3);
        assertEquals(1, solution.constraint_violations());
        assertFalse(validator.validate(solution).isValid());
    }

    @Test
    void validatorRejectsCorruptToursAndCaches() throws Exception {
        var original = tour(instance("best"), 1, 2, 3, 4, 5);
        assertTrue(validator.validate(original).isValid());
        for (Consumer<TSPTWSolution> corrupt : List.<Consumer<TSPTWSolution>>of(
                s -> s._tourcost++, s -> s._tourcost = Double.NaN,
                s -> s._makespan[2]++, s -> s._constraint_violations++,
                s -> s._infeasibility++, s -> s.nodes_available++,
                s -> s.node_assigned[2] = false,
                s -> s.permutation.set(2, 1), s -> s.permutation.set(2, -1),
                s -> s.permutation.set(2, s.n), s -> s.permutation.removeLast(),
                s -> s.permutation.set(0, 1), s -> s.permutation.set(s.n, 1))) {
            var copy = original.clone_solution();
            corrupt.accept(copy);
            assertFalse(validator.validate(copy).isValid());
        }
    }

    @Test
    void expiredDeadlineStopsLocalSearchWithoutModifyingTour() throws Exception {
        var solution = tour(instance("best"), 1, 2, 3, 4, 5);
        expireTimeLimit();
        assertFalse(solution.feasible_1shift_first());
        assertFalse(solution.two_opt_first());
        assertEquals(136, solution.cost());
        assertTrue(validator.validate(solution).isValid());
    }
}
