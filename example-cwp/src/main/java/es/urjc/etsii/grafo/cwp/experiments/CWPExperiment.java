package es.urjc.etsii.grafo.cwp.experiments;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithmBuilder;
import es.urjc.etsii.grafo.cwp.Main;
import es.urjc.etsii.grafo.cwp.constructives.CWPRandomConstructive;
import es.urjc.etsii.grafo.cwp.model.CWPInstance;
import es.urjc.etsii.grafo.cwp.model.CWPSolution;
import es.urjc.etsii.grafo.cwp.neighborhoods.SwapNodesNeighborhood;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchFirstImprovement;

import java.util.List;

/**
 * Compares the four multi-start configurations built from Mork's reusable algorithm components
 * (mirrors the {@code jmh} {@code CWPExperiment}, but constructive + local search + multi-start
 * are Mork's own instead of the hand-written port). Mork autodetects this class because it
 * extends {@link AbstractExperiment}.
 */
public class CWPExperiment extends AbstractExperiment<CWPSolution, CWPInstance> {

    private static final int MAX_ITERATIONS = 5000;

    @Override
    public List<Algorithm<CWPSolution, CWPInstance>> getAlgorithms() {
        var random = new SimpleAlgorithm<>("base", new CWPRandomConstructive());
        var firstImprovementRandom = new SimpleAlgorithm<>("base",
                new CWPRandomConstructive(), new LocalSearchFirstImprovement<>(new SwapNodesNeighborhood(true)));
        var firstImprovementLex = new SimpleAlgorithm<>("base",
                new CWPRandomConstructive(), new LocalSearchFirstImprovement<>(new SwapNodesNeighborhood(false)));
        var bestImprovement = new SimpleAlgorithm<>("base",
                new CWPRandomConstructive(), new LocalSearchBestImprovement<>(new SwapNodesNeighborhood(false)));

        return List.of(
                multiStart("Random", random),
                multiStart("First Improvement Random", firstImprovementRandom),
                multiStart("First Improvement Lex", firstImprovementLex),
                multiStart("Best Improvement", bestImprovement)
        );
    }

    private Algorithm<CWPSolution, CWPInstance> multiStart(String name, Algorithm<CWPSolution, CWPInstance> base) {
        return new MultiStartAlgorithmBuilder<CWPSolution, CWPInstance>()
                .withAlgorithmName(name)
                .withObjective(Main.MINIMIZE_WEIGHT)
                .withMaxIterations(MAX_ITERATIONS)
                .build(base);
    }
}
