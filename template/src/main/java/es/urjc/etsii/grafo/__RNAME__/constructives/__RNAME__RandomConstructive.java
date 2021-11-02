package es.urjc.etsii.grafo.__RNAME__.constructives;

import es.urjc.etsii.grafo.__RNAME__.model.__RNAME__Instance;
import es.urjc.etsii.grafo.__RNAME__.model.__RNAME__Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;

public class __RNAME__RandomConstructive extends Constructive<__RNAME__Solution, __RNAME__Instance> {

    @Override
    public __RNAME__Solution construct(__RNAME__Solution solution) {
        // IN --> Empty solution from solution(instance) constructor
        // OUT --> Feasible solution with an assigned score
        // TODO: Implement random constructive


        // Remember to call solution.updateLastModifiedTime() if the solution is modified without using moves!!
        solution.updateLastModifiedTime();
        //return solution;
        throw new UnsupportedOperationException("RandomConstructive not implemented yet");
    }
}
