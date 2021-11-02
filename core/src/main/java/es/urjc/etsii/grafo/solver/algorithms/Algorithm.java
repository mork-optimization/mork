package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;

import java.util.Objects;

//@InheritedComponent
// TODO Idea: Allow algorithms via config instead of a AbstractExperimentSetup
public abstract class Algorithm<S extends Solution<I>, I extends Instance> {

    private SolutionBuilder<S,I> builder;

    /**
     * Current algorithm short name, must be unique per execution. Truncated to 180 characters
     * @return Should include parameter configuration if same algorithm is used with different parameters
     */
    public String getShortName(){
        String s = this.toString().replaceAll("[\\s{}\\[\\]-_\\.=?+&^%,$#'\"@!]", "");
        return s.substring(0, Math.min(s.length(), 180));
    }

    /**
     * Runs the algorithm
     * @param instance Instance to solve
     * @return Built solution
     */
    public abstract S algorithm(I instance);

    /**
     * Create a new solution for the given instance. Solution is empty by default.
     * @param instance Instance
     * @return Empty solution, by default created calling the constructor Solution(Instance i)
     */
    public S newSolution(I instance){
        return this.builder.initializeSolution(instance);
    }

    /**
     * Get solution builder
     * @return solution builder
     */
    protected SolutionBuilder<S, I> getBuilder() {
        return builder;
    }

    public void setBuilder(SolutionBuilder<S, I> builder) {
        this.builder = Objects.requireNonNull(builder);
    }
}
