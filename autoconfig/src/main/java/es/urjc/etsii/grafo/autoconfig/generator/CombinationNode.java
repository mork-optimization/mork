package es.urjc.etsii.grafo.autoconfig.generator;

import java.util.List;

/**
 * One position in an ordered component combination.
 *
 * @param position zero-based position
 * @param choices valid component choices for the current prefix
 */
public record CombinationNode(int position, List<CombinationChoice> choices) {
}
