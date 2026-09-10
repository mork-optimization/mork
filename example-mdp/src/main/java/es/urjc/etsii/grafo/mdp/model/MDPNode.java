package es.urjc.etsii.grafo.mdp.model;

/**
 * A node of a Maximum Diversity instance. A plain domain helper (not a Mork type); the distance
 * between two nodes is read from the instance weight matrix. Reused unchanged from
 * {@code mork-experiments} (only the package changes).
 */
public class MDPNode {

    private final int index;
    private final MDPInstance instance;

    public MDPNode(int index, MDPInstance instance) {
        this.index = index;
        this.instance = instance;
    }

    public double getDistanceTo(MDPNode node) {
        return instance.getWeight(index, node.index);
    }

    public int getIndex() {
        return index;
    }

    public int compareTo(MDPNode node) {
        return index - node.getIndex();
    }

    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof MDPNode other) && index == other.index;
    }

    @Override
    public String toString() {
        return Integer.toString(index);
    }
}
