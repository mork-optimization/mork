package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.solution.Move;

public abstract class FLPMove extends Move<FLPSolution, FLPInstance> {

    final double score;

    public FLPMove(FLPSolution s, double score) {
        super(s);
        this.score = score;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+"{" +
                ", sc=" + score +
                '}';
    }

    public double getDelta() {
        return score;
    }

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();
}
