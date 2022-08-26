package es.urjc.etsii.grafo.solution.neighborhood;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.List;

public class ListExploreResult<M extends Move<S, I>, S extends Solution<S, I>, I extends Instance> extends ExploreResult<M,S,I> {
    private final List<M> moveList;

    ListExploreResult(List<M> moveList) {
        super(moveList.stream(), moveList.size());
        this.moveList = moveList;
    }

    public List<M> moveList() {
        return moveList;
    }
}
