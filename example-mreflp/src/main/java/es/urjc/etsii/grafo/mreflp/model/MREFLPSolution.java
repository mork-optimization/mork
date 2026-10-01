package es.urjc.etsii.grafo.mreflp.model;

import es.urjc.etsii.grafo.solution.Solution;
import java.util.Arrays;

public final class MREFLPSolution extends Solution<MREFLPSolution, MREFLPInstance> {
    private final int[] assignments, occupancies;
    private long cost;

    public MREFLPSolution(MREFLPInstance instance) {
        super(instance);
        assignments = new int[instance.n()];
        Arrays.fill(assignments, -1);
        occupancies = new int[instance.groups()];
    }

    public MREFLPSolution(MREFLPSolution other) {
        super(other);
        assignments = other.assignments.clone();
        occupancies = other.occupancies.clone();
        cost = other.cost;
    }

    public int group(int v) { return assignments[v]; }
    public int occupancy(int g) { return occupancies[g]; }
    public long cost() { return cost; }
    public int[] assignments() { return assignments.clone(); }

    public void assign(int v, int g) {
        if (assignments[v] != -1 || g < 0 || g >= occupancies.length || occupancies[g] >= getInstance().capacity()) {
            throw new IllegalArgumentException("Invalid construction assignment");
        }
        for (int u = 0; u < assignments.length; u++) if (assignments[u] != -1) {
            cost += getInstance().flow(u, v) * Math.abs(g - assignments[u]);
        }
        assignments[v] = g;
        occupancies[g]++;
    }

    void relocate(int v, int g, long delta) {
        int old = assignments[v];
        if (old < 0 || old == g || occupancies[g] >= getInstance().capacity()) throw new IllegalArgumentException("Infeasible relocation");
        occupancies[old]--;
        occupancies[g]++;
        assignments[v] = g;
        cost += delta;
    }

    void swap(int u, int v, long delta) {
        if (assignments[u] < 0 || assignments[v] < 0 || assignments[u] == assignments[v]) throw new IllegalArgumentException("Invalid swap");
        int old = assignments[u];
        assignments[u] = assignments[v];
        assignments[v] = old;
        cost += delta;
    }

    public long recalculateCost() {
        long result = 0;
        for (int u = 0; u < assignments.length; u++) for (int v = u + 1; v < assignments.length; v++) {
            if (assignments[u] >= 0 && assignments[v] >= 0) result += getInstance().flow(u, v) * Math.abs(assignments[u] - assignments[v]);
        }
        return result;
    }

    @Override public MREFLPSolution cloneSolution() { return new MREFLPSolution(this); }
    @Override public String toString() { return "MREFLPSolution{cost=" + cost + "}"; }
}
