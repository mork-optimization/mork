package es.urjc.etsii.grafo.BMSSC.model.sol;

import es.urjc.etsii.grafo.BMSSC.model.BMSSCInstance;
import es.urjc.etsii.grafo.solution.Move;

import java.util.Objects;

/**
 * Swap this point with another one in a different cluster. Swaps between points in the same cluster are not allowed.
 */
public class SwapMove extends BMSSCMove {

    private final int pointA;
    private final int pointB;
    private final int clusterA;
    private final int clusterB;
    private final double delta;
    private final double deltaClusterA;
    private final double deltaClusterB;

    /**
     * Move constructor
     *
     * @param solution solution
     */
    public SwapMove(BMSSCSolution solution, int pointA, int pointB) {
        super(solution);
        var ins = solution.getInstance();

        assert pointA >= 0 && pointB >= 0 : "Invalid point ID";
        assert solution.isAssigned(pointA) && solution.isAssigned(pointB) : "Not assigned";
        assert solution.clusterOf(pointA) != solution.clusterOf(pointB) : "Both points are in the same cluster";

        this.pointA = pointA;
        this.clusterA = solution.clusterOf(pointA);
        this.clusterB = solution.clusterOf(pointB);
        this.pointB = pointB;

        this.deltaClusterA = solution.assignCost[pointB][clusterA] - ins.distance(pointA, pointB) + solution.removeCost[pointA];
        this.deltaClusterB = solution.assignCost[pointA][clusterB] - ins.distance(pointA, pointB) + solution.removeCost[pointB];
        this.delta = this.deltaClusterA / solution.clusters[clusterA].size() + this.deltaClusterB / solution.clusters[clusterB].size();
    }

    @Override
    protected BMSSCSolution _execute(BMSSCSolution solution) {
        var ins = solution.getInstance();

        // removeCost[n] is negative; by removing a point from the cluster, the possible improvement from removing itself decreases
        for (int p : solution.getCluster(clusterA)) {
            solution.removeCost[p] += ins.distance(p, pointA) - ins.distance(p, pointB);
        }

        for (int p : solution.getCluster(clusterB)) {
            solution.removeCost[p] += ins.distance(p, pointB) - ins.distance(p, pointA);
        }

        for (int i = 0; i < ins.n; i++) {
            if(solution.clusterOf(i) != clusterA)
                solution.assignCost[i][clusterA] += ins.distance(i,pointB) - ins.distance(i,pointA);
            if(solution.clusterOf(i) != clusterB)
                solution.assignCost[i][clusterB] += ins.distance(i,pointA) - ins.distance(i,pointB);
        }

        // Update data structures
        solution.clusters[clusterA].remove(pointA);
        solution.clusters[clusterA].add(pointB);
        solution.clusters[clusterB].remove(pointB);
        solution.clusters[clusterB].add(pointA);
        solution.setOfPoint[pointB] = clusterA;
        solution.setOfPoint[pointA] = clusterB;

        // Update o.f.
        solution.clusterScore[clusterA] += deltaClusterA;
        solution.clusterScore[clusterB] += deltaClusterB;

        // Update cache for point being moved
        solution.assignCost[pointA][clusterB] = 0;
        solution.assignCost[pointB][clusterA] = 0;
        solution.assignCost[pointA][clusterA] = solution.calculateAssignCost(pointA, clusterA);
        solution.assignCost[pointB][clusterB] = solution.calculateAssignCost(pointB, clusterB);
        solution.removeCost[pointA] = solution.calculateRemoveCost(pointA);
        solution.removeCost[pointB] = solution.calculateRemoveCost(pointB);

        // Verify caches and return true as a swap move always modifies solution
        // Swap in same cluster is not allowed
        assert solution.cachesValid();
        return solution;
    }

    @Override
    public double getValue() {
        return delta;
    }

    @Override
    public String toString() {
        return "Swap(%s, %s) = %s".formatted(pointA, pointB, delta);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SwapMove swapMove = (SwapMove) o;
        return pointA == swapMove.pointA && pointB == swapMove.pointB && clusterA == swapMove.clusterA && clusterB == swapMove.clusterB && Double.compare(swapMove.delta, delta) == 0 && Double.compare(swapMove.deltaClusterA, deltaClusterA) == 0 && Double.compare(swapMove.deltaClusterB, deltaClusterB) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointA, pointB, clusterA, clusterB, delta, deltaClusterA, deltaClusterB);
    }
}
