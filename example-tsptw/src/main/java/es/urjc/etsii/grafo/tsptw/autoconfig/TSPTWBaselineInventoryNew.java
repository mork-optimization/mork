package es.urjc.etsii.grafo.tsptw.autoconfig;

import es.urjc.etsii.grafo.autoconfig.inventory.WhitelistInventoryFilter;
import es.urjc.etsii.grafo.tsptw.alg.GVNS;
import es.urjc.etsii.grafo.tsptw.constructives.TSPTWFeasibleConstructive;
import es.urjc.etsii.grafo.tsptw.constructives.TSPTWRandomConstructive;
import es.urjc.etsii.grafo.tsptw.improve.TSPTWInsertionSearch;
import es.urjc.etsii.grafo.tsptw.improve.TSPTWTwoOptSearch;
import es.urjc.etsii.grafo.tsptw.improve.TSPTWVND;
import es.urjc.etsii.grafo.tsptw.repair.*;
import es.urjc.etsii.grafo.tsptw.shake.TSPTWFeasibleInsertShake;
import es.urjc.etsii.grafo.tsptw.shake.TSPTWUnrestrictedInsertShake;

import java.util.Set;

/** Use --whitelist=TSPTWBaselineInventoryNew --tsptw.legacy-repair-space=true for the original 96 structures. */
public class TSPTWBaselineInventoryNew extends WhitelistInventoryFilter {
    @Override
    public Set<Class<?>> getWhitelist() {
        return Set.of(GVNS.class, TSPTWFeasibleConstructive.class, TSPTWRandomConstructive.class,
                TSPTWFeasibilityRepair.class, TSPTWBackwardViolated.class, TSPTWForwardNonviolated.class,
                TSPTWForwardViolated.class, TSPTWBackwardNonviolated.class,
                TSPTWFeasibleInsertShake.class, TSPTWUnrestrictedInsertShake.class,
                TSPTWVND.class, TSPTWInsertionSearch.class, TSPTWTwoOptSearch.class);
    }

    @Override
    public boolean include(Class<?> clazz) {
        // The standard whitelist includes subclasses; this preset must exclude the New variants.
        return whitelistedClasses.contains(clazz);
    }
}
