// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/** Three-dimensional dimension sweep from the upstream eaf3d implementation. */
final class Eaf3d {

    private Eaf3d() {
    }

    static List<List<double[]>> compute(double[][] points, int[] pointSets,
                                        int numberOfSets, int[] requestedLevels) {
        Point[] ordered = new Point[points.length];
        for (int i = 0; i < points.length; i++) {
            ordered[i] = new Point(points[i], pointSets[i]);
        }
        Arrays.sort(ordered, (left, right) -> Double.compare(left.values[2], right.values[2]));

        Frontier[] sets = frontiers(numberOfSets);
        Frontier[] levels = frontiers(numberOfSets);
        @SuppressWarnings("unchecked")
        List<double[]>[] output = new List[numberOfSets];
        for (int i = 0; i < output.length; i++) {
            output[i] = new ArrayList<>();
        }

        Point first = ordered[0];
        sets[first.set].addSet(first.values);
        levels[0].addInitial(first.values.clone());
        boolean[] seenSet = new boolean[numberOfSets];
        seenSet[first.set] = true;
        int startAt = 0;

        SweepNode[] levelNodes = new SweepNode[numberOfSets];
        for (int pointIndex = 1; pointIndex < ordered.length; pointIndex++) {
            Point current = ordered[pointIndex];
            Frontier currentSet = sets[current.set];
            SweepNode currentSetPrevious = currentSet.atLeft(current.values[0]);
            if (currentSetPrevious.y <= current.values[1]) {
                continue;
            }

            int stopAt = 0;
            for (int level = startAt; level >= stopAt; level--) {
                SweepNode left = levels[level].atLeft(current.values[0]);
                if (left.y <= current.values[1]) {
                    stopAt = level + 1;
                } else if (left.y < currentSetPrevious.y) {
                    levelNodes[level] = SweepNode.intersection(
                            current.values[0], left.y, left.next);
                } else {
                    levelNodes[level] = levels[level].below(currentSetPrevious.y);
                }
            }

            SweepNode setNode = currentSetPrevious;
            do {
                setNode = setNode.next;
                double lowerBound = Math.max(setNode.y, current.values[1]);
                for (int level = startAt; level >= stopAt; level--) {
                    SweepNode levelNode = levelNodes[level];
                    while (levelNode.y >= lowerBound
                            && (levelNode.y > lowerBound || lowerBound > current.values[1])) {
                        if (setNode.x <= levelNode.x) {
                            levelNode = levels[level].below(setNode.y);
                        } else {
                            levels[level + 1].addLevel(
                                    new double[]{levelNode.x, levelNode.y, current.values[2]},
                                    output[level + 1]);
                            levelNode = levelNode.next;
                        }
                    }
                    levelNodes[level] = levelNode;
                }
            } while (setNode.y > current.values[1]);

            for (int level = startAt; level >= stopAt; level--) {
                SweepNode levelNode = levelNodes[level];
                if (levelNode.x < setNode.x) {
                    levels[level + 1].addLevel(
                            new double[]{levelNode.x, current.values[1], current.values[2]},
                            output[level + 1]);
                }
            }

            currentSet.addSet(current.values);
            levels[stopAt].addLevel(current.values.clone(), output[stopAt]);

            if (!seenSet[current.set]) {
                if (startAt < numberOfSets - 2) {
                    startAt++;
                }
                seenSet[current.set] = true;
            }
        }

        for (int level = 0; level < numberOfSets; level++) {
            levels[level].appendTo(output[level]);
        }

        List<List<double[]>> result = new ArrayList<>(requestedLevels.length);
        for (int requestedLevel : requestedLevels) {
            List<double[]> surface = output[requestedLevel - 1];
            surface.sort(Eaf3d::compareLexicographically);
            result.add(surface);
        }
        return result;
    }

    private static Frontier[] frontiers(int count) {
        Frontier[] result = new Frontier[count];
        for (int i = 0; i < count; i++) {
            result[i] = new Frontier();
        }
        return result;
    }

    private static int compareLexicographically(double[] left, double[] right) {
        for (int i = 0; i < left.length; i++) {
            int comparison = Double.compare(left[i], right[i]);
            if (comparison != 0) {
                return comparison;
            }
        }
        return 0;
    }

    private record Point(double[] values, int set) {
    }

    private static final class Frontier {
        private final TreeMap<Double, SweepNode> nodes = new TreeMap<>();
        private final TreeMap<Double, SweepNode> nodesByY = new TreeMap<>();

        private Frontier() {
            SweepNode left = new SweepNode(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                    Double.NaN);
            SweepNode right = new SweepNode(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY,
                    Double.NaN);
            link(left, right);
            nodes.put(left.x, left);
            nodes.put(right.x, right);
            nodesByY.put(left.y, left);
            nodesByY.put(right.y, right);
        }

        private SweepNode atLeft(double x) {
            return nodes.floorEntry(x).getValue();
        }

        private SweepNode below(double y) {
            return nodesByY.lowerEntry(y).getValue();
        }

        private void addInitial(double[] point) {
            SweepNode previous = nodes.firstEntry().getValue();
            insertAfter(previous, new SweepNode(point));
        }

        private void addSet(double[] point) {
            SweepNode previous = atLeft(point[0]);
            if (previous.x == point[0]) {
                previous = previous.previous;
            }
            SweepNode inserted = new SweepNode(point);
            insertAfter(previous, inserted);
            SweepNode node = inserted.next;
            while (node.y >= inserted.y) {
                SweepNode next = node.next;
                remove(node);
                node = next;
            }
        }

        private void addLevel(double[] point, List<double[]> output) {
            SweepNode previous = nodeAbove(point[0], point[1]);
            SweepNode below = previous.next;
            if (below.x <= point[0]) {
                return;
            }

            SweepNode inserted = new SweepNode(point);
            insertAfter(previous, inserted);
            SweepNode node = inserted.previous;
            while (node.x >= inserted.x) {
                SweepNode prior = node.previous;
                remove(node);
                if (node.z < inserted.z) {
                    output.add(node.values());
                }
                node = prior;
            }
        }

        private SweepNode nodeAbove(double x, double y) {
            SweepNode equal = nodesByY.get(y);
            if (equal != null) {
                return equal.x > x ? equal : equal.previous;
            }
            return nodesByY.higherEntry(y).getValue();
        }

        private void appendTo(List<double[]> output) {
            SweepNode node = nodes.firstEntry().getValue().next;
            while (node.next != null) {
                output.add(node.values());
                node = node.next;
            }
        }

        private void insertAfter(SweepNode previous, SweepNode inserted) {
            SweepNode next = previous.next;
            link(previous, inserted);
            link(inserted, next);
            nodes.put(inserted.x, inserted);
            nodesByY.put(inserted.y, inserted);
        }

        private void remove(SweepNode node) {
            link(node.previous, node.next);
            nodes.remove(node.x, node);
            nodesByY.remove(node.y, node);
        }

        private static void link(SweepNode left, SweepNode right) {
            left.next = right;
            right.previous = left;
        }
    }

    private static final class SweepNode {
        private final double x;
        private final double y;
        private final double z;
        private SweepNode previous;
        private SweepNode next;

        private SweepNode(double[] point) {
            this(point[0], point[1], point[2]);
        }

        private SweepNode(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        private static SweepNode intersection(double x, double y, SweepNode next) {
            SweepNode result = new SweepNode(x, y, Double.NaN);
            result.next = next;
            return result;
        }

        private double[] values() {
            return new double[]{x, y, z};
        }
    }
}
