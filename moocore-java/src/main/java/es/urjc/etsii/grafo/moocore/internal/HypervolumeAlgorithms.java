// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class HypervolumeAlgorithms {

    private HypervolumeAlgorithms() {
    }

    public static double hypervolume(double[][] input, double[] reference, boolean[] maximise) {
        if (input == null) {
            throw new IllegalArgumentException("points cannot be null");
        }
        if (input.length == 0) {
            if (reference == null || reference.length == 0) {
                throw new IllegalArgumentException("reference cannot be empty");
            }
            return 0.0;
        }
        int objectives = MatrixUtils.validate(input, 1, 31);
        boolean[] directions = MatrixUtils.directions(maximise, objectives);
        double[] ref = MatrixUtils.toMinimisation(MatrixUtils.vector(reference, objectives, "reference"), directions);
        double[][] points = MatrixUtils.toMinimisation(input, directions);
        points = relevantNondominated(points, ref);
        if (points.length == 0) {
            return 0.0;
        }
        return hypervolumeMinimisation(points, ref, objectives);
    }

    public static double[] contributions(double[][] input, double[] reference, boolean[] maximise,
                                         boolean ignoreDominated) {
        if (input == null) {
            throw new IllegalArgumentException("points cannot be null");
        }
        if (input.length == 0) {
            return new double[0];
        }
        int objectives = MatrixUtils.validate(input, 2, 31);
        boolean[] directions = MatrixUtils.directions(maximise, objectives);
        double[] ref = MatrixUtils.toMinimisation(MatrixUtils.vector(reference, objectives, "reference"), directions);
        double[][] points = MatrixUtils.toMinimisation(input, directions);
        double[] result = new double[points.length];
        if (!ignoreDominated) {
            double total = hypervolumeMinimisation(relevant(points, ref), ref, objectives);
            for (int removed = 0; removed < points.length; removed++) {
                double[][] subset = new double[points.length - 1][];
                int next = 0;
                for (int i = 0; i < points.length; i++) {
                    if (i != removed) {
                        subset[next++] = points[i];
                    }
                }
                result[removed] = Math.max(0.0,
                        total - hypervolumeMinimisation(relevant(subset, ref), ref, objectives));
            }
            return result;
        }

        boolean[] nondominated = ParetoAlgorithms.isNondominated(points, new boolean[]{false}, true);
        Map<PointKey, List<Integer>> occurrences = new LinkedHashMap<>();
        for (int i = 0; i < points.length; i++) {
            if (nondominated[i] && strictlyBetterThanReference(points[i], ref)) {
                occurrences.computeIfAbsent(new PointKey(points[i]), ignored -> new ArrayList<>()).add(i);
            }
        }
        List<double[]> clean = new ArrayList<>();
        List<Integer> cleanOriginal = new ArrayList<>();
        for (Map.Entry<PointKey, List<Integer>> entry : occurrences.entrySet()) {
            if (entry.getValue().size() == 1) {
                clean.add(entry.getKey().values);
                cleanOriginal.add(entry.getValue().getFirst());
            } else {
                clean.add(entry.getKey().values);
                cleanOriginal.add(-1);
            }
        }
        double[][] cleanArray = clean.toArray(double[][]::new);
        double total = hypervolumeMinimisation(cleanArray, ref, objectives);
        for (int removed = 0; removed < cleanArray.length; removed++) {
            int original = cleanOriginal.get(removed);
            if (original < 0) {
                continue;
            }
            double[][] subset = new double[cleanArray.length - 1][];
            for (int source = 0, target = 0; source < cleanArray.length; source++) {
                if (source != removed) {
                    subset[target++] = cleanArray[source];
                }
            }
            result[original] = Math.max(0.0, total - hypervolumeMinimisation(subset, ref, objectives));
        }
        return result;
    }

    static double hypervolumeMinimisation(double[][] points, double[] reference, int dimensions) {
        if (points.length == 0) {
            return 0.0;
        }
        if (points[0].length != dimensions) {
            double[][] projected = new double[points.length][dimensions];
            for (int i = 0; i < points.length; i++) {
                System.arraycopy(points[i], 0, projected[i], 0, dimensions);
            }
            points = projected;
        }
        if (dimensions == 1) {
            double minimum = reference[0];
            for (double[] point : points) {
                minimum = Math.min(minimum, point[0]);
            }
            return Math.max(0.0, reference[0] - minimum);
        }
        if (dimensions == 2) {
            return hypervolume2d(points, reference);
        }
        if (dimensions == 3) {
            return hypervolume3d(points, reference);
        }
        return new DimensionSweep(points, reference).compute();
    }

    private static double hypervolume2d(double[][] input, double[] reference) {
        double[][] points = relevantNondominated(input, reference);
        Arrays.sort(points, Comparator.comparingDouble(point -> point[0]));
        double area = 0.0;
        double previousY = reference[1];
        for (double[] point : points) {
            if (point[1] < previousY) {
                area += (reference[0] - point[0]) * (previousY - point[1]);
                previousY = point[1];
            }
        }
        return area;
    }

    private static double hypervolume3d(double[][] input, double[] reference) {
        double[][] points = relevantNondominated(input, reference);
        Arrays.sort(points, Comparator.comparingDouble(point -> point[2]));
        SkylineArea area = new SkylineArea(reference[0], reference[1]);
        double volume = 0.0;
        int index = 0;
        while (index < points.length) {
            double z = points[index][2];
            while (index < points.length && points[index][2] == z) {
                area.add(points[index][0], points[index][1]);
                index++;
            }
            double nextZ = index < points.length ? points[index][2] : reference[2];
            volume += area.area * Math.max(0.0, nextZ - z);
        }
        return volume;
    }

    private static double[][] relevantNondominated(double[][] input, double[] reference) {
        double[][] relevant = relevant(input, reference);
        if (relevant.length == 0 || relevant[0].length == 1) {
            return relevant;
        }
        boolean[] nondominated = ParetoAlgorithms.isNondominated(relevant, new boolean[]{false}, false);
        return MatrixUtils.select(relevant, nondominated);
    }

    private static double[][] relevant(double[][] input, double[] reference) {
        List<double[]> relevant = new ArrayList<>();
        for (double[] point : input) {
            if (strictlyBetterThanReference(point, reference)) {
                relevant.add(point.clone());
            }
        }
        return relevant.toArray(double[][]::new);
    }

    private static boolean strictlyBetterThanReference(double[] point, double[] reference) {
        for (int objective = 0; objective < point.length; objective++) {
            if (point[objective] >= reference[objective]) {
                return false;
            }
        }
        return true;
    }

    private static final class SkylineArea {
        private final double referenceX;
        private final double referenceY;
        private final TreeMap<Double, Double> skyline = new TreeMap<>();
        private double area;

        private SkylineArea(double referenceX, double referenceY) {
            this.referenceX = referenceX;
            this.referenceY = referenceY;
        }

        private void add(double x, double y) {
            Map.Entry<Double, Double> floor = skyline.floorEntry(x);
            if (floor != null && floor.getValue() <= y) {
                return;
            }
            double cursor = x;
            double envelope = floor == null ? referenceY : floor.getValue();
            Map.Entry<Double, Double> next = skyline.ceilingEntry(x);
            while (envelope > y && cursor < referenceX) {
                double end = next == null ? referenceX : Math.min(next.getKey(), referenceX);
                area += Math.max(0.0, end - cursor) * (envelope - y);
                cursor = end;
                if (next == null) {
                    break;
                }
                envelope = next.getValue();
                next = skyline.higherEntry(next.getKey());
            }
            Map.Entry<Double, Double> removable = skyline.ceilingEntry(x);
            while (removable != null && removable.getValue() >= y) {
                skyline.remove(removable.getKey());
                removable = skyline.ceilingEntry(x);
            }
            skyline.put(x, y);
        }
    }

    /**
     * Dimension sweep for four or more objectives. The linked-list recurrence follows the
     * Fonseca-Paquete-Lopez-Ibanez algorithm used by upstream moocore's MPL-2.0 c/hv.c,
     * with the three-dimensional base case implemented by {@link SkylineArea}.
     */
    private static final class DimensionSweep {
        private static final int STOP_DIMENSION = 2;

        private final Node head;
        private final double[] reference;
        private final double[] bound;
        private final int dimensions;

        private DimensionSweep(double[][] points, double[] reference) {
            this.reference = reference;
            dimensions = reference.length;
            int linkedDimensions = dimensions - STOP_DIMENSION;
            head = new Node(null, linkedDimensions);
            Node[] nodes = new Node[points.length];
            for (int i = 0; i < points.length; i++) {
                nodes[i] = new Node(points[i], linkedDimensions);
            }
            for (int objective = STOP_DIMENSION; objective < dimensions; objective++) {
                int link = objective - STOP_DIMENSION;
                Node[] sorted = nodes.clone();
                int coordinate = objective;
                Arrays.sort(sorted, Comparator.comparingDouble(node -> node.point[coordinate]));
                head.next[link] = sorted[0];
                sorted[0].previous[link] = head;
                for (int i = 1; i < sorted.length; i++) {
                    sorted[i - 1].next[link] = sorted[i];
                    sorted[i].previous[link] = sorted[i - 1];
                }
                sorted[sorted.length - 1].next[link] = head;
                head.previous[link] = sorted[sorted.length - 1];
            }
            bound = new double[dimensions];
            Arrays.fill(bound, -Double.MAX_VALUE);
        }

        private double compute() {
            return recursive(dimensions - 1, countNodes());
        }

        private double recursive(int dimension, int count) {
            if (dimension == STOP_DIMENSION) {
                return hypervolume3d();
            }

            int link = dimension - STOP_DIMENSION;
            Node highest = head.previous[link];
            for (Node node = highest; node != head; node = node.previous[link]) {
                if (node.ignore < dimension) {
                    node.ignore = 0;
                }
            }

            Node next = head;
            while (count > 1 && (highest.point[dimension] > bound[dimension]
                    || highest.previous[link].point[dimension] >= bound[dimension])) {
                delete(highest, dimension);
                next = highest;
                highest = highest.previous[link];
                count--;
            }

            double volume = 0.0;
            if (count > 1) {
                Node previous = highest.previous[link];
                volume = previous.volume[link]
                        + previous.area[link] * (highest.point[dimension] - previous.point[dimension]);
            } else {
                double area = 1.0;
                for (int objective = 0; objective <= STOP_DIMENSION; objective++) {
                    area *= reference[objective] - highest.point[objective];
                }
                highest.area[0] = area;
                for (int i = 1; i <= link; i++) {
                    int objective = STOP_DIMENSION + i;
                    highest.area[i] = highest.area[i - 1]
                            * (reference[objective] - highest.point[objective]);
                }
            }

            while (true) {
                highest.volume[link] = volume;
                Node previous = highest.previous[link];
                if (highest.ignore >= dimension) {
                    highest.area[link] = previous.area[link];
                } else {
                    highest.area[link] = recursive(dimension - 1, count);
                    if (highest.area[link] <= previous.area[link]) {
                        highest.ignore = dimension;
                    }
                }

                if (next == head) {
                    volume += highest.area[link]
                            * (reference[dimension] - highest.point[dimension]);
                    return volume;
                }
                volume += highest.area[link] * (next.point[dimension] - highest.point[dimension]);
                bound[dimension] = next.point[dimension];
                reinsert(next, dimension);
                count++;
                highest = next;
                next = next.next[link];
            }
        }

        private double hypervolume3d() {
            Node node = head.next[0];
            SkylineArea area = new SkylineArea(reference[0], reference[1]);
            double volume = 0.0;
            while (node != head) {
                double coordinate = node.point[2];
                while (node != head && node.point[2] == coordinate) {
                    area.add(node.point[0], node.point[1]);
                    node = node.next[0];
                }
                double nextCoordinate = node == head ? reference[2] : node.point[2];
                volume += area.area * Math.max(0.0, nextCoordinate - coordinate);
            }
            return volume;
        }

        private void delete(Node node, int dimension) {
            for (int objective = STOP_DIMENSION; objective < dimension; objective++) {
                int link = objective - STOP_DIMENSION;
                node.previous[link].next[link] = node.next[link];
                node.next[link].previous[link] = node.previous[link];
                bound[objective] = Math.min(bound[objective], node.point[objective]);
            }
        }

        private void reinsert(Node node, int dimension) {
            for (int objective = STOP_DIMENSION; objective < dimension; objective++) {
                int link = objective - STOP_DIMENSION;
                node.previous[link].next[link] = node;
                node.next[link].previous[link] = node;
                bound[objective] = Math.min(bound[objective], node.point[objective]);
            }
        }

        private int countNodes() {
            int count = 0;
            for (Node node = head.next[0]; node != head; node = node.next[0]) {
                count++;
            }
            return count;
        }

        private static final class Node {
            private final double[] point;
            private final Node[] next;
            private final Node[] previous;
            private final double[] area;
            private final double[] volume;
            private int ignore;

            private Node(double[] point, int linkedDimensions) {
                this.point = point;
                next = new Node[linkedDimensions];
                previous = new Node[linkedDimensions];
                area = new double[linkedDimensions];
                volume = new double[linkedDimensions];
            }
        }
    }

    private static final class PointKey {
        private final double[] values;
        private final int hash;

        private PointKey(double[] values) {
            this.values = values.clone();
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
