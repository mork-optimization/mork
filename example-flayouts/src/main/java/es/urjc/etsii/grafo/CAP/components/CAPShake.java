package es.urjc.etsii.grafo.CAP.components;

import es.urjc.etsii.grafo.CAP.model.CAPInstance;
import es.urjc.etsii.grafo.CAP.model.CAPSolution;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.CategoricalParam;
import es.urjc.etsii.grafo.shake.Shake;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.Set;

public class CAPShake extends Shake<CAPSolution, CAPInstance> {

    private static final Set<Integer> validShakes = Set.of(1, 2, 5, 6, 7, 10);
    private final int type;

    @AutoconfigConstructor
    public CAPShake(@CategoricalParam(strings = {"1", "2", "5", "6", "7", "10"}) String type) {
        this.type = Integer.parseInt(type);
        if (!validShakes.contains(this.type)) {
            throw new IllegalArgumentException(String.format("Valid Shakes: %s, given %s", validShakes, type));
        }
    }

    @Override
    public CAPSolution shake(CAPSolution solution, int k) {
        switch (this.type){
            case 1 -> solution.Shake1(k,1);
            case 2 -> solution.Shake2(k,1);
            case 5 -> solution.Shake5(k,1);
            case 6 -> solution.Shake1(k,2);
            case 7 -> solution.Shake2(k,2);
            case 10 -> solution.Shake5(k,2);
            default -> throw new IllegalArgumentException("Unknown constructive type: " + type);
        }
        solution.Evaluate();
        assert DoubleComparator.equals(solution.getScore(), solution.recalculateScore());
        solution.notifyUpdate();
        return solution;
    }

    @Override
    public String toString() {
        return "Shake{" +
                "type='" + type + '\'' +
                '}';
    }
}
