package es.urjc.etsii.grafo.improve.ls;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.ProvidedParam;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.ListExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

import java.util.function.ToDoubleFunction;

/**
 * Local search procedures start from a given feasible solution and explore a determined neighborhood
 * in each iteration, replacing the current solution if a neighbor solution improves the objective
 * function of the current one. In this case, this local search procedure follows a First Improvement strategy.
 *  The first improvement strategy is related to executing the first move that improves the current solution
 *
 * @param <M> the type of move
 * @param <S> the type of problem solution
 * @param <I> the type of problem instances
 */
public class LocalSearchFirstImprovement<M extends Move<S, I>, S extends Solution<S,I>, I extends Instance> extends LocalSearch<M, S, I> {

    /**
     * Create a new local search method using the given neighborhood.
     * Uses the method Move::getValue as the guiding function, with fMaximize = fmode.
     * @param neighborhood neighborhood to use
     * @param fmode MAXIMIZE if the problem objective function is maximizing, MINIMIZE otherwise
     */
    @AutoconfigConstructor
    public LocalSearchFirstImprovement(
            @ProvidedParam FMode fmode,
            Neighborhood<M, S, I> neighborhood
    ) {
        super(fmode, neighborhood);
    }

    /**
     * Create a new local search method using the given neighborhood
     * @param neighborhood neighborhood to use
     * @param ofMaximize MAXIMIZE if the problem objective function is maximizing, MINIMIZE otherwise
     * @param fMaximize true if we should maximize the values returned by function f, false otherwise
     * @param f function used to get a double value from a move
     */
    protected LocalSearchFirstImprovement(FMode ofMaximize, Neighborhood<M, S, I> neighborhood, FMode fMaximize, ToDoubleFunction<M> f) {
        super(ofMaximize, neighborhood, fMaximize, f);
    }

    /**
     * {@inheritDoc}
     *
     * Get next move to execute.
     */
    @Override
    public M getMove(S solution) {
        var expRes = neighborhood.explore(solution);

        if(expRes instanceof ListExploreResult<M,S,I> list){
            for(var move: list.moveList()){
                if(improves(move)){
                    return move;
                }
            }
            return null;
        } else {
            var move = expRes.moves().filter(this::improves).findAny();
            return move.orElse(null);
        }
    }
}
