package es.urjc.etsii.grafo.VRPOD.model.solution;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

import java.util.ArrayList;
import java.util.List;

public class VRPODExtendedNeighborhood extends VRPODNeigh {

    @AutoconfigConstructor
    public VRPODExtendedNeighborhood() {}

    // Migrated from
    // public static final Supplier<Improver> EXPANDED_NEIGHBOURHOOD = () -> new GenericLocalSearch("EXPANDED_NEIGHBOURHOOD", Solution::getOdToRouteMovements, Solution::getRouteToODMovements, Solution::getInsertRouteMovements, Solution::getSwapRouteMovements, Solution::get2OptMovements);
    private final VRPODNeigh[] neighborhoods = new VRPODNeigh[]{
            new ODToRouteNeigh(),
            new RouteToODNeigh(),
            new InsertNeigh(),
            new SwapNeigh(),
            new OptNeigh()
    };

    @Override
    public List<BaseMove> listMoves(VRPODSolution solution) {
        var list = new ArrayList<BaseMove>();
        for(var neigh: neighborhoods){
            list.addAll(neigh.listMoves(solution));
        }
        return list;
    }
}
