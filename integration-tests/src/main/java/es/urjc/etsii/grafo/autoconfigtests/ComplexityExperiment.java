package es.urjc.etsii.grafo.autoconfigtests;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.autoconfigtests.model.ACInstance;
import es.urjc.etsii.grafo.autoconfigtests.model.ACSolution;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ComplexityExperiment extends AbstractExperiment<ACSolution, ACInstance> {
    @Override
    public List<Algorithm<ACSolution, ACInstance>> getAlgorithms() {
        return List.of(
                new SimpleAlgorithm<>("SimpleAlgorithm", new SleepyConstructive(),
                        Improver.serial(new SleepyImprover(), new SleepyLogImprover())
                )
        );
    }

    private static long getNoise(double mean, double stdDev) {
        long sleep =  (long) RandomManager.getRandom().nextGaussian(mean, stdDev);
        return Math.max(sleep, 0);
    }

    private static class SleepyImprover extends Improver<ACSolution, ACInstance> {
        public SleepyImprover() {
            super(Main.AC_OBJECTIVE);
        }

        @Override
        public ACSolution improve(ACSolution solution) {
            long sleep = solution.getInstance().length() * 1000L + getNoise(10000, 1000);
            ConcurrencyUtil.sleep(sleep, TimeUnit.MICROSECONDS);
            return solution;
        }
    }

    private static class SleepyLogImprover extends Improver<ACSolution, ACInstance> {
        public SleepyLogImprover() {
            super(Main.AC_OBJECTIVE);
        }

        @Override
        public ACSolution improve(ACSolution solution) {
            var sleep = Math.log(solution.getInstance().length()) * 3;
            sleep *= 1000; // millis to micro

            ConcurrencyUtil.sleep((long) sleep, TimeUnit.MICROSECONDS);
            return solution;
        }
    }

    private static class SleepyConstructive extends Constructive<ACSolution, ACInstance> {
        @Override
        public ACSolution construct(ACSolution solution) {
            // sleep with some noise to simulate different "constant" execution times
            ConcurrencyUtil.sleep(getNoise(1000, 1000), TimeUnit.MICROSECONDS);
            solution.notifyUpdate();
            return solution;
        }
    }

}
