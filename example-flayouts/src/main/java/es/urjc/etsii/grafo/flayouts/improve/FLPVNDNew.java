package es.urjc.etsii.grafo.flayouts.improve;

import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.flayouts.Main;
import es.urjc.etsii.grafo.flayouts.model.*;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchFirstImprovement;
import es.urjc.etsii.grafo.util.TimeControl;
import java.util.ArrayList;
import java.util.List;

/** VND variant selecting neighborhoods directly, allowing the same LS policy at every stage. */
public class FLPVNDNew extends Improver<FLPSolution, FLPInstance> {
    public enum Policy { FIRST, BEST }
    private final List<FLPPreservingNeighNew> neighborhoods;
    private final List<Improver<FLPSolution, FLPInstance>> improvers;
    private final Policy policy;

    @AutoconfigConstructor
    public FLPVNDNew(@ComponentParam(min = 2, max = 4) List<FLPPreservingNeighNew> neighborhoods,
                     @CategoricalParam(strings = {"FIRST", "BEST"}) Policy policy) {
        super(Main.FLOW);
        if (neighborhoods.size() < 2 || neighborhoods.size() > 4) throw new IllegalArgumentException("VND requires 2 to 4 neighborhoods");
        this.neighborhoods = List.copyOf(neighborhoods);
        this.policy = java.util.Objects.requireNonNull(policy);
        this.improvers = new ArrayList<>();
        for (var neighborhood : this.neighborhoods) {
            improvers.add(policy == Policy.FIRST ? new LocalSearchFirstImprovement<>(neighborhood)
                    : new LocalSearchBestImprovement<>(neighborhood));
        }
    }

    @Override
    public FLPSolution improve(FLPSolution solution) {
        int index = 0;
        while (index < improvers.size() && !TimeControl.isTimeUp()) {
            double before = solution.getScore();
            solution = improvers.get(index).improve(solution);
            index = objective.isBetter(solution, before) ? 0 : index + 1;
        }
        return solution;
    }

    @Override
    public String toString() { return "FLPVNDNew{policy=" + policy + ", neighborhoods=" + neighborhoods + "}"; }
}
