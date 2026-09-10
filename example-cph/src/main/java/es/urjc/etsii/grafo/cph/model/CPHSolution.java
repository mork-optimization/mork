package es.urjc.etsii.grafo.cph.model;

import es.urjc.etsii.grafo.solution.Solution;

import java.util.ArrayList;
import java.util.List;

/**
 * A CPH solution: the set of open hubs plus, for every client, the hub that serves it
 * ({@code spokes[i]} is the index of the hub serving node {@code i}, or {@code -1} if node
 * {@code i} is itself a hub). The score is the total client-to-hub distance (minimized).
 *
 * <p>The swap operations ({@link #isHub}, {@link #scoreAfterChangeHub}, {@link #changeHub})
 * mirror the {@code jmh} solution and are the neighbourhood used by the custom local
 * searches.
 */
public class CPHSolution extends Solution<CPHSolution, CPHInstance> {

    private List<Integer> hubs;
    // spokes[i] = index of the hub serving node i, or -1 if node i is a hub
    private int[] spokes;
    private double score;

    public CPHSolution(CPHInstance instance) {
        super(instance);
        this.hubs = new ArrayList<>();
        this.spokes = new int[instance.getNumNodes()];
        this.score = 0;
    }

    public CPHSolution(CPHSolution s) {
        super(s);
        this.hubs = new ArrayList<>(s.hubs);
        this.spokes = s.spokes.clone();
        this.score = s.score;
    }

    @Override
    public CPHSolution cloneSolution() {
        return new CPHSolution(this);
    }

    /** Cached objective value, used by the Objective. */
    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    /** Recompute the score from scratch (used by the constructive and the validator). */
    public double recalculateScore() {
        return computeWeight(this.hubs, this.spokes);
    }

    private double computeWeight(List<Integer> solutionHubs, int[] solutionSpokes) {
        int weight = 0;
        var instance = getInstance();
        for (int hub : solutionHubs) {
            for (int i = 0; i < solutionSpokes.length; i++) {
                if (solutionSpokes[i] == hub) {
                    weight += instance.getDistance(hub, i);
                }
            }
        }
        return weight;
    }

    // ---- state used by the constructive ----

    public void setHubsAndSpokes(List<Integer> hubs, int[] spokes) {
        this.hubs = hubs;
        this.spokes = spokes;
    }

    public List<Integer> getHubs() {
        return hubs;
    }

    // ---- swap neighbourhood (mirrors jmh) ----

    public boolean isHub(int node) {
        return hubs.contains(node);
    }

    /** Objective value that would result from replacing {@code oldHub} with {@code newHub}. */
    public double scoreAfterChangeHub(int oldHub, int newHub) {
        List<Integer> newHubs = new ArrayList<>(hubs.size());
        for (int hub : hubs) {
            newHubs.add(hub == oldHub ? newHub : hub);
        }

        int[] newSpokes = new int[spokes.length];
        for (int i = 0; i < spokes.length; i++) {
            newSpokes[i] = spokes[i] == oldHub ? newHub : spokes[i];
        }
        newSpokes[oldHub] = newHub;
        newSpokes[newHub] = -1;

        return computeWeight(newHubs, newSpokes);
    }

    /** Apply the replacement of {@code oldHub} by {@code newHub}, updating the cached score. */
    public void changeHub(int oldHub, int newHub) {
        for (int i = 0; i < hubs.size(); i++) {
            if (hubs.get(i) == oldHub) {
                hubs.set(i, newHub);
                break;
            }
        }
        for (int i = 0; i < spokes.length; i++) {
            if (spokes[i] == oldHub) {
                spokes[i] = newHub;
            }
        }
        spokes[oldHub] = newHub;
        spokes[newHub] = -1;
        this.score = computeWeight(hubs, spokes);
    }

    @Override
    public String toString() {
        List<List<Integer>> hubsWithClients = new ArrayList<>();
        for (int hub : hubs) {
            List<Integer> elements = new ArrayList<>();
            elements.add(hub);
            for (int i = 0; i < spokes.length; i++) {
                if (spokes[i] == hub) {
                    elements.add(i);
                }
            }
            hubsWithClients.add(elements);
        }
        return "Score: " + score + " " + hubsWithClients;
    }
}
