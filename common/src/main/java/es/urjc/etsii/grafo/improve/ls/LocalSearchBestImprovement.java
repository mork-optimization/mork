package es.urjc.etsii.grafo.improve.ls;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.ProvidedParam;
import es.urjc.etsii.grafo.annotations.ProvidedParamType;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.neighborhood.ListExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.util.CollectionUtil;

import java.util.function.ToDoubleFunction;

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
     * Uses the method Move::getValue as the guiding function, with fMaximize = maximize.
     * @param neighborhood neighborhood to use
     * @param maximize true if the problem objective function is maximizing, false otherwise
     */
    @AutoconfigConstructor
    public LocalSearchBestImprovement(
            @ProvidedParam(type = ProvidedParamType.MAXIMIZE) boolean maximize,
            Neighborhood<M, S, I> neighborhood
    ) {
        super(maximize, neighborhood);
    }

    /**
     * Create a new local search method using the given neighborhood
     * @param neighborhood neighborhood to use
     * @param ofMaximize true if the problem objective function is maximizing, false otherwise
     * @param fMaximize true if we should maximize the values returned by function f, false otherwise
     * @param f function used to get a double value from a move
     */
    protected LocalSearchBestImprovement(boolean ofMaximize, Neighborhood<M, S, I> neighborhood, boolean fMaximize, ToDoubleFunction<M> f) {
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
            return CollectionUtil.getBest(list.moveList(), f, fIsBetter);
        } else {
            var move = expRes.moves().reduce((m1, m2) -> {
                double score1 = this.f.applyAsDouble(m1);
                double score2 = this.f.applyAsDouble(m2);
                return fIsBetter.test(score2, score1) ? m2 : m1;
            });
            return move.orElse(null);
        }
    }
}
