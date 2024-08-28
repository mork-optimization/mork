package es.urjc.etsii.grafo.improve;

import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.metrics.DeclaredObjective;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Any method that improves a given solution is called an Improver. The classical example, but not limited to, is a local search.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@AlgorithmComponent
public abstract class Improver<S extends Solution<S,I>,I extends Instance> {

    private static final Logger log = LoggerFactory.getLogger(Improver.class);

    protected final Objective<?,S,I> objective;

    /**
     * Initialize common improver fields, to be called by subclasses
     * @param objective MAXIMIZE to maximize scores returned by the given move, MINIMIZE for minimizing
     */
    protected Improver(Objective<?,S,I> objective) {
        this.objective = objective;
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
        if(objective.isBetter(initialScore, endScore)){
            throw new IllegalStateException(String.format("Score has worsened after executing an improvement method: %s --> %s", initialScore, endScore));
        }
        Metrics.add(DeclaredObjective.class, solution.getScore());

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
    @SuppressWarnings("varargs")
    public static <S extends Solution<S,I>, I extends Instance> Improver<S,I> serial(Objective<?,S,I> objective, Improver<S, I>... improvers){
        return new SequentialImprover<>(objective, improvers);
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <S extends Solution<S,I>, I extends Instance> Improver<S,I> serial(Improver<S, I>... improvers){
        return new SequentialImprover<>(improvers);
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
            super(Context.getMainObjective()); // It does not matter as it does nothing
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
        @SuppressWarnings("varargs")
        public SequentialImprover(Objective<?,S,I> objective, Improver<S, I>... improvers) {
            super(objective);
            this.improvers = improvers;
        }

        @SafeVarargs
        @SuppressWarnings("varargs")
        public SequentialImprover(Improver<S, I>... improvers) {
            this(Context.getMainObjective(), improvers);
        }

        @AutoconfigConstructor
        @SuppressWarnings({"unchecked", "rawtype"})
        public SequentialImprover(
                Improver<S, I> improverA,
                Improver<S, I> improverB
        ) {
            this(new Improver[]{improverA, improverB});
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
