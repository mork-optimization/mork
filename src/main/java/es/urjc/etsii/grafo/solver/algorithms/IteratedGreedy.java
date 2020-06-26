package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.create.SolutionBuilder;
import es.urjc.etsii.grafo.solver.destructor.Shake;
import es.urjc.etsii.grafo.solver.improve.Improver;
import es.urjc.etsii.grafo.util.DoubleComparator;

public class IteratedGreedy<S extends Solution<I>, I extends Instance> extends Algorithm<S, I> {

    private Constructive<S, I> constructive;
    private Shake<S, I> shake;
    private Improver<S, I>[] improvers;

    protected IteratedGreedy(){}

    public IteratedGreedy(Constructive<S, I> constructive, Shake<S, I> shake, Improver<S, I>... improvers) {
        this.constructive = constructive;
        this.shake = shake;
        this.improvers = improvers;
    }

    public IteratedGreedy(Constructive<S, I> constructive, Shake<S, I> shake){
        this.constructive = constructive;
        this.shake = shake;
    }

    public IteratedGreedy(Constructive<S, I> constructive, Shake<S, I> shake, Improver<S, I> improver) {
        this(constructive, shake, new Improver[]{improver});
    }

    @Override
    public S algorithm(I instance, SolutionBuilder<S, I> builder) {
        S s = builder.initializeSolution(instance);
        s = this.constructive.construct(s);
        s = ls(s);

        double current;
        do {
            current = s.getOptimalValue();
            s = this.shake.shake(s, 1, 1, false);
            s = ls(s);
        } while (!DoubleComparator.equals(current, s.getOptimalValue()));

        return s;
    }

    private S ls(S s) {
        if(improvers == null) return s;

        for (Improver<S, I> improver : improvers) {
            s = improver.improve(s);
        }
        return s;
    }
}
