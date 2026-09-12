package es.urjc.etsii.grafo.bmssc.experiment;

import es.urjc.etsii.grafo.autoconfig.inventory.BlacklistInventoryFilter;
import es.urjc.etsii.grafo.bmssc.model.sol.ReassignNeighborhood;
import es.urjc.etsii.grafo.create.grasp.GreedyRandomGRASPConstructive;
import es.urjc.etsii.grafo.create.grasp.RandomGreedyGRASPConstructive;

import java.util.Set;

public class BlacklistedComponents extends BlacklistInventoryFilter {
    @Override
    public Set<Class<?>> getBlacklist() {
        // Construction must use the BMSSC wrapper; reassignment belongs inside the shake.
        return Set.of(GreedyRandomGRASPConstructive.class, RandomGreedyGRASPConstructive.class,
                ReassignNeighborhood.class);
    }
}
