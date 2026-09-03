package es.urjc.etsii.grafo.autoconfig.generator;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.annotations.*;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderUtil;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmComponentFactory;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter.*;

@Service
public class AlgorithmCandidateGenerator {
    private final AlgorithmInventoryService inventoryService;
    private final IExplorationFilter explorationFilter;
    private final Logger log = LoggerFactory.getLogger(AlgorithmCandidateGenerator.class);
    private final Map<Class<?>, List<ComponentParameter>> paramInfo;
    private final Map<TreeSettings, List<TreeNode>> treeCache = new HashMap<>();

    public AlgorithmCandidateGenerator(AlgorithmInventoryService inventoryService, IExplorationFilter explorationFilter) {
        this.inventoryService = inventoryService;
        this.explorationFilter = explorationFilter;
        this.paramInfo = analyzeParameters();
        log.debug("Components available for autoconfig: {}", paramInfo.keySet().stream().map(Class::getSimpleName).sorted().toList());
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
                // Add only the candidates accepted for this parameter to the exploration queue.
                addRecursiveCandidates(queue, notVisited, cp.getValues());
            }
        }
        return Optional.of(componentParameters);
    }

    private static List<ComponentParameter> analyzeParametersFactory(Map<Class<?>, Collection<Class<?>>> byType, Queue<Class<?>> queue, Set<Class<?>> notVisited, AlgorithmComponentFactory factory) {
        var params = factory.getRequiredParameters();
        for (var cp : params) {
            if (cp.recursive()) {
                var candidates = byType.get(cp.getComponentType());
                if (candidates == null) {
                    throw new IllegalArgumentException("Factory parameter %s references unknown component type %s"
                            .formatted(cp.getName(), cp.getComponentType().getSimpleName()));
                }
                cp.setValues(candidates.toArray());
                // Parameter has a known algorithm component type, for example Improver<S,I>
                // Add all implementations to the exploration queue
                addRecursiveCandidates(queue, notVisited, cp.getValues());
            }
        }
        return params;
    }

    private static void addRecursiveCandidates(Queue<Class<?>> queue, Set<Class<?>> notVisited, Object[] candidates) {
        for (var candidate : candidates) {
            var candidateClass = (Class<?>) candidate;
            if (notVisited.contains(candidateClass)) {
                notVisited.remove(candidateClass);
                queue.add(candidateClass);
            }
        }
    }

    protected ComponentParameter toComponentParameter(Map<Class<?>, Collection<Class<?>>> types, Parameter p) {
        // Either the parameter is annotated or it is a known type that we have to recursively analyze
        var type = p.getType();
        var name = p.getName();
        if (!isValidParamName(name)) {
            throw new IllegalArgumentException(String.format("Invalid parameter name %s, must match ([a-zA-Z][a-zA-Z0-9]*)", name));
        }
        validateAnnotations(p);
        if (p.isAnnotationPresent(IntegerParam.class)) {
            var annotation = p.getAnnotation(IntegerParam.class);
            validateIntegerParameter(p, annotation);
            return ComponentParameter.from(name, type, annotation);
        }
        if (p.isAnnotationPresent(RealParam.class)) {
            var annotation = p.getAnnotation(RealParam.class);
            validateRealParameter(p, annotation);
            return ComponentParameter.from(name, type, annotation);
        }
        if (p.isAnnotationPresent(CategoricalParam.class)) {
            validateStringValues(p, p.getAnnotation(CategoricalParam.class).strings());
            return ComponentParameter.from(name, type, p.getAnnotation(CategoricalParam.class));
        }
        if (p.isAnnotationPresent(OrdinalParam.class)) {
            validateStringValues(p, p.getAnnotation(OrdinalParam.class).strings());
            return ComponentParameter.from(name, type, p.getAnnotation(OrdinalParam.class));
        }
        if (p.isAnnotationPresent(ProvidedParam.class)) {
            return ComponentParameter.from(name, type, p.getAnnotation(ProvidedParam.class));
        }
        if (p.isAnnotationPresent(ComponentParam.class)) {
            var annotation = p.getAnnotation(ComponentParam.class);
            if (isCombinationType(type)) {
                var componentType = resolveCombinationComponentType(p);
                validateCombinationParameter(p, annotation);
                if (!types.containsKey(componentType)) {
                    throw new IllegalArgumentException(String.format(
                            "Parameter %s is annotated with @ComponentParam, but collection element type %s is not a known algorithm component type",
                            describe(p), componentType.getSimpleName()));
                }
                var candidates = filterCandidates(p, componentType, types.get(componentType));
                if (annotation.min() > candidates.size()) {
                    throw new IllegalArgumentException("Invalid @ComponentParam bounds for %s: min %s cannot be satisfied by %s eligible components"
                            .formatted(describe(p), annotation.min(), candidates.size()));
                }
                return ComponentParameter.combination(name, type, componentType, candidates, annotation.min(), annotation.max());
            }
            if (Collection.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException("@ComponentParam collections must use List<T>. Found %s in %s"
                        .formatted(type.getTypeName(), describe(p)));
            }
            if (!types.containsKey(type)) {
                throw new IllegalArgumentException(String.format(
                        "Parameter %s is annotated with @ComponentParam, but type %s is not a known algorithm component type",
                        describe(p), type.getSimpleName()));
            }
            return ComponentParameter.from(name, type, filterCandidates(p, type, types.get(type)));
        }

        // Last option, not annotated but type is known
        if (types.containsKey(type)) {
            return ComponentParameter.from(name, type, types.get(type));
        }
        return null;
    }

    private static boolean isCombinationType(Class<?> type) {
        return type == List.class || type.isArray();
    }

    private static Class<?> resolveCombinationComponentType(Parameter p) {
        var type = p.getType();
        if (type.isArray()) {
            var componentType = type.getComponentType();
            if (componentType.isPrimitive() || componentType.isArray()) {
                throw new IllegalArgumentException("@ComponentParam arrays must be one-dimensional reference arrays. Found %s in %s"
                        .formatted(type.getTypeName(), describe(p)));
            }
            return componentType;
        }

        Type parameterizedType = p.getParameterizedType();
        if (!(parameterizedType instanceof ParameterizedType listType)) {
            throw new IllegalArgumentException("@ComponentParam lists must declare an element type. Found raw List in %s"
                    .formatted(describe(p)));
        }
        Type elementType = listType.getActualTypeArguments()[0];
        if (elementType instanceof Class<?> elementClass) {
            return elementClass;
        }
        if (elementType instanceof ParameterizedType parameterizedElement && parameterizedElement.getRawType() instanceof Class<?> rawClass) {
            return rawClass;
        }
        throw new IllegalArgumentException("@ComponentParam list element type must resolve to a concrete component class. Found %s in %s"
                .formatted(elementType.getTypeName(), describe(p)));
    }

    private static void validateCombinationParameter(Parameter p, ComponentParam annotation) {
        if (annotation.min() < 0) {
            throw new IllegalArgumentException("Invalid @ComponentParam range for %s: min must be non-negative"
                    .formatted(describe(p)));
        }
        if (annotation.min() > annotation.max()) {
            throw new IllegalArgumentException("Invalid @ComponentParam range for %s: min %s > max %s"
                    .formatted(describe(p), annotation.min(), annotation.max()));
        }
    }

    private static void validateAnnotations(Parameter p) {
        int nAnnotations = 0;
        nAnnotations += p.isAnnotationPresent(IntegerParam.class) ? 1 : 0;
        nAnnotations += p.isAnnotationPresent(RealParam.class) ? 1 : 0;
        nAnnotations += p.isAnnotationPresent(CategoricalParam.class) ? 1 : 0;
        nAnnotations += p.isAnnotationPresent(OrdinalParam.class) ? 1 : 0;
        nAnnotations += p.isAnnotationPresent(ProvidedParam.class) ? 1 : 0;
        nAnnotations += p.isAnnotationPresent(ComponentParam.class) ? 1 : 0;
        if (nAnnotations > 1) {
            throw new IllegalArgumentException("Parameter %s has multiple autoconfig parameter annotations; use exactly one".formatted(describe(p)));
        }
    }

    private static void validateIntegerParameter(Parameter p, IntegerParam annotation) {
        if (annotation.min() > annotation.max()) {
            throw new IllegalArgumentException("Invalid @IntegerParam range for %s: min %s > max %s".formatted(describe(p), annotation.min(), annotation.max()));
        }
        if (!isIntegerType(p.getType())) {
            throw new IllegalArgumentException("@IntegerParam can only be used on integer-compatible parameters. Found %s in %s".formatted(p.getType().getSimpleName(), describe(p)));
        }
    }

    private static void validateRealParameter(Parameter p, RealParam annotation) {
        if (annotation.min() > annotation.max()) {
            throw new IllegalArgumentException("Invalid @RealParam range for %s: min %s > max %s".formatted(describe(p), annotation.min(), annotation.max()));
        }
        if (!Double.isFinite(annotation.min()) || !Double.isFinite(annotation.max())) {
            throw new IllegalArgumentException("Invalid @RealParam range for %s: min and max must be finite".formatted(describe(p)));
        }
        if (!isRealType(p.getType())) {
            throw new IllegalArgumentException("@RealParam can only be used on real-compatible parameters. Found %s in %s".formatted(p.getType().getSimpleName(), describe(p)));
        }
    }

    private static void validateStringValues(Parameter p, String[] values) {
        if (values.length == 0 && !p.getType().isEnum()) {
            throw new IllegalArgumentException("Categorical and ordinal params must have at least one value. Found 0 values in %s".formatted(describe(p)));
        }
    }

    private static boolean isIntegerType(Class<?> type) {
        return type == byte.class || type == Byte.class
                || type == short.class || type == Short.class
                || type == int.class || type == Integer.class
                || type == long.class || type == Long.class
                || type == String.class;
    }

    private static boolean isRealType(Class<?> type) {
        return type == float.class || type == Float.class
                || type == double.class || type == Double.class
                || type == String.class;
    }

    private static Collection<Class<?>> filterCandidates(Parameter p, Class<?> componentType, Collection<Class<?>> candidates) {
        var componentParam = p.getAnnotation(ComponentParam.class);
        Class<?>[] disallowed = componentParam.disallowed();
        if (disallowed.length == 0) {
            return candidates;
        }

        for (var disallowedClass : disallowed) {
            if (!componentType.isAssignableFrom(disallowedClass)) {
                throw new IllegalArgumentException(String.format(
                        "Invalid @ComponentParam restriction in %s: disallowed class %s is not assignable to parameter type %s",
                        describe(p), disallowedClass.getSimpleName(), componentType.getSimpleName()));
            }
        }

        var filtered = new ArrayList<Class<?>>();
        for (var candidate : candidates) {
            if (!isDisallowed(candidate, disallowed)) {
                filtered.add(candidate);
            }
        }
        return filtered;
    }

    private static boolean isDisallowed(Class<?> candidate, Class<?>[] disallowed) {
        for (var disallowedClass : disallowed) {
            if (disallowedClass.isAssignableFrom(candidate)) {
                return true;
            }
        }
        return false;
    }

    private static String describe(Parameter p) {
        return "%s parameter %s".formatted(p.getDeclaringExecutable(), p.getName());
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

        iraceParams.add(ComponentParameter.toIraceParameterString("ROOT", ParameterType.CATEGORICAL, initialDecisionValues, ""));
        for (var node : nodes) {
            String componentName = node.className();
            recursiveToIraceParams(node, iraceParams, "ROOT" + NAMEVALUE_SEP + componentName, "ROOT", componentName);
        }
        Collections.sort(iraceParams);
        return iraceParams;
    }

    private void recursiveToIraceParams(TreeNode node, ArrayList<String> params, String componentPath, String activationParam, String activationValue) {
        var nodeParams = this.paramInfo.get(node.clazz());
        String activationCondition = selected(activationParam, activationValue);

        for (var p : nodeParams) {
            if (p.getType() == ParameterType.PROVIDED) {
                continue;
            }
            String paramPath = componentPath + PARAM_SEP + p.getName();
            if (p.combination()) {
                recursiveCombinationToIraceParams(node.combinations().get(p.getName()), params, paramPath, activationParam, activationValue);
            } else if (p.recursive()) {
                var children = node.children().get(p.getName());
                var values = getValidChildrenValuesForParam(children, p, node);
                params.add(ComponentParameter.toIraceParameterString(paramPath, ParameterType.CATEGORICAL, values, activationCondition));
                for (var child : children) {
                    String childName = child.className();
                    recursiveToIraceParams(
                            child,
                            params,
                            paramPath + NAMEVALUE_SEP + childName,
                            paramPath,
                            childName
                    );
                }
            } else {
                params.add(p.toIraceParameterString(paramPath, activationCondition));
            }
        }
    }

    private void recursiveCombinationToIraceParams(
            CombinationTree combination,
            ArrayList<String> params,
            String collectionPath,
            String activationParam,
            String activationValue
    ) {
        if (combination == null) {
            throw new IllegalStateException("Missing combination tree for " + collectionPath);
        }

        String lengthPath = collectionPath + PARAM_SEP + "length";
        boolean variableLength = combination.min() != combination.max();
        if (variableLength) {
            params.add(ComponentParameter.toIraceParameterString(
                    lengthPath,
                    ParameterType.INTEGER,
                    new Object[]{combination.min(), combination.max()},
                    selected(activationParam, activationValue)
            ));
        }
        if (combination.max() == 0) {
            return;
        }

        recursiveCombinationNodeToIraceParams(
                combination.root(),
                params,
                collectionPath + PARAM_SEP + "item0",
                activationParam,
                activationValue,
                variableLength ? lengthPath : null
        );
    }

    private void recursiveCombinationNodeToIraceParams(
            CombinationNode combinationNode,
            ArrayList<String> params,
            String selectorPath,
            String activationParam,
            String activationValue,
            String lengthPath
    ) {
        Class<?>[] values = new Class<?>[combinationNode.choices().size()];
        for (int i = 0; i < combinationNode.choices().size(); i++) {
            values[i] = combinationNode.choices().get(i).component().clazz();
        }
        Arrays.sort(values, Comparator.comparing(Class::getSimpleName));

        String condition = selected(activationParam, activationValue);
        if (lengthPath != null) {
            condition += " & " + lengthPath + " >= " + (combinationNode.position() + 1);
        }
        params.add(ComponentParameter.toIraceParameterString(selectorPath, ParameterType.CATEGORICAL, values, condition));

        for (var choice : combinationNode.choices()) {
            String componentName = choice.component().className();
            String selectedPrefix = selectorPath + NAMEVALUE_SEP + componentName;
            recursiveToIraceParams(
                    choice.component(),
                    params,
                    selectedPrefix + PARAM_SEP + "component",
                    selectorPath,
                    componentName
            );
            if (choice.next() != null) {
                recursiveCombinationNodeToIraceParams(
                        choice.next(),
                        params,
                        selectedPrefix + PARAM_SEP + "item" + choice.next().position(),
                        selectorPath,
                        componentName,
                        lengthPath
                );
            }
        }
    }

    private static String selected(String parameter, String value) {
        return parameter + " %in% c(\"" + value + "\")";
    }

    private Class<?>[] getValidChildrenValuesForParam(List<TreeNode> childrenForParameter, ComponentParameter p, TreeNode node) {
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
    public synchronized List<TreeNode> buildTree(int maxDepth, int maxRepeat) {
        var settings = new TreeSettings(maxDepth, maxRepeat);
        var cached = treeCache.get(settings);
        if (cached != null) {
            return cached;
        }

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
        var tree = List.copyOf(list);
        treeCache.put(settings, tree);
        return tree;
    }

    private record TreeSettings(int maxDepth, int maxRepeat) {
    }

    protected TreeNode recursiveBuildTree(String currentParamName, Class<?> currentComponent, TreeContext context) {
        if(explorationFilter.reject(context, currentComponent)){
            log.trace("Ignoring component {} due to filter. Context: {}", currentComponent, context);
            return null;
        }
        var params = this.paramInfo.get(currentComponent);
        if (params == null) {
            log.trace("Ignoring component {} due to null params, context {}", currentComponent, context);
            return null;
        }
        context.push(currentComponent);
        var allChildren = new HashMap<String, List<TreeNode>>();
        var allCombinations = new HashMap<String, CombinationTree>();
        for (var p : params) {
            if (p.recursive()) {
                var values = p.getValues();
                var children = exploreImplementations(context, p, values);
                if (p.combination()) {
                    int effectiveMax = Math.min(p.getMax(), children.size());
                    if (p.getMin() > effectiveMax) {
                        context.pop();
                        return null;
                    }
                    var root = buildCombinationNode(0, effectiveMax, children, new HashSet<>());
                    allCombinations.put(p.getName(), new CombinationTree(p.getMin(), effectiveMax, root));
                } else if (children.isEmpty()) {
                    // No valid config found exploring this part of the tree, even if the other params have values we cannot continue
                    context.pop();
                    return null;
                } else {
                    allChildren.put(p.getName(), children);
                }
            }
        }
        context.pop();
        return new TreeNode(currentParamName, currentComponent, allChildren, allCombinations);
    }

    private ArrayList<TreeNode> exploreImplementations(TreeContext context, ComponentParameter p, Object[] values) {
        var children = new ArrayList<TreeNode>();
        var sortedValues = new ArrayList<Class<?>>(values.length);
        for (var value : values) {
            sortedValues.add((Class<?>) value);
        }
        sortedValues.sort(Comparator.comparing(Class::getSimpleName));

        for (var target : sortedValues) {
            var derivation = new Derivation(p.getComponentType(), target);
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

    private CombinationNode buildCombinationNode(int position, int max, List<TreeNode> candidates, Set<Class<?>> used) {
        if (position >= max) {
            return null;
        }
        var choices = new ArrayList<CombinationChoice>();
        for (var candidate : candidates) {
            if (used.contains(candidate.clazz())) {
                continue;
            }
            used.add(candidate.clazz());
            var next = buildCombinationNode(position + 1, max, candidates, used);
            choices.add(new CombinationChoice(candidate, next));
            used.remove(candidate.clazz());
        }
        return new CombinationNode(position, choices);
    }
}
