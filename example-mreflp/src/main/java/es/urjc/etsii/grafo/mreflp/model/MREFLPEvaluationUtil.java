package es.urjc.etsii.grafo.mreflp.model;

public final class MREFLPEvaluationUtil {
    private MREFLPEvaluationUtil() {}

    /** Direct O(n) evaluation, independent of the incremental cache. */
    public static long relocation(MREFLPSolution s, int v, int destination) {
        long delta = 0;
        for (int u = 0; u < s.getInstance().n(); u++) if (u != v) {
            delta += s.getInstance().flow(u, v) * (Math.abs(destination - s.group(u)) - Math.abs(s.group(v) - s.group(u)));
        }
        return delta;
    }

    public static long swap(MREFLPSolution s, int u, int v) {
        long delta = 0;
        int i = s.group(u), j = s.group(v);
        for (int x = 0; x < s.getInstance().n(); x++) if (x != u && x != v) {
            delta += s.getInstance().flow(u, x) * (Math.abs(j - s.group(x)) - Math.abs(i - s.group(x)));
            delta += s.getInstance().flow(v, x) * (Math.abs(i - s.group(x)) - Math.abs(j - s.group(x)));
        }
        return delta;
    }
}
