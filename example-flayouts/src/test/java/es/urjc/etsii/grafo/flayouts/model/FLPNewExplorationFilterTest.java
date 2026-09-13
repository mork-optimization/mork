package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.autoconfig.generator.TreeContext;
import es.urjc.etsii.grafo.flayouts.autoconfig.FLPExplorationFilterNew;
import es.urjc.etsii.grafo.flayouts.improve.FLPSimulatedAnnealingNew;
import es.urjc.etsii.grafo.flayouts.improve.FLPVNDNew;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.improve.VND;
import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchCachedBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchFirstImprovement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FLPNewExplorationFilterTest {
    private final FLPExplorationFilterNew filter = new FLPExplorationFilterNew();

    @Test
    void compositionsRejectIndirectNestingEvenWithRecursionEnabled() {
        for (var composition : List.of(VND.class, Improver.SequentialImprover.class, FLPVNDNew.class)) {
            var context = new TreeContext(1000, 10);
            context.push(SimpleAlgorithm.class);
            context.push(composition);
            // Model a future intermediate component to ensure the rule checks ancestors, not just the parent.
            context.push(Object.class);
            for (var nested : List.of(VND.class, Improver.SequentialImprover.class, FLPVNDNew.class,
                    FLPSimulatedAnnealingNew.class, Improver.NullImprover.class)) {
                assertTrue(filter.reject(context, nested), composition + " must reject " + nested);
            }
            for (var search : List.of(LocalSearchFirstImprovement.class, LocalSearchBestImprovement.class,
                    LocalSearchCachedBestImprovement.class)) assertFalse(filter.reject(context, search));
        }
    }

    @Test
    void repeatedImproverClassesAreRejectedAcrossIntermediateComponents() {
        var context = new TreeContext(1000, 10);
        context.push(SimpleAlgorithm.class);
        assertFalse(filter.reject(context, FLPSimulatedAnnealingNew.class));
        context.push(FLPSimulatedAnnealingNew.class);
        context.push(Object.class);
        assertTrue(filter.reject(context, FLPSimulatedAnnealingNew.class));
        assertFalse(filter.reject(context, LocalSearchBestImprovement.class));
    }

    @Test
    void equivalentFullVariantsRemainAvailableOutsideTheOrderedNeighborhoodList() {
        var context = new TreeContext(1000, 10);
        context.push(SimpleAlgorithm.class);
        context.push(LocalSearchBestImprovement.class);
        var full = List.of(FLPSwapNeighNew.class, FLPRelocateNeighNew.class, FLPOptNeighNew.class);
        for (var neighborhood : full) assertFalse(filter.reject(context, neighborhood));
        context.pop();
        context.push(FLPVNDNew.class);
        context.push(Object.class);
        for (var neighborhood : full) assertTrue(filter.reject(context, neighborhood));
        for (var neighborhood : List.of(FLPSwapNeighFastNew.class, FLPRelocateNeighFastNew.class,
                FLPOptNeighFastNew.class, FLPCandidateRelocateNeighNew.class, FLPBlockRelocateNeighNew.class)) {
            assertFalse(filter.reject(context, neighborhood));
        }
    }
}
