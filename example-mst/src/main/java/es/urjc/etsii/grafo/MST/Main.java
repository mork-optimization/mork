package es.urjc.etsii.grafo.MST;

import es.urjc.etsii.grafo.MST.model.MSTInstance;
import es.urjc.etsii.grafo.MST.model.MSTSolution;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;

public class Main {
    public static final Objective<?, MSTSolution, MSTInstance> OBJECTIVE =
            Objective.ofMinimizing("SpanningTree", MSTSolution::getScore, null);

    public static void main(String[] args) {
        Mork.start(args, OBJECTIVE);
    }
}
