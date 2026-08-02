package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.autoconfig.generator.AlgorithmCandidateGenerator;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import es.urjc.etsii.grafo.config.SolverConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Builds the compact, user-facing description of the autoconfig search space.
 */
@Service
public class AutoconfigSearchSpaceService {

    private final SearchSpaceSnapshot snapshot;

    public AutoconfigSearchSpaceService(
            SolverConfig solverConfig,
            AlgorithmCandidateGenerator candidateGenerator
    ) {
        var roots = candidateGenerator.buildTree(
                solverConfig.getTreeDepth(),
                solverConfig.getMaxDerivationRepetition()
        );
        var iraceParameters = candidateGenerator.toIraceParams(roots);

        var rootNames = new ArrayList<String>(roots.size());
        for (var root : roots) {
            rootNames.add(root.className());
        }
        rootNames.sort(String::compareTo);

        int parameterCount = 0;
        int combinationParameterCount = 0;
        var components = new ArrayList<ComponentDescription>();
        for (var entry : candidateGenerator.componentParams().entrySet()) {
            var parameters = new ArrayList<ParameterDescription>();
            for (var parameter : entry.getValue()) {
                parameters.add(describe(parameter));
                parameterCount++;
                if (parameter.combination()) {
                    combinationParameterCount++;
                }
            }
            parameters.sort(Comparator.comparing(ParameterDescription::name));
            components.add(new ComponentDescription(entry.getKey().getSimpleName(), List.copyOf(parameters)));
        }
        components.sort(Comparator.comparing(ComponentDescription::name));

        this.snapshot = new SearchSpaceSnapshot(
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
                List.copyOf(rootNames),
                List.copyOf(components)
        );
    }

    public SearchSpaceSnapshot getSnapshot() {
        return snapshot;
    }

    private static ParameterDescription describe(ComponentParameter parameter) {
        var type = parameter.getType();
        var values = new ArrayList<Object>();
        var choices = new ArrayList<String>();
        Object minimum = null;
        Object maximum = null;
        Integer minItems = null;
        Integer maxItems = null;

        if (type == ParameterType.NOT_ANNOTATED || type == ParameterType.COMBINATION) {
            for (var value : parameter.getValues()) {
                choices.add(((Class<?>) value).getSimpleName());
            }
            choices.sort(String::compareTo);
        } else if (type == ParameterType.INTEGER || type == ParameterType.REAL) {
            minimum = parameter.getValues()[0];
            maximum = parameter.getValues()[1];
        } else if (type == ParameterType.CATEGORICAL || type == ParameterType.ORDINAL) {
            for (var value : parameter.getValues()) {
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
                List.copyOf(values),
                minimum,
                maximum,
                minItems,
                maxItems,
                List.copyOf(choices)
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
    }
}
