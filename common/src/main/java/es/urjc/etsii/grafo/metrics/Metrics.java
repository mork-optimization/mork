package es.urjc.etsii.grafo.metrics;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;

import java.util.*;
import java.util.function.Function;

/**
 * Manages metrics instances. Example usage:
 * - Call {@link Metrics#resetMetrics()} to initialize a new instance of empty metrics
 * - Run the algorithm. Any algorithm component can get the current metrics instance using {@link Metrics#getCurrentThreadMetrics()}, and add data points to it using {@link AbstractMetric#add(long, double)}.
 * - Do something with the metrics after the algorithm finishes, for example merging (see below).
 * - Reset metrics before the next algorithm start executing.
 * <p>
 * Note that metrics are always ThreadLocal, which means that every thread works on its own independent copy.
 * Metrics are always disabled by default, and must be enabled by either the framework or manually by the user
 * Metrics from different threads can later be merged using {@link Metrics#merge(MetricsStorage...)}
 */
public final class Metrics {

    private static final Map<String, Function<Long, ? extends AbstractMetric>> initializers = new HashMap<>();
    private static InheritableThreadLocal<MetricsStorage> localMetrics = new InheritableThreadLocal<>();
    private static volatile boolean enabled = false;

    private Metrics(){}

    /**
     * Get metrics collected by the current thread.
     * @return metrics instance for the current thread
     * @throws IllegalStateException if the metrics have not been initialized for the current thread, or if the metrics are disabled
     */
    public static MetricsStorage getCurrentThreadMetrics(){
        checkEnabled();
        MetricsStorage metricsStorage = localMetrics.get();
        if(metricsStorage == null){
            throw new IllegalStateException("Called Metrics::getCurrentThreadMetrics before Metrics::resetMetrics");
        }
        return metricsStorage;
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
        localMetrics.set(new MetricsStorage());
    }

    /**
     * Initialize a new metrics object for use in the current thread
     * @throws IllegalStateException if the metrics are disabled
     * @param referenceTime reference time, see {@link MetricsStorage#MetricsStorage(long)} for a more detailed explanation.
     */
    public static void resetMetrics(long referenceTime){
        checkEnabled();
        localMetrics.set(new MetricsStorage(referenceTime));
    }

    /**
     * Enable metrics
     */
    public static void enableMetrics(){
        enabled = true;
    }

    /**
     * Disable metrics and empty all internal data structures
     */
    public static void disableMetrics(){
        localMetrics.remove();
        initializers.clear();
        enabled = false;
    }

    /**
     * Return true if metrics are enabled, false otherwise.
     * @return true if metrics are enabled, false otherwise
     */
    public static boolean areMetricsEnabled(){
        return enabled;
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractMetric> T get(String metricName){
        var storage = getCurrentThreadMetrics();
        ensureMetricInitialized(metricName, storage);
        // user is responsible for ensuring that the metric name corresponds with the expected type
        return (T) storage.metrics.get(metricName);
    }

    private static void ensureMetricInitialized(String metricName, MetricsStorage storage) {
        if(!storage.metrics.containsKey(metricName)){
            // If initializer is present create, else fail because user forgot to register their custom metric
            if(initializers.containsKey(metricName)){
                storage.metrics.put(metricName, initializers.get(metricName).apply(storage.referenceNanoTime));
            } else {
                throw new IllegalArgumentException("Unregistered metric: %s, did you forgot to register it?".formatted(metricName));
            }
        }
    }

    public static <T extends AbstractMetric> T get(Class<T> metric){
        return get(metric.getSimpleName());
    }

    /**
     * Register a new metric so the framework automatically tracks it
     * @param metricName metric name
     * @param initializer method to initialize the given metric
     * @param <T> metric type
     */
    public static <T extends AbstractMetric> void register(String metricName, Function<Long, T> initializer){
        if(initializers.containsKey(metricName)){
            throw new IllegalArgumentException("Metric already registered: %s".formatted(metricName));
        }
        initializers.put(metricName, initializer);
    }

    /**
     * Register a new metric so the framework automatically tracks it
     * @param metric metric implementation to track, must extend AbstractMetric
     * @param initializer Method used to initialize the given metric
     * @param <T> Metric type
     */
    public static <T extends AbstractMetric> void register(Class<T> metric, Function<Long, T> initializer){
        register(metric.getSimpleName(), initializer);
    }


    /**
     * Merge several metrics instances.
     * All data points will be translated to the lowest reference point
     * For example, if metrics A was created at 5, and metrics B reference time is 10,
     * the returned metrics will have 5 as reference point and all data points from metrics
     * B will have their instant incremented by 5.
     * @param metrics metrics to merge
     * @return new metric instance containing the data of all the metrics provided, with time corrected as necessary.
     */
    public static MetricsStorage merge(MetricsStorage... metrics) {
        return merge(Arrays.asList(metrics));
    }

    /**
     * Merge several metrics instances.
     * For example, if metrics A was created at 5, and metrics B reference time is 10,
     * the returned metrics will have 5 as reference point and all data points from metrics B will have their instant incremented by 5.
     *
     * @param metrics metric instances
     * @return new metric instance containing the data of all the metrics provided, with time corrected as necessary.
     */
    public static MetricsStorage merge(Iterable<MetricsStorage> metrics){
        Map<String, List<TimeValue>> grouped = new HashMap<>();
        for (var storage : metrics) {
            for(var metricName: storage.metrics.keySet()){
                grouped.computeIfAbsent(metricName, k -> new ArrayList<>());
                var list = grouped.get(metricName);
                list.addAll(storage.metrics.get(metricName).values);
            }
        }
        for(var e: grouped.entrySet()){
            Collections.sort(e.getValue());
        }
        var newStorage = new MetricsStorage(MetricsStorage.NO_REF);
        for(var e: grouped.entrySet()){
            var metricName = e.getKey();
            var newMetric = initializers.get(metricName).apply(MetricsStorage.NO_REF);
            for(var tv: e.getValue()){
                newMetric.add(tv.instant(), tv.value());
            }
            newStorage.metrics.put(metricName, newMetric);
        }

        return newStorage;
    }

    public static <S extends Solution<S,I>, I extends Instance> void addCurrentObjectives(S solution){
        if(!areMetricsEnabled()){
            return;
        }
        var storage = getCurrentThreadMetrics();
        for(var obj: Context.getObjectives().values()){
            var objective = (Objective<?, S, I>) obj;
            String name = objective.getName();
            ensureMetricInitialized(name, storage);
            storage.metrics.get(name).add(objective.evalSol(solution));
        }
    }

    public static void add(String metricName, double value){
        if(areMetricsEnabled()){
            get(metricName).add(value);
        }
    }
    public static <T extends AbstractMetric> void add(Class<T> metricType, double value){
        if(areMetricsEnabled()){
            get(metricType).add(value);
        }
    }

    public static void add(String metricName, long instant, double value){
        if(areMetricsEnabled()){
            get(metricName).add(instant, value);
        }
    }
    public static <T extends AbstractMetric> void add(Class<T> metricType, long instant, double value){
        if(areMetricsEnabled()){
            get(metricType).add(instant, value);
        }
    }


}
