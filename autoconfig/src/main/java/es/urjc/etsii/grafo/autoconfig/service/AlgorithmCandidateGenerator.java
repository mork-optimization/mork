package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

public class AlgorithmCandidateGenerator {
    private final AlgorithmInventoryService inventoryService;
    private final Logger log = LoggerFactory.getLogger(AlgorithmCandidateGenerator.class);
    private final Map<Class<?>, List<ComponentParameter>> paramInfo;

    public AlgorithmCandidateGenerator(AlgorithmInventoryService inventoryService) {
        this.inventoryService = inventoryService;
        this.paramInfo = analyzeAllComponentsParameters();
    }

    /**
     * Analyze algorithm components recursively, starting from all algorithm classes, and extract parameter information
     * @return parameter info for each component
     */
    public Map<Class<?>, List<ComponentParameter>> analyzeAllComponentsParameters(){
        var inventory = this.inventoryService.getInventory();
        var algClasses = inventory.componentsByType().get(Algorithm.class);
        var byType = inventory.componentsByType();

        Queue<Class<?>> queue = new ArrayDeque<>(algClasses);
        var notVisited = inventory.componentsByType().values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
        notVisited.removeAll(algClasses);
        var result = new HashMap<Class<?>, List<ComponentParameter>>();
        while(!queue.isEmpty()){
            var currentComponentClass = queue.remove();
            var constructor = findAutoconfigConstructor(currentComponentClass);
            if(constructor == null){
                log.debug("Skipping component {}, could not find constructor annotated with @AutoconfigConstructor", currentComponentClass.getSimpleName());
                continue;
            }
            var componentParameters = new ArrayList<ComponentParameter>();
            for(var p: constructor.getParameters()){
                var cp = toComponentParameter(byType, p);
                componentParameters.add(cp);
                if(cp.isRecursive()){
                    for(var candidate: byType.get(p.getType())){
                        if(notVisited.contains(candidate)){
                            notVisited.remove(candidate);
                            queue.add(candidate);
                        }
                    }
                }
            }
            result.put(currentComponentClass, componentParameters);
        }
        log.debug("Ignored components because they are not reachable: {}", notVisited);
        return result;
    }

    protected ComponentParameter toComponentParameter(Map<Class<?>, Collection<Class<?>>> types, Parameter p){
        // Either the parameter is annotated or it is a known type that we have to recursively analyze
        var type = p.getType();
        var name = p.getName();
        if(p.isAnnotationPresent(IntegerParam.class)){
            return ComponentParameter.from(name, p.getAnnotation(IntegerParam.class));
        }
        if(p.isAnnotationPresent(RealParam.class)){
            return ComponentParameter.from(name, p.getAnnotation(RealParam.class));
        }
        if(p.isAnnotationPresent(CategoricalParam.class)){
            return ComponentParameter.from(name, p.getAnnotation(CategoricalParam.class));
        }
        if(p.isAnnotationPresent(OrdinalParam.class)){
            return ComponentParameter.from(name, p.getAnnotation(OrdinalParam.class));
        }

        if(Boolean.TYPE.equals(type)){
            return new ComponentParameter(name, ParameterType.CATEGORICAL, false, new Object[]{true, false});
        }

        // Last option, not annotated but type is known
        if (types.containsKey(type)) {
            return ComponentParameter.from(name, types.get(type));
        }
        throw new IllegalArgumentException(String.format("Failed to map parameter %s, with type %s", name, type));
    }

    /**
     * Analyze an algorithm component class to find which constructor is annotated with @AutoconfigConstructor
     * @param algComponentClass Algorithm component to analyze
     * @return constructor annotated with @AutoconfigConstructor if present, null otherwise
     */
    protected Constructor<?> findAutoconfigConstructor(Class<?> algComponentClass){
        var constructors = algComponentClass.getConstructors();
        for(var c: constructors){
            var annotation = c.getAnnotation(AutoconfigConstructor.class);
            if(annotation != null){
                return c;
            }
        }
        return null;
    }
}
