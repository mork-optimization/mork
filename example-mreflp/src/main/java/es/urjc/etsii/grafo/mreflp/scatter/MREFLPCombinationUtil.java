package es.urjc.etsii.grafo.mreflp.scatter;

import es.urjc.etsii.grafo.mreflp.create.*;
import es.urjc.etsii.grafo.mreflp.model.*;
import es.urjc.etsii.grafo.util.ArrayUtil;
import es.urjc.etsii.grafo.util.random.RandomManager;

/** Reflection alignment and capacity-aware combination; group ordering is preserved. */
public final class MREFLPCombinationUtil {
    private MREFLPCombinationUtil() {}

    public static void requireSameInstance(MREFLPSolution left, MREFLPSolution right) {
        if (left.getInstance() != right.getInstance()) throw new IllegalArgumentException("Different instances");
    }

    public static boolean reflectedIsCloser(MREFLPSolution left, MREFLPSolution right) {
        requireSameInstance(left, right);
        int direct = 0, reflected = 0, k = left.getInstance().groups();
        for (int v = 0; v < left.getInstance().n(); v++) {
            if (left.group(v) != right.group(v)) direct++;
            if (left.group(v) != k - 1 - right.group(v)) reflected++;
        }
        return reflected < direct;
    }

    public static MREFLPSolution combine(MREFLPSolution left, MREFLPSolution right, double parentBias, double rcl) {
        boolean reflect = reflectedIsCloser(left, right);
        var instance = left.getInstance();
        var child = new MREFLPSolution(instance);
        var random = RandomManager.getRandom();
        int[] order = new int[instance.n()];
        for (int v = 0; v < order.length; v++) order[v] = v;
        ArrayUtil.shuffle(order);
        for (int v : order) {
            int a = left.group(v), b = reflect ? instance.groups() - 1 - right.group(v) : right.group(v);
            boolean favorLeft = random.nextDouble() < parentBias;
            int preferred = favorLeft ? a : b, other = favorLeft ? b : a;
            if (child.occupancy(preferred) < instance.capacity()) child.assign(v, preferred);
            else if (child.occupancy(other) < instance.capacity()) child.assign(v, other);
        }
        return MREFLPReconstructionUtil.complete(child, ConstructionPolicyNew.RCL, FacilityOrderNew.FLOW_DESCENDING, rcl, null, 0);
    }
}
