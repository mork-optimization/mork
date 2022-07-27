package es.urjc.etsii.grafo.io;

import es.urjc.etsii.grafo.solver.configuration.InstanceConfiguration;
import es.urjc.etsii.grafo.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.ref.SoftReference;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static es.urjc.etsii.grafo.util.IOUtil.checkExists;

/**
 * Class to manage instances during the solving lifecycle
 * @param <I> Instance class
 */
@Service
public class InstanceManager<I extends Instance> {

    private static final Logger log = LoggerFactory.getLogger(InstanceManager.class);
    private final SoftReference<I> EMPTY = new SoftReference<>(null);
    private final InstanceConfiguration instanceConfiguration;
    private final InstanceImporter<I> instanceImporter;

    private final Map<String, SoftReference<I>> cacheByPath;
    private final Map<String, List<String>> solveOrderByExperiment;


    /**
     * Build instance manager
     * @param instanceConfiguration instance configuration
     * @param instanceImporter instance importer
     */
    public InstanceManager(InstanceConfiguration instanceConfiguration, InstanceImporter<I> instanceImporter) {
        this.instanceConfiguration = instanceConfiguration;
        this.instanceImporter = instanceImporter;
        this.cacheByPath = new ConcurrentHashMap<>();
        this.solveOrderByExperiment = new ConcurrentHashMap<>();
    }


    /**
     * Get which instances have to be solved for a given experiment
     *
     * @param expName experiment name as string
     * @return Ordered list of instance identifiers, that can be later used by the getInstance method. Instances should be solved in the returned order.
     */
    public synchronized List<String> getInstanceSolveOrder(String expName){
        return this.solveOrderByExperiment.computeIfAbsent(expName, s -> {
            String instancePath = this.instanceConfiguration.getPath(expName);
            checkExists(instancePath);
            List<Path> instances = IOUtil.iterate(instancePath);

            List<String> sortedInstances;
            if(this.instanceConfiguration.isPreload()){
                sortedInstances = validateAndSort(expName, instances);
            } else {
                sortedInstances = lexicSort(instances);
            }
            return sortedInstances;
        });
    }

    protected List<String> validateAndSort(String expName, List<Path> instancePaths) {
        List<String> sortedInstances;
        log.info("Loading all instances to check correctness...");
        List<I> instances = new ArrayList<>();
        for(var p: instancePaths){
            log.debug("Loading instance: {}", p);
            I instance = loadInstance(p);
            instances.add(instance);
            cacheByPath.put(instance.getId(), new SoftReference<>(instance));
        }
        Collections.sort(instances);
        validate(instances, expName);
        sortedInstances = instances.stream().map(Instance::getPath).collect(Collectors.toList());
        if(log.isInfoEnabled()){
            var basePath = Path.of(instanceConfiguration.getPath(expName)).toAbsolutePath().normalize().toString();
            log.info("Instance validation completed, solve order: " + sortedInstances.stream().map(path -> path.replace(basePath, "")).toList());
        }
        return sortedInstances;
    }

    protected List<String> lexicSort(List<Path> instancePaths){
        List<String> sortedInstances = new ArrayList<>();
        for(var i: instancePaths){
            sortedInstances.add(i.toAbsolutePath().toString());
        }
        Collections.sort(sortedInstances);
        return sortedInstances;
    }


    protected void validate(List<I> instances, String expName) {
        if(instances.isEmpty()){
            throw new IllegalArgumentException("Could not load any instance for experiment: " + expName);
        }
        Set<String> names = new HashSet<>();
        for(var instance: instances){
            var name = instance.getId();
            if(names.contains(name)){
                throw new IllegalArgumentException("Duplicated instance name in instance folder, check that there aren't multiple instances with name: " + name);
            }
            names.add(name);
        }
    }

    /**
     * Returns an instance given a path, does not cache it.
     *
     * @param p Path of instance to load
     * @return Loaded instance
     */
    protected I getInstance(Path p){
        String absolutePath = p.toAbsolutePath().toString();
        I instance = this.cacheByPath.getOrDefault(absolutePath, EMPTY).get();
        if(instance == null){
            // Load and put in cache
            instance = loadInstance(p);
        }

        return instance;
    }

    protected I loadInstance(Path p){
        long startLoad = System.nanoTime();
        I instance = this.instanceImporter.importInstance(p.toFile());
        long endLoad = System.nanoTime();
        instance.setProperty(Instance.LOAD_TIME_NANOS, endLoad - startLoad);
        String absPath = p.toAbsolutePath().toString();
        instance.setPath(absPath);

        this.cacheByPath.put(absPath, new SoftReference<>(instance));
        return instance;
    }

    /**
     * Get instance by ID. The ID format is not guaranteed, it should be treated as an opaque constant.
     * @param instancePath instance path
     * @return Instance
     */
    public I getInstance(String instancePath){
        return getInstance(Path.of(instancePath));
    }

    /**
     * Purge instance cache
     */
    public void purgeCache(){
        this.cacheByPath.clear();
    }
}
