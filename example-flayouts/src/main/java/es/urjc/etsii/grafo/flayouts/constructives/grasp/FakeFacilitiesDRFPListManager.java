package es.urjc.etsii.grafo.flayouts.constructives.grasp;

import es.urjc.etsii.grafo.drflp.model.FLPSolution;

import java.util.Arrays;

public class FakeFacilitiesDRFPListManager extends DRFPListManager {

    private final double[] widths;

    public FakeFacilitiesDRFPListManager(double[] widths) {
        this.widths = widths;
    }

    @Override
    public String toString() {
        return "FakeFacLM{wID=" + Arrays.hashCode(widths) +  "}";
    }

    @Override
    public void beforeGRASP(FLPSolution solution) {
        super.beforeGRASP(solution);
        solution.addFakeFacilities(widths);
    }
}
