package es.urjc.etsii.grafo.algorithms;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.ProvidedParam;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.StringUtil;
import es.urjc.etsii.grafo.util.TimeControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example simple algorithm, executes:
 * Constructive → (Optional, if present) Local Searches → (Optional, if present) Shake → If did not improve end
 * ^_________________________________________|   else repeat
 * This class can be used to test all the pieces if they are working properly, or as a base for more complex algorithms
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class SimpleAlgorithm<S extends Solution<S, I>, I extends Instance> extends Algorithm<S, I> {
    private static Logger log = LoggerFactory.getLogger(SimpleAlgorithm.class.getName());

    protected final Constructive<S, I> constructive;
    protected final Improver<S, I> improver;

    /**
     * <p>Constructor for SimpleAlgorithm.</p>
     *
     * @param constructive a {@link Constructive} object.
     */
    public SimpleAlgorithm(String algorithmName, Constructive<S, I> constructive) {
        this(algorithmName, constructive, Improver.nul());
    }

    /**
     * <p>Constructor for SimpleAlgorithm.</p>
     *
     * @param algorithmName Algorithm name, uniquely identifies the current algorithm. Tip: If you dont care about the name, generate a random one using {@link StringUtil#randomAlgorithmName()}
     * @param constructive  a {@link Constructive} object.
     * @param improver      a {@link Improver} object.
     */
    @AutoconfigConstructor
    public SimpleAlgorithm(
            @ProvidedParam String algorithmName,
            Constructive<S, I> constructive,
            Improver<S, I> improver) {
        super(algorithmName);
        this.constructive = constructive;
        this.improver = improver;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Algorithm: Execute a single construction and then all the local searchs a single time.
     */
    @Override
    public S algorithm(I instance) {
        var solution = constructive(instance);
        solution = localSearch(solution);
        return solution;
    }

    /**
     * <p>Construct a solution first by calling the correct Java constructor and then
     * initialize it using the provided constructive method.</p>
     *
     * @param instance a I object.
     * @return a S object.
     */
    protected S constructive(I instance) {
        var solution = this.newSolution(instance);
        solution = constructive.construct(solution);
        assert Context.validate(solution);
        Metrics.addCurrentObjectives(solution);
        printStatus("Constructive", solution);
        return solution;
    }

    /**
     * <p>localSearch.</p>
     *
     * @param solution a S object.
     * @return a S object.
     */
    protected S localSearch(S solution) {
        if (TimeControl.isTimeUp()) {
            return solution;
        }
        solution = improver.improve(solution);
        assert Context.validate(solution);
        Metrics.addCurrentObjectives(solution);
        printStatus("Improver " + improver, solution);
        return solution;
    }

    /**
     * <p>printStatus.</p>
     *
     * @param phase    a {@link String} object.
     * @param solution a S object.
     */
    protected void printStatus(String phase, S solution) {
        log.debug("\t\t{}: {}", phase, solution);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getName() + "{" +
                "c=" + constructive +
                ", i=" + improver +
                '}';
    }
}
