package es.urjc.etsii.grafo.mreflp.model;

/** Exact delta cache with aggregated moved-row rebuilds and sparse-flow updates. */
public final class MREFLPDeltaCacheNew {
    private final long[][] delta;

    public MREFLPDeltaCacheNew(MREFLPSolution s) {
        int n = s.getInstance().n(), k = s.getInstance().groups();
        delta = new long[n][k];
        for (int v = 0; v < n; v++) rebuildRow(s, v);
    }

    public long relocation(int v, int group) { return delta[v][group]; }
    public long swap(MREFLPSolution s, int u, int v) {
        return delta[u][s.group(v)] + delta[v][s.group(u)] + 2 * s.getInstance().flow(u, v) * Math.abs(s.group(u) - s.group(v));
    }

    /** Called after applying the relocation, with the old group retained by the caller. */
    public void afterRelocation(MREFLPSolution s, int v, int oldGroup) {
        updateOthers(s, v, oldGroup, s.group(v), -1);
        rebuildRow(s, v);
    }

    /** Algebraically identical to two updates; neither intermediate assignment is exposed. */
    public void afterSwap(MREFLPSolution s, int u, int v, int oldU, int oldV) {
        updateOthers(s, u, oldU, oldV, v);
        updateOthers(s, v, oldV, oldU, u);
        rebuildRow(s, u);
        rebuildRow(s, v);
    }

    private void updateOthers(MREFLPSolution s, int v, int i, int j, int skip) {
        for (int u = 0; u < delta.length; u++) if (u != v && u != skip) {
            int g = s.group(u);
            long w = s.getInstance().flow(u, v);
            if (w == 0) continue;
            int originDifference = Math.abs(g - i) - Math.abs(g - j);
            for (int h = 0; h < delta[u].length; h++) {
                delta[u][h] += w * (Math.abs(h - j) - Math.abs(h - i) + originDifference);
            }
        }
    }

    private void rebuildRow(MREFLPSolution s, int v) {
        long[] groupFlows = new long[delta[v].length];
        long total = 0, costAtZero = 0;
        for (int u = 0; u < delta.length; u++) {
            long w = s.getInstance().flow(u, v);
            groupFlows[s.group(u)] += w;
            total += w;
            costAtZero += w * s.group(u);
        }
        long current = costAtZero, left = 0;
        for (int g = 0; g < groupFlows.length; g++) {
            delta[v][g] = current;
            left += groupFlows[g];
            current += 2 * left - total;
        }
        long origin = delta[v][s.group(v)];
        for (int g = 0; g < groupFlows.length; g++) delta[v][g] -= origin;
    }
}
