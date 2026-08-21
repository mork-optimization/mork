package es.urjc.etsii.grafo.graphs.mvc;

import es.urjc.etsii.grafo.graphs.model.Edge;
import es.urjc.etsii.grafo.graphs.model.MSTInstance;
import es.urjc.etsii.grafo.graphs.model.MSTSolution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.random.RandomType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class MVCConstructiveTest {

    @BeforeEach
    void setUp() {
        Context.reset();
        Context.Configurator.resetRandom(RandomType.DEFAULT, 1234);
    }

    /**
     * Small graph: a 5-cycle (0-1-2-3-4-0) plus a pendant edge (0-5).
     */
    private MSTInstance smallInstance() {
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
        return new MSTInstance("small", graph, edges, 1);
    }

    private void assertIsValidCover(MSTInstance instance, MSTSolution solution) {
        for (var edge : instance.getEdges()) {
            Assertions.assertTrue(solution.isInCover(edge.from()) || solution.isInCover(edge.to()),
                    "Edge " + edge + " is not covered by the solution");
        }
    }

    @Test
    void constructBuildsAFeasibleCover() {
        var instance = smallInstance();
        var constructive = new MVCConstructive();

        for (int i = 0; i < 50; i++) {
            var solution = constructive.construct(new MSTSolution(instance));
            assertIsValidCover(instance, solution);
            Assertions.assertEquals(solution.getCoverSize(), solution.getScore(), 1e-9);
        }
    }

    @Test
    void usedComponentsMatchesSelectedVertices() {
        var instance = smallInstance();
        var constructive = new MVCConstructive();
        var solution = constructive.construct(new MSTSolution(instance));

        var used = constructive.usedComponents(solution);
        Assertions.assertEquals(solution.getCoverVertices(), used);
    }

    @Test
    void handlesInstanceWithoutEdges() {
        List<Edge>[] graph = new List[3];
        for (int i = 0; i < 3; i++) {
            graph[i] = new ArrayList<>();
        }
        var instance = new MSTInstance("no-edges", graph, new ArrayList<>(), 1);
        var constructive = new MVCConstructive();

        var solution = constructive.construct(new MSTSolution(instance));
        Assertions.assertEquals(0, solution.getCoverSize());
    }
}
