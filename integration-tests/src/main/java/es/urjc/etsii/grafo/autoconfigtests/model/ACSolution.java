package es.urjc.etsii.grafo.autoconfigtests.model;

import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.DoubleComparator;

public class ACSolution extends Solution<ACSolution, ACInstance> {

    double score;

    public ACSolution(ACInstance ins) {
        super(ins);
    }

    public ACSolution(ACSolution other) {
        super(other);
        this.score = other.score;
    }

    @Override
    public ACSolution cloneSolution() {
        return new ACSolution(this);
    }

    @Override
    protected boolean _isBetterThan(ACSolution other) {
        return DoubleComparator.isGreater(this.score, other.score);
    }

    @Override
    public double getScore() {
        return this.score;
    }

    @Override
    public double recalculateScore() {
        return this.score;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "score=" + score +
                '}';
    }

    public void setScore(double score) {
        this.score = score;
    }
}
