package es.urjc.etsii.grafo.bmssc.model.sol;

import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.collections.BitSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static es.urjc.etsii.grafo.bmssc.util.BMSSCUtil.contribution;

public class BMSSCSolution extends Solution<BMSSCSolution, BMSSCInstance> {
    private final BitSet[] clusters;
    private final BitSet unassigned;
    private final int[] clusterOfPoint;
    // BitSet.size() scans its words: keep cardinalities to score moves in O(1).
    private final int[] sizes;
    private final int[] capacities;
    private final double[] pairSums;
    // Includes the point's own cluster; its distance to itself is zero.
    private final double[][] pointClusterDistance;
    private double cost;

    public BMSSCSolution(BMSSCInstance instance) {
        super(instance);
        clusters = new BitSet[instance.k];
        for (int c = 0; c < instance.k; c++) clusters[c] = new BitSet(instance.n);
        unassigned = new BitSet(instance.n);
        unassigned.add(0, instance.n);
        clusterOfPoint = new int[instance.n];
        Arrays.fill(clusterOfPoint, -1);
        sizes = new int[instance.k];
        capacities = instance.getClusterSizes();
        pairSums = new double[instance.k];
        pointClusterDistance = new double[instance.n][instance.k];
    }

    public BMSSCSolution(BMSSCSolution original) {
        super(original);
        clusters = new BitSet[original.clusters.length];
        for (int c = 0; c < clusters.length; c++) clusters[c] = new BitSet(original.clusters[c]);
        unassigned = new BitSet(original.unassigned);
        clusterOfPoint = original.clusterOfPoint.clone();
        sizes = original.sizes.clone();
        capacities = original.capacities.clone();
        pairSums = original.pairSums.clone();
        pointClusterDistance = new double[original.pointClusterDistance.length][];
        for (int p = 0; p < pointClusterDistance.length; p++) {
            pointClusterDistance[p] = original.pointClusterDistance[p].clone();
        }
        cost = original.cost;
    }

    @Override
    public BMSSCSolution cloneSolution() { return new BMSSCSolution(this); }

    public double getCost() { return cost; }
    public int clusterOf(int point) { return clusterOfPoint[point]; }
    public int[] getPointAssignments() { return clusterOfPoint.clone(); }
    public boolean isAssigned(int point) { return clusterOfPoint[point] != -1; }
    public int getClusterSize(int cluster) { return sizes[cluster]; }
    public int getClusterCapacity(int cluster) { return capacities[cluster]; }
    public double getPairSum(int cluster) { return pairSums[cluster]; }
    public double getPointClusterDistance(int point, int cluster) { return pointClusterDistance[point][cluster]; }
    public Set<Integer> getCluster(int cluster) { return Collections.unmodifiableSet(clusters[cluster]); }
    public Set<Integer> getNotAssignedPoints() { return Collections.unmodifiableSet(unassigned); }
    public boolean isFullCluster(int cluster) { return sizes[cluster] >= capacities[cluster]; }

    public boolean canAssign(int point, int cluster) {
        return !isAssigned(point) && !isFullCluster(cluster);
    }

    public boolean canReassign(int point, int cluster) {
        return isAssigned(point) && clusterOf(point) != cluster && !isFullCluster(cluster);
    }

    public boolean canSwap(int p, int q) {
        return isAssigned(p) && isAssigned(q) && clusterOf(p) != clusterOf(q);
    }

    public boolean feasibleClusterSizes() {
        for (int c = 0; c < sizes.length; c++) {
            if (sizes[c] != getInstance().getClusterSize(c)) return false;
        }
        return unassigned.isEmpty();
    }

    public void relaxClusterSizeConstraint(double margin) {
        if (!Double.isFinite(margin) || margin < 0) throw new IllegalArgumentException("Invalid capacity margin: " + margin);
        for (int c = 0; c < capacities.length; c++) {
            capacities[c] = (int) Math.min(getInstance().n, Math.round(getInstance().getClusterSize(c) * (1 + margin)));
        }
    }

    public void restoreClusterSizeConstraint() {
        for (int c = 0; c < capacities.length; c++) capacities[c] = getInstance().getClusterSize(c);
    }

    double assignDelta(int point, int cluster) {
        return contribution(pairSums[cluster] + pointClusterDistance[point][cluster], sizes[cluster] + 1)
                - contribution(pairSums[cluster], sizes[cluster]);
    }

    double reassignDelta(int point, int target) {
        int source = clusterOf(point);
        return contribution(pairSums[source] - pointClusterDistance[point][source], sizes[source] - 1)
                - contribution(pairSums[source], sizes[source]) + assignDelta(point, target);
    }

    double swapDelta(int p, int q) {
        int a = clusterOf(p), b = clusterOf(q);
        double distance = getInstance().distance(p, q);
        return (pointClusterDistance[q][a] - distance - pointClusterDistance[p][a]) / sizes[a]
                + (pointClusterDistance[p][b] - distance - pointClusterDistance[q][b]) / sizes[b];
    }

    void assign(int point, int cluster) {
        if (!canAssign(point, cluster)) throw new IllegalArgumentException("Invalid assignment");
        cost += assignDelta(point, cluster);
        pairSums[cluster] += pointClusterDistance[point][cluster];
        clusters[cluster].add(point);
        sizes[cluster]++;
        unassigned.remove(point);
        clusterOfPoint[point] = cluster;
        for (int p = 0; p < getInstance().n; p++) {
            pointClusterDistance[p][cluster] += getInstance().distance(p, point);
        }
        assert cachesValid();
    }

    void reassign(int point, int target) {
        if (!canReassign(point, target)) throw new IllegalArgumentException("Invalid reassignment");
        int source = clusterOf(point);
        cost += reassignDelta(point, target);
        pairSums[source] -= pointClusterDistance[point][source];
        pairSums[target] += pointClusterDistance[point][target];
        clusters[source].remove(point);
        clusters[target].add(point);
        sizes[source]--;
        sizes[target]++;
        if (sizes[source] < 2) pairSums[source] = 0;
        clusterOfPoint[point] = target;
        for (int p = 0; p < getInstance().n; p++) {
            double distance = getInstance().distance(p, point);
            pointClusterDistance[p][source] -= distance;
            pointClusterDistance[p][target] += distance;
            if (sizes[source] == 0) pointClusterDistance[p][source] = 0;
        }
        assert cachesValid();
    }

    void swap(int p, int q) {
        if (!canSwap(p, q)) throw new IllegalArgumentException("Invalid swap");
        int a = clusterOf(p), b = clusterOf(q);
        double distance = getInstance().distance(p, q);
        cost += swapDelta(p, q);
        pairSums[a] += pointClusterDistance[q][a] - distance - pointClusterDistance[p][a];
        pairSums[b] += pointClusterDistance[p][b] - distance - pointClusterDistance[q][b];
        if (sizes[a] < 2) pairSums[a] = 0;
        if (sizes[b] < 2) pairSums[b] = 0;
        clusters[a].remove(p);
        clusters[a].add(q);
        clusters[b].remove(q);
        clusters[b].add(p);
        clusterOfPoint[p] = b;
        clusterOfPoint[q] = a;
        for (int r = 0; r < getInstance().n; r++) {
            double change = getInstance().distance(r, q) - getInstance().distance(r, p);
            pointClusterDistance[r][a] += change;
            pointClusterDistance[r][b] -= change;
        }
        assert cachesValid();
    }

    public boolean cachesValid() {
        return BMSSCSolutionValidator.validateState(this, false).isValid();
    }

    @Override
    public String toString() { return Double.toString(cost); }
}
