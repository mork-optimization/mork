package es.urjc.etsii.grafo.VRPOD.destructives;

import es.urjc.etsii.grafo.VRPOD.model.solution.VRPODSolution;
import es.urjc.etsii.grafo.VRPOD.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.shake.Destructive;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.List;

public class IntelligentRouteDestructor extends Destructive<VRPODSolution, VRPODInstance> {

    @AutoconfigConstructor
    public IntelligentRouteDestructor() {}

    private void destroyOneRoute(VRPODSolution s) {
        var routes = s.getNormalDrivers();
        double[] accumulated = getDistribution(routes);

        // Tiramo la ruleta
        double random = RandomManager.getRandom().nextDouble();
        int i = 0;
        // Binary search seria mas rapido pero no interesa si tiene pocos elementos
        while (accumulated[i] <= random) {
            i++;
        }

        while(routes.get(i+1).getNodes().size()>0){
            int node = routes.get(i + 1).getNodes().get(0);
            s.deassign(node);
        }
    }

    private double[] getDistribution(List<VRPODSolution.Route> routes) {
        double[] ratio = new double[routes.size()-1];
        double total = 0;
        for (int i = 1; i < routes.size(); i++) {
            VRPODSolution.Route r = routes.get(i);
            ratio[i-1] = r.getCost() / r.getNumberOfClients();
            total += ratio[i-1];
        }

        double[] accumulated = new double[ratio.length];
        double acc = 0;
        for (int i = 0; i < accumulated.length; i++) {
            acc += ratio[i] / total;
            accumulated[i] = acc;
        }
        return accumulated;
    }

    @Override
    public String toString() {
        return "IntelligentRouteDestructor{}";
    }

    @Override
    public VRPODSolution destroy(VRPODSolution solution, int k) {
        int routesToDestroy = Math.min(k, solution.getNumberOfNormalDrivers()-1);
        for (int i = 0; i < routesToDestroy; i++) {
            // Destroys k routes, with 0 <= k <= 3
            destroyOneRoute(solution);
        }
        return solution;
    }
}
