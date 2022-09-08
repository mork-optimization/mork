package es.urjc.etsii.grafo.improve.ls;

import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.metrics.Metrics;
import es.urjc.etsii.grafo.solution.metrics.MetricsManager;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.TimeControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiPredicate;
import java.util.function.ToDoubleFunction;

/**
 * Local search procedures start from a given feasible solution and explore a determined neighborhood
 * in each iteration, replacing the current solution if a neighbor solution improves the objective
 * function of the current one. The search ends when all neighbor solutions are worse  meaning that a local optimum
 * is found.
 * <p>
 * There exist two typical strategies to explore the corresponding neighborhood:
 * best improvement and first improvement. To use or extend those procedures see {@link LocalSearchBestImprovement}
 * and {@link LocalSearchFirstImprovement} respectively.
 *
 * @param <M> type of move
 * @param <S> type of the problem solution
 * @param <I> type of the problem instance
 */
public abstract class LocalSearch<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> extends Improver<S, I> {

    private static final Logger log = LoggerFactory.getLogger(LocalSearch.class);

    private static final int WARN_LIMIT = 100_000;

    protected final Neighborhood<M, S, I> neighborhood;
    protected final BiPredicate<Double, Double> fIsBetter;
    private final boolean fMaximize;
    protected final ToDoubleFunction<M> f;

    /**
     * Create a new local search method using the given neighborhood
     * @param neighborhood neighborhood to use
     * @param ofMaximize true if the problem objective function is maximizing, false otherwise
     * @param fMaximize true if we should maximize the values returned by function f, false otherwise
     * @param f function used to get a double value from a move
     */
    protected LocalSearch(boolean ofMaximize, Neighborhood<M, S, I> neighborhood, boolean fMaximize, ToDoubleFunction<M> f) {
        super(ofMaximize);
        this.neighborhood = neighborhood;
        this.fMaximize = fMaximize;
        this.fIsBetter = DoubleComparator.isBetterFunction(fMaximize);
        this.f = f;
    }

    /**
     * Create a new local search method using the given neighborhood.
     * Uses the method Move::getValue as the guiding function, with fMaximize = maximize.
     * @param neighborhood neighborhood to use
     * @param maximize true if the problem objective function is maximizing, false otherwise
     */
    protected LocalSearch(boolean maximize, Neighborhood<M, S, I> neighborhood) {
        this(maximize, neighborhood, maximize, Move::getValue);
    }

    /**
     * {@inheritDoc}
     *
     * Improves a model.Solution
     * Iterates until we run out of time, or we cannot improve the current solution any further
     */
    @Override
    protected S _improve(S solution) {
        int rounds = 1;
        boolean improved = true;
        while (!TimeControl.isTimeUp() && improved) {
            log.debug("Executing iteration {} for {}", rounds, this.getClass().getSimpleName());
            improved = iteration(solution);
            rounds++;
            if(rounds == WARN_LIMIT){
                log.warn("Localsearch method {} may be stuck in an infinite loop (warn at {})", this.getClass().getSimpleName(),  WARN_LIMIT);
            }
        }
        log.debug("Improvement {} ended after {} iterations.", this.getClass().getSimpleName(), rounds);
        return solution;
    }

    /**
     * This procedure check if there are valid moves to neighbors solutions.
     * In that case, the move is executed. Otherwise, the procedure ends.
     * @return true if the solution has improved, false otherwise
     */
    public boolean iteration(S solution) {
        // Get next move to execute
        var move = getMove(solution);
        if (move == null) {
            return false; // There are no valid moves in the neighborhood, end local search
        }

        // Execute move, save metric if improved, and ask for another iteration
        double scoreBefore = solution.getScore();
        move.execute(solution);
        double scoreAfter = solution.getScore();
        if(this.ofIsBetter.test(scoreAfter, scoreBefore)){
            MetricsManager.addDatapoint(Metrics.BEST_OBJECTIVE_FUNCTION, scoreAfter);
        }
        return true;
    }


    /**
     * Get next move to execute, different strategies are possible
     *
     * @param solution Solution
     * @return Proposed move, null if there are no candidate moves in the neighborhood
     */
    public abstract M getMove(S solution);

}
