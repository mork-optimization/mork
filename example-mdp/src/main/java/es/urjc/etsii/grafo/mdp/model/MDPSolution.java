package es.urjc.etsii.grafo.mdp.model;

import es.urjc.etsii.grafo.mdp.util.Weighted;
import es.urjc.etsii.grafo.mdp.util.WeightedIterator;
import es.urjc.etsii.grafo.solution.Solution;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static es.urjc.etsii.grafo.mdp.util.Weighted.max;
import static es.urjc.etsii.grafo.mdp.util.Weighted.min;

/**
 * The MDP solution. Keeps its optimized incremental representation (per-node contribution list
 * {@code nodesDistance}, a presence array, and cached best/worst nodes) and extends Mork's
 * {@link Solution}. The score (total diversity, to be maximized) is exposed through
 * {@link #getWeight()}.
 *
 * <p>Reused <b>unchanged</b> from {@code mork-experiments} (only the package changes): the
 * {@code mork-full} migration keeps the whole model layer identical and only swaps the algorithm
 * layer for Mork's reusable Scatter Search components. The swap operation used by the local search
 * is {@link #changeNode(MDPNode, MDPNode)}, and {@link #calculateDistanceWithoutNode(MDPNode, MDPNode)}
 * gives the incremental delta the concrete {@code Move} needs.
 */
public class MDPSolution extends Solution<MDPSolution, MDPInstance>
        implements Comparable<MDPSolution>, Iterable<MDPNode> {

    private Weighted<MDPNode> worstWNode = null;
    private Weighted<MDPNode> bestWNode = null;

    private double totalWeight = 0;

    private List<Weighted<MDPNode>> nodesDistance;
    private List<MDPNode> nodes;
    private boolean[] nodesPresence;

    private int refSetPosition = -1;

    public MDPSolution(List<MDPNode> nodes, MDPInstance instance) {
        super(instance);
        this.nodes = nodes;
        this.nodesPresence = new boolean[instance.getNumNodes()];
        calculateWeightAndPresenceNodes();
    }

    public MDPSolution(MDPSolution solution) {
        super(solution);
        if (solution.nodesDistance == null) {
            nodes = new LinkedList<>(solution.nodes);
        } else {
            nodesDistance = new LinkedList<>();
            for (Weighted<MDPNode> wn : solution.nodesDistance) {
                nodesDistance.add(new Weighted<>(wn));
            }
            bestWNode = solution.bestWNode;
            worstWNode = solution.worstWNode;
        }
        nodesPresence = new boolean[getInstance().getNumNodes()];
        System.arraycopy(solution.nodesPresence, 0, nodesPresence, 0, nodesPresence.length);
        totalWeight = solution.totalWeight;
    }

    public MDPSolution(MDPInstance instance) {
        super(instance);
        nodesPresence = new boolean[instance.getNumNodes()];
        if (instance.isNodesDistanceCalculated()) {
            nodesDistance = new LinkedList<>();
            for (Weighted<MDPNode> wn : instance.getNodesDistance()) {
                nodesDistance.add(new Weighted<>(wn));
            }
            calculateWeightAndPresenceNodesCont();
        } else {
            nodes = new LinkedList<>(instance.getNodes());
            calculateWeightAndPresenceNodes();
        }
    }

    public MDPSolution(MDPInstance instance, List<Weighted<MDPNode>> nodesContribution) {
        super(instance);
        this.nodesDistance = new LinkedList<>();
        for (Weighted<MDPNode> wn : nodesContribution) {
            this.nodesDistance.add(new Weighted<>(wn));
        }
        nodesPresence = new boolean[instance.getNumNodes()];
        calculateWeightAndPresenceNodesCont();
    }

    @Override
    public MDPSolution cloneSolution() {
        return new MDPSolution(this);
    }

    private void calculateWeightAndPresenceNodes() {
        totalWeight = 0;
        for (int i = 0; i < nodes.size(); i++) {
            MDPNode node = nodes.get(i);
            nodesPresence[node.getIndex()] = true;
            for (int j = i + 1; j < nodes.size(); j++) {
                MDPNode otherNode = nodes.get(j);
                totalWeight += node.getDistanceTo(otherNode);
            }
        }
    }

    private void calculateWeightAndPresenceNodesCont() {
        totalWeight = 0;
        for (Weighted<MDPNode> wn : nodesDistance) {
            totalWeight += wn.getWeight();
            nodesPresence[wn.getElement().getIndex()] = true;
            refreshWorstAndBest(wn);
        }
        totalWeight = totalWeight / 2;
    }

    public Weighted<MDPNode> getWorstNode() {
        if (worstWNode == null) {
            calculateWorstBestWOC();
        }
        return worstWNode;
    }

    public Weighted<MDPNode> getBestNode() {
        if (bestWNode == null) {
            calculateWorstBestWOC();
        }
        return bestWNode;
    }

    private void calculateWorstBestWOC() {
        for (MDPNode n : nodes) {
            double weight = 0;
            for (MDPNode on : nodes) {
                weight += n.getDistanceTo(on);
            }
            Weighted<MDPNode> wn = new Weighted<>(n, weight);
            refreshWorstAndBest(wn);
        }
    }

    private void refreshWorstAndBest(Weighted<MDPNode> wn) {
        worstWNode = min(wn, worstWNode);
        bestWNode = max(wn, bestWNode);
    }

    /** Total diversity of the solution (cached), used as the objective value. */
    public double getWeight() {
        return totalWeight;
    }

    /** Recompute the total diversity from scratch, without side effects (used by the validator). */
    public double recalculateScore() {
        var instance = getInstance();
        List<Integer> present = new ArrayList<>();
        for (int i = 0; i < nodesPresence.length; i++) {
            if (nodesPresence[i]) {
                present.add(i);
            }
        }
        double weight = 0;
        for (int i = 0; i < present.size(); i++) {
            for (int j = i + 1; j < present.size(); j++) {
                weight += instance.getNode(present.get(i)).getDistanceTo(instance.getNode(present.get(j)));
            }
        }
        return weight;
    }

    @Override
    public int compareTo(MDPSolution solution) {
        if (getWeight() > solution.getWeight()) {
            return 1;
        } else if (getWeight() == solution.getWeight()) {
            return 0;
        } else {
            return -1;
        }
    }

    public boolean contains(MDPNode node) {
        return nodesPresence[node.getIndex()];
    }

    public List<Weighted<MDPNode>> getNodesDistance() {
        if (nodesDistance == null) {
            calculateNodesDistance();
        }
        return nodesDistance;
    }

    public void calculateNodesDistance() {
        if (nodesDistance == null) {
            nodesDistance = new ArrayList<>();
            worstWNode = null;
            bestWNode = null;
            double totalContribution = 0;
            for (MDPNode n : this) {
                double weight = 0;
                for (MDPNode on : this) {
                    weight += n.getDistanceTo(on);
                }
                totalContribution += weight;
                Weighted<MDPNode> wn = new Weighted<>(n, weight);
                nodesDistance.add(wn);
                refreshWorstAndBest(wn);
            }
            totalWeight = totalContribution / 2d;
            nodes = null;
        }
    }

    public void changeNode(MDPNode olderNode, MDPNode newNode) {
        if (!nodesPresence[olderNode.getIndex()]) {
            throw new InvalidParameterException("MDPNode " + olderNode.getIndex()
                    + " is not in the solution");
        }
        worstWNode = null;
        bestWNode = null;

        if (nodesDistance != null) {
            double newNodeDistance = 0;
            Weighted<MDPNode> newWN = new Weighted<>(newNode, 0);
            for (int i = 0, n = nodesDistance.size(); i < n; i++) {
                Weighted<MDPNode> wn = nodesDistance.get(i);
                if (wn.getElement() == olderNode) {
                    totalWeight -= wn.getWeight();
                    nodesDistance.set(i, newWN);
                } else {
                    wn.setWeight(wn.getWeight()
                            - wn.getElement().getDistanceTo(olderNode)
                            + wn.getElement().getDistanceTo(newNode));
                    newNodeDistance += wn.getElement().getDistanceTo(newNode);
                    refreshWorstAndBest(wn);
                }
            }
            newWN.setWeight(newNodeDistance);
            totalWeight += newNodeDistance;
            refreshWorstAndBest(newWN);
        } else {
            for (int i = 0, n = nodes.size(); i < n; i++) {
                MDPNode node = nodes.get(i);
                if (node == olderNode) {
                    // The node being swapped out leaves with all its pairwise distances, so it must
                    // not contribute to the delta. Including it here overcounted by dist(older,new).
                    nodes.set(i, newNode);
                    continue;
                }
                totalWeight -= node.getDistanceTo(olderNode);
                totalWeight += node.getDistanceTo(newNode);
            }
        }
        nodesPresence[olderNode.getIndex()] = false;
        nodesPresence[newNode.getIndex()] = true;
    }

    public void removeNode(MDPNode node) {
        worstWNode = null;
        bestWNode = null;
        if (nodesDistance != null) {
            for (Iterator<Weighted<MDPNode>> it = nodesDistance.iterator(); it.hasNext(); ) {
                Weighted<MDPNode> wn = it.next();
                if (wn.getElement() == node) {
                    totalWeight -= wn.getWeight();
                    it.remove();
                } else {
                    wn.setWeight(wn.getWeight() - wn.getElement().getDistanceTo(node));
                    refreshWorstAndBest(wn);
                }
            }
        } else {
            for (Iterator<MDPNode> it = nodes.iterator(); it.hasNext(); ) {
                MDPNode n = it.next();
                totalWeight -= n.getDistanceTo(node);
                if (n == node) {
                    it.remove();
                }
            }
        }
        nodesPresence[node.getIndex()] = false;
    }

    public void addNode(MDPNode node) {
        worstWNode = null;
        bestWNode = null;
        if (nodesDistance != null) {
            double newNodeContribution = 0;
            for (Weighted<MDPNode> wn : nodesDistance) {
                wn.setWeight(wn.getWeight() + wn.getElement().getDistanceTo(node));
                newNodeContribution += wn.getElement().getDistanceTo(node);
                refreshWorstAndBest(wn);
            }
            Weighted<MDPNode> wn = new Weighted<>(node, newNodeContribution);
            nodesDistance.add(wn);
            refreshWorstAndBest(wn);
            totalWeight += newNodeContribution;
        } else {
            for (MDPNode n : nodes) {
                totalWeight += n.getDistanceTo(node);
            }
            nodes.add(node);
        }
        nodesPresence[node.getIndex()] = true;
    }

    public void removeNodesContribution() {
        this.nodes = new ArrayList<>();
        for (Weighted<MDPNode> wNode : nodesDistance) {
            nodes.add(wNode.getElement());
        }
        nodesDistance = null;
    }

    /** Copy all mutable state from another solution into this one (same instance). */
    public void asMDPSolution(MDPSolution solution) {
        totalWeight = solution.totalWeight;
        nodesDistance = solution.nodesDistance;
        nodesPresence = solution.nodesPresence;
        nodes = solution.nodes;
        bestWNode = solution.bestWNode;
        worstWNode = solution.worstWNode;
    }

    public double calculateDistanceWithoutNode(MDPNode ignoredNode, MDPNode newNode) {
        double contribution = 0;
        for (MDPNode n : this) {
            if (n != ignoredNode) {
                contribution += newNode.getDistanceTo(n);
            }
        }
        return contribution;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Weight: ").append(totalWeight).append("[");
        for (int i = 0; i < nodesPresence.length; i++) {
            if (nodesPresence[i]) {
                sb.append(i).append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public int getNumNodes() {
        if (nodes != null) {
            return nodes.size();
        } else {
            return nodesDistance.size();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MDPSolution solution) {
            return Arrays.equals(nodesPresence, solution.nodesPresence);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(nodesPresence);
    }

    public List<MDPNode> createNodeList() {
        List<MDPNode> result = new ArrayList<>();
        for (MDPNode node : this) {
            result.add(node);
        }
        return result;
    }

    public int[] createNodeIndexes() {
        int[] nodeIndexes = new int[getNumNodes()];
        int i = 0;
        for (MDPNode n : this) {
            nodeIndexes[i] = n.getIndex();
            i++;
        }
        return nodeIndexes;
    }

    public void calculatePreciseWeight() {
        MDPNode[] orderedNodes = new MDPNode[getNumNodes()];
        int counter = 0;
        for (int i = 0; i < nodesPresence.length; i++) {
            if (nodesPresence[i]) {
                orderedNodes[counter] = getInstance().getNode(i);
                counter++;
            }
        }
        double weight = 0;
        for (int i = 0; i < orderedNodes.length; i++) {
            for (int j = i + 1; j < orderedNodes.length; j++) {
                weight += orderedNodes[i].getDistanceTo(orderedNodes[j]);
            }
        }
        totalWeight = weight;
    }

    public int getRefSetPosition() {
        return refSetPosition;
    }

    public void setRefSetPosition(int refSetPosition) {
        this.refSetPosition = refSetPosition;
    }

    @Override
    public Iterator<MDPNode> iterator() {
        if (nodes != null) {
            return nodes.iterator();
        } else {
            return new WeightedIterator<>(nodesDistance.iterator());
        }
    }
}
