package es.urjc.etsii.grafo.util.random;

import es.urjc.etsii.grafo.solver.SolverConfig;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * Multi-thread aware random manager
 */
@Service
public final class RandomManager {

    private static final Logger logger = Logger.getLogger(RandomManager.class.getName());

    // If the RandomManager is called before Spring starts, static methods will throw a NPE.
    private static ThreadLocal<RandomGenerator> localRandom;
    private static long[] seeds;
    private static boolean initialized = false;
    private static RandomType randomType;

    public static RandomGenerator getRandom(){
        if(!initialized){
            throw new IllegalStateException("Attempted to use RandomManager before initialization");
        }
        return localRandom.get();
    }

    // a bit hacky but uses constructor instead of static initializer for Spring compatibility
    public RandomManager(SolverConfig solverConfig){
        reinitialize(solverConfig.getRandomType(), solverConfig.getSeed(), solverConfig.getRepetitions());
    }

    /**
     * Resets random state ONLY FOR THE CALLING THREAD
     * each thread/worker is responsible for resetting their random state when appropriate
     * @param iteration Algorithm iteration current thread is going to execute.
     */
    public static void reset(int iteration){
        localRandom.set(RandomGeneratorFactory.of(RandomManager.randomType.getJavaName()).create(seeds[iteration]));
    }

    public static void reinitialize(RandomType randomType, long seed, int repetitions){
        RandomManager.randomType = randomType;
        Random initRandom = new Random(seed);
        seeds = new long[repetitions];
        for (int i = 0; i < seeds.length; i++) {
            seeds[i] = initRandom.nextLong();
        }
        logger.fine("Using seeds = " + Arrays.toString(seeds));
        localRandom = ThreadLocal.withInitial(() -> {throw new IllegalStateException("");});
        initialized = true;
    }

    /**
     * Dereference Random object for current thread, allowing it to be garbage collected.
     */
    public static void destroy(){
        localRandom.remove();
    }
}

