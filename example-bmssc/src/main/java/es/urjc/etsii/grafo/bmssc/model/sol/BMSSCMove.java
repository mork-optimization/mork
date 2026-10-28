package es.urjc.etsii.grafo.bmssc.model.sol;

import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.solution.Move;

public abstract class BMSSCMove extends Move<BMSSCSolution, BMSSCInstance> {
    private final double previousCost;

    protected BMSSCMove(BMSSCSolution solution) {
        super(solution);
        previousCost = solution.getCost();
    }

    protected final double roundedDelta(double delta) {
        // Report the change representable in the cached objective, including addition rounding.
        return (previousCost + delta) - previousCost;
    }

    public abstract double getCostDelta();
}
