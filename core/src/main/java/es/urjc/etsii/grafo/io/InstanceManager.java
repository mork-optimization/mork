package es.urjc.etsii.grafo.io;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.executors.Executor;
import es.urjc.etsii.grafo.util.Compression;
import es.urjc.etsii.grafo.util.IOUtil;
import es.urjc.etsii.grafo.util.InstancePathUtil;
import es.urjc.etsii.grafo.util.InstanceSelectionUtil;
import es.urjc.etsii.grafo.util.StringUtil;
import me.tongfei.progressbar.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static es.urjc.etsii.grafo.util.IOUtil.checkExists;

/**
 * Class to manage instances during the solving lifecycle
 *
 * @param <I> Instance class
 */
@Service
public class InstanceManager<I extends Instance> {

    private static final Logger log = LoggerFactory.getLogger(InstanceManager.class);
    private static final int MAX_LENGTH = 300;
    public static final String INDEX_SUFFIX = ".index";

    protected final InstanceConfiguration instanceConfiguration;
    protected final InstanceImporter<I> instanceImporter;

    protected final InstanceCache<I> cache;
    private final Map<String, Long> loadTimesNanos = new HashMap<>();
    protected final Map<String, List<String>> solveOrderByExperiment;
    protected final Map<String, Map<String, String>> authorizedLoadPathsByExperiment;


    /**
     * Build instance manager
     *
     * @param instanceConfiguration instance configuration
     * @param instanceImporter      instance importer
     */
    @Autowired
    public InstanceManager(InstanceConfiguration instanceConfiguration, InstanceImporter<I> instanceImporter) {
        this.instanceConfiguration = instanceConfiguration;
        this.instanceImporter = instanceImporter;
        this.cache = new InstanceCache<>();
        this.solveOrderByExperiment = new HashMap<>();
        this.authorizedLoadPathsByExperiment = new HashMap<>();
    }


    public synchronized List<String> getInstanceSolveOrder(String expName) {
        return getInstanceSolveOrder(expName, this.instanceConfiguration.isPreload());
    }

    /**
     * Get which instances have to be solved for a given experiment
     *
     * @param expName experiment name as string
     * @param preload if true load instances to use comparator to sort them, if false uses lexicograph sort by path name
     * @return Ordered list of instance load paths, that can be later used by the getInstance method. Instances should be solved in the returned order.
     */
    public synchronized List<String> getInstanceSolveOrder(String expName, boolean preload) {
        return this.solveOrderByExperiment.computeIfAbsent(expName, s -> {
            String configuredPath = this.instanceConfiguration.getPath(expName);
            checkExists(configuredPath);
            List<String> instancePaths = InstancePathUtil.list(configuredPath);

            List<String> sortedInstances = preload?
                    validateAndSort(expName, instancePaths):
                    lexicSort(instancePaths);
            return sortedInstances;
        });
    }

    protected List<String> validateAndSort(String expName, List<String> instancePaths) {
        List<String> sortedInstances;
        log.info("Loading all instances to check correctness...");
        List<I> instances = new ArrayList<>();
        var iterator = ProgressBar.wrap(instancePaths, Executor.getPBarBuilder("Instance validation"));
        for (var path : iterator) {
            log.debug("Loading instance: {}", path);
            I instance = loadInstance(path);
            instances.add(instance);
        }
        Collections.sort(instances);
        validate(instances, expName);
        sortedInstances = instances.stream().map(Instance::getPath).collect(Collectors.toList());
        logInstances(sortedInstances);
        return sortedInstances;
    }

    /**
     * Select the shared reference instance for JVM warm-up and AutoLLM probes.
     * An explicit reference must belong to the experiment's configured inputs.
     * Otherwise, import candidates with unknown load times and choose the fastest to load.
     *
     * @param expName       experiment name
     * @return reference instance load path
     */
    public synchronized String getReferenceInstancePath(String expName) {
        // Unlike experiment paths, an absent reference must not fall back to "default".
        String reference = this.instanceConfiguration.getPaths().get("reference");
        if (reference != null && !reference.isBlank()) {
            return requireConfiguredInstancePath(expName, reference.trim());
        }
        String selected = InstanceSelectionUtil.selectFastestLoadingInstance(
                getInstanceSolveOrder(expName, false), this::loadTimeNanos);
        log.info("Selected reference instance for {}: {}", expName, selected);
        return selected;
    }

    private long loadTimeNanos(String path) {
        String key = InstanceCache.canonicalize(path);
        I instance = this.cache.get(path);
        if (instance == null) {
            Long recorded = this.loadTimesNanos.get(key);
            if (recorded != null) return recorded;
            instance = getInstance(path);
        }
        long nanos = ((Number) instance.getProperty(Instance.LOAD_TIME_NANOS)).longValue();
        this.loadTimesNanos.put(key, nanos);
        return nanos;
    }

    private static void logInstances(List<String> sortedInstances) {
        if (!log.isInfoEnabled()) return;

        var sortedInstancesArray = sortedInstances.toArray(new String[0]);
        var prefix = StringUtil.longestCommonPrefix(sortedInstancesArray);
        var suffix = StringUtil.longestCommonSuffix(sortedInstancesArray);
        int bothLength = prefix.length() + suffix.length();
        List<String> list = new ArrayList<>();
        for (String path : sortedInstances) {
            if(bothLength >= path.length()){
                list.add(path);
            } else {
                String substring = path.substring(prefix.length(), path.length() - suffix.length());
                list.add(substring);
            }
        }
        var orderedList = list.toString();
        if (orderedList.length() > MAX_LENGTH) {
            orderedList = orderedList.substring(0, MAX_LENGTH - 4) + "...]";
        }
        log.info("Instance validation completed, solve order: {}", orderedList);
    }


    protected List<String> lexicSort(List<String> instancePaths) {
        List<String> sortedInstances = new ArrayList<>(instancePaths);
        Collections.sort(sortedInstances);
        return sortedInstances;
    }

    /**
     * Verify that a user supplied load path belongs to the configured instance source for an experiment.
     * This is intended for external callbacks, where the path is supplied by a client instead of being
     * produced by {@link #getInstanceSolveOrder(String)}.
     *
     * @param expName       experiment name
     * @param requestedPath user supplied instance load path
     * @return the canonical configured load path that should be used for loading
     */
    public synchronized String requireConfiguredInstancePath(String expName, String requestedPath) {
        Objects.requireNonNull(requestedPath, "Instance path cannot be null");
        if (requestedPath.isBlank()) {
            throw new IllegalArgumentException("Instance path cannot be blank");
        }

        var authorizedLoadPaths = this.authorizedLoadPathsByExperiment.computeIfAbsent(expName, this::buildAuthorizedLoadPaths);
        var canonicalRequestedPath = canonicalizeForAuthorization(requestedPath);
        var authorizedPath = authorizedLoadPaths.get(canonicalRequestedPath);
        if (authorizedPath == null) {
            var relativeToConfiguredPath = InstancePathUtil.resolve(configuredPathBase(expName), requestedPath);
            authorizedPath = authorizedLoadPaths.get(canonicalizeForAuthorization(relativeToConfiguredPath));
        }
        if (authorizedPath == null) {
            throw new IllegalArgumentException("Requested instance path is not configured for experiment %s: %s".formatted(expName, requestedPath));
        }
        return authorizedPath;
    }

    private Map<String, String> buildAuthorizedLoadPaths(String expName) {
        String configuredPath = this.instanceConfiguration.getPath(expName);
        checkExists(configuredPath);
        List<String> instancePaths = InstancePathUtil.list(configuredPath);

        Map<String, String> authorizedLoadPaths = new HashMap<>();
        for (String path : instancePaths) {
            authorizedLoadPaths.put(canonicalizeForAuthorization(path), path);
        }
        return authorizedLoadPaths;
    }

    private Path configuredPathBase(String expName) {
        Path configuredPath = Path.of(this.instanceConfiguration.getPath(expName));
        if (Files.isDirectory(configuredPath)) {
            return configuredPath;
        }
        return Optional.ofNullable(configuredPath.getParent()).orElse(Path.of("."));
    }

    private static String canonicalizeForAuthorization(String loadPath) {
        int index = loadPath.indexOf(Compression.SEP);
        if (index < 0) {
            return Path.of(loadPath).toAbsolutePath().normalize().toString();
        }
        String container = loadPath.substring(0, index);
        String entry = loadPath.substring(index);
        return Path.of(container).toAbsolutePath().normalize() + entry;
    }


    protected void validate(List<I> instances, String expName) {
        if (instances.isEmpty()) {
            throw new IllegalArgumentException("Could not load any instance for experiment: " + expName);
        }
        Set<String> names = new HashSet<>();
        for (var instance : instances) {
            var name = instance.getId();
            if (names.contains(name)) {
                throw new IllegalArgumentException("Duplicated instance name in instance folder, check that there aren't multiple instances with name: " + name);
            }
            names.add(name);
        }
    }

    /**
     * Returns an instance given a load path.
     *
     * @param path path or compressed load path of the instance to load
     * @return Loaded instance
     */
    public synchronized I getInstance(String path) {
        I instance = this.cache.get(path);
        if (instance == null) {
            instance = loadInstance(path);
        }
        return instance;
    }

    protected synchronized I loadInstance(String path) {
        long startLoad = System.nanoTime();
        I instance = this.instanceImporter.importInstance(path);
        long endLoad = System.nanoTime();
        instance.setProperty(Instance.LOAD_TIME_NANOS, endLoad - startLoad);
        for(var e: instance.customProperties().entrySet()){
            instance.setProperty(e.getKey(), e.getValue());
        }

        instance.setPath(IOUtil.relativizePath(path));

        this.cache.put(path, instance);
        this.loadTimesNanos.put(InstanceCache.canonicalize(path), endLoad - startLoad);
        return instance;
    }

    /**
     * Purge cached instances, retaining their measured loading times.
     */
    public synchronized void purgeCache() {
        this.cache.clear();
    }

    public InstanceImporter<I> getUserImporterImplementation() {
        return this.instanceImporter;
    }
}
