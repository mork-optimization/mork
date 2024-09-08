package es.urjc.etsii.grafo.autoconfigtests;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.autoconfigtests.model.ACInstance;
import es.urjc.etsii.grafo.autoconfigtests.model.ACSolution;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ComplexityExperiment extends AbstractExperiment<ACSolution, ACInstance> {
    @Override
    public List<Algorithm<ACSolution, ACInstance>> getAlgorithms() {
        return List.of(
                new SimpleAlgorithm<>("SimpleAlgorithm", Constructive.nul(), new SleepyImprover())
        );
    }

    private static class SleepyImprover extends Improver<ACSolution, ACInstance> {
        public SleepyImprover() {
            super(Objective.ofDefaultMaximize());
        }

        @Override
        public ACSolution _improve(ACSolution solution) {
            ConcurrencyUtil.sleep(solution.getInstance().length(), TimeUnit.MILLISECONDS);
            return solution;
        }
    }

}
