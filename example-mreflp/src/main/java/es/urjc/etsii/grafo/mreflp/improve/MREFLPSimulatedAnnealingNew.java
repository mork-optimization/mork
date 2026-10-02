package es.urjc.etsii.grafo.mreflp.improve;

import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.improve.sa.SimulatedAnnealingBuilder;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.mreflp.Main;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.solution.neighborhood.*;
import es.urjc.etsii.grafo.util.TimeControl;
import java.util.Objects;
import java.util.Optional;

/** Autoconfig adapter for Mork SA, with run-local sampling and incumbent metric recording. */
public final class MREFLPSimulatedAnnealingNew extends Improver<MREFLPSolution, MREFLPInstance> {
    private final RandomizableNeighborhood<MREFLPMove, MREFLPSolution, MREFLPInstance> neighborhood;
    private final double temperatureScale, cooling;
    private final int cycleLength, maxIterations;
    @AutoconfigConstructor
    public MREFLPSimulatedAnnealingNew(RandomizableNeighborhood<MREFLPMove, MREFLPSolution, MREFLPInstance> neighborhood,
                                     @RealParam(min = 0.01, max = 10) double temperatureScale,
                                     @RealParam(min = 0.5, max = 0.99) double cooling,
                                     @IntegerParam(min = 1, max = 1000) int cycleLength,
                                     @IntegerParam(min = 1, max = 1000) int maxIterations) {
        super(Main.COST);
        if (!(temperatureScale > 0 && Double.isFinite(temperatureScale)) || !(cooling > 0 && cooling < 1)
                || cycleLength < 1 || maxIterations < 1) throw new IllegalArgumentException("Invalid SA parameters");
        this.neighborhood = Objects.requireNonNull(neighborhood);
        this.temperatureScale = temperatureScale; this.cooling = cooling;
        this.cycleLength = cycleLength; this.maxIterations = maxIterations;
    }

    @Override public MREFLPSolution improve(MREFLPSolution input) {
        var solution = input.cloneSolution();
        Metrics.addCurrentObjectives(solution);
        if (TimeControl.isTimeUp()) return solution;
        double sum = 0;
        int worsening = 0;
        for (int sample = 0; sample < 64 && !TimeControl.isTimeUp(); sample++) {
            var move = neighborhood.getRandomMove(solution);
            if (move.isEmpty()) break;
            if (move.get().delta() > 0) { sum += move.get().delta(); worsening++; }
        }
        double temperature = temperatureScale * (worsening == 0 ? 1 : sum / worsening);
        // This wrapper is allocated per call, so no incumbent or temperature leaks between runs.
        var recording = new RandomizableNeighborhood<MREFLPMove, MREFLPSolution, MREFLPInstance>() {
            private long best = solution.cost();
            @Override public ExploreResult<MREFLPMove, MREFLPSolution, MREFLPInstance> explore(MREFLPSolution s) {
                return neighborhood.explore(s);
            }
            @Override public Optional<MREFLPMove> getRandomMove(MREFLPSolution s) {
                if (s.cost() < best) { best = s.cost(); Metrics.addCurrentObjectives(s); }
                return neighborhood.getRandomMove(s);
            }
        };
        var annealing = new SimulatedAnnealingBuilder<MREFLPMove, MREFLPSolution, MREFLPInstance>()
                .withObjective(Main.COST).withNeighborhood(recording).withInitialTempValue(temperature)
                .withCoolDownExponential(cooling).withCycleLength(cycleLength)
                .withTerminationCriteriaCustom((s, n, t, iteration) -> t <= 0.01 || iteration >= maxIterations)
                .build();
        var best = annealing.improve(solution);
        Metrics.addCurrentObjectives(best);
        return best;
    }
}
