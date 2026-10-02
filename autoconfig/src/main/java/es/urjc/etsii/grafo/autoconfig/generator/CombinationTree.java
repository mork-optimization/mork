package es.urjc.etsii.grafo.autoconfig.generator;

import java.util.List;

/** Eligible subtrees for an ordered collection of distinct implementation classes. */
public record CombinationTree(int min, int max, List<TreeNode> candidates) {
    public CombinationTree {
        candidates = List.copyOf(candidates);
        if (min < 0 || max < min || max > candidates.size()) {
            throw new IllegalArgumentException("Invalid component collection bounds: " + min + ".." + max);
        }
    }
}
