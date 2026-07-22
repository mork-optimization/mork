// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * HV4D+ as implemented by upstream moocore in {@code c/hv4d_priv.h}.
 *
 * <p>The point links replace the contiguous C node array, but the sweep,
 * contribution updates, and comparison order are unchanged.</p>
 */
final class Hypervolume4d {

    private Hypervolume4d() {
    }

    static double compute(double[][] input, double[] reference) {
        int[] order = new int[input.length];
        int count = 0;
        for (int row = 0; row < input.length; row++) {
            if (strictlyDominatesReference(input[row], reference)) {
                order[count++] = row;
            }
        }
        if (count == 0) {
            return 0.0;
        }
        KungParetoAlgorithms.sortByCoordinate(input, order, count, 3);

        Node firstSentinel = new Node(new double[]{
                -Double.MAX_VALUE, reference[1], -Double.MAX_VALUE, -Double.MAX_VALUE});
        Node secondSentinel = new Node(new double[]{
                reference[0], -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE});
        Node lastSentinel = new Node(new double[]{
                -Double.MAX_VALUE, -Double.MAX_VALUE, reference[2], reference[3]});
        resetSentinels(firstSentinel, secondSentinel, lastSentinel);

        Node previous = secondSentinel;
        for (int position = 0; position < count; position++) {
            Node node = new Node(input[order[position]]);
            previous.links[1] = node;
            node.links[3] = previous;
            previous = node;
        }
        previous.links[1] = lastSentinel;
        lastSentinel.links[3] = previous;

        return sweep(firstSentinel, secondSentinel, lastSentinel);
    }

    static BaseList createBaseList(Node[] nodes, double[] reference) {
        Node firstSentinel = new Node(new double[]{
                -Double.MAX_VALUE, reference[1], -Double.MAX_VALUE, -Double.MAX_VALUE});
        Node secondSentinel = new Node(new double[]{
                reference[0], -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE});
        Node lastSentinel = new Node(new double[]{
                -Double.MAX_VALUE, -Double.MAX_VALUE, reference[2], reference[3]});
        resetSentinels(firstSentinel, secondSentinel, lastSentinel);
        linkSorted(nodes, secondSentinel, lastSentinel, 2, 0);
        linkSorted(nodes, secondSentinel, lastSentinel, 3, 1);
        return new BaseList(firstSentinel, secondSentinel, lastSentinel);
    }

    private static void linkSorted(Node[] nodes, Node secondSentinel, Node lastSentinel,
                                   int objective, int link) {
        Node[] sorted = nodes.clone();
        Arrays.sort(sorted, Comparator.comparingDouble(node -> node.point[objective]));
        Node previous = secondSentinel;
        for (Node node : sorted) {
            previous.links[link] = node;
            node.links[2 + link] = previous;
            previous = node;
        }
        previous.links[link] = lastSentinel;
        lastSentinel.links[2 + link] = previous;
    }

    /** Computes one 4D contribution using upstream's {@code onec4dplusU} sweep. */
    static double contribution(BaseList list, Node candidate) {
        Node firstSentinel = list.first;
        Node secondSentinel = list.second;
        Node lastSentinel = list.last;
        Node zFirst = secondSentinel.links[0];
        Node zLast = lastSentinel.links[2];
        resetSentinels3d(firstSentinel, secondSentinel, lastSentinel);
        restartCurrentList(firstSentinel, secondSentinel);

        double[] candidatePoint = candidate.point;
        if (secondSentinel.links[1] != candidate
                || candidatePoint[3] == candidate.links[1].point[3]) {
            boolean added = false;
            int previousIgnore = candidate.ignore;
            candidate.ignore = 3;
            Node point = zFirst;

            while (point.point[2] <= candidatePoint[2]) {
                double[] current = point.point;
                if (current[3] <= candidatePoint[3] && point.ignore < 3) {
                    if (weaklyDominates3d(current, candidatePoint)) {
                        restoreZList(secondSentinel, lastSentinel, zFirst, zLast);
                        return 0.0;
                    }
                    Node auxiliary = new Node(upperBound3d(current, candidatePoint));
                    if (continueBaseUpdate(firstSentinel, secondSentinel,
                            lastSentinel, auxiliary, added)) {
                        added = true;
                    }
                }
                point = point.links[0];
            }
            candidate.ignore = previousIgnore;

            List<double[]> equalZ = new ArrayList<>();
            while (point != lastSentinel) {
                double[] current = point.point;
                if (current[3] <= candidatePoint[3] && point.ignore < 3) {
                    equalZ.add(upperBound3d(current, candidatePoint));
                }
                if (!equalZ.isEmpty() && point.links[0].point[2] > current[2]) {
                    if (equalZ.size() == 1) {
                        continueBaseUpdate(firstSentinel, secondSentinel, lastSentinel,
                                new Node(equalZ.getFirst()), false);
                    } else {
                        equalZ.sort(Comparator.comparingDouble((double[] p) -> p[1])
                                .thenComparingDouble(p -> p[0]));
                        double[] previous = null;
                        for (double[] intersection : equalZ) {
                            if (previous == null || intersection[0] < previous[0]) {
                                continueBaseUpdate(firstSentinel, secondSentinel, lastSentinel,
                                        new Node(intersection), false);
                            }
                            previous = intersection;
                        }
                    }
                    equalZ.clear();
                }
                point = point.links[0];
            }
        }

        Node point = candidate.links[1];
        while (point.point[3] <= candidatePoint[3]) {
            point = point.links[1];
        }

        Node candidatePreviousZ = candidate.links[2];
        Node candidateNextZ = candidate.links[0];
        if (!restartBaseAndFindClosest(firstSentinel, secondSentinel, candidate)) {
            candidate.ignore = 3;
            candidate.links[2] = candidatePreviousZ;
            candidate.links[0] = candidateNextZ;
            restoreZList(secondSentinel, lastSentinel, zFirst, zLast);
            return 0.0;
        }
        double volume = oneContribution3d(candidate);
        candidate.links[2] = candidatePreviousZ;
        candidate.links[0] = candidateNextZ;
        double hypervolume = volume * (point.point[3] - candidatePoint[3]);

        while (point != lastSentinel) {
            double[] current = point.point;
            if (weaklyDominates3d(current, candidatePoint)) {
                break;
            }
            if (point.ignore < 3) {
                Node auxiliary = new Node(upperBound3d(current, candidatePoint));
                if (restartBaseAndFindClosest(firstSentinel, secondSentinel, auxiliary)) {
                    volume -= oneContribution3d(auxiliary);
                    addToZ(auxiliary);
                    updateLinks(lastSentinel, auxiliary);
                }
                if (weaklyDominates3d(candidatePoint, current)) {
                    point.ignore = 3;
                }
            }
            hypervolume += volume * (point.links[1].point[3] - current[3]);
            point = point.links[1];
        }
        restoreZList(secondSentinel, lastSentinel, zFirst, zLast);
        return hypervolume;
    }

    private static void restoreZList(Node secondSentinel, Node lastSentinel,
                                     Node zFirst, Node zLast) {
        secondSentinel.links[0] = zFirst;
        zFirst.links[2] = secondSentinel;
        lastSentinel.links[2] = zLast;
        zLast.links[0] = lastSentinel;
    }

    private static boolean continueBaseUpdate(Node firstSentinel, Node secondSentinel,
                                              Node lastSentinel, Node newPoint,
                                              boolean equalZ) {
        double[] candidate = newPoint.point;
        Node point = secondSentinel.links[5];
        while (point.point[1] < candidate[1]) {
            point = point.links[5];
        }
        if (point.point[1] != candidate[1] || point.point[0] > candidate[0]) {
            point = point.links[4];
        }
        if (weaklyDominates3d(point.point, candidate)) {
            return false;
        }

        Node lexicographicPrevious = point;
        point = point.links[5];
        if (equalZ) {
            while (point.point[0] >= candidate[0]) {
                removeFromZ(point);
                point = point.links[5];
            }
            if (point != firstSentinel) {
                point.links[6] = newPoint;
            }
            newPoint.links[2] = lexicographicPrevious;
            newPoint.links[0] = lexicographicPrevious.links[0];
            newPoint.links[7] = firstSentinel;
        } else {
            newPoint.links[2] = lastSentinel.links[2];
            newPoint.links[0] = lastSentinel;
            while (point.point[0] >= candidate[0]) {
                point = point.links[5];
            }
            newPoint.links[7] = point;
        }

        newPoint.links[6] = lexicographicPrevious;
        lexicographicPrevious.links[5] = newPoint;
        newPoint.links[4] = lexicographicPrevious;
        newPoint.links[5] = point;
        point.links[4] = newPoint;
        newPoint.links[0].links[2] = newPoint;
        newPoint.links[2].links[0] = newPoint;
        return true;
    }

    private static double[] upperBound3d(double[] left, double[] right) {
        return new double[]{
                Math.max(left[0], right[0]),
                Math.max(left[1], right[1]),
                Math.max(left[2], right[2])};
    }

    private static boolean weaklyDominates3d(double[] left, double[] right) {
        return left[0] <= right[0] && left[1] <= right[1] && left[2] <= right[2];
    }

    private static double sweep(Node firstSentinel, Node secondSentinel, Node lastSentinel) {
        double volume = 0.0;
        double hypervolume = 0.0;
        Node point = secondSentinel.links[1];
        while (point != lastSentinel) {
            if (restartBaseAndFindClosest(firstSentinel, secondSentinel, point)) {
                volume += oneContribution3d(point);
                addToZ(point);
                updateLinks(lastSentinel, point);
            }
            hypervolume += volume * (point.links[1].point[3] - point.point[3]);
            point = point.links[1];
        }
        return hypervolume;
    }

    private static boolean restartBaseAndFindClosest(Node firstSentinel,
                                                     Node secondSentinel,
                                                     Node newPoint) {
        double[] candidate = newPoint.point;
        Node closestX = secondSentinel;
        Node closestY = firstSentinel;
        double closestX0 = closestX.point[0];
        double closestX1 = closestX.point[1];
        double closestY0 = closestY.point[0];
        double closestY1 = closestY.point[1];
        Node point = secondSentinel.links[0];
        restartCurrentList(firstSentinel, secondSentinel);

        while (true) {
            double[] current = point.point;
            boolean lessX = current[0] < candidate[0];
            boolean lessY = current[1] < candidate[1];
            boolean lessZ = current[2] < candidate[2];
            boolean equalX = current[0] == candidate[0];
            boolean equalY = current[1] == candidate[1];
            boolean equalZ = current[2] == candidate[2];
            boolean atMostX = lessX || equalX;
            boolean atMostY = lessY || equalY;
            boolean atMostZ = lessZ || equalZ;

            if (atMostX && atMostY && atMostZ) {
                return false;
            }
            if (!(lessZ || (equalZ && (lessY || (equalY && atMostX))))) {
                newPoint.links[6] = closestX;
                newPoint.links[7] = closestY;
                newPoint.links[2] = point.links[2];
                newPoint.links[0] = point;
                return true;
            }

            restoreCurrentLinks(point);
            if (lessY && (current[0] < closestX0
                    || (current[0] == closestX0 && current[1] < closestX1))) {
                closestX = point;
                closestX0 = current[0];
                closestX1 = current[1];
            } else if (lessX && (current[1] < closestY1
                    || (current[1] == closestY1 && current[0] < closestY0))) {
                closestY = point;
                closestY0 = current[0];
                closestY1 = current[1];
            }
            point = point.links[0];
        }
    }

    private static double oneContribution3d(Node newPoint) {
        setCurrentToClosest(newPoint);
        double[] candidate = newPoint.point;
        double area = computeAreaNoInners(candidate, newPoint.links[4], 1);
        double volume = 0.0;
        double lastZ = candidate[2];
        Node point = newPoint.links[0];

        while (true) {
            double[] current = point.point;
            volume += area * (current[2] - lastZ);
            if (current[0] <= candidate[0] && current[1] <= candidate[1]) {
                return volume;
            }

            setCurrentToClosest(point);
            if (current[0] < candidate[0]) {
                if (current[1] <= newPoint.links[5].point[1]) {
                    double[] bound = {candidate[0], current[1]};
                    area -= computeAreaNoInners(bound, newPoint.links[5], 0);
                    point.links[5] = newPoint.links[5];
                    point.links[4].links[5] = point;
                    newPoint.links[5] = point;
                }
            } else if (current[1] < candidate[1]) {
                if (current[0] <= newPoint.links[4].point[0]) {
                    double[] bound = {current[0], candidate[1]};
                    area -= computeAreaNoInners(bound, newPoint.links[4], 1);
                    point.links[4] = newPoint.links[4];
                    point.links[5].links[4] = point;
                    newPoint.links[4] = point;
                }
            } else {
                area -= computeAreaNoInners(current, point.links[4], 1);
                point.links[5].links[4] = point;
                point.links[4].links[5] = point;
            }
            lastZ = current[2];
            point = point.links[0];
        }
    }

    private static void updateLinks(Node lastSentinel, Node newPoint) {
        double[] candidate = newPoint.point;
        Node point = newPoint.links[0];
        while (point != lastSentinel) {
            double[] current = point.point;
            if (current[0] <= candidate[0] && current[1] <= candidate[1]
                    && (current[0] < candidate[0] || current[1] < candidate[1])) {
                return;
            }
            if (candidate[0] <= current[0]) {
                if (candidate[1] <= current[1]) {
                    removeFromZ(point);
                } else if (candidate[0] < current[0]
                        && lexicographic102(candidate, point.links[7].point)) {
                    point.links[7] = newPoint;
                }
            } else if (candidate[1] < current[1]
                    && lexicographic012(candidate, point.links[6].point)) {
                point.links[6] = newPoint;
            }
            point = point.links[0];
        }
    }

    private static double computeAreaNoInners(double[] point, Node boundary, int dimension) {
        int other = 1 - dimension;
        Node next = boundary.links[4 + dimension];
        double area = (boundary.point[other] - point[other])
                * (next.point[dimension] - point[dimension]);
        while (point[other] < next.point[other]) {
            boundary = next;
            next = next.links[4 + dimension];
            area += (boundary.point[other] - point[other])
                    * (next.point[dimension] - boundary.point[dimension]);
        }
        return area;
    }

    private static void addToZ(Node point) {
        point.links[0] = point.links[2].links[0];
        point.links[0].links[2] = point;
        point.links[2].links[0] = point;
    }

    private static void removeFromZ(Node point) {
        point.links[2].links[0] = point.links[0];
        point.links[0].links[2] = point.links[2];
    }

    private static void restartCurrentList(Node firstSentinel, Node secondSentinel) {
        firstSentinel.links[4] = secondSentinel;
        secondSentinel.links[5] = firstSentinel;
    }

    private static void restoreCurrentLinks(Node point) {
        setCurrentToClosest(point);
        point.links[4].links[5] = point;
        point.links[5].links[4] = point;
    }

    private static void setCurrentToClosest(Node point) {
        point.links[4] = point.links[6];
        point.links[5] = point.links[7];
    }

    private static boolean lexicographic102(double[] left, double[] right) {
        return left[1] < right[1]
                || (left[1] == right[1]
                && (left[0] < right[0]
                || (left[0] == right[0] && left[2] < right[2])));
    }

    private static boolean lexicographic012(double[] left, double[] right) {
        return left[0] < right[0]
                || (left[0] == right[0]
                && (left[1] < right[1]
                || (left[1] == right[1] && left[2] < right[2])));
    }

    private static boolean strictlyDominatesReference(double[] point, double[] reference) {
        for (int objective = 0; objective < 4; objective++) {
            if (point[objective] >= reference[objective]) {
                return false;
            }
        }
        return true;
    }

    private static void resetSentinels(Node first, Node second, Node last) {
        for (int dimension = 0; dimension < 2; dimension++) {
            first.links[dimension] = second;
            first.links[2 + dimension] = last;
            second.links[dimension] = last;
            second.links[2 + dimension] = first;
            last.links[dimension] = first;
            last.links[2 + dimension] = second;
        }
        first.links[6] = second;
        first.links[7] = first;
        second.links[6] = second;
        second.links[7] = first;
        last.links[6] = second;
        last.links[7] = first;
    }

    private static void resetSentinels3d(Node first, Node second, Node last) {
        first.links[0] = second;
        first.links[2] = last;
        second.links[0] = last;
        second.links[2] = first;
        last.links[0] = first;
        last.links[2] = second;
        first.links[6] = second;
        first.links[7] = first;
        second.links[6] = second;
        second.links[7] = first;
        last.links[6] = second;
        last.links[7] = first;
    }

    record BaseList(Node first, Node second, Node last) {
    }

    static final class Node {
        private static final Node[] NO_NODES = new Node[0];
        private static final double[] NO_VALUES = new double[0];

        final double[] point;
        final Node[] links = new Node[8];
        final Node[] higherNext;
        final Node[] higherPrevious;
        final double[] area;
        final double[] volume;
        int ignore;

        private Node(double[] point) {
            this(point, 0, 0);
        }

        Node(double[] point, int higherDimensions, int partialDimensions) {
            this.point = point;
            higherNext = higherDimensions == 0 ? NO_NODES : new Node[higherDimensions];
            higherPrevious = higherDimensions == 0 ? NO_NODES : new Node[higherDimensions];
            area = partialDimensions == 0 ? NO_VALUES : new double[partialDimensions];
            volume = partialDimensions == 0 ? NO_VALUES : new double[partialDimensions];
        }
    }
}
