package es.urjc.etsii.grafo.BMSSC.improve;

import es.urjc.etsii.grafo.BMSSC.model.BMSSCInstance;
import es.urjc.etsii.grafo.BMSSC.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.BMSSC.model.sol.ReassignMove;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.shake.Shake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static es.urjc.etsii.grafo.util.DoubleComparator.isLess;
import static es.urjc.etsii.grafo.util.DoubleComparator.isNegative;

public class StrategicOscillation extends Shake<BMSSCSolution, BMSSCInstance> {

    private static final Logger log = LoggerFactory.getLogger(StrategicOscillation.class);
    private final float increment;

    @AutoconfigConstructor
    public StrategicOscillation(
            @RealParam(min = 0, max = 1) double increment
    ) {
        // Increment size per K shake strength
        this.increment = (float) increment;
    }

    @Override
    public BMSSCSolution shake(BMSSCSolution solution, int k) {
        var ins = solution.getInstance();
        solution.relaxClusterSizeConstraint(this.increment * k);
        if(log.isDebugEnabled()){
            log.debug("Cluster sizes before: " + Arrays.toString(ins.getClusterSizes()));
            log.debug("Relaxed sizes before: " + Arrays.toString(solution.getClusterSizes()));
        }

        int c = 0;
        while(tryReassign(solution)){
            c++;
        }

        if(log.isDebugEnabled()){
            log.debug("Reassigned {} elements, Cluster sizes after: ", c);
            for (int i = 0; i < ins.k; i++) {
                log.debug(solution.getCluster(i).size() + " ");
            }
        }

        fixClusters(solution);
        solution.restoreClusterSizeConstraint();
        solution.notifyUpdate();
        return solution;
    }

    public void fixClusters(BMSSCSolution s) {
        var ins = s.getInstance();
        for (int i = 0; i < ins.k; i++) {
            while(s.getCluster(i).size() > ins.getClusterSize(i)){
                movePointAnywhere(i, s);
            }
        }

        assert s.feasibleClusterSizes(): "Invalid cluster sizes, failed to make solution feasible after repair. This should never happen.";
    }

    public void movePointAnywhere(int overloadedCluster, BMSSCSolution solution) {
        var ins = solution.getInstance();

        ReassignMove bestMove = null;
        for (int point: solution.getCluster(overloadedCluster)){
            for (int j = 0; j < ins.k; j++) {
                if(j == overloadedCluster || solution.getCluster(j).size() >= ins.getClusterSize(j)) {
                    continue;
                }
                var move = new ReassignMove(solution, point, j);
                if(bestMove == null || isLess(move.getValue(), bestMove.getValue())) {
                    bestMove = move;
                }
            }
        }
        assert bestMove != null: "Could not find a valid reassign move to reduce overloaded cluster %s, this should never happen".formatted(overloadedCluster);
        bestMove.execute(solution);
    }

    private boolean tryReassign(BMSSCSolution solution){
        var ins = solution.getInstance();
        ReassignMove bestMove = null;
        for (int point = 0; point < ins.n; point++) {
            for (int cluster = 0; cluster < ins.k; cluster++) {
                if(solution.clusterOf(point) == cluster || solution.getCluster(cluster).size() >= solution.getClusterSize(cluster)) { // TODO, swap cluster and point fors for performance
                    continue;
                }
                var move = new ReassignMove(solution, point, cluster);
                if(bestMove == null || isLess(move.getValue(), bestMove.getValue())){
                    bestMove = move;
                }
            }
        }
        if (bestMove != null && isNegative(bestMove.getValue())) {
            //System.out.println(bestMove);
            bestMove.execute(solution);
            return true;
        } else {
            return false;
        }
    }
}
