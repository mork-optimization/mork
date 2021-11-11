package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.util.random.RandomManager;
import es.urjc.etsii.grafo.util.random.RandomType;

public class HelperFactory {
    public static RandomManager getRandomManager(RandomType type, int seed, int repetitions){
        SolverConfig solverConfig = new SolverConfig();
        solverConfig.setSeed(seed);
        solverConfig.setRepetitions(repetitions);
        solverConfig.setRandomType(type);
        RandomManager r = new RandomManager(solverConfig);
        RandomManager.reinitialize(type, seed, repetitions);
        RandomManager.reset(0);
        return r;
    }
}
