package es.urjc.etsii.grafo.VRPOD.model.instance;

class VRPODInstanceProfitODIndependant extends VRPODInstance {

    /**
     * Benefit of the occasional driver when it does not deppend on the distance or the od. @see odsCustomersBenefit. Indexed by ocassional driver id
     */
    private double[] profit;

    public VRPODInstanceProfitODIndependant(String name, int numOccasionalDrivers, int capacity, int numberOfDestinations) {
        super(name, numOccasionalDrivers, capacity, numberOfDestinations);
        this.profit = new double[numberOfDestinations];
    }

    @Override
    void setProfit(int od, int customer, double value) {
        this.profit[customer] = value;
    }

    @Override
    public double getCost(int od, int customer) {
        return this.profit[customer];
    }
}
