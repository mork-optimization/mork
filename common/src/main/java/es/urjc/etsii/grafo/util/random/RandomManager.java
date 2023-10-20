package es.urjc.etsii.grafo.util.random;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/**
 * Multi-thread aware random manager
 */
public final class RandomManager {

    private static class RandomThreadLocal extends InheritableThreadLocal<RandomGenerator.JumpableGenerator> {
        @Override
        protected RandomGenerator.JumpableGenerator childValue(RandomGenerator.JumpableGenerator parentValue) {
            return (RandomGenerator.JumpableGenerator) parentValue.copyAndJump();
        }
    }

    private static final Logger logger = Logger.getLogger(RandomManager.class.getName());

    // If the RandomManager is called before Spring starts, static methods will throw a NPE.
    private static RandomThreadLocal localRandom;
    private static long[] seeds;
    private static boolean initialized = false;
    private static RandomType randomType;

    /**
     * Get RandomGenerator for the current thread. The returned RandomGenerator is not guaranteed to be thread safe, and
     * should only be used in the calling thread.
     *
     * @return RandomGenerator for the current thread
     */
    public static RandomGenerator getRandom(){
        if(!initialized){
            throw new IllegalStateException("Attempted to use RandomManager before initialization");
        }
        return localRandom.get();
    }

    /**
     * Resets random state ONLY FOR THE CALLING THREAD
     * each thread/worker is responsible for resetting their random state when appropriate
     *
     * @param iteration Algorithm iteration current thread is going to execute.
     */
    public static void reset(int iteration){
        localConfiguration(RandomManager.randomType, seeds[iteration]);
    }

    /**
     * Reset all random generators with the given initial seed
     *
     * @param randomType random type
     * @param seed random seed
     * @param repetitions (instance, algorithm) iteration
     */
    public static void globalConfiguration(RandomType randomType, long seed, int repetitions){
        RandomManager.randomType = randomType;
        Random initRandom = new Random(seed);
        seeds = new long[repetitions];
        for (int i = 0; i < seeds.length; i++) {
            seeds[i] = initRandom.nextLong();
        }
        logger.fine("Using seeds = " + Arrays.toString(seeds));
        localRandom = new RandomThreadLocal();
        initialized = true;
    }

    /**
     * Initialize random only in current thread
     * @param randomType random type
     * @param seed random seed
     */
    public static void localConfiguration(RandomType randomType, long seed){
        var rnd = RandomGeneratorFactory.of(randomType.getJavaName()).create(seed);
        if(rnd instanceof RandomGenerator.JumpableGenerator jumpableGenerator){
            localRandom.set(jumpableGenerator);
        } else {
            throw new IllegalArgumentException("RandomGenerator %s is not of type JumpableGenerator".formatted(randomType));
        }
    }

    /**
     * Remove Random object for current thread, allowing it to be garbage collected.
     */
    public static void destroy(){
        localRandom.remove();
    }
}

