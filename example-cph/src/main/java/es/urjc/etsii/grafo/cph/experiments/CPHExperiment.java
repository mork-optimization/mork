package es.urjc.etsii.grafo.cph.experiments;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithmBuilder;
import es.urjc.etsii.grafo.cph.Main;
import es.urjc.etsii.grafo.cph.constructives.CPHRandomConstructive;
import es.urjc.etsii.grafo.cph.model.CPHInstance;
import es.urjc.etsii.grafo.cph.model.CPHSolution;
import es.urjc.etsii.grafo.cph.neighborhoods.ChangeHubNeighborhood;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchFirstImprovement;

import java.util.List;

/**
 * Compares the four multi-start configurations built from Mork's reusable algorithm components
 * (mirrors the {@code jmh} {@code CPHExperiment}, but constructive + local search + multi-start
 * are Mork's own instead of the hand-written port). Mork autodetects this class because it
 * extends {@link AbstractExperiment}.
 */
public class CPHExperiment extends AbstractExperiment<CPHSolution, CPHInstance> {

    private static final int MAX_ITERATIONS = 5000;

    @Override
    public List<Algorithm<CPHSolution, CPHInstance>> getAlgorithms() {
        var random = new SimpleAlgorithm<>("base", new CPHRandomConstructive());
        var firstImprovementRandom = new SimpleAlgorithm<>("base",
                new CPHRandomConstructive(), new LocalSearchFirstImprovement<>(new ChangeHubNeighborhood(true)));
        var firstImprovementLex = new SimpleAlgorithm<>("base",
                new CPHRandomConstructive(), new LocalSearchFirstImprovement<>(new ChangeHubNeighborhood(false)));
        var bestImprovement = new SimpleAlgorithm<>("base",
                new CPHRandomConstructive(), new LocalSearchBestImprovement<>(new ChangeHubNeighborhood(false)));

        return List.of(
                multiStart("Random", random),
                multiStart("First Improvement Random", firstImprovementRandom),
                multiStart("First Improvement Lex", firstImprovementLex),
                multiStart("Best Improvement", bestImprovement)
        );
    }

    private Algorithm<CPHSolution, CPHInstance> multiStart(String name, Algorithm<CPHSolution, CPHInstance> base) {
        return new MultiStartAlgorithmBuilder<CPHSolution, CPHInstance>()
                .withAlgorithmName(name)
                .withObjective(Main.MINIMIZE_WEIGHT)
                .withMaxIterations(MAX_ITERATIONS)
                .build(base);
    }
}
