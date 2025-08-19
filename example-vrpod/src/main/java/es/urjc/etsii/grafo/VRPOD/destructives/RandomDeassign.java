package es.urjc.etsii.grafo.VRPOD.destructives;

import es.urjc.etsii.grafo.VRPOD.model.solution.VRPODSolution;
import es.urjc.etsii.grafo.VRPOD.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.shake.Destructive;
import es.urjc.etsii.grafo.util.CollectionUtil;

import java.util.ArrayList;

public class RandomDeassign extends Destructive<VRPODSolution, VRPODInstance> {

    @AutoconfigConstructor
    public RandomDeassign() {}

    public static final double K_STEP = 0.1;

    @Override
    public String toString() {
        return "RandomDeassign{}";
    }

    @Override
    public VRPODSolution destroy(VRPODSolution solution, int k) {
        int nClients = solution.ins.getNumberOfClients();
        int deassign = (int) (nClients * K_STEP * k);
        // In case K is big, deassign max is all clients
        deassign = Math.min(deassign, nClients);

        // Assume all clients are assigned, los assert estaban deshabilitados
        if(!solution.getUnassignedClients().isEmpty()){
            throw new IllegalStateException("Cannot execute destruction phase if there are unassigned clients");
        }

        var clients = new ArrayList<Integer>(nClients);
        for (int i = 1; i <= nClients; i++) {
            clients.add(i);
        }

        CollectionUtil.shuffle(clients);
        for (int i = 0; i < deassign; i++) {
            solution.deassign(clients.get(i));
        }
        return solution;
    }
}
