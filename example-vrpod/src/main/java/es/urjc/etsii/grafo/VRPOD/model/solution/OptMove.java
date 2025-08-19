package es.urjc.etsii.grafo.VRPOD.model.solution;

import es.urjc.etsii.grafo.util.CollectionUtil;

import java.util.Objects;

public class OptMove extends BaseMove {
    int driver;
    int position1;
    int position2;
    double costChangePosition1;
    double costChangePosition2;

    public OptMove(VRPODSolution solution, int driver, int position1, int position2, double costChangePosition1, double costChangePosition2) {
        super(solution);
        this.driver = driver;
        this.position1 = position1;
        this.position2 = position2;
        this.costChangePosition1 = costChangePosition1;
        this.costChangePosition2 = costChangePosition2;
    }

    @Override
    public VRPODSolution _execute(VRPODSolution solution) {
        asserts(solution);

        VRPODSolution.Route r = solution.normalDrivers.get(this.driver);
        solution.validate(r);

        CollectionUtil.reverseFragment(r.nodes, this.position1, this.position2);
        r.cost += this.getValue();
        solution.objValue += this.getValue();

        solution.validate(r);

        return solution;
    }

    public void asserts(VRPODSolution solution){
        int customer = solution.normalDrivers.get(driver).nodes.get(position1);
        assert solution.attendedByType[customer] == VRPODSolution.DeliveryType.STORE_DRIVER;
//            assert DoubleComparator.isNegativeOrZero(this.getCostDifference());
        assert solution.attendedByID[customer] == this.driver;
        assert position1 != position2;
    }

    @Override
    public double getValue() {
        return costChangePosition1 + costChangePosition2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptMove optMove = (OptMove) o;
        return driver == optMove.driver && position1 == optMove.position1 && position2 == optMove.position2 && Double.compare(optMove.costChangePosition1, costChangePosition1) == 0 && Double.compare(optMove.costChangePosition2, costChangePosition2) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(driver, position1, position2, costChangePosition1, costChangePosition2);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "driver=" + driver +
                ", position1=" + position1 +
                ", position2=" + position2 +
                ", costChangePosition1=" + costChangePosition1 +
                ", costChangePosition2=" + costChangePosition2 +
                '}';
    }
}
