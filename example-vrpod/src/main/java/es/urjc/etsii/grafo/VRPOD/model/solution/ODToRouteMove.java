package es.urjc.etsii.grafo.VRPOD.model.solution;

public class ODToRouteMove extends InOutMove {

    ODToRouteMove(VRPODSolution solution, int od, int customer, int normalDriver, int position, double odCost, double normalCost) {
        super(solution, od, customer, normalDriver, position, odCost, normalCost);
    }

    @Override
    public VRPODSolution _execute(VRPODSolution solution) {
        // Catch em all, this validate already fixed several bugs
        assert solution.attendedByType[this.customer] == VRPODSolution.DeliveryType.OCCASIONAL_DRIVER;
//            assert DoubleComparator.isPositiveOrZero(this.normalCost);
//            assert DoubleComparator.isNegativeOrZero(this.odCost);
//            assert DoubleComparator.isNegativeOrZero(this.getCostDifference());

        int ods = solution.getUsedOds().size();

        VRPODSolution.Route r = solution.normalDrivers.get(this.normalDriver);
        solution.validate(r);

        solution.attendedByID[this.customer] = this.normalDriver;
        solution.attendedByType[this.customer] = VRPODSolution.DeliveryType.STORE_DRIVER;
        r.nodes.add(this.position, this.customer);
        solution.ODattends[this.od] = VRPODSolution.NOT_ASSIGNED;
        solution.usedOds.remove(this.od);
        r.cost += this.normalCost;
        r.capacityLeft -= solution.ins.getPacketSize(this.customer);
        solution.objValue += this.getValue();
        solution.validate(r);

        assert ods == solution.getUsedOds().size() + 1;
        return solution;
    }
}