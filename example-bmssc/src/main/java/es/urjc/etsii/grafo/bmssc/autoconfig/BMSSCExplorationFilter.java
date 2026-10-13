package es.urjc.etsii.grafo.bmssc.autoconfig;

import es.urjc.etsii.grafo.autoconfig.generator.ExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.generator.TreeContext;

/** Project exploration rules; accepts every component by default. */
public class BMSSCExplorationFilter extends ExplorationFilter {
    @Override
    public boolean reject(TreeContext context, Class<?> component) {
        return false;
    }
}
