package es.urjc.etsii.grafo.VRPOD.model.solution;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

import java.util.ArrayList;
import java.util.List;

public class SwapNeigh extends VRPODNeigh {

    @AutoconfigConstructor
    public SwapNeigh() {}

    @Override
    public List<BaseMove> listMoves(VRPODSolution solution) {
        var ins = solution.getInstance();
        int nClients = ins.getNumberOfClients();
        List<BaseMove> moves = new ArrayList<>(nClients * nClients);

        for (int driver1 = 1; driver1 < solution.normalDrivers.size() -1; driver1++) {
            var r1 = solution.normalDrivers.get(driver1);

            // Each pair of positions is a possible movement
            for (int i = 0; i < r1.nodes.size(); i++) {
                int customer1 = r1.nodes.get(i);
                int size1 = ins.getPacketSize(customer1);

                for (int driver2 = driver1+1; driver2 < solution.normalDrivers.size(); driver2++) {
                    var r2 = solution.normalDrivers.get(driver2);

                    for (int j = 0; j < r2.nodes.size(); j++) {
                        int customer2 = r2.nodes.get(j);
                        int size2 = ins.getPacketSize(customer2);
                        if(r1.capacityLeft < size2 - size1 || r2.capacityLeft < size1 - size2) continue;

                        moves.add(new SwapMove(
                                solution,
                                driver1,
                                i,
                                solution._getReplaceCost(r1, i, customer2),
                                driver2,
                                j,
                                solution._getReplaceCost(r2, j, customer1)
                        ));
                    }
                }

                // Same route moves, OPT will do distance 1, less likely to have bugs?
                for (int j = i+2; j < r1.nodes.size(); j++) {
                    int customer2 = r1.nodes.get(j);
                    moves.add(new SwapMove(
                            solution,
                            driver1,
                            i,
                            solution._getReplaceCost(r1, i, customer2),
                            driver1,
                            j,
                            solution._getReplaceCost(r1, j, customer1)
                    ));
                }
            }
        }
        return moves;
    }
}
