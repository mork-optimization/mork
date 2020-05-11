package es.urjc.etsii.grafo.solution.stopping;

/**
 * Decides when the solution should stop being modified and finish the current algorithm-instance execution
 */
public interface StopPoint {

    boolean stop();
}
