package es.urjc.etsii.grafo.mdp.model;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.mdp.util.Weighted;

import java.util.ArrayList;
import java.util.List;

/**
 * Maximum Diversity instance: a complete graph of nodes with pairwise distances (stored as a
 * lower-triangular matrix), plus the number of nodes a solution must select. Node indices are
 * 0-based. Immutable once loaded. Reused unchanged from {@code mork-experiments}.
 */
public class MDPInstance extends Instance {

    private final double[][] weights;
    private final int numSolutionNodes;
    private final List<MDPNode> nodes;
    private List<Weighted<MDPNode>> nodesDistance;

    public MDPInstance(String name, int numSolutionNodes, double[][] weights) {
        super(name);
        this.numSolutionNodes = numSolutionNodes;
        this.weights = weights;
        this.nodes = new ArrayList<>();
        for (int i = 0; i < weights.length + 1; i++) {
            nodes.add(new MDPNode(i, this));
        }
        setProperty("nodes", nodes.size());
        setProperty("selected", numSolutionNodes);
    }

    public MDPNode getNode(int index) {
        return nodes.get(index);
    }

    public double getWeight(int i, int j) {
        if (i == j) {
            return 0;
        } else {
            return weights[Math.max(i, j) - 1][Math.min(i, j)];
        }
    }

    public int getNumNodes() {
        return nodes.size();
    }

    public List<MDPNode> getNodes() {
        return nodes;
    }

    public int getNumSolutionNodes() {
        return numSolutionNodes;
    }

    public boolean isNodesDistanceCalculated() {
        return nodesDistance != null;
    }

    /** Lazily-computed sum of distances from each node to all others (used by the D2 components). */
    public List<Weighted<MDPNode>> getNodesDistance() {
        if (nodesDistance == null) {
            nodesDistance = new ArrayList<>();
            for (MDPNode n : getNodes()) {
                double weight = 0;
                for (MDPNode on : getNodes()) {
                    weight += n.getDistanceTo(on);
                }
                nodesDistance.add(new Weighted<>(n, weight));
            }
        }
        return nodesDistance;
    }

    @Override
    public String toString() {
        return getId() + " (n=" + getNumNodes() + ", m=" + numSolutionNodes + ")";
    }
}
