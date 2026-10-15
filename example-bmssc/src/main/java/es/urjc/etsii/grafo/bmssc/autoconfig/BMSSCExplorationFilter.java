package es.urjc.etsii.grafo.bmssc.autoconfig;

import es.urjc.etsii.grafo.autoconfig.generator.ExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.generator.TreeContext;
import es.urjc.etsii.grafo.bmssc.improve.ShakeImprover;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.improve.VND;
import es.urjc.etsii.grafo.improve.ls.LocalSearch;

/** Keeps VND lists limited to local searches and excludes redundant wrappers. */
public class BMSSCExplorationFilter extends ExplorationFilter {
    @Override
    public boolean reject(TreeContext context, Class<?> component) {
        if (component == Improver.SequentialImprover.class) {
            return true;
        }
        if (!Improver.class.isAssignableFrom(component)) {
            return false;
        }
        for (Class<?> ancestor : context.branch()) {
            if (VND.class.isAssignableFrom(ancestor) && !LocalSearch.class.isAssignableFrom(component)) {
                return true;
            }
            if (ShakeImprover.class.isAssignableFrom(ancestor) && ShakeImprover.class.isAssignableFrom(component)) {
                return true;
            }
        }
        return false;
    }
}
