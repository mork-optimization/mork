package es.urjc.etsii.grafo.cph.neighborhoods;

import es.urjc.etsii.grafo.cph.model.CPHInstance;
import es.urjc.etsii.grafo.cph.model.CPHSolution;
import es.urjc.etsii.grafo.cph.model.ChangeHubMove;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.util.CollectionUtil;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The CPH swap neighbourhood: every move replaces an open hub with a node that is not currently a
 * hub. This is the {@code jmh} neighbourhood, now expressed as a Mork {@link Neighborhood}
 * so it can be consumed by {@code LocalSearchBestImprovement} / {@code LocalSearchFirstImprovement}.
 *
 * <p>The {@code randomized} flag reproduces the two first-improvement candidate orders of
 * {@code jmh}: lexicographical (moves generated in index order, lazily) or random (moves
 * materialized and shuffled). It is irrelevant for best improvement, which scans them all.
 */
public class ChangeHubNeighborhood extends Neighborhood<ChangeHubMove, CPHSolution, CPHInstance> {

    private final boolean randomized;

    public ChangeHubNeighborhood(boolean randomized) {
        this.randomized = randomized;
    }

    @Override
    public ExploreResult<ChangeHubMove, CPHSolution, CPHInstance> explore(CPHSolution solution) {
        int numNodes = solution.getInstance().getNumNodes();

        Stream<ChangeHubMove> moves = solution.getHubs().stream()
                .flatMap(oldHub -> IntStream.range(0, numNodes).boxed()
                        .filter(newHub -> !solution.isHub(newHub))
                        .map(newHub -> new ChangeHubMove(solution, oldHub, newHub)));

        if (randomized) {
            List<ChangeHubMove> list = moves.collect(Collectors.toList());
            CollectionUtil.shuffle(list);
            return ExploreResult.fromList(list);
        }
        return ExploreResult.fromStream(moves);
    }
}
