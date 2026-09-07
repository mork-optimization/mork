package es.urjc.etsii.grafo.autoconfig.generator;

/**
 * A factorized tree of ordered component selections without repetition.
 *
 * @param min minimum number of selected components
 * @param max maximum number of selected components
 * @param root first component position
 */
public record CombinationTree(int min, int max, CombinationNode root) {
}
