package es.urjc.etsii.grafo.mreflp.cmsa;

import es.urjc.etsii.grafo.algorithms.cmsa.CMSAConstructive;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.mreflp.create.MREFLPConstructiveNew;
import es.urjc.etsii.grafo.mreflp.model.*;
import java.util.*;

public final class MREFLPCMSAConstructiveNew extends CMSAConstructive<MREFLPSolution, MREFLPInstance, MREFLPAssignmentNew> {
    private final MREFLPConstructiveNew constructive;
    @AutoconfigConstructor
    public MREFLPCMSAConstructiveNew(MREFLPConstructiveNew constructive) {
        this.constructive = Objects.requireNonNull(constructive);
    }
    @Override public MREFLPSolution construct(MREFLPSolution solution) { return constructive.construct(solution); }
    @Override public Set<MREFLPAssignmentNew> usedComponents(MREFLPSolution solution) {
        var components = new HashSet<MREFLPAssignmentNew>();
        for (int v = 0; v < solution.getInstance().n(); v++) {
            if (solution.group(v) < 0) throw new IllegalArgumentException("Incomplete CMSA solution");
            components.add(new MREFLPAssignmentNew(v, solution.group(v)));
        }
        return Set.copyOf(components);
    }
}
