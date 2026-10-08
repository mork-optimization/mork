// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

/** Allocation-conscious implementation of upstream's HV3D+ dimension sweep. */
final class Hypervolume3d {

    private Hypervolume3d() {
    }

    static double compute(double[][] input, double[] reference) {
        int[] order = new int[input.length];
        int size = 0;
        for (int row = 0; row < input.length; row++) {
            double[] point = input[row];
            if (point[0] < reference[0]
                    && point[1] < reference[1]
                    && point[2] < reference[2]) {
                order[size++] = row;
            }
        }
        if (size == 0) {
            return 0.0;
        }
        KungParetoAlgorithms.sortByCoordinate(input, order, size, 2);

        int lower = size;
        int upper = size + 1;
        double[] x = new double[size + 2];
        double[] y = new double[size + 2];
        double[] z = new double[size];
        for (int position = 0; position < size; position++) {
            double[] point = input[order[position]];
            x[position] = point[0];
            y[position] = point[1];
            z[position] = point[2];
        }
        x[lower] = reference[0];
        y[lower] = -Double.MAX_VALUE;
        x[upper] = -Double.MAX_VALUE;
        y[upper] = reference[1];

        int[] previousY = new int[size + 2];
        int[] nextY = new int[size + 2];
        boolean[] dominated = new boolean[size];
        preprocess(x, y, size, lower, upper, previousY, nextY, dominated);
        return sweep(x, y, z, reference[2], size, lower, upper,
                previousY, nextY, dominated);
    }

    private static void preprocess(double[] x, double[] y, int size, int lower, int upper,
                                   int[] previousY, int[] nextY, boolean[] dominated) {
        KungParetoAlgorithms.PrimitiveSkyline skyline =
                new KungParetoAlgorithms.PrimitiveSkyline(size + 2);
        skyline.add(y[lower], x[lower], lower);
        skyline.add(y[upper], x[upper], upper);

        for (int point = 0; point < size; point++) {
            double pointY = canonicalZero(y[point]);
            int previous = skyline.floor(pointY);
            if (skyline.value(previous) <= x[point]) {
                dominated[point] = true;
                continue;
            }

            int next = skyline.ceiling(pointY);
            while (skyline.value(next) >= x[point]) {
                next = skyline.remove(next);
            }
            previous = skyline.floor(pointY);
            previousY[point] = skyline.payload(previous);
            nextY[point] = skyline.payload(next);
            skyline.add(pointY, x[point], point);
        }
    }

    private static double sweep(double[] x, double[] y, double[] z, double referenceZ,
                                int size, int lower, int upper, int[] previousY, int[] nextY,
                                boolean[] dominated) {
        int first = firstNondominated(dominated, 0, size);
        int firstPrevious = previousY[first];
        int firstNext = nextY[first];
        nextY[firstPrevious] = firstNext;
        previousY[firstNext] = firstPrevious;

        double area = 0.0;
        double volume = 0.0;
        for (int point = 0; point < size; point++) {
            if (dominated[point]) {
                continue;
            }
            area += areaContribution(point, x, y, nextY, previousY);
            nextY[previousY[point]] = point;
            previousY[nextY[point]] = point;

            int next = firstNondominated(dominated, point + 1, size);
            double nextZ = next < size ? z[next] : referenceZ;
            volume += area * (nextZ - z[point]);
        }
        return volume;
    }

    private static double areaContribution(int point, double[] x, double[] y,
                                           int[] nextY, int[] previousY) {
        int previous = previousY[point];
        int next = nextY[previous];
        double area = (x[previous] - x[point]) * (y[next] - y[point]);
        while (x[point] < x[next]) {
            previous = next;
            next = nextY[next];
            area += (x[previous] - x[point]) * (y[next] - y[previous]);
        }
        return area;
    }

    private static int firstNondominated(boolean[] dominated, int start, int size) {
        int point = start;
        while (point < size && dominated[point]) {
            point++;
        }
        return point;
    }

    private static double canonicalZero(double value) {
        return value == 0.0 ? 0.0 : value;
    }
}
