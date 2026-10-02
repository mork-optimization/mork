package es.urjc.etsii.grafo.mreflp.scatter;

import es.urjc.etsii.grafo.algorithms.scattersearch.SolutionDistance;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.mreflp.model.*;

/** Hamming distance minimized over identity and reflection of the group axis. */
public final class MREFLPSolutionDistanceNew extends SolutionDistance<MREFLPSolution, MREFLPInstance> {
    @AutoconfigConstructor public MREFLPSolutionDistanceNew() {}
    @Override public double distances(MREFLPSolution left, MREFLPSolution right) {
        MREFLPCombinationUtil.requireSameInstance(left, right);
        int direct = 0, reflected = 0, k = left.getInstance().groups();
        for (int v = 0; v < left.getInstance().n(); v++) {
            if (left.group(v) != right.group(v)) direct++;
            if (left.group(v) != k - 1 - right.group(v)) reflected++;
        }
        return Math.min(direct, reflected);
    }
}
