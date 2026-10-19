package es.urjc.etsii.grafo.autoconfig.screening;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Explicit preparation settings; durations are milliseconds. */
@ConfigurationProperties("instance-screening")
public record InstanceScreeningConfig(String sotaFile, String output, Double thresholdMillis,
        Double candidateMultiplier, Long horizonMillis, Integer confirmationRuns, Double fastFraction,
        String python, Integer warmupRepetitions, Long warmupMillis, String jobFile) {
    public InstanceScreeningConfig {
        output = output == null ? "instance-screening" : output;
        thresholdMillis = thresholdMillis == null ? 10.0 : thresholdMillis;
        candidateMultiplier = candidateMultiplier == null ? 3.0 : candidateMultiplier;
        confirmationRuns = confirmationRuns == null ? 30 : confirmationRuns;
        fastFraction = fastFraction == null ? 0.95 : fastFraction;
        python = python == null ? "python3" : python;
        warmupRepetitions = warmupRepetitions == null ? 5 : warmupRepetitions;
        warmupMillis = warmupMillis == null ? 1_000L : warmupMillis;
        if (!Double.isFinite(thresholdMillis) || thresholdMillis * 1e6 < 1 || thresholdMillis * 1e6 > Long.MAX_VALUE) {
            throw new IllegalArgumentException("instance-screening.threshold-millis must be finite, represent at least 1 nanosecond and fit in a long number of nanoseconds; got " + thresholdMillis);
        }
        if (!Double.isFinite(candidateMultiplier) || candidateMultiplier < 1) {
            throw new IllegalArgumentException("instance-screening.candidate-multiplier must be finite and at least 1; got " + candidateMultiplier);
        }
        horizonMillis = horizonMillis == null ? Math.max(1_000L, (long) Math.ceil(thresholdMillis * candidateMultiplier)) : horizonMillis;
        if (horizonMillis < 1_000 || horizonMillis > Long.MAX_VALUE / 1_000_000) {
            throw new IllegalArgumentException("instance-screening.horizon-millis must be at least 1000 ms and fit in a long number of nanoseconds; got " + horizonMillis);
        }
        if (thresholdMillis * candidateMultiplier > horizonMillis) {
            throw new IllegalArgumentException("instance-screening.horizon-millis must cover the candidate window (threshold-millis * candidate-multiplier); got "
                    + horizonMillis + " ms but requires at least " + thresholdMillis * candidateMultiplier + " ms");
        }
        if (confirmationRuns < 1 || confirmationRuns > 10_000) {
            throw new IllegalArgumentException("instance-screening.confirmation-runs must be between 1 and 10000 inclusive; got " + confirmationRuns);
        }
        if (!Double.isFinite(fastFraction) || fastFraction < 0 || fastFraction >= 1) {
            throw new IllegalArgumentException("instance-screening.fast-fraction must be finite and in [0, 1); got " + fastFraction);
        }
        if (warmupRepetitions < 1) {
            throw new IllegalArgumentException("instance-screening.warmup-repetitions must be at least 1; got " + warmupRepetitions);
        }
        if (warmupMillis < 1 || warmupMillis > horizonMillis) {
            throw new IllegalArgumentException("instance-screening.warmup-millis must be between 1 and horizon-millis (" + horizonMillis + ") inclusive; got " + warmupMillis);
        }
    }
    public long thresholdNanos() { return Math.round(thresholdMillis * 1_000_000); }
    public long candidateNanos() { return Math.round(thresholdMillis * candidateMultiplier * 1_000_000); }
    public long horizonNanos() { return horizonMillis * 1_000_000; }
}
