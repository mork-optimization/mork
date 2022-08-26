package es.urjc.etsii.grafo.solution.metrics;

/**
 * Manages metrics instances. Example usage:
 * - Call {@link MetricsManager#resetMetrics()} to initialize a new instance of empty metrics
 * - Run the algorithm. Any algorithm component can get the current metrics instance using {@link MetricsManager#getInstance()}, and add data points to it using {@link Metrics#addDatapoint(String, double)}.
 * - Do something with the metrics after the algorithm finishes, for example merging (see below).
 * - Reset metrics before the next algorithm start executing.
 *
 * Note that metrics are always ThreadLocal, which means that every thread works on its own independent copy.
 * Metrics are always disabled by default, and must be enabled by either the framework or manually by the user
 * Tip: Metrics from different threads can later be merged using {@link Metrics#merge(Metrics...)}
 */
public final class MetricsManager {

    private static ThreadLocal<Metrics> localMetrics = new ThreadLocal<>();
    private static volatile boolean enabled = false;

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

    /**
     * Register the "value" for a metric named "name" has happened at the given "absoluteTime"
     * If the metrics are not enabled, does nothing
     * @param name         metric name, for example "numberOfNodesAssigned". See public fields such as {@link Metrics#OBJECTIVE_FUNCTION}
     * @param value        value for the given metric
     * @param absoluteTime ¿when was the value retrieved or calculated? Use System.nanoTime() or equivalent
     */
    public static void addDatapoint(String name, double value, long absoluteTime){
        if(!areMetricsEnabled()){
            return;
        }
        var metrics = getInstance();
        metrics.addDatapoint(name, value, absoluteTime);
    }

    /**
     * Register the "value" for a metric named "name" has happened at the given "absoluteTime"
     * If the metrics are not enabled, does nothing
     * @param name         metric name, for example "numberOfNodesAssigned". See public fields such as {@link Metrics#OBJECTIVE_FUNCTION}
     * @param value        value for the given metric
     */
    public static void addDatapoint(String name, double value){
        if(!areMetricsEnabled()){
            return;
        }
        var metrics = getInstance();
        metrics.addDatapoint(name, value);
    }
}
