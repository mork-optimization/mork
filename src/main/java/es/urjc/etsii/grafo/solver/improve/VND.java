package es.urjc.etsii.grafo.solver.improve;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.List;

public class VND<S extends Solution<I>,I extends Instance> {

    public S doIt(S current,List<Improver<S,I>> improvers) {
        int currentLS = 0;
        while(currentLS < improvers.size()){
            double prev = current.getOptimalValue();
            var ls = improvers.get(currentLS);
            current = ls.improve(current);
            if (DoubleComparator.isLessOrEquals(prev, current.getOptimalValue())) {
                currentLS++;
            } else {
                currentLS = 0;
            }
        }
        return current;
    }
}
