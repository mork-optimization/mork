package es.urjc.etsii.grafo.graphs.mvc;

import es.urjc.etsii.grafo.algorithms.cmsa.CMSA;
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

import static es.urjc.etsii.grafo.graphs.mvc.MVCTestFixtures.assertIsValidCover;

/**
 * End-to-end test: builds a {@code CMSA} algorithm using {@link MVCConstructive} and
 * {@link MVCExactCoverSolver}, and checks it finds a valid, small vertex cover, without
 * requiring a full Spring Boot / Mork application context.
 */
class CMSAMinVertexCoverIntegrationTest {

    private static final SolutionBuilder<MSTSolution, MSTInstance> SOLUTION_BUILDER = new SolutionBuilder<>() {
        @Override
        public MSTSolution initializeSolution(MSTInstance instance) {
            return new MSTSolution(instance);
        }
    };

    @BeforeAll
    static void initMetrics() {
        Metrics.disableMetrics();
    }

    @BeforeEach
    void setUp() {
        Context.reset();
        Context.Configurator.setObjectives(Objective.ofMinimizing("Score", MSTSolution::getScore));
        Context.Configurator.resetRandom(RandomType.DEFAULT, 42);
    }

    @Test
    void findsAValidAndReasonablySmallCoverOnARandomGraph() {
        var instance = MSTInstanceImporter.generateErdosRenyi(30, 0.15, 7);
        var cmsa = buildCmsa(10, 200, 30);

        var solution = cmsa.algorithm(instance);

        assertIsValidCover(instance, solution);
        // A single randomized construction with greedyBias=0.0 (always prefers the lower-degree endpoint)
        // is a weak baseline: CMSA must do at least as well.
        var baseline = new MVCConstructive(0.0).construct(new MSTSolution(instance));
        Assertions.assertTrue(solution.getCoverSize() <= baseline.getCoverSize(),
                "CMSA cover (%d) should not be worse than a single randomized construction (%d)"
                        .formatted(solution.getCoverSize(), baseline.getCoverSize()));
    }

    @Test
    void adaptsSubInstanceOverIterationsAndKeepsImprovingOrStaying() {
        var instance = MSTInstanceImporter.generateErdosRenyi(20, 0.2, 11);

        var single = buildCmsa(5, 100, 1);
        var solutionAfter1Iter = single.algorithm(instance);

        // Run again with the same seed, but allow many more iterations
        Context.Configurator.resetRandom(RandomType.DEFAULT, 42);
        var many = buildCmsa(5, 100, 30);
        var solutionAfterManyIters = many.algorithm(instance);

        assertIsValidCover(instance, solutionAfter1Iter);
        assertIsValidCover(instance, solutionAfterManyIters);
        Assertions.assertTrue(solutionAfterManyIters.getCoverSize() <= solutionAfter1Iter.getCoverSize(),
                "Running more CMSA iterations should never yield a worse best solution");
    }

    private CMSA<MSTSolution, MSTInstance, Integer> buildCmsa(
            int solutionsPerIteration,
            long solverTimeLimitInMillis,
            int maxIterations
    ) {
        var cmsa = new CMSABuilder<MSTSolution, MSTInstance, Integer>()
                .withDefaultObjective()
                .withConstructive(new MVCConstructive())
                .withSolver(new MVCExactCoverSolver())
                .withSolutionsPerIteration(solutionsPerIteration)
                .withAgeMax(5)
                .withSolverTimeLimitInMillis(solverTimeLimitInMillis)
                .withMaxIterations(maxIterations)
                .build("CMSA-MVC-%diter".formatted(maxIterations));
        cmsa.setBuilder(SOLUTION_BUILDER);
        return cmsa;
    }
}
