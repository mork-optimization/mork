package es.urjc.etsii.grafo.autoconfigtests;

import es.urjc.etsii.grafo.autoconfigtests.model.ACInstance;
import es.urjc.etsii.grafo.autoconfigtests.model.ACSolution;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;

public class Main {
    public static final Objective<?, ACSolution, ACInstance> AC_OBJECTIVE = Objective.ofMaximizing("TestMaximize", ACSolution::getScore, null);

    public static void main(String[] args) {
        Mork.start(args, AC_OBJECTIVE);
    }
}
