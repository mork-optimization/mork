package es.urjc.etsii.grafo.metrics.timing;

/** A recorder owned by one execution thread. Implementations must not perform disk I/O here. */
public interface TimeStatsRecorder extends AutoCloseable {
    boolean isActive();
    void record(TimeStatsMethod method, long enter, long exit);
    @Override
    void close();
}
