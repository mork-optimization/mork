// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MooCoreTest {

    private static final double TOLERANCE = 1e-10;

    @Test
    void computesPublishedIndicators() {
        double[][] points = {{5, 5}, {4, 6}, {2, 7}, {7, 4}};

        assertEquals(38.0, MooCore.hypervolume(points, new double[]{10, 10}), TOLERANCE);
        assertEquals(39.0, MooCore.hypervolume(points, new double[]{0}, new boolean[]{true}), TOLERANCE);
        assertArrayEquals(new double[]{2, 1, 6, 3},
                MooCore.hypervolumeContributions(points, new double[]{10, 10}), TOLERANCE);
        assertEquals(2.594191919191919, MooCore.exactR2(points, new double[]{0, 0}), TOLERANCE);
        assertEquals(2.519696969696969,
                MooCore.exactR2(points, new double[]{10, 10}, new boolean[]{true}), TOLERANCE);
    }

    @Test
    void matchesDeterministicHypervolumeApproximations() {
        double[][] points = {{5, 5}, {4, 6}, {2, 7}, {7, 4}};
        assertEquals(37.9999789454246, MooCore.approximateHypervolume(
                points, new double[]{10, 10}, new boolean[]{false}, 262_144, 0,
                HypervolumeApproximation.RPHI_FWE_PLUS), 1e-9);
        assertEquals(37.9999580554839, MooCore.approximateHypervolume(
                points, new double[]{10, 10}, new boolean[]{false}, 262_144, 0,
                HypervolumeApproximation.DZ2019_HW), 1e-9);
    }

    @Test
    void seededMonteCarloHypervolumeIsReproducible() {
        double[][] points = {{5, 5}, {4, 6}, {2, 7}, {7, 4}};
        double first = MooCore.approximateHypervolume(
                points, new double[]{10, 10}, new boolean[]{false}, 10_000, 42,
                HypervolumeApproximation.DZ2019_MC);
        double second = MooCore.approximateHypervolume(
                points, new double[]{10, 10}, new boolean[]{false}, 10_000, 42,
                HypervolumeApproximation.DZ2019_MC);
        assertEquals(first, second, 0.0);
    }

    @Test
    void huaWangApproximationIsDeterministicAcrossSupportedDimensions() {
        for (int dimensions : new int[]{2, 3, 6, 9, 16, 31}) {
            double[][] points = new double[1][dimensions];
            double[] reference = new double[dimensions];
            Arrays.fill(reference, 1.0);
            double first = MooCore.approximateHypervolume(
                    points, reference, new boolean[]{false}, 256, 0,
                    HypervolumeApproximation.DZ2019_HW);
            double second = MooCore.approximateHypervolume(
                    points, reference, new boolean[]{false}, 256, 0,
                    HypervolumeApproximation.DZ2019_HW);

            assertTrue(Double.isFinite(first), "dimensions=" + dimensions);
            assertTrue(first > 0.0, "dimensions=" + dimensions);
            assertEquals(first, second, 0.0, "dimensions=" + dimensions);
        }
    }

    @Test
    void handlesDominanceDuplicatesAndDirections() {
        double[][] points = {{1, 1}, {0, 1}, {1, 0}, {1, 0}};
        assertArrayEquals(new boolean[]{false, true, true, false}, MooCore.isNondominated(points));
        assertArrayEquals(new boolean[]{false, true, true, true},
                MooCore.isNondominated(points, new boolean[]{false}, true));
        assertTrue(MooCore.anyDominated(points));
        assertArrayEquals(new int[]{1, 0, 0, 0}, MooCore.paretoRank(points));

        double[][] maximizing = {{-1, -1}, {0, -1}, {-1, 0}, {-1, 0}};
        assertArrayEquals(MooCore.isNondominated(points),
                MooCore.isNondominated(maximizing, new boolean[]{true}, false));
    }

    @Test
    void optimizedDominanceMatchesNaiveDefinition() {
        Random random = new Random(23);
        for (int dimensions = 2; dimensions <= 7; dimensions++) {
            for (int iteration = 0; iteration < 50; iteration++) {
                double[][] points = new double[30][dimensions];
                for (double[] point : points) {
                    for (int objective = 0; objective < dimensions; objective++) {
                        point[objective] = random.nextInt(8);
                    }
                }
                assertArrayEquals(naiveNondominated(points, false), MooCore.isNondominated(points));
                assertArrayEquals(naiveNondominated(points, true),
                        MooCore.isNondominated(points, new boolean[]{false}, true));
            }
        }
    }

    @Test
    void highDimensionalDispatchBoundariesMatchNaiveDefinition() {
        Random random = new Random(71);
        for (int dimensions : new int[]{4, 5, 9}) {
            for (int size : new int[]{15, 16, 17, 18, 33}) {
                for (int iteration = 0; iteration < 5; iteration++) {
                    double[][] points = new double[size][dimensions];
                    for (double[] point : points) {
                        for (int objective = 0; objective < dimensions; objective++) {
                            point[objective] = random.nextInt(6);
                        }
                    }
                    assertParetoOperationsMatchNaive(points, new boolean[]{false});
                }
            }
        }
    }

    @Test
    void highDimensionalDegenerateCoordinatesAndDirectionsMatchNaiveDefinition() {
        Random random = new Random(89);
        double[][] points = new double[35][9];
        for (double[] point : points) {
            for (int objective = 0; objective < 3; objective++) {
                point[objective] = random.nextInt(7);
            }
        }

        boolean[] maximise = new boolean[9];
        for (int objective = 0; objective < maximise.length; objective++) {
            maximise[objective] = objective % 2 == 1;
        }
        for (int rotation = 0; rotation < points[0].length; rotation++) {
            double[][] rotated = rotateObjectives(points, rotation);
            boolean[] rotatedDirections = rotateDirections(maximise, rotation);
            assertParetoOperationsMatchNaive(rotated, rotatedDirections);
        }

        double[][] equal = new double[18][9];
        for (int row = 0; row < equal.length; row++) {
            for (int objective = 0; objective < equal[row].length; objective++) {
                equal[row][objective] = (row + objective) % 2 == 0 ? 0.0 : -0.0;
            }
        }
        assertParetoOperationsMatchNaive(equal, new boolean[]{false});
    }

    @Test
    void paretoRanksMatchNaiveFrontExtraction() {
        Random random = new Random(47);
        for (int dimensions = 1; dimensions <= 6; dimensions++) {
            for (int iteration = 0; iteration < 25; iteration++) {
                double[][] points = new double[24][dimensions];
                for (double[] point : points) {
                    for (int objective = 0; objective < dimensions; objective++) {
                        point[objective] = random.nextInt(7);
                    }
                }
                assertArrayEquals(naiveRanks(points), MooCore.paretoRank(points));
            }
        }
    }

    @Test
    void twoDimensionalParetoOperationsMatchNaiveAcrossDirections() {
        Random random = new Random(107);
        boolean[][] directions = {{false}, {true}, {false, true}, {true, false}};
        for (int size : new int[]{1, 2, 15, 16, 17, 64}) {
            for (int iteration = 0; iteration < 30; iteration++) {
                double[][] points = new double[size][2];
                for (double[] point : points) {
                    point[0] = random.nextInt(11) - 5;
                    point[1] = random.nextInt(11) - 5;
                }
                double[][] original = copy(points);
                for (boolean[] maximise : directions) {
                    assertParetoOperationsMatchNaive(points, maximise);
                }
                assertMatrixEquals(original, points);
            }
        }

        double[][] limits = {
                {Double.NEGATIVE_INFINITY, 3.0},
                {Double.NEGATIVE_INFINITY, 3.0},
                {0.0, -0.0},
                {-0.0, 0.0},
                {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY},
                {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY}
        };
        for (boolean[] maximise : directions) {
            assertParetoOperationsMatchNaive(limits, maximise);
        }
    }

    @Test
    void paretoRanksTreatSignedZerosAsEqual() {
        double[][] positiveZeroFirst = {{0.0}, {-0.0}, {1.0}, {-1.0}};
        double[][] negativeZeroFirst = {{-0.0}, {0.0}, {1.0}, {-1.0}};
        int[] minimising = {1, 1, 2, 0};
        int[] maximising = {1, 1, 0, 2};

        assertArrayEquals(minimising, MooCore.paretoRank(positiveZeroFirst));
        assertArrayEquals(minimising, MooCore.paretoRank(negativeZeroFirst));
        assertArrayEquals(maximising,
                MooCore.paretoRank(positiveZeroFirst, new boolean[]{true}));
        assertArrayEquals(maximising,
                MooCore.paretoRank(negativeZeroFirst, new boolean[]{true}));
    }

    @Test
    void paretoRanksHandleDegenerateMatrices() {
        assertArrayEquals(new int[0], MooCore.paretoRank(new double[0][]));
        assertArrayEquals(new int[5], MooCore.paretoRank(new double[5][0]));
    }

    @Test
    void computesDistanceAndEpsilonIndicators() {
        double[][] points = {{1, 2}, {2, 1}};
        double[][] reference = {{1, 1}, {2, 2}};
        assertEquals(1.0, MooCore.igd(points, reference), TOLERANCE);
        assertEquals(0.5, MooCore.igdPlus(points, reference), TOLERANCE);
        assertEquals(1.0, MooCore.averageHausdorffDistance(points, reference, 1), TOLERANCE);
        assertEquals(1.0, MooCore.epsilonAdditive(points, reference), TOLERANCE);
        assertEquals(2.0, MooCore.epsilonMultiplicative(points, reference), TOLERANCE);
    }

    @Test
    void additiveEpsilonMatchesNaiveAcrossDirections() {
        Random random = new Random(103);
        for (int objectives = 1; objectives <= 12; objectives++) {
            boolean[] mixed = new boolean[objectives];
            for (int objective = 0; objective < objectives; objective++) {
                mixed[objective] = objective % 2 == 0;
            }
            boolean[][] directions = {{false}, {true}, mixed};
            for (int iteration = 0; iteration < 30; iteration++) {
                double[][] points = new double[15][objectives];
                double[][] reference = new double[17][objectives];
                for (double[] point : points) {
                    for (int objective = 0; objective < objectives; objective++) {
                        point[objective] = random.nextInt(21) - 10;
                    }
                }
                for (double[] target : reference) {
                    for (int objective = 0; objective < objectives; objective++) {
                        target[objective] = random.nextInt(21) - 10;
                    }
                }
                double[][] originalPoints = copy(points);
                double[][] originalReference = copy(reference);
                for (boolean[] maximise : directions) {
                    assertEquals(
                            naiveAdditiveEpsilon(points, reference, maximise),
                            MooCore.epsilonAdditive(points, reference, maximise),
                            TOLERANCE,
                            "objectives=" + objectives + ", iteration=" + iteration
                                    + ", maximise=" + Arrays.toString(maximise));
                }
                assertMatrixEquals(originalPoints, points);
                assertMatrixEquals(originalReference, reference);
            }
        }
    }

    @Test
    void computesIndicatorsAboveTheCDimensionLimit() {
        for (int dimensions : new int[]{256, 300}) {
            double[][] points = new double[2][dimensions];
            Arrays.fill(points[0], 1.0);
            Arrays.fill(points[1], 2.0);
            double[][] reference = new double[1][dimensions];
            Arrays.fill(reference[0], 1.5);
            double distance = 0.5 * Math.sqrt(dimensions);

            assertEquals(distance, MooCore.igd(points, reference), TOLERANCE);
            assertEquals(0.0, MooCore.igdPlus(points, reference), TOLERANCE);
            assertEquals(distance,
                    MooCore.averageHausdorffDistance(points, reference, 1), TOLERANCE);
            assertEquals(-0.5, MooCore.epsilonAdditive(points, reference), TOLERANCE);
            assertEquals(2.0 / 3.0,
                    MooCore.epsilonMultiplicative(points, reference), TOLERANCE);
        }
    }

    @Test
    void normalisesWithoutMutatingInput() {
        double[][] points = {{3.5, 5.5}, {3.6, 4.1}, {4.1, 3.2}, {5.5, 1.5}};
        double[][] original = copy(points);
        double[][] normalized = MooCore.normalise(points);

        assertArrayEquals(new double[]{0, 1}, normalized[0], TOLERANCE);
        assertArrayEquals(new double[]{0.05, 0.65}, normalized[1], TOLERANCE);
        assertArrayEquals(new double[]{0.3, 0.425}, normalized[2], TOLERANCE);
        assertArrayEquals(new double[]{1, 0}, normalized[3], TOLERANCE);
        assertMatrixEquals(original, points);
    }

    @Test
    void calculatesThreeDimensionalAndRecursiveHypervolume() {
        double[][] points3d = {{1, 4, 4}, {2, 2, 3}, {4, 1, 2}};
        assertEquals(bruteForceHypervolume(points3d, new double[]{5, 5, 5}),
                MooCore.hypervolume(points3d, new double[]{5, 5, 5}), TOLERANCE);

        double[][] points4d = {{1, 4, 4, 4}, {2, 2, 3, 3}, {4, 1, 2, 2}};
        assertEquals(bruteForceHypervolume(points4d, new double[]{5, 5, 5, 5}),
                MooCore.hypervolume(points4d, new double[]{5, 5, 5, 5}), TOLERANCE);
    }

    @Test
    void exactHypervolumeMatchesInclusionExclusionForSmallFronts() {
        Random random = new Random(91);
        for (int dimensions = 2; dimensions <= 5; dimensions++) {
            double[] reference = new double[dimensions];
            Arrays.fill(reference, 10.0);
            for (int iteration = 0; iteration < 30; iteration++) {
                double[][] points = new double[6][dimensions];
                for (double[] point : points) {
                    for (int objective = 0; objective < dimensions; objective++) {
                        point[objective] = 1 + random.nextInt(11);
                    }
                }
                assertEquals(bruteForceHypervolume(points, reference),
                        MooCore.hypervolume(points, reference), 1e-9,
                        "dimensions=" + dimensions + ", iteration=" + iteration
                                + ", points=" + Arrays.deepToString(points));
            }
        }
    }

    @Test
    void threeDimensionalHypervolumeMatchesInclusionExclusionAcrossDirections() {
        Random random = new Random(109);
        boolean[][] directions = {
                {false}, {true}, {false, false, true}, {true, false, true}
        };
        double[] reference = {5.0, 5.0, 5.0};
        for (int iteration = 0; iteration < 50; iteration++) {
            double[][] points = new double[10][3];
            for (double[] point : points) {
                point[0] = random.nextInt(13) - 4;
                point[1] = random.nextInt(13) - 4;
                point[2] = random.nextInt(5) - 2;
            }
            if (iteration % 2 == 0) {
                points[9] = points[0].clone();
            }
            double[][] originalPoints = copy(points);
            double[] originalReference = reference.clone();
            for (boolean[] maximise : directions) {
                assertEquals(
                        bruteForceHypervolume(points, reference, maximise),
                        MooCore.hypervolume(points, reference, maximise),
                        TOLERANCE,
                        "iteration=" + iteration
                                + ", maximise=" + Arrays.toString(maximise));
            }
            assertMatrixEquals(originalPoints, points);
            assertArrayEquals(originalReference, reference);
        }
    }

    @Test
    void exactHypervolumeMatchesAtInclusionExclusionBoundary() {
        Random random = new Random(92);
        for (int dimensions : new int[]{5, 9}) {
            double[] reference = new double[dimensions];
            Arrays.fill(reference, 1.0);
            for (int size : new int[]{12, 13}) {
                double[][] points = new double[size][dimensions];
                for (double[] point : points) {
                    double sum = 0.0;
                    for (int objective = 0; objective < dimensions; objective++) {
                        point[objective] = random.nextDouble();
                        sum += point[objective];
                    }
                    for (int objective = 0; objective < dimensions; objective++) {
                        point[objective] *= 0.5 / sum;
                    }
                }
                assertEquals(bruteForceHypervolume(points, reference),
                        MooCore.hypervolume(points, reference), 1e-9,
                        "dimensions=" + dimensions + ", size=" + size);
            }
        }
    }

    @Test
    void hypervolumeContributionsMatchLeaveOneOutDefinition() {
        Random random = new Random(101);
        for (int dimensions = 2; dimensions <= 5; dimensions++) {
            double[] reference = new double[dimensions];
            Arrays.fill(reference, 10.0);
            for (int iteration = 0; iteration < 20; iteration++) {
                double[][] points = new double[6][dimensions];
                for (double[] point : points) {
                    for (int objective = 0; objective < dimensions; objective++) {
                        point[objective] = 1 + random.nextInt(10);
                    }
                }
                double total = bruteForceHypervolume(points, reference);
                double[] expected = new double[points.length];
                for (int removed = 0; removed < points.length; removed++) {
                    double[][] subset = new double[points.length - 1][];
                    for (int source = 0, target = 0; source < points.length; source++) {
                        if (source != removed) {
                            subset[target++] = points[source];
                        }
                    }
                    expected[removed] = total - bruteForceHypervolume(subset, reference);
                }
                assertArrayEquals(expected,
                        MooCore.hypervolumeContributions(
                                points, reference, new boolean[]{false}, false), 1e-9,
                        "dimensions=" + dimensions + ", iteration=" + iteration
                                + ", points=" + Arrays.deepToString(points));
            }
        }
    }

    @Test
    void computesTwoDimensionalEaf() {
        double[][] points = {
                {3, 2}, {2, 3},
                {2.5, 1}, {1, 2},
                {1, 2}
        };
        int[] sets = {1, 1, 2, 2, 3};
        double[][] result = MooCore.eaf(points, sets, new double[]{0, 50, 100});

        double[][] expected = {
                {1, 2, 0}, {2.5, 1, 0},
                {1, 2, 50},
                {2, 3, 100}, {3, 2, 100}
        };
        assertMatrixEquals(expected, result);
    }

    @Test
    void computesThreeDimensionalEafSurfaces() {
        double[][] points = {{1, 3, 3}, {3, 1, 3}, {3, 3, 1}, {2, 2, 2}};
        int[] sets = {1, 1, 1, 2};
        double[][] actual = MooCore.eaf(points, sets);
        double[][] expected = {
                {1, 3, 3, 50}, {2, 2, 2, 50}, {3, 1, 3, 50}, {3, 3, 1, 50},
                {2, 3, 3, 100}, {3, 2, 3, 100}, {3, 3, 2, 100}
        };
        assertMatrixEquals(expected, actual);
    }

    @Test
    void computesThreeDimensionalEafForOneSet() {
        double[][] points = {{1, 3, 3}, {3, 1, 3}, {3, 3, 1}, {2, 2, 2}};
        int[] sets = {1, 1, 1, 1};
        double[][] expected = {
                {1, 3, 3, 100}, {2, 2, 2, 100}, {3, 1, 3, 100}, {3, 3, 1, 100}
        };
        assertMatrixEquals(expected, MooCore.eaf(points, sets));
    }

    @Test
    void matchesDocumentedEafDifferenceOutputs() {
        double[][] left = {
                {3, 2}, {2, 3},
                {2.5, 1}, {1, 2},
                {1, 2}
        };
        int[] leftSets = {1, 1, 2, 2, 3};
        double[][] right = {
                {4, 2.5}, {3, 3}, {2.5, 3.5},
                {3, 3}, {2.5, 3.5},
                {2, 1}
        };
        int[] rightSets = {1, 1, 1, 2, 2, 3};

        double[][] expectedPoints = {
                {1, 2, 2}, {2, 1, -1}, {2.5, 1, 0},
                {2, 2, 1}, {2, 3, 2}, {3, 2, 2},
                {2.5, 3.5, 0}, {3, 3, 0}, {4, 2.5, 1}
        };
        Dataset leftDataset = new Dataset(left, leftSets);
        Dataset rightDataset = new Dataset(right, rightSets);
        assertMatrixEquals(expectedPoints, MooCore.eafDifference(
                leftDataset, rightDataset, null, new boolean[]{false},
                EafDifferenceFormat.POINTS));

        double[][] expectedRectangles = {
                {2, 1, 2.5, 2, -1},
                {1, 2, 2, Double.POSITIVE_INFINITY, 2},
                {2.5, 1, Double.POSITIVE_INFINITY, 2, 0},
                {2, 2, 3, 3, 1},
                {2, 3.5, 2.5, Double.POSITIVE_INFINITY, 2},
                {2, 3, 3, 3.5, 2},
                {3, 2.5, 4, 3, 2},
                {3, 2, Double.POSITIVE_INFINITY, 2.5, 2},
                {4, 2.5, Double.POSITIVE_INFINITY, 3, 1}
        };
        assertMatrixEquals(expectedRectangles, MooCore.eafDifference(
                leftDataset, rightDataset, null, new boolean[]{false},
                EafDifferenceFormat.RECTANGLES));
    }

    @Test
    void computesWeightedAndRelativeHypervolume() {
        double[][] rectangles = {
                {1, 3, 2, Double.POSITIVE_INFINITY, 1},
                {2, 3.5, 2.5, Double.POSITIVE_INFINITY, 2},
                {2, 3, 3, 3.5, 3}
        };
        assertEquals(4.0, MooCore.weightedHypervolume(
                new double[][]{{2, 2}}, rectangles, new double[]{6}), TOLERANCE);
        assertEquals(26.0, MooCore.totalWeightedHypervolume(
                new double[][]{{2, 2}}, rectangles, new double[]{6}, new double[]{1}, 0.1), TOLERANCE);

        double[][] referenceSet = {{6, 6}, {2, 7}, {7, 2}};
        RelativeHypervolume relative = new RelativeHypervolume(
                new double[]{0}, referenceSet, new boolean[]{true});
        assertEquals(0.025, relative.compute(new double[][]{{5, 5}, {4, 6}, {2, 7}, {7, 4}}), TOLERANCE);
        assertEquals(0.0, relative.compute(referenceSet), TOLERANCE);

        double[][] maximisingRectangles = new double[rectangles.length][5];
        for (int i = 0; i < rectangles.length; i++) {
            maximisingRectangles[i][0] = -rectangles[i][2];
            maximisingRectangles[i][1] = -rectangles[i][3];
            maximisingRectangles[i][2] = -rectangles[i][0];
            maximisingRectangles[i][3] = -rectangles[i][1];
            maximisingRectangles[i][4] = rectangles[i][4];
        }
        assertEquals(4.0, MooCore.weightedHypervolume(
                new double[][]{{-2, -2}}, maximisingRectangles, new double[]{-6},
                new boolean[]{true}), TOLERANCE);
    }

    @Test
    void matchesUpstreamWeightedRectangleSweep() {
        double[][] rectangles = {
                {1, 3, 2, Double.POSITIVE_INFINITY, 1},
                {1, 2, 3, 3, 1},
                {2.5, 1, Double.POSITIVE_INFINITY, 2, 1},
                {2, 3.5, 2.5, Double.POSITIVE_INFINITY, 2},
                {2, 3, 3, 3.5, 2},
                {3, 2.5, 4, 3, 2},
                {3, 2, Double.POSITIVE_INFINITY, 2.5, 2},
                {4, 2.5, Double.POSITIVE_INFINITY, 3, 1}
        };
        assertEquals(9.5, MooCore.weightedHypervolume(
                new double[][]{{2, 2}}, rectangles, new double[]{6}), TOLERANCE);
        assertEquals(12.5, MooCore.weightedHypervolume(
                new double[][]{{1, 1}}, rectangles, new double[]{5}), TOLERANCE);
        assertEquals(0.0, MooCore.weightedHypervolume(
                new double[][]{{3, 3}}, rectangles, new double[]{5}), TOLERANCE);
    }

    @Test
    void weightedRectangleSweepMatchesDefinition() {
        Random random = new Random(61);
        double[] reference = {1, 1};
        for (int iteration = 0; iteration < 100; iteration++) {
            double[][] points = new double[10][2];
            for (double[] point : points) {
                point[0] = random.nextDouble();
                point[1] = random.nextDouble();
            }
            points = MooCore.filterDominated(points);
            double[][] rectangles = new double[8][5];
            for (double[] rectangle : rectangles) {
                rectangle[0] = random.nextDouble();
                rectangle[1] = random.nextDouble();
                rectangle[2] = rectangle[0] + random.nextDouble() * (1.0 - rectangle[0]);
                rectangle[3] = rectangle[1] + random.nextDouble() * (1.0 - rectangle[1]);
                rectangle[4] = random.nextDouble() * 3.0;
            }
            assertEquals(weightedHypervolumeDefinition(points, rectangles, reference),
                    MooCore.weightedHypervolume(points, rectangles, reference), TOLERANCE);
        }
    }

    @Test
    void estimatesHypeWeightedHypervolume() {
        double uniform = MooCore.hypeWeightedHypervolume(
                new double[][]{{2, 2}}, new double[]{4}, new double[]{1});
        assertEquals(4.0, uniform, 0.04);

        double maximising = MooCore.hypeWeightedHypervolume(
                new double[][]{{-2, -2}}, new double[]{-4}, new double[]{-1},
                new boolean[]{true}, 100_000, 0, HypeDistribution.UNIFORM, null);
        assertEquals(uniform, maximising, TOLERANCE);

        double exponential = MooCore.hypeWeightedHypervolume(
                new double[][]{{2, 2}}, new double[]{4}, new double[]{1},
                new boolean[]{false}, 100_000, 42, HypeDistribution.EXPONENTIAL,
                new double[]{0.2});
        assertEquals(1.14624, exponential, 0.04);
        assertEquals(exponential, MooCore.hypeWeightedHypervolume(
                new double[][]{{2, 2}}, new double[]{4}, new double[]{1},
                new boolean[]{false}, 100_000, 42, HypeDistribution.EXPONENTIAL,
                new double[]{0.2}), 0.0);

        double point = MooCore.hypeWeightedHypervolume(
                new double[][]{{2, 2}}, new double[]{4}, new double[]{1},
                new boolean[]{false}, 100_000, 42, HypeDistribution.POINT,
                new double[]{2.9, 0.9});
        assertEquals(0.64485, point, 0.04);
    }

    @Test
    void handlesEmptyPointCollectionsWhereDefined() {
        assertArrayEquals(new boolean[0], MooCore.isNondominated(new double[0][]));
        for (HypervolumeApproximation method : HypervolumeApproximation.values()) {
            assertEquals(0.0, MooCore.approximateHypervolume(
                    new double[0][], new double[]{4, 4}, new boolean[]{false}, 10, 0, method));
        }
        assertArrayEquals(new double[0],
                MooCore.hypervolumeContributions(new double[0][], new double[]{10, 10}));
        assertEquals(0.0, MooCore.hypervolume(new double[0][], new double[]{10, 10}));

        double[][] rectangles = {{1, 1, 3, 3, 2}};
        assertEquals(0.0, MooCore.weightedHypervolume(
                new double[0][], rectangles, new double[]{4}));
        assertEquals(0.0, MooCore.totalWeightedHypervolume(
                new double[0][], rectangles, new double[]{4}, new double[]{1}, 0.1));
        assertThrows(IllegalArgumentException.class, () -> MooCore.totalWeightedHypervolume(
                new double[0][], rectangles, new double[]{4}));

        assertEquals(0.0, MooCore.hypeWeightedHypervolume(
                new double[0][], new double[]{4}, new double[]{1}, new boolean[]{false},
                10, 0, HypeDistribution.UNIFORM, null));
        assertEquals(0.0, MooCore.hypeWeightedHypervolume(
                new double[0][], new double[]{4}, new double[]{1}, new boolean[]{false},
                10, 0, HypeDistribution.EXPONENTIAL, new double[]{0.2}));
        assertEquals(0.0, MooCore.hypeWeightedHypervolume(
                new double[0][], new double[]{4}, new double[]{1}, new boolean[]{false},
                10, 0, HypeDistribution.POINT, new double[]{2, 2}));
        assertThrows(IllegalArgumentException.class, () -> MooCore.anyDominated(new double[0][]));
    }

    @Test
    void parsesBlankLineSeparatedDatasets() throws Exception {
        String text = "0.5 0.5\n\n1 0\n0 1\n\n0.5 0.5\n";
        Dataset dataset = MooCore.readDataset(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));

        assertArrayEquals(new int[]{1, 2, 2, 3}, dataset.sets());
        assertMatrixEquals(new double[][]{{0.5, 0.5}, {1, 0}, {0, 1}, {0.5, 0.5}},
                dataset.points());
    }

    @Test
    void loadsBundledDatasetThroughVersionedCache() throws Exception {
        String oldCache = System.getProperty("moocore.cache");
        try {
            System.setProperty("moocore.cache", java.nio.file.Files.createTempDirectory("moocore-test").toString());
            Dataset dataset = MooCore.getDataset("input1.dat");
            assertEquals(100, dataset.points().length);
            assertEquals(10, Arrays.stream(dataset.sets()).distinct().count());
            assertEquals(93.55331425585321,
                    MooCore.hypervolume(dataset.points(), new double[]{10, 10}), TOLERANCE);

            Dataset highDimensional = MooCore.getDataset("ran.10pts.9d.10");
            assertEquals(100, highDimensional.points().length);
            assertEquals(9, highDimensional.points()[0].length);
            assertEquals(10, Arrays.stream(highDimensional.sets()).distinct().count());

        } finally {
            if (oldCache == null) {
                System.clearProperty("moocore.cache");
            } else {
                System.setProperty("moocore.cache", oldCache);
            }
        }
    }

    @Test
    void computesVorobevExpectationAndDeviation() {
        double[][] points = {{1, 3}, {3, 1}, {2, 2}};
        int[] sets = {1, 1, 2};

        VorobevResult result = MooCore.vorobev(points, sets, new double[]{4});
        assertEquals(62.5, result.threshold(), TOLERANCE);
        assertEquals(4.5, result.averageHypervolume(), TOLERANCE);
        assertMatrixEquals(new double[][]{{2, 3}, {3, 2}}, result.expectation());
        assertEquals(1.5, MooCore.vorobevDeviation(points, sets, new double[]{4}), TOLERANCE);
    }

    @Test
    void generatesReproducibleNondominatedSets() {
        double[][] first = MooCore.generateNondominatedSet(20, 4, NondominatedSetShape.SIMPLEX, 42);
        double[][] second = MooCore.generateNondominatedSet(20, 4, NondominatedSetShape.SIMPLEX, 42);
        assertMatrixEquals(first, second);
        assertFalse(MooCore.anyDominated(first));

        RandomGenerator expectedGenerator = RandomGeneratorFactory
                .<RandomGenerator>of("Xoroshiro128PlusPlus").create(42);
        double[][] expected = MooCore.generateNondominatedSet(
                20, 4, NondominatedSetShape.SIMPLEX, expectedGenerator);
        assertMatrixEquals(expected, first);

        long[][] integers = MooCore.generateIntegerNondominatedSet(20, 4,
                NondominatedSetShape.CONCAVE_SPHERE, 42);
        double[][] asDouble = Arrays.stream(integers)
                .map(row -> Arrays.stream(row).asDoubleStream().toArray())
                .toArray(double[][]::new);
        assertFalse(MooCore.anyDominated(asDouble));
    }

    @Test
    void validatesInvalidInputs() {
        assertThrows(IllegalArgumentException.class,
                () -> MooCore.hypervolume(new double[][]{{1, 2}, {3}}, new double[]{4, 4}));
        assertThrows(IllegalArgumentException.class,
                () -> MooCore.epsilonMultiplicative(new double[][]{{0, 1}}, new double[][]{{1, 1}}));
        assertThrows(UnsupportedOperationException.class,
                () -> MooCore.exactR2(new double[][]{{1, 2, 3}}, new double[]{0, 0, 0}));
    }

    private static double bruteForceHypervolume(double[][] points, double[] reference) {
        int combinations = 1 << points.length;
        double result = 0.0;
        for (int mask = 1; mask < combinations; mask++) {
            double volume = 1.0;
            int selected = 0;
            for (int objective = 0; objective < reference.length; objective++) {
                double lower = Double.NEGATIVE_INFINITY;
                for (int point = 0; point < points.length; point++) {
                    if ((mask & (1 << point)) != 0) {
                        lower = Math.max(lower, points[point][objective]);
                        if (objective == 0) {
                            selected++;
                        }
                    }
                }
                volume *= Math.max(0.0, reference[objective] - lower);
            }
            result += selected % 2 == 1 ? volume : -volume;
        }
        return result;
    }

    private static double bruteForceHypervolume(double[][] points, double[] reference,
                                                 boolean[] maximise) {
        double[][] minimising = copy(points);
        double[] minimisingReference = reference.clone();
        for (int objective = 0; objective < reference.length; objective++) {
            boolean direction = maximise.length == 1 ? maximise[0] : maximise[objective];
            if (direction) {
                minimisingReference[objective] = -minimisingReference[objective];
                for (double[] point : minimising) {
                    point[objective] = -point[objective];
                }
            }
        }
        return bruteForceHypervolume(minimising, minimisingReference);
    }

    private static double naiveAdditiveEpsilon(double[][] points, double[][] reference,
                                                boolean[] maximise) {
        double result = Double.NEGATIVE_INFINITY;
        for (double[] target : reference) {
            double bestPoint = Double.POSITIVE_INFINITY;
            for (double[] point : points) {
                double worstObjective = Double.NEGATIVE_INFINITY;
                for (int objective = 0; objective < point.length; objective++) {
                    boolean direction = maximise.length == 1
                            ? maximise[0]
                            : maximise[objective];
                    double difference = direction
                            ? target[objective] - point[objective]
                            : point[objective] - target[objective];
                    worstObjective = Math.max(worstObjective, difference);
                }
                bestPoint = Math.min(bestPoint, worstObjective);
            }
            result = Math.max(result, bestPoint);
        }
        return result;
    }

    private static boolean[] naiveNondominated(double[][] points, boolean keepWeakly) {
        boolean[] result = new boolean[points.length];
        Arrays.fill(result, true);
        for (int i = 0; i < points.length; i++) {
            for (int j = 0; j < points.length; j++) {
                if (i == j) {
                    continue;
                }
                boolean dominates = true;
                boolean strict = false;
                for (int objective = 0; objective < points[i].length; objective++) {
                    dominates &= points[j][objective] <= points[i][objective];
                    strict |= points[j][objective] < points[i][objective];
                }
                if (dominates && (strict || (!keepWeakly && j < i))) {
                    result[i] = false;
                    break;
                }
            }
        }
        return result;
    }

    private static int[] naiveRanks(double[][] points) {
        int[] ranks = new int[points.length];
        boolean[] remaining = new boolean[points.length];
        Arrays.fill(remaining, true);
        int left = points.length;
        int rank = 0;
        while (left > 0) {
            double[][] subset = new double[left][];
            int[] indices = new int[left];
            int next = 0;
            for (int i = 0; i < points.length; i++) {
                if (remaining[i]) {
                    subset[next] = points[i];
                    indices[next++] = i;
                }
            }
            boolean[] front = naiveNondominated(subset, true);
            for (int i = 0; i < front.length; i++) {
                if (front[i]) {
                    ranks[indices[i]] = rank;
                    remaining[indices[i]] = false;
                    left--;
                }
            }
            rank++;
        }
        return ranks;
    }

    private static void assertParetoOperationsMatchNaive(double[][] points, boolean[] maximise) {
        double[][] minimising = copy(points);
        for (int objective = 0; objective < minimising[0].length; objective++) {
            boolean direction = maximise.length == 1 ? maximise[0] : maximise[objective];
            if (direction) {
                for (double[] point : minimising) {
                    point[objective] = -point[objective];
                }
            }
        }

        boolean[] strictFront = naiveNondominated(minimising, false);
        boolean[] weakFront = naiveNondominated(minimising, true);
        assertArrayEquals(strictFront, MooCore.isNondominated(points, maximise, false));
        assertArrayEquals(weakFront, MooCore.isNondominated(points, maximise, true));
        assertEquals(hasFalse(strictFront), MooCore.anyDominated(points, maximise, false));
        assertEquals(hasFalse(weakFront), MooCore.anyDominated(points, maximise, true));
        assertArrayEquals(naiveRanks(minimising), MooCore.paretoRank(points, maximise));
    }

    private static boolean hasFalse(boolean[] values) {
        for (boolean value : values) {
            if (!value) {
                return true;
            }
        }
        return false;
    }

    private static double[][] rotateObjectives(double[][] points, int shift) {
        double[][] result = new double[points.length][points[0].length];
        for (int row = 0; row < points.length; row++) {
            for (int objective = 0; objective < points[row].length; objective++) {
                result[row][objective] = points[row][(objective + shift) % points[row].length];
            }
        }
        return result;
    }

    private static boolean[] rotateDirections(boolean[] directions, int shift) {
        boolean[] result = new boolean[directions.length];
        for (int objective = 0; objective < directions.length; objective++) {
            result[objective] = directions[(objective + shift) % directions.length];
        }
        return result;
    }

    private static double weightedHypervolumeDefinition(double[][] points, double[][] rectangles,
                                                         double[] reference) {
        double result = 0.0;
        for (double[] rectangle : rectangles) {
            double upperX = Math.min(rectangle[2], reference[0]);
            double upperY = Math.min(rectangle[3], reference[1]);
            double lowerX = Math.min(rectangle[0], reference[0]);
            double lowerY = Math.min(rectangle[1], reference[1]);
            if (lowerX == upperX || lowerY == upperY) {
                continue;
            }
            double[][] clipped = new double[points.length][2];
            for (int point = 0; point < points.length; point++) {
                clipped[point][0] = Math.max(points[point][0], lowerX);
                clipped[point][1] = Math.max(points[point][1], lowerY);
            }
            result += rectangle[4] * MooCore.hypervolume(
                    clipped, new double[]{upperX, upperY});
        }
        return result;
    }

    private static double[][] copy(double[][] matrix) {
        return Arrays.stream(matrix).map(double[]::clone).toArray(double[][]::new);
    }

    private static void assertMatrixEquals(double[][] expected, double[][] actual) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertArrayEquals(expected[i], actual[i], TOLERANCE, "row " + i);
        }
    }
}
