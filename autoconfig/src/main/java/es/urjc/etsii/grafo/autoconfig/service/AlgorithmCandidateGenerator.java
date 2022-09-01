package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;

public class AlgorithmCandidateGenerator {
    private static final Set<Class<?>> collectedClasses = Set.of(List.class, ArrayList.class, Set.class, HashSet.class, Collection.class);

    private final AlgorithmInventoryService inventoryService;
    private final Logger log = LoggerFactory.getLogger(AlgorithmCandidateGenerator.class);
    private final Map<Class<?>, List<ComponentParameter>> paramInfo;

    public AlgorithmCandidateGenerator(AlgorithmInventoryService inventoryService) {
        this.inventoryService = inventoryService;
        this.paramInfo = analyzeParametersRecursively();
    }

    /**
     * Analyze algorithm components recursively, starting from all algorithm classes, and extract parameter information
     * @return parameter info for each component
     */
    public Map<Class<?>, List<ComponentParameter>> analyzeParametersRecursively(){
        var inventory = this.inventoryService.getInventory();
        var algClasses = inventory.componentsByType().get(Algorithm.class);
        var byType = inventory.componentsByType();

        Queue<Class<?>> queue = new ArrayDeque<>(algClasses);
        var notVisited = inventory.allComponents();
        notVisited.removeAll(algClasses);

        // For each analyzed alg component, its list of parameters/dependencies
        var result = new HashMap<Class<?>, List<ComponentParameter>>();

        // Start exploring the algorithms using a BFS approach, continue by their dependencies
        while(!queue.isEmpty()){
            var currentComponentClass = queue.remove();
            var constructor = findAutoconfigConstructor(currentComponentClass);
            if(constructor == null){
                log.debug("Skipping component {}, could not find constructor annotated with @AutoconfigConstructor", currentComponentClass.getSimpleName());
                continue;
            }
            var componentParameters = new ArrayList<ComponentParameter>();
            for(Parameter p: constructor.getParameters()){
                var cp = toComponentParameter(byType, p);
                if(cp == null){
                    throw new IllegalArgumentException(String.format("Unknown parameter in constructor %s --> (%s %s). Fix: missing annotation, or no valid algorithm component found that implements the given type", constructor, p.getType(), p.getName()));
                }
                componentParameters.add(cp);
                if(cp.recursive()){
                    // Parameter has a known algorithm component type, for example Improver<S,I>
                    // Add all implementations to the exploration queue
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
        if(!notVisited.isEmpty()){
            log.debug("Ignored components because they are not reachable from any algorithms: {}", notVisited);
        }
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
        if(p.isAnnotationPresent(ProvidedParam.class)){
            return ComponentParameter.from(name, p.getAnnotation(ProvidedParam.class));
        }

//        if (collectedClasses.contains(type)) {
//            return new ComponentParameter(name, ParameterType.LIST, false, new Object[]{});
//        }

        // Last option, not annotated but type is known
        if (types.containsKey(type)) {
            return ComponentParameter.from(name, types.get(type));
        }
        return null;
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
