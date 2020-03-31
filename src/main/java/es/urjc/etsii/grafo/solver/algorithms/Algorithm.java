package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.Result;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.services.InheritedComponent;

@InheritedComponent
public interface Algorithm<S extends Solution<I>, I extends Instance> {

    Result execute(I ins, int repetitions);

    /**
     * Current algorithm short name, must be unique per execution
     * @return Should include parameter configuration if same algorithm is used with different parameters
     */
    default String getShortName(){
        return this.toString().replaceAll("[\\s{}\\[\\]-_=?+&^%$#@!]", "");
    }
}
