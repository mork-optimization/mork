package es.urjc.etsii.grafo.graphs;

import es.urjc.etsii.grafo.graphs.model.MSTInstance;
import es.urjc.etsii.grafo.graphs.model.MSTSolution;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;

public class Main {

    // Multiple "easy" graph problems are grouped in the same project as a demo
    // - MST: Minimum Spanning Tree
    // - SP: Shortest Paths
    // All reuse the same objective (minimize a double value)
    public static final Objective<?, MSTSolution, MSTInstance> OBJECTIVE =
            Objective.ofMinimizing("Score", MSTSolution::getScore, null);

    public static void main(String[] args) {
        Mork.start(args, OBJECTIVE);
    }
}
