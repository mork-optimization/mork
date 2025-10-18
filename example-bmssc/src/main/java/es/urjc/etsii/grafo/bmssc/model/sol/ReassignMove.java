package es.urjc.etsii.grafo.bmssc.model.sol;

import java.util.Objects;

/**
 * Removes a point from a cluster and adds it to another cluster
 */
public class ReassignMove extends BMSSCMove {

    private final int pointId;
    private final int targetCluster;
    private final double removeCost;
    private final double assignCost;
    private final double delta;
    private final int sourceCluster;

    /**
     * Move constructor
     *
     * @param solution solution
     */
    public ReassignMove(BMSSCSolution solution, int pointId, int targetCluster) {
        super(solution);

        assert solution.getClusterSize(targetCluster) > solution.clusters[targetCluster].size()
                : "Cannot add %s to cluster %s, max cluster size of %s reached".formatted(pointId, targetCluster, solution.getClusterSize(targetCluster));

        this.pointId = pointId;
        this.targetCluster = targetCluster;
        this.sourceCluster = solution.clusterOf(pointId);
        this.removeCost = solution.removeCost[pointId];
        this.assignCost = solution.assignCost[pointId][targetCluster];
        this.delta = getDelta(solution);

        assert this.targetCluster != this.sourceCluster : "Cannot reassign point to same cluster";
    }

    private double getDelta(BMSSCSolution solution) {
        int cSizeA = solution.clusters[sourceCluster].size();
        int cSizeB = solution.clusters[targetCluster].size();
        int newSizeA = cSizeA - 1;
        int newSizeB = cSizeB + 1;
        double cAScore = solution.clusterScore[sourceCluster];
        double cBScore = solution.clusterScore[targetCluster];

        double deltaA = cSizeA == 0 ? 0 :
                (cAScore + removeCost) / newSizeA - cAScore / cSizeA;

        double deltaB = (cBScore + assignCost) / newSizeB - cBScore / cSizeB;

        return deltaA + deltaB;
    }

    @Override
    protected BMSSCSolution _execute(BMSSCSolution solution) {
        var instance = solution.getInstance();
        assert !solution.getCluster(targetCluster).contains(pointId)
                : "Cluster %s already contains point %s".formatted(targetCluster, pointId);

        for (int p : solution.getCluster(sourceCluster)) {
            // The cluster where the point is coming from: size reduced
            solution.removeCost[p] += instance.distance(p, pointId);
        }

        for (int p : solution.getCluster(targetCluster)) {
            // The cluster where the point is being moved too: size increased
            solution.removeCost[p] -= instance.distance(p, pointId);
        }

        // Update caches for the rest of the clusters
        for (int i = 0; i < instance.n; i++) {
            double distance = instance.distance(i, pointId);
            if (solution.clusterOf(i) != sourceCluster) {
                solution.assignCost[i][sourceCluster] -= distance;
            }
            if (solution.clusterOf(i) != targetCluster) {
                solution.assignCost[i][targetCluster] += distance;
            }
        }

        // Update cache for point being moved
        solution.assignCost[pointId][targetCluster] = 0;
        solution.assignCost[pointId][sourceCluster] = -removeCost;
        solution.removeCost[pointId] = -assignCost;

        // Update data structures
        solution.clusters[sourceCluster].remove(pointId);
        solution.clusters[targetCluster].add(pointId);
        solution.setOfPoint[pointId] = targetCluster;

        // Update objective function
        solution.clusterScore[sourceCluster] += this.removeCost;
        solution.clusterScore[targetCluster] += this.assignCost;

        // Verify all caches only if running in validation mode
        assert solution.cachesValid() : "Cache is in invalid state";

        return solution; // Always modifies solution, it is not valid to reassign a move to the same cluster. Verified by assert in constructor
    }

    @Override
    public double getValue() {
        return this.delta;
    }

    @Override
    public String toString() {
        return "Reassign %s (%s) -> %s (%s)".formatted(pointId, removeCost, targetCluster, assignCost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReassignMove that = (ReassignMove) o;
        return pointId == that.pointId && targetCluster == that.targetCluster && Double.compare(that.removeCost, removeCost) == 0 && Double.compare(that.assignCost, assignCost) == 0 && Double.compare(that.delta, delta) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointId, targetCluster, removeCost, assignCost, delta);
    }
}
