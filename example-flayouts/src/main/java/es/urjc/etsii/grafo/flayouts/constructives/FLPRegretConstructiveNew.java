package es.urjc.etsii.grafo.flayouts.constructives;

import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.flayouts.model.*;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;
import java.util.ArrayList;
import java.util.Comparator;

/** Repair/construction by second- or third-best insertion regret. */
public class FLPRegretConstructiveNew extends Reconstructive<FLPSolution, FLPInstance> {
    private final int regretOrder;
    private final double fraction;
    private record Candidate(FLPNewMove move, double regret) {}

    @AutoconfigConstructor
    public FLPRegretConstructiveNew(@CategoricalParam(strings = {"2", "3"}) int regretOrder,
                                   @RealParam(min = 0, max = 0.3) double fraction) {
        if (regretOrder != 2 && regretOrder != 3) throw new IllegalArgumentException("Regret order must be 2 or 3");
        FLPNewUtil.probability(fraction, "fraction");
        this.regretOrder = regretOrder;
        this.fraction = fraction;
    }

    @Override
    public FLPSolution construct(FLPSolution solution) {
        while (!solution.getNotAssignedFacilities().isEmpty()) {
            var candidates = new ArrayList<Candidate>();
            for (int f : FLPNewUtil.missing(solution)) {
                if (TimeControl.isTimeUp()) return FLPNewUtil.completeByAppend(solution);
                var best = FLPConstructionUtil.bestInsertions(solution, f, regretOrder, false);
                if (best[0] == null) return FLPNewUtil.completeByAppend(solution);
                int last = 0;
                while (last + 1 < best.length && best[last + 1] != null) last++;
                candidates.add(new Candidate(best[0], best[last].delta() - best[0].delta()));
            }
            CollectionUtil.shuffle(candidates);
            candidates.sort(Comparator.comparingDouble(Candidate::regret).reversed());
            int size = Math.max(1, (int) Math.ceil(fraction * candidates.size()));
            candidates.get(RandomManager.getRandom().nextInt(size)).move().execute(solution);
        }
        return solution;
    }

    @Override
    public FLPSolution reconstruct(FLPSolution solution) { return construct(solution); }
    @Override
    public String toString() { return "FLPRegretConstructiveNew{regretOrder=" + regretOrder + ", fraction=" + fraction + "}"; }
}
