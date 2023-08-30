package es.urjc.etsii.grafo.metrics;

import java.util.TreeSet;

public abstract class AbstractMetric {
    protected final TreeSet<TimeValue> values;

    protected AbstractMetric() {
        this.values = new TreeSet<>();
    }

    /**
     * What is the name of the current metric
     * @return metric name, defaults to current class name if not overriden
     */
    public String getName(){
        return this.getClass().getSimpleName();
    }

    public void addDatapoint(double value, long absoluteTime){
        values.add(new TimeValue(absoluteTime, value));
    }
}
