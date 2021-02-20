//package es.urjc.etsii.grafo.solution.stopping;
//
///**
// * Decides when the solution should stop being modified and finish the current algorithm-instance execution.
// * Several criteria are possible, ex: time-based, etc
// */
//public interface StopPoint {
//
//    /**
//     * Notify when we start working on the current solution
//     */
//    void start();
//
//    /**
//     * Check if a StopPoint has already been started (to avoid initializing it twice)
//     * @return true if the StopPoint has already been initialized, false otherwise
//     */
//    boolean isStarted();
//
//
//    /**
//     * Check if we should stop working on the current solution
//     * @return true if we should stop working on the current solution, false otherwise
//     */
//    boolean stop();
//}
