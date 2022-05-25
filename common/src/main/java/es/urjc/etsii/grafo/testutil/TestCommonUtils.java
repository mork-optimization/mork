package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.util.random.RandomManager;
import es.urjc.etsii.grafo.util.random.RandomType;

import java.util.random.RandomGenerator;

public class TestCommonUtils {

    public static RandomGenerator initRandom(RandomType type, int seed, int repetitions){
        return initRandom(type, seed, repetitions, 0);
    }

    public static RandomGenerator initRandom(RandomType type, int seed, int repetitions, int iteration){
        RandomManager.reinitialize(type, seed, repetitions);
        RandomManager.reset(iteration);
        return RandomManager.getRandom();
    }
}
