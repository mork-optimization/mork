package es.urjc.etsii.grafo.VRPOD.model.instance;

import java.util.HashMap;
import java.util.Map;

public class VRPODInstanceProfitODDependant extends VRPODInstance {
    /**
     * For each occasional drive we store the benefit if they deliver the packet to the client
     * Ex: odsCustomersbenefit[driverId].get(customerId) returns the profit if the driver delivers to that customer
     */
    private Map<Integer,Double>[] odsCustomersBenefit;

    public VRPODInstanceProfitODDependant(String name, int numOccasionalDrivers, int capacity, int numberOfDestinations) {
        super(name, numOccasionalDrivers, capacity, numberOfDestinations);
        odsCustomersBenefit = new HashMap[numOccasionalDrivers +1];
        for (int i = 1; i < odsCustomersBenefit.length; i++) {
            odsCustomersBenefit[i] = new HashMap<>();
        }
    }

    @Override
    void setProfit(int od, int customer, double value) {
        this.odsCustomersBenefit[od].put(customer, value);
    }

    @Override
    public double getCost(int od, int customer) {
        return odsCustomersBenefit[od].get(customer);
    }
}
