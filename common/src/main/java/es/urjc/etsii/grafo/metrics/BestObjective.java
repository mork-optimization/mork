package es.urjc.etsii.grafo.metrics;

import es.urjc.etsii.grafo.algorithms.FMode;

public class BestObjective extends AbstractMetric {

    private final FMode fMode;

    public BestObjective(long referenceInstant) {
        super(referenceInstant);
        this.fMode = Metrics.getFMode();
    }

    @Override
    public void add(long instant, double value) {
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
