package es.urjc.etsii.grafo.flayouts.constructives.grasp;

import es.urjc.etsii.grafo.drflp.model.FLPSolution;

import java.util.Arrays;

public class DRFPFakeFacilitiesBySwapListManager extends DRFPBySwapListManager {
    private final double[] widths;

    public DRFPFakeFacilitiesBySwapListManager(double[] widths) {
        this.widths = widths;
    }

    @Override
    public String toString() {
        return "FakeFacBySwapLM{wID=" + Arrays.hashCode(widths) +  "}";
    }

    @Override
    public void beforeGRASP(FLPSolution solution) {
        super.beforeGRASP(solution);
        solution.addFakeFacilities(widths);
    }
}
