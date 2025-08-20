package es.urjc.etsii.grafo.bmssc.improve;


import es.urjc.etsii.grafo.bmssc.Main;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.bmssc.model.sol.SwapMove;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.util.TimeControl;

import static es.urjc.etsii.grafo.util.DoubleComparator.isLess;
import static es.urjc.etsii.grafo.util.DoubleComparator.isNegative;

public class BestImpLS extends Improver<BMSSCSolution, BMSSCInstance> {

    @AutoconfigConstructor
    public BestImpLS() {
        super(Main.OBJ);
    }

    public boolean iteration(BMSSCSolution solution) {

        var ins = solution.getInstance();
        SwapMove bestMove = null;
        for (int i = 0; i < ins.n - 1; i++) {
            for (int j = i + 1; j < ins.n; j++) {
                if (solution.clusterOf(i) == solution.clusterOf(j))
                    continue;
                var swap = new SwapMove(solution, i, j);
                if(bestMove == null || isLess(swap.getValue(), bestMove.getValue())){
                    bestMove = swap;
                }
            }
        }

        if (bestMove != null && isNegative(bestMove.getValue())){
            bestMove.execute(solution);
            Metrics.addCurrentObjectives(solution);
        }
        return bestMove != null;
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
