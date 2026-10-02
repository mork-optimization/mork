package es.urjc.etsii.grafo.mreflp.autoconfig;

import es.urjc.etsii.grafo.autoconfig.inventory.BlacklistInventoryFilter;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.improve.ls.LocalSearchCachedBestImprovement;
import java.util.Set;

/** Optional --blacklist=MREFLPInventoryFilterNew: supported move contracts without sequential composition. */
public final class MREFLPInventoryFilterNew extends BlacklistInventoryFilter {
    @Override public Set<Class<?>> getBlacklist() { return Set.of(LocalSearchCachedBestImprovement.class, Improver.SequentialImprover.class); }
}
