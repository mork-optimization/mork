package es.urjc.etsii.grafo.mreflp.improve;

import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.improve.VND;
import es.urjc.etsii.grafo.mreflp.Main;
import es.urjc.etsii.grafo.mreflp.model.*;

/** The existing serial execution with a smaller initial autoconfig combination domain. */
public final class SequentialImproverNew extends Improver.SequentialImprover<MREFLPSolution, MREFLPInstance> {
    @AutoconfigConstructor
    @SafeVarargs
    @SuppressWarnings("varargs")
    public SequentialImproverNew(@ComponentParam(min = 2, max = 3,
            disallowed = {VND.class, Improver.SequentialImprover.class, Improver.NullImprover.class})
                                 Improver<MREFLPSolution, MREFLPInstance>... improvers) {
        super(Main.COST, improvers.clone());
        if (improvers.length < 2 || improvers.length > 3) throw new IllegalArgumentException("Expected two or three phases");
    }
}
