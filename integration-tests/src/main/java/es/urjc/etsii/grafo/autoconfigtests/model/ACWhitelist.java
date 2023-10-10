package es.urjc.etsii.grafo.autoconfigtests.model;

import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.autoconfig.inventory.WhitelistInventoryFilter;
import es.urjc.etsii.grafo.autoconfigtests.components.FasterInvertedConstructive;
import es.urjc.etsii.grafo.autoconfigtests.components.FlippyFlopImprover;
import es.urjc.etsii.grafo.autoconfigtests.components.SlowConstructive;

import java.util.Set;

public class ACWhitelist extends WhitelistInventoryFilter {
    @Override
    public Set<Class<?>> getWhitelist() {
        return Set.of(
                SimpleAlgorithm.class,
                SlowConstructive.class,
                FasterInvertedConstructive.class,
                FlippyFlopImprover.class
        );
    }
}
