package es.urjc.etsii.grafo.metrics;

public class MetricUtil {
    /**
     * Calculate the area delimited between y = 0 and the given metric, in range [referenceTime+skipNanos, referenceTime+skipNanos+duration]
     * Should be extremely fast, with O(n) complexity, being N the number of data points for the given metric
     * @param metric metric instance
     * @param skipNanos ignore any datapoint whose timestamp is less than (referenceTime+skipNanos)
     * @param duration  pick only data points in range [referenceTime+skipNanos, referenceTime+skipNanos+duration]. Range is inclusive.
     * @return area calculation as a double value
     */
    public double areaUnderCurve(AbstractMetric metric, long skipNanos, long duration) {
        var set = metric.values;
        var name = metric.getName();
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
}
