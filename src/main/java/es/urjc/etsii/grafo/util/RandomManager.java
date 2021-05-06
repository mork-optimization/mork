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
    private static boolean initialized = false;

    public static Random getRandom(){
        if(!initialized){
            throw new IllegalStateException("Attempted to use RandomManager before initialization");
        }
        return localRandom.get();
    }

    public static int nextInt(int min, int max){
        return getRandom().nextInt(max - min) + min;
    }

    // a bit hacky but uses constructor instead of static initializer for Spring compatibility
    protected RandomManager(@Value("${seed}") int seed){
        if(initialized){
            logger.warning(String.format("RandomManager already initialized with seed %s, overwritted with %s", initialSeed, seed));
        }
        initialSeed = seed;
        logger.info("Using initial seed = " + initialSeed);
        localRandom = ThreadLocal.withInitial(() -> {
            var r = new Random(initialSeed);
            return r;
        });
        initialized = true;
    }

    /**
     * Resets random state ONLY FOR THE CALLING THREAD
     * each thread is responsible of resetting their random state when appropriate
     */
    public static void reset(){
        reset(0);
    }

    /**
     * Resets random state ONLY FOR THE CALLING THREAD
     * each thread is responsible of resetting their random state when appropriate
     * @param iteration Algorithm iteration current thread is going to execute.
     */
    public static void reset(int iteration){
        getRandom().setSeed(initialSeed+iteration);
    }

}

