// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

public final class Indicators {

    private Indicators() {
    }

    public static double igd(double[][] input, double[][] reference, boolean[] maximise) {
        PreparedSets sets = prepare(input, reference, maximise);
        return directedDistance(sets.points, sets.reference, 1);
    }

    public static double igdPlus(double[][] input, double[][] reference, boolean[] maximise) {
        PreparedSets sets = prepare(input, reference, maximise);
        return directedDistancePlus(sets.points, sets.reference);
    }

    public static double averageHausdorffDistance(double[][] input, double[][] reference,
                                                   boolean[] maximise, int p) {
        if (p <= 0) {
            throw new IllegalArgumentException("p must be positive");
        }
        PreparedSets sets = prepare(input, reference, maximise);
        return Math.max(directedDistance(sets.points, sets.reference, p),
                directedDistance(sets.reference, sets.points, p));
    }

    public static double epsilonAdditive(double[][] input, double[][] reference, boolean[] maximise) {
        int objectives = MatrixUtils.validate(input, 1);
        MatrixUtils.validate(reference, 1);
        if (reference[0].length != objectives) {
            throw new IllegalArgumentException("points and reference must have the same number of objectives");
        }
        boolean[] directions = MatrixUtils.directions(maximise, objectives);
        if (objectives == 1) {
            return epsilonAdditive1d(input, reference, directions[0]);
        }

        boolean anyMinimise = false;
        boolean anyMaximise = false;
        for (boolean direction : directions) {
            anyMinimise |= !direction;
            anyMaximise |= direction;
        }
        if (!anyMaximise) {
            return epsilonAdditiveMinimise(input, reference);
        }
        if (!anyMinimise) {
            return epsilonAdditiveMaximise(input, reference);
        }
        return epsilonAdditiveMixed(input, reference, directions);
    }

    private static double epsilonAdditive1d(double[][] points, double[][] reference,
                                             boolean maximise) {
        double result = Double.NEGATIVE_INFINITY;
        for (double[] target : reference) {
            double bestPoint = Double.POSITIVE_INFINITY;
            for (double[] point : points) {
                double difference = maximise
                        ? target[0] - point[0]
                        : point[0] - target[0];
                bestPoint = Math.min(bestPoint, difference);
            }
            result = Math.max(result, bestPoint);
        }
        return result;
    }

    private static double epsilonAdditiveMinimise(double[][] points, double[][] reference) {
        double result = Double.NEGATIVE_INFINITY;
        targetLoop:
        for (double[] target : reference) {
            double bestPoint = Double.POSITIVE_INFINITY;
            for (double[] point : points) {
                double worstObjective = Math.max(
                        point[0] - target[0], point[1] - target[1]);
                if (worstObjective >= bestPoint) {
                    continue;
                }
                for (int objective = 2; objective < point.length; objective++) {
                    worstObjective = Math.max(
                            worstObjective, point[objective] - target[objective]);
                }
                if (worstObjective <= result) {
                    continue targetLoop;
                }
                bestPoint = Math.min(bestPoint, worstObjective);
            }
            result = Math.max(result, bestPoint);
        }
        return result;
    }

    private static double epsilonAdditiveMaximise(double[][] points, double[][] reference) {
        double result = Double.NEGATIVE_INFINITY;
        targetLoop:
        for (double[] target : reference) {
            double bestPoint = Double.POSITIVE_INFINITY;
            for (double[] point : points) {
                double worstObjective = Math.max(
                        target[0] - point[0], target[1] - point[1]);
                if (worstObjective >= bestPoint) {
                    continue;
                }
                for (int objective = 2; objective < point.length; objective++) {
                    worstObjective = Math.max(
                            worstObjective, target[objective] - point[objective]);
                }
                if (worstObjective <= result) {
                    continue targetLoop;
                }
                bestPoint = Math.min(bestPoint, worstObjective);
            }
            result = Math.max(result, bestPoint);
        }
        return result;
    }

    private static double epsilonAdditiveMixed(double[][] points, double[][] reference,
                                                boolean[] maximise) {
        double result = Double.NEGATIVE_INFINITY;
        targetLoop:
        for (double[] target : reference) {
            double bestPoint = Double.POSITIVE_INFINITY;
            for (double[] point : points) {
                double first = maximise[0]
                        ? target[0] - point[0]
                        : point[0] - target[0];
                double second = maximise[1]
                        ? target[1] - point[1]
                        : point[1] - target[1];
                double worstObjective = Math.max(first, second);
                if (worstObjective >= bestPoint) {
                    continue;
                }
                for (int objective = 2; objective < point.length; objective++) {
                    double difference = maximise[objective]
                            ? target[objective] - point[objective]
                            : point[objective] - target[objective];
                    worstObjective = Math.max(worstObjective, difference);
                }
                if (worstObjective <= result) {
                    continue targetLoop;
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

        if (objectives == 1) {
            return epsilonMultiplicative1d(input, reference, directions[0]);
        }

        boolean anyMinimise = false;
        boolean anyMaximise = false;
        for (boolean direction : directions) {
            anyMinimise |= !direction;
            anyMaximise |= direction;
        }
        if (!anyMaximise) {
            return epsilonMultiplicativeMinimise(input, reference);
        }
        if (!anyMinimise) {
            return epsilonMultiplicativeMaximise(input, reference);
        }
        return epsilonMultiplicativeMixed(input, reference, directions);
    }

    private static double epsilonMultiplicative1d(double[][] points, double[][] reference,
                                                   boolean maximise) {
        double result = Double.NEGATIVE_INFINITY;
        for (double[] target : reference) {
            double bestPoint = Double.POSITIVE_INFINITY;
            for (double[] point : points) {
                double ratio = maximise
                        ? target[0] / point[0]
                        : point[0] / target[0];
                bestPoint = Math.min(bestPoint, ratio);
            }
            result = Math.max(result, bestPoint);
        }
        return result;
    }

    private static double epsilonMultiplicativeMinimise(
            double[][] points, double[][] reference) {
        double result = Double.NEGATIVE_INFINITY;
        targetLoop:
        for (double[] target : reference) {
            double bestPoint = Double.POSITIVE_INFINITY;
            for (double[] point : points) {
                double worstObjective = Math.max(
                        point[0] / target[0], point[1] / target[1]);
                if (worstObjective >= bestPoint) {
                    continue;
                }
                for (int objective = 2; objective < point.length; objective++) {
                    worstObjective = Math.max(
                            worstObjective, point[objective] / target[objective]);
                }
                if (worstObjective <= result) {
                    continue targetLoop;
                }
                bestPoint = Math.min(bestPoint, worstObjective);
            }
            result = Math.max(result, bestPoint);
        }
        return result;
    }

    private static double epsilonMultiplicativeMaximise(
            double[][] points, double[][] reference) {
        double result = Double.NEGATIVE_INFINITY;
        targetLoop:
        for (double[] target : reference) {
            double bestPoint = Double.POSITIVE_INFINITY;
            for (double[] point : points) {
                double worstObjective = Math.max(
                        target[0] / point[0], target[1] / point[1]);
                if (worstObjective >= bestPoint) {
                    continue;
                }
                for (int objective = 2; objective < point.length; objective++) {
                    worstObjective = Math.max(
                            worstObjective, target[objective] / point[objective]);
                }
                if (worstObjective <= result) {
                    continue targetLoop;
                }
                bestPoint = Math.min(bestPoint, worstObjective);
            }
            result = Math.max(result, bestPoint);
        }
        return result;
    }

    private static double epsilonMultiplicativeMixed(
            double[][] points, double[][] reference, boolean[] maximise) {
        double result = Double.NEGATIVE_INFINITY;
        targetLoop:
        for (double[] target : reference) {
            double bestPoint = Double.POSITIVE_INFINITY;
            for (double[] point : points) {
                double first = maximise[0]
                        ? target[0] / point[0]
                        : point[0] / target[0];
                double second = maximise[1]
                        ? target[1] / point[1]
                        : point[1] / target[1];
                double worstObjective = Math.max(first, second);
                if (worstObjective >= bestPoint) {
                    continue;
                }
                for (int objective = 2; objective < point.length; objective++) {
                    double ratio = maximise[objective]
                            ? target[objective] / point[objective]
                            : point[objective] / target[objective];
                    worstObjective = Math.max(worstObjective, ratio);
                }
                if (worstObjective <= result) {
                    continue targetLoop;
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
        double[][] points = toMinimisationIfNeeded(input, directions);
        double[] ideal = MatrixUtils.toMinimisation(MatrixUtils.vector(reference, objectives, "reference"), directions);
        int[] order = KungParetoAlgorithms.sortLexicographically2d(points);

        int index = 0;
        while (index < points.length && points[order[index]][0] < ideal[0]) {
            index++;
        }
        if (index == points.length) {
            return points[order[index - 1]][1] <= ideal[1]
                    ? 0.0
                    : Double.POSITIVE_INFINITY;
        }

        double previousFirst = points[order[index]][0] - ideal[0];
        double previousSecond = points[order[index]][1] - ideal[1];
        if (previousSecond < 0.0) {
            return 0.0;
        }
        double result = utility(previousFirst, previousSecond, Double.POSITIVE_INFINITY);
        while (++index < points.length) {
            double first = points[order[index]][0] - ideal[0];
            double second = points[order[index]][1] - ideal[1];
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
                                        boolean[] maximise) {
        int objectives = MatrixUtils.validate(input, 1);
        MatrixUtils.validate(reference, 1);
        if (reference[0].length != objectives) {
            throw new IllegalArgumentException("points and reference must have the same number of objectives");
        }
        boolean[] directions = MatrixUtils.directions(maximise, objectives);
        return new PreparedSets(toMinimisationIfNeeded(input, directions),
                toMinimisationIfNeeded(reference, directions));
    }

    private static double directedDistance(double[][] points, double[][] targets, int p) {
        double sum = 0.0;
        for (double[] target : targets) {
            double bestSquared = Double.POSITIVE_INFINITY;
            for (double[] point : points) {
                double squared = 0.0;
                for (int objective = 0; objective < point.length; objective++) {
                    double difference = point[objective] - target[objective];
                    squared += difference * difference;
                }
                if (Double.isNaN(squared)) {
                    return Double.NaN;
                }
                if (squared < bestSquared) {
                    bestSquared = squared;
                    if (bestSquared == 0.0) {
                        break;
                    }
                }
            }
            if (p == 1) {
                sum += Math.sqrt(bestSquared);
            } else if (p == 2) {
                sum += bestSquared;
            } else {
                sum += Math.pow(Math.sqrt(bestSquared), p);
            }
        }
        double mean = sum / targets.length;
        if (p == 1) {
            return mean;
        }
        if (p == 2) {
            return Math.sqrt(mean);
        }
        return Math.pow(mean, 1.0 / p);
    }

    private static double directedDistancePlus(double[][] points, double[][] targets) {
        double sum = 0.0;
        for (double[] target : targets) {
            double bestSquared = Double.POSITIVE_INFINITY;
            for (double[] point : points) {
                double squared = 0.0;
                for (int objective = 0; objective < point.length; objective++) {
                    double difference = Math.max(point[objective] - target[objective], 0.0);
                    squared += difference * difference;
                }
                bestSquared = Math.min(bestSquared, squared);
                if (bestSquared == 0.0) {
                    break;
                }
            }
            sum += Math.sqrt(bestSquared);
        }
        return sum / targets.length;
    }

    private static double[][] toMinimisationIfNeeded(double[][] points, boolean[] maximise) {
        for (boolean direction : maximise) {
            if (direction) {
                return MatrixUtils.toMinimisation(points, maximise);
            }
        }
        return points;
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
