package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;

import java.util.Objects;

//@InheritedComponent
// TODO Idea: Allow algorithms via config instead of a AbstractExperimentSetup

/**
 * Base algorithm class, all algorithms should extend this class or any of its subclasses.
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
public abstract class Algorithm<S extends Solution<S,I>, I extends Instance> {

    private SolutionBuilder<S,I> builder;

    /**
     * Current algorithm short name, must be unique per execution. Truncated to 180 characters
     *
     * @return Should include parameter configuration if same algorithm is used with different parameters
     */
    public String getShortName(){
        String s = this.toString().replaceAll("[Aa]lgorithm", "").replaceAll("[\\s{}\\[\\]-_\\.=?+&^%,$#'\"@!]", "");
        return s.substring(0, Math.min(s.length(), 30));
    }

    /**
     * Runs the algorithm
     *
     * @param instance Instance to solve
     * @return Built solution
     */
    public abstract S algorithm(I instance);

    /**
     * Create a new solution for the given instance. Solution is empty by default.
     *
     * @param instance Instance
     * @return Empty solution, by default created calling the constructor Solution(Instance i)
     */
    public S newSolution(I instance){
        return this.builder.initializeSolution(instance);
    }

    /**
     * Get solution builder
     *
     * @return solution builder
     */
    protected SolutionBuilder<S, I> getBuilder() {
        return builder;
    }

    /**
     * Set solution builder, used by the framework.
     * In case an algorithms contains another algorithms, this method should be overridden as follows:
     * <pre>
     *     &#64;Override
     *     public void setBuilder(SolutionBuilder&#60;S, I&#62; builder) {
     *         super.setBuilder(builder);
     *         this.algorithm.setBuilder(builder);
     *     }
     * </pre>
     *
     * @param builder solution builder
     */
    public void setBuilder(SolutionBuilder<S, I> builder) {
        this.builder = Objects.requireNonNull(builder);
    }
}
