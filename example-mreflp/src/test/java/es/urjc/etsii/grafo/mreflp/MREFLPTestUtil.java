package es.urjc.etsii.grafo.mreflp;

import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.mreflp.alg.*;
import es.urjc.etsii.grafo.mreflp.experiments.PaperExperiment;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomType;
import java.util.Random;

final class MREFLPTestUtil {
    private MREFLPTestUtil() {}
    static void context(long seed) {
        Context.reset();
        Context.Configurator.setObjectives(Main.COST);
        Context.Configurator.setValidator(new MREFLPSolutionValidator());
        Context.Configurator.resetRandom(RandomType.DEFAULT, seed);
        Metrics.disableMetrics();
        TimeControl.remove();
    }
    static MREFLPInstance instance(int n, int r, int k, long seed) {
        var random = new Random(seed);
        long[][] flows = new long[n][n];
        for (int u = 0; u < n; u++) for (int v = u + 1; v < n; v++) flows[u][v] = flows[v][u] = random.nextInt(100);
        return new MREFLPInstance("fixture", "test", r, k, flows, 0, "fixture-hash");
    }
    static MREFLPSolution solution(MREFLPInstance i, int... groups) {
        var solution = new MREFLPSolution(i);
        for (int v = 0; v < groups.length; v++) solution.assign(v, groups[v]);
        solution.notifyUpdate();
        return solution;
    }
    static LMLS algorithm(LMLSVariant variant, int restarts) {
        return withBuilder(PaperExperiment.paperAlgorithm(variant, restarts));
    }
    static <A extends Algorithm<MREFLPSolution, MREFLPInstance>> A withBuilder(A algorithm) {
        algorithm.setBuilder(new SolutionBuilder<>() {
            @Override public MREFLPSolution initializeSolution(MREFLPInstance i) { return new MREFLPSolution(i); }
        });
        return algorithm;
    }
}
