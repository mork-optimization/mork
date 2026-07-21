// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import java.util.ArrayList;
import java.util.Arrays;
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
        double[][] points = toMinimisation(input, directions);
        if (objectives == 1) {
            return nondominated1d(points, keepWeakly);
        }

        if (objectives >= 4) {
            return KungParetoAlgorithms.nondominated(points, keepWeakly);
        }

        if (objectives == 2) {
            return nondominated2d(points, keepWeakly);
        }
        List<UniquePoint> unique = unique(points);
        boolean[] uniqueNondominated = nondominated3d(unique);
        return expand(unique, uniqueNondominated, input.length, keepWeakly);
    }

    public static boolean anyDominated(double[][] input, boolean[] maximise, boolean keepWeakly) {
        if (input == null || input.length == 0) {
            throw new IllegalArgumentException("points cannot be empty");
        }
        int objectives = MatrixUtils.validate(input, 1, 255);
        boolean[] directions = MatrixUtils.directions(maximise, objectives);
        double[][] points = toMinimisation(input, directions);
        if (objectives == 1) {
            if (!keepWeakly) {
                return points.length > 1;
            }
            double first = points[0][0];
            for (int row = 1; row < points.length; row++) {
                if (points[row][0] != first) {
                    return true;
                }
            }
            return false;
        }
        if (objectives >= 4) {
            return KungParetoAlgorithms.anyDominated(points, keepWeakly);
        }

        if (objectives == 2) {
            return anyDominated2d(points, keepWeakly);
        }
        List<UniquePoint> unique = unique(points);
        if (!keepWeakly && unique.size() < points.length) {
            return true;
        }
        return anyDominated3d(unique);
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
        double[][] points = toMinimisation(input, directions);
        if (objectives == 1) {
            return ranks1d(points);
        }
        if (objectives == 2) {
            return ranks2d(points);
        }
        return KungParetoAlgorithms.ranks(points);
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

    private static boolean[] nondominated2d(double[][] points, boolean keepWeakly) {
        int[] order = KungParetoAlgorithms.sortReverseLexicographically2d(points);
        boolean[] result = new boolean[points.length];
        Arrays.fill(result, true);
        double previousFirst = points[order[0]][0];
        double previousSecond = points[order[0]][1];
        for (int position = 1; position < order.length; position++) {
            int index = order[position];
            double first = points[index][0];
            double second = points[index][1];
            if (previousFirst > first) {
                previousFirst = first;
                previousSecond = second;
            } else if (!keepWeakly
                    || previousFirst != first
                    || previousSecond != second) {
                result[index] = false;
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

    private static boolean anyDominated2d(double[][] points, boolean keepWeakly) {
        int[] order = KungParetoAlgorithms.sortReverseLexicographically2d(points);
        double previousFirst = points[order[0]][0];
        double previousSecond = points[order[0]][1];
        for (int position = 1; position < order.length; position++) {
            double first = points[order[position]][0];
            double second = points[order[position]][1];
            if (previousFirst > first) {
                previousFirst = first;
                previousSecond = second;
            } else if (!keepWeakly
                    || previousFirst != first
                    || previousSecond != second) {
                return true;
            }
        }
        return false;
    }

    private static boolean anyDominated3d(List<UniquePoint> unique) {
        Integer[] order = order(unique, 3);
        TreeMap<Double, Double> skyline = new TreeMap<>();
        for (int index : order) {
            double[] point = unique.get(index).values;
            Map.Entry<Double, Double> floor = skyline.floorEntry(point[1]);
            if (floor != null && floor.getValue() <= point[2]) {
                return true;
            }
            Map.Entry<Double, Double> current = skyline.ceilingEntry(point[1]);
            while (current != null && current.getValue() >= point[2]) {
                skyline.remove(current.getKey());
                current = skyline.ceilingEntry(point[1]);
            }
            skyline.put(point[1], point[2]);
        }
        return false;
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

    private static double[][] toMinimisation(double[][] points, boolean[] maximise) {
        for (boolean direction : maximise) {
            if (direction) {
                return MatrixUtils.toMinimisation(points, maximise);
            }
        }
        return points;
    }

    private static int[] ranks2d(double[][] points) {
        int[] order = KungParetoAlgorithms.sortReverseLexicographically2d(points);
        int[] result = new int[points.length];
        double[] frontLast = new double[points.length];
        frontLast[0] = points[order[0]][0];
        int numberOfFronts = 0;
        int lastRank = 0;
        for (int position = 1; position < order.length; position++) {
            int index = order[position];
            int previous = order[position - 1];
            double first = points[index][0];
            if (first == points[previous][0]
                    && points[index][1] == points[previous][1]) {
                result[index] = lastRank;
                continue;
            }

            if (first < frontLast[numberOfFronts]) {
                int low = 0;
                int high = numberOfFronts + 1;
                while (low < high) {
                    int middle = (low + high) >>> 1;
                    if (first < frontLast[middle]) {
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
            frontLast[lastRank] = first;
            result[index] = lastRank;
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

}
