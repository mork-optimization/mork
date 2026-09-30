package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.vns.VNS;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.flayouts.Main;
import es.urjc.etsii.grafo.flayouts.constructives.FLPRandomConstructiveNew;
import es.urjc.etsii.grafo.flayouts.improve.*;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.improve.ls.*;
import es.urjc.etsii.grafo.metrics.AbstractMetric;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.shake.Shake;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.*;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static es.urjc.etsii.grafo.flayouts.model.FLPNewTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class FLPNewAlgorithmTest {
    @BeforeEach void setup() { initialize(1234); }
    @AfterEach void teardown() { cleanup(); }

    @Test
    void everyNewPreservingNeighborhoodWorksWithFirstBestAndCachedSearch() {
        for (var neighborhood : List.of(new FLPSwapNeighNew(false), new FLPSwapNeighNew(), new FLPRelocateNeighNew(false),
                new FLPRelocateNeighNew(), new FLPOptNeighNew(false), new FLPOptNeighNew(),
                new FLPCandidateRelocateNeighNew(4, 1, FLPCandidateRelocateNeighNew.Relation.FLOW),
                new FLPBlockRelocateNeighNew(2, false, FLPBlockRelocateNeighNew.Scope.ALL))) {
            List<LocalSearch<FLPNewMove, FLPSolution, FLPInstance>> searches = List.of(
                    new LocalSearchFirstImprovement<>(neighborhood), new LocalSearchBestImprovement<>(neighborhood),
                    new LocalSearchCachedBestImprovement<>(neighborhood, 4));
            for (var search : searches) {
                var initial = new FLPRandomConstructiveNew().construct(new FLPSolution(instance(10, 3)));
                double before = initial.getScore();
                var result = assertTimeout(Duration.ofSeconds(5), () -> search.improve(initial));
                assertState(result, true);
                assertTrue(result.getScore() <= before);
                for (var move : neighborhood.explore(result).moves().toList()) assertTrue(move.delta() >= 0);
            }
        }
    }

    @Test
    void vndAndAnnealingKeepBestSolutionsAndTerminateOnDegenerateInputs() {
        var neighborhoods = List.of(new FLPRelocateNeighNew(), new FLPSwapNeighNew(), new FLPOptNeighNew());
        for (int n : new int[]{0, 1, 2, 10}) {
            var s = new FLPRandomConstructiveNew().construct(new FLPSolution(instance(n, 2)));
            for (var neighborhood : neighborhoods) {
                var before = s.cloneSolution();
                var sa = new FLPSimulatedAnnealingNew(neighborhood, 0.8, 0.99, 5, 20);
                var result = assertTimeout(Duration.ofSeconds(5), () -> sa.improve(s));
                assertState(result, true);
                assertTrue(result.getScore() <= s.getScore());
                assertUnchanged(before, s);
            }
        }
        var sa = new FLPSimulatedAnnealingNew(new FLPSwapNeighNew(false), 0.5, 0.99, 1, 20);
        var s = new FLPRandomConstructiveNew().construct(new FLPSolution(instance(8, 2)));
        TimeControl.setMaxExecutionTime(0, TimeUnit.NANOSECONDS);
        TimeControl.start();
        assertEquals(s.getScore(), sa.improve(s).getScore());
    }

    @Test
    void vnsUsesReturnedImproverSolutionAndReportsAnInitialFeasibleObjective() {
        var instance = instance(3, 2);
        var bad = fixture(instance, new int[]{0,1,2}, new int[]{});
        var good = fixture(instance, new int[]{0,1}, new int[]{2});
        assertTrue(good.getScore() < bad.getScore());
        var constructor = new Constructive<FLPSolution, FLPInstance>() {
            @Override public FLPSolution construct(FLPSolution ignored) { return bad.cloneSolution(); }
        };
        var improver = new Improver<>(Main.FLOW) {
            int calls;

            @Override
            public FLPSolution improve(FLPSolution solution) {
                return ++calls == 1 ? solution : good.cloneSolution();
            }
        };
        var algorithm = new VNS<>("replacement", 1, constructor, Shake.nul(), improver);
        setBuilder(algorithm);
        assertEquals(good.getScore(), algorithm.algorithm(instance).getScore());

        Metrics.enableMetrics();
        Metrics.register("Flow", reference -> new AbstractMetric(reference) {});
        Metrics.resetMetrics();
        var noImprovement = new VNS<>("baseline", 1, constructor, Shake.nul(), Improver.nul());
        setBuilder(noImprovement);
        var result = noImprovement.algorithm(instance);
        assertState(result, true);
        assertFalse(Metrics.get("Flow").getValues().isEmpty());
        assertEquals(result.getScore(), Metrics.get("Flow").getValues().first().value());
    }

    static void setBuilder(Algorithm<FLPSolution, FLPInstance> algorithm) {
        algorithm.setBuilder(new SolutionBuilder<>() {
            @Override public FLPSolution initializeSolution(FLPInstance instance) { return new FLPSolution(instance); }
        });
    }
}
