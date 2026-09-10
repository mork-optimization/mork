package es.urjc.etsii.grafo.mmdp.experiments;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithmBuilder;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchFirstImprovement;
import es.urjc.etsii.grafo.mmdp.Main;
import es.urjc.etsii.grafo.mmdp.constructives.MMDPRandomConstructive;
import es.urjc.etsii.grafo.mmdp.model.MMDPInstance;
import es.urjc.etsii.grafo.mmdp.model.MMDPSolution;
import es.urjc.etsii.grafo.mmdp.neighborhoods.ReplaceNodeNeighborhood;

import java.util.List;

/**
 * Compares the four multi-start configurations built from Mork's reusable algorithm components
 * (mirrors the {@code jmh} {@code MMDPExperiment}, but constructive + local search + multi-start
 * are Mork's own instead of the hand-written port). Mork autodetects this class because it
 * extends {@link AbstractExperiment}.
 */
public class MMDPExperiment extends AbstractExperiment<MMDPSolution, MMDPInstance> {

    private static final int MAX_ITERATIONS = 5000;

    @Override
    public List<Algorithm<MMDPSolution, MMDPInstance>> getAlgorithms() {
        var random = new SimpleAlgorithm<>("base", new MMDPRandomConstructive());
        var firstImprovementRandom = new SimpleAlgorithm<>("base",
                new MMDPRandomConstructive(), new LocalSearchFirstImprovement<>(new ReplaceNodeNeighborhood(true)));
        var firstImprovementLex = new SimpleAlgorithm<>("base",
                new MMDPRandomConstructive(), new LocalSearchFirstImprovement<>(new ReplaceNodeNeighborhood(false)));
        var bestImprovement = new SimpleAlgorithm<>("base",
                new MMDPRandomConstructive(), new LocalSearchBestImprovement<>(new ReplaceNodeNeighborhood(false)));

        return List.of(
                multiStart("Random", random),
                multiStart("First Improvement Random", firstImprovementRandom),
                multiStart("First Improvement Lex", firstImprovementLex),
                multiStart("Best Improvement", bestImprovement)
        );
    }

    private Algorithm<MMDPSolution, MMDPInstance> multiStart(String name, Algorithm<MMDPSolution, MMDPInstance> base) {
        return new MultiStartAlgorithmBuilder<MMDPSolution, MMDPInstance>()
                .withAlgorithmName(name)
                .withObjective(Main.MAXIMIZE_DIVERSITY)
                .withMaxIterations(MAX_ITERATIONS)
                .build(base);
    }
}
