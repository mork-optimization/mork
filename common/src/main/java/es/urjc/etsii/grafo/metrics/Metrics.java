package es.urjc.etsii.grafo.metrics;

import java.util.*;
import java.util.function.Supplier;

/**
 * Stores metrics of different things that are happening while solving.
 * THIS CLASS IS NOT THREAD SAFE
 */
public class Metrics {

    protected final Map<String, AbstractMetric> metrics;
    protected final Map<String, Supplier<? extends AbstractMetric>> suppliers;
    protected final long referenceTime;

    /**
     * Create a new metrics instance
     *
     * @param referenceTime all data points will be relative to this time, use the returned value of System.nanoTime()
     */
    protected Metrics(long referenceTime) {
        this.metrics = new HashMap<>();
        this.suppliers = new HashMap<>();
        this.referenceTime = referenceTime;
    }

    /**
     * Create a new metrics instance taking the current nanoTime as a reference point
     */
    protected Metrics() {
        this(System.nanoTime());
    }

    /**
     * Register the "value" for a metric named "name" has happened at the current time, measured using System.nanoTime().
     *
     * @param name  metric name, for example "numberOfNodesAssigned".
     * @param initializeMetric Method that initializes the metric if it does not exist yet
     * @param value value for the given metric
     */
    public void addDatapoint(String name, Supplier<? extends AbstractMetric> initializeMetric, double value) {
        addDatapoint(name, initializeMetric, value, System.nanoTime());
    }

    /**
     * Register the "value" for a metric named "name" has happened at the given "absoluteTime"
     *
     * @param name             metric name, for example "numberOfNodesAssigned".
     * @param initializeMetric
     * @param value            value for the given metric
     * @param absoluteTime     ¿when was the value retrieved or calculated? Use System.nanoTime() or equivalent
     */
    public void addDatapoint(String name, Supplier<? extends AbstractMetric> initializeMetric, double value, long absoluteTime) {
        if (absoluteTime < this.referenceTime) {
            throw new IllegalArgumentException(String.format("Alg. start time (%s) is greater than the given time (%s)", this.referenceTime, absoluteTime));
        }
        long nanosSinceAlgStarted = absoluteTime - this.referenceTime;
        if(!this.metrics.containsKey(name)){
            this.metrics.put(name, initializeMetric.get());
            this.suppliers.put(name, initializeMetric);
        }
        this.metrics.get(name).addDatapoint(value, nanosSinceAlgStarted);
    }

    // TODO reimplement merge metrics
//    /**
//     * Merge several metrics instances.
//     * For example, if metrics A was created at 5, and metrics B reference time is 10,
//     * the returned metrics will have 5 as reference point and all data points from metrics B will have their timeElapsed incremented by 5.
//     *
//     * @param metrics metric instances
//     * @return new metric instance containing the data of all the metrics provided, with time corrected as necessary.
//     */
//    public static Metrics merge(Metrics... metrics) {
//        return merge(Arrays.asList(metrics));
//    }
//
//    /**
//     * Merge several metrics instances.
//     * For example, if metrics A was created at 5, and metrics B reference time is 10,
//     * the returned metrics will have 5 as reference point and all data points from metrics B will have their timeElapsed incremented by 5.
//     *
//     * @param metrics metric instances
//     * @return new metric instance containing the data of all the metrics provided, with time corrected as necessary.
//     */
//    public static Metrics merge(Iterable<Metrics> metrics) {
//        long minTime = Long.MAX_VALUE;
//        for (var m : metrics) {
//            minTime = Math.min(minTime, m.referenceTime);
//        }
//        var newMetric = new Metrics(minTime);
//        for (var m : metrics) {
//            for (var e : m.metrics.entrySet()) {
//                String name = e.getKey();
//                for (var timeValue : e.getValue()) {
//                    newMetric.addDatapoint(name, timeValue.value(), timeValue.timeElapsed() + m.referenceTime);
//                }
//            }
//        }
//        return newMetric;
//    }

}
