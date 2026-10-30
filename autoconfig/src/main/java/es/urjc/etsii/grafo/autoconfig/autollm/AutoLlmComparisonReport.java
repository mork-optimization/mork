package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.algorithms.FMode;
import java.util.*;

/** Aggregates journal measurements without loading curve arrays or exported workbooks. */
public final class AutoLlmComparisonReport {
    private AutoLlmComparisonReport() {}
    public enum Ranking { NONE, COMMON_SOTA }
    public record Row(String instanceId, String instance, AutoLlmComparisonUtil.Measured sota,
                      AutoLlmComparisonUtil.Measured proposal, double difference, Double improvementPercent, String outcome) {}
    public record Run(int trial, String targetId, int repetitions, String journal, List<Row> instances,
                      int wins, int ties, int losses, int undefinedPercentages, Double meanImprovementPercent) {}
    public record Ranked(int trial, double meanImprovementPercent) {}

    public static Run run(int trial, String target, int repetitions, int expectedInstances, String journal,
                          List<AutoLlmComparisonUtil.MeasuredInstance> instances, FMode mode) {
        if (instances.size() != expectedInstances) throw new IllegalStateException("Instance count differs from the approved testing set");
        var rows = new ArrayList<Row>(); int wins = 0, ties = 0, losses = 0, undefined = 0; double sum = 0;
        for (var instance : instances) {
            var sota = instance.algorithms().get("SOTA"); var proposal = instance.algorithms().get("IraceProposal");
            if (sota == null || proposal == null || sota.objective().count() != repetitions || proposal.objective().count() != repetitions)
                throw new IllegalStateException("Missing objective or repetitions for " + instance.instance());
            for (var algorithm : instance.counts().values()) for (var count : algorithm.entrySet())
                if (!count.getKey().equals("SUCCEEDED") && count.getValue() > 0) throw new IllegalStateException("Failed evaluations for " + instance.instance());
            double reference = sota.objective().mean(), candidate = proposal.objective().mean();
            double gain = mode == FMode.MINIMIZE ? reference - candidate : candidate - reference;
            boolean tie = Math.abs(gain) <= 1e-9 * Math.max(1, Math.max(Math.abs(reference), Math.abs(candidate)));
            String outcome = tie ? "TIE" : gain > 0 ? "WIN" : "LOSS";
            if (tie) ties++; else if (gain > 0) wins++; else losses++;
            Double percent = reference == 0 ? null : 100 * gain / Math.abs(reference);
            if (percent == null) undefined++; else sum += percent;
            rows.add(new Row(instance.instanceId(), instance.instance(), sota, proposal, candidate - reference, percent, outcome));
        }
        return new Run(trial, target, repetitions, journal, rows, wins, ties, losses, undefined,
                undefined > 0 || rows.isEmpty() ? null : sum / rows.size());
    }

    public static List<Ranked> rank(List<Run> runs, FMode mode) {
        if (runs.isEmpty()) return List.of();
        var references = new TreeMap<String,Double>(); var expected = new TreeSet<String>();
        for (var row : runs.getFirst().instances()) expected.add(row.instanceId());
        for (var run : runs) {
            var identities = new TreeSet<String>();
            for (var row : run.instances()) {
                identities.add(row.instanceId()); references.merge(row.instanceId(), row.sota().objective().mean() / runs.size(), Double::sum);
            }
            if (!identities.equals(expected)) throw new IllegalStateException("Cross-run ranking requires identical instance identities");
        }
        var result = new ArrayList<Ranked>();
        for (var run : runs) {
            double sum = 0;
            for (var row : run.instances()) {
                double reference = references.get(row.instanceId());
                if (reference <= 0) throw new IllegalStateException("Common-reference ranking requires positive SOTA references; use host-matched absolute differences for zero/negative objectives");
                double gain = mode == FMode.MINIMIZE ? reference - row.proposal().objective().mean() : row.proposal().objective().mean() - reference;
                sum += 100 * gain / reference;
            }
            result.add(new Ranked(run.trial(), sum / run.instances().size()));
        }
        result.sort(Comparator.comparingDouble(Ranked::meanImprovementPercent).reversed().thenComparingInt(Ranked::trial));
        return result;
    }
}
