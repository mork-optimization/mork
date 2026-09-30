package es.urjc.etsii.grafo.flayouts.constructives;

import es.urjc.etsii.grafo.flayouts.model.*;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.List;

/** Insertion candidates and bounded best-insertion lists shared by FLP constructors. */
public final class FLPConstructionUtil {
    private FLPConstructionUtil() {}

    /** A construction must finish even if neighborhood exploration reaches the deadline. */
    public static List<FLPAddNeigh.AddMove> insertionCandidates(FLPSolution solution, FLPAddNeigh neighborhood) {
        if (solution.getNotAssignedFacilities().isEmpty()) return List.of();
        if (TimeControl.isTimeUp()) return List.of(FLPNewUtil.appendMissing(solution));
        var moves = neighborhood.exploreList(solution);
        return moves.isEmpty() ? List.of(FLPNewUtil.appendMissing(solution)) : moves;
    }

    public static FLPNewMove[] bestInsertions(FLPSolution solution, int facility, int count, boolean appendOnly) {
        FLPNewMove[] best = new FLPNewMove[count];
        for (int row = 0; row < solution.nRows(); row++) {
            int first = appendOnly ? solution.rowSize(row) : 0;
            for (int pos = first; pos <= solution.rowSize(row); pos++) {
                if (TimeControl.isTimeUp()) return best;
                var move = FLPNewUtil.add(solution, facility, row, pos);
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
