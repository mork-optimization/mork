package es.urjc.etsii.grafo.bmssc.shake;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.bmssc.util.BMSSCNewUtil;
import es.urjc.etsii.grafo.shake.Destructive;
import es.urjc.etsii.grafo.util.TimeControl;

/** Remove a uniform sample of distinct points and return a replacement partial solution. */
public class RandomRemoval extends Destructive<BMSSCSolution, BMSSCInstance> {
    private final double fraction;

    @AutoconfigConstructor
    public RandomRemoval(@RealParam(min = 0.01, max = 0.5) double fraction) {
        BMSSCNewUtil.validateUnitInterval(fraction);
        this.fraction = fraction;
    }

    @Override
    public BMSSCSolution destroy(BMSSCSolution solution, int k) {
        int count = BMSSCNewUtil.removalCount(solution, fraction, k);
        if (count == 0 || TimeControl.isTimeUp()) return solution;
        int[] points = new int[solution.getInstance().n];
        for (int p = 0; p < points.length; p++) points[p] = p;
        boolean[] removed = new boolean[points.length];
        for (int point : BMSSCNewUtil.sample(points, count)) removed[point] = true;
        if (TimeControl.isTimeUp()) return solution;
        return BMSSCNewUtil.retain(solution, removed);
    }

    @Override
    public String toString() { return "RandomRemoval{fraction=" + fraction + "}"; }
}
