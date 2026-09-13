package es.urjc.etsii.grafo.flayouts.constructives;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.create.Reconstructive;
import es.urjc.etsii.grafo.flayouts.model.*;
import es.urjc.etsii.grafo.util.CollectionUtil;
import es.urjc.etsii.grafo.util.random.RandomManager;

/** Corrected copy of the random append policy; also repairs partially assigned layouts. */
public class DRFPRandomConstructiveNew extends Reconstructive<FLPSolution, FLPInstance> {
    @AutoconfigConstructor
    public DRFPRandomConstructiveNew() {}

    @Override
    public FLPSolution construct(FLPSolution solution) {
        var missing = FLPNewUtil.missing(solution);
        CollectionUtil.shuffle(missing);
        for (int facility : missing) {
            int row = RandomManager.getRandom().nextInt(solution.nRows());
            FLPNewUtil.add(solution, facility, row, solution.rowSize(row), true).execute(solution);
        }
        return solution;
    }

    @Override
    public FLPSolution reconstruct(FLPSolution solution) { return construct(solution); }
}
