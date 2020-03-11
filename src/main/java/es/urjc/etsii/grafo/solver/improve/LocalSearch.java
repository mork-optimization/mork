package es.urjc.etsii.grafo.solver.improve;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Neighborhood;
import es.urjc.etsii.grafo.solution.Solution;

public abstract class LocalSearch<S extends Solution<I>,I extends Instance> implements Improver<S,I> {
    protected final Neighborhood<S,I>[] providers;
    protected String lsType;

    public LocalSearch(String lsType, Neighborhood<S,I>... ps) {
        this.lsType = lsType;
        this.providers = ps;
    }

    public boolean iteration(S s) {

        // Buscar el move a ejecutar
        var move = getMove(s);

        if(move == null || !move.improves()) {
            return false; // No existen movimientos válidos, finalizar
        }

        // Ejecutamos el move y pedimos otra iteracion
        move.execute();
        return true;
    }

    /**
     * Get move to execute, different strategies are possible
     * @param s Solution
     * @return Proposed move
     */
    protected abstract Move<S,I> getMove(S s);
}
