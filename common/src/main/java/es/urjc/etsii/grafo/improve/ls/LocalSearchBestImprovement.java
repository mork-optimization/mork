package es.urjc.etsii.grafo.improve.ls;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.ListExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

/**
 * Local search procedures start from a given feasible solution and explore a determined neighborhood
 * in each iteration, replacing the current solution if a neighbor solution improves the objective
 * function of the current one. In this case, this local search procedure follows a Best Improvement strategy.
 *  The best improvement strategy is related to executing the best move of the given neighbors.
 *
 * @param <M> the type of move
 * @param <S> the type of problem solution
 * @param <I> the type of problem instances
 */
public class LocalSearchBestImprovement<M extends Move<S, I>, S extends Solution<S,I>, I extends Instance> extends LocalSearch<M, S, I> {

    /**
     * Create a new local search method using the given neighborhood.
     * Uses the method Move::getValue as the guiding function, with fMaximize = fmode.
     * @param neighborhood neighborhood to use
     */
    @AutoconfigConstructor
    public LocalSearchBestImprovement(
            Neighborhood<M, S, I> neighborhood
    ) {
        super(neighborhood);
    }

    /**
     * Create a new local search method using the given neighborhood
     * @param neighborhood neighborhood to use
     * @param objective objective function to optimize
     */
    public LocalSearchBestImprovement(Objective<M,S,I> objective, Neighborhood<M, S, I> neighborhood) {
        super(objective, neighborhood);
    }

    /**
     * {@inheritDoc}
     *
     * Get next move to execute.
     */
    @Override
    public M getMove(S solution) {
        var expRes = neighborhood.explore(solution);
        M bestMove;
        if(expRes instanceof ListExploreResult<M,S,I> list){
            bestMove = objective.bestMove(list.moveList());
        } else {
            var move = expRes.moves().reduce(objective::bestMove);
            bestMove = move.orElse(null);
        }
        // Check if best move actually improves, if not end
        return bestMove != null && improves(bestMove) ? bestMove : null;
    }
}
