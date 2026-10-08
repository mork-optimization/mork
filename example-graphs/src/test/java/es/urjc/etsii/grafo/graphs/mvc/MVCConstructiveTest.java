package es.urjc.etsii.grafo.graphs.mvc;

import es.urjc.etsii.grafo.graphs.model.MSTSolution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.random.RandomType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static es.urjc.etsii.grafo.graphs.mvc.MVCTestFixtures.assertIsValidCover;
import static es.urjc.etsii.grafo.graphs.mvc.MVCTestFixtures.cycleWithPendant;
import static es.urjc.etsii.grafo.graphs.mvc.MVCTestFixtures.edgeless;

class MVCConstructiveTest {

    @BeforeEach
    void setUp() {
        Context.reset();
        Context.Configurator.resetRandom(RandomType.DEFAULT, 1234);
    }

    @Test
    void constructBuildsAFeasibleCover() {
        var instance = cycleWithPendant();
        var constructive = new MVCConstructive();

        for (int i = 0; i < 50; i++) {
            var solution = constructive.construct(new MSTSolution(instance));
            assertIsValidCover(instance, solution);
            Assertions.assertEquals(solution.getCoverSize(), solution.getScore(), 1e-9);
        }
    }

    @Test
    void usedComponentsMatchesSelectedVertices() {
        var instance = cycleWithPendant();
        var constructive = new MVCConstructive();
        var solution = constructive.construct(new MSTSolution(instance));

        var used = constructive.usedComponents(solution);
        Assertions.assertEquals(solution.getCoverVertices(), used);
    }

    @Test
    void handlesInstanceWithoutEdges() {
        var instance = edgeless(3);
        var constructive = new MVCConstructive();

        var solution = constructive.construct(new MSTSolution(instance));
        Assertions.assertEquals(0, solution.getCoverSize());
    }
}
