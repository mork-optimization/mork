package es.urjc.etsii.grafo.autoconfig.generator;

import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Counts declarations and explains the cost of collection encoding without expanding permutations. */
public final class AutoconfigEncodingUtil {
    private AutoconfigEncodingUtil() {}

    public record Counts(BigInteger declarations, BigInteger prefixDeclarationEstimate, BigInteger forbiddenConstraints) {
        private static final Counts ZERO = new Counts(BigInteger.ZERO, BigInteger.ZERO, BigInteger.ZERO);
        private static final Counts DECLARATION = new Counts(BigInteger.ONE, BigInteger.ONE, BigInteger.ZERO);

        private Counts add(Counts other) {
            return new Counts(declarations.add(other.declarations), prefixDeclarationEstimate.add(other.prefixDeclarationEstimate),
                    forbiddenConstraints.add(other.forbiddenConstraints));
        }
    }

    public record RootCounts(String root, Counts counts) {}

    public record CollectionCounts(String path, int minItems, int maxItems, int candidateCount,
                                   BigInteger positionSelectors, BigInteger prefixSelectorsEstimate,
                                   BigInteger candidateDeclarationsPerPosition, Counts counts) {}

    public record EncodingDiagnostics(Counts total, BigInteger avoidedDeclarations,
                                      List<RootCounts> roots, List<CollectionCounts> collections) {
        public EncodingDiagnostics {
            roots = List.copyOf(roots);
            collections = List.copyOf(collections);
        }
    }

    public static Counts count(List<TreeNode> roots, Map<Class<?>, List<ComponentParameter>> parameters) {
        return new Counter(parameters).total(roots);
    }

    public static EncodingDiagnostics describe(List<TreeNode> roots, Map<Class<?>, List<ComponentParameter>> parameters) {
        var counter = new Counter(parameters);
        var total = counter.total(roots);
        var rootCounts = new ArrayList<RootCounts>();
        var collections = new ArrayList<CollectionCounts>();
        for (var root : roots) {
            rootCounts.add(new RootCounts(root.className(), counter.node(root)));
            counter.collect(root, "ROOT_" + root.className(), collections);
        }
        rootCounts.sort(Comparator.comparing(RootCounts::root));
        collections.sort(Comparator.comparing(CollectionCounts::path));
        return new EncodingDiagnostics(total, total.prefixDeclarationEstimate.subtract(total.declarations), rootCounts, collections);
    }

    private static final class Counter {
        private final Map<Class<?>, List<ComponentParameter>> parameters;
        private final Map<TreeNode, Counts> cache = new IdentityHashMap<>();

        private Counter(Map<Class<?>, List<ComponentParameter>> parameters) { this.parameters = parameters; }

        private Counts total(List<TreeNode> roots) {
            var result = Counts.DECLARATION; // ROOT selector
            for (var root : roots) result = result.add(node(root));
            return result;
        }

        private Counts node(TreeNode node) {
            var known = cache.get(node);
            if (known != null) return known;
            var result = Counts.ZERO;
            for (var parameter : parameters.get(node.clazz())) {
                if (parameter.getType() == ParameterType.PROVIDED) continue;
                if (parameter.combination()) {
                    result = result.add(collection(node.combinations().get(parameter.getName())));
                } else {
                    result = result.add(Counts.DECLARATION);
                    if (parameter.recursive()) {
                        for (var child : node.children().get(parameter.getName())) result = result.add(node(child));
                    }
                }
            }
            cache.put(node, result);
            return result;
        }

        private Counts candidateCounts(CombinationTree combination) {
            var result = Counts.ZERO;
            for (var candidate : combination.candidates()) result = result.add(node(candidate));
            return result;
        }

        private Counts collection(CombinationTree combination) {
            var candidates = candidateCounts(combination);
            var slots = BigInteger.valueOf(combination.max());
            var length = combination.min() == combination.max() ? BigInteger.ZERO : BigInteger.ONE;
            var declarations = slots.multiply(BigInteger.ONE.add(candidates.declarations)).add(length);
            var prefixSelectors = BigInteger.ZERO;
            var candidateRepetitions = BigInteger.ZERO;
            var prefixes = BigInteger.ONE;
            var occurrences = BigInteger.ONE;
            int n = combination.candidates().size();
            for (int position = 0; position < combination.max(); position++) {
                prefixSelectors = prefixSelectors.add(prefixes);
                candidateRepetitions = candidateRepetitions.add(occurrences);
                prefixes = prefixes.multiply(BigInteger.valueOf(n - position));
                occurrences = occurrences.multiply(BigInteger.valueOf(n - position - 1));
            }
            var previous = prefixSelectors.add(candidateRepetitions.multiply(candidates.prefixDeclarationEstimate)).add(length);
            var pairs = slots.multiply(slots.subtract(BigInteger.ONE)).divide(BigInteger.TWO);
            return new Counts(declarations, previous, pairs.add(slots.multiply(candidates.forbiddenConstraints)));
        }

        private void collect(TreeNode node, String path, List<CollectionCounts> result) {
            for (var parameter : parameters.get(node.clazz())) {
                if (!parameter.recursive()) continue;
                String parameterPath = path + "." + parameter.getName();
                if (parameter.combination()) {
                    var combination = node.combinations().get(parameter.getName());
                    var prefixSelectors = BigInteger.ZERO;
                    var prefixes = BigInteger.ONE;
                    for (int position = 0; position < combination.max(); position++) {
                        prefixSelectors = prefixSelectors.add(prefixes);
                        prefixes = prefixes.multiply(BigInteger.valueOf(combination.candidates().size() - position));
                    }
                    result.add(new CollectionCounts(parameterPath, combination.min(), combination.max(), combination.candidates().size(),
                            BigInteger.valueOf(combination.max()), prefixSelectors, candidateCounts(combination).declarations,
                            collection(combination)));
                    for (int position = 0; position < combination.max(); position++) {
                        for (var candidate : combination.candidates()) {
                            collect(candidate, parameterPath + ".item" + position + "_" + candidate.className() + ".component", result);
                        }
                    }
                } else {
                    for (var child : node.children().get(parameter.getName())) collect(child, parameterPath + "_" + child.className(), result);
                }
            }
        }
    }
}
