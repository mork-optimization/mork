// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/** Upstream HV3D+ preprocessing and dimension sweep. */
final class Hypervolume3d {

    private Hypervolume3d() {
    }

    static double compute(double[][] input, double[] reference) {
        Node[] points = relevant(input, reference);
        if (points.length == 0) {
            return 0.0;
        }
        Arrays.sort(points, (left, right) -> numericCompare(left.z, right.z));
        preprocess(points, reference);
        return sweep(points, reference);
    }

    private static Node[] relevant(double[][] input, double[] reference) {
        Node[] points = new Node[input.length];
        int size = 0;
        for (double[] point : input) {
            if (point[0] < reference[0]
                    && point[1] < reference[1]
                    && point[2] < reference[2]) {
                points[size++] = new Node(point[0], point[1], point[2]);
            }
        }
        return Arrays.copyOf(points, size);
    }

    private static void preprocess(Node[] points, double[] reference) {
        Node lower = new Node(reference[0], -Double.MAX_VALUE, -Double.MAX_VALUE);
        Node upper = new Node(-Double.MAX_VALUE, reference[1], -Double.MAX_VALUE);
        TreeMap<Double, Node> skyline = new TreeMap<>();
        skyline.put(lower.y, lower);
        skyline.put(upper.y, upper);

        for (Node point : points) {
            double y = canonicalZero(point.y);
            Map.Entry<Double, Node> previous = skyline.floorEntry(y);
            if (previous.getValue().x <= point.x) {
                point.dominated = true;
                continue;
            }

            Map.Entry<Double, Node> next = skyline.ceilingEntry(y);
            while (next.getValue().x >= point.x) {
                skyline.remove(next.getKey());
                next = skyline.ceilingEntry(y);
            }
            previous = skyline.lowerEntry(next.getKey());
            point.previousY = previous.getValue();
            point.nextY = next.getValue();
            skyline.put(y, point);
        }
    }

    private static double sweep(Node[] points, double[] reference) {
        Node first = firstNondominated(points, 0);
        Node lower = first.previousY;
        Node upper = first.nextY;
        lower.nextY = upper;
        upper.previousY = lower;

        double area = 0.0;
        double volume = 0.0;
        for (int index = 0; index < points.length; index++) {
            Node point = points[index];
            if (point.dominated) {
                continue;
            }
            area += areaContribution(point);
            point.previousY.nextY = point;
            point.nextY.previousY = point;

            int next = firstNondominatedIndex(points, index + 1);
            double nextZ = next < points.length ? points[next].z : reference[2];
            volume += area * (nextZ - point.z);
        }
        return volume;
    }

    private static double areaContribution(Node point) {
        Node previous = point.previousY;
        Node next = previous.nextY;
        double area = (previous.x - point.x) * (next.y - point.y);
        while (point.x < next.x) {
            previous = next;
            next = next.nextY;
            area += (previous.x - point.x) * (next.y - previous.y);
        }
        return area;
    }

    private static Node firstNondominated(Node[] points, int start) {
        return points[firstNondominatedIndex(points, start)];
    }

    private static int firstNondominatedIndex(Node[] points, int start) {
        int index = start;
        while (index < points.length && points[index].dominated) {
            index++;
        }
        return index;
    }

    private static int numericCompare(double left, double right) {
        return left == right ? 0 : Double.compare(left, right);
    }

    private static double canonicalZero(double value) {
        return value == 0.0 ? 0.0 : value;
    }

    private static final class Node {
        private final double x;
        private final double y;
        private final double z;
        private Node previousY;
        private Node nextY;
        private boolean dominated;

        private Node(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}
