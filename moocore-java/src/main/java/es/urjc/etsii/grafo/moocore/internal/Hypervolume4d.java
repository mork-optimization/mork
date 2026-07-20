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
        List<double[]> relevant = new ArrayList<>();
        for (double[] point : input) {
            if (strictlyDominatesReference(point, reference)) {
                relevant.add(point);
            }
        }
        if (relevant.isEmpty()) {
            return 0.0;
        }
        relevant.sort(Comparator.comparingDouble(point -> point[3]));

        Node firstSentinel = new Node(new double[]{
                -Double.MAX_VALUE, reference[1], -Double.MAX_VALUE, -Double.MAX_VALUE});
        Node secondSentinel = new Node(new double[]{
                reference[0], -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE});
        Node lastSentinel = new Node(new double[]{
                -Double.MAX_VALUE, -Double.MAX_VALUE, reference[2], reference[3]});
        resetSentinels(firstSentinel, secondSentinel, lastSentinel);

        Node previous = secondSentinel;
        for (double[] point : relevant) {
            Node node = new Node(point);
            previous.next[1] = node;
            node.previous[1] = previous;
            previous = node;
        }
        previous.next[1] = lastSentinel;
        lastSentinel.previous[1] = previous;

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
            previous.next[link] = node;
            node.previous[link] = previous;
            previous = node;
        }
        previous.next[link] = lastSentinel;
        lastSentinel.previous[link] = previous;
    }

    /** Computes one 4D contribution using upstream's {@code onec4dplusU} sweep. */
    static double contribution(BaseList list, Node candidate) {
        Node firstSentinel = list.first;
        Node secondSentinel = list.second;
        Node lastSentinel = list.last;
        Node zFirst = secondSentinel.next[0];
        Node zLast = lastSentinel.previous[0];
        resetSentinels3d(firstSentinel, secondSentinel, lastSentinel);
        restartCurrentList(firstSentinel, secondSentinel);

        double[] candidatePoint = candidate.point;
        if (secondSentinel.next[1] != candidate
                || candidatePoint[3] == candidate.next[1].point[3]) {
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
                point = point.next[0];
            }
            candidate.ignore = previousIgnore;

            List<double[]> equalZ = new ArrayList<>();
            while (point != lastSentinel) {
                double[] current = point.point;
                if (current[3] <= candidatePoint[3] && point.ignore < 3) {
                    equalZ.add(upperBound3d(current, candidatePoint));
                }
                if (!equalZ.isEmpty() && point.next[0].point[2] > current[2]) {
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
                point = point.next[0];
            }
        }

        Node point = candidate.next[1];
        while (point.point[3] <= candidatePoint[3]) {
            point = point.next[1];
        }

        Node candidatePreviousZ = candidate.previous[0];
        Node candidateNextZ = candidate.next[0];
        if (!restartBaseAndFindClosest(firstSentinel, secondSentinel, candidate)) {
            candidate.ignore = 3;
            candidate.previous[0] = candidatePreviousZ;
            candidate.next[0] = candidateNextZ;
            restoreZList(secondSentinel, lastSentinel, zFirst, zLast);
            return 0.0;
        }
        double volume = oneContribution3d(candidate);
        candidate.previous[0] = candidatePreviousZ;
        candidate.next[0] = candidateNextZ;
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
            hypervolume += volume * (point.next[1].point[3] - current[3]);
            point = point.next[1];
        }
        restoreZList(secondSentinel, lastSentinel, zFirst, zLast);
        return hypervolume;
    }

    private static void restoreZList(Node secondSentinel, Node lastSentinel,
                                     Node zFirst, Node zLast) {
        secondSentinel.next[0] = zFirst;
        zFirst.previous[0] = secondSentinel;
        lastSentinel.previous[0] = zLast;
        zLast.next[0] = lastSentinel;
    }

    private static boolean continueBaseUpdate(Node firstSentinel, Node secondSentinel,
                                              Node lastSentinel, Node newPoint,
                                              boolean equalZ) {
        double[] candidate = newPoint.point;
        Node point = secondSentinel.currentNext[1];
        while (point.point[1] < candidate[1]) {
            point = point.currentNext[1];
        }
        if (point.point[1] != candidate[1] || point.point[0] > candidate[0]) {
            point = point.currentNext[0];
        }
        if (weaklyDominates3d(point.point, candidate)) {
            return false;
        }

        Node lexicographicPrevious = point;
        point = point.currentNext[1];
        if (equalZ) {
            while (point.point[0] >= candidate[0]) {
                removeFromZ(point);
                point = point.currentNext[1];
            }
            if (point != firstSentinel) {
                point.closest[0] = newPoint;
            }
            newPoint.previous[0] = lexicographicPrevious;
            newPoint.next[0] = lexicographicPrevious.next[0];
            newPoint.closest[1] = firstSentinel;
        } else {
            newPoint.previous[0] = lastSentinel.previous[0];
            newPoint.next[0] = lastSentinel;
            while (point.point[0] >= candidate[0]) {
                point = point.currentNext[1];
            }
            newPoint.closest[1] = point;
        }

        newPoint.closest[0] = lexicographicPrevious;
        lexicographicPrevious.currentNext[1] = newPoint;
        newPoint.currentNext[0] = lexicographicPrevious;
        newPoint.currentNext[1] = point;
        point.currentNext[0] = newPoint;
        newPoint.next[0].previous[0] = newPoint;
        newPoint.previous[0].next[0] = newPoint;
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
        Node point = secondSentinel.next[1];
        while (point != lastSentinel) {
            if (restartBaseAndFindClosest(firstSentinel, secondSentinel, point)) {
                volume += oneContribution3d(point);
                addToZ(point);
                updateLinks(lastSentinel, point);
            }
            hypervolume += volume * (point.next[1].point[3] - point.point[3]);
            point = point.next[1];
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
        Node point = secondSentinel.next[0];
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
                newPoint.closest[0] = closestX;
                newPoint.closest[1] = closestY;
                newPoint.previous[0] = point.previous[0];
                newPoint.next[0] = point;
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
            point = point.next[0];
        }
    }

    private static double oneContribution3d(Node newPoint) {
        setCurrentToClosest(newPoint);
        double[] candidate = newPoint.point;
        double area = computeAreaNoInners(candidate, newPoint.currentNext[0], 1);
        double volume = 0.0;
        double lastZ = candidate[2];
        Node point = newPoint.next[0];

        while (true) {
            double[] current = point.point;
            volume += area * (current[2] - lastZ);
            if (current[0] <= candidate[0] && current[1] <= candidate[1]) {
                return volume;
            }

            setCurrentToClosest(point);
            if (current[0] < candidate[0]) {
                if (current[1] <= newPoint.currentNext[1].point[1]) {
                    double[] bound = {candidate[0], current[1]};
                    area -= computeAreaNoInners(bound, newPoint.currentNext[1], 0);
                    point.currentNext[1] = newPoint.currentNext[1];
                    point.currentNext[0].currentNext[1] = point;
                    newPoint.currentNext[1] = point;
                }
            } else if (current[1] < candidate[1]) {
                if (current[0] <= newPoint.currentNext[0].point[0]) {
                    double[] bound = {current[0], candidate[1]};
                    area -= computeAreaNoInners(bound, newPoint.currentNext[0], 1);
                    point.currentNext[0] = newPoint.currentNext[0];
                    point.currentNext[1].currentNext[0] = point;
                    newPoint.currentNext[0] = point;
                }
            } else {
                area -= computeAreaNoInners(current, point.currentNext[0], 1);
                point.currentNext[1].currentNext[0] = point;
                point.currentNext[0].currentNext[1] = point;
            }
            lastZ = current[2];
            point = point.next[0];
        }
    }

    private static void updateLinks(Node lastSentinel, Node newPoint) {
        double[] candidate = newPoint.point;
        Node point = newPoint.next[0];
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
                        && lexicographic102(candidate, point.closest[1].point)) {
                    point.closest[1] = newPoint;
                }
            } else if (candidate[1] < current[1]
                    && lexicographic012(candidate, point.closest[0].point)) {
                point.closest[0] = newPoint;
            }
            point = point.next[0];
        }
    }

    private static double computeAreaNoInners(double[] point, Node boundary, int dimension) {
        int other = 1 - dimension;
        Node next = boundary.currentNext[dimension];
        double area = (boundary.point[other] - point[other])
                * (next.point[dimension] - point[dimension]);
        while (point[other] < next.point[other]) {
            boundary = next;
            next = next.currentNext[dimension];
            area += (boundary.point[other] - point[other])
                    * (next.point[dimension] - boundary.point[dimension]);
        }
        return area;
    }

    private static void addToZ(Node point) {
        point.next[0] = point.previous[0].next[0];
        point.next[0].previous[0] = point;
        point.previous[0].next[0] = point;
    }

    private static void removeFromZ(Node point) {
        point.previous[0].next[0] = point.next[0];
        point.next[0].previous[0] = point.previous[0];
    }

    private static void restartCurrentList(Node firstSentinel, Node secondSentinel) {
        firstSentinel.currentNext[0] = secondSentinel;
        secondSentinel.currentNext[1] = firstSentinel;
    }

    private static void restoreCurrentLinks(Node point) {
        setCurrentToClosest(point);
        point.currentNext[0].currentNext[1] = point;
        point.currentNext[1].currentNext[0] = point;
    }

    private static void setCurrentToClosest(Node point) {
        point.currentNext[0] = point.closest[0];
        point.currentNext[1] = point.closest[1];
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
            first.next[dimension] = second;
            first.previous[dimension] = last;
            second.next[dimension] = last;
            second.previous[dimension] = first;
            last.next[dimension] = first;
            last.previous[dimension] = second;
        }
        first.closest[0] = second;
        first.closest[1] = first;
        second.closest[0] = second;
        second.closest[1] = first;
        last.closest[0] = second;
        last.closest[1] = first;
    }

    private static void resetSentinels3d(Node first, Node second, Node last) {
        first.next[0] = second;
        first.previous[0] = last;
        second.next[0] = last;
        second.previous[0] = first;
        last.next[0] = first;
        last.previous[0] = second;
        first.closest[0] = second;
        first.closest[1] = first;
        second.closest[0] = second;
        second.closest[1] = first;
        last.closest[0] = second;
        last.closest[1] = first;
    }

    record BaseList(Node first, Node second, Node last) {
    }

    static final class Node {
        final double[] point;
        final Node[] next = new Node[2];
        final Node[] previous = new Node[2];
        final Node[] currentNext = new Node[2];
        final Node[] closest = new Node[2];
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
            higherNext = new Node[higherDimensions];
            higherPrevious = new Node[higherDimensions];
            area = new double[partialDimensions];
            volume = new double[partialDimensions];
        }
    }
}
