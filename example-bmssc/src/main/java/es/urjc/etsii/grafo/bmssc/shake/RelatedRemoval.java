package es.urjc.etsii.grafo.bmssc.shake;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.bmssc.util.BMSSCNewUtil;
import es.urjc.etsii.grafo.shake.Destructive;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.ArrayList;
import java.util.Comparator;

/** Remove a random seed and its nearest points, breaking distance ties by point ID. */
public class RelatedRemoval extends Destructive<BMSSCSolution, BMSSCInstance> {
    private final double fraction;

    @AutoconfigConstructor
    public RelatedRemoval(@RealParam(min = 0.01, max = 0.5) double fraction) {
        BMSSCNewUtil.validateUnitInterval(fraction);
        this.fraction = fraction;
    }

    @Override
    public BMSSCSolution destroy(BMSSCSolution solution, int k) {
        int count = BMSSCNewUtil.removalCount(solution, fraction, k);
        if (count == 0 || TimeControl.isTimeUp()) return solution;
        var instance = solution.getInstance();
        int seed = RandomManager.getRandom().nextInt(instance.n);
        var points = new ArrayList<Integer>(instance.n - 1);
        for (int p = 0; p < instance.n; p++) {
            if (p != seed) points.add(p);
        }
        points.sort(Comparator.<Integer>comparingDouble(p -> instance.distance(seed, p)).thenComparingInt(p -> p));
        boolean[] removed = new boolean[instance.n];
        removed[seed] = true;
        for (int i = 0; i < count - 1; i++) removed[points.get(i)] = true;
        if (TimeControl.isTimeUp()) return solution;
        return BMSSCNewUtil.retain(solution, removed);
    }

    @Override
    public String toString() { return "RelatedRemoval{fraction=" + fraction + "}"; }
}
