package es.urjc.etsii.grafo.flayouts.constructives;

import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.flayouts.model.FLPAddNeigh;
import es.urjc.etsii.grafo.flayouts.model.FLPInstance;
import es.urjc.etsii.grafo.flayouts.model.FLPSolution;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.ArrayList;

/**
 * Generate random solutions for validation purposes
 */
public class DRFPRandomConstructive extends Reconstructive<FLPSolution, FLPInstance> {

    @Override
    public FLPSolution construct(FLPSolution solution) {
        var facilities = new ArrayList<>(solution.getNotAssignedFacilities());
        if(facilities.isEmpty()){
            return solution;
        }

        var random = RandomManager.getRandom();
        CollectionUtil.shuffle(facilities);

        for (int f : facilities) {
            int row = random.nextInt(solution.nRows());
            int pos = solution.rowSize(row);
            var addMove = new FLPAddNeigh.AddMove(solution, row, pos, f);
            addMove.execute(solution);
        }

        return solution;
    }

    @Override
    public FLPSolution reconstruct(FLPSolution solution) {
        return construct(solution);
    }
}
