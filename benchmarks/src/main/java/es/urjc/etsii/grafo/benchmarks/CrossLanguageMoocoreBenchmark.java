// SPDX-License-Identifier: MIT
package es.urjc.etsii.grafo.benchmarks;

import es.urjc.etsii.grafo.moocore.HypervolumeApproximation;
import es.urjc.etsii.grafo.moocore.MooCore;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class CrossLanguageMoocoreBenchmark {

    private static final double RELATIVE_TOLERANCE = 1e-9;
    private static final double ABSOLUTE_TOLERANCE = 1e-12;

    @Benchmark
    public boolean[] nondominated(NondominatedState state) {
        return MooCore.isNondominated(
                state.points, new boolean[]{true}, state.keepWeakly);
    }

    @Benchmark
    public int[] paretoRank(ParetoRankState state) {
        return MooCore.paretoRank(state.points);
    }

    @Benchmark
    public double hypervolume(HypervolumeState state) {
        return MooCore.hypervolume(state.points, state.reference);
    }

    @Benchmark
    public double approximateHypervolume(HypervolumeApproximationState state) {
        return MooCore.approximateHypervolume(
                state.points, state.reference, new boolean[]{false},
                state.samples, state.seed, state.method);
    }

    @Benchmark
    public double epsilonAdditive(EpsilonState state) {
        return MooCore.epsilonAdditive(state.points, state.reference);
    }

    @Benchmark
    public double igdPlus(IgdPlusState state) {
        return MooCore.igdPlus(state.points, state.reference);
    }

    public abstract static class CaseState {

        protected String caseId;
        protected Properties properties;
        protected double[][] points;

        protected void loadCase(String selectedCaseId) {
            caseId = selectedCaseId;
            String caseDirectory = caseDirectory();
            if (caseDirectory == null || caseDirectory.isBlank()) {
                throw new IllegalStateException(
                        "Environment variable MOOCORE_CROSS_LANGUAGE_CASES is required");
            }
            Path directory = Path.of(caseDirectory).toAbsolutePath().normalize();
            Path propertyFile = directory.resolve(caseId + ".properties");
            properties = new Properties();
            try (var input = Files.newInputStream(propertyFile)) {
                properties.load(input);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot load benchmark case " + propertyFile, e);
            }
            points = rows(readMatrix(resolve(directory, "points")), integer("size"));
        }

        protected Path resolve(Path directory, String key) {
            String value = properties.getProperty(key);
            if (value == null || value.isBlank()) {
                throw new IllegalStateException("Missing property " + key + " for " + caseId);
            }
            return directory.resolve(value).normalize();
        }

        protected int integer(String key) {
            return Integer.parseInt(properties.getProperty(key));
        }

        protected long longValue(String key) {
            return Long.parseLong(properties.getProperty(key));
        }

        protected double[][] matrix(String key) {
            String caseDirectory = caseDirectory();
            return readMatrix(resolve(Path.of(caseDirectory).toAbsolutePath().normalize(), key));
        }

        private static String caseDirectory() {
            String directory = System.getProperty("moocore.crossLanguage.cases");
            if (directory == null || directory.isBlank()) {
                directory = System.getenv("MOOCORE_CROSS_LANGUAGE_CASES");
            }
            return directory;
        }

        protected double scalarOracle() {
            double[][] oracle = matrix("oracle");
            if (oracle.length != 1 || oracle[0].length != 1) {
                throw new IllegalStateException("Scalar oracle expected for " + caseId);
            }
            return oracle[0][0];
        }
    }

    @State(Scope.Benchmark)
    public static class NondominatedState extends CaseState {

        @Param("unset")
        public String selectedCaseId;

        boolean keepWeakly;

        @Setup(Level.Trial)
        public void setup() {
            loadCase(selectedCaseId);
            keepWeakly = Boolean.parseBoolean(properties.getProperty("keepWeakly"));
            boolean[] actual = MooCore.isNondominated(
                    points, new boolean[]{true}, keepWeakly);
            double[][] oracle = matrix("oracle");
            if (oracle.length != actual.length) {
                throw new IllegalStateException("Oracle length mismatch for " + caseId);
            }
            for (int i = 0; i < actual.length; i++) {
                if (actual[i] != (oracle[i][0] != 0.0)) {
                    throw new IllegalStateException(
                            "Nondominated result differs from Python at index " + i
                                    + " for " + caseId);
                }
            }
        }
    }

    @State(Scope.Benchmark)
    public static class ParetoRankState extends CaseState {

        @Param("unset")
        public String selectedCaseId;

        @Setup(Level.Trial)
        public void setup() {
            loadCase(selectedCaseId);
            int[] actual = MooCore.paretoRank(points);
            double[][] oracle = matrix("oracle");
            if (oracle.length != actual.length) {
                throw new IllegalStateException("Oracle length mismatch for " + caseId);
            }
            for (int i = 0; i < actual.length; i++) {
                if (actual[i] != (int) oracle[i][0]) {
                    throw new IllegalStateException(
                            "Pareto rank differs from Python at index " + i
                                    + " for " + caseId);
                }
            }
        }
    }

    public abstract static class ReferencedState extends CaseState {

        protected double[][] referenceMatrix;

        protected void loadReferencedCase(String selectedCaseId) {
            loadCase(selectedCaseId);
            referenceMatrix = matrix("reference");
        }
    }

    @State(Scope.Benchmark)
    public static class HypervolumeState extends ReferencedState {

        @Param("unset")
        public String selectedCaseId;

        double[] reference;

        @Setup(Level.Trial)
        public void setup() {
            loadReferencedCase(selectedCaseId);
            reference = vector(referenceMatrix, caseId);
            assertClose(scalarOracle(), MooCore.hypervolume(points, reference), caseId);
        }
    }

    @State(Scope.Benchmark)
    public static class HypervolumeApproximationState extends ReferencedState {

        @Param("unset")
        public String selectedCaseId;

        double[] reference;
        long samples;
        long seed;
        HypervolumeApproximation method;

        @Setup(Level.Trial)
        public void setup() {
            loadReferencedCase(selectedCaseId);
            reference = vector(referenceMatrix, caseId);
            samples = longValue("samples");
            seed = longValue("seed");
            method = HypervolumeApproximation.valueOf(properties.getProperty("method"));
            double actual = MooCore.approximateHypervolume(
                    points, reference, new boolean[]{false}, samples, seed, method);
            double expected = scalarOracle();
            if (method == HypervolumeApproximation.DZ2019_MC) {
                if (!Double.isFinite(actual) || relativeError(expected, actual) > 0.05) {
                    throw new IllegalStateException(
                            "Monte Carlo approximation is not within 5% of exact HV for "
                                    + caseId + ": exact=" + expected + ", actual=" + actual);
                }
            } else {
                assertClose(expected, actual, caseId);
            }
        }
    }

    @State(Scope.Benchmark)
    public static class EpsilonState extends ReferencedState {

        @Param("unset")
        public String selectedCaseId;

        double[][] reference;

        @Setup(Level.Trial)
        public void setup() {
            loadReferencedCase(selectedCaseId);
            reference = referenceMatrix;
            assertClose(scalarOracle(), MooCore.epsilonAdditive(points, reference), caseId);
        }
    }

    @State(Scope.Benchmark)
    public static class IgdPlusState extends ReferencedState {

        @Param("unset")
        public String selectedCaseId;

        double[][] reference;

        @Setup(Level.Trial)
        public void setup() {
            loadReferencedCase(selectedCaseId);
            reference = referenceMatrix;
            assertClose(scalarOracle(), MooCore.igdPlus(points, reference), caseId);
        }
    }

    private static double[] vector(double[][] matrix, String caseId) {
        if (matrix.length != 1) {
            throw new IllegalStateException("Vector expected for " + caseId);
        }
        return matrix[0];
    }

    private static double[][] rows(double[][] matrix, int size) {
        if (size < 0 || size > matrix.length) {
            throw new IllegalStateException(
                    "Requested " + size + " rows from matrix with " + matrix.length);
        }
        return Arrays.copyOf(matrix, size);
    }

    private static double relativeError(double expected, double actual) {
        if (expected == 0.0) {
            return Math.abs(actual);
        }
        return Math.abs(expected - actual) / Math.abs(expected);
    }

    private static void assertClose(double expected, double actual, String caseId) {
        double tolerance = ABSOLUTE_TOLERANCE
                + RELATIVE_TOLERANCE * Math.abs(expected);
        if (!Double.isFinite(actual) || Math.abs(expected - actual) > tolerance) {
            throw new IllegalStateException(
                    "Result differs from Python for " + caseId + ": expected="
                            + expected + ", actual=" + actual + ", tolerance=" + tolerance);
        }
    }

    private static double[][] readMatrix(Path path) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(path);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read matrix " + path, e);
        }
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        if (buffer.remaining() < 12
                || buffer.get() != 'M'
                || buffer.get() != 'O'
                || buffer.get() != 'O'
                || buffer.get() != 'C') {
            throw new IllegalStateException("Invalid matrix header in " + path);
        }
        int rows = buffer.getInt();
        int columns = buffer.getInt();
        long expectedBytes = 12L + (long) rows * columns * Double.BYTES;
        if (rows < 0 || columns < 0 || expectedBytes != bytes.length) {
            throw new IllegalStateException("Invalid matrix dimensions in " + path);
        }
        double[][] matrix = new double[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                matrix[row][column] = buffer.getDouble();
            }
        }
        return matrix;
    }
}
