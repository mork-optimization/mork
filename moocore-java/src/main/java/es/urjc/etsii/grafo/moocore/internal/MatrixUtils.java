// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class MatrixUtils {

    private MatrixUtils() {
    }

    public static int validate(double[][] points, int minimumObjectives, int maximumObjectives) {
        if (points == null) {
            throw new IllegalArgumentException("points cannot be null");
        }
        if (points.length == 0) {
            if (minimumObjectives > 0) {
                throw new IllegalArgumentException("points cannot be empty");
            }
            return 0;
        }
        if (points[0] == null) {
            throw new IllegalArgumentException("point rows cannot be null");
        }
        int objectives = points[0].length;
        if (objectives < minimumObjectives) {
            throw new IllegalArgumentException("points must have at least " + minimumObjectives + " objectives");
        }
        if (objectives > maximumObjectives) {
            throw new UnsupportedOperationException("at most " + maximumObjectives + " objectives are supported");
        }
        for (double[] point : points) {
            if (point == null || point.length != objectives) {
                throw new IllegalArgumentException("points must be a rectangular matrix");
            }
            for (double value : point) {
                if (Double.isNaN(value)) {
                    throw new IllegalArgumentException("NaN objective values are not supported");
                }
            }
        }
        return objectives;
    }

    public static boolean[] directions(boolean[] maximise, int objectives) {
        if (maximise == null || maximise.length == 0) {
            return new boolean[objectives];
        }
        if (maximise.length == objectives) {
            return maximise.clone();
        }
        if (maximise.length == 1) {
            boolean[] result = new boolean[objectives];
            Arrays.fill(result, maximise[0]);
            return result;
        }
        throw new IllegalArgumentException("maximise must have length 1 or match the number of objectives");
    }

    public static double[] vector(double[] values, int objectives, String name) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException(name + " cannot be empty");
        }
        if (values.length == objectives) {
            return values.clone();
        }
        if (values.length == 1) {
            double[] result = new double[objectives];
            Arrays.fill(result, values[0]);
            return result;
        }
        throw new IllegalArgumentException(name + " must have length 1 or match the number of objectives");
    }

    public static double[][] toMinimisation(double[][] points, boolean[] maximise) {
        double[][] result = deepCopy(points);
        for (int row = 0; row < result.length; row++) {
            for (int objective = 0; objective < maximise.length; objective++) {
                if (maximise[objective]) {
                    result[row][objective] = -result[row][objective];
                }
            }
        }
        return result;
    }

    public static double[] toMinimisation(double[] point, boolean[] maximise) {
        double[] result = point.clone();
        for (int objective = 0; objective < maximise.length; objective++) {
            if (maximise[objective]) {
                result[objective] = -result[objective];
            }
        }
        return result;
    }

    public static double[][] deepCopy(double[][] matrix) {
        double[][] result = new double[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            result[i] = matrix[i].clone();
        }
        return result;
    }

    public static double[][] select(double[][] matrix, boolean[] selected) {
        int count = 0;
        for (boolean value : selected) {
            if (value) {
                count++;
            }
        }
        double[][] result = new double[count][];
        int next = 0;
        for (int i = 0; i < selected.length; i++) {
            if (selected[i]) {
                result[next++] = matrix[i].clone();
            }
        }
        return result;
    }

    public static Map<Integer, int[]> groups(int[] sets, int rows) {
        if (sets == null || sets.length != rows) {
            throw new IllegalArgumentException("sets must have one value per row");
        }
        Map<Integer, List<Integer>> lists = new LinkedHashMap<>();
        for (int i = 0; i < sets.length; i++) {
            lists.computeIfAbsent(sets[i], ignored -> new ArrayList<>()).add(i);
        }
        Map<Integer, int[]> groups = new LinkedHashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : lists.entrySet()) {
            int[] indices = new int[entry.getValue().size()];
            for (int i = 0; i < indices.length; i++) {
                indices[i] = entry.getValue().get(i);
            }
            groups.put(entry.getKey(), indices);
        }
        return groups;
    }

    public static double[][] rows(double[][] matrix, int[] indices) {
        double[][] result = new double[indices.length][];
        for (int i = 0; i < indices.length; i++) {
            result[i] = matrix[indices[i]].clone();
        }
        return result;
    }
}
