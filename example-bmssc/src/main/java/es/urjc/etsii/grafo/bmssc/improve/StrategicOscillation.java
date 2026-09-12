package es.urjc.etsii.grafo.bmssc.improve;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.bmssc.model.sol.ReassignMove;
import es.urjc.etsii.grafo.bmssc.model.sol.ReassignNeighborhood;
import es.urjc.etsii.grafo.bmssc.util.BMSSCUtil;
import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.shake.Shake;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeControl;

import static es.urjc.etsii.grafo.util.DoubleComparator.isLess;

public class StrategicOscillation extends Shake<BMSSCSolution, BMSSCInstance> {
    private final double increment;
    private final ReassignNeighborhood neighborhood = new ReassignNeighborhood();
    private final LocalSearchBestImprovement<ReassignMove, BMSSCSolution, BMSSCInstance> descent =
            new LocalSearchBestImprovement<>(neighborhood);

    @AutoconfigConstructor
    public StrategicOscillation(@RealParam(min = 0, max = 1) double increment) {
        if (!Double.isFinite(increment) || increment < 0 || increment > 1) {
            throw new IllegalArgumentException("Invalid capacity increment: " + increment);
        }
        this.increment = increment;
    }

    @Override
    public BMSSCSolution shake(BMSSCSolution solution, int k) {
        if (k < 0) throw new IllegalArgumentException("Negative shake strength");
        if (!solution.feasibleClusterSizes()) throw new IllegalArgumentException("Shake requires a feasible solution");
        if (TimeControl.isTimeUp() || k == 0 || increment == 0) return solution;
        var working = solution.cloneSolution();
        boolean repaired = BMSSCUtil.withPartialSolution(() -> {
            try {
                working.relaxClusterSizeConstraint(increment * k);
                descent.improve(working);
                return repair(working);
            } finally {
                working.restoreClusterSizeConstraint();
            }
        });
        if (!repaired || TimeControl.isTimeUp()) return solution;
        assert Context.validate(working);
        Metrics.addCurrentObjectives(working);
        return working;
    }

    private boolean repair(BMSSCSolution solution) {
        var instance = solution.getInstance();
        for (int source = 0; source < instance.k; source++) {
            while (solution.getClusterSize(source) > instance.getClusterSize(source)) {
                if (TimeControl.isTimeUp()) return false;
                ReassignMove best = null;
                var moves = neighborhood.exploreRepair(solution, source).moves().iterator();
                while (moves.hasNext()) {
                    var move = moves.next();
                    if (best == null || isLess(move.getCostDelta(), best.getCostDelta())) best = move;
                }
                if (TimeControl.isTimeUp()) return false;
                if (best == null) throw new IllegalStateException("Cannot repair overloaded cluster " + source);
                // Repair must accept a worsening move when it is the least costly feasible option.
                best.execute(solution);
            }
        }
        return solution.feasibleClusterSizes();
    }

    @Override
    public String toString() { return "StrategicOscillation{increment=" + increment + "}"; }
}
