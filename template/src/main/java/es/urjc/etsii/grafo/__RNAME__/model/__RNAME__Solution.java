package es.urjc.etsii.grafo.__RNAME__.model;

import es.urjc.etsii.grafo.solution.Solution;

public class __RNAME__Solution extends Solution<__RNAME__Instance> {

    /**
     * Initialize solution from instance
     * @param ins
     */
    public __RNAME__Solution(__RNAME__Instance ins) {
        super(ins);
        // TODO Initialize data structures if necessary
    }

    /**
     * Clone constructor
     * @param s Solution to clone
     */
    public __RNAME__Solution(__RNAME__Solution s) {
        super(s);
        // TODO Copy ALL solution data, we are cloning a solution
        throw new UnsupportedOperationException("__RNAME__Solution() in __RNAME__ not implemented yet");
    }


    @Override
    @SuppressWarnings("unchecked")
    public __RNAME__Solution cloneSolution() {
        // You do not need to modify this method
        // Call clone constructor
        return new __RNAME__Solution(this);
    }


    /**
     * Compare current solution against another. Return best one.
     * @param <S> Solution class
     * @param o Solution to compare
     * @return Best solution
     */
    @Override
    public <S extends Solution<__RNAME__Instance>> S getBetterSolution(S o) {
        // TODO given two solutions, which one is better?
        throw new UnsupportedOperationException("getBetterSolution() in __RNAME__ not implemented yet");
        // Example:
        // Minimize total cost, better solution has lower cost
        // return this.getScore() <= o.getScore() ? (S) this : o;
    }

    /**
     * Get the current solution score.
     * The difference between this method and recalculateScore is that
     * this result can be a property of the solution, or cached,
     * it does not have to be calculated each time this method is called
     * @return current solution score as double
     */
    @Override
    public double getScore() {
        // TODO: Implement efficient score calculation.
        // Can be as simple as a score property that gets updated when the solution changes
        // Example: return this.score;
        // Another ok start implementation can be: return recalculateScore();
        throw new UnsupportedOperationException("getScore() in __RNAME__ not implemented yet");
    }

    /**
     * Recalculate solution score and validate current solution state
     * You must check that no constraints are broken, and that all costs are valid
     * The difference between this method and getScore is that we must recalculate the score from scratch,
     * without using any cache/shortcuts.
     * DO NOT UPDATE CACHES / MAKE SURE THIS METHOD DOES NOT HAVE SIDE EFFECTS
     * @return current solution score as double
     */
    @Override
    public double recalculateScore() {
        // TODO calculate solution score from scratch, without using caches
        //  and without modifying the current solution. Careful with side effects.
        throw new UnsupportedOperationException("recalculateScore() in __RNAME__ not implemented yet");
    }

    /**
     * Generate a string representation of this solution. Used when printing progress to console,
     * show as minimal info as possible
     * @return Small string representing the current solution (Example: id + score)
     */
    @Override
    public String toString() {
        // TODO: When all fields are implemented use your IDE to autogenerate this method
        //  using only the most important fields.
        // This method will be called to print best solutions in console while solving.
        throw new UnsupportedOperationException("toString() in __RNAME__Solution not implemented yet");
    }
}
