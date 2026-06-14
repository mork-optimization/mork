package es.urjc.etsii.grafo.io;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.util.Compression;
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
        return instancePaths.stream()
                .min(Comparator.comparingLong(this::loadTimeNanos).thenComparing(Comparator.naturalOrder()))
                .orElseThrow();
    }

    private long loadTimeNanos(String instancePath) {
        var instance = this.cache.get(instancePath);
        if (instance == null) {
            return Long.MAX_VALUE;
        }
        var loadTime = instance.getPropertyOrDefault(Instance.LOAD_TIME_NANOS, Long.MAX_VALUE);
        return loadTime instanceof Number number ? number.longValue() : Long.MAX_VALUE;
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
        int index = instancePath.indexOf(Compression.SEP);
        return index < 0 ? instancePath : instancePath.substring(0, index);
    }
}
