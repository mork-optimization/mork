package es.urjc.etsii.grafo.bmssc.model.sol;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.util.ArrayUtil;

/** Enumerates every swap once in the order induced by a fresh point permutation. */
public class ShuffledSwapNeighborhood extends SwapNeighborhoodNew {
    @AutoconfigConstructor
    public ShuffledSwapNeighborhood() {}

    @Override
    protected int[] pointOrder(BMSSCSolution solution) {
        int[] points = super.pointOrder(solution);
        ArrayUtil.shuffle(points);
        return points;
    }
}
