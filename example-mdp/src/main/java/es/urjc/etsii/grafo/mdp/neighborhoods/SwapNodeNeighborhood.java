package es.urjc.etsii.grafo.mdp.neighborhoods;

import es.urjc.etsii.grafo.mdp.model.MDPInstance;
import es.urjc.etsii.grafo.mdp.model.MDPNode;
import es.urjc.etsii.grafo.mdp.model.MDPSolution;
import es.urjc.etsii.grafo.mdp.model.SwapNodeMove;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

import java.util.List;
import java.util.stream.Stream;

/**
 * The MDP swap neighbourhood: every move replaces a selected node with a node that is not currently
 * selected. Expressed as a Mork {@link Neighborhood} so it can drive
 * {@code LocalSearchBestImprovement}, which is used as the improvement method of the Scatter Search.
 */
public class SwapNodeNeighborhood extends Neighborhood<SwapNodeMove, MDPSolution, MDPInstance> {

    @Override
    public ExploreResult<SwapNodeMove, MDPSolution, MDPInstance> explore(MDPSolution solution) {
        MDPInstance instance = solution.getInstance();
        List<MDPNode> selected = solution.createNodeList();

        Stream<SwapNodeMove> moves = selected.stream()
                .flatMap(oldNode -> instance.getNodes().stream()
                        .filter(newNode -> !solution.contains(newNode))
                        .map(newNode -> new SwapNodeMove(solution, oldNode, newNode)));

        return ExploreResult.fromStream(moves);
    }
}
