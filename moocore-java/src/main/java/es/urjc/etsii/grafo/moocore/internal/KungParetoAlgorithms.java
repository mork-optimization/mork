// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import java.util.Arrays;

/**
 * Primitive-index implementation of the Kung-Luccio-Preparata maxima algorithm.
 *
 * <p>The recursion and cutoffs follow the implementation in upstream moocore's
 * {@code nondominated_kung.h}. Java row indices replace C row pointers and a
 * stable primitive double radix sort replaces its pointer-oriented radix sort.</p>
 */
final class KungParetoAlgorithms {

    static final int SMALL_THRESHOLD = 16;
    static final int MERGE_THRESHOLD = 1_024;

    private KungParetoAlgorithms() {
    }

    static boolean[] nondominated(double[][] points, boolean keepWeakly) {
        int dimensions = points[0].length;
        int[] rows = identity(points.length);
        int[] selected;
        if (rows.length <= SMALL_THRESHOLD) {
            selected = maximaBruteForce(points, rows, 0, dimensions, keepWeakly);
        } else {
            Workspace workspace = new Workspace(points.length);
            sortByCoordinate(points, rows, 0, workspace);
            selected = maxima(points, rows, 0, dimensions, keepWeakly, workspace);
        }
        boolean[] result = new boolean[points.length];
        for (int row : selected) {
            result[row] = true;
        }
        return result;
    }

    static boolean anyDominated(double[][] points, boolean keepWeakly) {
        int dimensions = points[0].length;
        for (int left = 0; left < points.length - 1; left++) {
            for (int right = left + 1; right < points.length; right++) {
                int comparison = dominance(points[left], points[right], 0, dimensions);
                if (comparison != 0
                        || (!keepWeakly && equal(points[left], points[right], 0, dimensions))) {
                    return true;
                }
            }
        }
        return false;
    }

    static int[] ranks(double[][] points) {
        int dimensions = points[0].length;
        int[] ranks = new int[points.length];
        if (points.length < 2) {
            return ranks;
        }

        int[] active = identity(points.length);
        Workspace workspace = new Workspace(points.length);
        if (dimensions == 3) {
            sortLexicographically(points, active, 0, 3, workspace);
        } else {
            sortByCoordinate(points, active, 0, workspace);
        }
        boolean[] currentFront = new boolean[points.length];
        int front = 0;
        while (active.length > 0) {
            int[] selected;
            if (dimensions == 3) {
                selected = maxima3dSorted(points, active, 0, true, workspace);
            } else if (active.length <= 2) {
                selected = maximaBruteForce(points, active, 0, dimensions, true);
            } else {
                selected = maximaKung(points, active, 0, dimensions, true, workspace);
            }
            for (int row : selected) {
                ranks[row] = front;
                currentFront[row] = true;
            }
            if (selected.length == active.length) {
                break;
            }
            int[] remaining = new int[active.length - selected.length];
            int next = 0;
            for (int row : active) {
                if (!currentFront[row]) {
                    remaining[next++] = row;
                }
            }
            for (int row : selected) {
                currentFront[row] = false;
            }
            active = remaining;
            front++;
        }
        return ranks;
    }

    static int[] sortReverseLexicographically2d(double[][] points) {
        int[] rows = identity(points.length);
        int[] scratch = new int[points.length];
        int[] counts = new int[256];
        radixSortByCoordinate(points, rows, 0, scratch, counts);
        radixSortByCoordinate(points, rows, 1, scratch, counts);
        return rows;
    }

    private static int[] maxima(double[][] points, int[] rows, int offset,
                                int dimensions, boolean keepWeakly, Workspace workspace) {
        if (rows.length <= SMALL_THRESHOLD) {
            return maximaBruteForce(points, rows, offset, dimensions, keepWeakly);
        }
        return maximaKung(points, rows, offset, dimensions, keepWeakly, workspace);
    }

    private static int[] maximaKung(double[][] points, int[] rows, int offset,
                                    int dimensions, boolean keepWeakly, Workspace workspace) {
        if (dimensions == 3) {
            return maxima3d(points, rows, offset, keepWeakly, workspace);
        }

        int split = halfSizeWithoutSplittingDuplicates(points, rows, offset);
        if (split == rows.length) {
            int[] shifted = rows.clone();
            sortByCoordinate(points, shifted, offset + 1, workspace);
            if (dimensions == 4) {
                return maxima3d(points, shifted, offset + 1, keepWeakly, workspace);
            }
            return maxima(points, shifted, offset + 1, dimensions - 1, keepWeakly, workspace);
        }

        int[] left = Arrays.copyOfRange(rows, 0, split);
        int[] right = Arrays.copyOfRange(rows, split, rows.length);
        left = maxima(points, left, offset, dimensions, keepWeakly, workspace);
        right = maxima(points, right, offset, dimensions, keepWeakly, workspace);
        right = mergeNextDimension(points, left, right, offset, dimensions, workspace);
        return concatenate(left, right);
    }

    private static int[] mergeNextDimension(double[][] points, int[] left, int[] right,
                                            int offset, int dimensions, Workspace workspace) {
        if (left.length == 0 || right.length == 0) {
            return right;
        }
        if ((long) left.length * right.length <= MERGE_THRESHOLD
                || left.length == 1 || right.length == 1) {
            return mergeBruteForce(points, left, right, offset + 1, dimensions - 1);
        }

        int[] shiftedLeft = left.clone();
        int[] shiftedRight = right.clone();
        sortByCoordinate(points, shiftedLeft, offset + 1, workspace);
        sortByCoordinate(points, shiftedRight, offset + 1, workspace);
        if (dimensions == 4) {
            return merge3d(points, shiftedLeft, shiftedRight, offset + 1, workspace);
        }
        return mergeWithoutBase(
                points, shiftedLeft, shiftedRight, offset + 1, dimensions - 1, workspace);
    }

    private static int[] merge(double[][] points, int[] left, int[] right,
                               int offset, int dimensions, Workspace workspace) {
        if (left.length == 0 || right.length == 0) {
            return right;
        }
        if ((long) left.length * right.length <= MERGE_THRESHOLD
                || left.length == 1 || right.length == 1) {
            return mergeBruteForce(points, left, right, offset, dimensions);
        }
        return mergeWithoutBase(points, left, right, offset, dimensions, workspace);
    }

    private static int[] mergeWithoutBase(double[][] points, int[] left, int[] right,
                                          int offset, int dimensions, Workspace workspace) {
        int rightSplit = halfSizeWithoutSplittingDuplicates(points, right, offset);
        int[] rightFirst = Arrays.copyOfRange(right, 0, rightSplit);
        int[] rightSecond = Arrays.copyOfRange(right, rightSplit, right.length);
        double pivot = points[right[rightSecond.length == 0 ? rightSplit - 1 : rightSplit]][offset];
        int leftSplit = upperBound(points, left, offset, pivot);
        int[] leftFirst = Arrays.copyOfRange(left, 0, leftSplit);
        int[] leftSecond = Arrays.copyOfRange(left, leftSplit, left.length);

        if (leftSecond.length == 0 && rightSecond.length == 0) {
            return mergeNextDimension(
                    points, leftFirst, rightFirst, offset, dimensions, workspace);
        }
        if (leftSecond.length > 0 && rightSecond.length > 0) {
            rightSecond = merge(
                    points, leftSecond, rightSecond, offset, dimensions, workspace);
        }
        if (leftFirst.length > 0) {
            rightFirst = merge(points, leftFirst, rightFirst, offset, dimensions, workspace);
            if (rightSecond.length > 0) {
                rightSecond = mergeNextDimension(
                        points, leftFirst, rightSecond, offset, dimensions, workspace);
            }
        }
        return concatenate(rightFirst, rightSecond);
    }

    private static int[] mergeBruteForce(double[][] points, int[] left, int[] right,
                                         int offset, int dimensions) {
        int[] result = new int[right.length];
        int size = 0;
        for (int candidate : right) {
            boolean dominated = false;
            for (int point : left) {
                if (weaklyDominates(points[point], points[candidate], offset, dimensions)) {
                    dominated = true;
                    break;
                }
            }
            if (!dominated) {
                result[size++] = candidate;
            }
        }
        return Arrays.copyOf(result, size);
    }

    private static int[] merge3d(double[][] points, int[] left, int[] right, int offset,
                                 Workspace workspace) {
        PrimitiveSkyline skyline = workspace.skyline;
        skyline.reset();
        int[] result = new int[right.length];
        int size = 0;
        int nextLeft = 0;
        for (int candidate : right) {
            double candidateFirst = points[candidate][offset];
            while (nextLeft < left.length
                    && numericCompare(points[left[nextLeft]][offset], candidateFirst) <= 0) {
                addToSkyline(skyline, points[left[nextLeft]], offset + 1, offset + 2);
                nextLeft++;
            }
            double second = canonicalZero(points[candidate][offset + 1]);
            int previous = skyline.floor(second);
            if (previous == 0 || skyline.value(previous) > points[candidate][offset + 2]) {
                result[size++] = candidate;
            }
        }
        return Arrays.copyOf(result, size);
    }

    private static int[] maxima3d(double[][] points, int[] input, int offset,
                                  boolean keepWeakly, Workspace workspace) {
        int[] rows = input.clone();
        sortLexicographically(points, rows, offset, 3, workspace);
        return maxima3dSorted(points, rows, offset, keepWeakly, workspace);
    }

    private static int[] maxima3dSorted(double[][] points, int[] rows, int offset,
                                        boolean keepWeakly, Workspace workspace) {
        PrimitiveSkyline skyline = workspace.skyline;
        skyline.reset();
        int[] result = new int[rows.length];
        int size = 0;
        int start = 0;
        while (start < rows.length) {
            int end = start + 1;
            while (end < rows.length
                    && equal(points[rows[start]], points[rows[end]], offset, 3)) {
                end++;
            }
            double second = canonicalZero(points[rows[start]][offset + 1]);
            double third = points[rows[start]][offset + 2];
            int previous = skyline.floor(second);
            boolean dominated = previous != 0 && skyline.value(previous) <= third;
            if (!dominated) {
                if (keepWeakly) {
                    for (int i = start; i < end; i++) {
                        result[size++] = rows[i];
                    }
                } else {
                    result[size++] = rows[start];
                }
                addToSkyline(skyline, points[rows[start]], offset + 1, offset + 2);
            }
            start = end;
        }
        return Arrays.copyOf(result, size);
    }

    private static void addToSkyline(PrimitiveSkyline skyline, double[] point,
                                     int secondObjective, int thirdObjective) {
        double second = canonicalZero(point[secondObjective]);
        double third = point[thirdObjective];
        int previous = skyline.floor(second);
        if (previous != 0 && skyline.value(previous) <= third) {
            return;
        }
        int current = skyline.ceiling(second);
        while (current != 0 && skyline.value(current) >= third) {
            current = skyline.remove(current);
        }
        skyline.add(second, third);
    }

    private static int[] maximaBruteForce(double[][] points, int[] rows, int offset,
                                          int dimensions, boolean keepWeakly) {
        boolean[] selected = new boolean[rows.length];
        Arrays.fill(selected, true);
        for (int candidate = 1; candidate < rows.length; candidate++) {
            for (int other = 0; other < candidate; other++) {
                if (!selected[other]) {
                    continue;
                }
                int comparison = dominance(
                        points[rows[other]], points[rows[candidate]], offset, dimensions);
                if (comparison < 0) {
                    selected[candidate] = false;
                    break;
                }
                if (comparison > 0) {
                    selected[other] = false;
                } else if (!keepWeakly
                        && equal(points[rows[other]], points[rows[candidate]], offset, dimensions)) {
                    if (rows[other] < rows[candidate]) {
                        selected[candidate] = false;
                        break;
                    }
                    selected[other] = false;
                }
            }
        }

        int size = 0;
        for (boolean value : selected) {
            if (value) {
                size++;
            }
        }
        int[] result = new int[size];
        int next = 0;
        for (int i = 0; i < rows.length; i++) {
            if (selected[i]) {
                result[next++] = rows[i];
            }
        }
        return result;
    }

    private static int dominance(double[] left, double[] right, int offset, int dimensions) {
        boolean less = false;
        boolean greater = false;
        int end = offset + dimensions;
        for (int objective = offset; objective < end; objective++) {
            less |= left[objective] < right[objective];
            greater |= left[objective] > right[objective];
            if (less && greater) {
                return 0;
            }
        }
        if (less) {
            return -1;
        }
        return greater ? 1 : 0;
    }

    private static boolean weaklyDominates(double[] left, double[] right,
                                           int offset, int dimensions) {
        int end = offset + dimensions;
        for (int objective = offset; objective < end; objective++) {
            if (left[objective] > right[objective]) {
                return false;
            }
        }
        return true;
    }

    private static boolean equal(double[] left, double[] right, int offset, int dimensions) {
        int end = offset + dimensions;
        for (int objective = offset; objective < end; objective++) {
            if (left[objective] != right[objective]) {
                return false;
            }
        }
        return true;
    }

    private static int halfSizeWithoutSplittingDuplicates(
            double[][] points, int[] rows, int objective) {
        int middle = rows.length / 2;
        double value = points[rows[middle]][objective];
        int left = middle;
        while (left > 0 && points[rows[left - 1]][objective] == value) {
            left--;
        }
        int right = middle + 1;
        while (right < rows.length && points[rows[right]][objective] == value) {
            right++;
        }
        if (left == 0) {
            return right;
        }
        if (right == rows.length) {
            return left;
        }
        return middle - left <= right - middle ? left : right;
    }

    private static int upperBound(double[][] points, int[] rows, int objective, double value) {
        int low = 0;
        int high = rows.length;
        while (low < high) {
            int middle = (low + high) >>> 1;
            if (numericCompare(points[rows[middle]][objective], value) <= 0) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return low;
    }

    private static void sortByCoordinate(double[][] points, int[] rows, int objective,
                                         Workspace workspace) {
        radixSortByCoordinate(points, rows, objective, workspace);
    }

    private static void sortLexicographically(double[][] points, int[] rows,
                                              int offset, int dimensions, Workspace workspace) {
        for (int objective = offset + dimensions - 1; objective >= offset; objective--) {
            radixSortByCoordinate(points, rows, objective, workspace);
        }
    }

    private static void radixSortByCoordinate(double[][] points, int[] rows, int objective,
                                              Workspace workspace) {
        radixSortByCoordinate(
                points, rows, objective, workspace.sortScratch, workspace.radixCounts);
    }

    private static void radixSortByCoordinate(double[][] points, int[] rows, int objective,
                                              int[] scratch, int[] counts) {
        int[] source = rows;
        int[] target = scratch;
        for (int shift = 0; shift < Long.SIZE; shift += Byte.SIZE) {
            Arrays.fill(counts, 0);
            for (int i = 0; i < rows.length; i++) {
                int row = source[i];
                int bucket = (int) ((sortableKey(points[row][objective]) >>> shift) & 0xffL);
                counts[bucket]++;
            }
            int position = 0;
            for (int bucket = 0; bucket < counts.length; bucket++) {
                int size = counts[bucket];
                counts[bucket] = position;
                position += size;
            }
            for (int i = 0; i < rows.length; i++) {
                int row = source[i];
                int bucket = (int) ((sortableKey(points[row][objective]) >>> shift) & 0xffL);
                target[counts[bucket]++] = row;
            }
            int[] swap = source;
            source = target;
            target = swap;
        }
    }

    private static long sortableKey(double value) {
        long bits = Double.doubleToRawLongBits(canonicalZero(value));
        return bits < 0 ? ~bits : bits ^ Long.MIN_VALUE;
    }

    private static int numericCompare(double left, double right) {
        return left == right ? 0 : Double.compare(left, right);
    }

    private static double canonicalZero(double value) {
        return value == 0.0 ? 0.0 : value;
    }

    private static int[] identity(int size) {
        int[] result = new int[size];
        for (int i = 0; i < size; i++) {
            result[i] = i;
        }
        return result;
    }

    private static int[] concatenate(int[] first, int[] second) {
        int[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /** Array-backed AVL tree for the two-coordinate skyline used by the 3D base case. */
    private static final class PrimitiveSkyline {
        private final double[] keys;
        private final double[] values;
        private final int[] left;
        private final int[] right;
        private final int[] parent;
        private final int[] height;
        private final int[] previous;
        private final int[] next;
        private int root;
        private int allocated;

        private PrimitiveSkyline(int capacity) {
            int arraySize = capacity + 1;
            keys = new double[arraySize];
            values = new double[arraySize];
            left = new int[arraySize];
            right = new int[arraySize];
            parent = new int[arraySize];
            height = new int[arraySize];
            previous = new int[arraySize];
            next = new int[arraySize];
        }

        private void reset() {
            root = 0;
            allocated = 0;
        }

        private int floor(double key) {
            int node = root;
            int result = 0;
            while (node != 0) {
                int comparison = numericCompare(keys[node], key);
                if (comparison <= 0) {
                    result = node;
                    node = right[node];
                } else {
                    node = left[node];
                }
            }
            return result;
        }

        private int ceiling(double key) {
            int node = root;
            int result = 0;
            while (node != 0) {
                int comparison = numericCompare(keys[node], key);
                if (comparison >= 0) {
                    result = node;
                    node = left[node];
                } else {
                    node = right[node];
                }
            }
            return result;
        }

        private double value(int node) {
            return values[node];
        }

        private void add(double key, double value) {
            int node = ++allocated;
            keys[node] = key;
            values[node] = value;
            left[node] = 0;
            right[node] = 0;
            parent[node] = 0;
            height[node] = 1;
            previous[node] = 0;
            next[node] = 0;
            if (root == 0) {
                root = node;
                return;
            }

            int current = root;
            int insertionParent;
            while (true) {
                insertionParent = current;
                if (numericCompare(key, keys[current]) < 0) {
                    current = left[current];
                    if (current == 0) {
                        left[insertionParent] = node;
                        next[node] = insertionParent;
                        previous[node] = previous[insertionParent];
                        if (previous[node] != 0) {
                            next[previous[node]] = node;
                        }
                        previous[insertionParent] = node;
                        break;
                    }
                } else {
                    current = right[current];
                    if (current == 0) {
                        right[insertionParent] = node;
                        previous[node] = insertionParent;
                        next[node] = next[insertionParent];
                        if (next[node] != 0) {
                            previous[next[node]] = node;
                        }
                        next[insertionParent] = node;
                        break;
                    }
                }
            }
            parent[node] = insertionParent;
            rebalance(insertionParent);
        }

        private int remove(int requested) {
            int node = requested;
            int following;
            if (left[node] != 0 && right[node] != 0) {
                int successor = next[node];
                keys[node] = keys[successor];
                values[node] = values[successor];
                node = successor;
                following = requested;
            } else {
                following = next[node];
            }

            int before = previous[node];
            int after = next[node];
            if (before != 0) {
                next[before] = after;
            }
            if (after != 0) {
                previous[after] = before;
            }

            int replacement = left[node] != 0 ? left[node] : right[node];
            int oldParent = parent[node];
            if (replacement != 0) {
                parent[replacement] = oldParent;
            }
            if (oldParent == 0) {
                root = replacement;
            } else if (left[oldParent] == node) {
                left[oldParent] = replacement;
            } else {
                right[oldParent] = replacement;
            }
            rebalance(oldParent);
            return following;
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

    private static final class Workspace {
        private final int[] sortScratch;
        private final int[] radixCounts = new int[256];
        private final PrimitiveSkyline skyline;

        private Workspace(int size) {
            sortScratch = new int[size];
            skyline = new PrimitiveSkyline(size);
        }
    }

}
