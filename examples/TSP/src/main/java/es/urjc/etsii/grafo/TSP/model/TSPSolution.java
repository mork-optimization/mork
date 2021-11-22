package es.urjc.etsii.grafo.TSP.model;

import es.urjc.etsii.grafo.solution.Solution;

import java.util.Arrays;

public class TSPSolution extends Solution<TSPSolution, TSPInstance> {

    /**
     * Total length of the route
     */
    private double distance;

    /**
     * Circular route represented by an integer array:
     * I
     */
    private final int[] route;

    /**
     * Initialize solution from instance
     *
     * @param ins instance of the problem
     */
    public TSPSolution(TSPInstance ins) {
        super(ins);
        this.route = new int[ins.numberOfLocations()];
        Arrays.fill(route, -1);
        distance = -1;
    }

    /**
     * Clone constructor
     *
     * @param s Solution to clone
     */
    public TSPSolution(TSPSolution s) {
        super(s);
        this.route = s.route.clone();
        this.distance = s.distance;
    }


    @Override
    public TSPSolution cloneSolution() {
        return new TSPSolution(this);
    }

    @Override
    protected boolean _isBetterThan(TSPSolution other) {
        return this.distance < other.distance;
    }

    /**
     * Get the current solution score.
     * The difference between this method and recalculateScore is that
     * this result can be a property of the solution, or cached,
     * it does not have to be calculated each time this method is called
     *
     * @return current solution score as double
     */
    @Override
    public double getScore() {
        if (distance == -1) {
            return recalculateScore();
        } else {
            return this.distance;
        }
    }

    /**
     * Recalculate solution score and validate current solution state
     * You must check that no constraints are broken, and that all costs are valid
     * The difference between this method and getScore is that we must recalculate the score from scratch,
     * without using any cache/shortcuts.
     * DO NOT UPDATE CACHES / MAKE SURE THIS METHOD DOES NOT HAVE SIDE EFFECTS
     *
     * @return current solution score as double
     */
    @Override
    public double recalculateScore() {
        var distance = 0;
        for (int i = 0; i < this.route.length; i++) {
            var j = (i + 1) % this.route.length;
            distance += this.getInstance().getDistance(i, j);
        }
        this.distance = distance;
        return this.distance;
    }

    /**
     * Generate a string representation of this solution. Used when printing progress to console,
     * show as minimal info as possible
     *
     * @return Small string representing the current solution
     */
    @Override
    public String toString() {
        return Arrays.toString(this.route) + "\n" + "Score: " + this.distance;
    }
}
