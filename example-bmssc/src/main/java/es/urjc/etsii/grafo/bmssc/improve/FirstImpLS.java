package es.urjc.etsii.grafo.bmssc.improve;

import es.urjc.etsii.grafo.bmssc.Main;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.bmssc.model.sol.SwapMove;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.util.TimeControl;

import static es.urjc.etsii.grafo.util.DoubleComparator.isNegative;

public class FirstImpLS extends Improver<BMSSCSolution, BMSSCInstance> {

    @AutoconfigConstructor
    public FirstImpLS() {
        super(Main.OBJ);
    }

    public boolean iteration(BMSSCSolution solution) {

        var instance = solution.getInstance();
        for (int i = 0; i < instance.n - 1; i++) {
            for (int j = i + 1; j < instance.n; j++) {
                if (solution.clusterOf(i) == solution.clusterOf(j)) continue;
                var swap = new SwapMove(solution, i, j);
                if(isNegative(swap.getValue())){
                    swap.execute(solution);
                    Metrics.addCurrentObjectives(solution);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public BMSSCSolution improve(BMSSCSolution solution) {
        int rounds = 0;
        while (!TimeControl.isTimeUp() && iteration(solution)){
            rounds++;
        }
        return solution;
    }
}
