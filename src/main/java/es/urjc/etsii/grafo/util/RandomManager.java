package es.urjc.etsii.grafo.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Multi-thread aware random manager
 */
@Service
public final class RandomManager {

    // If the RandomManager is called before Spring starts, static methods will throw a NPE.
    private final AtomicInteger SEED;
    private static ThreadLocal<Random> localRandom;

    public static Random getRandom(){
        return localRandom.get();
    }

    public static int nextInt(int min, int max){
        return localRandom.get().nextInt(max - min) + min;
    }

    protected RandomManager(@Value("${randomseed}") int seed){
        SEED = new AtomicInteger(seed);
        System.out.println("[INFO] Using seed = " + SEED.get());
        localRandom = ThreadLocal.withInitial(() -> new Random(SEED.getAndIncrement()));
    }
}

