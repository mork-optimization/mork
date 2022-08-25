package es.urjc.etsii.grafo.solution.metrics;

/**
 * Pairs of (timeMoment, value)
 * @param timeElapsed when was this value taken? elapsed time since a reference point
 *                  in the context of a solution: time since the algorithm started executing
 * @param value value at the given timeElapsed
 */
public record TimeValue(long timeElapsed, double value) implements Comparable<TimeValue>{

    @Override
    public int compareTo(TimeValue o) {
        return Long.compare(this.timeElapsed, o.timeElapsed);
    }
}
