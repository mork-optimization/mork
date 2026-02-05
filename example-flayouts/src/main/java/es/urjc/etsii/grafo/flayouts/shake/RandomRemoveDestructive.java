package es.urjc.etsii.grafo.flayouts.shake;

import es.urjc.etsii.grafo.flayouts.model.FLPAddNeigh;
import es.urjc.etsii.grafo.flayouts.model.FLPInstance;
import es.urjc.etsii.grafo.flayouts.model.FLPSolution;
import es.urjc.etsii.grafo.flayouts.model.FLPRemoveNeigh;
import es.urjc.etsii.grafo.shake.Destructive;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static es.urjc.etsii.grafo.util.CollectionUtil.shuffle;
import static java.lang.Math.max;
import static java.lang.Math.round;

/**
 * Partially destroys a DRFP solution by removing part of its facilities.
 */
public class RandomRemoveDestructive extends Destructive<FLPSolution, FLPInstance> {

    private final double ratio;
    private final boolean bulkStrategy = true;

    public RandomRemoveDestructive(double ratio) {
        this.ratio = ratio;
    }

    @Override
    public FLPSolution destroy(FLPSolution solution, int k) {
        var instance = solution.getInstance();
        var facilities = solution.getRows();

        // How many facilities should be removed from the solution, remove at least one
        long n = max(1, round(instance.nFacilities() * ratio));

        var facilitiesToRemove = new HashSet<Integer>();
        var allIds = new ArrayList<Integer>();

        // Get all assigned facilities IDs
        for (int i = 0; i < solution.nRows(); i++) {
            for (int j = 0; j < solution.rowSize(i); j++) {
                allIds.add(facilities[i][j]);
            }
        }

        // choose N random facilities without repetition
        shuffle(allIds);
        for (int i = 0; i < n; i++) {
            facilitiesToRemove.add(allIds.get(i));
        }

        if(bulkStrategy){
            return bulkRemove(solution, facilitiesToRemove);
        } else {
            return moveRemove(solution, facilitiesToRemove);
        }
    }

    public FLPSolution bulkRemove(FLPSolution solution, Set<Integer> facilitiesToRemove){
        var facilities = solution.getRows();
        var newSolution = solution.cloneSolution(); // TODO replace with new Solution and add only those not blocked?
        newSolution.deassignAll();
        for (int row = 0; row < solution.nRows(); row++) {
            for (int pos = 0; pos < solution.rowSize(row); pos++) {
                var facility = facilities[row][pos];
                if(!facilitiesToRemove.contains(facility)){
                    var add = new FLPAddNeigh.AddMove(newSolution, row, pos, facility);
                    add.execute(newSolution);
                }
            }
        }
        return newSolution;
    }

    public FLPSolution moveRemove(FLPSolution solution, Set<Integer> facilitiesToRemove){
        var coords = solution.locateAll();
        var newSolution = solution.cloneSolution();
        for (int facility : facilitiesToRemove) {
            var coordinate = coords[facility];
            var remove = new FLPRemoveNeigh.RemoveMove(solution, coordinate.row(), coordinate.pos(), facility);
            remove.execute(newSolution);
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
