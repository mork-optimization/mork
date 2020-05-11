package es.urjc.etsii.grafo.solution.stopping;

/**
 * Never stops
 */
public class DummyStop implements StopPoint {

    /**
     * Always false
     * @return false
     */
    @Override
    public boolean stop() {
        return false;
    }
}
