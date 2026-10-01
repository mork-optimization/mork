package es.urjc.etsii.grafo.mreflp.model;

import es.urjc.etsii.grafo.io.Instance;
import java.util.Map;

/** Immutable capacity-constrained assignment instance. Group indices are zero-based. */
public final class MREFLPInstance extends Instance {
    private final String name, category, sourceHash;
    private final int capacity, groups, missingWidths;
    private final long[][] flows;

    public MREFLPInstance(String name, String category, int capacity, int groups, long[][] flows,
                          int missingWidths, String sourceHash) {
        super(name + "-r" + capacity);
        if (capacity < 1 || groups < 1 || (long) capacity * groups < flows.length || flows.length == 0) {
            throw new IllegalArgumentException("Infeasible dimensions");
        }
        this.name = name;
        this.category = category;
        this.capacity = capacity;
        this.groups = groups;
        this.missingWidths = missingWidths;
        this.sourceHash = sourceHash;
        this.flows = new long[flows.length][];
        for (long[] row : flows) {
            if (row == null || row.length != flows.length) throw new IllegalArgumentException("Non-square flow matrix");
        }
        for (int i = 0; i < flows.length; i++) {
            this.flows[i] = flows[i].clone();
            for (int j = 0; j < flows.length; j++) {
                if (flows[i][j] < 0 || flows[i][j] != flows[j][i] || (i == j && flows[i][j] != 0)) {
                    throw new IllegalArgumentException("Flows must be nonnegative, symmetric, with zero diagonal");
                }
            }
        }
    }

    public int n() { return flows.length; }
    public int capacity() { return capacity; }
    public int groups() { return groups; }
    public long flow(int u, int v) { return flows[u][v]; }
    public String name() { return name; }
    public String category() { return category; }
    public String sourceHash() { return sourceHash; }
    public int missingWidths() { return missingWidths; }

    @Override public Map<String, Object> customProperties() {
        return Map.of("facilities", n(), "capacity", capacity, "groups", groups,
                "category", category, "missingWidths", missingWidths);
    }
}
