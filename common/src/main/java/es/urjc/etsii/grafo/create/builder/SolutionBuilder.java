package es.urjc.etsii.grafo.create.builder;

import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

/**
 * How to generate empty solutions from a given instance.
 * If problem dependant, implement your own version.
 * Time taken by the builder is not counted towards algorithm execution time
 */
@InheritedComponent
public abstract class SolutionBuilder<S extends Solution<S,I>, I extends Instance> {
     /**
      * Generate a solution with the parameters given by the user
      *
      * @param instance Instance used to build the empty solution
      * @return empty solution referencing the given instance
      */
     public abstract S initializeSolution(I instance);
}
