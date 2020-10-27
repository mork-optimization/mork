package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.create.SolutionBuilder;
import es.urjc.etsii.grafo.solver.destructor.Shake;
import es.urjc.etsii.grafo.solver.improve.Improver;

import java.util.Arrays;
import java.util.logging.Logger;

public class IteratedGreedy<S extends Solution<I>, I extends Instance> extends Algorithm<S, I> {

    private static final Logger logger = Logger.getLogger(IteratedGreedy.class.getName());

    private Constructive<S, I> constructive;
    private Shake<S, I> shake;
    private Improver<S, I>[] improvers;

    /**
     * Number of iterations
     */
    private int n;

    protected IteratedGreedy(){}

    @SafeVarargs
    public IteratedGreedy(int n, Constructive<S, I> constructive, Shake<S, I> shake, Improver<S, I>... improvers) {
        this.n = n;
        this.constructive = constructive;
        this.shake = shake;
        this.improvers = improvers;
    }

    public IteratedGreedy(int n, Constructive<S, I> constructive, Shake<S, I> shake){
        this.n = n;
        this.constructive = constructive;
        this.shake = shake;
    }

    public IteratedGreedy(int n, Constructive<S, I> constructive, Shake<S, I> shake, Improver<S, I> improver) {
        this(n, constructive, shake, new Improver[]{improver});
    }

    @Override
    public S algorithm(I instance, SolutionBuilder<S, I> builder) {
        S s = builder.initializeSolution(instance);
        s = this.constructive.construct(s);
        s = ls(s);
        logger.fine(String.format("Initial solution: %s - %s", s.getScore(), s));
        for (int i = 0; i < n; i++) {
            var temp = this.shake.shake(s, 1, 1, false);
            temp = ls(temp);
            s = s.getBetterSolution(temp);
            if(s == temp){
                logger.fine(String.format("Improved at iteration %s: %s - %s", i, s.getScore(), s));
            }
        }

        return s;
    }

    private S ls(S s) {
        if(improvers == null) return s;

        for (Improver<S, I> improver : improvers) {
            s = improver.improve(s);
        }
        return s;
    }

    @Override
    public String toString() {
        return "IteratedGreedy{" +
                "constructive=" + constructive +
                ", shake=" + shake +
                ", improvers=" + Arrays.toString(improvers) +
                '}';
    }
}
