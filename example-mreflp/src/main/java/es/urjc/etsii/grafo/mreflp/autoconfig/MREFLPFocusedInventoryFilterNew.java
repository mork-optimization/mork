package es.urjc.etsii.grafo.mreflp.autoconfig;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.IteratedGreedy;
import es.urjc.etsii.grafo.algorithms.vns.VNS;
import es.urjc.etsii.grafo.autoconfig.inventory.BlacklistInventoryFilter;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.improve.ls.LocalSearchBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchCachedBestImprovement;
import es.urjc.etsii.grafo.improve.ls.LocalSearchFirstImprovement;
import es.urjc.etsii.grafo.mreflp.alg.LMLS;
import es.urjc.etsii.grafo.mreflp.alg.LMLSNew;
import es.urjc.etsii.grafo.mreflp.cmsa.MREFLPCMSAConstructiveNew;
import es.urjc.etsii.grafo.mreflp.create.MREFLPConstructive;
import es.urjc.etsii.grafo.mreflp.improve.OneMoveTabuSearch;
import es.urjc.etsii.grafo.mreflp.improve.SwapDescent;

import java.util.Set;

/** Focused tuning inventory; the original LMLS still builds its original operators internally. */
public final class MREFLPFocusedInventoryFilterNew extends BlacklistInventoryFilter {
    private static final Set<Class<?>> ROOTS = Set.of(LMLS.class, LMLSNew.class, IteratedGreedy.class, VNS.class);

    @Override
    public Set<Class<?>> getBlacklist() {
        return Set.of(LocalSearchCachedBestImprovement.class, Improver.SequentialImprover.class,
                LocalSearchFirstImprovement.class, LocalSearchBestImprovement.class,
                MREFLPConstructive.class, OneMoveTabuSearch.class, SwapDescent.class,
                MREFLPCMSAConstructiveNew.class);
    }

    @Override
    public boolean include(Class<?> clazz) {
        if (Algorithm.class.isAssignableFrom(clazz) && !ROOTS.contains(clazz)) return false;
        return super.include(clazz);
    }
}
