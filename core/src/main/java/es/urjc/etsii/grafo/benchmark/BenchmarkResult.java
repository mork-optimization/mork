package es.urjc.etsii.grafo.benchmark;

import jnt.scimark2.Constants;
import jnt.scimark2.ScimarkAPI;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/** A single SciMark measurement, with the inputs needed to validate its reuse. */
public record BenchmarkResult(int version, Protocol protocol, String measuredAt,
                              Map<String, String> environment, Scores scores, double score) {
    static final int VERSION = 1;

    public BenchmarkResult {
        Objects.requireNonNull(protocol);
        Objects.requireNonNull(scores);
        environment = Map.copyOf(environment);
        Instant.parse(measuredAt);
        if (version != VERSION || Double.compare(score, scores.composite()) != 0) {
            throw new IllegalArgumentException("Unsupported or inconsistent benchmark data");
        }
    }

    public record Protocol(String suite, int seed, double minimumSeconds,
                           int fftSize, int sorSize, int sparseSize, int sparseNonzeros, int luSize) {
        static Protocol current(long seed) {
            return new Protocol("scimark-2.2-small", (int) seed, Constants.RESOLUTION_DEFAULT,
                    Constants.FFT_SIZE, Constants.SOR_SIZE, Constants.SPARSE_SIZE_M, Constants.SPARSE_SIZE_nz, Constants.LU_SIZE);
        }
    }

    public record Scores(double fft, double sor, double monteCarlo, double sparseMatmult, double lu) {
        public Scores {
            for (double value : new double[]{fft, sor, monteCarlo, sparseMatmult, lu}) {
                if (!Double.isFinite(value) || value <= 0) throw new IllegalArgumentException("Invalid benchmark score");
            }
            if (!Double.isFinite((fft + sor + monteCarlo + sparseMatmult + lu) / 5)) {
                throw new IllegalArgumentException("Invalid composite benchmark score");
            }
        }

        public double composite() { return (fft + sor + monteCarlo + sparseMatmult + lu) / 5; }

        static Scores from(ScimarkAPI.BenchmarkResult result) {
            return new Scores(result.getFFTScore(), result.getSORScore(), result.getMonteCarloScore(),
                    result.getSparseMatmultScore(), result.getLUScore());
        }
    }

    static BenchmarkResult measured(long seed, Map<String, String> environment, Scores scores) {
        return new BenchmarkResult(VERSION, Protocol.current(seed), Instant.now().toString(), environment, scores, scores.composite());
    }

    boolean matches(long seed, Map<String, String> current) {
        return protocol.equals(Protocol.current(seed)) && BenchmarkEnvironment.identifiable(current) && environment.equals(current);
    }
}
