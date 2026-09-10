package es.urjc.etsii.grafo.cwp.model;

import es.urjc.etsii.grafo.solution.Solution;

import java.util.ArrayList;
import java.util.List;

/**
 * A CWP solution: a linear ordering (permutation) of all the graph vertices. The score is the
 * maximum cut over all positions of the ordering (minimized).
 *
 * <p>The swap operations ({@link #scoreAfterSwap}, {@link #swapNodes}) mirror the
 * {@code jmh} solution and are the neighbourhood used by the custom local searches:
 * swapping the positions of two vertices in the ordering.
 */
public class CWPSolution extends Solution<CWPSolution, CWPInstance> {

    private List<Integer> order;
    private double score;

    public CWPSolution(CWPInstance instance) {
        super(instance);
        this.order = new ArrayList<>();
        this.score = 0;
    }

    public CWPSolution(CWPSolution s) {
        super(s);
        this.order = new ArrayList<>(s.order);
        this.score = s.score;
    }

    @Override
    public CWPSolution cloneSolution() {
        return new CWPSolution(this);
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double recalculateScore() {
        return computeMaxCut(this.order);
    }

    private double computeMaxCut(List<Integer> ordering) {
        var instance = getInstance();
        int weight = 0;
        // For each position, count edges from a node at that position or before it
        // to a node after it, and keep the maximum such cut.
        for (int i = 0; i < ordering.size(); i++) {
            int cut = 0;
            for (int j = 0; j <= i; j++) {
                for (int k = i + 1; k < ordering.size(); k++) {
                    cut += instance.getWeight(ordering.get(j), ordering.get(k));
                }
            }
            weight = Math.max(weight, cut);
        }
        return weight;
    }

    // ---- state used by the constructive ----

    public void setOrder(List<Integer> order) {
        this.order = order;
    }

    public List<Integer> getOrder() {
        return order;
    }

    // ---- swap neighbourhood (mirrors jmh) ----

    /** Objective value that would result from swapping the positions of the two given nodes. */
    public double scoreAfterSwap(int nodeA, int nodeB) {
        List<Integer> newOrder = new ArrayList<>(order.size());
        for (int node : order) {
            if (node == nodeA) {
                newOrder.add(nodeB);
            } else if (node == nodeB) {
                newOrder.add(nodeA);
            } else {
                newOrder.add(node);
            }
        }
        return computeMaxCut(newOrder);
    }

    /** Swap the positions of the two given nodes in the ordering, updating the cached score. */
    public void swapNodes(int nodeA, int nodeB) {
        int changes = 0;
        for (int i = 0; i < order.size(); i++) {
            int node = order.get(i);
            if (node == nodeA) {
                order.set(i, nodeB);
                changes++;
            } else if (node == nodeB) {
                order.set(i, nodeA);
                changes++;
            }
            if (changes == 2) {
                break;
            }
        }
        this.score = computeMaxCut(order);
    }

    @Override
    public String toString() {
        return "Score: " + score + " " + order;
    }
}
