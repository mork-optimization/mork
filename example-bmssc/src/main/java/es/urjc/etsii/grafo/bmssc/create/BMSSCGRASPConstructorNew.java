package es.urjc.etsii.grafo.bmssc.create;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.CategoricalParam;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.bmssc.Main;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCMove;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.bmssc.util.BMSSCNewUtil;
import es.urjc.etsii.grafo.bmssc.util.BMSSCNewUtil.SeedStrategy;
import es.urjc.etsii.grafo.bmssc.util.BMSSCUtil;
import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.create.grasp.GraspBuilder;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.util.Context;

import java.util.Objects;

/** Copy of BMSSCGRASPConstructor exposing both GRASP strategies and reconstruction. */
public class BMSSCGRASPConstructorNew extends Reconstructive<BMSSCSolution, BMSSCInstance> {
    public enum Strategy { GREEDY_RANDOM, RANDOM_GREEDY }

    private final double alpha;
    private final Strategy strategy;
    private final SeedStrategy seedStrategy;
    private final Reconstructive<BMSSCSolution, BMSSCInstance> delegate;

    @AutoconfigConstructor
    public BMSSCGRASPConstructorNew(@RealParam(min = 0, max = 1) double alpha,
            @CategoricalParam(strings = {"GREEDY_RANDOM", "RANDOM_GREEDY"}) Strategy strategy,
            @CategoricalParam(strings = {"FARTHEST_FIRST", "RANDOM"}) SeedStrategy seedStrategy) {
        BMSSCNewUtil.validateUnitInterval(alpha);
        this.alpha = alpha;
        this.strategy = Objects.requireNonNull(strategy);
        this.seedStrategy = Objects.requireNonNull(seedStrategy);
        var builder = new GraspBuilder<BMSSCMove, BMSSCSolution, BMSSCInstance>()
                .withObjective(Main.OBJ).withAlphaValue(alpha)
                .withListManager(new BMSSCListManagerNew(seedStrategy));
        if (strategy == Strategy.GREEDY_RANDOM) builder.withStrategyGreedyRandom();
        else builder.withStrategyRandomGreedy();
        this.delegate = builder.build();
    }

    @Override
    public BMSSCSolution construct(BMSSCSolution solution) {
        var result = BMSSCUtil.withPartialSolution(() -> delegate.construct(solution));
        return finish(result);
    }

    @Override
    public BMSSCSolution reconstruct(BMSSCSolution solution) {
        var result = BMSSCUtil.withPartialSolution(() -> delegate.reconstruct(solution));
        return finish(result);
    }

    private BMSSCSolution finish(BMSSCSolution solution) {
        if (!solution.feasibleClusterSizes()) throw new IllegalStateException("Construction did not fill cluster quotas");
        assert Context.validate(solution);
        Metrics.addCurrentObjectives(solution);
        return solution;
    }

    @Override
    public String toString() {
        return "BMSSCGRASPConstructorNew{alpha=" + alpha + ", strategy=" + strategy + ", seedStrategy=" + seedStrategy + "}";
    }
}
