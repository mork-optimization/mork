// SPDX-License-Identifier: MIT
package es.urjc.etsii.grafo.benchmarks;

import es.urjc.etsii.grafo.moocore.HypervolumeApproximation;
import es.urjc.etsii.grafo.moocore.Dataset;
import es.urjc.etsii.grafo.moocore.EafDifferenceFormat;
import es.urjc.etsii.grafo.moocore.HypeDistribution;
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
    public double[] hypervolumeContributions(HypervolumeContributionState state) {
        return MooCore.hypervolumeContributions(state.points, state.reference);
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

    @Benchmark
    public double exactR2(ExactR2State state) {
        return MooCore.exactR2(state.points, state.reference);
    }

    @Benchmark
    public double epsilonMultiplicative(MultiplicativeEpsilonState state) {
        return MooCore.epsilonMultiplicative(state.points, state.reference);
    }

    @Benchmark
    public double[][] eaf(EafState state) {
        return MooCore.eaf(state.points, state.sets);
    }

    @Benchmark
    public double[][] eafDifference(EafDifferenceState state) {
        return MooCore.eafDifference(state.left, state.right, null,
                new boolean[]{false}, state.format);
    }

    @Benchmark
    public double weightedHypervolume(WeightedHypervolumeState state) {
        return MooCore.weightedHypervolume(state.points, state.rectangles, state.reference);
    }

    @Benchmark
    public double hypeWeightedHypervolume(HypeState state) {
        return MooCore.hypeWeightedHypervolume(state.points, state.reference, state.ideal,
                new boolean[]{false}, state.samples, state.seed, state.distribution, state.mu);
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

        protected int[] integerVector(String key) {
            double[][] matrix = matrix(key);
            int[] result = new int[matrix.length];
            for (int row = 0; row < matrix.length; row++) {
                if (matrix[row].length != 1 || matrix[row][0] != Math.rint(matrix[row][0])) {
                    throw new IllegalStateException("Integer vector expected for " + caseId);
                }
                result[row] = (int) matrix[row][0];
            }
            return result;
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
    public static class HypervolumeContributionState extends ReferencedState {

        @Param("unset")
        public String selectedCaseId;

        double[] reference;

        @Setup(Level.Trial)
        public void setup() {
            loadReferencedCase(selectedCaseId);
            reference = vector(referenceMatrix, caseId);
            assertVectorClose(matrix("oracle"),
                    MooCore.hypervolumeContributions(points, reference), caseId);
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

    @State(Scope.Benchmark)
    public static class ExactR2State extends ReferencedState {

        @Param("unset")
        public String selectedCaseId;

        double[] reference;

        @Setup(Level.Trial)
        public void setup() {
            loadReferencedCase(selectedCaseId);
            reference = vector(referenceMatrix, caseId);
            assertClose(scalarOracle(), MooCore.exactR2(points, reference), caseId);
        }
    }

    @State(Scope.Benchmark)
    public static class MultiplicativeEpsilonState extends ReferencedState {

        @Param("unset")
        public String selectedCaseId;

        double[][] reference;

        @Setup(Level.Trial)
        public void setup() {
            loadReferencedCase(selectedCaseId);
            reference = referenceMatrix;
            assertClose(scalarOracle(), MooCore.epsilonMultiplicative(points, reference), caseId);
        }
    }

    @State(Scope.Benchmark)
    public static class EafState extends CaseState {

        @Param("unset")
        public String selectedCaseId;

        int[] sets;

        @Setup(Level.Trial)
        public void setup() {
            loadCase(selectedCaseId);
            sets = integerVector("sets");
            assertMatrixCloseUnordered(matrix("oracle"), MooCore.eaf(points, sets), caseId);
        }
    }

    @State(Scope.Benchmark)
    public static class EafDifferenceState extends CaseState {

        @Param("unset")
        public String selectedCaseId;

        Dataset left;
        Dataset right;
        EafDifferenceFormat format;

        @Setup(Level.Trial)
        public void setup() {
            loadCase(selectedCaseId);
            left = new Dataset(points, integerVector("sets"));
            right = new Dataset(matrix("rightPoints"), integerVector("rightSets"));
            format = EafDifferenceFormat.valueOf(properties.getProperty("format"));
            assertMatrixCloseUnordered(matrix("oracle"),
                    MooCore.eafDifference(left, right, null,
                            new boolean[]{false}, format), caseId);
        }
    }

    @State(Scope.Benchmark)
    public static class WeightedHypervolumeState extends ReferencedState {

        @Param("unset")
        public String selectedCaseId;

        double[] reference;
        double[][] rectangles;

        @Setup(Level.Trial)
        public void setup() {
            loadReferencedCase(selectedCaseId);
            reference = vector(referenceMatrix, caseId);
            rectangles = matrix("auxiliary");
            assertClose(scalarOracle(),
                    MooCore.weightedHypervolume(points, rectangles, reference), caseId);
        }
    }

    @State(Scope.Benchmark)
    public static class HypeState extends ReferencedState {

        @Param("unset")
        public String selectedCaseId;

        double[] reference;
        double[] ideal;
        int samples;
        long seed;
        HypeDistribution distribution;
        double[] mu;

        @Setup(Level.Trial)
        public void setup() {
            loadReferencedCase(selectedCaseId);
            reference = vector(referenceMatrix, caseId);
            ideal = vector(matrix("ideal"), caseId);
            samples = integer("samples");
            seed = longValue("seed");
            distribution = HypeDistribution.valueOf(properties.getProperty("distribution"));
            String configuredMu = properties.getProperty("mu");
            mu = configuredMu == null || configuredMu.isBlank() ? null : vector(matrix("mu"), caseId);
            double actual = MooCore.hypeWeightedHypervolume(points, reference, ideal,
                    new boolean[]{false}, samples, seed, distribution, mu);
            double expected = scalarOracle();
            if (!Double.isFinite(actual) || relativeError(expected, actual) > 0.05) {
                throw new IllegalStateException("HypE result exceeds 5% relative error for "
                        + caseId + ": expected=" + expected + ", actual=" + actual);
            }
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

    private static void assertVectorClose(double[][] expectedMatrix, double[] actual,
                                          String caseId) {
        if (expectedMatrix.length != actual.length) {
            throw new IllegalStateException("Vector length differs for " + caseId);
        }
        for (int i = 0; i < actual.length; i++) {
            if (expectedMatrix[i].length != 1) {
                throw new IllegalStateException("Vector oracle expected for " + caseId);
            }
            assertClose(expectedMatrix[i][0], actual[i], caseId + " at index " + i);
        }
    }

    private static void assertMatrixCloseUnordered(double[][] expected, double[][] actual,
                                                    String caseId) {
        if (expected.length != actual.length) {
            throw new IllegalStateException("Matrix row count differs for " + caseId
                    + ": expected=" + expected.length + ", actual=" + actual.length);
        }
        double[][] sortedExpected = expected.clone();
        double[][] sortedActual = actual.clone();
        Arrays.sort(sortedExpected, CrossLanguageMoocoreBenchmark::compareRows);
        Arrays.sort(sortedActual, CrossLanguageMoocoreBenchmark::compareRows);
        for (int row = 0; row < sortedExpected.length; row++) {
            if (sortedExpected[row].length != sortedActual[row].length) {
                throw new IllegalStateException("Matrix column count differs for " + caseId);
            }
            for (int column = 0; column < sortedExpected[row].length; column++) {
                double expectedValue = sortedExpected[row][column];
                double actualValue = sortedActual[row][column];
                if (Double.doubleToLongBits(expectedValue) == Double.doubleToLongBits(actualValue)) {
                    continue;
                }
                assertClose(expectedValue, actualValue,
                        caseId + " at row " + row + ", column " + column);
            }
        }
    }

    private static int compareRows(double[] left, double[] right) {
        int columns = Math.min(left.length, right.length);
        for (int column = 0; column < columns; column++) {
            int comparison = Double.compare(left[column], right[column]);
            if (comparison != 0) {
                return comparison;
            }
        }
        return Integer.compare(left.length, right.length);
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
