package es.urjc.etsii.grafo.flayouts.constructives.grasp;


import es.urjc.etsii.grafo.flayouts.model.*;

import java.util.Objects;

public class DRFPAddMove extends FLPMove {
    private final int position;
    private final int row;
    private final Facility facilityPosition;

    public DRFPAddMove(FLPSolution solution, int row, int position, int id, double cost) {
        super(solution, cost);
        this.row = row;
        this.position = position;
        this.facilityPosition = new Facility(id);
    }

    public DRFPAddMove(FLPSolution solution, int row, int position, int id) {
        double cost = solution.insertCost(row, position, facilityPosition);
        super(solution, cost);
        this.row = row;
        this.position = position;
        this.facilityPosition = new Facility(f);
        this.cost =
    }


    @Override
    protected FLPSolution _execute(FLPSolution solution) {
        solution.insert(row, position, cost, facilityPosition);
        return solution;
    }

    @Override
    public String toString() {
        return "DRFPAddMove{" +
                "c=" + cost +
                ", f=" + facilityPosition.facility.id +
                ", row=" + row +
                ", pos=" + position +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DRFPAddMove that = (DRFPAddMove) o;
        return Double.compare(that.cost, cost) == 0 && position == that.position && row == that.row && Objects.equals(facilityPosition, that.facilityPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cost, position, row, facilityPosition);
    }
}
