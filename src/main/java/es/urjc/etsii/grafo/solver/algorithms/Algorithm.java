package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;

//@InheritedComponent
// TODO Idea: Allow algorithms via config instead of a AbstractExperimentSetup
public abstract class Algorithm<S extends Solution<I>, I extends Instance> {

    /**
     * Current algorithm short name, must be unique per execution. Truncated to 180 characters
     * @return Should include parameter configuration if same algorithm is used with different parameters
     */
    public String getShortName(){
        String s = this.toString().replaceAll("[\\s{}\\[\\]-_\\.=?+&^%,$#'\"@!]", "");
        return s.substring(0, Math.min(s.length(), 180));
    }

    /**
     * Runs the algorithm over the empty but initialized solution
     * @param instance Instance
     * @return Built solution
     */
    public abstract S algorithm(I instance, SolutionBuilder<S,I> builder);

}
