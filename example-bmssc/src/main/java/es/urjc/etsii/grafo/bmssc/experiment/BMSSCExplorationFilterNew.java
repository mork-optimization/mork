package es.urjc.etsii.grafo.bmssc.experiment;

import es.urjc.etsii.grafo.autoconfig.generator.ExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.generator.TreeContext;
import es.urjc.etsii.grafo.bmssc.improve.ShakeImprover;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.improve.VND;
import es.urjc.etsii.grafo.improve.ls.LocalSearch;

/** Keep improver composition flat enough to export the complete irace search space. */
public class BMSSCExplorationFilterNew extends ExplorationFilter {
    @Override
    public boolean reject(TreeContext context, Class<?> currentComponent) {
        if (!Improver.class.isAssignableFrom(currentComponent)) return false;
        for (var ancestor : context.branch()) {
            // Three configurable searches already cover the custom swap searches. Keeping only these
            // as list entries also blocks indirect nesting through ShakeImprover, not just direct VND/sequence nesting.
            boolean insideSequence = VND.class.isAssignableFrom(ancestor)
                    || Improver.SequentialImprover.class.isAssignableFrom(ancestor);
            if (insideSequence && !LocalSearch.class.isAssignableFrom(currentComponent)) return true;

            // Shake(VND(searches)) and Shake(Sequential(searches)) remain available, but never Shake(Shake(...)).
            if (ShakeImprover.class.isAssignableFrom(ancestor) && ShakeImprover.class.isAssignableFrom(currentComponent)) return true;
        }
        return false;
    }
}
