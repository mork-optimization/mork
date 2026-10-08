// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import java.util.Arrays;

public final class ParetoAlgorithms {

    private ParetoAlgorithms() {
    }

    public static boolean[] isNondominated(double[][] input, boolean[] maximise, boolean keepWeakly) {
        if (input == null) {
            throw new IllegalArgumentException("points cannot be null");
        }
        if (input.length == 0) {
            return new boolean[0];
        }
        int objectives = MatrixUtils.validate(input, 1, 255);
        boolean[] directions = MatrixUtils.directions(maximise, objectives);
        if (objectives == 1) {
            return nondominated1d(input, directions[0], keepWeakly);
        }
        if (objectives == 2) {
            return nondominated2d(input, directions, keepWeakly);
        }

        FlatPoints points = FlatPoints.toMinimisation(input, directions);
        return KungParetoAlgorithms.nondominated(points, keepWeakly);
    }

    public static boolean anyDominated(double[][] input, boolean[] maximise, boolean keepWeakly) {
        if (input == null || input.length == 0) {
            throw new IllegalArgumentException("points cannot be empty");
        }
        int objectives = MatrixUtils.validate(input, 1, 255);
        boolean[] directions = MatrixUtils.directions(maximise, objectives);
        if (objectives == 1) {
            if (!keepWeakly) {
                return input.length > 1;
            }
            double first = input[0][0];
            for (int row = 1; row < input.length; row++) {
                if (input[row][0] != first) {
                    return true;
                }
            }
            return false;
        }
        if (objectives == 2) {
            return anyDominated2d(input, directions, keepWeakly);
        }

        FlatPoints points = FlatPoints.toMinimisation(input, directions);
        return KungParetoAlgorithms.anyDominated(points, keepWeakly);
    }

    public static int[] ranks(double[][] input, boolean[] maximise) {
        if (input == null) {
            throw new IllegalArgumentException("points cannot be null");
        }
        if (input.length == 0) {
            return new int[0];
        }
        int objectives = MatrixUtils.validate(input, 0, 255);
        if (objectives == 0) {
            return new int[input.length];
        }
        boolean[] directions = MatrixUtils.directions(maximise, objectives);
        if (objectives == 1) {
            return ranks1d(input, directions[0]);
        }
        if (objectives == 2) {
            return ranks2d(input, directions);
        }
        FlatPoints points = FlatPoints.toMinimisation(input, directions);
        return KungParetoAlgorithms.ranks(points);
    }

    private static boolean[] nondominated1d(
            double[][] points, boolean maximise, boolean keepWeakly) {
        double best = Double.POSITIVE_INFINITY;
        for (double[] point : points) {
            best = Math.min(best, objectiveValue(point[0], maximise));
        }
        boolean[] result = new boolean[points.length];
        boolean found = false;
        for (int i = 0; i < points.length; i++) {
            if (objectiveValue(points[i][0], maximise) == best && (keepWeakly || !found)) {
                result[i] = true;
                found = true;
            }
        }
        return result;
    }

    private static boolean[] nondominated2d(
            double[][] points, boolean[] maximise, boolean keepWeakly) {
        int[] order = KungParetoAlgorithms.sortByFirstObjective2d(points, maximise[0]);
        boolean[] result = new boolean[points.length];
        double bestSecond = Double.POSITIVE_INFINITY;
        boolean hasPrevious = false;
        int start = 0;
        while (start < order.length) {
            double first = objectiveValue(points[order[start]][0], maximise[0]);
            int end = start + 1;
            double groupBest = objectiveValue(points[order[start]][1], maximise[1]);
            int firstBestIndex = order[start];
            while (end < order.length
                    && objectiveValue(points[order[end]][0], maximise[0]) == first) {
                int index = order[end];
                double second = objectiveValue(points[index][1], maximise[1]);
                if (second < groupBest || (second == groupBest && index < firstBestIndex)) {
                    groupBest = second;
                    firstBestIndex = index;
                }
                end++;
            }
            if (!hasPrevious || bestSecond > groupBest) {
                for (int position = start; position < end; position++) {
                    int index = order[position];
                    if (objectiveValue(points[index][1], maximise[1]) == groupBest
                            && (keepWeakly || index == firstBestIndex)) {
                        result[index] = true;
                    }
                }
            }
            bestSecond = Math.min(bestSecond, groupBest);
            hasPrevious = true;
            start = end;
        }
        return result;
    }

    private static boolean anyDominated2d(
            double[][] points, boolean[] maximise, boolean keepWeakly) {
        int[] order = KungParetoAlgorithms.sortByFirstObjective2d(points, maximise[0]);
        double bestSecond = Double.POSITIVE_INFINITY;
        boolean hasPrevious = false;
        int start = 0;
        while (start < order.length) {
            double first = objectiveValue(points[order[start]][0], maximise[0]);
            int end = start + 1;
            double groupBest = objectiveValue(points[order[start]][1], maximise[1]);
            int bestCount = 1;
            while (end < order.length
                    && objectiveValue(points[order[end]][0], maximise[0]) == first) {
                double second = objectiveValue(points[order[end]][1], maximise[1]);
                if (second < groupBest) {
                    groupBest = second;
                    bestCount = 1;
                } else if (second == groupBest) {
                    bestCount++;
                }
                end++;
            }
            if ((hasPrevious && bestSecond <= groupBest) || end - start > bestCount
                    || (!keepWeakly && bestCount > 1)) {
                return true;
            }
            bestSecond = groupBest;
            hasPrevious = true;
            start = end;
        }
        return false;
    }

    private static int[] ranks1d(double[][] points, boolean maximise) {
        double[] sorted = new double[points.length];
        for (int i = 0; i < points.length; i++) {
            sorted[i] = canonicalZero(objectiveValue(points[i][0], maximise));
        }
        Arrays.sort(sorted);
        int unique = 0;
        for (double value : sorted) {
            if (unique == 0 || value != sorted[unique - 1]) {
                sorted[unique++] = value;
            }
        }
        int[] result = new int[points.length];
        for (int i = 0; i < points.length; i++) {
            result[i] = Arrays.binarySearch(
                    sorted, 0, unique,
                    canonicalZero(objectiveValue(points[i][0], maximise)));
        }
        return result;
    }

    private static double canonicalZero(double value) {
        return value == 0.0 ? 0.0 : value;
    }

    private static int[] ranks2d(double[][] points, boolean[] maximise) {
        int[] order = KungParetoAlgorithms.sortLexicographically2dByFirst(points, maximise);
        int[] result = new int[points.length];
        double[] frontLast = new double[points.length];
        frontLast[0] = objectiveValue(points[order[0]][1], maximise[1]);
        int numberOfFronts = 0;
        int lastRank = 0;
        for (int position = 1; position < order.length; position++) {
            int index = order[position];
            int previous = order[position - 1];
            double second = objectiveValue(points[index][1], maximise[1]);
            if (second == objectiveValue(points[previous][1], maximise[1])
                    && objectiveValue(points[index][0], maximise[0])
                    == objectiveValue(points[previous][0], maximise[0])) {
                result[index] = lastRank;
                continue;
            }

            if (second < frontLast[numberOfFronts]) {
                int low = 0;
                int high = numberOfFronts + 1;
                while (low < high) {
                    int middle = (low + high) >>> 1;
                    if (second < frontLast[middle]) {
                        high = middle;
                    } else {
                        low = middle + 1;
                    }
                }
                lastRank = low;
            } else {
                numberOfFronts++;
                lastRank = numberOfFronts;
            }
            frontLast[lastRank] = second;
            result[index] = lastRank;
        }
        return result;
    }

    private static double objectiveValue(double value, boolean maximise) {
        return maximise ? -value : value;
    }

}
