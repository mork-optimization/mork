package es.urjc.etsii.grafo.graphs.mvc;

import es.urjc.etsii.grafo.algorithms.cmsa.CMSABuilder;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.graphs.model.MSTInstance;
import es.urjc.etsii.grafo.graphs.model.MSTInstanceImporter;
import es.urjc.etsii.grafo.graphs.model.MSTSolution;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.random.RandomType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * End-to-end test: builds a {@code CMSA} algorithm using {@link MVCConstructive} and
 * {@link MVCExactCoverSolver}, and checks it finds a valid, small vertex cover, without
 * requiring a full Spring Boot / Mork application context.
 */
class CMSAMinVertexCoverIntegrationTest {

    @BeforeAll
    static void initMetrics() {
        Metrics.disableMetrics();
    }

    @BeforeEach
    void setUp() {
        Context.reset();
        Context.Configurator.setObjectives(Objective.ofMinimizing("Score", MSTSolution::getScore, null));
        Context.Configurator.resetRandom(RandomType.DEFAULT, 42);
    }

    private void assertIsValidCover(MSTInstance instance, MSTSolution solution) {
        for (var edge : instance.getEdges()) {
            Assertions.assertTrue(solution.isInCover(edge.from()) || solution.isInCover(edge.to()),
                    "Edge " + edge + " is not covered by the solution");
        }
    }

    @Test
    void findsAValidAndReasonablySmallCoverOnARandomGraph() {
        var instance = MSTInstanceImporter.generateErdosRenyi(30, 0.15, 7);

        var cmsa = new CMSABuilder<MSTSolution, MSTInstance>()
                .withDefaultObjective()
                .withConstructive(new MVCConstructive())
                .withSolver(new MVCExactCoverSolver())
                .withSolutionsPerIteration(10)
                .withAgeMax(5)
                .withSolverTimeLimitInMillis(200)
                .withMaxIterations(30)
                .build("CMSA-MVC-Test");
        cmsa.setBuilder(new SolutionBuilder<>() {
            @Override
            public MSTSolution initializeSolution(MSTInstance instance) {
                return new MSTSolution(instance);
            }
        });

        var solution = cmsa.algorithm(instance);

        assertIsValidCover(instance, solution);
        // A single unbiased random construction is a weak baseline: CMSA must do at least as well.
        var baseline = new MVCConstructive(0.0).construct(new MSTSolution(instance));
        Assertions.assertTrue(solution.getCoverSize() <= baseline.getCoverSize(),
                "CMSA cover (%d) should not be worse than a single unbiased random construction (%d)"
                        .formatted(solution.getCoverSize(), baseline.getCoverSize()));
    }

    @Test
    void adaptsSubInstanceOverIterationsAndKeepsImprovingOrStaying() {
        var instance = MSTInstanceImporter.generateErdosRenyi(20, 0.2, 11);

        // Run with a single iteration first
        var single = new CMSABuilder<MSTSolution, MSTInstance>()
                .withDefaultObjective()
                .withConstructive(new MVCConstructive())
                .withSolver(new MVCExactCoverSolver())
                .withSolutionsPerIteration(5)
                .withAgeMax(5)
                .withSolverTimeLimitInMillis(100)
                .withMaxIterations(1)
                .build("CMSA-MVC-1iter");
        single.setBuilder(new SolutionBuilder<>() {
            @Override
            public MSTSolution initializeSolution(MSTInstance instance) {
                return new MSTSolution(instance);
            }
        });

        Context.Configurator.resetRandom(RandomType.DEFAULT, 42);
        var solutionAfter1Iter = single.algorithm(instance);

        // Run again with the same seed, but allow many more iterations
        Context.Configurator.resetRandom(RandomType.DEFAULT, 42);
        var many = new CMSABuilder<MSTSolution, MSTInstance>()
                .withDefaultObjective()
                .withConstructive(new MVCConstructive())
                .withSolver(new MVCExactCoverSolver())
                .withSolutionsPerIteration(5)
                .withAgeMax(5)
                .withSolverTimeLimitInMillis(100)
                .withMaxIterations(30)
                .build("CMSA-MVC-30iter");
        many.setBuilder(new SolutionBuilder<>() {
            @Override
            public MSTSolution initializeSolution(MSTInstance instance) {
                return new MSTSolution(instance);
            }
        });
        var solutionAfterManyIters = many.algorithm(instance);

        assertIsValidCover(instance, solutionAfter1Iter);
        assertIsValidCover(instance, solutionAfterManyIters);
        Assertions.assertTrue(solutionAfterManyIters.getCoverSize() <= solutionAfter1Iter.getCoverSize(),
                "Running more CMSA iterations should never yield a worse best solution");
    }
}
