package es.urjc.etsii.grafo.graphs.mvc;

import es.urjc.etsii.grafo.graphs.model.Edge;
import es.urjc.etsii.grafo.graphs.model.MSTInstance;
import es.urjc.etsii.grafo.graphs.model.MSTSolution;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class MVCTestFixtures {

    private MVCTestFixtures() {
    }

    /**
     * A 5-cycle (0-1-2-3-4-0) plus a pendant edge (0-5).
     */
    static MSTInstance cycleWithPendant() {
        int[][] edges = {{0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 0}, {0, 5}};
        return graph("cycle+pendant", 6, edges);
    }

    static MSTInstance edgeless(int vertices) {
        return graph("no-edges", vertices, new int[][]{});
    }

    @SuppressWarnings("unchecked")
    private static MSTInstance graph(String name, int vertices, int[][] endpoints) {
        List<Edge>[] graph = new List[vertices];
        for (int i = 0; i < vertices; i++) {
            graph[i] = new ArrayList<>();
        }

        List<Edge> edges = new ArrayList<>();
        for (int[] endpoint : endpoints) {
            var edge = new Edge(endpoint[0], endpoint[1], 1);
            graph[endpoint[0]].add(edge);
            graph[endpoint[1]].add(edge);
            edges.add(edge);
        }
        return new MSTInstance(name, graph, edges, 1);
    }

    static void assertIsValidCover(MSTInstance instance, MSTSolution solution) {
        for (Edge edge : instance.getEdges()) {
            Assertions.assertTrue(solution.isInCover(edge.from()) || solution.isInCover(edge.to()),
                    "Edge " + edge + " is not covered by the solution");
        }
    }

    static Set<Integer> allVertices(MSTInstance instance) {
        Set<Integer> candidates = new HashSet<>();
        for (int vertex = 0; vertex < instance.v(); vertex++) {
            candidates.add(vertex);
        }
        return candidates;
    }
}
