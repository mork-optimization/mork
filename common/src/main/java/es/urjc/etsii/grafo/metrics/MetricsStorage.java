package es.urjc.etsii.grafo.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores metrics of different things that are happening while solving.
 * THIS CLASS IS NOT THREAD SAFE
 */
public class MetricsStorage {

    public static final long NO_REF = -1;
    protected final Map<String, AbstractMetric> metrics;
    protected final long referenceNanoTime;

    /**
     * Create a new metrics instance
     *
     * @param referenceNanoTime all data points will be relative to this time, use the returned value of System.nanoTime()
     */
    public MetricsStorage(long referenceNanoTime) {
        this.metrics = new ConcurrentHashMap<>();
        this.referenceNanoTime = referenceNanoTime;
    }

    /**
     * Create a new metrics instance taking the current nanoTime as a reference point
     */
    public MetricsStorage() {
        this(System.nanoTime());
    }

    public Map<String, AbstractMetric> getMetrics() {
        return metrics;
    }

    public long getReferenceNanoTime() {
        return referenceNanoTime;
    }
}
