// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore;

import java.util.Arrays;

/** A matrix of objective vectors and the set identifier of each row. */
public record Dataset(double[][] points, int[] sets) {

    public Dataset {
        if (points == null || sets == null) {
            throw new IllegalArgumentException("points and sets cannot be null");
        }
        if (points.length != sets.length) {
            throw new IllegalArgumentException("sets must have one value per point");
        }
        points = deepCopy(points);
        sets = sets.clone();
    }

    @Override
    public double[][] points() {
        return deepCopy(points);
    }

    @Override
    public int[] sets() {
        return sets.clone();
    }

    double[][] pointsView() {
        return points;
    }

    int[] setsView() {
        return sets;
    }

    /** Return the dataset as a matrix whose final column contains set identifiers. */
    public double[][] toMatrix() {
        int objectives = points.length == 0 ? 0 : points[0].length;
        double[][] result = new double[points.length][objectives + 1];
        for (int i = 0; i < points.length; i++) {
            System.arraycopy(points[i], 0, result[i], 0, objectives);
            result[i][objectives] = sets[i];
        }
        return result;
    }

    private static double[][] deepCopy(double[][] matrix) {
        double[][] copy = new double[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i] == null) {
                throw new IllegalArgumentException("point rows cannot be null");
            }
            copy[i] = Arrays.copyOf(matrix[i], matrix[i].length);
        }
        return copy;
    }
}
