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
        int count = grouped.points.length;
        Integer[] byX = pointOrder(count);
        Integer[] byY = pointOrder(count);
        Arrays.sort(byX, (left, right) -> Double.compare(
                grouped.points[left][0], grouped.points[right][0]));
        Arrays.sort(byY, (left, right) -> Double.compare(
                grouped.points[right][1], grouped.points[left][1]));

        int[] attained = new int[grouped.numberOfSets];
        List<List<double[]>> result = emptySurfaces(levels.length);
        for (int surface = 0; surface < levels.length; surface++) {
            Arrays.fill(attained, 0);
            int x = 0;
            int y = 0;
            int run = grouped.groupByPoint[byX[x]];
            attained[run]++;
            int attainedSets = 1;

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
                        }
                        attained[run]++;
                    }
                }
                if (attainedSets < levels[surface]) {
                    continue;
                }

                do {
                    do {
                        int point = byY[y];
                        if (grouped.points[point][0] <= grouped.points[byX[x]][0]) {
                            run = grouped.groupByPoint[point];
                            attained[run]--;
                            if (attained[run] == 0) {
                                attainedSets--;
                            }
                        }
                        y++;
                    } while (y < count
                            && grouped.points[byY[y]][1] == grouped.points[byY[y - 1]][1]);
                } while (attainedSets >= levels[surface] && y < count);

                result.get(surface).add(new double[]{
                        grouped.points[byX[x]][0], grouped.points[byY[y - 1]][1]});
            } while (x < count - 1 && y < count);
        }
        return result;
    }

    private static List<List<double[]>> surfaces3d(GroupedPoints grouped, int[] levels) {
        return Eaf3d.compute(grouped.points, grouped.groupByPoint,
                grouped.numberOfSets, levels);
    }

    private static Integer[] pointOrder(int size) {
        Integer[] order = new Integer[size];
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
        List<List<double[]>> surfaces = surfaces2d(combined, levels);
        List<double[]> result = new ArrayList<>();
        Set<RowKey> seen = new HashSet<>();
        for (List<double[]> surface : surfaces) {
            for (double[] point : surface) {
                double color = differenceColor(point[0], point[1], left, right, intervals);
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
        List<List<double[]>> surfaces = surfaces2d(combined, levels);
        List<double[]> result = new ArrayList<>();
        for (int upperLevel = 1; upperLevel < surfaces.size(); upperLevel++) {
            addRectanglesBetweenSurfaces(result, surfaces.get(upperLevel - 1),
                    surfaces.get(upperLevel), left, right, intervals);
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
                                                     GroupedPoints left,
                                                     GroupedPoints right,
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
                            differenceColor(lower[0], lower[1], left, right, intervals));
                }
                top = upper[1];
                upperIndex++;
                if (upperIndex >= upperSurface.size()) {
                    addRemainingRectangles(rectangles, lowerSurface, lowerIndex, top,
                            left, right, intervals);
                    return;
                }
                upper = upperSurface.get(upperIndex);
            }

            if (lower[0] < upper[0]) {
                addRectangle(rectangles, lower[0], lower[1], upper[0], top,
                        differenceColor(lower[0], lower[1], left, right, intervals));
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
                            left, right, intervals);
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
                                               GroupedPoints left,
                                               GroupedPoints right,
                                               int intervals) {
        for (int i = start; i < surface.size(); i++) {
            double[] point = surface.get(i);
            addRectangle(rectangles, point[0], point[1], Double.POSITIVE_INFINITY, top,
                    differenceColor(point[0], point[1], left, right, intervals));
            top = point[1];
        }
    }

    private static void addRectangle(List<double[]> rectangles, double lowerX, double lowerY,
                                     double upperX, double upperY, double color) {
        if (lowerX < upperX && lowerY < upperY) {
            rectangles.add(new double[]{lowerX, lowerY, upperX, upperY, color});
        }
    }

    private static double differenceColor(double x, double y, GroupedPoints left,
                                          GroupedPoints right, int intervals) {
        double leftFraction = attainedCount(x, y, left) / (double) left.numberOfSets;
        double rightFraction = attainedCount(x, y, right) / (double) right.numberOfSets;
        return intervals * (leftFraction - rightFraction);
    }

    private static int attainedCount(double x, double y, GroupedPoints grouped) {
        boolean[] attained = new boolean[grouped.numberOfSets];
        int count = 0;
        for (int i = 0; i < grouped.points.length; i++) {
            if (!attained[grouped.groupByPoint[i]]
                    && grouped.points[i][0] <= x && grouped.points[i][1] <= y) {
                attained[grouped.groupByPoint[i]] = true;
                count++;
            }
        }
        return count;
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

    private static double[] uniqueCoordinates(double[][] points, int dimension) {
        TreeSet<Double> values = new TreeSet<>();
        for (double[] point : points) {
            values.add(point[dimension]);
        }
        double[] result = new double[values.size()];
        int index = 0;
        for (double value : values) {
            result[index++] = value;
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
