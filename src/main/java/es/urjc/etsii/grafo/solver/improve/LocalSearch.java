package es.urjc.etsii.grafo.solver.improve;

import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Neighborhood;
import es.urjc.etsii.grafo.solution.Solution;

public abstract class LocalSearch implements Improver {
    protected final Neighborhood[] providers;
    protected String lsType;

    public LocalSearch(String lsType, Neighborhood... ps) {
        this.lsType = lsType;
        this.providers = ps;
    }

    public boolean iteration(Solution s) {

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
    protected abstract Move getMove(Solution s);
}
