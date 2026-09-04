package es.urjc.etsii.grafo.graphs;

import es.urjc.etsii.grafo.graphs.model.MSTInstance;
import es.urjc.etsii.grafo.graphs.model.MSTSolution;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solver.Mork;

public class Main {

    // Multiple graph problems are grouped in the same project as a demo
    // - MST: Minimum Spanning Tree (easy, exact algorithms: Kruskal, Prim)
    // - SP: Shortest Paths (easy, exact algorithm: Dijkstra, Floyd-Warshall)
    // - MVC: Minimum Vertex Cover (NP-hard, solved with the CMSA metaheuristic)
    // All reuse the same objective (minimize a double value) and solution class
    public static final Objective<?, MSTSolution, MSTInstance> OBJECTIVE =
            Objective.ofMinimizing("Score", MSTSolution::getScore);

    public static void main(String[] args) {
        Mork.start(args, OBJECTIVE);
    }
}
