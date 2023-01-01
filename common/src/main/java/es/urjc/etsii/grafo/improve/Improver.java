package es.urjc.etsii.grafo.improve;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.ProvidedParam;
import es.urjc.etsii.grafo.annotations.ProvidedParamType;
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

    protected final FMode fmode;
    protected final BiPredicate<Double, Double> ofIsBetter;

    /**
     * Initialize common improver fields, to be called by subclasses
     * @param fmode MAXIMIZE to maximize scores returned by the given move, MINIMIZE for minimizing
     */
    protected Improver(FMode fmode) {
        this.fmode = fmode;
        this.ofIsBetter = DoubleComparator.isBetterFunction(fmode);
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

    @SafeVarargs
    public static <S extends Solution<S,I>, I extends Instance> Improver<S,I> serial(FMode fmode, Improver<S, I>... improvers){
        return new SequentialImprover<>(fmode, improvers);
    }

    /**
     * Do nothing local search
     *
     * @param <S> Solution class
     * @param <I> Instance class
     */
    public static class NullImprover<S extends Solution<S,I>,I extends Instance> extends Improver<S,I> {

        @AutoconfigConstructor
        public NullImprover() {
            super(FMode.MINIMIZE); // It does not matter as it does nothing
        }

        @Override
        protected S _improve(S solution) {
            return solution;
        }

        @Override
        public S improve(S solution) {
            return solution;

        }
    }

    public static class SequentialImprover<S extends Solution<S,I>,I extends Instance> extends Improver<S,I> {

        private final Improver<S,I>[] improvers;

        @SafeVarargs
        public SequentialImprover(FMode fmode, Improver<S, I>... improvers) {
            super(fmode);
            this.improvers = improvers;
        }

        @AutoconfigConstructor
        public SequentialImprover(
                @ProvidedParam(type = ProvidedParamType.MAXIMIZE) FMode fmode,
                Improver<S, I> improverA,
                Improver<S, I> improverB
        ) {
            super(fmode);
            this.improvers = new Improver[]{improverA, improverB};
        }

        @Override
        protected S _improve(S solution) {
            for(var improver: improvers){
                solution = improver._improve(solution);
            }
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
