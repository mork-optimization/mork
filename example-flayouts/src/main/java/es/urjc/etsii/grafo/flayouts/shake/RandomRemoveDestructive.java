package es.urjc.etsii.grafo.flayouts.shake;

import es.urjc.etsii.grafo.flayouts.model.FLPInstance;
import es.urjc.etsii.grafo.flayouts.model.FLPSolution;
import es.urjc.etsii.grafo.shake.Destructive;

import java.util.ArrayList;
import java.util.HashSet;

import static es.urjc.etsii.grafo.util.CollectionUtil.shuffle;
import static java.lang.Math.max;
import static java.lang.Math.round;

/**
 * Partially destroys a DRFP solution by removing part of its facilities.
 */
public class RandomRemoveDestructive extends Destructive<FLPSolution, FLPInstance> {

    private final double ratio;

    public RandomRemoveDestructive(double ratio) {
        this.ratio = ratio;
    }

    @Override
    public FLPSolution destroy(FLPSolution solution, int k) {
        var instance = solution.getInstance();
        var facilities = solution.getRows();

        // How many facilities should be removed from the solution, remove at least one
        long n = max(1, round(instance.getNRealFacilities() * ratio));

        var blockedFacilities = new HashSet<Integer>();
        var allIds = new ArrayList<Integer>();

        // Get all assigned facilities IDs
        for (int i = 0; i < solution.nRows(); i++) {
            for (int j = 0; j < solution.rowSize(i); j++) {
                allIds.add(facilities[i][j].facility.id);
            }
        }

        // Block N facilities
        shuffle(allIds);
        for (int i = 0; i < n; i++) {
            blockedFacilities.add(allIds.get(i));
        }

        // Rebuild solution without blocked facilities
        var newSolution = solution.cloneSolution();
        newSolution.deassignAll();
        for (int i = 0; i < solution.nRows(); i++) {
            for (int j = 0; j < solution.rowSize(i); j++) {
                var facility = facilities[i][j];
                if(!blockedFacilities.contains(facility)){
                    newSolution.insertLast(i, facility);
                }
            }
        }

        return newSolution;
    }

    @Override
    public String toString() {
        return "RandRemDest{" +
                "r=" + ratio +
                '}';
    }
}
