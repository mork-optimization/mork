package es.urjc.etsii.grafo.tsptw.autoconfig;

import es.urjc.etsii.grafo.autoconfig.generator.ExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.generator.TreeContext;

/** Project exploration rules; accepts every component by default. */
public class TSPTWExplorationFilter extends ExplorationFilter {
    @Override
    public boolean reject(TreeContext context, Class<?> component) {
        return false;
    }
}
