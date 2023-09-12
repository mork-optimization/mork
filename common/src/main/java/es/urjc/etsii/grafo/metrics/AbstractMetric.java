package es.urjc.etsii.grafo.metrics;

import java.util.TreeSet;

public abstract class AbstractMetric {
    protected final TreeSet<TimeValue> values;
    private final long referenceNanoTime;

    protected AbstractMetric(long referenceNanoTime) {
        this.referenceNanoTime = referenceNanoTime;
        this.values = new TreeSet<>();
    }

    /**
     * What is the name of the current metric
     * @return metric name, defaults to current class name if not overriden
     */
    public String getName(){
        return this.getClass().getSimpleName();
    }

    public void add(long instant, double value){
        values.add(new TimeValue(instant- referenceNanoTime, value));
    }

    public void add(double value){
        if(referenceNanoTime == MetricsStorage.NO_REF){
            throw new IllegalStateException("Cannot add data point without reference instant");
        }
        values.add(new TimeValue(System.nanoTime() - referenceNanoTime, value));
    }

    public TreeSet<TimeValue> getValues() {
        return values;
    }
}
