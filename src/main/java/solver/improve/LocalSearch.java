package solver.improve;

import solution.Move;
import solution.Neighborhood;
import solution.Solution;

public abstract class LocalSearch implements Improver {
    protected final Neighborhood[] providers;
    protected String lsType;

    public LocalSearch(String lsType, Neighborhood... ps) {
        this.lsType = lsType;
        this.providers = ps;
    }

    public boolean iteration(Solution s) {

        // Buscar el movement a ejecutar
        var movement = getMovement(s);

        if(movement == null || !movement.improves()) {
            return false; // No existen movimientos válidos, finalizar
        }

        // Ejecutamos el movement y pedimos otra iteracion
        movement.execute();
        return true;
    }

    /**
     * Get movement to execute, different strategies are possible
     * @param s Solution
     * @return Proposed movement
     */
    protected abstract Move getMovement(Solution s);
}
