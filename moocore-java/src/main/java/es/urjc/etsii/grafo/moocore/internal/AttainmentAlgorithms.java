// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import es.urjc.etsii.grafo.moocore.EafDifferenceFormat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class AttainmentAlgorithms {

    private static final double LEVEL_TOLERANCE = Math.sqrt(Math.ulp(1.0));

    private AttainmentAlgorithms() {
    }

    public static double[][] eaf(double[][] input, int[] sets, double[] requestedPercentiles) {
        int dimensions = MatrixUtils.validate(input, 2, 3);
        GroupedPoints grouped = group(input, sets);
        double[] percentiles = percentiles(requestedPercentiles, grouped.numberOfSets);
        int[] levels = new int[percentiles.length];
        for (int i = 0; i < levels.length; i++) {
            levels[i] = percentileToLevel(percentiles[i], grouped.numberOfSets);
        }
        List<List<double[]>> surfaces = dimensions == 2
                ? surfaces2d(grouped, levels)
                : surfaces3d(grouped, levels);
        List<double[]> result = new ArrayList<>();
        for (int i = 0; i < surfaces.size(); i++) {
            for (double[] point : surfaces.get(i)) {
                double[] row = Arrays.copyOf(point, dimensions + 1);
                row[dimensions] = percentiles[i];
                result.add(row);
            }
        }
        return result.toArray(double[][]::new);
    }

    public static double[][] eafDifference(double[][] left, int[] leftSets,
                                           double[][] right, int[] rightSets,
                                           boolean[] maximise, Integer requestedIntervals,
                                           EafDifferenceFormat format) {
        int dimensions = MatrixUtils.validate(left, 2, 2);
        if (MatrixUtils.validate(right, 2, 2) != dimensions) {
            throw new IllegalArgumentException("left and right must have equal dimensions");
        }
        boolean[] directions = MatrixUtils.directions(maximise, dimensions);
        double[][] minimisedLeft = MatrixUtils.toMinimisation(left, directions);
        double[][] minimisedRight = MatrixUtils.toMinimisation(right, directions);
        GroupedPoints leftGrouped = group(minimisedLeft, leftSets);
        GroupedPoints rightGrouped = group(minimisedRight, rightSets);
        int intervals = requestedIntervals == null
                ? Math.min(leftGrouped.numberOfSets, rightGrouped.numberOfSets)
                : Math.min(requestedIntervals, Math.min(leftGrouped.numberOfSets, rightGrouped.numberOfSets));
        if (intervals <= 0) {
            throw new IllegalArgumentException("intervals must be positive");
        }

        double[][] result = format == EafDifferenceFormat.RECTANGLES
                ? differenceRectangles(leftGrouped, rightGrouped, intervals)
                : differencePoints(leftGrouped, rightGrouped, intervals);
        restoreDirections(result, directions, format);
        return result;
    }

    private static List<List<double[]>> surfaces2d(GroupedPoints grouped, int[] levels) {
        return surfaces2d(grouped, levels, -1);
    }

    private static List<List<double[]>> surfaces2d(
            GroupedPoints grouped, int[] levels, int leftSets) {
        int count = grouped.points.length;
        int[] byX = pointOrder(count);
        int[] byY = pointOrder(count);
        KungParetoAlgorithms.sortByCoordinate(grouped.points, byX, count, 0);
        KungParetoAlgorithms.sortByCoordinate(grouped.points, byY, count, 1, true);

        int[] attained = new int[grouped.numberOfSets];
        List<List<double[]>> result = emptySurfaces(levels.length);
        for (int surface = 0; surface < levels.length; surface++) {
            Arrays.fill(attained, 0);
            int x = 0;
            int y = 0;
            int run = grouped.groupByPoint[byX[x]];
            attained[run]++;
            int attainedSets = 1;
            int attainedLeft = leftSets >= 0 && run < leftSets ? 1 : 0;
            int attainedRight = leftSets >= 0 && run >= leftSets ? 1 : 0;
            int emittedLeft = 0;
            int emittedRight = 0;

            do {
                while (x < count - 1
                        && (attainedSets < levels[surface]
                        || grouped.points[byX[x]][0] == grouped.points[byX[x + 1]][0])) {
                    x++;
                    int point = byX[x];
                    if (grouped.points[point][1] <= grouped.points[byY[y]][1]) {
                        run = grouped.groupByPoint[point];
                        if (attained[run] == 0) {
                            attainedSets++;
                            if (leftSets >= 0) {
                                if (run < leftSets) {
                                    attainedLeft++;
                                } else {
                                    attainedRight++;
                                }
                            }
                        }
                        attained[run]++;
                    }
                }
                if (attainedSets < levels[surface]) {
                    continue;
                }

                do {
                    emittedLeft = attainedLeft;
                    emittedRight = attainedRight;
                    do {
                        int point = byY[y];
                        if (grouped.points[point][0] <= grouped.points[byX[x]][0]) {
                            run = grouped.groupByPoint[point];
                            attained[run]--;
                            if (attained[run] == 0) {
                                attainedSets--;
                                if (leftSets >= 0) {
                                    if (run < leftSets) {
                                        attainedLeft--;
                                    } else {
                                        attainedRight--;
                                    }
                                }
                            }
                        }
                        y++;
                    } while (y < count
                            && grouped.points[byY[y]][1] == grouped.points[byY[y - 1]][1]);
                } while (attainedSets >= levels[surface] && y < count);

                double pointX = grouped.points[byX[x]][0];
                double pointY = grouped.points[byY[y - 1]][1];
                result.get(surface).add(leftSets < 0
                        ? new double[]{pointX, pointY}
                        : new double[]{pointX, pointY, emittedLeft, emittedRight});
            } while (x < count - 1 && y < count);
        }
        return result;
    }

    private static List<List<double[]>> surfaces3d(GroupedPoints grouped, int[] levels) {
        return Eaf3d.compute(grouped.points, grouped.groupByPoint,
                grouped.numberOfSets, levels);
    }

    private static int[] pointOrder(int size) {
        int[] order = new int[size];
        for (int i = 0; i < size; i++) {
            order[i] = i;
        }
        return order;
    }

    private static double[][] differencePoints(GroupedPoints left, GroupedPoints right, int intervals) {
        GroupedPoints combined = combine(left, right);
        int[] levels = new int[combined.numberOfSets];
        for (int i = 0; i < levels.length; i++) {
            levels[i] = i + 1;
        }
        List<List<double[]>> surfaces = surfaces2d(combined, levels, left.numberOfSets);
        List<double[]> result = new ArrayList<>();
        Set<RowKey> seen = new HashSet<>();
        for (List<double[]> surface : surfaces) {
            for (double[] point : surface) {
                double color = differenceColor(
                        point, left.numberOfSets, right.numberOfSets, intervals);
                double[] row = {point[0], point[1], color};
                if (seen.add(new RowKey(row))) {
                    result.add(row);
                }
            }
        }
        return result.toArray(double[][]::new);
    }

    private static double[][] differenceRectangles(GroupedPoints left, GroupedPoints right, int intervals) {
        GroupedPoints combined = combine(left, right);
        int[] levels = new int[combined.numberOfSets];
        for (int i = 0; i < levels.length; i++) {
            levels[i] = i + 1;
        }
        List<List<double[]>> surfaces = surfaces2d(combined, levels, left.numberOfSets);
        List<double[]> result = new ArrayList<>();
        for (int upperLevel = 1; upperLevel < surfaces.size(); upperLevel++) {
            addRectanglesBetweenSurfaces(result, surfaces.get(upperLevel - 1),
                    surfaces.get(upperLevel), left.numberOfSets,
                    right.numberOfSets, intervals);
        }
        return result.toArray(double[][]::new);
    }

    /*
     * This sweep follows eaf_compute_rectangles in upstream eaf.c. That file is
     * Copyright (C) 2005-2025 Carlos M. Fonseca and Manuel Lopez-Ibanez and is
     * distributed under MPL-2.0.
     */
    private static void addRectanglesBetweenSurfaces(List<double[]> rectangles,
                                                     List<double[]> lowerSurface,
                                                     List<double[]> upperSurface,
                                                     int leftSets,
                                                     int rightSets,
                                                     int intervals) {
        if (lowerSurface.isEmpty() || upperSurface.isEmpty()) {
            return;
        }

        int lowerIndex = 0;
        int upperIndex = 0;
        double[] lower = lowerSurface.get(lowerIndex);
        double[] upper = upperSurface.get(upperIndex);
        double top = Double.POSITIVE_INFINITY;

        while (true) {
            while (lower[1] < upper[1]) {
                if (lower[0] < upper[0]) {
                    addRectangle(rectangles, lower[0], upper[1], upper[0], top,
                            differenceColor(lower, leftSets, rightSets, intervals));
                }
                top = upper[1];
                upperIndex++;
                if (upperIndex >= upperSurface.size()) {
                    addRemainingRectangles(rectangles, lowerSurface, lowerIndex, top,
                            leftSets, rightSets, intervals);
                    return;
                }
                upper = upperSurface.get(upperIndex);
            }

            if (lower[0] < upper[0]) {
                addRectangle(rectangles, lower[0], lower[1], upper[0], top,
                        differenceColor(lower, leftSets, rightSets, intervals));
            }
            top = lower[1];
            lowerIndex++;
            if (lowerIndex >= lowerSurface.size()) {
                return;
            }
            lower = lowerSurface.get(lowerIndex);

            if (upper[1] == top) {
                upperIndex++;
                if (upperIndex >= upperSurface.size()) {
                    addRemainingRectangles(rectangles, lowerSurface, lowerIndex, top,
                            leftSets, rightSets, intervals);
                    return;
                }
                upper = upperSurface.get(upperIndex);
            }
        }
    }

    private static void addRemainingRectangles(List<double[]> rectangles,
                                               List<double[]> surface,
                                               int start,
                                               double top,
                                               int leftSets,
                                               int rightSets,
                                               int intervals) {
        for (int i = start; i < surface.size(); i++) {
            double[] point = surface.get(i);
            addRectangle(rectangles, point[0], point[1], Double.POSITIVE_INFINITY, top,
                    differenceColor(point, leftSets, rightSets, intervals));
            top = point[1];
        }
    }

    private static void addRectangle(List<double[]> rectangles, double lowerX, double lowerY,
                                     double upperX, double upperY, double color) {
        if (lowerX < upperX && lowerY < upperY) {
            rectangles.add(new double[]{lowerX, lowerY, upperX, upperY, color});
        }
    }

    private static double differenceColor(double[] point, int leftSets,
                                          int rightSets, int intervals) {
        double leftFraction = point[2] / leftSets;
        double rightFraction = point[3] / rightSets;
        return intervals * (leftFraction - rightFraction);
    }

    private static GroupedPoints group(double[][] input, int[] sets) {
        Map<Integer, int[]> groups = MatrixUtils.groups(sets, input.length);
        double[][] points = new double[input.length][];
        int[] groupByPoint = new int[input.length];
        int next = 0;
        int group = 0;
        for (int[] indices : groups.values()) {
            for (int index : indices) {
                points[next] = input[index].clone();
                groupByPoint[next] = group;
                next++;
            }
            group++;
        }
        return new GroupedPoints(points, groupByPoint, groups.size());
    }

    private static GroupedPoints combine(GroupedPoints left, GroupedPoints right) {
        double[][] points = new double[left.points.length + right.points.length][];
        int[] groups = new int[points.length];
        for (int i = 0; i < left.points.length; i++) {
            points[i] = left.points[i];
            groups[i] = left.groupByPoint[i];
        }
        for (int i = 0; i < right.points.length; i++) {
            points[left.points.length + i] = right.points[i];
            groups[left.points.length + i] = left.numberOfSets + right.groupByPoint[i];
        }
        return new GroupedPoints(points, groups, left.numberOfSets + right.numberOfSets);
    }

    private static double[] percentiles(double[] requested, int numberOfSets) {
        if (requested == null || requested.length == 0) {
            double[] result = new double[numberOfSets];
            for (int i = 0; i < numberOfSets; i++) {
                result[i] = (i + 1) * 100.0 / numberOfSets;
            }
            result[result.length - 1] = 100.0;
            return result;
        }
        TreeSet<Double> unique = new TreeSet<>();
        for (double percentile : requested) {
            if (!Double.isFinite(percentile) || percentile < 0.0 || percentile > 100.0) {
                throw new IllegalArgumentException("percentiles must be in [0, 100]");
            }
            unique.add(percentile);
        }
        double[] result = new double[unique.size()];
        int index = 0;
        for (double percentile : unique) {
            result[index++] = percentile;
        }
        return result;
    }

    private static int percentileToLevel(double percentile, int numberOfSets) {
        double value = numberOfSets * percentile / 100.0;
        int level = value - Math.floor(value) <= LEVEL_TOLERANCE
                ? (int) Math.floor(value)
                : (int) Math.ceil(value);
        return Math.max(1, Math.min(numberOfSets, level));
    }

    private static List<List<double[]>> emptySurfaces(int size) {
        List<List<double[]>> result = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            result.add(new ArrayList<>());
        }
        return result;
    }

    private static void restoreDirections(double[][] rows, boolean[] maximise,
                                          EafDifferenceFormat format) {
        for (double[] row : rows) {
            if (format == EafDifferenceFormat.POINTS) {
                for (int objective = 0; objective < maximise.length; objective++) {
                    if (maximise[objective]) {
                        row[objective] = -row[objective];
                    }
                }
            } else {
                for (int objective = 0; objective < maximise.length; objective++) {
                    if (maximise[objective]) {
                        double lower = -row[objective + maximise.length];
                        double upper = -row[objective];
                        row[objective] = lower;
                        row[objective + maximise.length] = upper;
                    }
                }
            }
        }
    }

    private record GroupedPoints(double[][] points, int[] groupByPoint, int numberOfSets) {
    }

    private static final class RowKey {
        private final double[] values;
        private final int hash;

        private RowKey(double[] values) {
            this.values = values.clone();
            this.hash = Arrays.hashCode(values);
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof RowKey other && Arrays.equals(values, other.values);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
