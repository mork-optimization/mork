package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.random.RandomManager;
import es.urjc.etsii.grafo.util.random.RandomType;

import java.util.random.RandomGenerator;

public class TestCommonUtils {

    public static SolverConfig solverConfig(RandomType type, int seed, int repetitions){
        var config = new SolverConfig();
        config.setSeed(seed);
        config.setRepetitions(repetitions);
        config.setRandomType(type);
        return config;
    }

    public static RandomGenerator initRandom(){
        return initRandom(TestCommonUtils.solverConfig(RandomType.DEFAULT, 0, 1));
    }
    public static RandomGenerator initRandom(SolverConfig config){
        return initRandom(config, 0);
    }

    public static RandomGenerator initRandom(SolverConfig config, int iteration){
        Context.Configurator.resetRandom(config, iteration);
        return RandomManager.getRandom();
    }
}
