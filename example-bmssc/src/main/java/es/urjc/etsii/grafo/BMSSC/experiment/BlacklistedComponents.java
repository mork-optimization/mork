package es.urjc.etsii.grafo.BMSSC.experiment;

import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.autoconfig.inventory.BlacklistInventoryFilter;

import java.util.Set;

public class BlacklistedComponents extends BlacklistInventoryFilter {
    @Override
    public Set<Class<?>> getBlacklist() {
        return Set.of(SimpleAlgorithm.class);
    }
}
