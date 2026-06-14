package es.urjc.etsii.grafo.io;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * Selects the instance to use for JVM warm-up.
 *
 * @param <I> Instance class
 */
class WarmupInstanceSelector<I extends Instance> {

    private static final Logger log = LoggerFactory.getLogger(WarmupInstanceSelector.class);

    private final SolverConfig solverConfig;
    private final InstanceConfiguration instanceConfiguration;
    private final InstanceCache<I> cache;

    WarmupInstanceSelector(SolverConfig solverConfig, InstanceConfiguration instanceConfiguration, InstanceCache<I> cache) {
        this.solverConfig = solverConfig;
        this.instanceConfiguration = instanceConfiguration;
        this.cache = cache;
    }

    /**
     * Select the instance path that should be used for JVM warm-up.
     *
     * @param expName       experiment name
     * @param instancePaths solve order for the experiment
     * @return warm-up instance path
     */
    String select(String expName, List<String> instancePaths) {
        var warmupConfig = this.solverConfig.getWarmup();
        if (warmupConfig.hasInstancePath()) {
            return warmupConfig.getInstancePath().trim();
        }
        if (instancePaths.isEmpty()) {
            throw new IllegalArgumentException("Cannot select a warm-up instance for an empty experiment: " + expName);
        }
        return this.instanceConfiguration.isPreload() ?
                selectFastestLoadedInstance(instancePaths) :
                selectSmallestInstanceFile(instancePaths);
    }

    private String selectFastestLoadedInstance(List<String> instancePaths) {
        var loadTimes = instancePaths.stream()
                .map(instancePath -> new InstanceLoadTime(instancePath, loadTimeNanos(instancePath)))
                .toList();
        long missingLoadTimes = loadTimes.stream().filter(loadTime -> loadTime.nanos() == null).count();
        if (missingLoadTimes > 0) {
            log.warn(
                    "Cannot use validation load times for warm-up selection because {} of {} instances are not cached. Falling back to smallest instance file.",
                    missingLoadTimes,
                    instancePaths.size()
            );
            return selectSmallestInstanceFile(instancePaths);
        }

        return loadTimes.stream()
                .min(Comparator.comparingLong(InstanceLoadTime::nanos).thenComparing(InstanceLoadTime::path))
                .map(InstanceLoadTime::path)
                .orElseThrow();
    }

    private Long loadTimeNanos(String instancePath) {
        var instance = this.cache.get(instancePath);
        if (instance == null) {
            return null;
        }
        var loadTime = instance.getPropertyOrDefault(Instance.LOAD_TIME_NANOS, Long.MAX_VALUE);
        return loadTime instanceof Number number ? number.longValue() : null;
    }

    private String selectSmallestInstanceFile(List<String> instancePaths) {
        return instancePaths.stream()
                .min(Comparator.comparingLong(this::fileSize).thenComparing(Comparator.naturalOrder()))
                .orElseThrow();
    }

    private long fileSize(String instancePath) {
        var path = containerPath(instancePath);
        try {
            return Files.size(Path.of(path));
        } catch (IOException | RuntimeException e) {
            log.warn("Could not determine instance size for warm-up auto-selection, using lowest priority: {}", instancePath);
            return Long.MAX_VALUE;
        }
    }

    private static String containerPath(String instancePath) {
        return IOUtil.containerPath(instancePath);
    }

    private record InstanceLoadTime(String path, Long nanos) {}
}
