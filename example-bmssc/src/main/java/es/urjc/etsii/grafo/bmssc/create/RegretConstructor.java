package es.urjc.etsii.grafo.bmssc.create;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.AssignMove;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.bmssc.util.BMSSCNewUtil;
import es.urjc.etsii.grafo.bmssc.util.BMSSCNewUtil.SeedStrategy;
import es.urjc.etsii.grafo.bmssc.util.BMSSCUtil;
import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.ArrayList;

/** Regret-2 insertion: prioritize points whose next-best cluster is substantially more expensive. */
public class RegretConstructor extends Reconstructive<BMSSCSolution, BMSSCInstance> {
    private final double alpha;

    @AutoconfigConstructor
    public RegretConstructor(@RealParam(min = 0, max = 1) double alpha) {
        BMSSCNewUtil.validateUnitInterval(alpha);
        this.alpha = alpha;
    }

    @Override
    public BMSSCSolution construct(BMSSCSolution solution) {
        BMSSCUtil.withPartialSolution(() -> {
            BMSSCNewUtil.seed(solution, SeedStrategy.FARTHEST_FIRST);
            return solution;
        });
        return reconstruct(solution);
    }

    @Override
    public BMSSCSolution reconstruct(BMSSCSolution solution) {
        BMSSCUtil.withPartialSolution(() -> {
            assignMissing(solution);
            return solution;
        });
        if (!solution.feasibleClusterSizes()) throw new IllegalStateException("Reconstruction did not fill cluster quotas");
        assert Context.validate(solution);
        Metrics.addCurrentObjectives(solution);
        return solution;
    }

    private void assignMissing(BMSSCSolution solution) {
        var random = RandomManager.getRandom();
        while (!solution.getNotAssignedPoints().isEmpty()) {
            int available = 0;
            for (int c = 0; c < solution.getInstance().k; c++) {
                if (!solution.isFullCluster(c)) available++;
            }
            if (available < 2 || TimeControl.isTimeUp()) {
                BMSSCNewUtil.fillQuotas(solution, false);
                return;
            }
            var candidates = new ArrayList<AssignMove>();
            double[] regrets = new double[solution.getNotAssignedPoints().size()];
            double min = Double.POSITIVE_INFINITY, max = Double.NEGATIVE_INFINITY;
            for (int point : solution.getNotAssignedPoints()) {
                AssignMove best = null;
                double second = Double.POSITIVE_INFINITY;
                int ties = 0;
                for (int c = 0; c < solution.getInstance().k; c++) {
                    if (TimeControl.isTimeUp()) {
                        BMSSCNewUtil.fillQuotas(solution, false);
                        return;
                    }
                    if (solution.isFullCluster(c)) continue;
                    var move = new AssignMove(solution, point, c);
                    double delta = move.getCostDelta();
                    if (best == null || delta < best.getCostDelta()) {
                        if (best != null) second = best.getCostDelta();
                        best = move;
                        ties = 1;
                    } else {
                        second = Math.min(second, delta);
                        if (delta == best.getCostDelta() && random.nextInt(++ties) == 0) best = move;
                    }
                }
                double regret = second - best.getCostDelta();
                regrets[candidates.size()] = regret;
                candidates.add(best);
                min = Math.min(min, regret);
                max = Math.max(max, regret);
            }
            double threshold = max - alpha * (max - min);
            int chosen = -1, eligible = 0;
            for (int i = 0; i < candidates.size(); i++) {
                if (!DoubleComparator.isLess(regrets[i], threshold) && random.nextInt(++eligible) == 0) chosen = i;
            }
            candidates.get(chosen).execute(solution);
        }
    }

    @Override
    public String toString() { return "RegretConstructor{alpha=" + alpha + "}"; }
}
