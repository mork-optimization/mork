package es.urjc.etsii.grafo.improve;

import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.metrics.Metrics;
import es.urjc.etsii.grafo.solution.metrics.MetricsManager;
import es.urjc.etsii.grafo.util.DoubleComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiPredicate;

/**
 * Any method that improves a given solution is called an Improver. The classical example, but not limited to, is a local search.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@AlgorithmComponent
public abstract class Improver<S extends Solution<S,I>,I extends Instance> {

    private static final Logger log = LoggerFactory.getLogger(Improver.class);

    protected final boolean ofMaximize;
    protected final BiPredicate<Double, Double> ofIsBetter;

    protected Improver(boolean ofMaximize) {
        this.ofMaximize = ofMaximize;
        this.ofIsBetter = DoubleComparator.isBetterFunction(ofMaximize);
    }

    /**
     * Improves a model.Solution
     * Iterates until we run out of time, or we cannot improve the current es.urjc.etsii.grafo.solution any further
     *
     * @param solution model.Solution to improve
     * @return Improved s
     */
    public S improve(S solution){
        // Before
        long startTime = System.nanoTime();
        double initialScore = solution.getScore();

        // Improve
        S improvedSolution = this._improve(solution);

        // After
        long endTime = System.nanoTime();
        long elapsedMillis = (endTime - startTime) / 1_000_000;
        double endScore = improvedSolution.getScore();

        // Log, verify and store
        log.debug("Done in {}: {} --> {}", elapsedMillis, initialScore, endScore);
        if(ofIsBetter.test(initialScore, endScore)){
            throw new IllegalStateException(String.format("Score has worsened after executing an improvement method: %s --> %s", initialScore, endScore));
        }
        if(ofIsBetter.test(endScore, initialScore)){
            MetricsManager.addDatapoint(Metrics.BEST_OBJECTIVE_FUNCTION, endScore);
        }
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
        private NullImprover() {
            super(false); // It does not matter as it does nothing
        }

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
