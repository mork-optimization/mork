package es.urjc.etsii.grafo.mmdp.model;

import es.urjc.etsii.grafo.solution.Solution;

import java.util.ArrayList;
import java.util.List;

/**
 * An MMDP solution: the subset of selected nodes. The score is the minimum pairwise distance among
 * the selected nodes (maximized).
 *
 * <p>The swap operations ({@link #contains}, {@link #scoreAfterReplace}, {@link #replaceNode})
 * mirror the {@code jmh} solution and are the neighbourhood used by the custom local
 * searches: replacing a selected node with a non-selected one.
 */
public class MMDPSolution extends Solution<MMDPSolution, MMDPInstance> {

    private List<Integer> nodes;
    private double score;

    public MMDPSolution(MMDPInstance instance) {
        super(instance);
        this.nodes = new ArrayList<>();
        this.score = 0;
    }

    public MMDPSolution(MMDPSolution s) {
        super(s);
        this.nodes = new ArrayList<>(s.nodes);
        this.score = s.score;
    }

    @Override
    public MMDPSolution cloneSolution() {
        return new MMDPSolution(this);
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double recalculateScore() {
        return computeMinDistance(this.nodes);
    }

    private double computeMinDistance(List<Integer> selected) {
        var instance = getInstance();
        double min = instance.getDistance(selected.get(0), selected.get(1));
        for (int i = 0; i < selected.size(); i++) {
            for (int j = i + 1; j < selected.size(); j++) {
                min = Math.min(min, instance.getDistance(selected.get(i), selected.get(j)));
            }
        }
        return min;
    }

    // ---- state used by the constructive ----

    public void setNodes(List<Integer> nodes) {
        this.nodes = nodes;
    }

    public List<Integer> getNodes() {
        return nodes;
    }

    // ---- swap neighbourhood (mirrors jmh) ----

    public boolean contains(int node) {
        return nodes.contains(node);
    }

    /** Objective value that would result from replacing {@code oldNode} with {@code newNode}. */
    public double scoreAfterReplace(int oldNode, int newNode) {
        List<Integer> newNodes = new ArrayList<>(nodes.size());
        for (int node : nodes) {
            newNodes.add(node == oldNode ? newNode : node);
        }
        return computeMinDistance(newNodes);
    }

    /** Replace {@code oldNode} with {@code newNode} in the selection, updating the cached score. */
    public void replaceNode(int oldNode, int newNode) {
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i) == oldNode) {
                nodes.set(i, newNode);
                break;
            }
        }
        this.score = computeMinDistance(nodes);
    }

    @Override
    public String toString() {
        return "Score: " + score + " " + nodes;
    }
}
