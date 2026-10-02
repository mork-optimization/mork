package es.urjc.etsii.grafo.mreflp.neighborhood;

import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;
import java.util.*;
import java.util.stream.StreamSupport;

/** Lazy enumeration and rejection-free uniform sampling within each feasible move family. */
public final class MREFLPNeighborhoodUtil {
    private MREFLPNeighborhoodUtil() {}

    public static ExploreResult<MREFLPMove, MREFLPSolution, MREFLPInstance> explore(
            MREFLPSolution solution, boolean relocations, boolean swaps) {
        Iterator<MREFLPMove> iterator = new Iterator<>() {
            private boolean relocationPhase = relocations;
            private int first, second;
            private MREFLPMove next;
            private final long version = solution.getVersion();

            private void advance() {
                if (solution.getVersion() != version) throw new IllegalStateException("Solution changed during enumeration");
                while (next == null && !TimeControl.isTimeUp()) {
                    if (relocationPhase) {
                        if (first == solution.getInstance().n()) {
                            relocationPhase = false; first = 0; second = 1;
                            continue;
                        }
                        if (second == solution.getInstance().groups()) { first++; second = 0; continue; }
                        int v = first, g = second++;
                        if (g != solution.group(v) && solution.occupancy(g) < solution.getInstance().capacity()) {
                            next = new MREFLPMove(solution, v, g, false, MREFLPEvaluationUtil.relocation(solution, v, g));
                        }
                    } else {
                        if (!swaps || first >= solution.getInstance().n()) return;
                        if (second <= first) second = first + 1;
                        if (second >= solution.getInstance().n()) { first++; second = first + 1; continue; }
                        int u = first, v = second++;
                        if (solution.group(u) != solution.group(v)) {
                            next = new MREFLPMove(solution, u, v, true, MREFLPEvaluationUtil.swap(solution, u, v));
                        }
                    }
                }
            }

            @Override public boolean hasNext() { advance(); return next != null; }
            @Override public MREFLPMove next() {
                advance();
                if (next == null) throw new NoSuchElementException();
                var result = next; next = null; return result;
            }
        };
        return ExploreResult.fromStream(StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED | Spliterator.NONNULL), false));
    }

    public static Optional<MREFLPMove> randomRelocation(MREFLPSolution s) {
        var i = s.getInstance();
        int freeGroups = 0;
        for (int g = 0; g < i.groups(); g++) if (s.occupancy(g) < i.capacity()) freeGroups++;
        long total = 0;
        for (int v = 0; v < i.n(); v++) total += freeGroups - (s.occupancy(s.group(v)) < i.capacity() ? 1 : 0);
        if (total == 0 || TimeControl.isTimeUp()) return Optional.empty();
        long pick = RandomManager.getRandom().nextLong(total);
        for (int v = 0; v < i.n(); v++) {
            int choices = freeGroups - (s.occupancy(s.group(v)) < i.capacity() ? 1 : 0);
            if (pick >= choices) { pick -= choices; continue; }
            for (int g = 0; g < i.groups(); g++) {
                if (g == s.group(v) || s.occupancy(g) == i.capacity()) continue;
                if (pick-- == 0) return Optional.of(new MREFLPMove(s, v, g, false, MREFLPEvaluationUtil.relocation(s, v, g)));
            }
        }
        throw new IllegalStateException("Relocation sampling count mismatch");
    }

    public static Optional<MREFLPMove> randomSwap(MREFLPSolution s) {
        int n = s.getInstance().n();
        long total = 0;
        for (int v = 0; v < n; v++) total += n - s.occupancy(s.group(v));
        if (total == 0 || TimeControl.isTimeUp()) return Optional.empty();
        long pick = RandomManager.getRandom().nextLong(total);
        for (int u = 0; u < n; u++) {
            int choices = n - s.occupancy(s.group(u));
            if (pick >= choices) { pick -= choices; continue; }
            for (int v = 0; v < n; v++) {
                if (s.group(u) == s.group(v)) continue;
                if (pick-- == 0) {
                    int first = Math.min(u, v), second = Math.max(u, v);
                    return Optional.of(new MREFLPMove(s, first, second, true, MREFLPEvaluationUtil.swap(s, first, second)));
                }
            }
        }
        throw new IllegalStateException("Swap sampling count mismatch");
    }

    public static Optional<MREFLPMove> randomMixed(MREFLPSolution s, double relocationProbability) {
        boolean relocation = RandomManager.getRandom().nextDouble() < relocationProbability;
        var move = relocation ? randomRelocation(s) : randomSwap(s);
        return move.isPresent() ? move : relocation ? randomSwap(s) : randomRelocation(s);
    }
}
