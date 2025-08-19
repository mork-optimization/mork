package es.urjc.etsii.grafo.VRPOD.destructives;

import es.urjc.etsii.grafo.VRPOD.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.VRPOD.model.solution.*;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.shake.Shake;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.ArrayList;
import java.util.Arrays;

public class RandomMovement extends Shake<VRPODSolution, VRPODInstance> {

    private final VRPODNeigh[] neighborhoods = new VRPODNeigh[]{
            new ODToRouteNeigh(),
            new RouteToODNeigh(),
            new InsertNeigh(),
            new SwapNeigh(),
            new OptNeigh()
    };

    private final int multiplier;

    @AutoconfigConstructor
    public RandomMovement(
            @IntegerParam(min = 1, max = 50) int multiplier
    ) {
        this.multiplier = multiplier;
    }

    @Override
    public VRPODSolution shake(VRPODSolution solution, int k) {
        int nMoves = k * this.multiplier;
        for (int i = 0; i < nMoves && !TimeControl.isTimeUp(); i++) {
            iteration(solution);
        }
        return solution;
    }

    /**
     * @param s current solution
     */
    public void iteration(VRPODSolution s) {
        if (!s.getUnassignedClients().isEmpty()) {
            throw new IllegalStateException("Cannot start a destruction phase if there are unassigned clients");
        }

        var random = RandomManager.getRandom();
        boolean executed = false;

        do {
            var providersFiltered = new ArrayList<>(Arrays.asList(this.neighborhoods));
            int neighbourhood = random.nextInt(providersFiltered.size());
            var movements = providersFiltered.get(neighbourhood).listMoves(s);
            if (movements.isEmpty()) {
                // FAILED ITERATION, DELETE INVALID NEIGHBOURHOOD AND RETRY
                providersFiltered.remove(neighbourhood);
                continue;
            }
            var chosenMovement = movements.get(random.nextInt(movements.size()));
            chosenMovement.execute(s);
            executed = true;
        } while (!executed);

    }

    @Override
    public String toString() {
        return "RandomMovement{" +
                "neighs=" + Arrays.asList(this.neighborhoods) +
                '}';
    }
}
