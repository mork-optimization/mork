package es.urjc.etsii.grafo.tsptw.autoconfig;

import es.urjc.etsii.grafo.autoconfig.generator.ExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.generator.TreeContext;
import es.urjc.etsii.grafo.tsptw.alg.GVNS;
import es.urjc.etsii.grafo.tsptw.repair.TSPTWFeasibilityRepair;
import es.urjc.etsii.grafo.tsptw.repair.TSPTWFeasibilityRepairFullNew;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/** GVNS with a fixed full repair bundle by default; legacy mode also permits configurable repairs. */
public class TSPTWExplorationFilter extends ExplorationFilter {
    private final boolean legacyRepairSpace;

    public TSPTWExplorationFilter() {
        this(false);
    }

    @Autowired
    public TSPTWExplorationFilter(@Value("${tsptw.legacy-repair-space:false}") boolean legacyRepairSpace) {
        this.legacyRepairSpace = legacyRepairSpace;
    }

    @Override
    public boolean reject(TreeContext context, Class<?> component) {
        if (context.branch().isEmpty()) return component != GVNS.class;
        return !legacyRepairSpace && TSPTWFeasibilityRepair.class.isAssignableFrom(component)
                && component != TSPTWFeasibilityRepairFullNew.class;
    }
}
