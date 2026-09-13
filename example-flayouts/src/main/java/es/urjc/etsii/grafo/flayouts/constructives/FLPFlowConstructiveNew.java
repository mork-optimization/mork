package es.urjc.etsii.grafo.flayouts.constructives;

import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.flayouts.model.*;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;
import java.util.Comparator;
import java.util.Objects;

/** Facility-first flow ordering followed by cheapest insertion or append. */
public class FLPFlowConstructiveNew extends Reconstructive<FLPSolution, FLPInstance> {
    public enum Order { RANDOM, TOTALFLOW, FLOWPERLENGTH }
    public enum Placement { APPEND, ALLPOSITIONS }
    private final Order order;
    private final Placement placement;
    private final double randomness;

    @AutoconfigConstructor
    public FLPFlowConstructiveNew(
            @CategoricalParam(strings = {"RANDOM", "TOTALFLOW", "FLOWPERLENGTH"}) Order order,
            @CategoricalParam(strings = {"APPEND", "ALLPOSITIONS"}) Placement placement,
            @RealParam(min = 0, max = 1) double randomness) {
        FLPNewUtil.probability(randomness, "randomness");
        this.order = Objects.requireNonNull(order);
        this.placement = Objects.requireNonNull(placement);
        this.randomness = randomness;
    }

    @Override
    public FLPSolution construct(FLPSolution solution) {
        if (TimeControl.isTimeUp()) return FLPNewUtil.completeByAppend(solution);
        var missing = FLPNewUtil.missing(solution);
        var instance = solution.getInstance();
        double[] priority = new double[instance.nFacilities()];
        if (order != Order.RANDOM) {
            for (int f : missing) {
                if (TimeControl.isTimeUp()) return FLPNewUtil.completeByAppend(solution);
                for (int g = 0; g < priority.length; g++) if (g != f) priority[f] += instance.flow(f, g);
                if (order == Order.FLOWPERLENGTH) priority[f] /= Math.max(1, instance.length(f));
            }
            es.urjc.etsii.grafo.util.CollectionUtil.shuffle(missing);
            missing.sort(Comparator.<Integer>comparingDouble(f -> priority[f]).reversed());
        }
        var random = RandomManager.getRandom();
        while (!missing.isEmpty()) {
            if (TimeControl.isTimeUp()) return FLPNewUtil.completeByAppend(solution);
            int index = order == Order.RANDOM || random.nextDouble() < randomness ? random.nextInt(missing.size()) : 0;
            int facility = missing.remove(index);
            var best = FLPConstructionUtil.bestInsertions(solution, facility, 1, placement == Placement.APPEND)[0];
            if (best == null) return FLPNewUtil.completeByAppend(solution);
            best.execute(solution);
        }
        return solution;
    }

    @Override
    public FLPSolution reconstruct(FLPSolution solution) { return construct(solution); }
    @Override
    public String toString() { return "FLPFlowConstructiveNew{order=" + order + ", placement=" + placement + ", randomness=" + randomness + "}"; }
}
