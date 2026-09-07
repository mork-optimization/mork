package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.generator.TreeNode;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import es.urjc.etsii.grafo.config.SolverConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static es.urjc.etsii.grafo.util.CollectionUtil.immutableListMap;

/**
 * Immutable canonical representation of the automatic configuration search space.
 */
@Service
public final class AutoconfigSearchSpace {

    private final List<TreeNode> roots;
    private final Map<Class<?>, List<ComponentParameter>> componentParameters;
    private final List<String> iraceParameters;
    private final SearchSpaceSnapshot snapshot;

    public AutoconfigSearchSpace(
            SolverConfig solverConfig,
            AlgorithmCandidateGenerator candidateGenerator
    ) {
        this.roots = List.copyOf(candidateGenerator.buildTree(
                solverConfig.getTreeDepth(),
                solverConfig.getMaxDerivationRepetition()
        ));
        this.componentParameters = immutableListMap(candidateGenerator.componentParams());
        this.iraceParameters = List.copyOf(candidateGenerator.toIraceParams(roots));
        this.snapshot = createSnapshot(solverConfig);
    }

    public List<TreeNode> roots() {
        return roots;
    }

    public Map<Class<?>, List<ComponentParameter>> componentParameters() {
        return componentParameters;
    }

    public List<String> iraceParameters() {
        return iraceParameters;
    }

    public SearchSpaceSnapshot snapshot() {
        return snapshot;
    }

    private SearchSpaceSnapshot createSnapshot(SolverConfig solverConfig) {
        var rootNames = new ArrayList<String>(roots.size());
        for (var root : roots) {
            rootNames.add(root.className());
        }
        rootNames.sort(String::compareTo);

        int parameterCount = 0;
        int combinationParameterCount = 0;
        var components = new ArrayList<ComponentDescription>();
        for (var entry : componentParameters.entrySet()) {
            var parameters = new ArrayList<ParameterDescription>();
            for (var parameter : entry.getValue()) {
                parameters.add(describe(parameter));
                parameterCount++;
                if (parameter.combination()) {
                    combinationParameterCount++;
                }
            }
            parameters.sort(Comparator.comparing(ParameterDescription::name));
            components.add(new ComponentDescription(entry.getKey().getSimpleName(), parameters));
        }
        components.sort(Comparator.comparing(ComponentDescription::name));

        return new SearchSpaceSnapshot(
                new GenerationLimits(
                        solverConfig.getTreeDepth(),
                        solverConfig.getMaxDerivationRepetition()
                ),
                new SearchSpaceSummary(
                        rootNames.size(),
                        components.size(),
                        parameterCount,
                        combinationParameterCount,
                        iraceParameters.size()
                ),
                rootNames,
                components
        );
    }

    private static ParameterDescription describe(ComponentParameter parameter) {
        var type = parameter.getType();
        var domain = parameter.getValues();
        var values = new ArrayList<Object>();
        var choices = new ArrayList<String>();
        Object minimum = null;
        Object maximum = null;
        Integer minItems = null;
        Integer maxItems = null;

        if (type == ParameterType.NOT_ANNOTATED || type == ParameterType.COMBINATION) {
            for (var value : domain) {
                choices.add(((Class<?>) value).getSimpleName());
            }
            choices.sort(String::compareTo);
        } else if (type == ParameterType.INTEGER || type == ParameterType.REAL) {
            minimum = domain[0];
            maximum = domain[1];
        } else if (type == ParameterType.CATEGORICAL || type == ParameterType.ORDINAL) {
            for (var value : domain) {
                values.add(value);
            }
        }

        if (type == ParameterType.COMBINATION) {
            minItems = parameter.getMin();
            maxItems = parameter.getMax();
        }

        return new ParameterDescription(
                parameter.getName(),
                publicKind(type),
                values,
                minimum,
                maximum,
                minItems,
                maxItems,
                choices
        );
    }

    private static ParameterKind publicKind(ParameterType type) {
        return switch (type) {
            case REAL -> ParameterKind.REAL;
            case INTEGER -> ParameterKind.INTEGER;
            case CATEGORICAL -> ParameterKind.CATEGORICAL;
            case ORDINAL -> ParameterKind.ORDINAL;
            case PROVIDED -> ParameterKind.PROVIDED;
            case NOT_ANNOTATED -> ParameterKind.COMPONENT;
            case COMBINATION -> ParameterKind.COMBINATION;
        };
    }

    public enum ParameterKind {
        INTEGER,
        REAL,
        CATEGORICAL,
        ORDINAL,
        PROVIDED,
        COMPONENT,
        COMBINATION
    }

    public record SearchSpaceSnapshot(
            GenerationLimits limits,
            SearchSpaceSummary summary,
            List<String> roots,
            List<ComponentDescription> components
    ) {
        public SearchSpaceSnapshot {
            roots = List.copyOf(roots);
            components = List.copyOf(components);
        }
    }

    public record GenerationLimits(int treeDepth, int maxDerivationRepetition) {
    }

    public record SearchSpaceSummary(
            int rootCount,
            int componentCount,
            int parameterCount,
            int combinationParameterCount,
            int generatedIraceParameterCount
    ) {
    }

    public record ComponentDescription(String name, List<ParameterDescription> parameters) {
        public ComponentDescription {
            parameters = List.copyOf(parameters);
        }
    }

    public record ParameterDescription(
            String name,
            ParameterKind kind,
            List<Object> values,
            Object minimum,
            Object maximum,
            Integer minItems,
            Integer maxItems,
            List<String> choices
    ) {
        public ParameterDescription {
            values = List.copyOf(values);
            choices = List.copyOf(choices);
        }
    }
}
