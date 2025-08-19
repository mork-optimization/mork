package es.urjc.etsii.grafo.VRPOD.model.solution;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

import java.util.ArrayList;
import java.util.List;

public class ODToRouteNeigh extends VRPODNeigh {

    @AutoconfigConstructor
    public ODToRouteNeigh() {}

    @Override
    public List<BaseMove> listMoves(VRPODSolution solution) {
        var ins = solution.getInstance();
        int nClients = ins.getNumberOfClients();
        List<BaseMove> movements = new ArrayList<>(nClients * nClients);
        for (int i = 0; i < solution.ODattends.length; i++) {
            if(solution.ODattends[i] == VRPODSolution.NOT_ASSIGNED) continue;
            movements.addAll(solution.moveCostFromOdToNormalDriver(i));
        }
        return movements;
    }
}
