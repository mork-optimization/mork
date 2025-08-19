package es.urjc.etsii.grafo.VRPOD.model.solution;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;

import java.util.ArrayList;
import java.util.List;

public class OptNeigh extends VRPODNeigh {

    @AutoconfigConstructor
    public OptNeigh() {}

    @Override
    public List<BaseMove> listMoves(VRPODSolution solution) {
        var ins = solution.getInstance();
        int nClients = ins.getNumberOfClients();
        List<BaseMove> moves = new ArrayList<>(nClients * nClients);

        for (int driver = 1; driver < solution.normalDrivers.size(); driver++) {
            VRPODSolution.Route r = solution.normalDrivers.get(driver);
            assert r != null;
            // Each pair of positions is a possible movement
            for (int i = 0; i < r.nodes.size()-1; i++) {
                int leftLimit = r.nodes.get(i);
                int prev = i == 0? 0 : r.nodes.get(i - 1);
                for (int j = i+1; j < r.nodes.size(); j++) {
                    int rightLimit = r.nodes.get(j);
                    int next = j == r.nodes.size() - 1 ? 0 : r.nodes.get(j + 1);
                    moves.add(new OptMove(
                            solution,
                            driver,
                            i,
                            j,
                            -ins.getDistance(prev, leftLimit) + ins.getDistance(prev, rightLimit),
                            -ins.getDistance(rightLimit, next) + ins.getDistance(leftLimit, next)
                    ));
                }
            }
        }
        return  moves;
    }

}
