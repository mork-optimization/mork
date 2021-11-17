package es.urjc.etsii.grafo.TSP.model;

import es.urjc.etsii.grafo.solution.Solution;

public class TSPSolution extends Solution<TSPSolution, TSPInstance> {

    /**
     * Initialize solution from instance
     *
     * @param ins
     */
    public TSPSolution(TSPInstance ins) {
        super(ins);
        // TODO Initialize data structures if necessary
    }

    /**
     * Clone constructor
     *
     * @param s Solution to clone
     */
    public TSPSolution(TSPSolution s) {
        super(s);
        // TODO Copy ALL solution data, we are cloning a solution
        throw new UnsupportedOperationException("TSPSolution() in TSP not implemented yet");
    }


    @Override
    public TSPSolution cloneSolution() {
        // You do not need to modify this method
        // Call clone constructor
        return new TSPSolution(this);
    }

    @Override
    protected boolean _isBetterThan(TSPSolution other) {
        // TODO given two solutions, is the current solution STRICTLY better than the other?
        throw new UnsupportedOperationException("isBetterThan() in TSP not implemented yet");
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
        // TODO: Implement efficient score calculation.
        // Can be as simple as a score property that gets updated when the solution changes
        // Example: return this.score;
        // Another ok start implementation can be: return recalculateScore();
        throw new UnsupportedOperationException("getScore() in TSP not implemented yet");
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
        // TODO calculate solution score from scratch, without using caches
        //  and without modifying the current solution. Careful with side effects.
        throw new UnsupportedOperationException("recalculateScore() in TSP not implemented yet");
    }

    /**
     * Generate a string representation of this solution. Used when printing progress to console,
     * show as minimal info as possible
     *
     * @return Small string representing the current solution (Example: id + score)
     */
    @Override
    public String toString() {
        // TODO: When all fields are implemented use your IDE to autogenerate this method
        //  using only the most important fields.
        // This method will be called to print best solutions in console while solving.
        throw new UnsupportedOperationException("toString() in TSPSolution not implemented yet");
    }
}
