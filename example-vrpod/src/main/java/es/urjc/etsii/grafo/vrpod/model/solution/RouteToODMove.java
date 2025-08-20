package es.urjc.etsii.grafo.vrpod.model.solution;

public class RouteToODMove extends InOutMove {

    public RouteToODMove(VRPODSolution solution, int od, int customer, int normalDriver, int position, double odCost, double normalCost) {
        super(solution, od, customer, normalDriver, position, odCost, normalCost);
    }

    @Override
    public VRPODSolution _execute(VRPODSolution solution) {
        assert solution.attendedByType[this.customer] == VRPODSolution.DeliveryType.STORE_DRIVER;
//            assert DoubleComparator.isNegativeOrZero(this.normalCost);
//            assert DoubleComparator.isPositiveOrZero(this.odCost);
//            assert DoubleComparator.isNegativeOrZero(this.getCostDifference());

        int ods = solution.getUsedOds().size();

        VRPODSolution.Route r = solution.normalDrivers.get(this.normalDriver);
        solution.validate(r);
        solution.attendedByID[this.customer] = this.od;
        solution.attendedByType[this.customer] = VRPODSolution.DeliveryType.OCCASIONAL_DRIVER;
        solution.usedOds.add(this.od);
        r.nodes.remove(this.position);
        solution.ODattends[this.od] = this.customer;
        r.cost += this.normalCost;
        r.capacityLeft += solution.ins.getPacketSize(this.customer);
        solution.objValue += this.getValue();
        solution.validate(r);

        assert solution.getUsedOds().size() == ods + 1;
        return solution;
    }
}


