package es.urjc.etsii.grafo.solver.improve;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.MoveComparator;
import es.urjc.etsii.grafo.solution.Neighborhood;
import es.urjc.etsii.grafo.solution.Solution;

public abstract class LocalSearch<M extends Move<S, I>, S extends Solution<I>, I extends Instance> extends IteratedImprover<S, I> {
    protected final Neighborhood<M, S, I>[] providers;
    protected final MoveComparator<M, S, I> comparator;
    protected String lsType;

    @SafeVarargs
    public LocalSearch(MoveComparator<M, S, I> comparator, String lsType, Neighborhood<M, S, I>... ps) {
        this.comparator = comparator;
        this.lsType = lsType;
        this.providers = ps;
    }

    /**
     * Build a new local search
     *
     * @param maximizing true if a movement with a bigger score is better
     * @param lsType     name for the local search
     * @param ps         neighborhood that generates the movements
     */
    public LocalSearch(boolean maximizing, String lsType, Neighborhood<M, S, I>... ps) {
        this(new DefaultMoveComparator<>(maximizing), lsType, ps);
    }


    public boolean iteration(S s) {
        // Buscar el move a ejecutar
        var move = getMove(s);

        if (move == null || !move.improves()) {
            return false; // No existen movimientos válidos, finalizar
        }

        // Ejecutamos el move y pedimos otra iteracion
        move.execute();
        return true;
    }

    /**
     * Get move to execute, different strategies are possible
     *
     * @param s Solution
     * @return Proposed move
     */
    protected abstract M getMove(S s);
}
