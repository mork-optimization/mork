package es.urjc.etsii.grafo.flayouts.shake;

import es.urjc.etsii.grafo.flayouts.model.*;
import es.urjc.etsii.grafo.util.random.RandomManager;
import java.util.ArrayList;
import java.util.List;

public final class FLPDestructionUtil {
    private FLPDestructionUtil() {}

    public static List<Integer> assigned(FLPSolution solution) {
        var result = new ArrayList<Integer>();
        for (int[] row : FLPNewUtil.rows(solution)) for (int f : row) result.add(f);
        return result;
    }

    /** Noise is the fraction of the ranked list eligible for random selection; zero selects the best. */
    public static int removeRank(List<Integer> ranked, double noise) {
        int bound = Math.max(1, (int) Math.ceil(noise * ranked.size()));
        return ranked.remove(RandomManager.getRandom().nextInt(bound));
    }

    public static FLPSolution destroy(FLPSolution original, List<Integer> removed) {
        var solution = original.cloneSolution();
        if (!removed.isEmpty()) {
            int[] ids = new int[removed.size()];
            for (int i = 0; i < ids.length; i++) ids[i] = removed.get(i);
            FLPNewUtil.remove(solution, ids).execute(solution);
        }
        return solution;
    }
}
