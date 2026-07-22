// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

/** Upstream moocore's dimension-sweep HVC2D algorithm. */
final class HypervolumeContributions2d {

    private HypervolumeContributions2d() {
    }

    static double[] compute(double[][] points, double[] reference, boolean ignoreDominated) {
        double[] contributions = new double[points.length];
        int[] order = new int[points.length];
        int count = 0;
        for (int i = 0; i < points.length; i++) {
            if (points[i][0] < reference[0]) {
                order[count++] = i;
            }
        }
        if (count == 0) {
            return contributions;
        }
        KungParetoAlgorithms.sortLexicographically(points, order, count, 0, 1);
        if (ignoreDominated) {
            computeIgnoringDominated(points, reference, order, count, contributions);
        } else {
            computeWithDominated(points, reference, order, count, contributions);
        }
        return contributions;
    }

    private static void computeIgnoringDominated(double[][] points, double[] reference,
                                                 int[] order, int count, double[] contributions) {
        int position = firstBelowReference(points, reference[1], order, count);
        if (position == count) {
            return;
        }
        int previous = order[position++];
        double height = reference[1] - points[previous][1];
        while (position < count) {
            int current = order[position];
            if (points[previous][1] > points[current][1]) {
                contributions[previous] = (points[current][0] - points[previous][0]) * height;
                height = points[previous][1] - points[current][1];
                previous = current;
                position++;
            } else if (points[previous][0] == points[current][0]) {
                if (points[previous][1] == points[current][1]) {
                    height = 0.0;
                    previous = current;
                }
                do {
                    position++;
                } while (position < count
                        && points[previous][0] == points[order[position]][0]);
            } else {
                do {
                    position++;
                } while (position < count
                        && points[previous][1] <= points[order[position]][1]);
            }
        }
        contributions[previous] = (reference[0] - points[previous][0]) * height;
    }

    private static void computeWithDominated(double[][] points, double[] reference,
                                             int[] order, int count, double[] contributions) {
        int position = firstBelowReference(points, reference[1], order, count);
        if (position == count) {
            return;
        }
        int previous = order[position++];
        double height = reference[1] - points[previous][1];
        while (position < count) {
            int current = order[position];
            if (points[previous][1] > points[current][1]) {
                contributions[previous] += (points[current][0] - points[previous][0]) * height;
                height = points[previous][1] - points[current][1];
                previous = current;
                position++;
            } else if (points[previous][0] < points[current][0]) {
                double dominatedHeight = points[current][1] - points[previous][1];
                if (dominatedHeight < height) {
                    contributions[previous] += (points[current][0] - points[previous][0])
                            * (height - dominatedHeight);
                    height = dominatedHeight;
                }
                position++;
            } else if (points[previous][1] == points[current][1]) {
                height = 0.0;
                previous = current;
                do {
                    position++;
                } while (position < count
                        && points[previous][1] <= points[order[position]][1]);
            } else {
                height = Math.min(height, points[current][1] - points[previous][1]);
                do {
                    position++;
                } while (position < count
                        && points[previous][0] == points[order[position]][0]);
            }
        }
        contributions[previous] += (reference[0] - points[previous][0]) * height;
    }

    private static int firstBelowReference(double[][] points, double referenceY,
                                           int[] order, int count) {
        int position = 0;
        while (position < count && points[order[position]][1] >= referenceY) {
            position++;
        }
        return position;
    }
}
