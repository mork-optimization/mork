package es.urjc.etsii.grafo.autoconfigtests.model;

import es.urjc.etsii.grafo.solution.Solution;

/**
 * Test solution to validate autoconfig behaviour, each component will add or remove some arbitrary quantity from its score
 */
public class ACSolution extends Solution<ACSolution, ACInstance> {

    double multiplier = 1;

    /**
     * Build a new solution to validate the autoconfig behaviour
     * @param instance test instance
     */
    public ACSolution(ACInstance instance) {
        super(instance);
    }

    public ACSolution(ACSolution other) {
        super(other);
        this.multiplier = other.multiplier;
    }

    @Override
    public ACSolution cloneSolution() {
        return new ACSolution(this);
    }

    @Override
    public double getScore() {
        return Math.max(0, this.multiplier * getInstance().length());
    }

    @Override
    public double recalculateScore() {
        return Math.max(0, this.multiplier * getInstance().length());
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "score=" + getScore() +
                '}';
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return multiplier;
    }
}
