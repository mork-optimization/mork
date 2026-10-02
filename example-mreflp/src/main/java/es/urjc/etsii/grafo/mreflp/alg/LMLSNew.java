package es.urjc.etsii.grafo.mreflp.alg;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.mreflp.create.MREFLPConstructiveNew;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.util.TimeControl;
import java.util.Objects;

/** Copy of the LMLS restart loop with configurable construction, improvement and normalization. */
public final class LMLSNew extends Algorithm<MREFLPSolution, MREFLPInstance> {
    private final MREFLPConstructiveNew constructive;
    private final Improver<MREFLPSolution, MREFLPInstance> improver;
    private final boolean learningEnabled, normalize;
    private final double epsilon, alpha, beta, gamma, rho;
    private final int maxRestarts;

    @AutoconfigConstructor
    public LMLSNew(@ProvidedParam String name, MREFLPConstructiveNew constructive,
                   Improver<MREFLPSolution, MREFLPInstance> improver,
                   @CategoricalParam(strings = {"true", "false"}) boolean learningEnabled,
                   @CategoricalParam(strings = {"true", "false"}) boolean normalize,
                   @RealParam(min = 0, max = 1) double epsilon,
                   @RealParam(min = 0.01, max = 0.99) double alpha,
                   @RealParam(min = 0.01, max = 0.99) double beta,
                   @RealParam(min = 0.01, max = 0.99) double gamma,
                   @RealParam(min = 0.01, max = 0.99) double rho,
                   @IntegerParam(min = 0, max = 1000) int maxRestarts) {
        super(name);
        if (!(epsilon >= 0 && epsilon <= 1) || maxRestarts < 0) throw new IllegalArgumentException("Invalid restart parameters");
        for (double factor : new double[]{alpha, beta, gamma, rho}) {
            if (!(factor > 0 && factor < 1)) throw new IllegalArgumentException("Learning factors must be in (0,1)");
        }
        this.constructive = Objects.requireNonNull(constructive);
        this.improver = Objects.requireNonNull(improver);
        this.learningEnabled = learningEnabled;
        this.normalize = normalize;
        this.epsilon = epsilon;
        this.alpha = alpha; this.beta = beta; this.gamma = gamma; this.rho = rho;
        this.maxRestarts = maxRestarts;
    }

    @Override public MREFLPSolution algorithm(MREFLPInstance instance) {
        if (!TimeControl.isEnabled() && maxRestarts == 0) throw new IllegalStateException("LMLSNew needs a deadline or restart limit");
        var learning = learningEnabled ? new LearningMatrixNew(instance.n(), instance.groups(), alpha, beta, gamma, rho, normalize) : null;
        MREFLPSolution best = null;
        int restarts = 0;
        do {
            var solution = constructive.construct(newSolution(instance), learning, epsilon);
            int[] initial = learning == null ? null : solution.assignments();
            Metrics.addCurrentObjectives(solution);
            if (!TimeControl.isTimeUp()) solution = improver.improve(solution);
            if (best == null || solution.cost() < best.cost()) {
                best = solution.cloneSolution();
                Metrics.addCurrentObjectives(best);
            }
            if (learning != null && !TimeControl.isTimeUp()) learning.update(initial, solution);
            restarts++;
        } while (!TimeControl.isTimeUp() && (maxRestarts == 0 || restarts < maxRestarts));
        return best;
    }
}
