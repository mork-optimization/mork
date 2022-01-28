package es.urjc.etsii.grafo.solver.destructor;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Reconstructive;

public class DestroyRebuild<S extends Solution<S,I>, I extends Instance> extends Shake<S,I>{

    private final Reconstructive<S, I> constructive;
    private final Destructive<S, I> destructive;

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
