package es.urjc.etsii.grafo.tsptw.improve;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.tsptw.model.TSPTWNewMoveUtil;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;

/** First-improvement relocation of consecutive customers without reversing their order. */
public class TSPTWOrOptSearchNew extends TSPTWCostSearch {
    private final int blockLength;

    @AutoconfigConstructor
    public TSPTWOrOptSearchNew(@IntegerParam(min = 2, max = 3) int blockLength) {
        if (blockLength < 2 || blockLength > 3) throw new IllegalArgumentException("Block length must be 2 or 3");
        this.blockLength = blockLength;
    }

    public TSPTWOrOptSearchNew() { this(2); }

    @Override
    protected boolean improveOnce(TSPTWSolution solution) {
        return TSPTWNewMoveUtil.improveRelocation(solution, blockLength, true);
    }

    @Override
    public String toString() { return "TSPTWOrOptSearchNew{blockLength=" + blockLength + "}"; }
}
