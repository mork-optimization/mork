package es.urjc.etsii.grafo.metrics;

import es.urjc.etsii.grafo.algorithms.FMode;

/**
 * Metric that stores the best objective values seen so far
 */
public class DeclaredObjective extends AbstractMetric {

    private final String name;
    private final FMode fMode;

    /**
     * <p>Constructor for BestObjective.</p>
     * @param fmode FMode to use when comparing values
     * @param referenceInstant nanoTime representing the start of the experiment, or an arbitrary point in time
     *                         used to relativize all other times when adding values to the metric
     */
    public DeclaredObjective(String name, FMode fmode, long referenceInstant) {
        super(referenceInstant);
        this.name = name;
        this.fMode = fmode;
    }

    @Override
    public synchronized void add(long instant, double value) {
        var t = new TimeValue(instant, value);
        // Datapoint is inserted only if it improves the curve
        var previous = this.values.floor(t);
        if(previous == null){
            super.add(instant, value);
        } else if(fMode.isBetter(value, previous.value())){
            // Remove all points that are worse than the new one
            for (var iterator = this.values.tailSet(previous, false).iterator(); iterator.hasNext(); ) {
                var tv = iterator.next();
                if (fMode.isBetterOrEqual(value, tv.value())) {
                    iterator.remove();
                } else {
                    break;
                }
            }
            super.add(instant, value);
        }
    }

    @Override
    public void add(double value) {
        if(values.isEmpty() || fMode.isBetter(value, values.last().value())){
            super.add(value);
        }
    }
}
