// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

/** Contiguous row-major objective matrix used by allocation-sensitive kernels. */
final class FlatPoints {

    private final double[] values;
    private final int rows;
    private final int objectives;

    private FlatPoints(double[] values, int rows, int objectives) {
        this.values = values;
        this.rows = rows;
        this.objectives = objectives;
    }

    static FlatPoints toMinimisation(double[][] points, boolean[] maximise) {
        int rows = points.length;
        int objectives = points[0].length;
        double[] values = new double[rows * objectives];
        int position = 0;
        for (double[] point : points) {
            for (int objective = 0; objective < objectives; objective++) {
                double value = point[objective];
                values[position++] = maximise[objective] ? -value : value;
            }
        }
        return new FlatPoints(values, rows, objectives);
    }

    int rows() {
        return rows;
    }

    int objectives() {
        return objectives;
    }

    double get(int row, int objective) {
        return values[row * objectives + objective];
    }

    double[] values() {
        return values;
    }

    int offset(int row, int objective) {
        return row * objectives + objective;
    }
}
