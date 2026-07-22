// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Three-dimensional dimension sweep from the upstream eaf3d implementation. */
final class Eaf3d {

    private Eaf3d() {
    }

    static List<List<double[]>> compute(double[][] points, int[] pointSets,
                                        int numberOfSets, int[] requestedLevels) {
        int[] ordered = new int[points.length];
        for (int i = 0; i < points.length; i++) {
            ordered[i] = i;
        }
        KungParetoAlgorithms.sortByCoordinate(points, ordered, ordered.length, 2);

        Frontier[] sets = frontiers(numberOfSets);
        Frontier[] levels = frontiers(numberOfSets);
        @SuppressWarnings("unchecked")
        List<double[]>[] output = new List[numberOfSets];
        for (int i = 0; i < output.length; i++) {
            output[i] = new ArrayList<>();
        }

        int first = ordered[0];
        sets[pointSets[first]].addSet(points[first]);
        levels[0].addInitial(points[first].clone());
        boolean[] seenSet = new boolean[numberOfSets];
        seenSet[pointSets[first]] = true;
        int startAt = 0;

        SweepNode[] levelNodes = new SweepNode[numberOfSets];
        for (int pointIndex = 1; pointIndex < ordered.length; pointIndex++) {
            int current = ordered[pointIndex];
            double[] currentPoint = points[current];
            int currentPointSet = pointSets[current];
            Frontier currentSet = sets[currentPointSet];
            SweepNode currentSetPrevious = currentSet.atLeft(currentPoint[0]);
            if (currentSetPrevious.y <= currentPoint[1]) {
                continue;
            }

            int stopAt = 0;
            for (int level = startAt; level >= stopAt; level--) {
                SweepNode left = levels[level].atLeft(currentPoint[0]);
                if (left.y <= currentPoint[1]) {
                    stopAt = level + 1;
                } else if (left.y < currentSetPrevious.y) {
                    levelNodes[level] = SweepNode.intersection(
                            currentPoint[0], left.y, left.next);
                } else {
                    levelNodes[level] = levels[level].below(currentSetPrevious.y);
                }
            }

            SweepNode setNode = currentSetPrevious;
            do {
                setNode = setNode.next;
                double lowerBound = Math.max(setNode.y, currentPoint[1]);
                for (int level = startAt; level >= stopAt; level--) {
                    SweepNode levelNode = levelNodes[level];
                    while (levelNode.y >= lowerBound
                            && (levelNode.y > lowerBound || lowerBound > currentPoint[1])) {
                        if (setNode.x <= levelNode.x) {
                            levelNode = levels[level].below(setNode.y);
                        } else {
                            levels[level + 1].addLevel(
                                    new double[]{levelNode.x, levelNode.y, currentPoint[2]},
                                    output[level + 1]);
                            levelNode = levelNode.next;
                        }
                    }
                    levelNodes[level] = levelNode;
                }
            } while (setNode.y > currentPoint[1]);

            for (int level = startAt; level >= stopAt; level--) {
                SweepNode levelNode = levelNodes[level];
                if (levelNode.x < setNode.x) {
                    levels[level + 1].addLevel(
                            new double[]{levelNode.x, currentPoint[1], currentPoint[2]},
                            output[level + 1]);
                }
            }

            currentSet.addSet(currentPoint);
            levels[stopAt].addLevel(currentPoint.clone(), output[stopAt]);

            if (!seenSet[currentPointSet]) {
                if (startAt < numberOfSets - 2) {
                    startAt++;
                }
                seenSet[currentPointSet] = true;
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

    private static final class Frontier {
        private final PrimitiveIndex nodes = new PrimitiveIndex();
        private final PrimitiveIndex nodesByY = new PrimitiveIndex();

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
            return nodes.floor(x);
        }

        private SweepNode below(double y) {
            return nodesByY.lower(y);
        }

        private void addInitial(double[] point) {
            SweepNode previous = nodes.first();
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
            return nodesByY.higher(y);
        }

        private void appendTo(List<double[]> output) {
            SweepNode node = nodes.first().next;
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

    /** Array-backed AVL index with TreeMap-compatible replacement semantics. */
    private static final class PrimitiveIndex {
        private double[] keys = new double[16];
        private SweepNode[] values = new SweepNode[16];
        private int[] left = new int[16];
        private int[] right = new int[16];
        private int[] parent = new int[16];
        private int[] height = new int[16];
        private int root;
        private int allocated;
        private int free;

        private SweepNode get(double key) {
            int node = find(key);
            return node == 0 ? null : values[node];
        }

        private SweepNode first() {
            int node = root;
            while (left[node] != 0) {
                node = left[node];
            }
            return values[node];
        }

        private SweepNode floor(double key) {
            int node = root;
            int result = 0;
            while (node != 0) {
                int comparison = Double.compare(keys[node], key);
                if (comparison <= 0) {
                    result = node;
                    node = right[node];
                } else {
                    node = left[node];
                }
            }
            return values[result];
        }

        private SweepNode lower(double key) {
            int node = root;
            int result = 0;
            while (node != 0) {
                if (Double.compare(keys[node], key) < 0) {
                    result = node;
                    node = right[node];
                } else {
                    node = left[node];
                }
            }
            return values[result];
        }

        private SweepNode higher(double key) {
            int node = root;
            int result = 0;
            while (node != 0) {
                if (Double.compare(keys[node], key) > 0) {
                    result = node;
                    node = left[node];
                } else {
                    node = right[node];
                }
            }
            return values[result];
        }

        private void put(double key, SweepNode value) {
            if (root == 0) {
                root = allocate(key, value);
                return;
            }
            int node = root;
            while (true) {
                int comparison = Double.compare(key, keys[node]);
                if (comparison == 0) {
                    values[node] = value;
                    return;
                }
                int next = comparison < 0 ? left[node] : right[node];
                if (next != 0) {
                    node = next;
                    continue;
                }
                int inserted = allocate(key, value);
                parent[inserted] = node;
                if (comparison < 0) {
                    left[node] = inserted;
                } else {
                    right[node] = inserted;
                }
                rebalance(node);
                return;
            }
        }

        private void remove(double key, SweepNode expected) {
            int requested = find(key);
            if (requested == 0 || values[requested] != expected) {
                return;
            }
            int removed = requested;
            if (left[requested] != 0 && right[requested] != 0) {
                removed = right[requested];
                while (left[removed] != 0) {
                    removed = left[removed];
                }
                keys[requested] = keys[removed];
                values[requested] = values[removed];
            }

            int replacement = left[removed] != 0 ? left[removed] : right[removed];
            int oldParent = parent[removed];
            if (replacement != 0) {
                parent[replacement] = oldParent;
            }
            if (oldParent == 0) {
                root = replacement;
            } else if (left[oldParent] == removed) {
                left[oldParent] = replacement;
            } else {
                right[oldParent] = replacement;
            }
            rebalance(oldParent);
            release(removed);
        }

        private int find(double key) {
            int node = root;
            while (node != 0) {
                int comparison = Double.compare(key, keys[node]);
                if (comparison == 0) {
                    return node;
                }
                node = comparison < 0 ? left[node] : right[node];
            }
            return 0;
        }

        private int allocate(double key, SweepNode value) {
            int node;
            if (free != 0) {
                node = free;
                free = left[node];
            } else {
                node = ++allocated;
                ensureCapacity(node);
            }
            keys[node] = key;
            values[node] = value;
            left[node] = 0;
            right[node] = 0;
            parent[node] = 0;
            height[node] = 1;
            return node;
        }

        private void release(int node) {
            values[node] = null;
            right[node] = 0;
            parent[node] = 0;
            height[node] = 0;
            left[node] = free;
            free = node;
        }

        private void ensureCapacity(int requested) {
            if (requested < keys.length) {
                return;
            }
            int capacity = keys.length << 1;
            keys = Arrays.copyOf(keys, capacity);
            values = Arrays.copyOf(values, capacity);
            left = Arrays.copyOf(left, capacity);
            right = Arrays.copyOf(right, capacity);
            parent = Arrays.copyOf(parent, capacity);
            height = Arrays.copyOf(height, capacity);
        }

        private void rebalance(int node) {
            while (node != 0) {
                updateHeight(node);
                int subtreeRoot = node;
                int balance = height[left[node]] - height[right[node]];
                if (balance > 1) {
                    if (height[left[left[node]]] < height[right[left[node]]]) {
                        rotateLeft(left[node]);
                    }
                    subtreeRoot = rotateRight(node);
                } else if (balance < -1) {
                    if (height[right[right[node]]] < height[left[right[node]]]) {
                        rotateRight(right[node]);
                    }
                    subtreeRoot = rotateLeft(node);
                }
                node = parent[subtreeRoot];
            }
        }

        private int rotateLeft(int node) {
            int newRoot = right[node];
            int middle = left[newRoot];
            replaceParent(node, newRoot);
            left[newRoot] = node;
            parent[node] = newRoot;
            right[node] = middle;
            if (middle != 0) {
                parent[middle] = node;
            }
            updateHeight(node);
            updateHeight(newRoot);
            return newRoot;
        }

        private int rotateRight(int node) {
            int newRoot = left[node];
            int middle = right[newRoot];
            replaceParent(node, newRoot);
            right[newRoot] = node;
            parent[node] = newRoot;
            left[node] = middle;
            if (middle != 0) {
                parent[middle] = node;
            }
            updateHeight(node);
            updateHeight(newRoot);
            return newRoot;
        }

        private void replaceParent(int node, int replacement) {
            int oldParent = parent[node];
            parent[replacement] = oldParent;
            if (oldParent == 0) {
                root = replacement;
            } else if (left[oldParent] == node) {
                left[oldParent] = replacement;
            } else {
                right[oldParent] = replacement;
            }
        }

        private void updateHeight(int node) {
            height[node] = Math.max(height[left[node]], height[right[node]]) + 1;
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
