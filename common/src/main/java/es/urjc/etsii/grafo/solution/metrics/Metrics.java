package es.urjc.etsii.grafo.solution.metrics;

import java.util.*;

/**
 * Stores metrics of different things that are happening while solving.
 * THIS CLASS IS NOT THREAD SAFE
 */
public class Metrics {
    public static final String OBJECTIVE_FUNCTION = "objective";
    public static final String BEST_OBJECTIVE_FUNCTION = "bestobjective";

    protected final Map<String, TreeSet<TimeValue>> metricValues;
    protected final long referencePoint;

    /**
     * Create a new metrics instance
     *
     * @param referenceTime all data points will be relative to this time, use the returned value of System.nanoTime()
     */
    protected Metrics(long referenceTime) {
        this.metricValues = new HashMap<>();
        this.referencePoint = referenceTime;
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
     * @param name  metric name, for example "numberOfNodesAssigned". See public fields such as {@link Metrics#OBJECTIVE_FUNCTION}
     * @param value value for the given metric
     */
    public void addDatapoint(String name, double value) {
        addDatapoint(name, value, System.nanoTime());
    }

    /**
     * Register the "value" for a metric named "name" has happened at the given "absoluteTime"
     *
     * @param name         metric name, for example "numberOfNodesAssigned". See public fields such as {@link Metrics#OBJECTIVE_FUNCTION}
     * @param value        value for the given metric
     * @param absoluteTime ¿when was the value retrieved or calculated? Use System.nanoTime() or equivalent
     */
    public void addDatapoint(String name, double value, long absoluteTime) {
        if (absoluteTime < this.referencePoint) {
            throw new IllegalArgumentException(String.format("Alg. start time (%s) is greater than the given time (%s)", this.referencePoint, absoluteTime));
        }
        long nanosSinceAlgStarted = absoluteTime - this.referencePoint;
        this.metricValues.computeIfAbsent(name, e -> new TreeSet<>());
        this.metricValues.get(name).add(new TimeValue(nanosSinceAlgStarted, value));
    }

    public TreeSet<TimeValue> byName(String name){
        if(!this.metricValues.containsKey(name)){
            throw new IllegalArgumentException("Unknown metric: " + name);
        }
        return this.metricValues.get(name);
    }

    /**
     * Calculate the area delimited between y = 0 and the given metric, in range [referenceTime+skipNanos, referenceTime+skipNanos+duration]
     * Should be extremely fast, with O(n) complexity, being N the number of data points for the given metric
     * @param name      metric to use
     * @param skipNanos ignore any datapoint whose timestamp is less than (referenceTime+skipNanos)
     * @param duration  pick only data points in range [referenceTime+skipNanos, referenceTime+skipNanos+duration]. Range is inclusive.
     * @return area calculation as a double value
     */
    public double hypervolume(String name, long skipNanos, long duration) {
        if (!this.metricValues.containsKey(name)) {
            throw new IllegalArgumentException("Unknown metric: " + name);
        }
        var set = this.metricValues.get(name);
        if (set.isEmpty()) {
            throw new IllegalArgumentException("Cannot calculate hypervolume if there are no datapoints, for metric " + name);
        }

        var startInterval = new TimeValue(skipNanos, -1);
        long endTime = Math.addExact(skipNanos, duration);

        // Find the latest point in time whose timestamp is equal or lower than the start range
        var previousElement = set.floor(startInterval);
        if (previousElement == null) {
            throw new IllegalArgumentException(String.format("Metric %s does not have any element before %s, first element is at %s", name, skipNanos, set.ceiling(startInterval)));
        }

        // All points are already relative to the referenceTime, we just need to skip those whose time value is less than skipNanos
        var filteredSet = set.tailSet(startInterval);

        // Keep track of the previous point (X, Y) coordinates
        double area = 0;
        double lastValue = previousElement.value();
        long lastTime = skipNanos;

        // While the time is lower than endTime, add to area the square delimited by time horizontally and value vertically
        for (var tv : filteredSet) {
            if (tv.timeElapsed() > endTime) {
                break;
            }
            area += (tv.timeElapsed() - lastTime) * lastValue;
            lastValue = tv.value();
            lastTime = tv.timeElapsed();
        }
        // Complete the area until the cutoff mark, the points may have ended earlier in time
        area += (endTime - lastTime) * lastValue;

        return area;
    }

    /**
     * Merge several metrics instances.
     * For example, if metrics A was created at 5, and metrics B reference time is 10,
     * the returned metrics will have 5 as reference point and all data points from metrics B will have their timeElapsed incremented by 5.
     *
     * @param metrics metric instances
     * @return new metric instance containing the data of all the metrics provided, with time corrected as necessary.
     */
    public static Metrics merge(Metrics... metrics) {
        return merge(Arrays.asList(metrics));
    }

    /**
     * Merge several metrics instances.
     * For example, if metrics A was created at 5, and metrics B reference time is 10,
     * the returned metrics will have 5 as reference point and all data points from metrics B will have their timeElapsed incremented by 5.
     *
     * @param metrics metric instances
     * @return new metric instance containing the data of all the metrics provided, with time corrected as necessary.
     */
    public static Metrics merge(Collection<Metrics> metrics) {
        long minTime = Long.MAX_VALUE;
        for (var m : metrics) {
            minTime = Math.min(minTime, m.referencePoint);
        }
        var newMetric = new Metrics(minTime);
        for (var m : metrics) {
            for (var e : m.metricValues.entrySet()) {
                String name = e.getKey();
                for (var timeValue : e.getValue()) {
                    newMetric.addDatapoint(name, timeValue.value(), timeValue.timeElapsed() + m.referencePoint);
                }
            }
        }
        return newMetric;
    }

}
