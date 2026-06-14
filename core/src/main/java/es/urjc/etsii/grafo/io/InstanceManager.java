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
import java.lang.ref.SoftReference;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    protected final SoftReference<I> EMPTY = new SoftReference<>(null);
    protected final InstanceConfiguration instanceConfiguration;
    protected final SolverConfig solverConfig;
    protected final InstanceImporter<I> instanceImporter;

    protected final Map<String, SoftReference<I>> cacheByPath;
    protected final Map<String, List<String>> solveOrderByExperiment;


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
        this.solverConfig = solverConfig;
        this.instanceImporter = instanceImporter;
        this.cacheByPath = new ConcurrentHashMap<>();
        this.solveOrderByExperiment = new ConcurrentHashMap<>();
    }


    public synchronized List<String> getInstanceSolveOrder(String expName) {
        return getInstanceSolveOrder(expName, this.instanceConfiguration.isPreload());
    }

    /**
     * Get which instances have to be solved for a given experiment
     *
     * @param expName experiment name as string
     * @param preload if true load instances to use comparator to sort them, if false uses lexicograph sort by path name
     * @return Ordered list of instance identifiers, that can be later used by the getInstance method. Instances should be solved in the returned order.
     */
    public synchronized List<String> getInstanceSolveOrder(String expName, boolean preload) {
        return this.solveOrderByExperiment.computeIfAbsent(expName, s -> {
            String instancePath = this.instanceConfiguration.getPath(expName);
            checkExists(instancePath);
            List<String> instances = isIndexFile(instancePath)?
                    listIndexFile(instancePath):
                    listNormalFile(instancePath);

            List<String> sortedInstances = preload?
                    validateAndSort(expName, instances):
                    lexicSort(instances);
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
        var parentPath = indexFile.getParent();
        try (var in = BOMInputStream.builder().setInputStream(Files.newInputStream(indexFile)).get();
             var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return reader.lines()
                    .map(line -> line.startsWith("\uFEFF") ? line.substring(1) : line)
                    .filter(p -> !p.startsWith("#"))
                    .filter(p -> !p.isBlank())
                    .map(Path::of)
                    .map(parentPath::resolve)
                    .map(Path::toAbsolutePath)
                    .map(Path::toString)
                    .map(IOUtil::checkExists)
                    .toList();
        } catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    private boolean isIndexFile(String instancePath) {
        var file = new File(instancePath);
        log.debug("Not an index file: {}", instancePath);
        return file.isFile() && instancePath.endsWith(INDEX_SUFFIX);
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
            cacheByPath.put(instance.getId(), new SoftReference<>(instance));
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
     * @param instancePaths solve order for the experiment
     * @return warm-up instance path
     */
    public String getWarmupInstancePath(String expName, List<String> instancePaths) {
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
        var instance = getInstance(instancePath);
        var loadTime = instance.getPropertyOrDefault(Instance.LOAD_TIME_NANOS, Long.MAX_VALUE);
        if (loadTime instanceof Number number) {
            return number.longValue();
        }
        return Long.MAX_VALUE;
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
     * Returns an instance given a path
     *
     * @param path Path of instance to load
     * @return Loaded instance
     */
    public synchronized I getInstance(String path) {
        I instance = this.cacheByPath.getOrDefault(path, EMPTY).get();
        if (instance == null) {
            // Load and put in cache
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

        this.cacheByPath.put(path, new SoftReference<>(instance));
        return instance;
    }

    /**
     * Purge instance cache
     */
    public void purgeCache() {
        this.cacheByPath.clear();
    }

    public InstanceImporter<I> getUserImporterImplementation() {
        return this.instanceImporter;
    }
}
