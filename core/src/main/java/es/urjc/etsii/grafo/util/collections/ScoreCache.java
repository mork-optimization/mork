package es.urjc.etsii.grafo.util.collections;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

/**
 * ScoreCache, ejects unused objects and completely destroys the cache
 * if the VM has low free memory. GIV ME RAMMMM nomnomnom
 *
 * @param <S> Solution type
 * @param <I> Instance type
 */
public class ScoreCache<S extends Solution<S,I>,I extends Instance> {

    /**
     * Maximum cache size
     */
    private static final int MAX_SIZE = 1_000_000;

    /**
     * % of Java VM Free RAM to use for the ScoreCache
     */
    private static final double USAGE_RATIO = 0.8;

    /**
     * Almacenamos para cada hash el valor correcto de la f.o.
     */
    private Cache<S, Double> cache = Caffeine.newBuilder().maximumSize(MAX_SIZE).build();

    /**
     * Get free memory in bytes
     * @return free memory in bytes
     */
    private static long getMaxMemory(){
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * Returns solution score if cached, else calculates, puts in cache and returns it.
     *
     * @param sol solution to calculate score
     * @return Current solution score
     */
    public Double getScore(S sol){
        return cache.get(sol, Solution::getScore);
    }

}
