package es.urjc.etsii.grafo.improve;

import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.logging.Logger;

/**
 * Any method that improves a given solution is called an Improver. The classical example, but not limited to, is a local search.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@AlgorithmComponent
public abstract class Improver<S extends Solution<S,I>,I extends Instance> {

    private static final Logger log = Logger.getLogger(Improver.class.getName());

    /**
     * Improves a model.Solution
     * Iterates until we run out of time, or we cannot improve the current es.urjc.etsii.grafo.solution any further
     *
     * @param solution model.Solution to improve
     * @return Improved s
     */
    public S improve(S solution){
        long startTime = System.nanoTime();
        double initialScore = solution.getScore();
        S improvedSolution = this._improve(solution);
        long endTime = System.nanoTime();
        long ellapedMillis = (endTime - startTime) / 1_000_000;
        double endScore = improvedSolution.getScore();
        log.fine(String.format("Done in %s: %s --> %s", ellapedMillis, initialScore, endScore));
        return improvedSolution;
    }

    /**
     * Create a no operation improve method
     * Returns the solution immediately without executing any operation
     * @param <S> Solution class
     * @param <I> Instance class
     * @return Null improve method
     */
    public static <S extends Solution<S,I>, I extends Instance> Improver<S,I> nul(){
        return new NullImprover<>();
    }

    /**
     * Do nothing local search
     *
     * @param <S> Solution class
     * @param <I> Instance class
     */
    private static class NullImprover<S extends Solution<S,I>,I extends Instance> extends Improver<S,I> {
        @Override
        protected S _improve(S solution) {
            return solution;
        }
    }

    /**
     * Improves a Solution
     * Iterates until we run out of time, or we cannot improve the current solution any further
     *
     * @param solution Solution to improve
     * @return Improved solution
     */
    protected abstract S _improve(S solution);
}
