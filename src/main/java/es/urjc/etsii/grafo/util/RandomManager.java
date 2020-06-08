package es.urjc.etsii.grafo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    private static int initialSeed;

    public static Random getRandom(){
        return localRandom.get();
    }

    public static int nextInt(int min, int max){
        return localRandom.get().nextInt(max - min) + min;
    }

    // a bit hacky but uses constructor instead of static initializer for Spring compatibility
    protected RandomManager(@Value("${seed}") int seed){
        initialSeed = seed;
        logger.info("Using initial seed = " + initialSeed);
        localRandom = ThreadLocal.withInitial(() -> {
            var r = new Random(initialSeed);
            return r;
        });
    }

    /**
     * Resets random state ONLY FOR THE CALLING THREAD
     * each thread is responsible of resetting their random state when appropriate
     */
    public static void reset(){
        RandomManager.localRandom.get().setSeed(initialSeed);
    }

    /**
     * Resets random state ONLY FOR THE CALLING THREAD
     * each thread is responsible of resetting their random state when appropriate
     */
    public static void reset(int iteration){
        RandomManager.localRandom.get().setSeed(initialSeed+iteration);
    }
}

