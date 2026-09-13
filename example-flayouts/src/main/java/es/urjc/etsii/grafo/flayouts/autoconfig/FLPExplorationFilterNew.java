package es.urjc.etsii.grafo.flayouts.autoconfig;

import es.urjc.etsii.grafo.autoconfig.generator.ExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.generator.TreeContext;
import es.urjc.etsii.grafo.flayouts.improve.FLPVNDNew;
import es.urjc.etsii.grafo.flayouts.model.*;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.improve.VND;
import es.urjc.etsii.grafo.improve.ls.LocalSearch;
import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchCachedBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchFirstImprovement;
import es.urjc.etsii.grafo.shake.RandomMoveShake;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

import java.util.Set;

/** Bound improver composition and enforce neighborhood roles without changing component implementations. */
public class FLPExplorationFilterNew extends ExplorationFilter {
    private static final Set<Class<?>> SEQUENCE_SEARCHES = Set.of(
            LocalSearchFirstImprovement.class, LocalSearchBestImprovement.class, LocalSearchCachedBestImprovement.class);
    private static final Set<Class<?>> VND_NEIGHBORHOODS = Set.of(
            FLPSwapNeighFastNew.class, FLPRelocateNeighFastNew.class, FLPOptNeighFastNew.class,
            FLPCandidateRelocateNeighNew.class, FLPBlockRelocateNeighNew.class);

    @Override
    public boolean reject(TreeContext context, Class<?> component) {
        boolean improver = Improver.class.isAssignableFrom(component);
        boolean neighborhood = Neighborhood.class.isAssignableFrom(component);
        for (var ancestor : context.branch()) {
            // Check the entire path: an intermediate component must not bypass these limits
            // when max-derivation-repetition permits recursive construction.
            if (improver) {
                if (ancestor == component) return true;
                if ((VND.class.isAssignableFrom(ancestor) || Improver.SequentialImprover.class.isAssignableFrom(ancestor)
                        || FLPVNDNew.class.isAssignableFrom(ancestor)) && !SEQUENCE_SEARCHES.contains(component)) return true;
            }
            // Full and fast versions enumerate the same moves. Compare them in standalone LS,
            // without enumerating both versions (or both together) in every ordered VND list.
            if (neighborhood && FLPVNDNew.class.isAssignableFrom(ancestor) && !VND_NEIGHBORHOODS.contains(component)) return true;
        }
        var parent = context.branch().peek();
        if (parent == null) return false;
        if (!neighborhood) return false;
        if (LocalSearchCachedBestImprovement.class.isAssignableFrom(parent)) {
            return !FLPPreservingNeighNew.class.isAssignableFrom(component);
        }
        if (LocalSearch.class.isAssignableFrom(parent) || parent == RandomMoveShake.class) {
            return !FLPPreservingNeighNew.class.isAssignableFrom(component)
                    && component != FLPRelocateNeigh.class && component != FLPSwapNeigh.class && component != FLPOptNeigh.class;
        }
        return false;
    }
}
