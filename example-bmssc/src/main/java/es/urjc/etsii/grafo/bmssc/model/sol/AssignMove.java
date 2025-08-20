package es.urjc.etsii.grafo.bmssc.model.sol;

import java.util.Objects;

/**
 * Assigns a point to a cluster
 */
public class AssignMove extends BMSSCMove {

    private final int point;
    private final int cluster;
    private final double delta;
    private final double clusterScoreChange;

    /**
     * Move constructor
     *
     * @param solution solution
     * @param point
     * @param cluster
     */
    public AssignMove(BMSSCSolution solution, int point, int cluster) {
        super(solution);
        assert !solution.isFullCluster(cluster): "Cluster is full, cannot assign point";
        assert !solution.clusters[cluster].contains(point) : "Point already in cluster";

        this.point = point;
        this.cluster = cluster;
        this.clusterScoreChange = solution.assignCost[point][cluster];

        this.delta = getDelta(solution);
    }

    private double getDelta(BMSSCSolution solution){
        int cSize = solution.clusters[cluster].size();
        if(cSize == 0) return 0; // Avoid division by 0
        double cScore = solution.clusterScore[cluster];
        int newSize = cSize + 1;
        // Before we contributed cScore / cSize,
        // now it would be (cScore + cScoreChange) / (cSize+1)
        // So difference is [(cScore + cScoreChange) / (cSize+1)] - ( cScore / cSize )
        // Which can be simplified to
        return (cScore + clusterScoreChange) / newSize - cScore / cSize;
    }

    @Override
    protected BMSSCSolution _execute(BMSSCSolution solution) {
        var ins = solution.getInstance();
        assert !solution.isFullCluster(cluster): "Cluster is full, cannot assign point";
        assert !solution.clusters[cluster].contains(point) : "Point already in cluster";
        assert solution.notAssignedPoints.contains(point) : "Point already in cluster";

        solution.clusterScore[cluster] += clusterScoreChange;
        solution.clusters[cluster].add(point);
        solution.notAssignedPoints.remove(point);
        solution.setOfPoint[point] = cluster;
        solution.assignCost[point][cluster] = 0;
        solution.removeCost[point] = -clusterScoreChange; // Remove cost is the reverse of the add cost

        for (int i = 0; i < ins.n; i++) {
            if(solution.clusterOf(i) != cluster){
                solution.assignCost[i][cluster] += ins.distance(point, i);
            } else {
                // Same cluster
                solution.removeCost[i] -= ins.distance(point, i);
            }
        }
        assert solution.cachesValid();
        return solution;
    }

    @Override
    public double getValue() {
        return delta;
    }

    @Override
    public String toString() {
        return "Assign %s -> %s, cost %s".formatted(point, cluster, delta);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignMove that = (AssignMove) o;
        return point == that.point && cluster == that.cluster && Double.compare(that.delta, delta) == 0 && Double.compare(that.clusterScoreChange, clusterScoreChange) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(point, cluster, delta, clusterScoreChange);
    }

    public int getPoint() {
        return point;
    }

    public int getCluster() {
        return cluster;
    }

    public double getDelta() {
        return delta;
    }

    public double getClusterScoreChange() {
        return clusterScoreChange;
    }

}
