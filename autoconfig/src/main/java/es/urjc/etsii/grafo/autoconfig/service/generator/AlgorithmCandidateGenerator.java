package es.urjc.etsii.grafo.autoconfig.service.generator;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.autoconfig.AlgorithmBuilderUtil;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import es.urjc.etsii.grafo.autoconfig.service.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.service.factories.AlgorithmComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;

import static es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter.*;

@Service
public class AlgorithmCandidateGenerator {
    private static final Set<Class<?>> collectedClasses = Set.of(List.class, ArrayList.class, Set.class, HashSet.class, Collection.class);

    private final AlgorithmInventoryService inventoryService;
    private final Logger log = LoggerFactory.getLogger(AlgorithmCandidateGenerator.class);
    private final Map<Class<?>, List<ComponentParameter>> paramInfo;

    public AlgorithmCandidateGenerator(AlgorithmInventoryService inventoryService) {
        this.inventoryService = inventoryService;
        this.paramInfo = analyzeParameters();
        log.info("Components available for autoconfig: {}", paramInfo.keySet().stream().map(Class::getSimpleName).sorted().toList());
    }

    protected boolean isValidParamName(String name) {
        return name.matches("[a-zA-Z][a-zA-Z0-9]*");
    }

    /**
     * Analyze algorithm components recursively, starting from all algorithm classes, and extract parameter information
     *
     * @return parameter info for each component
     */
    protected Map<Class<?>, List<ComponentParameter>> analyzeParameters() {
        var inventory = this.inventoryService.getInventory();
        var algClasses = inventory.componentsByType().get(Algorithm.class);
        var byType = inventory.componentsByType();

        Queue<Class<?>> queue = new ArrayDeque<>(algClasses);
        var notVisited = inventory.allComponents();
        notVisited.removeAll(algClasses);

        // For each analyzed alg component, its list of parameters/dependencies
        var result = new HashMap<Class<?>, List<ComponentParameter>>();

        // Start exploring the algorithms using a BFS approach, continue by their dependencies
        while (!queue.isEmpty()) {
            var currentComponentClass = queue.remove();
            var factory = this.inventoryService.getFactoryFor(currentComponentClass);
            if (factory != null) {
                // First strategy: using a factory
                List<ComponentParameter> params = analyzeParametersFactory(byType, queue, notVisited, factory);
                result.put(currentComponentClass, params);
            } else {
                // Second strategy: use autoconfig constructor
                var constructor = AlgorithmBuilderUtil.findAutoconfigConstructor(currentComponentClass);
                if (constructor == null) {
                    log.debug("Skipping component {}, could not find constructor annotated with @AutoconfigConstructor", currentComponentClass.getSimpleName());
                    continue;
                }
                analyzeParametersConstructor(byType, queue, notVisited, constructor)
                        .ifPresent(cp -> result.put(currentComponentClass, cp));
            }
        }
        if (!notVisited.isEmpty()) {
            log.debug("Ignored components because they are not reachable from any algorithms: {}", notVisited);
        }
        return result;
    }

    private Optional<ArrayList<ComponentParameter>> analyzeParametersConstructor(Map<Class<?>, Collection<Class<?>>> byType, Queue<Class<?>> queue, Set<Class<?>> notVisited, Constructor<?> constructor) {
        var componentParameters = new ArrayList<ComponentParameter>();
        for (Parameter p : constructor.getParameters()) {
            var cp = toComponentParameter(byType, p);
            if (cp == null) {
                log.debug("Constructor {} ignored because parameter {} with type {} is not annotated and it is not a known type", constructor, p.getName(), p.getType());
                return Optional.empty();
            }
            componentParameters.add(cp);
            if (cp.recursive()) {
                // Parameter has a known algorithm component type, for example Improver<S,I>
                // Add all implementations to the exploration queue
                for (var candidate : byType.get(p.getType())) {
                    if (notVisited.contains(candidate)) {
                        notVisited.remove(candidate);
                        queue.add(candidate);
                    }
                }
            }
        }
        return Optional.of(componentParameters);
    }

    private static List<ComponentParameter> analyzeParametersFactory(Map<Class<?>, Collection<Class<?>>> byType, Queue<Class<?>> queue, Set<Class<?>> notVisited, AlgorithmComponentFactory factory) {
        var params = factory.getRequiredParameters();
        for (var cp : params) {
            if (cp.recursive()) {
                var candidates = byType.get(cp.getJavaType());
                cp.setValues(candidates.toArray());
                // Parameter has a known algorithm component type, for example Improver<S,I>
                // Add all implementations to the exploration queue
                for (var candidate : candidates) {
                    if (notVisited.contains(candidate)) {
                        notVisited.remove(candidate);
                        queue.add(candidate);
                    }
                }
            }
        }
        return params;
    }

    protected ComponentParameter toComponentParameter(Map<Class<?>, Collection<Class<?>>> types, Parameter p) {
        // Either the parameter is annotated or it is a known type that we have to recursively analyze
        var type = p.getType();
        var name = p.getName();
        if (!isValidParamName(name)) {
            throw new IllegalArgumentException(String.format("Invalid parameter name %s, must match[a-zA-Z][a-zA-Z0-9]*)", name));
        }
        if (p.isAnnotationPresent(IntegerParam.class)) {
            return ComponentParameter.from(name, type, p.getAnnotation(IntegerParam.class));
        }
        if (p.isAnnotationPresent(RealParam.class)) {
            return ComponentParameter.from(name, type, p.getAnnotation(RealParam.class));
        }
        if (p.isAnnotationPresent(CategoricalParam.class)) {
            return ComponentParameter.from(name, type, p.getAnnotation(CategoricalParam.class));
        }
        if (p.isAnnotationPresent(OrdinalParam.class)) {
            return ComponentParameter.from(name, type, p.getAnnotation(OrdinalParam.class));
        }
        if (p.isAnnotationPresent(ProvidedParam.class)) {
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

    public Map<Class<?>, List<ComponentParameter>> componentParams() {
        return Collections.unmodifiableMap(paramInfo);
    }

    public List<String> toIraceParams(List<TreeNode> nodes) {
        var iraceParams = new ArrayList<String>();
        Class<?>[] initialDecisionValues = new Class[nodes.size()];
        for (int i = 0; i < nodes.size(); i++) {
            initialDecisionValues[i] = nodes.get(i).clazz();
        }
        Arrays.sort(initialDecisionValues, Comparator.comparing(Class::getSimpleName));

        var firstParam = ComponentParameter.toIraceParameterString("ROOT", ParameterType.CATEGORICAL, initialDecisionValues, "", "", "");
        firstParam = firstParam.substring(0, firstParam.lastIndexOf("|"));
        iraceParams.add(firstParam);

        // Preorder DFS tree transversal
        var context = new ArrayDeque<String>();
        for (var node : nodes) {
            recursiveToIraceParams(node, iraceParams, context);
        }
        assert context.isEmpty();
        Collections.sort(iraceParams);
        return iraceParams;
    }

    protected void recursiveToIraceParams(TreeNode node, ArrayList<String> params, ArrayDeque<String> context) {
        String componentDecisionPrefix = toIraceParamName(context);
        if (componentDecisionPrefix.isBlank()) {
            componentDecisionPrefix = node.paramName();
        } else {
            componentDecisionPrefix += PARAM_SEP + node.paramName();
        }

        String componentDecisionValue = node.clazz().getSimpleName();
        context.push(node.paramName() + NAMEVALUE_SEP + componentDecisionValue);
        var nodeParams = this.paramInfo.get(node.clazz());

        for (var p : nodeParams) {
            if (p.getType() != ParameterType.PROVIDED) {
                context.push(p.getName());
                var iraceParamName = toIraceParamName(context);
                String iraceParam = p.recursive() ?
                        p.toIraceParameterStringNotAnnotated(iraceParamName, componentDecisionPrefix, componentDecisionValue, getValidChildrenValuesForParam(node, p)) :
                        p.toIraceParameterString(iraceParamName, componentDecisionPrefix, componentDecisionValue);

                params.add(iraceParam);
                context.pop();
            }
        }

        for (var entry : node.children().entrySet()) {
            var candidates = entry.getValue();
            for (var child : candidates) {
                recursiveToIraceParams(child, params, context);
            }
        }

        context.pop();
    }

    private Class<?>[] getValidChildrenValuesForParam(TreeNode node, ComponentParameter p) {
        var childrenForParameter = node.children().get(p.getName());
        Class<?>[] validClasses = new Class[childrenForParameter.size()];
        if (childrenForParameter.isEmpty()) {
            throw new IllegalStateException(String.format("Empty children for param %s in node %s, should have been pruned before", p, node));
        }
        for (int i = 0; i < childrenForParameter.size(); i++) {
            validClasses[i] = childrenForParameter.get(i).clazz();
        }
        Arrays.sort(validClasses, Comparator.comparing(Class::getSimpleName));
        return validClasses;
    }

    // Generate combinations using a recursive DFS approach, bounded by the maxDepth
    public List<TreeNode> buildTree(int maxDepth, int maxRepeat) {
        var list = new ArrayList<TreeNode>();
        for (Class<?> startPoint : inventoryService.getInventory().componentsByType().get(Algorithm.class)) {
            var treeContext = new TreeContext(maxDepth, maxRepeat);
            var node = recursiveBuildTree("ROOT", startPoint, treeContext);

            assert treeContext.derivationCounter().values().stream().mapToInt(i -> i).filter(i -> i != 0).findAny().isEmpty() : "[BUG FOUND] Derivation counter must be empty after full tree walk: " + treeContext.derivationCounter();
            assert treeContext.branch().isEmpty() : "[BUG FOUND] Branch context must be empty after full tree walk: " + treeContext.branch();

            if (node != null) {
                list.add(node);
            }
        }
        return list;
    }

    protected TreeNode recursiveBuildTree(String currentParamName, Class<?> currentComponent, TreeContext context) {
        var params = this.paramInfo.get(currentComponent);
        if (params == null) {
            log.debug("Ignoring component {} due to null params, context {}", currentComponent, context);
            return null;
        }
        context.push(currentComponent.getSimpleName());
        var allChildren = new HashMap<String, List<TreeNode>>();
        for (var p : params) {
            if (p.recursive()) {
                var values = p.getValues();
                assert values.length > 0;
                var children = exploreImplementations(context, p, values);
                if (children.isEmpty()) {
                    // No valid config found exploring this part of the tree, even if the other params have values we cannot continue
                    context.pop();
                    return null;
                }
                allChildren.put(p.getName(), children);
            }
        }
        context.pop();
        return new TreeNode(currentParamName, currentComponent, allChildren);
    }

    private ArrayList<TreeNode> exploreImplementations(TreeContext context, ComponentParameter p, Object[] values) {
        var children = new ArrayList<TreeNode>();
        for (var object : values) {
            var target = (Class<?>) object;
            var derivation = new Derivation(p.getJavaType(), target);
            if(context.inLimits(derivation)){
                context.pushDerivation(derivation);
                var currentChildNode = recursiveBuildTree(p.getName(), target, context);
                context.popDerivation(derivation);
                if (currentChildNode != null) {
                    children.add(currentChildNode);
                }
            }
        }
        return children;
    }
}
