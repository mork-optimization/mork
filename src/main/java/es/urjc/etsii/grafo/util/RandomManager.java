package es.urjc.etsii.grafo.util;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Multi-threads aware random manager
 * Use -Dseed=1111 when starting the JVM to set the initial seed
 */
public final class RandomManager {

    // TODO usar $SEED como variable de entorno para facilitar la integracion con Docker
    private static final AtomicInteger SEED;
    private static final ThreadLocal<Random> localRandom;

    static {
        String maybeSeed = System.getProperty("seed");
        SEED = new AtomicInteger(maybeSeed == null ? 1234 : Integer.parseInt(maybeSeed));
        System.out.println("[INFO] Using seed = " + SEED.get());
        localRandom = ThreadLocal.withInitial(() -> new Random(SEED.getAndIncrement()));
    }

    public static Random getRandom(){
        return localRandom.get();
    }

    public static int nextInt(int min, int max){
        return localRandom.get().nextInt(max - min) + min;
    }

    private RandomManager(){}
}

