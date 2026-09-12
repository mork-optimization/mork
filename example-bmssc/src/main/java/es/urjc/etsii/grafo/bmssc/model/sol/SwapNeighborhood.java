package es.urjc.etsii.grafo.bmssc.model.sol;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

public class SwapNeighborhood extends Neighborhood<SwapMove, BMSSCSolution, BMSSCInstance> {
    @AutoconfigConstructor
    public SwapNeighborhood() {}

    @Override
    public ExploreResult<SwapMove, BMSSCSolution, BMSSCInstance> explore(BMSSCSolution solution) {
        var moves = new Spliterators.AbstractSpliterator<SwapMove>(Long.MAX_VALUE, Spliterator.ORDERED | Spliterator.NONNULL) {
            int p = 0;
            int q = 1;

            @Override
            public boolean tryAdvance(Consumer<? super SwapMove> action) {
                while (p < solution.getInstance().n - 1 && !TimeControl.isTimeUp()) {
                    if (q >= solution.getInstance().n) {
                        q = ++p + 1;
                        continue;
                    }
                    int other = q++;
                    if (solution.canSwap(p, other)) {
                        action.accept(new SwapMove(solution, p, other));
                        return true;
                    }
                }
                return false;
            }
        };
        return ExploreResult.fromStream(StreamSupport.stream(moves, false));
    }
}
