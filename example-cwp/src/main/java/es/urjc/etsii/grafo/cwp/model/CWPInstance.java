package es.urjc.etsii.grafo.cwp.model;

import es.urjc.etsii.grafo.io.Instance;

/**
 * Cutwidth instance: an undirected graph stored as an adjacency matrix ({@code getWeight(i,j)} is
 * 1 if there is an edge, 0 otherwise), plus the number of edges. Vertices are 0-based (as in
 * {@code jmh}). Immutable once loaded.
 */
public class CWPInstance extends Instance {

    private final int numNodes;
    private final int numEdges;
    private final int[][] weights;

    public CWPInstance(String name, int numNodes, int numEdges, int[][] weights) {
        super(name);
        this.numNodes = numNodes;
        this.numEdges = numEdges;
        this.weights = weights;
        setProperty("nodes", numNodes);
        setProperty("edges", numEdges);
    }

    /** 1 if nodes i and j are joined by an edge, 0 otherwise. */
    public int getWeight(int i, int j) {
        return weights[i][j];
    }

    public int getNumNodes() {
        return numNodes;
    }

    public int getNumEdges() {
        return numEdges;
    }

    @Override
    public String toString() {
        return getId() + " (n=" + numNodes + ", e=" + numEdges + ")";
    }
}
