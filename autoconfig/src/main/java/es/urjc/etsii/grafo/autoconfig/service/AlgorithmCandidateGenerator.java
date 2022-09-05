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

import static es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter.*;

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
    protected Map<Class<?>, List<ComponentParameter>> analyzeParametersRecursively(){
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
            var factory = this.inventoryService.getFactoryFor(currentComponentClass);
            if(factory != null){
                // First strategy: using a factory
                var params = factory.getRequiredParameters();
                for(var cp: params){
                    if(cp.recursive()){
                        // Parameter has a known algorithm component type, for example Improver<S,I>
                        // Add all implementations to the exploration queue
                        for(var candidate: byType.get(cp.getJavaType())){
                            if(notVisited.contains(candidate)){
                                notVisited.remove(candidate);
                                queue.add(candidate);
                            }
                        }
                    }
                }
                result.put(currentComponentClass, params);

            } else {

                // Second strategy: use autoconfig constructor
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
            return ComponentParameter.from(name, type, p.getAnnotation(IntegerParam.class));
        }
        if(p.isAnnotationPresent(RealParam.class)){
            return ComponentParameter.from(name, type, p.getAnnotation(RealParam.class));
        }
        if(p.isAnnotationPresent(CategoricalParam.class)){
            return ComponentParameter.from(name, type, p.getAnnotation(CategoricalParam.class));
        }
        if(p.isAnnotationPresent(OrdinalParam.class)){
            return ComponentParameter.from(name, type,  p.getAnnotation(OrdinalParam.class));
        }
        if(p.isAnnotationPresent(ProvidedParam.class)){
            return ComponentParameter.from(name, type, p.getAnnotation(ProvidedParam.class));
        }

//        if (collectedClasses.contains(type)) {
//            return new ComponentParameter(name, ParameterType.LIST, false, new Object[]{});
//        }

        // Last option, not annotated but type is known
        if (types.containsKey(type)) {
            return ComponentParameter.from(name, type, types.get(type));
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

    public Map<Class<?>, List<ComponentParameter>> componentParams() {
        return Collections.unmodifiableMap(paramInfo);
    }

    public List<String> toIraceParams(List<Node> nodes){
        var iraceParams = new ArrayList<String>();
        String[] initialDecisionValues = new String[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            initialDecisionValues[i] = nodes.get(i).clazz.getSimpleName();
        }
        // TODO un poco chapucero
        var firstParam = ComponentParameter.toIraceParameterString("ROOT", "c", initialDecisionValues, "", "", "");
        firstParam = firstParam.substring(0, firstParam.lastIndexOf("|"));
        iraceParams.add(firstParam);

        // Preorder DFS tree transversal
        var context = new ArrayDeque<String>();
        for(var node: nodes){
            recursiveToIraceParams(node, iraceParams, context);
        }
        assert context.isEmpty();
        return iraceParams;
    }

    protected void recursiveToIraceParams(Node node, ArrayList<String> params, ArrayDeque<String> context) {
        String componentDecisionPrefix = toIraceParamName(context);
        if(componentDecisionPrefix.isBlank()){
            componentDecisionPrefix = node.paramName;
        } else {
            componentDecisionPrefix += PARAM_SEP + node.paramName;
        }

        String componentDecisionValue = node.clazz.getSimpleName();
        context.push(node.paramName + NAMEVALUE_SEP + componentDecisionValue);
        var nodeParams = this.paramInfo.get(node.clazz);

        for(var p: nodeParams){
            if(p.getType() != ParameterType.PROVIDED){
                context.push(p.name());
                var iraceParamName = toIraceParamName(context);
                String iraceParam = p.toIraceParameterString(iraceParamName, componentDecisionPrefix, componentDecisionValue);
                params.add(iraceParam);
                context.pop();
            }
        }

        for(var entry: node.children().entrySet()){
            var candidates = entry.getValue();
            for(var child: candidates){
                recursiveToIraceParams(child, params, context);
            }
        }

        context.pop();
    }

    // Generate combinations using a recursive DFS approach, bounded by the depth
    public List<Node> buildTree(int depth){
        var list = new ArrayList<Node>();
        for (Class<?> startPoint : inventoryService.getInventory().componentsByType().get(Algorithm.class)) {
            var node = recursiveBuildTree("ROOT", depth, startPoint, new ArrayDeque<>());
            if(node != null){
                list.add(node);
            }
        }
        return list;
    }

    protected Node recursiveBuildTree(String currentParamName, int maxDepth, Class<?> currentComponent, ArrayDeque<String> context){
        var params = this.paramInfo.get(currentComponent);
        if(params == null){
            log.debug("Ignoring component {} due to null params, context {}", currentComponent, context);
            return null;
        }
        context.push(currentComponent.getSimpleName());
        var allChildren = new HashMap<String, List<Node>>();
        for(var p: params){
            if(p.recursive()){
                var values = p.getValues();
                if(values.length == 0){
                    throw new IllegalArgumentException("Should not happen, testing");
                }
                var children = new ArrayList<Node>();
                if(context.size() < maxDepth){
                    for(var v: values){
                        var currentChildNode = recursiveBuildTree(p.name(), maxDepth, (Class<?>) v, context);
                        if(currentChildNode != null){
                            children.add(currentChildNode);
                        }
                    }
                }
                if(children.isEmpty()){
                    // No valid config found exploring this part of the tree, even if the other params have values we cannot continue
                    return null;
                }
                allChildren.put(p.name(), children);
            }
        }
        context.pop();
        return new Node(currentParamName, currentComponent, allChildren);
    }

    public record Node(String paramName, Class<?> clazz, Map<String, List<Node>> children){
        public Node(String paramName, Class<?> clazz){
            this(paramName, clazz, new HashMap<>());
        }

        @Override
        public String toString() {
            return "Node{" +
                    "n='" + paramName + '\'' +
                    ", t=" + clazz +
                    '}';
        }
    }
}
