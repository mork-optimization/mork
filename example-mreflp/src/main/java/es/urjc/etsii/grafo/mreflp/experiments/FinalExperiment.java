package es.urjc.etsii.grafo.mreflp.experiments;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithm;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.mreflp.Main;
import es.urjc.etsii.grafo.mreflp.alg.LMLSVariant;
import es.urjc.etsii.grafo.mreflp.model.MREFLPInstance;
import es.urjc.etsii.grafo.mreflp.model.MREFLPSolution;

import java.util.ArrayList;
import java.util.List;

public class FinalExperiment extends AbstractExperiment<MREFLPSolution, MREFLPInstance> {

    private final AutomaticAlgorithmBuilder<MREFLPSolution, MREFLPInstance> builder;

    public FinalExperiment(AutomaticAlgorithmBuilder<MREFLPSolution, MREFLPInstance> builder) {
        this.builder = builder;
    }

    @Override
    public List<Algorithm<MREFLPSolution, MREFLPInstance>> getAlgorithms() {
        var algorithms = new ArrayList<>(builder.buildTopElites("autoconfig-final-elites.json", 3));

        var sota = PaperExperiment.paperAlgorithm(LMLSVariant.LMLS, 0);
        sota.setName("SOTA-LMLS");
        algorithms.add(sota);

        return makeMultistart(algorithms);
    }

    public List<Algorithm<MREFLPSolution, MREFLPInstance>> makeMultistart(List<Algorithm<MREFLPSolution, MREFLPInstance>> algorithms){
        var multistarts = new ArrayList<Algorithm<MREFLPSolution, MREFLPInstance>>();
        for (var algorithm: algorithms){
            multistarts.add(makeMultistart(algorithm));
        }
        return multistarts;
    }

    public Algorithm<MREFLPSolution, MREFLPInstance> makeMultistart(Algorithm<MREFLPSolution, MREFLPInstance> algorithm){
        var name = algorithm.getName();
        return new MultiStartAlgorithm<>(name, Main.COST, algorithm, 1_000_000, 1_000_000, 1_000_000);
    }
}
