package es.urjc.etsii.grafo.VRPOD.constructives;

import es.urjc.etsii.grafo.VRPOD.model.solution.VRPODSolution;
import es.urjc.etsii.grafo.VRPOD.model.instance.VRPODInstance;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.*;

import static es.urjc.etsii.grafo.util.DoubleComparator.*;

/**
 * Adaptation of GRASPConstructor in original VRPOD project, the rest of constructive methods are ignored as this is the only one that was not commented
 */
public class VRPODGRASPConstructive extends Reconstructive<VRPODSolution, VRPODInstance> {

    private final AlphaProvider alphaProvider;
    private final String randomType;

    /**
     * GRASP Constructor, mantains a fixed alpha value
     * @param alpha Randomness, adjusts the candidate list size.
     *                   Take values between [0,1] being 1 --> totally random, 0 --> full greedy.
     */
    @AutoconfigConstructor
    public VRPODGRASPConstructive(
            @RealParam(min = 0, max = 1) double alpha
    ){
        assert alpha >= 0d && alpha <= 1d;
        randomType = String.format("FIXED{a=%.2f}", alpha);
        alphaProvider = () -> alpha;
    }

    /**
     * GRASP Constructor, generates a random alpha in each construction
     * @param minAlpha minimum value for the random alpha
     * @param maxAlpha maximum value for the random alpha
     */
    public VRPODGRASPConstructive(double minAlpha, double maxAlpha){
        assert isPositiveOrZero(minAlpha) && isLessOrEquals(minAlpha, 1d);
        assert isPositiveOrZero(maxAlpha) && isLessOrEquals(maxAlpha, 1d);
        assert isGreater(maxAlpha, minAlpha);
        alphaProvider = () -> RandomManager.getRandom().nextDouble() * (maxAlpha - minAlpha) + minAlpha;
        randomType = String.format("RANGE{min=%.2f, max=%.2f}", minAlpha, maxAlpha);
    }

    /**
     * GRASP Constructor, generates a random alpha in each construction, between 0 and 1 (inclusive).
     */
    public VRPODGRASPConstructive(){
        this(0, 1);
    }

    @Override
    public VRPODSolution reconstruct(VRPODSolution solution) {
        return assignMissingClients(solution);
    }

    private int getLimit(List<Assign> candidateList, double umbral){
        int indexTope;
        for (indexTope = 0; indexTope < candidateList.size(); indexTope++) {
            // TODO busqueda binaria
            if (isPositive(candidateList.get(indexTope).cost - umbral))
                break;
        }
        return indexTope;
    }

    @Override
    public VRPODSolution construct(VRPODSolution solution) {
        return assignMissingClients(solution);
    }

    public VRPODSolution assignMissingClients(VRPODSolution sol) {
        var r = RandomManager.getRandom();
        List<Assign> cl = generateCandidateList(sol);
        double alpha = alphaProvider.getAlpha();
        while (!cl.isEmpty()) {
            double min = cl.get(0).cost;
            double max = cl.get(cl.size() - 1).cost;
            double umbral = min + (alpha) * (max - min);

            int indexTope = getLimit(cl, umbral);

            Assign chosen = cl.get(r.nextInt(indexTope));

            if(chosen.isOcasional){
                sol.assignOD(chosen.driver, chosen.customer, chosen.cost);
            } else {
                sol.assignNormal(chosen.driver, chosen.position, chosen.customer, chosen.cost);
            }
            sol.notifyUpdate();

            cl = generateCandidateList(sol);

            // Catch bugs while building the solution
            // no-op if running in performance mode, triggers score recalculation if debugging
            assert isPositiveOrZero(sol.getOptimalValue());
        }
        if(!sol.getUnassignedClients().isEmpty()){
            throw new IllegalStateException("Finished assigning clients but there are unassigned clients");
        }
        return sol;
    }

    @SuppressWarnings("Duplicates")
    private List<Assign> generateCandidateList(VRPODSolution sol) {

        Set<Integer> unassignedClients = sol.getUnassignedClients();
        // Presize array
        List<Assign> candidateList = new ArrayList<>(unassignedClients.size() * unassignedClients.size());

        for(int client : unassignedClients){
            for(int driver: sol.getAvailableNormalDriversFor(client)){
                var normalDrivers = sol.getNormalDrivers();
                if(driver >= normalDrivers.size()){
                    candidateList.add(new Assign(driver, 0, client, sol.getInsertCost(driver, 0, client)));
                } else {
                    for (int i = 0; i <= normalDrivers.get(driver).getNodes().size(); i++) {
                        candidateList.add(new Assign(driver, i, client, sol.getInsertCost(driver, i, client)));
                    }
                }
            }
            for(int odDriver: sol.getAvailableODDriversFor(client)){
                candidateList.add(new Assign(odDriver, client, sol.getAssignCostOD(odDriver, client)));
            }
        }

        Collections.sort(candidateList);
        return candidateList;
    }


    @Override
    public String toString() {
        return "GRASPConstructor{" +
                "alpha=" + randomType +
                '}';
    }

    private class Assign implements Comparable<Assign> {

        boolean isOcasional;
        int driver;
        int position;
        int customer;
        double cost;

        /**
         * Occassional driver assign constructor
         * @param ODdriver
         * @param customer
         * @param cost
         */
        Assign(int ODdriver, int customer, double cost) {
            this.driver = ODdriver;
            this.isOcasional = true;
            this.customer = customer;
            this.cost = cost;
        }

        /**
         * Normal route assign constructor
         * @param normalDriver
         * @param position
         * @param customer
         * @param cost
         */
        Assign(int normalDriver, int position, int customer, double cost) {
            this.driver = normalDriver;
            this.position = position;
            this.isOcasional = false;
            this.customer = customer;
            this.cost = cost;
        }

        @Override
        public int compareTo(Assign assign) {
            return Double.compare(cost, assign.cost);
        }

        @Override
        public String toString() {
            return "Assign{" +
                    "isOcasional=" + isOcasional +
                    ", driver=" + driver +
                    (!isOcasional? (", position=" + position):"") +
                    ", customer=" + customer +
                    ", cost=" + cost +
                    '}';
        }
    }

    interface AlphaProvider {
        double getAlpha();
    }
}
