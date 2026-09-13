package es.urjc.etsii.grafo.bmssc.shake;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.bmssc.util.BMSSCNewUtil;
import es.urjc.etsii.grafo.shake.Destructive;
import es.urjc.etsii.grafo.util.TimeControl;

import static es.urjc.etsii.grafo.bmssc.util.BMSSCUtil.contribution;

/** Recompute removal gains after every selection without changing the original solution. */
public class WorstRemoval extends Destructive<BMSSCSolution, BMSSCInstance> {
    private final double fraction;

    @AutoconfigConstructor
    public WorstRemoval(@RealParam(min = 0.01, max = 0.5) double fraction) {
        BMSSCNewUtil.validateUnitInterval(fraction);
        this.fraction = fraction;
    }

    @Override
    public BMSSCSolution destroy(BMSSCSolution solution, int k) {
        int count = BMSSCNewUtil.removalCount(solution, fraction, k);
        if (count == 0 || TimeControl.isTimeUp()) return solution;
        var instance = solution.getInstance();
        int[] sizes = new int[instance.k];
        double[] pairSums = new double[instance.k];
        double[] ownDistances = new double[instance.n];
        boolean[] removed = new boolean[instance.n];
        for (int c = 0; c < instance.k; c++) {
            sizes[c] = solution.getClusterSize(c);
            pairSums[c] = solution.getPairSum(c);
        }
        for (int p = 0; p < instance.n; p++) ownDistances[p] = solution.getPointClusterDistance(p, solution.clusterOf(p));
        for (int i = 0; i < count; i++) {
            int chosen = -1;
            double best = Double.POSITIVE_INFINITY;
            for (int p = 0; p < instance.n; p++) {
                if (TimeControl.isTimeUp()) return solution;
                if (removed[p]) continue;
                int c = solution.clusterOf(p);
                double delta = contribution(pairSums[c] - ownDistances[p], sizes[c] - 1) - contribution(pairSums[c], sizes[c]);
                if (delta < best) {
                    best = delta;
                    chosen = p;
                }
            }
            removed[chosen] = true;
            int cluster = solution.clusterOf(chosen);
            pairSums[cluster] -= ownDistances[chosen];
            if (--sizes[cluster] < 2) pairSums[cluster] = 0;
            for (int p = 0; p < instance.n; p++) {
                if (!removed[p] && solution.clusterOf(p) == cluster) ownDistances[p] -= instance.distance(p, chosen);
            }
        }
        if (TimeControl.isTimeUp()) return solution;
        return BMSSCNewUtil.retain(solution, removed);
    }

    @Override
    public String toString() { return "WorstRemoval{fraction=" + fraction + "}"; }
}
