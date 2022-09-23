package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.autoconfig.service.factories.AlgorithmComponentFactory;
import es.urjc.etsii.grafo.autoconfig.service.filter.InventoryFilterStrategy;
import es.urjc.etsii.grafo.util.ClassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableMap;

/**
 * Manage algorithm components at runtime. When starting the application, discovers all available components.
 * Can build any configured component easily from string,
 * as long as there is a matching constructor in the algorithm component.
 */
@Service
@Profile("autoconfig")
public class AlgorithmInventoryService {
    private static final Logger log = LoggerFactory.getLogger(AlgorithmInventoryService.class);
    private final InventoryFilterStrategy filterStrategy;

    protected Map<Class<?>, Collection<Class<?>>> componentsByType = new HashMap<>();
    protected Map<String, Class<?>> componentByName = new HashMap<>();
    protected Map<String, String> aliases = getDefaultAliases();
    protected Map<String, AlgorithmComponentFactory> factories = new HashMap<>();

    @Value("${advanced.scan-pkgs:es.urjc.etsii}")
    protected String pkgs;

    public AlgorithmInventoryService(InventoryFilterStrategy filterStrategy, List<AlgorithmComponentFactory> factoryList) {
        this.filterStrategy = filterStrategy;
        for(var f: factoryList){
            this.registerFactory(f);
        }
    }

    private static Map<String, String> getDefaultAliases() {
        var map = new HashMap<String, String>();
        map.put("GRASP", "GreedyRandomGRASPConstructive");
        map.put("GRASPConstructive", "GreedyRandomGRASPConstructive");
        map.put("GraspConstructive", "GreedyRandomGRASPConstructive");
        map.put("NullGRASPListManager", "NullGraspListManager");
        return map;
    }

    /**
     * Create an alias for target with name alias. Example registerAlias("ILS", "IteratedLocalSearch")
     * @param alias New name, reference target
     * @param target target name
     */
    public void registerAlias(String alias, String target){
        if(this.aliases.containsKey(target)){
            throw new IllegalArgumentException(String.format("%s is already an alias, call registerAlias(%s, %s) instead of registerAlias(%s, %s)", target, alias, this.aliases.get(target), alias, target));
        }
        if(!this.factories.containsKey(target) && !this.componentByName.containsKey(target)){
            throw new IllegalArgumentException(String.format("Cannot register alias for unknown target: %s --> %s", alias, target));
        }
        throwIfKnown(alias, true, true);
        this.aliases.put(alias, target);
    }

    private void throwIfKnown(String name, boolean checkComponents, boolean checkFactories){
        if(this.aliases.containsKey(name)){
            throw new IllegalArgumentException(String.format("Alias already exists: %s --> %s", name, this.aliases.get(name)));
        }
        if(checkFactories && this.factories.containsKey(name)){
            throw new IllegalArgumentException(String.format("Factory already exists with name: %s", name));
        }
        if(checkComponents && this.componentByName.containsKey(name)){
            throw new IllegalArgumentException(String.format("Component already exists with name: %s", name));
        }
    }

    /**
     * Provide a function that given a parameter map can create components with id 'name'
     * @param factory function to execute such as f(params) --> returns component of type name
     */
    public void registerFactory(AlgorithmComponentFactory factory){
        if(!this.filterStrategy.include(factory.produces())){
            log.debug("Ignoring factory {} because it is excluded by the filter {}", factory.getClass().getSimpleName(), this.filterStrategy.getClass().getSimpleName());
            return;
        }
        log.debug("Adding factory {} produces {}", factory.getClass().getSimpleName(), factory.produces().getSimpleName());
        var clazz = factory.produces();
        String name = clazz.getSimpleName();
        throwIfKnown(name, false, true);
        this.factories.put(name, factory);
    }

    @PostConstruct
    protected void runComponentDiscovery(){
        runComponentDiscovery(this.pkgs);
    }

    protected boolean checkComponentName(Set<String> invalidNames, String name){
        boolean isValid = name.matches("[a-zA-Z][a-zA-Z0-9]*");
        if(!isValid && invalidNames != null){
            invalidNames.add(name);
        }
        return isValid;
    }

    protected void runComponentDiscovery(String pkgs){
        var types = new ArrayList<Class<?>>();
        var failedValidationSet = new HashSet<String>();
        for(var pkg: pkgs.split(",")){
            types.addAll(ClassUtil.findTypesByAnnotation(pkg, AlgorithmComponent.class));
        }
        for(var type: types){
            if(!isAccesible(type)){
                log.debug("Ignoring component {} because it is not public", type);
                continue;
            }
            String componentName = type.getSimpleName();
            if(!this.filterStrategy.include(type)){
                log.debug("Ignoring component {} because it is excluded by filter {}", componentName, this.filterStrategy.getClass().getSimpleName());
                continue;
            }
            checkComponentName(failedValidationSet, componentName);
            throwIfKnown(componentName, true, false);
            componentByName.put(componentName, type);
            classify(componentsByType, type);
        }
        if(!failedValidationSet.isEmpty()){
            throw new IllegalArgumentException("Invalid component names detected: " + failedValidationSet);
        }
        log.info("Algorithm components found: {}", componentByName.keySet());
        log.debug("Classified algorithm components: {}", componentsByType);
    }

    private static boolean isAccesible(Class<?> clazz){
        return Modifier.isPublic(clazz.getModifiers());
    }

    private static void classify(Map<Class<?>, Collection<Class<?>>> componentsByType, Class<?> initialType) {
        for (Class<?> type = initialType; type != null; type = type.getSuperclass()){
            if(!ClassUtil.isObjectClass(type)){
                componentsByType.computeIfAbsent(type, a -> new HashSet<>()).add(initialType);
            }
        }
    }

    /**
     * Get current algorithm inventory, ie the sum of all algorithm components, either autodetected or manually registered
     * @return unmodifiable algorithm inventory. Do not attempt to modify its data structures
     */
    public AlgorithmInventory getInventory(){
        return new AlgorithmInventory(unmodifiableMap(componentsByType), unmodifiableMap(componentByName), unmodifiableMap(aliases), unmodifiableMap(factories));
    }

    public record AlgorithmInventory (
            Map<Class<?>, Collection<Class<?>>> componentsByType,
            Map<String, Class<?>> componentByName,
            Map<String, String> aliases,
            Map<String, AlgorithmComponentFactory> factories
    ){
        /**
         * Get all detected algorithm components
         * @return New set containing a reference to each algorithm component detected
         */
        public Set<Class<?>> allComponents(){
            return componentsByType.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        }
    }

    public AlgorithmComponentFactory getFactoryFor(Class<?> clazz){
        return this.factories.get(clazz.getSimpleName());
    }
}
