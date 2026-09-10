package es.urjc.etsii.grafo.cph.model;

import es.urjc.etsii.grafo.io.Instance;

/**
 * Capacitated p-hub instance: nodes with (x, y) coordinates and a demand, the number of hubs
 * to open and the capacity of each hub. Node indices are 0-based (as in the {@code jmh}
 * project). Immutable once loaded.
 */
public class CPHInstance extends Instance {

    private final int numNodes;
    private final int numHubs;
    private final int hubCapacity;
    // nodeData[i] = {x, y, demand} for node i (0-based)
    private final int[][] nodeData;

    public CPHInstance(String name, int numNodes, int numHubs, int hubCapacity, int[][] nodeData) {
        super(name);
        this.numNodes = numNodes;
        this.numHubs = numHubs;
        this.hubCapacity = hubCapacity;
        this.nodeData = nodeData;
        setProperty("nodes", numNodes);
        setProperty("hubs", numHubs);
        setProperty("capacity", hubCapacity);
    }

    /** Euclidean distance between two nodes, truncated to int (as in {@code jmh}). */
    public int getDistance(int i, int j) {
        double dx = Math.pow(nodeData[j][0] - nodeData[i][0], 2);
        double dy = Math.pow(nodeData[j][1] - nodeData[i][1], 2);
        return (int) Math.sqrt(dx + dy);
    }

    public int getDemand(int node) {
        return nodeData[node][2];
    }

    public int getNumNodes() {
        return numNodes;
    }

    public int getNumHubs() {
        return numHubs;
    }

    public int getHubCapacity() {
        return hubCapacity;
    }

    @Override
    public String toString() {
        return getId() + " (n=" + numNodes + ", p=" + numHubs + ", c=" + hubCapacity + ")";
    }
}
