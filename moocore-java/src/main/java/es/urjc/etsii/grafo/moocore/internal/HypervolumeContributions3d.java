// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

/** Upstream moocore's HVC3D+ dimension-sweep algorithm. */
final class HypervolumeContributions3d {

    private HypervolumeContributions3d() {
    }

    static double[] compute(double[][] points, double[] reference) {
        double[] contributions = new double[points.length];
        int[] order = new int[points.length];
        int count = 0;
        for (int i = 0; i < points.length; i++) {
            if (strictlyDominatesReference(points[i], reference)) {
                order[count++] = i;
            }
        }
        if (count == 0) {
            return contributions;
        }
        KungParetoAlgorithms.sortLexicographically(points, order, count, 2, 1, 0);
        Node[] relevant = new Node[count];
        for (int position = 0; position < count; position++) {
            int row = order[position];
            relevant[position] = new Node(points[row], row);
        }

        Node firstSentinel = new Node(new double[]{
                -Double.MAX_VALUE, reference[1], -Double.MAX_VALUE}, -3);
        Node secondSentinel = new Node(new double[]{
                reference[0], -Double.MAX_VALUE, -Double.MAX_VALUE}, -2);
        Node lastSentinel = new Node(new double[]{
                -Double.MAX_VALUE, -Double.MAX_VALUE, reference[2]}, -1);
        resetSentinels(firstSentinel, secondSentinel, lastSentinel);
        linkByZ(relevant, secondSentinel, lastSentinel);
        preprocess(firstSentinel, secondSentinel, lastSentinel, count);
        sweep(firstSentinel, secondSentinel, lastSentinel);

        Node point = secondSentinel.next;
        while (point != lastSentinel) {
            if (!point.ignore) {
                contributions[point.index] = point.volume;
            }
            point = point.next;
        }
        return contributions;
    }

    private static void preprocess(Node firstSentinel, Node secondSentinel,
                                   Node lastSentinel, int count) {
        KungParetoAlgorithms.PrimitiveSkyline frontier =
                new KungParetoAlgorithms.PrimitiveSkyline(count + 2);
        Node[] frontierNodes = new Node[count + 2];
        int secondSentinelIndex = count;
        int firstSentinelIndex = count + 1;
        frontierNodes[secondSentinelIndex] = secondSentinel;
        frontierNodes[firstSentinelIndex] = firstSentinel;

        Node point = secondSentinel.next;
        frontierNodes[0] = point;
        frontier.add(canonicalZero(secondSentinel.point[1]), secondSentinel.point[0],
                secondSentinelIndex);
        frontier.add(canonicalZero(point.point[1]), point.point[0], 0);
        frontier.add(canonicalZero(firstSentinel.point[1]), firstSentinel.point[0],
                firstSentinelIndex);
        point.closest[0] = secondSentinel;
        point.closest[1] = firstSentinel;

        int pointIndex = 1;
        point = point.next;
        while (point != lastSentinel) {
            frontierNodes[pointIndex] = point;
            Node next = point.next;
            double y = canonicalZero(point.point[1]);
            int previousEntry = frontier.floor(y);
            Node previous = frontierNodes[frontier.payload(previousEntry)];
            if (frontier.key(previousEntry) == y) {
                if (previous.point[0] <= point.point[0]) {
                    if (equal3d(previous.point, point.point)) {
                        previous.ignore = true;
                    }
                    removeFromZ(point);
                    point = next;
                    pointIndex++;
                    continue;
                }
                frontier.remove(previousEntry);
                previousEntry = frontier.floor(y);
                previous = frontierNodes[frontier.payload(previousEntry)];
            }

            if (previous.point[0] <= point.point[0]) {
                removeFromZ(point);
            } else {
                int successorEntry = frontier.ceiling(y);
                Node successor = frontierNodes[frontier.payload(successorEntry)];
                while (successor.point[0] >= point.point[0]) {
                    successorEntry = frontier.remove(successorEntry);
                    successor = frontierNodes[frontier.payload(successorEntry)];
                }
                point.closest[0] = previous;
                point.closest[1] = successor;
                frontier.add(y, point.point[0], pointIndex);
            }
            point = next;
            pointIndex++;
        }
    }

    private static double sweep(Node firstSentinel, Node secondSentinel, Node lastSentinel) {
        firstSentinel.currentNext[0] = secondSentinel;
        secondSentinel.currentNext[1] = firstSentinel;
        Node point = secondSentinel.next;
        if (point == lastSentinel) {
            return 0.0;
        }

        point.volume = 0.0;
        point.lastSliceZ = point.point[2];
        setupNondominatedPoint(point);
        point.area = computeArea(point.point, point.currentNext[0], point.head[1], 1);
        double area = point.area;
        addNondominatedPoint(point);
        double volume = area * (point.next.point[2] - point.point[2]);
        point = point.next;

        while (point != lastSentinel) {
            point.volume = 0.0;
            point.lastSliceZ = point.point[2];
            setupNondominatedPoint(point);
            updateVolume(point.point, point.head[1], 1);
            point.area = computeArea(point.point, point.currentNext[0], point.head[1], 1);
            area += point.area;

            Node delimiter = point.currentNext[0];
            delimiter.area -= computeArea(
                    delimiter.point[0], point.point[1],
                    point.head[1], delimiter.head[0], 0);

            delimiter = point.currentNext[1];
            delimiter.area -= computeArea(
                    point.point[0], delimiter.point[1],
                    point.head[0], delimiter.head[1], 1);

            addNondominatedPoint(point);
            volume += area * (point.next.point[2] - point.point[2]);
            point = point.next;
        }
        setupNondominatedPoint(lastSentinel);
        updateVolume(lastSentinel.point, lastSentinel.head[1], 1);
        return volume;
    }

    private static void setupNondominatedPoint(Node point) {
        point.currentNext[0] = point.closest[0];
        point.currentNext[1] = point.closest[1];
        point.head[1] = point.currentNext[0].currentNext[1];
        point.head[0] = point.currentNext[1].currentNext[0];
    }

    private static void addNondominatedPoint(Node point) {
        if (point.currentNext[0].head[1].point[1] >= point.point[1]) {
            point.currentNext[0].head[1] = point;
            point.currentNext[0].head[0] = point.currentNext[0].currentNext[0];
        } else {
            Node delimiter = point.currentNext[0].head[0];
            while (delimiter.point[1] >= point.point[1]) {
                delimiter = delimiter.currentNext[0];
            }
            point.currentNext[0].head[0] = delimiter;
            delimiter.currentNext[1] = point;
        }

        if (point.currentNext[1].head[0].point[0] >= point.point[0]) {
            point.currentNext[1].head[0] = point;
            point.currentNext[1].head[1] = point.currentNext[1].currentNext[1];
        } else {
            Node delimiter = point.currentNext[1].head[1];
            while (delimiter.point[0] >= point.point[0]) {
                delimiter = delimiter.currentNext[1];
            }
            point.currentNext[1].head[1] = delimiter;
            delimiter.currentNext[0] = point;
        }

        Node lowerNeighbour = point.currentNext[0].currentNext[1];
        if (lowerNeighbour.point[1] > point.point[1]
                || (lowerNeighbour.point[1] == point.point[1]
                && lowerNeighbour.point[0] > point.point[0])) {
            point.currentNext[0].currentNext[1] = point;
        }

        Node upperNeighbour = point.currentNext[1].currentNext[0];
        if (upperNeighbour.point[0] > point.point[0]
                || (upperNeighbour.point[0] == point.point[0]
                && upperNeighbour.point[1] > point.point[1])) {
            point.currentNext[1].currentNext[0] = point;
        }
    }

    private static void updateVolume(double[] point, Node delimiter, int dimension) {
        int other = 1 - dimension;
        updateVolume(delimiter.currentNext[other], point[2]);
        while (point[other] < delimiter.point[other]) {
            updateVolume(delimiter, point[2]);
            delimiter = delimiter.currentNext[dimension];
        }
        updateVolume(delimiter, point[2]);
    }

    private static void updateVolume(Node point, double z) {
        point.volume += point.area * (z - point.lastSliceZ);
        point.lastSliceZ = z;
    }

    private static double computeArea(double[] point, Node boundary, Node inner, int dimension) {
        return computeArea(point[0], point[1], boundary, inner, dimension);
    }

    private static double computeArea(
            double x, double y, Node boundary, Node inner, int dimension) {
        int other = 1 - dimension;
        double pointDimension = dimension == 0 ? x : y;
        double pointOther = other == 0 ? x : y;
        double area = (boundary.point[other] - pointOther)
                * (inner.point[dimension] - pointDimension);
        while (pointOther < inner.point[other]) {
            boundary = inner;
            inner = inner.currentNext[dimension];
            area += (boundary.point[other] - pointOther)
                    * (inner.point[dimension] - boundary.point[dimension]);
        }
        return area;
    }

    private static void linkByZ(Node[] points, Node secondSentinel, Node lastSentinel) {
        Node previous = secondSentinel;
        for (Node point : points) {
            previous.next = point;
            point.previous = previous;
            previous = point;
        }
        previous.next = lastSentinel;
        lastSentinel.previous = previous;
    }

    private static void resetSentinels(Node first, Node second, Node last) {
        first.next = second;
        first.previous = last;
        second.next = last;
        second.previous = first;
        last.next = first;
        last.previous = second;
        first.closest[0] = second;
        first.closest[1] = first;
        second.closest[0] = second;
        second.closest[1] = first;
        last.closest[0] = second;
        last.closest[1] = first;
        first.head[0] = first;
        first.head[1] = first;
        second.head[0] = second;
        second.head[1] = second;
        last.head[0] = last;
        last.head[1] = last;
    }

    private static void removeFromZ(Node point) {
        point.previous.next = point.next;
        point.next.previous = point.previous;
    }

    private static boolean strictlyDominatesReference(double[] point, double[] reference) {
        return point[0] < reference[0]
                && point[1] < reference[1]
                && point[2] < reference[2];
    }

    private static boolean equal3d(double[] left, double[] right) {
        return left[0] == right[0] && left[1] == right[1] && left[2] == right[2];
    }

    private static double canonicalZero(double value) {
        return value == 0.0 ? 0.0 : value;
    }

    private static final class Node {
        private final double[] point;
        private final int index;
        private final Node[] currentNext = new Node[2];
        private final Node[] closest = new Node[2];
        private final Node[] head = new Node[2];
        private Node next;
        private Node previous;
        private double area;
        private double volume;
        private double lastSliceZ;
        private boolean ignore;

        private Node(double[] point, int index) {
            this.point = point;
            this.index = index;
        }
    }
}
