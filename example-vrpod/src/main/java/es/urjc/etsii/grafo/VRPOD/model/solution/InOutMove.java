package es.urjc.etsii.grafo.VRPOD.model.solution;

import java.util.Objects;

public abstract class InOutMove extends BaseMove {
    int od;
    int customer;
    int normalDriver;
    int position;
    double odCost;
    double normalCost;

    public InOutMove(VRPODSolution solution, int od, int customer, int normalDriver, int position, double odCost, double normalCost) {
        super(solution);
        assert odCost != Double.MAX_VALUE;
        assert normalCost != Double.MAX_VALUE;
        this.od = od;
        this.customer = customer;
        this.normalDriver = normalDriver;
        this.position = position;
        this.normalCost = normalCost;
        this.odCost = odCost;
    }

    @Override
    public double getValue() {
        return odCost + normalCost;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InOutMove inOutMove = (InOutMove) o;
        return od == inOutMove.od && customer == inOutMove.customer && normalDriver == inOutMove.normalDriver && position == inOutMove.position && Double.compare(inOutMove.odCost, odCost) == 0 && Double.compare(inOutMove.normalCost, normalCost) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(od, customer, normalDriver, position, odCost, normalCost);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "od=" + od +
                ", customer=" + customer +
                ", normalDriver=" + normalDriver +
                ", position=" + position +
                ", odCost=" + odCost +
                ", normalCost=" + normalCost +
                '}';
    }
}


