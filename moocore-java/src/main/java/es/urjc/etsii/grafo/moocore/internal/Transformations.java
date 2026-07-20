// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import es.urjc.etsii.grafo.moocore.NondominatedSetShape;

import java.util.random.RandomGenerator;

public final class Transformations {

    private Transformations() {
    }

    public static double[][] normalise(double[][] input, double[] targetRange, double[] lower,
                                        double[] upper, boolean[] maximise) {
        int objectives = MatrixUtils.validate(input, 2, 255);
        if (targetRange == null || targetRange.length != 2) {
            throw new IllegalArgumentException("targetRange must contain two values");
        }
        boolean[] directions = MatrixUtils.directions(maximise, objectives);
        double[] low = bounds(lower, input, objectives, true);
        double[] high = bounds(upper, input, objectives, false);
        double[][] result = new double[input.length][objectives];
        for (int row = 0; row < input.length; row++) {
            for (int objective = 0; objective < objectives; objective++) {
                double start = directions[objective] ? targetRange[1] : targetRange[0];
                double end = directions[objective] ? targetRange[0] : targetRange[1];
                double denominator = high[objective] - low[objective];
                if (denominator == 0.0) {
                    result[row][objective] = start;
                } else {
                    double fraction = (input[row][objective] - low[objective]) / denominator;
                    result[row][objective] = start + fraction * (end - start);
                }
            }
        }
        return result;
    }

    public static double[][] generate(int size, int objectives, NondominatedSetShape shape,
                                      RandomGenerator random) {
        if (size <= 0 || objectives <= 0) {
            throw new IllegalArgumentException("size and objectives must be positive");
        }
        if (shape == null || random == null) {
            throw new IllegalArgumentException("shape and random cannot be null");
        }
        while (true) {
            double[][] result = new double[size][objectives];
            for (int row = 0; row < size; row++) {
                if (shape == NondominatedSetShape.SIMPLEX
                        || shape == NondominatedSetShape.CONVEX_SIMPLEX
                        || shape == NondominatedSetShape.INVERTED_SIMPLEX
                        || shape == NondominatedSetShape.CONCAVE_SIMPLEX) {
                    sampleSimplex(result[row], random);
                } else {
                    sampleSphere(result[row], random);
                }
                transformShape(result[row], shape);
            }
            if (!ParetoAlgorithms.anyDominated(result, new boolean[]{false}, false)) {
                return result;
            }
        }
    }

    public static long[][] generateInteger(int size, int objectives, NondominatedSetShape shape,
                                           RandomGenerator random) {
        double[][] points = generate(size, objectives, shape, random);
        double scale = 64.0;
        while (true) {
            double[][] rounded = new double[size][objectives];
            long[][] result = new long[size][objectives];
            for (int row = 0; row < size; row++) {
                for (int objective = 0; objective < objectives; objective++) {
                    long value = (long) (points[row][objective] * scale);
                    result[row][objective] = value;
                    rounded[row][objective] = value;
                }
            }
            if (!ParetoAlgorithms.anyDominated(rounded, new boolean[]{false}, false)) {
                return result;
            }
            scale *= 2.0;
            if (!Double.isFinite(scale)) {
                throw new ArithmeticException("unable to produce a nondominated integer set");
            }
        }
    }

    private static double[] bounds(double[] configured, double[][] points, int objectives, boolean minimum) {
        double[] result;
        if (configured == null || configured.length == 0) {
            result = new double[objectives];
            for (int objective = 0; objective < objectives; objective++) {
                result[objective] = minimum ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
            }
        } else {
            result = MatrixUtils.vector(configured, objectives, minimum ? "lower" : "upper");
        }
        for (int objective = 0; objective < objectives; objective++) {
            if (configured == null || configured.length == 0 || Double.isNaN(result[objective])) {
                double bound = minimum ? Double.POSITIVE_INFINITY : Double.NEGATIVE_INFINITY;
                for (double[] point : points) {
                    bound = minimum ? Math.min(bound, point[objective]) : Math.max(bound, point[objective]);
                }
                result[objective] = bound;
            }
        }
        return result;
    }

    private static void sampleSimplex(double[] point, RandomGenerator random) {
        double sum = 0.0;
        for (int i = 0; i < point.length; i++) {
            double value = -Math.log1p(-random.nextDouble());
            point[i] = value;
            sum += value;
        }
        for (int i = 0; i < point.length; i++) {
            point[i] /= sum;
        }
    }

    private static void sampleSphere(double[] point, RandomGenerator random) {
        double norm = 0.0;
        for (int i = 0; i < point.length; i++) {
            double value = Math.abs(random.nextGaussian());
            point[i] = value;
            norm += value * value;
        }
        norm = Math.sqrt(norm);
        for (int i = 0; i < point.length; i++) {
            point[i] /= norm;
        }
    }

    private static void transformShape(double[] point, NondominatedSetShape shape) {
        for (int i = 0; i < point.length; i++) {
            point[i] = switch (shape) {
                case SIMPLEX, CONCAVE_SPHERE -> point[i];
                case CONVEX_SPHERE, INVERTED_SIMPLEX -> 1.0 - point[i];
                case CONVEX_SIMPLEX -> point[i] * point[i];
                case CONCAVE_SIMPLEX -> 1.0 - point[i] * point[i];
            };
        }
    }
}
