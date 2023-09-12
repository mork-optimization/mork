package es.urjc.etsii.grafo.metrics;

/**
 * Pairs of (instant, value)
 * @param value value to record
 * @param instant when was the value recorded? elapsed time in nanoseconds since a reference point.
 *                When used in the context of metrics, the reference point is the instant the algorithm started executing
 */
public record TimeValue(long instant, double value) implements Comparable<TimeValue>{

    @Override
    public int compareTo(TimeValue o) {
        return Long.compare(this.instant, o.instant);
    }
}
