package es.urjc.etsii.grafo.TSP;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.solver.Mork;

public class Main {
    public static void main(String[] args) {
        Mork.start(args, FMode.MINIMIZE);
    }
}
