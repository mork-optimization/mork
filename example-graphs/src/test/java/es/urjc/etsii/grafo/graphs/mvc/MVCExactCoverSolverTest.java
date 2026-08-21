package es.urjc.etsii.grafo.graphs.mvc;

import es.urjc.etsii.grafo.graphs.model.Edge;
import es.urjc.etsii.grafo.graphs.model.MSTInstance;
import es.urjc.etsii.grafo.graphs.model.MSTSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class MVCExactCoverSolverTest {

    /**
     * A 5-cycle (0-1-2-3-4-0) plus a pendant edge (0-5).
     * Minimum vertex cover size is known to be 3, e.g. {0,1,3}: an odd cycle of length n
     * needs ceil(n/2) vertices, and vertex 0 also covers the pendant edge for free.
     */
    private MSTInstance cycleWithPendant() {
        List<Edge>[] graph = new List[6];
        for (int i = 0; i < 6; i++) {
            graph[i] = new ArrayList<>();
        }
        List<Edge> edges = new ArrayList<>();
        int[][] pairs = {{0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 0}, {0, 5}};
        for (var pair : pairs) {
            var edge = new Edge(pair[0], pair[1], 1);
            graph[pair[0]].add(edge);
            graph[pair[1]].add(edge);
            edges.add(edge);
        }
        return new MSTInstance("cycle+pendant", graph, edges, 1);
    }

    private void assertIsValidCover(MSTInstance instance, MSTSolution solution) {
        for (var edge : instance.getEdges()) {
            Assertions.assertTrue(solution.isInCover(edge.from()) || solution.isInCover(edge.to()),
                    "Edge " + edge + " is not covered by the solution");
        }
    }

    private Set<Object> allVertices(MSTInstance instance) {
        Set<Object> candidates = new java.util.HashSet<>();
        for (int v = 0; v < instance.v(); v++) {
            candidates.add(v);
        }
        return candidates;
    }

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
        Set<Object> candidates = Set.of(0, 1, 3);

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
        Set<Object> candidates = Set.of(1, 2, 3, 4, 5);

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
}
