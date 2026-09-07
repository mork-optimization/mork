package es.urjc.etsii.grafo.autoconfig.generator;

/**
 * A selected component and the remaining choices after selecting it.
 *
 * @param component selected component subtree
 * @param next next position, or {@code null} when the maximum length has been reached
 */
public record CombinationChoice(TreeNode component, CombinationNode next) {
}
