package es.urjc.etsii.grafo.metrics;

import es.urjc.etsii.grafo.algorithms.FMode;

public class BestObjective extends AbstractMetric {
    private static final String NAME = BestObjective.class.getSimpleName();
    private double bestKnownValue;

    private final FMode fMode;

    public BestObjective() {
        this.fMode = MetricsManager.getFMode();
    }

    @Override
    public void addDatapoint(double value, long absoluteTime) {
        if(fMode.isBetter(value, bestKnownValue)){
            bestKnownValue = value;
            super.addDatapoint(value, absoluteTime);
        }
    }

    public static void add(double value) {
        BestObjective.add(value, System.nanoTime());
    }
    public static void add(double value, long absoluteTime) {
        if(!MetricsManager.areMetricsEnabled()) return;
        var metrics = MetricsManager.getInstance();
        metrics.addDatapoint(NAME, BestObjective::new, value, absoluteTime);
    }
}
