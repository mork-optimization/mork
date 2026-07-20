// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import java.util.Arrays;

public final class Indicators {

    private Indicators() {
    }

    public static double igd(double[][] input, double[][] reference, boolean[] maximise) {
        PreparedSets sets = prepare(input, reference, maximise, false);
        return directedDistance(sets.points, sets.reference, 1, false);
    }

    public static double igdPlus(double[][] input, double[][] reference, boolean[] maximise) {
        PreparedSets sets = prepare(input, reference, maximise, false);
        double sum = 0.0;
        for (double[] target : sets.reference) {
            double bestSquared = Double.POSITIVE_INFINITY;
            for (double[] point : sets.points) {
                double squared = 0.0;
                for (int objective = 0; objective < point.length; objective++) {
                    double distance = Math.max(point[objective] - target[objective], 0.0);
                    squared += distance * distance;
                }
                bestSquared = Math.min(bestSquared, squared);
            }
            sum += Math.sqrt(bestSquared);
        }
        return sum / sets.reference.length;
    }

    public static double averageHausdorffDistance(double[][] input, double[][] reference,
                                                   boolean[] maximise, int p) {
        if (p <= 0) {
            throw new IllegalArgumentException("p must be positive");
        }
        PreparedSets sets = prepare(input, reference, maximise, false);
        return Math.max(directedDistance(sets.points, sets.reference, p, false),
                directedDistance(sets.reference, sets.points, p, false));
    }

    public static double epsilonAdditive(double[][] input, double[][] reference, boolean[] maximise) {
        PreparedSets sets = prepare(input, reference, maximise, false);
        double result = Double.NEGATIVE_INFINITY;
        for (double[] target : sets.reference) {
            double bestPoint = Double.POSITIVE_INFINITY;
            for (double[] point : sets.points) {
                double worstObjective = Double.NEGATIVE_INFINITY;
                for (int objective = 0; objective < point.length; objective++) {
                    worstObjective = Math.max(worstObjective, point[objective] - target[objective]);
                }
                bestPoint = Math.min(bestPoint, worstObjective);
            }
            result = Math.max(result, bestPoint);
        }
        return result;
    }

    public static double epsilonMultiplicative(double[][] input, double[][] reference,
                                                boolean[] maximise) {
        int objectives = MatrixUtils.validate(input, 1);
        MatrixUtils.validate(reference, 1);
        if (reference[0].length != objectives) {
            throw new IllegalArgumentException("points and reference must have the same number of objectives");
        }
        boolean[] directions = MatrixUtils.directions(maximise, objectives);
        for (double[] point : input) {
            for (double value : point) {
                if (value <= 0.0) {
                    throw new IllegalArgumentException("multiplicative epsilon requires positive values");
                }
            }
        }
        for (double[] point : reference) {
            for (double value : point) {
                if (value <= 0.0) {
                    throw new IllegalArgumentException("multiplicative epsilon requires positive values");
                }
            }
        }

        double result = Double.NEGATIVE_INFINITY;
        for (double[] target : reference) {
            double bestPoint = Double.POSITIVE_INFINITY;
            for (double[] point : input) {
                double worstObjective = Double.NEGATIVE_INFINITY;
                for (int objective = 0; objective < objectives; objective++) {
                    double ratio = directions[objective]
                            ? target[objective] / point[objective]
                            : point[objective] / target[objective];
                    worstObjective = Math.max(worstObjective, ratio);
                }
                bestPoint = Math.min(bestPoint, worstObjective);
            }
            result = Math.max(result, bestPoint);
        }
        return result;
    }

    public static double exactR2(double[][] input, double[] reference, boolean[] maximise) {
        int objectives = MatrixUtils.validate(input, 2, 2);
        boolean[] directions = MatrixUtils.directions(maximise, objectives);
        double[][] points = MatrixUtils.toMinimisation(input, directions);
        double[] ideal = MatrixUtils.toMinimisation(MatrixUtils.vector(reference, objectives, "reference"), directions);
        Arrays.sort(points, (left, right) -> {
            int comparison = Double.compare(left[0], right[0]);
            return comparison != 0 ? comparison : Double.compare(left[1], right[1]);
        });

        int index = 0;
        while (index < points.length && points[index][0] < ideal[0]) {
            index++;
        }
        if (index == points.length) {
            return points[index - 1][1] <= ideal[1] ? 0.0 : Double.POSITIVE_INFINITY;
        }

        double previousFirst = points[index][0] - ideal[0];
        double previousSecond = points[index][1] - ideal[1];
        if (previousSecond < 0.0) {
            return 0.0;
        }
        double result = utility(previousFirst, previousSecond, Double.POSITIVE_INFINITY);
        while (++index < points.length) {
            double first = points[index][0] - ideal[0];
            double second = points[index][1] - ideal[1];
            if (second >= 0.0 && second < previousSecond) {
                result += utility(previousSecond, previousFirst, first)
                        + utility(first, second, previousSecond);
                previousFirst = first;
                previousSecond = second;
            }
        }
        result += utility(previousSecond, previousFirst, Double.POSITIVE_INFINITY);
        return result / 2.0;
    }

    private static PreparedSets prepare(double[][] input, double[][] reference,
                                        boolean[] maximise, boolean positive) {
        int objectives = MatrixUtils.validate(input, 1);
        MatrixUtils.validate(reference, 1);
        if (reference[0].length != objectives) {
            throw new IllegalArgumentException("points and reference must have the same number of objectives");
        }
        if (positive) {
            for (double[][] values : new double[][][]{input, reference}) {
                for (double[] point : values) {
                    for (double value : point) {
                        if (value <= 0.0) {
                            throw new IllegalArgumentException("all objective values must be positive");
                        }
                    }
                }
            }
        }
        boolean[] directions = MatrixUtils.directions(maximise, objectives);
        return new PreparedSets(MatrixUtils.toMinimisation(input, directions),
                MatrixUtils.toMinimisation(reference, directions));
    }

    private static double directedDistance(double[][] points, double[][] targets, int p, boolean plus) {
        double sum = 0.0;
        for (double[] target : targets) {
            double best = Double.POSITIVE_INFINITY;
            for (double[] point : points) {
                double powered = 0.0;
                for (int objective = 0; objective < point.length; objective++) {
                    double difference = point[objective] - target[objective];
                    if (plus) {
                        difference = Math.max(difference, 0.0);
                    }
                    powered += Math.pow(Math.abs(difference), 2.0);
                }
                double euclidean = Math.sqrt(powered);
                best = Math.min(best, Math.pow(euclidean, p));
            }
            sum += best;
        }
        return Math.pow(sum / targets.length, 1.0 / p);
    }

    private static double utility(double y1, double y2, double y2Prime) {
        if (y1 == 0.0) {
            return 0.0;
        }
        double w = y2 / (y1 + y2);
        double wPrime = Double.isInfinite(y2Prime) ? 1.0 : y2Prime / (y1 + y2Prime);
        return y1 * (wPrime * wPrime - w * w);
    }

    private record PreparedSets(double[][] points, double[][] reference) {
    }
}
