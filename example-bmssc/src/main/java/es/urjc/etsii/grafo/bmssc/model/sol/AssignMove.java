package es.urjc.etsii.grafo.bmssc.model.sol;

import java.util.Objects;

public final class AssignMove extends BMSSCMove {
    private final int point;
    private final int cluster;
    private final double delta;

    public AssignMove(BMSSCSolution solution, int point, int cluster) {
        super(solution);
        if (!solution.canAssign(point, cluster)) throw new IllegalArgumentException("Invalid assignment");
        this.point = point;
        this.cluster = cluster;
        this.delta = solution.assignDelta(point, cluster);
    }

    @Override
    protected BMSSCSolution _execute(BMSSCSolution solution) {
        solution.assign(point, cluster);
        return solution;
    }

    @Override
    public double getCostDelta() { return delta; }
    public int getPoint() { return point; }
    public int getCluster() { return cluster; }

    @Override
    public String toString() { return "Assign " + point + " -> " + cluster + ", delta " + delta; }

    @Override
    public boolean equals(Object o) {
        return o instanceof AssignMove other && point == other.point && cluster == other.cluster;
    }

    @Override
    public int hashCode() { return Objects.hash(point, cluster); }
}
