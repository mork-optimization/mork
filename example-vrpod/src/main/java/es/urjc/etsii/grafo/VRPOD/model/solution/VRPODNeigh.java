package es.urjc.etsii.grafo.VRPOD.model.solution;

import es.urjc.etsii.grafo.VRPOD.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

import java.util.List;

public abstract class VRPODNeigh extends Neighborhood<BaseMove, VRPODSolution, VRPODInstance> {

    @Override
    public ExploreResult<BaseMove, VRPODSolution, VRPODInstance> explore(VRPODSolution solution) {
        return ExploreResult.fromList(listMoves(solution));
    }

    public abstract List<BaseMove> listMoves(VRPODSolution solution);
}
