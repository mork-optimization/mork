package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceImporter;
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
    private final Map<String, String> pathByName;


    /**
     * Build instance manager
     * @param instanceConfiguration instance configuration
     * @param instanceImporter instance importer
     */
    public InstanceManager(InstanceConfiguration instanceConfiguration, InstanceImporter<I> instanceImporter) {
        this.instanceConfiguration = instanceConfiguration;
        this.instanceImporter = instanceImporter;
        this.pathByName = new ConcurrentHashMap<>();
        this.cacheByPath = new ConcurrentHashMap<>();
    }


    /**
     * Get which instances have to be solved for a given experiment
     *
     * @param expName experiment name as string
     * @return Ordered list of instance identifiers, that can be later used by the getInstance method. Instances should be solved in the returned order.
     */
    public List<String> getInstanceSolveOrder(String expName){
        String instancePath = this.instanceConfiguration.getPath(expName);
        log.info("Loading all instances to check correctness...");
        checkExists(instancePath);
        List<Path> instancePaths = IOUtil.iterate(instancePath);
        List<I> instances = new ArrayList<>();
        for(var p: instancePaths){
            log.debug("Loading instance: {}", p);
            I instance = loadInstance(p);
            instances.add(instance);
        }
        Collections.sort(instances);
        validate(instances, expName);
        // Return only the instance names
        List<String> instanceNames = instances.stream().map(Instance::getName).collect(Collectors.toList());
        log.info("Instance validation completed, solve order: " + instanceNames);
        return instanceNames;
    }

    private void validate(List<I> instances, String expName) {
        if(instances.isEmpty()){
            throw new IllegalArgumentException("Could not load any instance for experiment: " + expName);
        }
        Set<String> names = new HashSet<>();
        for(var instance: instances){
            var name = instance.getName();
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
    public I getInstance(Path p){
        String absolutePath = p.toAbsolutePath().toString();
        I instance = this.cacheByPath.getOrDefault(absolutePath, EMPTY).get();
        if(instance == null){
            // Load and put in cache
            instance = loadInstance(p);
        }

        return instance;
    }

    protected I loadInstance(Path p){
        I instance = this.instanceImporter.importInstance(p.toFile());
        String absPath = p.toAbsolutePath().toString();
        String name = instance.getName();
        this.pathByName.put(name, absPath);
        this.cacheByPath.put(absPath, new SoftReference<>(instance));
        return instance;
    }

    /**
     * Get instance by name
     * @param name instance name
     * @return Instance
     */
    public I getInstance(String name){
        if(!pathByName.containsKey(name)){
            throw new IllegalArgumentException("Unknown instance name: " + name);
        }
        return getInstance(Path.of(pathByName.get(name)));
    }

    /**
     * Purge instance cache
     */
    public void purge(){
        this.cacheByPath.clear();
    }
}
