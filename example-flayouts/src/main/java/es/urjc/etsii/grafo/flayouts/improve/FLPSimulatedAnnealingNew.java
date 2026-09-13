package es.urjc.etsii.grafo.flayouts.improve;

import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.flayouts.Main;
import es.urjc.etsii.grafo.flayouts.model.*;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;

/** Tunable variant of Mork SA with sampled scale, bounded cycles, and best-solution retention. */
public class FLPSimulatedAnnealingNew extends Improver<FLPSolution, FLPInstance> {
    private final FLPPreservingNeighNew neighborhood;
    private final double acceptance;
    private final double cooling;
    private final int cycleMultiplier;
    private final int maxCycles;

    @AutoconfigConstructor
    public FLPSimulatedAnnealingNew(FLPPreservingNeighNew neighborhood,
                                   @RealParam(min = 0.2, max = 0.8) double acceptance,
                                   @RealParam(min = 0.90, max = 0.99) double cooling,
                                   @CategoricalParam(strings = {"1", "5", "10"}) int cycleMultiplier,
                                   @IntegerParam(min = 1, max = 1000) int maxCycles) {
        super(Main.FLOW);
        if (!(acceptance > 0 && acceptance < 1) || !(cooling > 0 && cooling < 1)
                || cycleMultiplier < 1 || maxCycles < 1) throw new IllegalArgumentException("Invalid SA parameters");
        this.neighborhood = java.util.Objects.requireNonNull(neighborhood);
        this.acceptance = acceptance;
        this.cooling = cooling;
        this.cycleMultiplier = cycleMultiplier;
        this.maxCycles = maxCycles;
    }

    @Override
    public FLPSolution improve(FLPSolution solution) {
        var working = solution.cloneSolution();
        var best = working.cloneSolution();
        double temperature = initialTemperature(working);
        double minimum = temperature * 1e-6;
        long cycleLength = (long) cycleMultiplier * Math.max(1, solution.nAssigned());
        for (int cycle = 0; cycle < maxCycles && temperature > minimum && !TimeControl.isTimeUp(); cycle++) {
            for (long i = 0; i < cycleLength && !TimeControl.isTimeUp(); i++) {
                var candidate = neighborhood.getRandomMove(working);
                if (candidate.isEmpty()) return best;
                var move = candidate.get();
                if (move.delta() <= 0 || RandomManager.getRandom().nextDouble() < Math.exp(-move.delta() / temperature)) {
                    move.execute(working);
                    if (objective.isBetter(working, best)) {
                        best = working.cloneSolution();
                        Metrics.addCurrentObjectives(best);
                    }
                }
            }
            temperature *= cooling;
        }
        return best;
    }

    private double initialTemperature(FLPSolution solution) {
        double total = 0;
        int count = 0;
        for (int i = 0; i < 32 && !TimeControl.isTimeUp(); i++) {
            var candidate = neighborhood.getRandomMove(solution);
            if (candidate.isEmpty()) break;
            double delta = candidate.get().delta();
            if (delta > 0) { total += delta; count++; }
        }
        double scale = count == 0 ? Math.max(1, Math.abs(solution.getScore()) / Math.max(1, solution.nAssigned())) : total / count;
        return -scale / Math.log(acceptance);
    }

    @Override
    public String toString() {
        return "FLPSimulatedAnnealingNew{neighborhood=" + neighborhood + ", acceptance=" + acceptance
                + ", cooling=" + cooling + ", cycleMultiplier=" + cycleMultiplier + ", maxCycles=" + maxCycles + "}";
    }
}
