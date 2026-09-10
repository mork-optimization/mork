package es.urjc.etsii.grafo.mmdp.neighborhoods;

import es.urjc.etsii.grafo.mmdp.model.MMDPInstance;
import es.urjc.etsii.grafo.mmdp.model.MMDPSolution;
import es.urjc.etsii.grafo.mmdp.model.ReplaceNodeMove;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.util.CollectionUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The MMDP swap neighbourhood: every move replaces a selected node with a non-selected one. The
 * {@code jmh} neighbourhood, expressed as a Mork {@link Neighborhood} for the reusable
 * local searches. The {@code randomized} flag reproduces the random / lexicographical candidate
 * orders of {@code jmh}. Node indices are 0-based.
 */
public class ReplaceNodeNeighborhood extends Neighborhood<ReplaceNodeMove, MMDPSolution, MMDPInstance> {

    private final boolean randomized;

    public ReplaceNodeNeighborhood(boolean randomized) {
        this.randomized = randomized;
    }

    @Override
    public ExploreResult<ReplaceNodeMove, MMDPSolution, MMDPInstance> explore(MMDPSolution solution) {
        int numNodes = solution.getInstance().getNumNodes();

        Stream<ReplaceNodeMove> moves = solution.getNodes().stream()
                .flatMap(oldNode -> IntStream.range(0, numNodes).boxed()
                        .filter(newNode -> !solution.contains(newNode))
                        .map(newNode -> new ReplaceNodeMove(solution, oldNode, newNode)));

        if (randomized) {
            List<ReplaceNodeMove> list = moves.collect(Collectors.toList());
            CollectionUtil.shuffle(list);
            return ExploreResult.fromList(list);
        }
        return ExploreResult.fromStream(moves);
    }
}
