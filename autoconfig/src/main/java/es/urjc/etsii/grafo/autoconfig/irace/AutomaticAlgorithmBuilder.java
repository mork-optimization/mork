package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderService;
import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpec;
import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.generator.CombinationChoice;
import es.urjc.etsii.grafo.autoconfig.generator.CombinationNode;
import es.urjc.etsii.grafo.autoconfig.generator.CombinationTree;
import es.urjc.etsii.grafo.autoconfig.generator.TreeNode;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import tools.jackson.databind.JsonNode;

import java.util.*;

public class AutomaticAlgorithmBuilder<S extends Solution<S,I>, I extends Instance> extends AlgorithmBuilder<S,I> {

    private final List<TreeNode> algorithmCandidateTree;
    private final AlgorithmBuilderService algorithmBuilder;
    private final Map<Class<?>, List<ComponentParameter>> componentParams;

    public AutomaticAlgorithmBuilder(SolverConfig solverConfig, AlgorithmCandidateGenerator candidateGenerator, AlgorithmBuilderService algorithmBuilder) {
        this.algorithmBuilder = algorithmBuilder;
        this.algorithmCandidateTree = candidateGenerator.buildTree(solverConfig.getTreeDepth(), solverConfig.getMaxDerivationRepetition());
        this.componentParams = candidateGenerator.componentParams();
    }

    public List<TreeNode> getAlgorithmCandidateTree() {
        return Collections.unmodifiableList(algorithmCandidateTree);
    }

    public ComponentSpec asComponentSpec(AlgorithmConfiguration config){
        String rootName = requiredValue(config, "ROOT");
        TreeNode root = null;
        for (var candidate : algorithmCandidateTree) {
            if (candidate.className().equals(rootName)) {
                root = candidate;
                break;
            }
        }
        if (root == null) {
            var availableRoots = new ArrayList<String>();
            for (var candidate : algorithmCandidateTree) {
                availableRoots.add(candidate.className());
            }
            throw new IllegalArgumentException("Unknown ROOT component %s, available roots: %s"
                    .formatted(rootName, availableRoots));
        }
        return toComponentSpec(root, "ROOT" + ComponentParameter.NAMEVALUE_SEP + rootName, config);
    }

    public String asJson(AlgorithmConfiguration config) {
        return algorithmBuilder.toJson(asComponentSpec(config));
    }

    public JsonNode asJsonTree(AlgorithmConfiguration config) {
        return algorithmBuilder.toJsonTree(asComponentSpec(config));
    }

    @SuppressWarnings("unchecked")
    public Algorithm<S, I> buildFromJson(String jsonDescription){
        return (Algorithm<S, I>) this.algorithmBuilder.buildAlgorithmFromJson(jsonDescription);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Algorithm<S, I> buildFromConfig(AlgorithmConfiguration config) {
        return (Algorithm<S, I>) algorithmBuilder.buildAlgorithm(asComponentSpec(config));
    }

    private ComponentSpec toComponentSpec(TreeNode node, String componentPath, AlgorithmConfiguration config) {
        var parameters = new LinkedHashMap<String, Object>();
        for (var parameter : componentParams.get(node.clazz())) {
            if (parameter.getType() == ParameterType.PROVIDED) {
                continue;
            }
            String parameterPath = componentPath + ComponentParameter.PARAM_SEP + parameter.getName();
            if (parameter.combination()) {
                parameters.put(
                        parameter.getName(),
                        toCombinationSpecs(node.combinations().get(parameter.getName()), parameterPath, config)
                );
            } else if (parameter.recursive()) {
                String selectedComponent = requiredValue(config, parameterPath);
                TreeNode child = findChild(node.children().get(parameter.getName()), selectedComponent, parameterPath);
                parameters.put(
                        parameter.getName(),
                        toComponentSpec(
                                child,
                                parameterPath + ComponentParameter.NAMEVALUE_SEP + selectedComponent,
                                config
                        )
                );
            } else {
                parameters.put(
                        parameter.getName(),
                        IraceParameterValueUtil.decode(
                                parameter.getType(),
                                parameter.getName(),
                                requiredValue(config, parameterPath)
                        )
                );
            }
        }
        return new ComponentSpec(node.className(), parameters);
    }

    private List<ComponentSpec> toCombinationSpecs(CombinationTree combination, String collectionPath, AlgorithmConfiguration config) {
        if (combination == null) {
            throw new IllegalStateException("Missing combination tree for " + collectionPath);
        }
        int length = combination.min() == combination.max()
                ? combination.min()
                : Integer.parseInt(requiredValue(config, collectionPath + ComponentParameter.PARAM_SEP + "length"));
        if (length < combination.min() || length > combination.max()) {
            throw new IllegalArgumentException("Invalid length %s for %s, expected range [%s, %s]"
                    .formatted(length, collectionPath, combination.min(), combination.max()));
        }

        var components = new ArrayList<ComponentSpec>(length);
        CombinationNode current = combination.root();
        String selectorPath = collectionPath + ComponentParameter.PARAM_SEP + "item0";
        for (int position = 0; position < length; position++) {
            if (current == null) {
                throw new IllegalArgumentException("Combination %s ended before configured length %s"
                        .formatted(collectionPath, length));
            }
            String selectedComponent = requiredValue(config, selectorPath);
            CombinationChoice choice = null;
            for (var candidate : current.choices()) {
                if (candidate.component().className().equals(selectedComponent)) {
                    choice = candidate;
                    break;
                }
            }
            if (choice == null) {
                throw new IllegalArgumentException("Invalid component %s for %s"
                        .formatted(selectedComponent, selectorPath));
            }
            String selectedPrefix = selectorPath + ComponentParameter.NAMEVALUE_SEP + selectedComponent;
            components.add(toComponentSpec(
                    choice.component(),
                    selectedPrefix + ComponentParameter.PARAM_SEP + "component",
                    config
            ));
            current = choice.next();
            if (current != null) {
                selectorPath = selectedPrefix + ComponentParameter.PARAM_SEP + "item" + current.position();
            }
        }
        return components;
    }

    private static TreeNode findChild(List<TreeNode> children, String selectedComponent, String parameterPath) {
        if (children != null) {
            for (var child : children) {
                if (child.className().equals(selectedComponent)) {
                    return child;
                }
            }
        }
        throw new IllegalArgumentException("Invalid component %s for %s".formatted(selectedComponent, parameterPath));
    }

    private static String requiredValue(AlgorithmConfiguration config, String parameter) {
        return config.getValue(parameter)
                .orElseThrow(() -> new IllegalArgumentException("Missing required algorithm parameter " + parameter));
    }
}
