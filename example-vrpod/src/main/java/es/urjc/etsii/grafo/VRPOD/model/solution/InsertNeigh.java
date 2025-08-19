package es.urjc.etsii.grafo.VRPOD.model.solution;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

import java.util.ArrayList;
import java.util.List;

public class InsertNeigh extends VRPODNeigh {

    @AutoconfigConstructor
    public InsertNeigh() {}


    @Override
    public List<BaseMove> listMoves(VRPODSolution solution) {
        var ins = solution.getInstance();
        var nClients = ins.getNumberOfClients();
        List<BaseMove> moves = new ArrayList<>(nClients * nClients);

        // Generar movimientos entre rutas
        for (int driver1 = 1; driver1 < solution.normalDrivers.size() -1; driver1++) {
            assert solution.normalDrivers.get(driver1) != null;
            VRPODSolution.Route r1 = solution.normalDrivers.get(driver1);

            for (int position1 = 0; position1 < r1.nodes.size(); position1++) {
                int customer = r1.nodes.get(position1);
                for (int driver2 = driver1+1; driver2 < solution.normalDrivers.size(); driver2++) {
                    assert solution.normalDrivers.get(driver2) != null;
                    VRPODSolution.Route r2 = solution.normalDrivers.get(driver2);

                    if(!solution.canCarry(driver2, customer)) continue;

                    for (int position2 = 0; position2 < r2.nodes.size(); position2++) {
                        moves.add(new InsertMove(
                                solution,
                                customer,
                                driver1,
                                position1,
                                solution.getRemoveCost(driver1, position1, customer),
                                driver2,
                                position2,
                                solution.getInsertCost(driver2, position2, customer)
                        ));
                    }
                }
            }
        }

        // Generar movimientos dentro de la misma ruta
        for (int driver = 1; driver < solution.normalDrivers.size(); driver++) {
            VRPODSolution.Route r = solution.normalDrivers.get(driver);
            for (int dest = 0; dest < r.nodes.size() - 2; dest++) {
                // +1 porque incluimos el swap con distancia 1 como insert, al ser mas facil de aplicar esta formula que la del swap
                for (int origen = dest+1; origen < r.nodes.size(); origen++) {
                    var customer = r.nodes.get(origen);
                    moves.add(new InsertMove(
                            solution,
                            customer,
                            driver,
                            origen,
                            solution.getRemoveCost(driver, origen, customer),
                            driver,
                            dest,
                            solution._getInsertCost(r, dest, customer)));
                }
            }
        }
        return moves;
    }
}
