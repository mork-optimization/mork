package es.urjc.etsii.grafo.VRPOD.model.solution;

import java.util.Objects;

public class InsertMove extends BaseMove {
    int customer;
    int driver1;
    int driver2;
    double costChangeRoute1;
    double costChangeRoute2;
    int position1;
    int position2;

    public InsertMove(VRPODSolution solution, int customer, int driver1, int position1, double costChangeRoute1, int driver2, int position2, double costChangeRoute2) {
        super(solution);
        this.customer = customer;
        this.driver1 = driver1;
        this.driver2 = driver2;
        this.costChangeRoute1 = costChangeRoute1;
        this.costChangeRoute2 = costChangeRoute2;
        this.position1 = position1;
        this.position2 = position2;

        // VALIDATION: If same route and same position -> cost must be 0
        assert this.driver1 != this.driver2 || this.position1 != this.position2 || this.getValue() == 0;
    }

    @Override
    public double getValue() {
        return costChangeRoute1 + costChangeRoute2;
    }

    @Override
    public VRPODSolution _execute(VRPODSolution solution) {

        assert solution.attendedByType[this.customer] == VRPODSolution.DeliveryType.STORE_DRIVER;
//            assert DoubleComparator.isNegativeOrZero(this.costChangeRoute1);
//            assert DoubleComparator.isPositiveOrZero(this.costChangeRoute2);
//            assert DoubleComparator.isNegativeOrZero(this.getCostDifference());
        assert solution.attendedByID[this.customer] == this.driver1;

        solution.attendedByID[this.customer] = this.driver2;
        VRPODSolution.Route r1 = solution.normalDrivers.get(this.driver1);
        VRPODSolution.Route r2 = solution.normalDrivers.get(this.driver2);
        solution.validate(r1, r2);
        r1.nodes.remove(this.position1);
        r2.nodes.add(this.position2, this.customer);
        double packetSize = solution.ins.getPacketSize(this.customer);
        r1.capacityLeft += packetSize;
        r2.capacityLeft -= packetSize;
        r1.cost += this.costChangeRoute1;
        r2.cost += this.costChangeRoute2;
        solution.objValue += this.getValue();
        solution.validate(r1, r2);

        return solution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InsertMove that = (InsertMove) o;
        return customer == that.customer && driver1 == that.driver1 && driver2 == that.driver2 && Double.compare(that.costChangeRoute1, costChangeRoute1) == 0 && Double.compare(that.costChangeRoute2, costChangeRoute2) == 0 && position1 == that.position1 && position2 == that.position2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(customer, driver1, driver2, costChangeRoute1, costChangeRoute2, position1, position2);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "customer=" + customer +
                ", driver1=" + driver1 +
                ", driver2=" + driver2 +
                ", costChangeRoute1=" + costChangeRoute1 +
                ", costChangeRoute2=" + costChangeRoute2 +
                ", position1=" + position1 +
                ", position2=" + position2 +
                '}';
    }
}