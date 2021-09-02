package es.urjc.etsii.grafo.solver.improve;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.MoveComparator;
import es.urjc.etsii.grafo.solution.Neighborhood;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.Arrays;

public abstract class LocalSearch<M extends Move<S, I>, S extends Solution<I>, I extends Instance> extends IteratedImprover<S, I> {
    protected final Neighborhood<M, S, I>[] providers;
    protected final MoveComparator<M, S, I> comparator;
    protected final String lsName;

    @SafeVarargs
    public LocalSearch(MoveComparator<M, S, I> comparator, String lsName, Neighborhood<M, S, I>... ps) {
        this.comparator = comparator;
        this.providers = ps;
        this.lsName = lsName.strip();
    }

    @SafeVarargs
    public LocalSearch(MoveComparator<M, S, I> comparator, Neighborhood<M, S, I>... ps) {
        this(comparator, "", ps);
    }

    /**
     * Build a new local search
     * @param lsName Local Search name. If present, toString works as name{}. If not, Classname{neigh=[neigborhoods],comp=comparator}
     * @param maximizing true if a movement with a bigger score is better
     * @param ps         neighborhood that generates the movements
     */
    public LocalSearch(boolean maximizing, String lsName, Neighborhood<M, S, I>... ps) {
        this(new DefaultMoveComparator<>(maximizing), lsName, ps);
    }

    /**
     * Build a new local search
     *
     * @param maximizing true if a movement with a bigger score is better
     * @param ps         neighborhood that generates the movements
     */
    public LocalSearch(boolean maximizing, Neighborhood<M, S, I>... ps) {
        this(new DefaultMoveComparator<>(maximizing), "", ps);
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

    @Override
    public String toString() {
        if(this.lsName.isEmpty()){
            return this.getClass().getSimpleName()+"{" +
                    "neig=" + Arrays.toString(providers) +
                    ", comp=" + comparator +
                    '}';
        } else {
            return this.lsName;
        }
    }

    /**
     * Get move to execute, different strategies are possible
     *
     * @param s Solution
     * @return Proposed move
     */
    protected abstract M getMove(S s);

}
