package es.urjc.etsii.grafo.solver.improve;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.logging.Logger;

/**
 * Any method that improves a given solution is called an Improver. The classical example, but not limited to, is a local search.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
public abstract class Improver<S extends Solution<S,I>,I extends Instance> {

    private static final Logger log = Logger.getLogger(Improver.class.getName());

    /**
     * Improves a model.Solution
     * Iterates until we run out of time, or we cannot improve the current es.urjc.etsii.grafo.solution any further
     *
     * @param s model.Solution to improve
     * @return Improved s
     */
    public S improve(S s){
        long startTime = System.nanoTime();
        double initialScore = s.getScore();
        S improvedSolution = this._improve(s);
        long endTime = System.nanoTime();
        long ellapedMillis = (endTime - startTime) / 1_000_000;
        double endScore = improvedSolution.getScore();
        log.fine(String.format("Done in %s: %s --> %s", ellapedMillis, initialScore, endScore));
        return improvedSolution;
    }

    /**
     * Improves a model.Solution
     * Iterates until we run out of time, or we cannot improve the current es.urjc.etsii.grafo.solution any further
     *
     * @param s model.Solution to improve
     * @return Improved s
     */
    protected abstract S _improve(S s);
}
