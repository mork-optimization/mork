package es.urjc.etsii.grafo.metrics;

import es.urjc.etsii.grafo.algorithms.FMode;

import java.util.Map;
import java.util.TreeSet;

/**
 * Manages metrics instances. Example usage:
 * - Call {@link MetricsManager#resetMetrics()} to initialize a new instance of empty metrics
 * - Run the algorithm. Any algorithm component can get the current metrics instance using {@link MetricsManager#getInstance()}, and add data points to it using {@link AbstractMetric#addDatapoint(double, long)}.
 * - Do something with the metrics after the algorithm finishes, for example merging (see below).
 * - Reset metrics before the next algorithm start executing.
 * <p>
 * Note that metrics are always ThreadLocal, which means that every thread works on its own independent copy.
 * Metrics are always disabled by default, and must be enabled by either the framework or manually by the user
 * Tip: Metrics from different threads can later be merged using {@link Metrics#merge(Metrics...)}
 */
public final class MetricsManager {

    private static ThreadLocal<Metrics> localMetrics = new ThreadLocal<>();
    private static volatile boolean enabled = false;
    private static FMode fmode;

    private MetricsManager(){}

    /**
     * Get current metrics instance
     * @return metrics instance for the current thread
     * @throws IllegalStateException if the metrics have not been initialized for the current thread, or if the metrics are disabled
     */
    public static Metrics getInstance(){
        checkEnabled();
        Metrics metrics = localMetrics.get();
        if(metrics == null){
            throw new IllegalStateException("Called MetricsManager::getInstance before MetricsManager::resetMetrics");
        }
        return metrics;
    }

    private static void checkEnabled() {
        if(!enabled){
            throw new IllegalStateException("Metrics are disabled, enable them first");
        }
    }

    /**
     * Initialize a new metrics object for use in the current thread
     * @throws IllegalStateException if the metrics are disabled
     */
    public static void resetMetrics(){
        checkEnabled();
        localMetrics.set(new Metrics());
    }

    /**
     * Initialize a new metrics object for use in the current thread
     * @throws IllegalStateException if the metrics are disabled
     * @param referenceTime reference time, see {@link Metrics#Metrics(long)} for a more detailed explanation.
     */
    public static void resetMetrics(long referenceTime){
        checkEnabled();
        localMetrics.set(new Metrics(referenceTime));
    }

    /**
     * Enable metrics
     */
    public static void enableMetrics(){
        enabled = true;
    }

    /**
     * Disable metrics
     */
    public static void disableMetrics(){
        localMetrics.remove();
        enabled = false;
    }

    /**
     * Return true if metrics are enabled, false otherwise.
     * @return true if metrics are enabled, false otherwise
     */
    public static boolean areMetricsEnabled(){
        return enabled;
    }


    public static void setSolvingMode(FMode fmode) {
        MetricsManager.fmode = fmode;
    }

    public static FMode getFMode(){
        return fmode;
    }

    /**
     * Get all metric data generated in current context.
     * The map is not copied for performance, it should NEVER be modified.
     * @return raw metric data
     */
    public static Map<String, AbstractMetric> getAllMetricData(){
        return getInstance().metrics;
    }

    public AbstractMetric getMetric(String name){
        var metrics = getInstance().metrics;
        if(!metrics.containsKey(name)){
            throw new IllegalArgumentException("Unknown metric: " + name);
        }
        return metrics.get(name);
    }
}
