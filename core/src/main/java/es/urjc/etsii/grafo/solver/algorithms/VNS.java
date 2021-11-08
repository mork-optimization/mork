package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.destructor.Shake;
import es.urjc.etsii.grafo.solver.improve.Improver;
import es.urjc.etsii.grafo.solver.services.MorkLifecycle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class VNS<S extends Solution<I>, I extends Instance> extends Algorithm<S, I> {

    private static final Logger log = Logger.getLogger(VNS.class.getName());
    protected final String algorithmName;

    protected List<Improver<S, I>> improvers;
    protected Constructive<S, I> constructive;
    protected List<Shake<S, I>> shakes;
    protected KProvider<I> kProvider;

    /**
     * Execute VNS until finished
     * @param algorithmName Algorithm name, example: "VNSWithRandomConstructive"
     * @param kProvider k value provider, @see VNS.KProvider
     * @param shake Perturbation method
     * @param constructive Constructive method
     * @param improvers List of improvers/local searches
     */
    @SafeVarargs
    public VNS(String algorithmName, KProvider<I> kProvider, Shake<S, I> shake, Constructive<S, I> constructive, Improver<S, I>... improvers) {
        this(algorithmName, kProvider, Collections.singletonList(shake), constructive, improvers);
    }

    /**
     * Execute VNS until finished
     * @param algorithmName Algorithm name, example: "VNSWithRandomConstructive"
     * @param kProvider k value provider, @see VNS.KProvider
     * @param shakes Perturbation method
     * @param constructive Constructive method
     * @param improvers List of improvers/local searches
     */
    @SafeVarargs
    public VNS(String algorithmName, KProvider<I> kProvider, List<Shake<S, I>> shakes, Constructive<S, I> constructive, Improver<S, I>... improvers) {
        this.algorithmName = algorithmName;
        this.kProvider = kProvider;

        // Ensure Ks are sorted, maxK is the last element
        this.shakes = shakes;
        this.constructive = constructive;
        this.improvers = Arrays.asList(improvers);
    }

    public S algorithm(I instance) {
        var solution = this.newSolution(instance);
        solution = constructive.construct(solution);
        solution = localSearch(solution);

        int currentKIndex = 0;
        // While stop not request OR k in range. k check is done and breaks inside loop
        while (!MorkLifecycle.stop()) {
            int currentK = kProvider.getK(instance, currentKIndex);
            if(currentK == KProvider.STOPNOW){
                printStatus(currentKIndex + ":STOPNOW", solution);
                break;
            }
            printStatus(currentKIndex + ":" + currentK, solution);
            S bestSolution = solution;

            for(var shake: shakes){
                S copy = bestSolution.cloneSolution();
                copy = shake.shake(copy, currentK);
                copy = localSearch(copy);
                //System.out.print(copy.getOptimalValue()+",");
                bestSolution = bestSolution.getBetterSolution(copy);
            }
            if (bestSolution == solution) {
                currentKIndex++;
            } else {
                solution = bestSolution;
                currentKIndex = 0;
            }
        }
        return solution;
    }

    private S localSearch(S solution) {
        for (Improver<S, I> ls : improvers) {
            solution = ls.improve(solution);
        }
        return solution;
    }

    private void printStatus(String phase, S s) {
        log.fine(String.format("\t\t\t%s: %s\n", phase, s));
    }

    @Override
    public String toString() {
        return "VNS{" +
                "improvers=" + improvers +
                ", constructive=" + constructive +
                ", shakes=" + shakes +
                ", kprov=" + kProvider +
                '}';
    }

    @Override
    public String getShortName() {
        return this.algorithmName;
    }

    /**
     * Calculates K value for each VNS step.
     */
    public interface KProvider<I extends Instance> {
        int STOPNOW = -1;

        /**
         * Calculate K value during VNS execution.
         * @param instance Current instance, provided as a parameter so K can be adapted or scaled to instance size.
         * @param kIndex Current k strength. Starts in 0 and increments by 1 each time the solution does not improve.
         * @return K value. Return KProvider.STOPNOW to stop when calculated K is greater than max K, and the VNS should terminate
         */
        int getK(I instance, int kIndex);
    }
}
