// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import es.urjc.etsii.grafo.moocore.HypeDistribution;

import java.util.Arrays;
import java.util.random.RandomGenerator;

public final class WeightedHypervolumeAlgorithms {

    private WeightedHypervolumeAlgorithms() {
    }

    public static double rectangles(double[][] points, double[][] rectangles,
                                    double[] reference, boolean[] maximise) {
        int dimensions = validateTwoDimensionalPoints(points);
        boolean[] directions = MatrixUtils.directions(maximise, dimensions);
        if (rectangles == null) {
            throw new IllegalArgumentException("rectangles cannot be null");
        }
        double[][] data = MatrixUtils.toMinimisation(points, directions);
        double[] ref = MatrixUtils.toMinimisation(
                MatrixUtils.vector(reference, dimensions, "reference"), directions);
        double[][] processedRectangles = new double[rectangles.length][];
        int rectangleCount = 0;
        for (double[] rectangle : rectangles) {
            if (rectangle == null || rectangle.length != 5) {
                throw new IllegalArgumentException("each rectangle must contain lower x/y, upper x/y, and weight");
            }
            double[] transformed = rectangle.clone();
            for (int objective = 0; objective < dimensions; objective++) {
                if (directions[objective]) {
                    transformed[objective] = -rectangle[objective + dimensions];
                    transformed[objective + dimensions] = -rectangle[objective];
                }
            }
            for (int coordinate = 0; coordinate < dimensions * 2; coordinate++) {
                transformed[coordinate] = Math.min(transformed[coordinate], ref[coordinate % dimensions]);
            }
            if (transformed[0] != transformed[2] && transformed[1] != transformed[3]) {
                processedRectangles[rectangleCount++] = transformed;
            }
        }
        if (data.length == 0 || rectangleCount == 0) {
            return 0.0;
        }
        processedRectangles = Arrays.copyOf(processedRectangles, rectangleCount);
        return rectangleSweep(data, processedRectangles);
    }

    public static double total(double[][] points, double[][] rectangles, double[] reference,
                               boolean[] maximise, double[] configuredIdeal, double scaleFactor) {
        if (!(scaleFactor > 0.0 && scaleFactor <= 1.0)) {
            throw new IllegalArgumentException("scaleFactor must be in (0, 1]");
        }
        int dimensions = validateTwoDimensionalPoints(points);
        double[] ref = MatrixUtils.vector(reference, dimensions, "reference");
        double[] ideal;
        if (configuredIdeal == null || configuredIdeal.length == 0) {
            if (points.length == 0) {
                throw new IllegalArgumentException("ideal cannot be inferred from an empty front");
            }
            boolean[] directions = MatrixUtils.directions(maximise, dimensions);
            ideal = new double[dimensions];
            for (int objective = 0; objective < dimensions; objective++) {
                ideal[objective] = extreme(points, objective, directions[objective]);
            }
        } else {
            ideal = MatrixUtils.vector(configuredIdeal, dimensions, "ideal");
        }
        double beta = scaleFactor * Math.abs((ref[0] - ideal[0]) * (ref[1] - ideal[1]));
        return HypervolumeAlgorithms.hypervolume(points, ref, maximise)
                + beta * rectangles(points, rectangles, ref, maximise);
    }

    public static double hype(double[][] input, double[] reference, double[] ideal,
                              boolean[] maximise, int samples, long seed,
                              HypeDistribution distribution, double[] mu) {
        int dimensions = validateTwoDimensionalPoints(input);
        if (samples <= 0) {
            throw new IllegalArgumentException("samples must be positive");
        }
        if (distribution == null) {
            throw new IllegalArgumentException("distribution cannot be null");
        }
        boolean[] directions = MatrixUtils.directions(maximise, dimensions);
        double[][] points = MatrixUtils.toMinimisation(input, directions);
        double[] ref = MatrixUtils.toMinimisation(MatrixUtils.vector(reference, dimensions, "reference"), directions);
        double[] idealPoint = MatrixUtils.toMinimisation(MatrixUtils.vector(ideal, dimensions, "ideal"), directions);
        double[] parameter = mu == null ? null : MatrixUtils.toMinimisation(
                MatrixUtils.vector(mu, distribution == HypeDistribution.EXPONENTIAL ? 1 : dimensions, "mu"),
                distribution == HypeDistribution.EXPONENTIAL ? new boolean[]{false} : directions);
        if (distribution == HypeDistribution.EXPONENTIAL && (parameter == null || parameter[0] <= 0.0)) {
            throw new IllegalArgumentException("exponential distribution requires a positive mu");
        }
        if (distribution == HypeDistribution.POINT && parameter == null) {
            throw new IllegalArgumentException("point distribution requires a two-dimensional mu");
        }
        if (distribution == HypeDistribution.UNIFORM && parameter != null) {
            throw new IllegalArgumentException("uniform distribution does not accept mu");
        }
        if (points.length == 0) {
            return 0.0;
        }

        double[][] normalized = new double[points.length][2];
        for (int i = 0; i < points.length; i++) {
            for (int objective = 0; objective < 2; objective++) {
                normalized[i][objective] = (points[i][objective] - idealPoint[objective])
                        / (ref[objective] - idealPoint[objective]);
            }
        }
        double[] normalizedMu = null;
        if (distribution == HypeDistribution.POINT) {
            normalizedMu = new double[2];
            for (int objective = 0; objective < 2; objective++) {
                normalizedMu[objective] = (parameter[objective] - idealPoint[objective])
                        / (ref[objective] - idealPoint[objective]);
            }
        }

        RandomGenerator random = RandomGenerators.create(seed);
        int dominated = 0;
        for (int sample = 0; sample < samples; sample++) {
            double x;
            double y;
            switch (distribution) {
                case UNIFORM -> {
                    x = random.nextDouble();
                    y = random.nextDouble();
                }
                case EXPONENTIAL -> {
                    if (sample < samples / 2) {
                        x = -parameter[0] * Math.log(Math.max(random.nextDouble(), Double.MIN_VALUE));
                        y = random.nextDouble();
                    } else {
                        x = random.nextDouble();
                        y = -parameter[0] * Math.log(Math.max(random.nextDouble(), Double.MIN_VALUE));
                    }
                }
                case POINT -> {
                    double gaussian = random.nextGaussian();
                    // The upstream bivariate sampler also draws the zero-weight residual.
                    random.nextGaussian();
                    x = normalizedMu[0] + 0.25 * gaussian;
                    y = normalizedMu[1] + 0.25 * gaussian;
                }
                default -> throw new IllegalStateException("unexpected distribution");
            }
            if (isDominated(x, y, normalized)) {
                dominated++;
            }
        }
        double volume = (ref[0] - idealPoint[0]) * (ref[1] - idealPoint[1]);
        return volume * dominated / samples;
    }

    private static int validateTwoDimensionalPoints(double[][] points) {
        if (points == null) {
            throw new IllegalArgumentException("points cannot be null");
        }
        return points.length == 0 ? 2 : MatrixUtils.validate(points, 2, 2);
    }

    private static double rectangleSweep(double[][] points, double[][] rectangles) {
        Arrays.sort(points, (left, right) -> {
            int byY = Double.compare(right[1], left[1]);
            return byY != 0 ? byY : Double.compare(left[0], right[0]);
        });
        Arrays.sort(rectangles, (left, right) -> {
            int byUpperY = Double.compare(right[3], left[3]);
            return byUpperY != 0 ? byUpperY : Double.compare(left[2], right[2]);
        });

        double lastTop = rectangles[rectangles.length - 1][3];
        double lastRight = -Double.MAX_VALUE;
        for (double[] rectangle : rectangles) {
            lastRight = Math.max(lastRight, rectangle[2]);
        }

        int pointIndex = 0;
        double[] point = points[pointIndex];
        double top = rectangles[0][3];
        while (point[1] >= rectangles[0][3]) {
            top = point[1];
            pointIndex++;
            if (pointIndex >= points.length || top == lastTop || point[0] >= lastRight) {
                return 0.0;
            }
            point = points[pointIndex];
        }

        double weightedHypervolume = 0.0;
        while (true) {
            int rectangleIndex = 0;
            do {
                double[] rectangle = rectangles[rectangleIndex];
                if (point[0] < rectangle[2] && rectangle[1] < top) {
                    weightedHypervolume += (rectangle[2] - Math.max(point[0], rectangle[0]))
                            * (Math.min(top, rectangle[3]) - Math.max(point[1], rectangle[1]))
                            * rectangle[4];
                }
                rectangleIndex++;
            } while (rectangleIndex < rectangles.length
                    && point[1] < rectangles[rectangleIndex][3]);

            do {
                top = point[1];
                pointIndex++;
                if (pointIndex >= points.length || top == lastTop || point[0] >= lastRight) {
                    return weightedHypervolume;
                }
                point = points[pointIndex];
            } while (top == point[1] && point[1] >= rectangles[0][3]);
        }
    }

    private static boolean isDominated(double x, double y, double[][] points) {
        for (double[] point : points) {
            if (point[0] <= x && point[1] <= y) {
                return true;
            }
        }
        return false;
    }

    private static double extreme(double[][] points, int objective, boolean maximise) {
        double result = maximise ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        for (double[] point : points) {
            result = maximise ? Math.max(result, point[objective]) : Math.min(result, point[objective]);
        }
        return result;
    }
}
