package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.solver.SolverConfig;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Multi-thread aware random manager
 */
@Service
public final class RandomManager {

    private static final Logger logger = Logger.getLogger(RandomManager.class.getName());

    // If the RandomManager is called before Spring starts, static methods will throw a NPE.
    private static ThreadLocal<Random> localRandom;
    private static long[] seeds;
    private static boolean initialized = false;

    public static Random getRandom(){
        if(!initialized){
            throw new IllegalStateException("Attempted to use RandomManager before initialization");
        }
        return localRandom.get();
    }

    public static int nextInt(int min, int max){
        if(max <= min){
            throw new IllegalArgumentException(String.format("Max (%s) must be strictly greater than min (%s)", max, min));
        }
        return getRandom().nextInt(max - min) + min;
    }

    // a bit hacky but uses constructor instead of static initializer for Spring compatibility
    public RandomManager(SolverConfig solverConfig){
        reinitialize(solverConfig.getSeed(), solverConfig.getRepetitions());
    }

    /**
     * Resets random state ONLY FOR THE CALLING THREAD
     * each thread/worker is responsible of resetting their random state when appropriate
     * @param iteration Algorithm iteration current thread is going to execute.
     */
    public static void reset(int iteration){
        getRandom().setSeed(seeds[iteration]);
    }

    public static void reinitialize(long seed, int repetitions){
        Random initRandom = new Random(seed);
        seeds = new long[repetitions];
        for (int i = 0; i < seeds.length; i++) {
            seeds[i] = initRandom.nextLong();
        }
        logger.fine("Using seeds = " + Arrays.toString(seeds));
        localRandom = ThreadLocal.withInitial(() -> new Random(0));
        initialized = true;
    }

    /**
     * Dereference Random object for current thread, allowing it to be garbage collected.
     */
    public static void destroy(){
        localRandom.remove();
    }
}

