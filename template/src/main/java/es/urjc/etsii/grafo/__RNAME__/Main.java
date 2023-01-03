package es.urjc.etsii.grafo.__RNAME__;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.solver.Mork;

public class Main {
    public static void main(String[] args) {
        // TODO Configure if maximizing or minimizing objective function.
        Mork.start(args, FMode.MINIMIZE);
    }
}
