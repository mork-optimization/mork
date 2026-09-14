package es.urjc.etsii.grafo.tsptw.repair;

import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.aop.TimeStats;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.tsptw.model.TSPTWUtil;
import es.urjc.etsii.grafo.util.TimeControl;

/** In-place insertion repair, accepting reduced total lateness rather than reduced tour cost. */
@AlgorithmComponent
public abstract class TSPTWRepairPhase {
    private final boolean backward;
    private final boolean violated;

    protected TSPTWRepairPhase(boolean backward, boolean violated) {
        this.backward = backward;
        this.violated = violated;
    }

    @TimeStats
    public void repair(TSPTWSolution solution) {
        if (solution.constraint_violations() == 0 || TimeControl.isTimeUp()) return;
        int n = solution.getInstance().n();
        var positions = TSPTWUtil.shuffledPositions(solution, violated);
        while (!positions.isEmpty() && !TimeControl.isTimeUp()) {
            int position = positions.removeLast();
            assert solution.isLateAt(position) == violated;
            var candidate = solution.cloneSolution();
            boolean moved = false;
            int firstSwap = backward ? position - 1 : position;
            int step = backward ? -1 : 1;
            for (int d = firstSwap; d > 0 && d < n - 1 && !TimeControl.isTimeUp(); d += step) {
                if (candidate.infeasible_move(d, d + 1)) break;
                candidate.swap(d);
                if (candidate.infeasibility() < solution.infeasibility()) {
                    solution.copy_from(candidate);
                    solution.notifyUpdate();
                    solution.assert_solution();
                    moved = true;
                    if (solution.infeasibility() == 0) return;
                }
            }
            if (moved) positions = TSPTWUtil.shuffledPositions(solution, violated);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
