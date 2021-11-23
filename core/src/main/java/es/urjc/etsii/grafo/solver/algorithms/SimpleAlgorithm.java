package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.improve.Improver;
import es.urjc.etsii.grafo.util.ValidationUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Example simple algorithm, executes:
 * Constructive → (Optional, if present) Local Searches → (Optional, if present) Shake → If did not improve end
 *                                               ^_________________________________________|   else repeat
 * This class can be used to test all the pieces if they are working properly, or as a base for more complex algorithms
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class SimpleAlgorithm<S extends Solution<S,I>, I extends Instance> extends Algorithm<S,I>{

    private static Logger log = Logger.getLogger(SimpleAlgorithm.class.getName());

    protected final Constructive<S,I> constructive;
    protected final List<Improver<S, I>> improvers;
    protected final String algorithmName;

    @SafeVarargs
    /**
     * <p>Constructor for SimpleAlgorithm.</p>
     *
     * @param constructive a {@link es.urjc.etsii.grafo.solver.create.Constructive} object.
     * @param improvers a {@link es.urjc.etsii.grafo.solver.improve.Improver} object.
     */
    public SimpleAlgorithm(Constructive<S, I> constructive, Improver<S,I>... improvers){
        this("", constructive, improvers);
    }

    @SafeVarargs
    /**
     * <p>Constructor for SimpleAlgorithm.</p>
     *
     * @param algorithmName a {@link java.lang.String} object.
     * @param constructive a {@link es.urjc.etsii.grafo.solver.create.Constructive} object.
     * @param improvers a {@link es.urjc.etsii.grafo.solver.improve.Improver} object.
     */
    public SimpleAlgorithm(String algorithmName, Constructive<S, I> constructive, Improver<S,I>... improvers){
        this.algorithmName = algorithmName.trim();
        this.constructive = constructive;
        if(improvers != null && improvers.length >= 1){
            this.improvers = Arrays.asList(improvers);
        } else {
            this.improvers = new ArrayList<>();
        }
    }

    /**
     * {@inheritDoc}
     *
     * Algorithm: Execute a single construction and then all the local searchs a single time.
     */
    @Override
    public S algorithm(I instance) {
        var solution = this.newSolution(instance);
        solution = constructive.construct(solution);
        ValidationUtil.assertValidScore(solution);
        printStatus("Constructive", solution);
        solution = localSearch(solution);
        return solution;
    }

    /**
     * <p>localSearch.</p>
     *
     * @param solution a S object.
     * @return a S object.
     */
    protected S localSearch(S solution) {
        for (int i = 0; i < improvers.size(); i++) {
            Improver<S, I> ls = improvers.get(i);
            solution = ls.improve(solution);
            ValidationUtil.assertValidScore(solution);
            printStatus("Improver " + i, solution);
        }
        return solution;
    }

    /**
     * <p>printStatus.</p>
     *
     * @param phase a {@link java.lang.String} object.
     * @param s a S object.
     */
    protected void printStatus(String phase, S s){
        log.fine(() -> String.format("\t\t%s: %s", phase, s));
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "S{" +
                "c=" + constructive +
                ", i=" + improvers +
                '}';
    }

    /** {@inheritDoc} */
    @Override
    public String getShortName() {
        return this.algorithmName.isEmpty() ? super.getShortName() : algorithmName;
    }
}
