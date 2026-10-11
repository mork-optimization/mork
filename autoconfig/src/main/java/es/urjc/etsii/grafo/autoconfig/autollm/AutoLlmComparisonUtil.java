package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithm;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;

public final class AutoLlmComparisonUtil {
    private AutoLlmComparisonUtil() {}
    public static <S extends Solution<S,I>, I extends Instance> Algorithm<S,I> restart(String name, Algorithm<S,I> algorithm) {
        return new MultiStartAlgorithm<>(name, Context.getMainObjective(), algorithm, Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2);
    }
}
