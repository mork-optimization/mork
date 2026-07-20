// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import es.urjc.etsii.grafo.moocore.internal.Hypervolume4d.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class HypervolumeAlgorithms {

    private static final int INCLUSION_EXCLUSION_MAX_POINTS = 12;

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
        if (dimensions == 4) {
            return Hypervolume4d.compute(points, reference);
        }
        double[][] relevant = relevant(points, reference);
        if (relevant.length == 0) {
            return 0.0;
        }
        if (relevant.length <= INCLUSION_EXCLUSION_MAX_POINTS) {
            return inclusionExclusion(relevant, reference, dimensions);
        }
        return new DimensionSweep(relevant, reference).compute();
    }

    private static double inclusionExclusion(double[][] points, double[] reference, int dimensions) {
        double[] sums = new double[2];
        for (double[] point : points) {
            sums[1] += boxVolume(point, reference, dimensions);
        }
        double[][] bounds = new double[points.length - 1][dimensions];
        int[] starts = new int[Math.max(0, points.length - 2)];
        for (int first = 0; first < points.length - 1; first++) {
            int second = first + 1;
            while (true) {
                upperBound(bounds[0], points[first], points[second], dimensions);
                sums[0] += boxVolume(bounds[0], reference, dimensions);
                if (second == points.length - 1) {
                    break;
                }

                int index = ++second;
                int top = 0;
                while (true) {
                    double[] parent = bounds[top];
                    top++;
                    double[] child = bounds[top];
                    upperBound(child, points[index], parent, dimensions);
                    sums[top & 1] += boxVolume(child, reference, dimensions);
                    index++;
                    if (index < points.length) {
                        starts[top - 1] = index;
                    } else if (top > 1) {
                        top -= 2;
                        index = starts[top];
                    } else {
                        break;
                    }
                }
            }
        }
        return sums[1] - sums[0];
    }

    private static void upperBound(double[] target, double[] left, double[] right, int dimensions) {
        for (int objective = 0; objective < dimensions; objective++) {
            target[objective] = Math.max(left[objective], right[objective]);
        }
    }

    private static double boxVolume(double[] point, double[] reference, int dimensions) {
        double volume = 1.0;
        for (int objective = 0; objective < dimensions; objective++) {
            volume *= reference[objective] - point[objective];
        }
        return volume;
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
     * Dimension sweep for five or more objectives. It follows upstream's
     * Fonseca-Paquete-Lopez-Ibanez recurrence and stops at an HV4D+ base case.
     */
    private static final class DimensionSweep {
        private static final int STOP_DIMENSION = 3;

        private final Node head;
        private final Hypervolume4d.BaseList baseList;
        private final double[] reference;
        private final double[] bound;
        private final int dimensions;

        private DimensionSweep(double[][] points, double[] reference) {
            this.reference = reference;
            dimensions = reference.length;
            int higherDimensions = dimensions - STOP_DIMENSION - 1;
            int partialDimensions = dimensions - STOP_DIMENSION;
            head = new Node(null, higherDimensions, partialDimensions);
            Node[] nodes = new Node[points.length];
            for (int i = 0; i < points.length; i++) {
                nodes[i] = new Node(points[i], higherDimensions, partialDimensions);
            }
            baseList = Hypervolume4d.createBaseList(nodes, reference);
            for (int objective = STOP_DIMENSION + 1; objective < dimensions; objective++) {
                int link = objective - STOP_DIMENSION - 1;
                Node[] sorted = nodes.clone();
                int coordinate = objective;
                Arrays.sort(sorted, Comparator.comparingDouble(node -> node.point[coordinate]));
                head.higherNext[link] = sorted[0];
                sorted[0].higherPrevious[link] = head;
                for (int i = 1; i < sorted.length; i++) {
                    sorted[i - 1].higherNext[link] = sorted[i];
                    sorted[i].higherPrevious[link] = sorted[i - 1];
                }
                sorted[sorted.length - 1].higherNext[link] = head;
                head.higherPrevious[link] = sorted[sorted.length - 1];
            }
            bound = new double[dimensions];
            Arrays.fill(bound, -Double.MAX_VALUE);
        }

        private double compute() {
            return recursive(dimensions - 1, countNodes());
        }

        private double recursive(int dimension, int count) {
            int link = dimension - STOP_DIMENSION - 1;
            int areaIndex = dimension - STOP_DIMENSION;
            Node highest = head.higherPrevious[link];
            for (Node node = highest; node != head; node = node.higherPrevious[link]) {
                if (node.ignore < dimension) {
                    node.ignore = 0;
                }
            }

            Node next = head;
            while (count > 1 && (highest.point[dimension] > bound[dimension]
                    || highest.higherPrevious[link].point[dimension] >= bound[dimension])) {
                delete(highest, dimension);
                next = highest;
                highest = highest.higherPrevious[link];
                count--;
            }

            double volume = 0.0;
            if (count > 1) {
                Node previous = highest.higherPrevious[link];
                volume = previous.volume[areaIndex]
                        + previous.area[areaIndex]
                        * (highest.point[dimension] - previous.point[dimension]);
            } else {
                double area = 1.0;
                for (int objective = 0; objective < STOP_DIMENSION; objective++) {
                    area *= reference[objective] - highest.point[objective];
                }
                highest.area[0] = area;
                for (int i = 1; i <= areaIndex; i++) {
                    int objective = STOP_DIMENSION + i - 1;
                    highest.area[i] = highest.area[i - 1]
                            * (reference[objective] - highest.point[objective]);
                }
            }

            while (true) {
                highest.volume[areaIndex] = volume;
                Node previous = highest.higherPrevious[link];
                if (highest.ignore >= dimension) {
                    highest.area[areaIndex] = previous.area[areaIndex];
                } else {
                    if (dimension - 1 == STOP_DIMENSION) {
                        highest.area[areaIndex] = previous.area[areaIndex]
                                + oneContribution4d(highest);
                    } else {
                        highest.area[areaIndex] = recursive(dimension - 1, count);
                    }
                    if (highest.ignore == dimension - 1) {
                        highest.ignore = dimension;
                    }
                }

                if (next == head) {
                    volume += highest.area[areaIndex]
                            * (reference[dimension] - highest.point[dimension]);
                    return volume;
                }
                volume += highest.area[areaIndex]
                        * (next.point[dimension] - highest.point[dimension]);
                bound[dimension] = next.point[dimension];
                reinsert(next, dimension);
                count++;
                highest = next;
                next = next.higherNext[link];
            }
        }

        private double oneContribution4d(Node candidate) {
            double contribution = Hypervolume4d.contribution(baseList, candidate);
            if (contribution <= 0.0) {
                candidate.ignore = STOP_DIMENSION;
                return 0.0;
            }
            return contribution;
        }

        private void delete(Node node, int dimension) {
            for (int objective = STOP_DIMENSION + 1; objective < dimension; objective++) {
                int link = objective - STOP_DIMENSION - 1;
                node.higherPrevious[link].higherNext[link] = node.higherNext[link];
                node.higherNext[link].higherPrevious[link] = node.higherPrevious[link];
                bound[objective] = Math.min(bound[objective], node.point[objective]);
            }
            deleteBase(node);
            bound[STOP_DIMENSION] = Math.min(bound[STOP_DIMENSION],
                    node.point[STOP_DIMENSION]);
        }

        private void reinsert(Node node, int dimension) {
            for (int objective = STOP_DIMENSION + 1; objective < dimension; objective++) {
                int link = objective - STOP_DIMENSION - 1;
                node.higherPrevious[link].higherNext[link] = node;
                node.higherNext[link].higherPrevious[link] = node;
                bound[objective] = Math.min(bound[objective], node.point[objective]);
            }
            reinsertBase(node);
            bound[STOP_DIMENSION] = Math.min(bound[STOP_DIMENSION],
                    node.point[STOP_DIMENSION]);
        }

        private static void deleteBase(Node node) {
            for (int link = 0; link < 2; link++) {
                node.previous[link].next[link] = node.next[link];
                node.next[link].previous[link] = node.previous[link];
            }
        }

        private static void reinsertBase(Node node) {
            for (int link = 0; link < 2; link++) {
                node.previous[link].next[link] = node;
                node.next[link].previous[link] = node;
            }
        }

        private int countNodes() {
            int count = 0;
            for (Node node = baseList.second().next[0];
                 node != baseList.last(); node = node.next[0]) {
                count++;
            }
            return count;
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
