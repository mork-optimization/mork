package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.util.RandomManager;

public class HelperFactory {
    public static RandomManager getRandomManager(int seed, int repetitions){
        SolverConfig solverConfig = new SolverConfig();
        solverConfig.setSeed(seed);
        solverConfig.setRepetitions(repetitions);
        RandomManager r = new RandomManager(solverConfig);
        return r;
    }
}
