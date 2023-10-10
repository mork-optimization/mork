package es.urjc.etsii.tsptests;

import es.urjc.etsii.grafo.algorithms.scattersearch.ScatterSearch;
import es.urjc.etsii.grafo.autoconfig.inventory.BlacklistInventoryFilter;

import java.util.Set;

public class TSPBlacklist extends BlacklistInventoryFilter {
    @Override
    public Set<Class<?>> getBlacklist() {
        return Set.of(ScatterSearch.class);
    }
}
