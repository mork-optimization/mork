package es.urjc.etsii.grafo.flayouts.constructives;

import es.urjc.etsii.grafo.flayouts.model.*;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;

/** Small bounded insertion lists shared by the flow and regret constructors. */
public final class FLPConstructionUtil {
    private FLPConstructionUtil() {}

    public static FLPNewMove[] bestInsertions(FLPSolution solution, int facility, int count, boolean appendOnly) {
        FLPNewMove[] best = new FLPNewMove[count];
        for (int row = 0; row < solution.nRows(); row++) {
            int first = appendOnly ? solution.rowSize(row) : 0;
            for (int pos = first; pos <= solution.rowSize(row); pos++) {
                if (TimeControl.isTimeUp()) return best;
                var move = FLPNewUtil.add(solution, facility, row, pos, true);
                int index = 0;
                while (index < count && best[index] != null && (best[index].delta() < move.delta()
                        || (best[index].delta() == move.delta() && RandomManager.getRandom().nextBoolean()))) index++;
                if (index == count) continue;
                System.arraycopy(best, index, best, index + 1, count - index - 1);
                best[index] = move;
            }
        }
        return best;
    }
}
