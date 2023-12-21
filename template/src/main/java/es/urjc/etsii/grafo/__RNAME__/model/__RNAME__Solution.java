package es.urjc.etsii.grafo.__RNAME__.model;

import es.urjc.etsii.grafo.solution.Solution;

public class __RNAME__Solution extends Solution<__RNAME__Solution, __RNAME__Instance> {

    /**
     * Initialize solution from instance
     *
     * @param ins
     */
    public __RNAME__Solution(__RNAME__Instance ins) {
        super(ins);
        // TODO Initialize solution data structures if necessary
    }

    /**
     * Clone constructor
     *
     * @param s Solution to clone
     */
    public __RNAME__Solution(__RNAME__Solution s) {
        super(s);
        // TODO Copy ALL solution data, we are cloning a solution
        throw new UnsupportedOperationException("__RNAME__Solution::new(__RNAME__Solution) not implemented yet");
    }


    @Override
    public __RNAME__Solution cloneSolution() {
        // You do not need to modify this method
        // Call clone constructor
        return new __RNAME__Solution(this);
    }

    @Override
    protected boolean _isBetterThan(__RNAME__Solution other) {
        // TODO given two solutions, is the current solution STRICTLY better than the other?
        // Example implementation for a maximization problem:
        // return DoubleComparator.isGreater(this.getScore(), other.getScore());
        throw new UnsupportedOperationException("__RNAME__Solution::isBetterThan not implemented yet");
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
        // The following implementation is NOT efficient but gets you started quickly
        return recalculateScore();
    }

    /**
     * Recalculate solution score from scratch, using the problem objective function.
     * The difference between this method and getScore is that we must recalculate the score from scratch,
     * without using any cache/shortcuts.
     * This method will be used to validate the correct behaviour of the getScore() method, and to help catch
     * bugs or mistakes when changing incremental score calculation.
     * DO NOT UPDATE CACHES IN THIS METHOD / MAKE SURE THIS METHOD DOES NOT HAVE SIDE EFFECTS
     * DO NOT UPDATE CACHES IN THIS METHOD / MAKE SURE THIS METHOD DOES NOT HAVE SIDE EFFECTS
     * and once more
     * DO NOT UPDATE CACHES IN THIS METHOD / MAKE SURE THIS METHOD DOES NOT HAVE SIDE EFFECTS
     *
     * @return current solution score as double
     */
    @Override
    public double recalculateScore() {
        // TODO calculate solution score from scratch without modifying the current solution.
        //  Be careful with side effects.
        throw new UnsupportedOperationException("__RNAME__Solution:recalculateScore not implemented yet");
    }

    /**
     * Generate a string representation of this solution. Used when printing progress to console,
     * show as minimal info as possible
     *
     * @return Small string representing the current solution (Example: id + score)
     */
    @Override
    public String toString() {
        // TODO: When all fields are implemented delete this method and use your IDE
        //  to autogenerate it using only the most important fields.
        // This method will be called to print best solutions in console while solving, and by your IDE when debugging
        // WARNING: DO NOT UPDATE CACHES IN THIS METHOD / MAKE SURE THIS METHOD DOES NOT HAVE SIDE EFFECTS
        // Calling toString to a solution should NEVER change or update any of its fields
        throw new UnsupportedOperationException("__RNAME__Solution::toString not implemented yet");
    }

//    /**
//     * Optionally provide a way to calculate custom solution properties
//     * @return a map with the property names as keys,
//     * and a function to calculate the property value given the solution as value
//     */
//    @Override
//    public Map<String, Function<__RNAME__Solution, Object>> customProperties() {
//        var properties = new HashMap<String, Function<__RNAME__Solution, Object>>();
//        properties.put("myCustomPropertyName", s -> s.getScore());
//        properties.put("myCustomProperty2Name", __RNAME__Solution::getScore);
//        return properties;
//    }
}
