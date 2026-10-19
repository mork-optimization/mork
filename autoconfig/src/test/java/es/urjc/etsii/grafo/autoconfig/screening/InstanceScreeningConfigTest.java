package es.urjc.etsii.grafo.autoconfig.screening;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InstanceScreeningConfigTest {
    @Test void defaultsImplementTheScreeningCriterion() {
        var c = new InstanceScreeningConfig(null, null, null, null, null, null, null, null, null, null, null);
        assertEquals(10_000_000, c.thresholdNanos());
        assertEquals(30_000_000, c.candidateNanos());
        assertEquals(1_000_000_000L, c.horizonNanos());
        assertEquals(30, c.confirmationRuns()); assertEquals(.95, c.fastFraction());
    }
    @Test void defaultHorizonCoversTheCandidateMarginRoundedUpToMillis() {
        var c = new InstanceScreeningConfig(null, null, 400.1, 3.0, null, null, null, null, null, null, null);
        assertEquals(1_201, c.horizonMillis());
        assertTrue(c.horizonNanos() >= c.candidateNanos());
    }
    @Test void explicitLongerObservationWindowsRemainConfigurable() {
        var c = new InstanceScreeningConfig(null, null, null, null, 10_000L, null, null, null, null, null, null);
        assertEquals(10_000, c.horizonMillis());
    }
    @Test void rejectsUnmeasurableOrNonfiniteThresholds() {
        for (double threshold : new double[]{-1, 0, 0.0000001, Double.NaN, Double.POSITIVE_INFINITY, Double.MAX_VALUE}) {
            var error = assertThrows(IllegalArgumentException.class, () -> new InstanceScreeningConfig(null, null, threshold, null, null, null, null, null, null, null, null));
            assertTrue(error.getMessage().contains("instance-screening.threshold-millis"));
            assertTrue(error.getMessage().contains("nanoseconds"));
        }
    }
    @Test void identifiesInvalidCandidateMultipliers() {
        for (double multiplier : new double[]{0, .99, Double.NaN, Double.POSITIVE_INFINITY}) {
            var error = assertThrows(IllegalArgumentException.class, () -> new InstanceScreeningConfig(null, null, null, multiplier, null, null, null, null, null, null, null));
            assertTrue(error.getMessage().contains("instance-screening.candidate-multiplier"));
            assertTrue(error.getMessage().contains("finite and at least 1"));
        }
    }
    @Test void identifiesUnrepresentableHorizons() {
        for (long horizon : new long[]{-1, 0, 999, Long.MAX_VALUE}) {
            var error = assertThrows(IllegalArgumentException.class, () -> new InstanceScreeningConfig(null, null, null, null, horizon, null, null, null, null, null, null));
            assertTrue(error.getMessage().contains("instance-screening.horizon-millis"));
            assertTrue(error.getMessage().contains("at least 1000 ms and fit in a long number of nanoseconds"));
        }
    }
    @Test void explainsWhenTheHorizonDoesNotCoverTheCandidateWindow() {
        var error = assertThrows(IllegalArgumentException.class, () -> new InstanceScreeningConfig(null, null, 500.0, 3.0, 1_000L, null, null, null, 1, 1L, null));
        assertTrue(error.getMessage().contains("instance-screening.horizon-millis"));
        assertTrue(error.getMessage().contains("candidate window"));
        assertTrue(error.getMessage().contains("1000 ms but requires at least 1500.0 ms"));
    }
    @Test void identifiesInvalidConfirmationCounts() {
        for (int runs : new int[]{-1, 0, 10_001}) {
            var error = assertThrows(IllegalArgumentException.class, () -> new InstanceScreeningConfig(null, null, null, null, null, runs, null, null, null, null, null));
            assertTrue(error.getMessage().contains("instance-screening.confirmation-runs"));
            assertTrue(error.getMessage().contains("between 1 and 10000 inclusive"));
        }
    }
    @Test void identifiesInvalidFastFractions() {
        for (double fraction : new double[]{-.01, 1, Double.NaN, Double.POSITIVE_INFINITY}) {
            var error = assertThrows(IllegalArgumentException.class, () -> new InstanceScreeningConfig(null, null, null, null, null, null, fraction, null, null, null, null));
            assertTrue(error.getMessage().contains("instance-screening.fast-fraction"));
            assertTrue(error.getMessage().contains("finite and in [0, 1)"));
        }
    }
    @Test void identifiesInvalidWarmupCounts() {
        for (int repetitions : new int[]{-1, 0}) {
            var error = assertThrows(IllegalArgumentException.class, () -> new InstanceScreeningConfig(null, null, null, null, null, null, null, null, repetitions, null, null));
            assertTrue(error.getMessage().contains("instance-screening.warmup-repetitions"));
            assertTrue(error.getMessage().contains("at least 1"));
        }
    }
    @Test void identifiesWarmupDurationsOutsideTheHorizon() {
        for (long millis : new long[]{-1, 0, 1_001}) {
            var error = assertThrows(IllegalArgumentException.class, () -> new InstanceScreeningConfig(null, null, null, null, 1_000L, null, null, null, 1, millis, null));
            assertTrue(error.getMessage().contains("instance-screening.warmup-millis"));
            assertTrue(error.getMessage().contains("between 1 and horizon-millis (1000) inclusive"));
        }
    }
}
