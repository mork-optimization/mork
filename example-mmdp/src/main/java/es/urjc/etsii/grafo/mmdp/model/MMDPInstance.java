package es.urjc.etsii.grafo.mmdp.model;

import es.urjc.etsii.grafo.io.Instance;

/**
 * MaxMin Diversity instance: a complete graph of nodes with pairwise distances (a weight matrix),
 * plus the number of nodes a solution must select. Node indices are 0-based (as in
 * {@code jmh}). Immutable once loaded.
 */
public class MMDPInstance extends Instance {

    private final int numNodes;
    private final int numSolutionNodes;
    private final double[][] weights;

    public MMDPInstance(String name, int numNodes, int numSolutionNodes, double[][] weights) {
        super(name);
        this.numNodes = numNodes;
        this.numSolutionNodes = numSolutionNodes;
        this.weights = weights;
        setProperty("nodes", numNodes);
        setProperty("selected", numSolutionNodes);
    }

    public double getDistance(int i, int j) {
        return weights[i][j];
    }

    public int getNumNodes() {
        return numNodes;
    }

    public int getNumSolutionNodes() {
        return numSolutionNodes;
    }

    @Override
    public String toString() {
        return getId() + " (n=" + numNodes + ", m=" + numSolutionNodes + ")";
    }
}
