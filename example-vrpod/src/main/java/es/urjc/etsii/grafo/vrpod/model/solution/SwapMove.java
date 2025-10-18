package es.urjc.etsii.grafo.vrpod.model.solution;

import java.util.Objects;

public class SwapMove extends BaseMove {

    int driver1;
    int driver2;
    int position1;
    int position2;
    double costChangePosition1;
    double costChangePosition2;

    public SwapMove(VRPODSolution solution, int driver1, int position1, double costChangePosition1, int driver2, int position2, double costChangePosition2) {
        super(solution);
        this.driver1 = driver1;
        this.position1 = position1;
        this.driver2 = driver2;
        this.position2 = position2;
        this.costChangePosition1 = costChangePosition1;
        this.costChangePosition2 = costChangePosition2;
    }

    @Override
    public VRPODSolution _execute(VRPODSolution solution) {
        VRPODSolution.Route r1 = solution.normalDrivers.get(this.driver1);
        int customer1 = r1.nodes.get(position1);
        VRPODSolution.Route r2 = solution.normalDrivers.get(this.driver2);
        int customer2 = r2.nodes.get(position2);

        assert solution.attendedByType[customer1] == VRPODSolution.DeliveryType.STORE_DRIVER;
        assert solution.attendedByType[customer2] == VRPODSolution.DeliveryType.STORE_DRIVER;
        assert solution.attendedByID[customer1] == this.driver1;
        assert solution.attendedByID[customer2] == this.driver2;
//            assert DoubleComparator.isNegativeOrZero(this.getCostDifference());
        solution.validate(r1, r2);


        r1.nodes.set(this.position1, customer2);
        r1.cost += this.costChangePosition1;
        r1.capacityLeft += solution.ins.getPacketSize(customer1);
        r1.capacityLeft -= solution.ins.getPacketSize(customer2);
        solution.attendedByID[customer1] = driver2;

        r2.nodes.set(this.position2, customer1);
        r2.cost += this.costChangePosition2;
        r2.capacityLeft += solution.ins.getPacketSize(customer2);
        r2.capacityLeft -= solution.ins.getPacketSize(customer1);
        solution.attendedByID[customer2] = driver1;

        solution.objValue += this.getValue();

        assert solution.attendedByID[customer1] == this.driver2;
        assert solution.attendedByID[customer2] == this.driver1;
        solution.validate(r1, r2);

        return solution;
    }

    @Override
    public double getValue() {
        return costChangePosition1 + costChangePosition2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SwapMove swapMove = (SwapMove) o;
        return driver1 == swapMove.driver1 && driver2 == swapMove.driver2 && position1 == swapMove.position1 && position2 == swapMove.position2 && Double.compare(swapMove.costChangePosition1, costChangePosition1) == 0 && Double.compare(swapMove.costChangePosition2, costChangePosition2) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(driver1, driver2, position1, position2, costChangePosition1, costChangePosition2);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "driver1=" + driver1 +
                ", driver2=" + driver2 +
                ", position1=" + position1 +
                ", position2=" + position2 +
                ", costChangePosition1=" + costChangePosition1 +
                ", costChangePosition2=" + costChangePosition2 +
                '}';
    }
}

