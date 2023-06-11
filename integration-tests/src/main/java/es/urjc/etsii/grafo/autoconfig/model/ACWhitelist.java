package es.urjc.etsii.grafo.autoconfig.model;

import es.urjc.etsii.grafo.algorithms.SimpleAlgorithm;
import es.urjc.etsii.grafo.autoconfig.components.FasterInvertedConstructive;
import es.urjc.etsii.grafo.autoconfig.components.FlippyFlopImprover;
import es.urjc.etsii.grafo.autoconfig.components.SlowConstructive;
import es.urjc.etsii.grafo.autoconfig.service.filter.WhitelistFilterStrategy;

import java.util.Set;

public class ACWhitelist extends WhitelistFilterStrategy {
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
