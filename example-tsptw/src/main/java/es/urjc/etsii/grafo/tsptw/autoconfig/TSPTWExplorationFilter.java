package es.urjc.etsii.grafo.tsptw.autoconfig;

import es.urjc.etsii.grafo.autoconfig.generator.ExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.generator.TreeContext;
import es.urjc.etsii.grafo.tsptw.alg.GVNS;

/** Restrict roots to GVNS. Its typed dependencies enforce each component's feasibility contract. */
public class TSPTWExplorationFilter extends ExplorationFilter {
    @Override
    public boolean reject(TreeContext context, Class<?> component) {
        return context.branch().isEmpty() && component != GVNS.class;
    }
}
