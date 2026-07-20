// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore;

/** Vorob'ev threshold, expectation, and average hypervolume. */
public record VorobevResult(double threshold, double[][] expectation, double averageHypervolume) {

    public VorobevResult {
        expectation = copy(expectation);
    }

    @Override
    public double[][] expectation() {
        return copy(expectation);
    }

    double[][] expectationView() {
        return expectation;
    }

    private static double[][] copy(double[][] matrix) {
        double[][] result = new double[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            result[i] = matrix[i].clone();
        }
        return result;
    }
}
