package es.urjc.etsii.grafo.graphs.mvc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static es.urjc.etsii.grafo.graphs.mvc.MVCTestFixtures.allVertices;
import static es.urjc.etsii.grafo.graphs.mvc.MVCTestFixtures.assertIsValidCover;
import static es.urjc.etsii.grafo.graphs.mvc.MVCTestFixtures.cycleWithPendant;

class MVCExactCoverSolverTest {

    @Test
    void solvesToOptimalityWithFullCandidateSet() {
        var instance = cycleWithPendant();
        var solver = new MVCExactCoverSolver();

        var solution = solver.solve(instance, allVertices(instance), 5_000);

        assertIsValidCover(instance, solution);
        Assertions.assertEquals(3, solution.getCoverSize());
    }

    @Test
    void neverReturnsMoreVerticesThanCandidatesGiven() {
        var instance = cycleWithPendant();
        var solver = new MVCExactCoverSolver();

        // The optimal cover {0,1,3} is already among the candidates: an exact solver
        // restricted to this set must not add any unnecessary vertex.
        Set<Integer> candidates = Set.of(0, 1, 3);

        var solution = solver.solve(instance, candidates, 5_000);

        assertIsValidCover(instance, solution);
        Assertions.assertEquals(3, solution.getCoverSize());
        Assertions.assertEquals(Set.of(0, 1, 3), solution.getCoverVertices());
    }

    @Test
    void restrictedCandidatesCanForceASuboptimalButFeasibleCover() {
        var instance = cycleWithPendant();
        var solver = new MVCExactCoverSolver();

        // Excluding vertex 0 forces vertex 5 to be picked to cover edge (0,5),
        // and forces both endpoints of every cycle edge incident to 0 to be picked instead of 0.
        Set<Integer> candidates = Set.of(1, 2, 3, 4, 5);

        var solution = solver.solve(instance, candidates, 5_000);

        assertIsValidCover(instance, solution);
        Assertions.assertFalse(solution.isInCover(0));
        Assertions.assertTrue(solution.isInCover(5));
    }

    @Test
    void alwaysReturnsAFeasibleSolutionEvenWithNoTimeBudget() {
        var instance = cycleWithPendant();
        var solver = new MVCExactCoverSolver();
        var candidates = allVertices(instance);

        // Search should abort almost immediately, falling back to the trivial
        // "select every candidate" solution, which must still be feasible.
        var solution = solver.solve(instance, candidates, 0);

        assertIsValidCover(instance, solution);
        Assertions.assertEquals(instance.v(), solution.getCoverSize());
    }

    @Test
    void rejectsCandidatesThatCannotCoverEveryEdge() {
        var instance = cycleWithPendant();
        var solver = new MVCExactCoverSolver();

        // Neither endpoint of edge (0, 5) is available.
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> solver.solve(instance, Set.of(1, 2, 3, 4), 5_000));
    }
}
