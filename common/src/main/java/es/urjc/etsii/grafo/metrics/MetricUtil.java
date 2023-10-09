package es.urjc.etsii.grafo.metrics;

import es.urjc.etsii.grafo.util.DoubleComparator;

public class MetricUtil {

    /**
     * Calculate the area delimited between y = 0 and the given metric, in range [referenceTime+skipNanos, referenceTime+skipNanos+duration]
     * Should be extremely fast, with O(n) complexity, being N the number of data points for the given metric.
     *
     * @param metricType metric type
     * @param skipNanos  ignore any datapoint whose timestamp is less than (referenceTime+skipNanos).
     * @param duration   pick only data points in range [referenceTime+skipNanos, referenceTime+skipNanos+duration]. Range is inclusive.
     * @param logScale   Scale area under curve using natural logarithm
     * @return area calculation as a double value
     * @throws IllegalArgumentException if the area is unbounded or if the metric does not contain at least 1 datapoint.
     */
    public static double areaUnderCurve(Class<? extends AbstractMetric> metricType, long skipNanos, long duration, boolean logScale) {
        return areaUnderCurve(metricType.getSimpleName(), skipNanos, duration, logScale);
    }

    /**
     * Calculate the area delimited between y = 0 and the given metric, in range [referenceTime+skipNanos, referenceTime+skipNanos+duration]
     * Should be extremely fast, with O(n) complexity, being N the number of data points for the given metric.
     *
     * @param metricName metric name
     * @param skipNanos  ignore any datapoint whose timestamp is less than (referenceTime+skipNanos).
     * @param duration   pick only data points in range [referenceTime+skipNanos, referenceTime+skipNanos+duration]. Range is inclusive.
     * @param logScale   Scale area under curve using natural logarithm
     * @return area calculation as a double value
     * @throws IllegalArgumentException if the area is unbounded or if the metric does not contain at least 1 datapoint.
     */
    public static double areaUnderCurve(String metricName, long skipNanos, long duration, boolean logScale) {
        return areaUnderCurve(Metrics.get(metricName), skipNanos, duration, logScale);
    }

    /**
     * Calculate the area delimited between y = 0 and the given metric, in range [referenceTime+skipNanos, referenceTime+skipNanos+duration]
     * Should be extremely fast, with O(n) complexity, being N the number of data points for the given metric.
     *
     * @param metric    metric instance
     * @param skipNanos ignore any datapoint whose timestamp is less than (referenceTime+skipNanos).
     * @param duration  pick only data points in range [referenceTime+skipNanos, referenceTime+skipNanos+duration]. Range is inclusive.
     * @param logScale  Scale area under curve using natural logarithm
     * @return area calculation as a double value
     * @throws IllegalArgumentException if the area is unbounded or if the metric does not contain at least 1 datapoint.
     */
    public static double areaUnderCurve(AbstractMetric metric, long skipNanos, long duration, boolean logScale) {
        var set = metric.values;
        var name = metric.getName();
        if (set.isEmpty()) {
            throw new IllegalArgumentException("Cannot calculate area under curve if there are no values recorded for metric " + name);
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
            if (tv.instant() > endTime) {
                break;
            }
            // Calculate area of current rectangle
            var height = lastValue;
            if(DoubleComparator.isNegative(height)){
                throw new IllegalArgumentException("Negative f.o value detected in metric " + name + " at time " + tv.instant() + " with value " + height);
            }
            area += (tv.instant() - lastTime) * height;
            // Advance limit
            lastValue = tv.value();
            lastTime = tv.instant();
        }
        // Complete the area until the right cutoff mark
        var height = lastValue;
        area += (endTime - lastTime) * height;

        if(!logScale){
            return area;
        }
        if(area > 0){
            return Math.log(area);
        }
        if(area < 0 || !Double.isFinite(area)){
            throw new IllegalArgumentException("Cannot calculate AUC for metric " + name + ", invalid result: " + area);
        }
        throw new AssertionError("Should be impossible to reach");
    }
}
