// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        double[][] points = MatrixUtils.toMinimisation(input, directions);
        if (objectives == 1) {
            return nondominated1d(points, keepWeakly);
        }

        List<UniquePoint> unique = unique(points);
        boolean[] uniqueNondominated;
        if (objectives == 2) {
            uniqueNondominated = nondominated2d(unique);
        } else if (objectives == 3) {
            uniqueNondominated = nondominated3d(unique);
        } else if (objectives == 4) {
            uniqueNondominated = nondominated4d(unique);
        } else {
            uniqueNondominated = nondominatedNaive(unique, objectives);
        }
        return expand(unique, uniqueNondominated, input.length, keepWeakly);
    }

    public static boolean anyDominated(double[][] points, boolean[] maximise, boolean keepWeakly) {
        if (points == null || points.length == 0) {
            throw new IllegalArgumentException("points cannot be empty");
        }
        boolean[] nondominated = isNondominated(points, maximise, keepWeakly);
        for (boolean value : nondominated) {
            if (!value) {
                return true;
            }
        }
        return false;
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
        double[][] points = MatrixUtils.toMinimisation(input, directions);
        if (objectives == 1) {
            return ranks1d(points);
        }
        if (objectives == 2) {
            return ranks2d(points);
        }

        int[] ranks = new int[points.length];
        boolean[] remaining = new boolean[points.length];
        Arrays.fill(remaining, true);
        int left = points.length;
        int front = 0;
        while (left > 0) {
            int[] original = new int[left];
            double[][] subset = new double[left][];
            int next = 0;
            for (int i = 0; i < points.length; i++) {
                if (remaining[i]) {
                    original[next] = i;
                    subset[next++] = points[i];
                }
            }
            boolean[] current = isNondominated(subset, new boolean[objectives], true);
            int removed = 0;
            for (int i = 0; i < current.length; i++) {
                if (current[i]) {
                    ranks[original[i]] = front;
                    remaining[original[i]] = false;
                    removed++;
                }
            }
            if (removed == 0) {
                throw new IllegalStateException("failed to identify a Pareto front");
            }
            left -= removed;
            front++;
        }
        return ranks;
    }

    private static boolean[] nondominated1d(double[][] points, boolean keepWeakly) {
        double best = Double.POSITIVE_INFINITY;
        for (double[] point : points) {
            best = Math.min(best, point[0]);
        }
        boolean[] result = new boolean[points.length];
        boolean found = false;
        for (int i = 0; i < points.length; i++) {
            if (points[i][0] == best && (keepWeakly || !found)) {
                result[i] = true;
                found = true;
            }
        }
        return result;
    }

    private static boolean[] nondominated2d(List<UniquePoint> unique) {
        Integer[] order = order(unique, 2);
        boolean[] result = new boolean[unique.size()];
        double bestSecond = Double.POSITIVE_INFINITY;
        for (int index : order) {
            double second = unique.get(index).values[1];
            if (second < bestSecond) {
                result[index] = true;
                bestSecond = second;
            }
        }
        return result;
    }

    private static boolean[] nondominated3d(List<UniquePoint> unique) {
        Integer[] order = order(unique, 3);
        boolean[] result = new boolean[unique.size()];
        TreeMap<Double, Double> skyline = new TreeMap<>();
        for (int index : order) {
            double[] point = unique.get(index).values;
            Map.Entry<Double, Double> floor = skyline.floorEntry(point[1]);
            if (floor != null && floor.getValue() <= point[2]) {
                continue;
            }
            result[index] = true;
            Map.Entry<Double, Double> current = skyline.ceilingEntry(point[1]);
            while (current != null && current.getValue() >= point[2]) {
                skyline.remove(current.getKey());
                current = skyline.ceilingEntry(point[1]);
            }
            skyline.put(point[1], point[2]);
        }
        return result;
    }

    private static boolean[] nondominated4d(List<UniquePoint> unique) {
        Integer[] order = order(unique, 4);
        double[] secondCoordinates = coordinates(unique, 1);
        double[] thirdCoordinates = coordinates(unique, 2);
        FenwickMinimum2d previous = new FenwickMinimum2d(secondCoordinates, thirdCoordinates, unique);
        boolean[] result = new boolean[unique.size()];
        for (int index : order) {
            double[] point = unique.get(index).values;
            int second = Arrays.binarySearch(secondCoordinates, point[1]) + 1;
            int third = Arrays.binarySearch(thirdCoordinates, point[2]) + 1;
            if (previous.minimum(second, third) > point[3]) {
                result[index] = true;
                previous.update(second, third, point[3]);
            }
        }
        return result;
    }

    private static boolean[] nondominatedNaive(List<UniquePoint> unique, int objectives) {
        boolean[] result = new boolean[unique.size()];
        Arrays.fill(result, true);
        for (int i = 0; i < unique.size(); i++) {
            double[] candidate = unique.get(i).values;
            for (int j = 0; j < unique.size(); j++) {
                if (i != j && dominates(unique.get(j).values, candidate, objectives)) {
                    result[i] = false;
                    break;
                }
            }
        }
        return result;
    }

    private static boolean dominates(double[] left, double[] right, int objectives) {
        boolean strict = false;
        for (int objective = 0; objective < objectives; objective++) {
            if (left[objective] > right[objective]) {
                return false;
            }
            strict |= left[objective] < right[objective];
        }
        return strict;
    }

    private static Integer[] order(List<UniquePoint> unique, int objectives) {
        Integer[] order = new Integer[unique.size()];
        for (int i = 0; i < order.length; i++) {
            order[i] = i;
        }
        Arrays.sort(order, (left, right) -> {
            double[] a = unique.get(left).values;
            double[] b = unique.get(right).values;
            for (int objective = 0; objective < objectives; objective++) {
                int comparison = Double.compare(a[objective], b[objective]);
                if (comparison != 0) {
                    return comparison;
                }
            }
            return Integer.compare(unique.get(left).firstIndex, unique.get(right).firstIndex);
        });
        return order;
    }

    private static double[] coordinates(List<UniquePoint> points, int objective) {
        double[] values = new double[points.size()];
        for (int i = 0; i < points.size(); i++) {
            values[i] = points.get(i).values[objective];
        }
        Arrays.sort(values);
        int unique = 0;
        for (double value : values) {
            if (unique == 0 || value != values[unique - 1]) {
                values[unique++] = value;
            }
        }
        return Arrays.copyOf(values, unique);
    }

    private static List<UniquePoint> unique(double[][] points) {
        Map<PointKey, Integer> byValue = new HashMap<>();
        List<UniquePoint> result = new ArrayList<>();
        for (int i = 0; i < points.length; i++) {
            PointKey key = new PointKey(points[i]);
            Integer index = byValue.get(key);
            if (index == null) {
                byValue.put(key, result.size());
                UniquePoint point = new UniquePoint(points[i].clone(), i);
                point.originalIndices.add(i);
                result.add(point);
            } else {
                result.get(index).originalIndices.add(i);
            }
        }
        return result;
    }

    private static boolean[] expand(List<UniquePoint> unique, boolean[] uniqueValues, int size, boolean keepWeakly) {
        boolean[] result = new boolean[size];
        for (int i = 0; i < unique.size(); i++) {
            if (!uniqueValues[i]) {
                continue;
            }
            UniquePoint point = unique.get(i);
            if (keepWeakly) {
                for (int index : point.originalIndices) {
                    result[index] = true;
                }
            } else {
                result[point.firstIndex] = true;
            }
        }
        return result;
    }

    private static int[] ranks1d(double[][] points) {
        double[] sorted = new double[points.length];
        for (int i = 0; i < points.length; i++) {
            sorted[i] = canonicalZero(points[i][0]);
        }
        Arrays.sort(sorted);
        Map<Double, Integer> rank = new HashMap<>();
        int nextRank = 0;
        for (double value : sorted) {
            if (!rank.containsKey(value)) {
                rank.put(value, nextRank++);
            }
        }
        int[] result = new int[points.length];
        for (int i = 0; i < points.length; i++) {
            result[i] = rank.get(canonicalZero(points[i][0]));
        }
        return result;
    }

    private static double canonicalZero(double value) {
        return value == 0.0 ? 0.0 : value;
    }

    private static int[] ranks2d(double[][] points) {
        List<UniquePoint> unique = unique(points);
        Integer[] order = order(unique, 2);
        double[] seconds = new double[unique.size()];
        for (int i = 0; i < unique.size(); i++) {
            seconds[i] = unique.get(i).values[1];
        }
        Arrays.sort(seconds);
        int uniqueSeconds = 0;
        for (double value : seconds) {
            if (uniqueSeconds == 0 || value != seconds[uniqueSeconds - 1]) {
                seconds[uniqueSeconds++] = value;
            }
        }
        FenwickMaximum fronts = new FenwickMaximum(uniqueSeconds);
        int[] uniqueRanks = new int[unique.size()];
        for (int index : order) {
            double second = unique.get(index).values[1];
            int coordinate = Arrays.binarySearch(seconds, 0, uniqueSeconds, second) + 1;
            int rank = fronts.maximum(coordinate);
            uniqueRanks[index] = rank;
            fronts.update(coordinate, rank + 1);
        }
        int[] result = new int[points.length];
        for (int i = 0; i < unique.size(); i++) {
            for (int original : unique.get(i).originalIndices) {
                result[original] = uniqueRanks[i];
            }
        }
        return result;
    }

    private static final class UniquePoint {
        private final double[] values;
        private final int firstIndex;
        private final List<Integer> originalIndices = new ArrayList<>();

        private UniquePoint(double[] values, int firstIndex) {
            this.values = values;
            this.firstIndex = firstIndex;
        }
    }

    private static final class PointKey {
        private final double[] values;
        private final int hash;

        private PointKey(double[] values) {
            this.values = values.clone();
            for (int i = 0; i < this.values.length; i++) {
                if (this.values[i] == 0.0) {
                    this.values[i] = 0.0;
                }
            }
            this.hash = Arrays.hashCode(this.values);
        }

        @Override
        public boolean equals(Object object) {
            return object instanceof PointKey other && Arrays.equals(values, other.values);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    private static final class FenwickMaximum {
        private final int[] values;

        private FenwickMaximum(int size) {
            values = new int[size + 1];
        }

        private int maximum(int index) {
            int maximum = 0;
            while (index > 0) {
                maximum = Math.max(maximum, values[index]);
                index -= index & -index;
            }
            return maximum;
        }

        private void update(int index, int value) {
            while (index < values.length) {
                values[index] = Math.max(values[index], value);
                index += index & -index;
            }
        }
    }

    private static final class FenwickMinimum2d {
        private final double[] globalThirdCoordinates;
        private final double[][] thirdCoordinates;
        private final double[][] minimum;

        @SuppressWarnings("unchecked")
        private FenwickMinimum2d(double[] seconds, double[] thirds, List<UniquePoint> points) {
            globalThirdCoordinates = thirds;
            List<Double>[] coordinates = new List[seconds.length + 1];
            for (int i = 1; i < coordinates.length; i++) {
                coordinates[i] = new ArrayList<>();
            }
            for (UniquePoint point : points) {
                int second = Arrays.binarySearch(seconds, point.values[1]) + 1;
                double third = point.values[2];
                for (int i = second; i < coordinates.length; i += i & -i) {
                    coordinates[i].add(third);
                }
            }
            thirdCoordinates = new double[coordinates.length][];
            minimum = new double[coordinates.length][];
            for (int i = 1; i < coordinates.length; i++) {
                coordinates[i].sort(Double::compare);
                double[] unique = new double[coordinates[i].size()];
                int size = 0;
                for (double value : coordinates[i]) {
                    if (size == 0 || value != unique[size - 1]) {
                        unique[size++] = value;
                    }
                }
                thirdCoordinates[i] = Arrays.copyOf(unique, size);
                minimum[i] = new double[size + 1];
                Arrays.fill(minimum[i], Double.POSITIVE_INFINITY);
            }
        }

        private void update(int second, int third, double value) {
            double coordinate = globalThirdCoordinates[third - 1];
            for (int i = second; i < minimum.length; i += i & -i) {
                int localThird = Arrays.binarySearch(thirdCoordinates[i], coordinate) + 1;
                for (int j = localThird; j < minimum[i].length; j += j & -j) {
                    minimum[i][j] = Math.min(minimum[i][j], value);
                }
            }
        }

        private double minimum(int second, int third) {
            double result = Double.POSITIVE_INFINITY;
            double coordinate = globalThirdCoordinates[third - 1];
            for (int i = second; i > 0; i -= i & -i) {
                int localThird = upperBound(thirdCoordinates[i], coordinate);
                for (int j = localThird; j > 0; j -= j & -j) {
                    result = Math.min(result, minimum[i][j]);
                }
            }
            return result;
        }

        private static int upperBound(double[] values, double target) {
            int low = 0;
            int high = values.length;
            while (low < high) {
                int middle = (low + high) >>> 1;
                if (values[middle] <= target) {
                    low = middle + 1;
                } else {
                    high = middle;
                }
            }
            return low;
        }
    }
}
