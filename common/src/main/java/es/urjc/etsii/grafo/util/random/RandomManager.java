package es.urjc.etsii.grafo.util.random;

import es.urjc.etsii.grafo.util.Context;

import java.util.random.RandomGenerator;

/**
 * Multi-thread aware random manager
 */
public final class RandomManager {


    /**
     * Get RandomGenerator for the current thread. The returned RandomGenerator is not guaranteed to be thread safe, and
     * should only be used in the calling thread.
     *
     * @return RandomGenerator for the current thread
     */
    public static RandomGenerator getRandom(){
        return Context.getRandom();
    }


}

