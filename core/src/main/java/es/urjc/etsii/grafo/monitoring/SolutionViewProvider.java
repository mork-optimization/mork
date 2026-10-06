package es.urjc.etsii.grafo.monitoring;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import java.util.Map;

/** Optional problem-specific JSON view, captured before solutions are released. No HTML or Java object graphs. */
public interface SolutionViewProvider<S extends Solution<S, I>, I extends Instance> {
    String rendererId();
    Map<String, Object> describe(S solution);
}
