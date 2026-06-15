package es.urjc.etsii.grafo.io;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.executors.Executor;
import es.urjc.etsii.grafo.util.Compression;
import es.urjc.etsii.grafo.util.IOUtil;
import es.urjc.etsii.grafo.util.StringUtil;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
    protected final WarmupInstanceSelector<I> warmupSelector;
    protected final Map<String, List<String>> solveOrderByExperiment;
    protected final Map<String, Map<String, String>> authorizedLoadPathsByExperiment;


    /**
     * Build instance manager
     *
     * @param instanceConfiguration instance configuration
     * @param solverConfig          solver configuration
     * @param instanceImporter      instance importer
     */
    @Autowired
    public InstanceManager(InstanceConfiguration instanceConfiguration, SolverConfig solverConfig, InstanceImporter<I> instanceImporter) {
        this.instanceConfiguration = instanceConfiguration;
        this.instanceImporter = instanceImporter;
        this.cache = new InstanceCache<>();
        this.warmupSelector = new WarmupInstanceSelector<>(solverConfig, instanceConfiguration, this.cache);
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
            List<String> instancePaths = isIndexFile(configuredPath)?
                    listIndexFile(configuredPath):
                    listNormalFile(configuredPath);

            List<String> sortedInstances = preload?
                    validateAndSort(expName, instancePaths):
                    lexicSort(instancePaths);
            return sortedInstances;
        });
    }

    private List<String> listNormalFile(String instancePath) {
        List<String> files = IOUtil.iterate(instancePath);
        for (var iterator = files.iterator(); iterator.hasNext(); ) {
            var f = iterator.next();
            if (f.endsWith(INDEX_SUFFIX)) {
                log.info("Ignoring index file: {}", f);
                iterator.remove();
            }
        }
        return files;
    }

    private List<String> listIndexFile(String instancePath) {
        Path indexFile = Path.of(instancePath);
        var parentPath = Optional.ofNullable(indexFile.getParent()).orElse(Path.of("."));
        try (var in = BOMInputStream.builder().setInputStream(Files.newInputStream(indexFile)).get();
             var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(line -> line.startsWith("\uFEFF") ? line.substring(1) : line)
                    .map(String::trim)
                    .filter(p -> !p.startsWith("#"))
                    .filter(p -> !p.isBlank())
                    .map(path -> resolveIndexEntry(parentPath, path))
                    .map(IOUtil::checkLoadPathExists)
                    .toList();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private static String resolveIndexEntry(Path parentPath, String instancePath) {
        int index = instancePath.indexOf(Compression.SEP);
        if (index < 0) {
            return parentPath.resolve(Path.of(instancePath)).toAbsolutePath().toString();
        }
        String container = instancePath.substring(0, index);
        String entry = instancePath.substring(index);
        return parentPath.resolve(Path.of(container)).toAbsolutePath() + entry;
    }

    private boolean isIndexFile(String instancePath) {
        var file = new File(instancePath);
        boolean isIndex = file.isFile() && instancePath.endsWith(INDEX_SUFFIX);
        if (!isIndex) {
            log.debug("Not an index file: {}", instancePath);
        }
        return isIndex;
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
     * Select the instance path that should be used for JVM warm-up.
     *
     * @param expName       experiment name
     * @param instancePaths solve order load paths for the experiment
     * @return warm-up instance load paths
     */
    public String getWarmupInstancePath(String expName, List<String> instancePaths) {
        return this.warmupSelector.select(expName, instancePaths);
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
            var relativeToConfiguredPath = resolveIndexEntry(configuredPathBase(expName), requestedPath);
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
        List<String> instancePaths = isIndexFile(configuredPath)?
                listIndexFile(configuredPath):
                listNormalFile(configuredPath);

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
        return instance;
    }

    /**
     * Purge instance cache
     */
    public synchronized void purgeCache() {
        this.cache.clear();
    }

    public InstanceImporter<I> getUserImporterImplementation() {
        return this.instanceImporter;
    }
}
