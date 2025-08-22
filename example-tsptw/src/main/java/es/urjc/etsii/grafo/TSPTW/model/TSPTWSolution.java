package es.urjc.etsii.grafo.TSPTW.model;

import es.urjc.etsii.grafo.solution.Solution;

public class TSPTWSolution extends Solution<TSPTWSolution, TSPTWInstance> {

    /**
     * Initialize solution from instance
     *
     * @param instance
     */
    public TSPTWSolution(TSPTWInstance instance) {
        super(instance);
        // TODO Initialize solution data structures if necessary
    }

    /**
     * Clone constructor
     *
     * @param solution Solution to clone
     */
    public TSPTWSolution(TSPTWSolution solution) {
        super(solution);
        // TODO Copy ALL solution data, we are cloning a solution
        throw new UnsupportedOperationException("TSPTWSolution::new(TSPTWSolution) not implemented yet");
    }


    @Override
    public TSPTWSolution cloneSolution() {
        // You do not need to modify this method
        // Call clone constructor
        return new TSPTWSolution(this);
    }

    /**
     * Get the current solution score.
     * The difference between this method and recalculateScore is that
     * this result can be a property of the solution, or cached,
     * it does not have to be calculated each time this method is called
     *
     * @return current solution score as double
     */
    public double getScore() {
        // TODO: Implement efficient score calculation.
        // We recommend caching scores and updating them when the solution changes
        // Example: return this.score;
        throw new UnsupportedOperationException("TSPTWSolution:getScore not implemented yet");
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
        throw new UnsupportedOperationException("TSPTWSolution::toString not implemented yet");
    }

//    /**
//     * Optionally provide a way to calculate custom solution properties
//     * @return a map with the property names as keys,
//     * and a function to calculate the property value given the solution as value
//     */
//    @Override
//    public Map<String, Function<TSPTWSolution, Object>> customProperties() {
//        var properties = new HashMap<String, Function<TSPTWSolution, Object>>();
//        properties.put("myCustomPropertyName", s -> s.getScore());
//        properties.put("myCustomProperty2Name", TSPTWSolution::getScore);
//        return properties;
//    }
}
