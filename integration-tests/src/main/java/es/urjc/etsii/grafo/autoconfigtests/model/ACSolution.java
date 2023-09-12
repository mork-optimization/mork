package es.urjc.etsii.grafo.autoconfigtests.model;

import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.DoubleComparator;

public class ACSolution extends Solution<ACSolution, ACInstance> {

    double multiplier = 1;

    public ACSolution(ACInstance ins) {
        super(ins);
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
    protected boolean _isBetterThan(ACSolution other) {
        return DoubleComparator.isGreater(this.multiplier, other.multiplier);
    }

    @Override
    public double getScore() {
        return this.multiplier * getInstance().length();
    }

    @Override
    public double recalculateScore() {
        return this.multiplier * getInstance().length();
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
