package es.urjc.etsii.grafo.cwp.neighborhoods;

import es.urjc.etsii.grafo.cwp.model.CWPInstance;
import es.urjc.etsii.grafo.cwp.model.CWPSolution;
import es.urjc.etsii.grafo.cwp.model.SwapNodesMove;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.util.CollectionUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The CWP swap neighbourhood: every move exchanges the positions of two vertices in the ordering.
 * The {@code jmh} neighbourhood, expressed as a Mork {@link Neighborhood} for the reusable
 * local searches. The {@code randomized} flag reproduces the random / lexicographical candidate
 * orders of {@code jmh}.
 */
public class SwapNodesNeighborhood extends Neighborhood<SwapNodesMove, CWPSolution, CWPInstance> {

    private final boolean randomized;

    public SwapNodesNeighborhood(boolean randomized) {
        this.randomized = randomized;
    }

    @Override
    public ExploreResult<SwapNodesMove, CWPSolution, CWPInstance> explore(CWPSolution solution) {
        int numNodes = solution.getInstance().getNumNodes();

        Stream<SwapNodesMove> moves = solution.getOrder().stream()
                .flatMap(oldNode -> IntStream.range(0, numNodes).boxed()
                        .filter(newNode -> oldNode != newNode.intValue())
                        .map(newNode -> new SwapNodesMove(solution, oldNode, newNode)));

        if (randomized) {
            List<SwapNodesMove> list = moves.collect(Collectors.toList());
            CollectionUtil.shuffle(list);
            return ExploreResult.fromList(list);
        }
        return ExploreResult.fromStream(moves);
    }
}
