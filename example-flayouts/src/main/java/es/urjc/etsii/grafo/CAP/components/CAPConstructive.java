package es.urjc.etsii.grafo.CAP.components;

import es.urjc.etsii.grafo.CAP.model.CAPInstance;
import es.urjc.etsii.grafo.CAP.model.CAPSolution;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.CategoricalParam;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.create.Constructive;

public class CAPConstructive extends Constructive<CAPSolution, CAPInstance> {

    private final String type;
    private final double alpha;

    @AutoconfigConstructor
    public CAPConstructive(
            @CategoricalParam(strings = {"random", "greedyB1", "greedyB2"}) String type,
            @RealParam(min = 0, max = 1) double alpha
    ) {
        this.type = type;
        this.alpha = alpha;
    }

    @Override
    public CAPSolution construct(CAPSolution solution) {
        switch (this.type){
            case "random" -> solution.ConstructRandom(1);
            case "greedyB1" -> solution.ConstructGreedyB(1, this.alpha);
            case "greedyB2" -> solution.ConstructGreedyB(2, this.alpha);
            default -> throw new IllegalArgumentException("Unknown constructive type: " + type);
        }
        solution.Evaluate();
        solution.notifyUpdate();
        return solution;
    }

    @Override
    public String toString() {
        return "Const{" +
                "type='" + type + '\'' +
                '}';
    }
}
