package es.urjc.etsii.grafo.io;

import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.executors.Executor;
import es.urjc.etsii.grafo.util.IOUtil;
import es.urjc.etsii.grafo.util.StringUtil;
import me.tongfei.progressbar.ProgressBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.ref.SoftReference;
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
    protected final SoftReference<I> EMPTY = new SoftReference<>(null);
    protected final InstanceConfiguration instanceConfiguration;
    protected final InstanceImporter<I> instanceImporter;

    protected final Map<String, SoftReference<I>> cacheByPath;
    protected final Map<String, List<String>> solveOrderByExperiment;


    /**
     * Build instance manager
     *
     * @param instanceConfiguration instance configuration
     * @param instanceImporter      instance importer
     */
    public InstanceManager(InstanceConfiguration instanceConfiguration, InstanceImporter<I> instanceImporter) {
        this.instanceConfiguration = instanceConfiguration;
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
            List<String> instances = IOUtil.iterate(instancePath);

            List<String> sortedInstances;
            if (preload) {
                sortedInstances = validateAndSort(expName, instances);
            } else {
                sortedInstances = lexicSort(instances);
            }
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
            cacheByPath.put(instance.getId(), new SoftReference<>(instance));
        }
        Collections.sort(instances);
        validate(instances, expName);
        sortedInstances = instances.stream().map(Instance::getPath).collect(Collectors.toList());
        logInstances(sortedInstances);
        return sortedInstances;
    }

    private static void logInstances(List<String> sortedInstances) {
        if (!log.isInfoEnabled()) return;

        var sortedInstancesArray = sortedInstances.toArray(new String[0]);
        var prefix = StringUtil.longestCommonPrefix(sortedInstancesArray);
        var suffix = StringUtil.longestCommonSuffix(sortedInstancesArray);
        int bothLength = prefix.length() + suffix.length();
        List<String> list = new ArrayList<>();
        for (String path : sortedInstances) {
            if(bothLength > path.length()){
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
        instance.setPath(path);

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
