package es.urjc.etsii.grafo.VRPOD.model.solution;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

import java.util.ArrayList;
import java.util.List;

public class RouteToODNeigh extends VRPODNeigh {

    @AutoconfigConstructor
    public RouteToODNeigh() {}


    @Override
    public List<BaseMove> listMoves(VRPODSolution solution) {
        var ins = solution.getInstance();
        int nClients = ins.getNumberOfClients();
        List<BaseMove> movements = new ArrayList<>(nClients * nClients);
        for (int i = 1; i < solution.normalDrivers.size(); i++) {
            assert solution.normalDrivers.get(i) != null;
            VRPODSolution.Route r = solution.normalDrivers.get(i);
            for (int j = 0; j < r.nodes.size(); j++) {
                int customerId = r.nodes.get(j);
                for(int k: solution.getAvailableODDriversFor(customerId)){
                    double costOfOd = solution._getAssignCostOD(k, customerId);
                    double costOfDriver = solution.getRemoveCost(i, j, customerId);
                    movements.add(new RouteToODMove(solution, k, customerId, i, j, costOfOd, costOfDriver));
                }
            }
        }
        return movements;
    }
}
