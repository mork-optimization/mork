package es.urjc.etsii.grafo.solution.stopping;

/**
 * Never stops
 */
public class DummyStop implements StopPoint {

    /**
     * Do nothing
     */
    @Override
    public void start() {

    }

    /**
     * Always false
     * @return false
     */
    @Override
    public boolean stop() {
        return false;
    }
}
