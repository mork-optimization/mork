package es.urjc.etsii.grafo.autoconfig.screening;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.metrics.TimeValue;
import java.util.List;
import static es.urjc.etsii.grafo.autoconfig.screening.ScreeningData.*;

/** Pure classification; a poor but fast local optimum is not an early hit on a better common target. */
public final class ScreeningUtil {
    private ScreeningUtil() {}
    /** Nearest-rank summaries of confirmations that attained the common target. Unreached targets are omitted. */
    public record TtbSummary(int attained, Long medianNanos, Long p95Nanos, Long maxNanos) {}
    public static TtbSummary summarize(List<Run> confirmations, Double target, FMode mode, long horizon) {
        var times = new java.util.ArrayList<Long>();
        if (target != null) for (Run run : confirmations) {
            Long time = firstAttainment(run, target, mode, horizon);
            if (time != null) times.add(time);
        }
        times.sort(Long::compare);
        if (times.isEmpty()) return new TtbSummary(0, null, null, null);
        return new TtbSummary(times.size(), times.get((int) Math.ceil(times.size() * .5) - 1),
                times.get((int) Math.ceil(times.size() * .95) - 1), times.getLast());
    }
    private static boolean equal(FMode mode, double a, double b) {
        return mode.isBetterOrEqual(a, b) && mode.isBetterOrEqual(b, a);
    }
    public static Long firstAttainment(Run run, double target, FMode mode, long horizon) {
        if (!run.success() || run.objective() == null || !Double.isFinite(run.objective())
                || run.runtimeNanos() < horizon || run.curve() == null || run.curve().isEmpty()) return null;
        long previousTime = -1;
        Double previousValue = null;
        Long first = null;
        for (TimeValue point : run.curve()) {
            if (point.instant() < 0 || point.instant() < previousTime || point.instant() > run.runtimeNanos()
                    || !Double.isFinite(point.value())
                    || previousValue != null && mode.isBetter(previousValue, point.value())) return null;
            previousTime = point.instant(); previousValue = point.value();
            if (point.instant() <= horizon && equal(mode, point.value(), target) && first == null) first = point.instant();
        }
        if (!equal(mode, previousValue, run.objective())) return null;
        return first;
    }
    public static boolean candidate(Run initial, InstanceScreeningConfig config, FMode mode) {
        if (initial.objective() == null) return false;
        Long ttb = firstAttainment(initial, initial.objective(), mode, config.horizonNanos());
        return ttb != null && ttb <= config.candidateNanos();
    }
    public static Decision classify(Run initial, List<Run> confirmations, InstanceScreeningConfig config, FMode mode) {
        Long initialTtb = initial.objective() == null ? null : firstAttainment(initial, initial.objective(), mode, config.horizonNanos());
        if (initialTtb == null) return new Decision(initial.instance(), "INCONCLUSIVE", initial.objective(), null,
                0, 0, 1, "Initial run failed or has invalid timing/curve evidence: " + initial.failure());
        if (initialTtb > config.candidateNanos()) return new Decision(initial.instance(), "RETAINED", initial.objective(),
                initialTtb, 0, 0, 0, "Initial TTB exceeds the candidate boundary");
        double target = initial.objective();
        for (Run run : confirmations) if (run.success() && run.objective() != null && Double.isFinite(run.objective())) {
            target = mode.best(target, run.objective());
        }
        int hits = 0, failures = 0;
        var seeds = new java.util.HashSet<Long>();
        for (Run run : confirmations) {
            if (!run.instance().equals(initial.instance()) || !run.fingerprint().equals(initial.fingerprint())
                    || run.seed() == initial.seed() || !seeds.add(run.seed())) { failures++; continue; }
            Long ownTtb = run.objective() == null ? null : firstAttainment(run, run.objective(), mode, config.horizonNanos());
            if (ownTtb == null) { failures++; continue; }
            Long targetTtb = firstAttainment(run, target, mode, config.horizonNanos());
            if (targetTtb != null && targetTtb < config.thresholdNanos()) hits++;
        }
        if (confirmations.size() != config.confirmationRuns() || failures != 0) {
            return new Decision(initial.instance(), "INCONCLUSIVE", target, initialTtb, confirmations.size(), hits, failures,
                    "Confirmation runs are incomplete or contain invalid evidence");
        }
        boolean trivial = (double) hits / config.confirmationRuns() > config.fastFraction();
        return new Decision(initial.instance(), trivial ? "TRIVIAL" : "RETAINED", target, initialTtb,
                confirmations.size(), hits, 0, trivial ? "Fast common-target fraction exceeds threshold" : "Fast common-target fraction does not exceed threshold");
    }
}
