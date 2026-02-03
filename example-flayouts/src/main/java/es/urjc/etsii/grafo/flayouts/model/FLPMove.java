package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.solution.Move;

public abstract class FLPMove extends Move<FLPSolution, FLPInstance> {

    protected final double delta;

    public FLPMove(FLPSolution s, double delta) {
        super(s);
        this.delta = delta;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+"{" +
                ", sc=" + delta +
                '}';
    }

    public double delta() {
        return delta;
    }

    @Override
    public abstract boolean equals(Object o);

    @Override
    public abstract int hashCode();
}
