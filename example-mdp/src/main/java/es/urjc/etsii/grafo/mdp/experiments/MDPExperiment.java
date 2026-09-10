package es.urjc.etsii.grafo.mdp.experiments;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithmBuilder;
import es.urjc.etsii.grafo.algorithms.scattersearch.ScatterSearchBuilder;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.mdp.Main;
import es.urjc.etsii.grafo.mdp.constructives.MDPGreedyConstructive;
import es.urjc.etsii.grafo.mdp.constructives.MDPRandomConstructive;
import es.urjc.etsii.grafo.mdp.constructives.TabuD2Constructive;
import es.urjc.etsii.grafo.mdp.improve.TabuSearchImprover;
import es.urjc.etsii.grafo.mdp.model.MDPInstance;
import es.urjc.etsii.grafo.mdp.model.MDPSolution;
import es.urjc.etsii.grafo.mdp.neighborhoods.SwapNodeNeighborhood;
import es.urjc.etsii.grafo.mdp.scattersearch.MDPD2Combinator;
import es.urjc.etsii.grafo.mdp.scattersearch.MDPRandomCombinator;
import es.urjc.etsii.grafo.mdp.scattersearch.MDPSolutionDistance;
import es.urjc.etsii.grafo.mdp.scattersearch.TabuD2Combinator;
import es.urjc.etsii.grafo.mdp.scattersearch.TabuD2Memory;

import java.util.List;

/**
 * Compares a random-constructive baseline against the three Scatter Search configurations,
 * assembled from Mork's reusable {@link ScatterSearchBuilder} (mirrors the {@code jmh}
 * {@code MDPExperiment} / the {@code mork-experiments} hand-written Scatter Search port). Mork
 * autodetects this class because it extends {@link AbstractExperiment}.
 */
public class MDPExperiment extends AbstractExperiment<MDPSolution, MDPInstance> {

    private static final int MAX_ITERATIONS = 5000;

    /** Mirrors the hand-written port: NUM_BEST_SOLUTIONS(5) + NUM_DIVERSE_SOLUTIONS(5). */
    private static final int REFSET_SIZE = 10;
    private static final double DIVERSITY_RATIO = 0.5;

    @Override
    public List<Algorithm<MDPSolution, MDPInstance>> getAlgorithms() {
        var randomConstructive = new MultiStartAlgorithmBuilder<MDPSolution, MDPInstance>()
                .withAlgorithmName("Random Constructive")
                .withObjective(Main.MAXIMIZE_DIVERSITY)
                .withMaxIterations(MAX_ITERATIONS)
                .build(new SimpleAlgorithm<>("base", new MDPRandomConstructive()));

        var withoutInf = new ScatterSearchBuilder<MDPSolution, MDPInstance>()
                .withName("SS without Information")
                .withObjective(Main.MAXIMIZE_DIVERSITY)
                .withConstructive(new MDPRandomConstructive())
                .withConstructiveForDiversity(new MDPRandomConstructive())
                .withImprover(new LocalSearchBestImprovement<>(new SwapNodeNeighborhood()))
                .withCombinator(new MDPRandomCombinator())
                .withDistance(new MDPSolutionDistance())
                .withRefsetSize(REFSET_SIZE)
                .withDiversity(DIVERSITY_RATIO)
                .build();

        var withInf = new ScatterSearchBuilder<MDPSolution, MDPInstance>()
                .withName("SS with Information")
                .withObjective(Main.MAXIMIZE_DIVERSITY)
                .withConstructive(new MDPGreedyConstructive(0.5))
                .withConstructiveForDiversity(new MDPRandomConstructive())
                .withImprover(new LocalSearchBestImprovement<>(new SwapNodeNeighborhood()))
                .withCombinator(new MDPD2Combinator())
                .withDistance(new MDPSolutionDistance())
                .withRefsetSize(REFSET_SIZE)
                .withDiversity(DIVERSITY_RATIO)
                .build();

        TabuD2Memory memory = new TabuD2Memory();
        var withMemory = new ScatterSearchBuilder<MDPSolution, MDPInstance>()
                .withName("SS with Memory")
                .withObjective(Main.MAXIMIZE_DIVERSITY)
                .withConstructive(new TabuD2Constructive(memory))
                .withConstructiveForDiversity(new MDPRandomConstructive())
                .withImprover(new TabuSearchImprover(Main.MAXIMIZE_DIVERSITY))
                .withCombinator(new TabuD2Combinator(memory))
                .withDistance(new MDPSolutionDistance())
                .withRefsetSize(REFSET_SIZE)
                .withDiversity(DIVERSITY_RATIO)
                .build();

        return List.of(randomConstructive, withoutInf, withInf, withMemory);
    }
}
