package es.urjc.etsii.grafo.mreflp.improve;

import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.mreflp.Main;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;

/** Algorithm 5. Scans unordered pairs and executes only strictly improving swaps. */
public final class SwapDescent extends Improver<MREFLPSolution, MREFLPInstance> {
    private final boolean cached;
    public SwapDescent(boolean cached) { super(Main.COST); this.cached = cached; }

    @Override public MREFLPSolution improve(MREFLPSolution input) {
        var s = input.cloneSolution();
        if (TimeControl.isTimeUp()) return s;
        var cache = cached ? new MREFLPDeltaCache(s) : null;
        var random = RandomManager.getRandom();
        while (!TimeControl.isTimeUp()) {
            long bestDelta = 0;
            int first = -1, second = -1, ties = 0;
            for (int u = 0; u < s.getInstance().n(); u++) {
                if (TimeControl.isTimeUp()) return s;
                for (int v = u + 1; v < s.getInstance().n(); v++) if (s.group(u) != s.group(v)) {
                    long delta = cached ? cache.swap(s, u, v) : MREFLPEvaluationUtil.swap(s, u, v);
                    if (delta < bestDelta) { bestDelta = delta; first = u; second = v; ties = 1; }
                    else if (delta < 0 && delta == bestDelta && random.nextInt(++ties) == 0) { first = u; second = v; }
                }
            }
            if (first < 0 || TimeControl.isTimeUp()) break;
            int oldU = s.group(first), oldV = s.group(second);
            new MREFLPMove(s, first, second, true, bestDelta).execute(s);
            if (cached) cache.afterSwap(s, first, second, oldU, oldV);
        }
        return s;
    }
}
