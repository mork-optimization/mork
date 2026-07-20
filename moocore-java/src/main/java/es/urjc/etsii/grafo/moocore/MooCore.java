// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore;

import es.urjc.etsii.grafo.moocore.internal.AttainmentAlgorithms;
import es.urjc.etsii.grafo.moocore.internal.DatasetRepository;
import es.urjc.etsii.grafo.moocore.internal.HypervolumeAlgorithms;
import es.urjc.etsii.grafo.moocore.internal.HypervolumeApproximationAlgorithms;
import es.urjc.etsii.grafo.moocore.internal.Indicators;
import es.urjc.etsii.grafo.moocore.internal.MatrixUtils;
import es.urjc.etsii.grafo.moocore.internal.ParetoAlgorithms;
import es.urjc.etsii.grafo.moocore.internal.Transformations;
import es.urjc.etsii.grafo.moocore.internal.WeightedHypervolumeAlgorithms;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

/** Static facade for multi-objective mathematical operations. */
public final class MooCore {

    private static final boolean[] MINIMISE = {false};

    private MooCore() {
    }

    public static double igd(double[][] points, double[][] reference) {
        return igd(points, reference, MINIMISE);
    }

    public static double igd(double[][] points, double[][] reference, boolean[] maximise) {
        return Indicators.igd(points, reference, maximise);
    }

    public static double igdPlus(double[][] points, double[][] reference) {
        return igdPlus(points, reference, MINIMISE);
    }

    public static double igdPlus(double[][] points, double[][] reference, boolean[] maximise) {
        return Indicators.igdPlus(points, reference, maximise);
    }

    public static double averageHausdorffDistance(double[][] points, double[][] reference, int p) {
        return averageHausdorffDistance(points, reference, MINIMISE, p);
    }

    public static double averageHausdorffDistance(double[][] points, double[][] reference) {
        return averageHausdorffDistance(points, reference, MINIMISE, 1);
    }

    public static double averageHausdorffDistance(double[][] points, double[][] reference,
                                                  boolean[] maximise, int p) {
        return Indicators.averageHausdorffDistance(points, reference, maximise, p);
    }

    public static double epsilonAdditive(double[][] points, double[][] reference) {
        return epsilonAdditive(points, reference, MINIMISE);
    }

    public static double epsilonAdditive(double[][] points, double[][] reference, boolean[] maximise) {
        return Indicators.epsilonAdditive(points, reference, maximise);
    }

    public static double epsilonMultiplicative(double[][] points, double[][] reference) {
        return epsilonMultiplicative(points, reference, MINIMISE);
    }

    public static double epsilonMultiplicative(double[][] points, double[][] reference,
                                               boolean[] maximise) {
        return Indicators.epsilonMultiplicative(points, reference, maximise);
    }

    public static double exactR2(double[][] points, double[] reference) {
        return exactR2(points, reference, MINIMISE);
    }

    public static double exactR2(double[][] points, double[] reference, boolean[] maximise) {
        return Indicators.exactR2(points, reference, maximise);
    }

    public static double hypervolume(double[][] points, double[] reference) {
        return hypervolume(points, reference, MINIMISE);
    }

    public static double hypervolume(double[][] points, double reference) {
        return hypervolume(points, new double[]{reference}, MINIMISE);
    }

    public static double hypervolume(double[][] points, double[] reference, boolean[] maximise) {
        return HypervolumeAlgorithms.hypervolume(points, reference, maximise);
    }

    public static double[] hypervolumeContributions(double[][] points, double[] reference) {
        return hypervolumeContributions(points, reference, MINIMISE, true);
    }

    public static double[] hypervolumeContributions(double[][] points, double[] reference,
                                                     boolean[] maximise) {
        return hypervolumeContributions(points, reference, maximise, true);
    }

    public static double[] hypervolumeContributions(double[][] points, double[] reference,
                                                     boolean[] maximise, boolean ignoreDominated) {
        return HypervolumeAlgorithms.contributions(points, reference, maximise, ignoreDominated);
    }

    public static double approximateHypervolume(double[][] points, double[] reference,
                                                HypervolumeApproximation method) {
        return approximateHypervolume(points, reference, MINIMISE, 262_144, 0L, method);
    }

    public static double approximateHypervolume(double[][] points, double[] reference) {
        return approximateHypervolume(points, reference, HypervolumeApproximation.RPHI_FWE_PLUS);
    }

    public static double approximateHypervolume(double[][] points, double[] reference,
                                                boolean[] maximise, long samples, long seed,
                                                HypervolumeApproximation method) {
        if (method == null) {
            throw new IllegalArgumentException("method cannot be null");
        }
        return HypervolumeApproximationAlgorithms.approximate(
                points, reference, maximise, samples, seed, method);
    }

    public static boolean[] isNondominated(double[][] points) {
        return isNondominated(points, MINIMISE, false);
    }

    public static boolean[] isNondominated(double[][] points, boolean[] maximise, boolean keepWeakly) {
        return ParetoAlgorithms.isNondominated(points, maximise, keepWeakly);
    }

    public static boolean anyDominated(double[][] points) {
        return anyDominated(points, MINIMISE, false);
    }

    public static boolean anyDominated(double[][] points, boolean[] maximise, boolean keepWeakly) {
        return ParetoAlgorithms.anyDominated(points, maximise, keepWeakly);
    }

    public static double[][] filterDominated(double[][] points) {
        return filterDominated(points, MINIMISE, false);
    }

    public static double[][] filterDominated(double[][] points, boolean[] maximise, boolean keepWeakly) {
        return MatrixUtils.select(points, isNondominated(points, maximise, keepWeakly));
    }

    public static boolean[] isNondominatedWithinSets(double[][] points, int[] sets,
                                                      boolean[] maximise, boolean keepWeakly) {
        MatrixUtils.validate(points, 2, 255);
        Map<Integer, int[]> groups = MatrixUtils.groups(sets, points.length);
        boolean[] result = new boolean[points.length];
        for (int[] indices : groups.values()) {
            double[][] group = new double[indices.length][];
            for (int i = 0; i < indices.length; i++) {
                group[i] = points[indices[i]];
            }
            boolean[] selected = isNondominated(group, maximise, keepWeakly);
            for (int i = 0; i < indices.length; i++) {
                result[indices[i]] = selected[i];
            }
        }
        return result;
    }

    public static boolean[] isNondominatedWithinSets(double[][] points, int[] sets) {
        return isNondominatedWithinSets(points, sets, MINIMISE, false);
    }

    public static Dataset filterDominatedWithinSets(Dataset dataset,
                                                     boolean[] maximise, boolean keepWeakly) {
        double[][] points = dataset.pointsView();
        int[] sets = dataset.setsView();
        boolean[] selected = isNondominatedWithinSets(points, sets, maximise, keepWeakly);
        int count = 0;
        for (boolean value : selected) {
            if (value) {
                count++;
            }
        }
        double[][] resultPoints = new double[count][];
        int[] resultSets = new int[count];
        int next = 0;
        for (int i = 0; i < selected.length; i++) {
            if (selected[i]) {
                resultPoints[next] = points[i];
                resultSets[next] = sets[i];
                next++;
            }
        }
        return new Dataset(resultPoints, resultSets);
    }

    public static Dataset filterDominatedWithinSets(Dataset dataset) {
        return filterDominatedWithinSets(dataset, MINIMISE, false);
    }

    public static int[] paretoRank(double[][] points) {
        return paretoRank(points, MINIMISE);
    }

    public static int[] paretoRank(double[][] points, boolean[] maximise) {
        return ParetoAlgorithms.ranks(points, maximise);
    }

    public static double[][] normalise(double[][] points) {
        return normalise(points, new double[]{0.0, 1.0}, null, null, MINIMISE);
    }

    public static double[][] normalise(double[][] points, double[] targetRange, double[] lower,
                                       double[] upper, boolean[] maximise) {
        return Transformations.normalise(points, targetRange, lower, upper, maximise);
    }

    public static double[][] generateNondominatedSet(int size, int objectives,
                                                     NondominatedSetShape shape, long seed) {
        return Transformations.generate(size, objectives, shape, seededRandom(seed));
    }

    public static double[][] generateNondominatedSet(int size, int objectives,
                                                     NondominatedSetShape shape,
                                                     RandomGenerator random) {
        return Transformations.generate(size, objectives, shape, random);
    }

    public static long[][] generateIntegerNondominatedSet(int size, int objectives,
                                                          NondominatedSetShape shape, long seed) {
        return Transformations.generateInteger(size, objectives, shape, seededRandom(seed));
    }

    public static double[][] eaf(double[][] points, int[] sets) {
        return eaf(points, sets, new double[0]);
    }

    public static double[][] eaf(double[][] points, int[] sets, double[] percentiles) {
        return AttainmentAlgorithms.eaf(points, sets, percentiles);
    }

    public static double[][] eafDifference(Dataset left, Dataset right, Integer intervals,
                                           boolean[] maximise, EafDifferenceFormat format) {
        if (format == null) {
            throw new IllegalArgumentException("format cannot be null");
        }
        return AttainmentAlgorithms.eafDifference(
                left.pointsView(), left.setsView(), right.pointsView(), right.setsView(),
                maximise, intervals, format);
    }

    public static double[][] eafDifference(Dataset left, Dataset right) {
        return eafDifference(left, right, null, MINIMISE, EafDifferenceFormat.POINTS);
    }

    public static double[][] eafDifference(Dataset left, Dataset right,
                                           EafDifferenceFormat format) {
        return eafDifference(left, right, null, MINIMISE, format);
    }

    public static VorobevResult vorobev(double[][] points, int[] sets, double[] reference) {
        MatrixUtils.validate(points, 2, 3);
        Hypervolume indicator = new Hypervolume(reference);
        double average = averageWithinSets(points, sets, indicator);
        double low = 0.0;
        double high = 100.0;
        double previous = Double.POSITIVE_INFINITY;
        double threshold = 50.0;
        double[][] expectation = new double[0][];
        for (int iteration = 0; iteration < 128; iteration++) {
            threshold = (low + high) * 0.5;
            double[][] surface = eaf(points, sets, new double[]{threshold});
            expectation = withoutLastColumn(surface);
            double value = indicator.compute(expectation);
            if (value > average) {
                low = threshold;
            } else {
                high = threshold;
            }
            if (Double.compare(previous, value) == 0) {
                break;
            }
            previous = value;
        }
        return new VorobevResult(threshold, expectation, average);
    }

    public static double vorobevDeviation(double[][] points, int[] sets, double[] reference) {
        return vorobevDeviation(points, sets, reference, null);
    }

    public static double vorobevDeviation(double[][] points, int[] sets, double[] reference,
                                          double[][] configuredExpectation) {
        Hypervolume indicator = new Hypervolume(reference);
        double[][] expectation;
        double average;
        if (configuredExpectation == null) {
            VorobevResult result = vorobev(points, sets, reference);
            expectation = result.expectationView();
            average = result.averageHypervolume();
        } else {
            expectation = MatrixUtils.deepCopy(configuredExpectation);
            average = averageWithinSets(points, sets, indicator);
        }
        double expectationHypervolume = indicator.compute(expectation);
        Map<Integer, int[]> groups = MatrixUtils.groups(sets, points.length);
        double unionAverage = 0.0;
        for (int[] indices : groups.values()) {
            double[][] group = MatrixUtils.rows(points, indices);
            double[][] union = new double[group.length + expectation.length][];
            System.arraycopy(group, 0, union, 0, group.length);
            System.arraycopy(expectation, 0, union, group.length, expectation.length);
            unionAverage += indicator.compute(union);
        }
        unionAverage /= groups.size();
        return 2.0 * unionAverage - average - expectationHypervolume;
    }

    public static double weightedHypervolume(double[][] points, double[][] rectangles,
                                             double[] reference) {
        return WeightedHypervolumeAlgorithms.rectangles(points, rectangles, reference, MINIMISE);
    }

    public static double weightedHypervolume(double[][] points, double[][] rectangles,
                                             double[] reference, boolean[] maximise) {
        return WeightedHypervolumeAlgorithms.rectangles(points, rectangles, reference, maximise);
    }

    public static double totalWeightedHypervolume(double[][] points, double[][] rectangles,
                                                  double[] reference, double[] ideal,
                                                  double scaleFactor) {
        return WeightedHypervolumeAlgorithms.total(
                points, rectangles, reference, MINIMISE, ideal, scaleFactor);
    }

    public static double totalWeightedHypervolume(double[][] points, double[][] rectangles,
                                                  double[] reference, boolean[] maximise,
                                                  double[] ideal, double scaleFactor) {
        return WeightedHypervolumeAlgorithms.total(
                points, rectangles, reference, maximise, ideal, scaleFactor);
    }

    public static double totalWeightedHypervolume(double[][] points, double[][] rectangles,
                                                  double[] reference) {
        return totalWeightedHypervolume(points, rectangles, reference, null, 0.1);
    }

    public static double hypeWeightedHypervolume(double[][] points, double[] reference,
                                                 double[] ideal, boolean[] maximise, int samples,
                                                 long seed, HypeDistribution distribution,
                                                 double[] mu) {
        return WeightedHypervolumeAlgorithms.hype(
                points, reference, ideal, maximise, samples, seed, distribution, mu);
    }

    public static double hypeWeightedHypervolume(double[][] points, double[] reference,
                                                 double[] ideal) {
        return hypeWeightedHypervolume(points, reference, ideal, MINIMISE, 100_000, 0L,
                HypeDistribution.UNIFORM, null);
    }

    public static LargestEafDifference largestEafDifference(List<Dataset> datasets,
                                                            double[] reference, int intervals) {
        return largestEafDifference(datasets, reference, MINIMISE, intervals, null);
    }

    public static LargestEafDifference largestEafDifference(List<Dataset> datasets,
                                                            double[] reference) {
        return largestEafDifference(datasets, reference, MINIMISE, 5, null);
    }

    public static LargestEafDifference largestEafDifference(List<Dataset> datasets,
                                                            double[] reference,
                                                            boolean[] maximise,
                                                            int intervals,
                                                            double[] configuredIdeal) {
        if (datasets == null || datasets.size() < 2) {
            throw new IllegalArgumentException("at least two datasets are required");
        }
        boolean[] directions = MatrixUtils.directions(maximise, 2);
        double[] ideal;
        if (configuredIdeal == null || configuredIdeal.length == 0) {
            ideal = new double[]{directions[0] ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY,
                    directions[1] ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY};
            for (Dataset dataset : datasets) {
                for (double[] point : dataset.pointsView()) {
                    ideal[0] = directions[0] ? Math.max(ideal[0], point[0]) : Math.min(ideal[0], point[0]);
                    ideal[1] = directions[1] ? Math.max(ideal[1], point[1]) : Math.min(ideal[1], point[1]);
                }
            }
        } else {
            ideal = MatrixUtils.vector(configuredIdeal, 2, "ideal");
        }
        int bestLeft = -1;
        int bestRight = -1;
        double bestValue = 0.0;
        for (int left = 0; left < datasets.size() - 1; left++) {
            for (int right = left + 1; right < datasets.size(); right++) {
                double[][] difference = eafDifference(datasets.get(left), datasets.get(right),
                        intervals, directions, EafDifferenceFormat.RECTANGLES);
                double leftValue = weightedDifferenceSide(ideal, reference, difference, true, directions);
                double rightValue = weightedDifferenceSide(ideal, reference, difference, false, directions);
                double value = Math.min(leftValue, rightValue);
                if (bestLeft < 0 || value > bestValue) {
                    bestValue = value;
                    bestLeft = left;
                    bestRight = right;
                }
            }
        }
        return new LargestEafDifference(bestLeft, bestRight, bestValue);
    }

    public static double[] applyWithinSets(double[][] points, int[] sets,
                                           ToDoubleFunction<double[][]> operation) {
        if (operation == null) {
            throw new IllegalArgumentException("operation cannot be null");
        }
        Map<Integer, int[]> groups = MatrixUtils.groups(sets, points.length);
        double[] result = new double[groups.size()];
        int next = 0;
        for (int[] indices : groups.values()) {
            result[next++] = operation.applyAsDouble(MatrixUtils.rows(points, indices));
        }
        return result;
    }

    public static double[] applyRowsWithinSets(double[][] points, int[] sets,
                                               Function<double[][], double[]> operation) {
        if (operation == null) {
            throw new IllegalArgumentException("operation cannot be null");
        }
        Map<Integer, int[]> groups = MatrixUtils.groups(sets, points.length);
        double[] result = new double[points.length];
        for (int[] indices : groups.values()) {
            double[] values = operation.apply(MatrixUtils.rows(points, indices));
            if (values.length != indices.length) {
                throw new IllegalArgumentException("row operation must return one value per input row");
            }
            for (int i = 0; i < indices.length; i++) {
                result[indices[i]] = values[i];
            }
        }
        return result;
    }

    public static double[][] applyMatricesWithinSets(double[][] points, int[] sets,
                                                     Function<double[][], double[][]> operation) {
        if (operation == null) {
            throw new IllegalArgumentException("operation cannot be null");
        }
        Map<Integer, int[]> groups = MatrixUtils.groups(sets, points.length);
        List<double[]> groupedResults = new ArrayList<>();
        boolean rowPreserving = true;
        double[][] ordered = new double[points.length][];
        for (int[] indices : groups.values()) {
            double[][] values = operation.apply(MatrixUtils.rows(points, indices));
            if (values.length == indices.length) {
                for (int i = 0; i < indices.length; i++) {
                    ordered[indices[i]] = values[i].clone();
                }
            } else if (values.length == 1) {
                rowPreserving = false;
                groupedResults.add(values[0].clone());
            } else {
                throw new IllegalArgumentException("matrix operation must preserve rows or return one row per set");
            }
        }
        return rowPreserving ? ordered : groupedResults.toArray(double[][]::new);
    }

    public static Dataset readDataset(Path path) throws IOException {
        return DatasetRepository.read(path);
    }

    public static Dataset readDataset(InputStream input) throws IOException {
        return DatasetRepository.read(input);
    }

    public static Dataset getDataset(String name) throws IOException {
        return DatasetRepository.get(name);
    }

    public static Path getDatasetPath(String name) throws IOException {
        return DatasetRepository.getPath(name, false, 3, Duration.ofSeconds(1));
    }

    public static Path getDatasetPath(String name, boolean force, int retries,
                                      Duration delay) throws IOException {
        return DatasetRepository.getPath(name, force, retries, delay);
    }

    private static RandomGenerator seededRandom(long seed) {
        return RandomGeneratorFactory.<RandomGenerator>of("L64X128MixRandom").create(seed);
    }

    private static double averageWithinSets(double[][] points, int[] sets, Hypervolume indicator) {
        double[] values = applyWithinSets(points, sets, indicator);
        double sum = 0.0;
        for (double value : values) {
            sum += value;
        }
        return sum / values.length;
    }

    private static double[][] withoutLastColumn(double[][] matrix) {
        double[][] result = new double[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            result[i] = Arrays.copyOf(matrix[i], matrix[i].length - 1);
        }
        return result;
    }

    private static double weightedDifferenceSide(double[] ideal, double[] reference,
                                                 double[][] difference, boolean positive,
                                                 boolean[] maximise) {
        List<double[]> selected = new ArrayList<>();
        for (double[] rectangle : difference) {
            if ((positive && rectangle[4] >= 1.0) || (!positive && rectangle[4] <= -1.0)) {
                double[] copy = rectangle.clone();
                copy[4] = 1.0;
                selected.add(copy);
            }
        }
        if (selected.isEmpty()) {
            return 0.0;
        }
        return WeightedHypervolumeAlgorithms.rectangles(new double[][]{ideal},
                selected.toArray(double[][]::new), reference, maximise);
    }
}
