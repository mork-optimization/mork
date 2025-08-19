package es.urjc.etsii.grafo.CAP.components;

import es.urjc.etsii.grafo.CAP.model.CAPInstance;
import es.urjc.etsii.grafo.CAP.model.CAPSolution;
import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.CategoricalParam;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.TimeControl;

public class CAPLS extends Improver<CAPSolution, CAPInstance> {
    
    private final String type;

    @AutoconfigConstructor
    public CAPLS(@CategoricalParam(strings = {"exc_bi", "exc_hi", "exc_fi", "ins_f", "ins_bi", "ins_hi", "ext_fhi"}) String type) {
        super(Context.getMainObjective());
        this.type = type;
    }
    
    @Override
    public CAPSolution improve(CAPSolution solution) {
        boolean improves;
        do {
            improves = switch (this.type){
                case "exc_bi" -> solution.ExploreExchangeInc(0);
                case "exc_hi" -> solution.ExploreExchangeInc(1);
                case "exc_fi" -> solution.ExploreExchangeInc(2);
                case "ins_f" -> solution.ExploreInsert(2);
                case "ins_bi" -> solution.ExploreInsertInc(0);
                case "ins_hi" -> solution.ExploreInsertInc(1);
                case "ext_fhi" -> solution.ExploreExtendedFirstHybridInc();
                default -> throw new IllegalArgumentException("Unknown LS type: " + type);
            };
            assert DoubleComparator.equals(solution.getScore(), solution.recalculateScore());
            if(improves){
                solution.notifyUpdate();
            }
        } while (improves && !TimeControl.isTimeUp());
        return solution;
    }


    @Override
    public String toString() {
        return "LS{" +
                "type='" + type + '\'' +
                '}';
    }
}
