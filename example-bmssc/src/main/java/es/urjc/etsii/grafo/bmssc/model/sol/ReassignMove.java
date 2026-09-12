package es.urjc.etsii.grafo.bmssc.model.sol;

import java.util.Objects;

public final class ReassignMove extends BMSSCMove {
    private final int point;
    private final int cluster;
    private final double delta;

    public ReassignMove(BMSSCSolution solution, int point, int cluster) {
        super(solution);
        if (!solution.canReassign(point, cluster)) throw new IllegalArgumentException("Invalid reassignment");
        this.point = point;
        this.cluster = cluster;
        this.delta = solution.reassignDelta(point, cluster);
    }

    @Override
    protected BMSSCSolution _execute(BMSSCSolution solution) {
        solution.reassign(point, cluster);
        return solution;
    }

    @Override
    public double getCostDelta() { return delta; }
    public int getPoint() { return point; }
    public int getCluster() { return cluster; }

    @Override
    public String toString() { return "Reassign " + point + " -> " + cluster + ", delta " + delta; }

    @Override
    public boolean equals(Object o) {
        return o instanceof ReassignMove other && point == other.point && cluster == other.cluster;
    }

    @Override
    public int hashCode() { return Objects.hash(point, cluster); }
}
