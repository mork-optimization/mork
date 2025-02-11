package es.urjc.etsii.grafo.improve.ls;

import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    protected final Objective<M, S, I> objective;


    /**
     * Create a new local search method using the given neighborhood
     *
     * @param neighborhood neighborhood to use
     * @param objective    MAXIMIZE to maximize scores returned by the given move, MINIMIZE for minimizing
     */
    protected LocalSearch(Objective<M, S, I> objective, Neighborhood<M, S, I> neighborhood) {
        super(objective);
        this.objective = objective;
        this.neighborhood = neighborhood;
    }

    /**
     * Create a new local search method using the given neighborhood.
     * Uses the method Move::getValue as the guiding function, with fMaximize = maximize.
     *
     * @param neighborhood neighborhood to use
     */
    protected LocalSearch(Neighborhood<M, S, I> neighborhood) {
        this(Context.getMainObjective(), neighborhood);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Improves a model.Solution
     * Iterates until we run out of time, or we cannot improve the current solution any further
     */
    @Override
    public S improve(S solution) {
        int rounds = 1;
        boolean improved = true;
        while (!TimeControl.isTimeUp() && improved) {
            improved = iteration(rounds, solution);
            rounds++;
            if (rounds == WARN_LIMIT) {
                log.warn("Localsearch method {} may be stuck in an infinite loop (warn at {})", this.getClass().getSimpleName(), WARN_LIMIT);
            }
        }
        log.debug("Improvement {} ended after {} iterations.", this.getClass().getSimpleName(), rounds);
        return solution;
    }

    /**
     * This procedure check if there are valid moves to neighbors solutions.
     * In that case, the move is executed. Otherwise, the procedure ends.
     *
     * @return true if the solution has improved, false otherwise
     */
    public boolean iteration(int rounds, S solution) {
        // Get next move to execute
        var move = getMove(solution);
        if (move == null) {
            log.trace("Ending LS, no valid moves found in neighborhood");
            return false; // There are no valid moves in the neighborhood, end local search
        }
        if(log.isTraceEnabled()){
            log.trace("Step {}, current {}, executing: {}", rounds, this.objective.evalSol(solution), move);
        }
        // Execute move, save metric if improved, and ask for another iteration
        move.execute(solution);
        Metrics.addCurrentObjectives(solution);

        return true;
    }


    /**
     * Get next move to execute, different strategies are possible
     *
     * @param solution Solution
     * @return Proposed move, null if there are no candidate moves in the neighborhood
     */
    public abstract M getMove(S solution);

    protected boolean improves(M move) {
        double score = this.objective.evalMove(move);
        return this.objective.improves(score);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                .replace("LocalSearch", "LS")
                .replace("BestImprovement", "BI")
                .replace("FirstImprovement", "FI")
                + "{" +
                "neigh=" + neighborhood +
                '}';
    }
}
