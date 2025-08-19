package es.urjc.etsii.grafo.BMSSC.experiment;

import es.urjc.etsii.grafo.autoconfig.generator.ExplorationFilter;
import es.urjc.etsii.grafo.autoconfig.generator.TreeContext;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.improve.VND;
import org.slf4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class BMSSCExplorationFilter extends ExplorationFilter {

    private static Logger log = org.slf4j.LoggerFactory.getLogger(BMSSCExplorationFilter.class);
    private record ClassPair(Class<?> a, Class<?> b) {}
    private final Set<ClassPair> blockedRelationships = new HashSet<>();

    public BMSSCExplorationFilter() {
        // VND cannot have sequential improvers inside, use either one or the other
        this.blockedRelationships.add(new ClassPair(VND.class, Improver.SequentialImprover.class));
        this.blockedRelationships.add(new ClassPair(Improver.SequentialImprover.class, VND.class));

        // VND should not use a NullImprover, as it is almost the same as a SequentialImprover with two improvers.
        this.blockedRelationships.add(new ClassPair(VND.class, Improver.NullImprover.class));
    }

    @Override
    public boolean reject(TreeContext context, Class<?> currentComponent) {
        for(var parent: context.branch()){
            var pair = new ClassPair(parent, currentComponent);
            if(blockedRelationships.contains(pair)){
                log.trace("Rejecting {} because relationship {} is blocked", currentComponent.getSimpleName(), pair);
                return true;
            }
        }
        return false;
    }
}
