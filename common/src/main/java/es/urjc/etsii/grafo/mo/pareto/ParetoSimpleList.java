package es.urjc.etsii.grafo.mo.pareto;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ParetoSimpleList<S extends Solution<S,I>, I extends Instance> extends ParetoSet<S, I> {

    private List<double[]> values = new ArrayList<>();

    public ParetoSimpleList(int nObjectives) {
        super(nObjectives);
    }

    @Override
    public synchronized void clear() {
        super.clear();
        values.clear();
    }

    @Override
    public synchronized boolean add(double[] solution){
        // Reserve size for the worst case directly
        var dominated = new ArrayList<Integer>(this.values.size());
        boolean addToPareto = true;
        int idx = 0;
        for(var frontSolution: this.values){
            boolean bestInAll = true;
            boolean worstInAll = true;
            for(int i = 0; i < frontSolution.length; i++){
                int comp = DoubleComparator.comparator(solution[i], frontSolution[i]);
                if (comp < 0) {
                    worstInAll = false;
                } else if (comp > 0)  {
                    bestInAll = false;
                }
            }
            if (worstInAll) {
                addToPareto = false;
                break;
            }
            if (bestInAll) {
                dominated.add(idx);
            }
            idx++;
        }
        // If the solution is not added to the pareto front, the dominated set must be empty or we have a serious bug
        assert addToPareto || dominated.isEmpty();

        // remove dominated solutions in reverse order to cancel index shifting
        for(int i = dominated.size()-1; i >= 0; i--){
            var ejectedSolution = this.values.remove((int)dominated.get(i));
            super.ejectedSolution(ejectedSolution);
        }
        if(addToPareto){
            this.values.add(solution);
        }
        return addToPareto;
    }

    @Override
    public synchronized int size() {
        return values.size();
    }

    @Override
    public synchronized Stream<double[]> stream() {
        return values.stream();
    }
}
