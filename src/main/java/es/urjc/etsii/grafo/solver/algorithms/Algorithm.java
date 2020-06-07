package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.SolutionBuilder;
import es.urjc.etsii.grafo.solver.services.InheritedComponent;

@InheritedComponent
public interface Algorithm<S extends Solution<I>, I extends Instance> {

    /**
     * Current algorithm short name, must be unique per execution
     * @return Should include parameter configuration if same algorithm is used with different parameters
     */
    default String getShortName(){
        return this.toString().replaceAll("[\\s{}\\[\\]-_=?+&^%$#@!]", "");
    }

    /**
     * Runs the algorithm over the empty but initialized solution
     * @param i Instance
     * @return Built solution
     */
    S algorithm(I instance, SolutionBuilder<S,I> builder);

}
