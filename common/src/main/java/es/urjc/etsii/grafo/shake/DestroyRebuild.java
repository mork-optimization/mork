package es.urjc.etsii.grafo.shake;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

public class DestroyRebuild<S extends Solution<S,I>, I extends Instance> extends Shake<S,I>{

    private final Reconstructive<S, I> constructive;
    private final Destructive<S, I> destructive;

    @AutoconfigConstructor
    public DestroyRebuild(Reconstructive<S, I> constructive, Destructive<S, I> destructive) {
        this.constructive = constructive;
        this.destructive = destructive;
    }

    @Override
    public S shake(S solution, int k) {
        solution = destructive.destroy(solution, k);
        if(solution != null){
            solution = constructive.reconstruct(solution);
        }
        return solution;
    }

    @Override
    public String toString() {
        return "DR{" +
                "d=" + destructive +
                ", r=" + constructive +
                '}';
    }
}
