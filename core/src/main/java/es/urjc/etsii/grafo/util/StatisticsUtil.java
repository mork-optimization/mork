package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.algorithms.FMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Finite-sample summaries. Missing values are never substituted with zero. */
public final class StatisticsUtil {
    private StatisticsUtil() {}

    public record Summary(int count, Double best, Double mean, Double median, Double standardDeviation,
                          Double lowerQuartile, Double upperQuartile, Double worst,
                          Double absoluteGap, Double percentageGap) {}

    public static Summary summarize(List<Double> values, FMode mode, Double reference) {
        var accumulator = new Accumulator();
        for (var value : values) accumulator.add(value);
        return accumulator.summary(mode, reference);
    }

    /** Constant-space live statistics. Exact quantiles remain available in completed reports. */
    public static final class OnlineAccumulator {
        private int count;
        private double mean, m2, minimum = Double.POSITIVE_INFINITY, maximum = Double.NEGATIVE_INFINITY;
        public void add(double value) {
            if (!Double.isFinite(value)) return;
            count++; double delta = value - mean; mean += delta / count; m2 += delta * (value - mean);
            minimum = Math.min(minimum, value); maximum = Math.max(maximum, value);
        }
        public Summary summary(FMode mode) {
            if (count == 0) return new Summary(0, null, null, null, null, null, null, null, null, null);
            return new Summary(count, mode == FMode.MAXIMIZE ? maximum : minimum, mean, null,
                    count < 2 ? null : Math.sqrt(m2 / (count - 1)), null, null,
                    mode == FMode.MAXIMIZE ? minimum : maximum, null, null);
        }
    }

    /** Constant-time updates; sorted quantiles are computed only when a new summary is requested. */
    public static final class Accumulator {
        private final List<Double> values = new ArrayList<>();
        private double mean, m2;
        public void add(Double value) {
            if (value == null || !Double.isFinite(value)) return;
            values.add(value);
            double delta = value - mean; mean += delta / values.size(); m2 += delta * (value - mean);
        }
        public Summary summary(FMode mode, Double reference) {
            var sorted = new ArrayList<>(values);
            if (sorted.isEmpty()) return new Summary(0, null, null, null, null, null, null, null, null, null);
            Collections.sort(sorted);
            double best = mode == FMode.MAXIMIZE ? sorted.getLast() : sorted.getFirst();
            double worst = mode == FMode.MAXIMIZE ? sorted.getFirst() : sorted.getLast();
            Double gap = reference == null || !Double.isFinite(reference) ? null
                    : (mode == FMode.MAXIMIZE ? reference - best : best - reference);
            Double percent = gap == null || reference == 0 ? null : 100 * gap / Math.abs(reference);
            return new Summary(sorted.size(), best, mean, quantile(sorted, .5),
                    sorted.size() < 2 ? null : Math.sqrt(m2 / (sorted.size() - 1)),
                    quantile(sorted, .25), quantile(sorted, .75), worst, gap, percent);
        }
    }

    private static double quantile(List<Double> sorted, double fraction) {
        double index = fraction * (sorted.size() - 1);
        int lower = (int) index;
        int upper = Math.min(sorted.size() - 1, lower + 1);
        return sorted.get(lower) + (sorted.get(upper) - sorted.get(lower)) * (index - lower);
    }
}
