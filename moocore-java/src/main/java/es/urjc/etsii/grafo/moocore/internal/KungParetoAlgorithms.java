// SPDX-License-Identifier: MPL-2.0
package es.urjc.etsii.grafo.moocore.internal;

import java.util.Arrays;

/** Primitive-index implementation of the Kung-Luccio-Preparata maxima algorithm. */
final class KungParetoAlgorithms {

    static final int SMALL_THRESHOLD = 16;
    static final int MERGE_THRESHOLD = 1_024;
    private static final int EARLY_DOMINANCE_PAIRS = 4_096;
    private static final int RADIX_SIZE = 256;
    private static final int RADIX_PASSES = Long.BYTES;

    private KungParetoAlgorithms() {
    }

    static boolean[] nondominated(double[][] points, boolean keepWeakly) {
        int[] rows = identity(points.length);
        Workspace workspace = new Workspace(points.length);
        int selected = nondominated(points, rows, points[0].length, keepWeakly, workspace);
        boolean[] result = new boolean[points.length];
        for (int i = 0; i < selected; i++) {
            result[rows[i]] = true;
        }
        return result;
    }

    static boolean anyDominated(double[][] points, boolean keepWeakly) {
        int dimensions = points[0].length;
        int earlyResult = earlyDominanceCheck(points, dimensions, keepWeakly);
        if (earlyResult >= 0) {
            return earlyResult != 0;
        }
        int[] rows = identity(points.length);
        Workspace workspace = new Workspace(points.length);
        return nondominated(points, rows, dimensions, keepWeakly, workspace) != points.length;
    }

    /** Returns one for dominated, zero for an exhaustive antichain, and minus one at the cutoff. */
    private static int earlyDominanceCheck(
            double[][] points, int dimensions, boolean keepWeakly) {
        int checked = 0;
        for (int left = 0; left < points.length - 1; left++) {
            for (int right = left + 1; right < points.length; right++) {
                int comparison = dominance(points[left], points[right], 0, dimensions);
                if (comparison != 0
                        || (!keepWeakly && equal(points[left], points[right], 0, dimensions))) {
                    return 1;
                }
                if (++checked == EARLY_DOMINANCE_PAIRS) {
                    return -1;
                }
            }
        }
        return 0;
    }

    static int[] ranks(double[][] points) {
        int dimensions = points[0].length;
        int[] ranks = new int[points.length];
        if (points.length < 2) {
            return ranks;
        }

        int[] active = identity(points.length);
        int[] candidates = new int[points.length];
        boolean[] currentFront = new boolean[points.length];
        Workspace workspace = new Workspace(points.length);
        if (dimensions == 3) {
            sortLexicographically(points, active, 0, active.length, 0, 3, workspace);
        } else {
            sortByCoordinate(points, active, 0, active.length, 0, workspace);
        }

        int activeSize = active.length;
        int front = 0;
        while (activeSize > 0) {
            System.arraycopy(active, 0, candidates, 0, activeSize);
            int selected;
            if (dimensions == 3) {
                selected = maxima3dSorted(
                        points, candidates, 0, activeSize, 0, true, workspace);
            } else if (activeSize <= SMALL_THRESHOLD) {
                selected = maximaBruteForce(
                        points, candidates, 0, activeSize, 0, dimensions, true,
                        workspace.selected);
            } else {
                selected = maximaKung(
                        points, candidates, 0, activeSize, 0, dimensions, true, workspace);
            }
            for (int i = 0; i < selected; i++) {
                int row = candidates[i];
                ranks[row] = front;
                currentFront[row] = true;
            }
            if (selected == activeSize) {
                break;
            }
            int next = 0;
            for (int i = 0; i < activeSize; i++) {
                int row = active[i];
                if (!currentFront[row]) {
                    active[next++] = row;
                }
            }
            for (int i = 0; i < selected; i++) {
                currentFront[candidates[i]] = false;
            }
            activeSize = next;
            front++;
        }
        return ranks;
    }

    static int[] sortLexicographically2dByFirst(double[][] points, boolean[] maximise) {
        int[] rows = identity(points.length);
        RadixWorkspace workspace = new RadixWorkspace(points.length);
        radixSortByCoordinate(points, rows, 0, rows.length, 0, maximise[0], workspace);
        int start = 0;
        while (start < rows.length) {
            double first = directedValue(points[rows[start]][0], maximise[0]);
            int end = start + 1;
            while (end < rows.length
                    && directedValue(points[rows[end]][0], maximise[0]) == first) {
                end++;
            }
            if (end - start > 1) {
                radixSortByCoordinate(points, rows, start, end, 1, maximise[1], workspace);
            }
            start = end;
        }
        return rows;
    }

    static int[] sortByFirstObjective2d(double[][] points, boolean maximise) {
        int[] rows = identity(points.length);
        boolean ascending = true;
        boolean descending = true;
        double previous = directedValue(points[0][0], maximise);
        for (int row = 1; row < points.length && (ascending || descending); row++) {
            double current = directedValue(points[row][0], maximise);
            ascending &= numericCompare(previous, current) <= 0;
            descending &= numericCompare(previous, current) >= 0;
            previous = current;
        }
        if (ascending) {
            return rows;
        }
        if (descending) {
            for (int left = 0, right = rows.length - 1; left < right; left++, right--) {
                rows[left] = right;
                rows[right] = left;
            }
            return rows;
        }
        RadixWorkspace workspace = new RadixWorkspace(points.length);
        radixSortByCoordinate(points, rows, 0, rows.length, 0, maximise, workspace);
        return rows;
    }

    static int[] sortLexicographically2d(double[][] points) {
        int[] rows = identity(points.length);
        RadixWorkspace workspace = new RadixWorkspace(points.length);
        radixSortByCoordinate(points, rows, 0, rows.length, 1, workspace);
        radixSortByCoordinate(points, rows, 0, rows.length, 0, workspace);
        return rows;
    }

    private static int nondominated(double[][] points, int[] rows, int dimensions,
                                    boolean keepWeakly, Workspace workspace) {
        if (rows.length <= SMALL_THRESHOLD) {
            return maximaBruteForce(
                    points, rows, 0, rows.length, 0, dimensions, keepWeakly,
                    workspace.selected);
        }
        sortByCoordinate(points, rows, 0, rows.length, 0, workspace);
        return maxima(points, rows, 0, rows.length, 0, dimensions, keepWeakly, workspace);
    }

    private static int maxima(double[][] points, int[] rows, int from, int to, int offset,
                              int dimensions, boolean keepWeakly, Workspace workspace) {
        if (to - from <= SMALL_THRESHOLD) {
            return maximaBruteForce(
                    points, rows, from, to, offset, dimensions, keepWeakly,
                    workspace.selected);
        }
        return maximaKung(points, rows, from, to, offset, dimensions, keepWeakly, workspace);
    }

    private static int maximaKung(double[][] points, int[] rows, int from, int to, int offset,
                                  int dimensions, boolean keepWeakly, Workspace workspace) {
        if (dimensions == 3) {
            return maxima3d(points, rows, from, to, offset, keepWeakly, workspace);
        }

        int split = halfSizeWithoutSplittingDuplicates(points, rows, from, to, offset);
        if (split == to) {
            sortByCoordinate(points, rows, from, to, offset + 1, workspace);
            if (dimensions == 4) {
                return maxima3d(points, rows, from, to, offset + 1, keepWeakly, workspace);
            }
            return maxima(
                    points, rows, from, to, offset + 1, dimensions - 1, keepWeakly, workspace);
        }

        int leftEnd = maxima(
                points, rows, from, split, offset, dimensions, keepWeakly, workspace);
        int rightEnd = maxima(
                points, rows, split, to, offset, dimensions, keepWeakly, workspace);
        rightEnd = mergeNextDimension(
                points, rows, from, leftEnd, split, rightEnd, offset, dimensions, workspace);
        int rightSize = rightEnd - split;
        System.arraycopy(rows, split, rows, leftEnd, rightSize);
        return leftEnd + rightSize;
    }

    private static int mergeNextDimension(double[][] points, int[] rows,
                                          int leftFrom, int leftTo,
                                          int rightFrom, int rightTo,
                                          int offset, int dimensions, Workspace workspace) {
        if (leftFrom == leftTo || rightFrom == rightTo) {
            return rightTo;
        }
        if ((long) (leftTo - leftFrom) * (rightTo - rightFrom) <= MERGE_THRESHOLD) {
            return mergeBruteForce(
                    points, rows, leftFrom, leftTo, rightFrom, rightTo,
                    offset + 1, dimensions - 1);
        }

        sortByCoordinate(points, rows, leftFrom, leftTo, offset + 1, workspace);
        sortByCoordinate(points, rows, rightFrom, rightTo, offset + 1, workspace);
        if (dimensions == 4) {
            return merge3d(
                    points, rows, leftFrom, leftTo, rightFrom, rightTo,
                    offset + 1, workspace);
        }
        return mergeWithoutBase(
                points, rows, leftFrom, leftTo, rightFrom, rightTo,
                offset + 1, dimensions - 1, workspace);
    }

    private static int merge(double[][] points, int[] rows,
                             int leftFrom, int leftTo, int rightFrom, int rightTo,
                             int offset, int dimensions, Workspace workspace) {
        if (leftFrom == leftTo || rightFrom == rightTo) {
            return rightTo;
        }
        if ((long) (leftTo - leftFrom) * (rightTo - rightFrom) <= MERGE_THRESHOLD) {
            return mergeBruteForce(
                    points, rows, leftFrom, leftTo, rightFrom, rightTo, offset, dimensions);
        }
        return mergeWithoutBase(
                points, rows, leftFrom, leftTo, rightFrom, rightTo,
                offset, dimensions, workspace);
    }

    private static int mergeWithoutBase(double[][] points, int[] rows,
                                        int leftFrom, int leftTo,
                                        int rightFrom, int rightTo,
                                        int offset, int dimensions, Workspace workspace) {
        int rightSplit = halfSizeWithoutSplittingDuplicates(
                points, rows, rightFrom, rightTo, offset);
        double pivot = points[rows[rightSplit == rightTo ? rightSplit - 1 : rightSplit]][offset];
        int leftSplit = upperBound(points, rows, leftFrom, leftTo, offset, pivot);

        if (leftSplit == leftTo && rightSplit == rightTo) {
            return mergeNextDimension(
                    points, rows, leftFrom, leftSplit, rightFrom, rightSplit,
                    offset, dimensions, workspace);
        }

        int rightSecondEnd = rightTo;
        if (leftSplit < leftTo && rightSplit < rightTo) {
            rightSecondEnd = merge(
                    points, rows, leftSplit, leftTo, rightSplit, rightTo,
                    offset, dimensions, workspace);
        }

        int rightFirstEnd = rightSplit;
        if (leftFrom < leftSplit) {
            rightFirstEnd = merge(
                    points, rows, leftFrom, leftSplit, rightFrom, rightSplit,
                    offset, dimensions, workspace);
            if (rightSplit < rightSecondEnd) {
                rightSecondEnd = mergeNextDimension(
                        points, rows, leftFrom, leftSplit, rightSplit, rightSecondEnd,
                        offset, dimensions, workspace);
            }
        }

        int secondSize = rightSecondEnd - rightSplit;
        System.arraycopy(rows, rightSplit, rows, rightFirstEnd, secondSize);
        return rightFirstEnd + secondSize;
    }

    private static int mergeBruteForce(double[][] points, int[] rows,
                                       int leftFrom, int leftTo,
                                       int rightFrom, int rightTo,
                                       int offset, int dimensions) {
        int write = rightFrom;
        for (int candidatePosition = rightFrom; candidatePosition < rightTo; candidatePosition++) {
            int candidate = rows[candidatePosition];
            boolean dominated = false;
            for (int pointPosition = leftFrom; pointPosition < leftTo; pointPosition++) {
                if (weaklyDominates(
                        points[rows[pointPosition]], points[candidate], offset, dimensions)) {
                    dominated = true;
                    break;
                }
            }
            if (!dominated) {
                rows[write++] = candidate;
            }
        }
        return write;
    }

    private static int merge3d(double[][] points, int[] rows,
                               int leftFrom, int leftTo, int rightFrom, int rightTo,
                               int offset, Workspace workspace) {
        PrimitiveSkyline skyline = workspace.skyline;
        skyline.reset();
        int write = rightFrom;
        int nextLeft = leftFrom;
        for (int candidatePosition = rightFrom;
             candidatePosition < rightTo;
             candidatePosition++) {
            int candidate = rows[candidatePosition];
            double candidateFirst = points[candidate][offset];
            while (nextLeft < leftTo
                    && numericCompare(points[rows[nextLeft]][offset], candidateFirst) <= 0) {
                addToSkyline(skyline, points[rows[nextLeft]], offset + 1, offset + 2);
                nextLeft++;
            }
            double second = canonicalZero(points[candidate][offset + 1]);
            int previous = skyline.floor(second);
            if (previous == 0 || skyline.value(previous) > points[candidate][offset + 2]) {
                rows[write++] = candidate;
            }
        }
        return write;
    }

    private static int maxima3d(double[][] points, int[] rows, int from, int to, int offset,
                                boolean keepWeakly, Workspace workspace) {
        sortLexicographically(points, rows, from, to, offset, 3, workspace);
        return maxima3dSorted(points, rows, from, to, offset, keepWeakly, workspace);
    }

    private static int maxima3dSorted(double[][] points, int[] rows, int from, int to, int offset,
                                      boolean keepWeakly, Workspace workspace) {
        PrimitiveSkyline skyline = workspace.skyline;
        skyline.reset();
        int write = from;
        int start = from;
        while (start < to) {
            int firstRow = rows[start];
            int end = start + 1;
            while (end < to && equal(points[firstRow], points[rows[end]], offset, 3)) {
                end++;
            }
            double second = canonicalZero(points[firstRow][offset + 1]);
            double third = points[firstRow][offset + 2];
            int previous = skyline.floor(second);
            boolean dominated = previous != 0 && skyline.value(previous) <= third;
            if (!dominated) {
                if (keepWeakly) {
                    for (int i = start; i < end; i++) {
                        rows[write++] = rows[i];
                    }
                } else {
                    rows[write++] = firstRow;
                }
                addToSkyline(skyline, points[firstRow], offset + 1, offset + 2);
            }
            start = end;
        }
        return write;
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

    private static int maximaBruteForce(double[][] points, int[] rows, int from, int to,
                                        int offset, int dimensions, boolean keepWeakly,
                                        boolean[] selected) {
        Arrays.fill(selected, from, to, true);
        for (int candidate = from + 1; candidate < to; candidate++) {
            for (int other = from; other < candidate; other++) {
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

        int write = from;
        for (int i = from; i < to; i++) {
            if (selected[i]) {
                rows[write++] = rows[i];
            }
        }
        return write;
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
            double[][] points, int[] rows, int from, int to, int objective) {
        int middle = (from + to) >>> 1;
        double value = points[rows[middle]][objective];
        int left = middle;
        while (left > from && points[rows[left - 1]][objective] == value) {
            left--;
        }
        int right = middle + 1;
        while (right < to && points[rows[right]][objective] == value) {
            right++;
        }
        if (left == from) {
            return right;
        }
        if (right == to) {
            return left;
        }
        return middle - left <= right - middle ? left : right;
    }

    private static int upperBound(double[][] points, int[] rows, int from, int to,
                                  int objective, double value) {
        int low = from;
        int high = to;
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

    private static void sortByCoordinate(double[][] points, int[] rows, int from, int to,
                                         int objective, RadixWorkspace workspace) {
        radixSortByCoordinate(points, rows, from, to, objective, workspace);
    }

    private static void sortLexicographically(double[][] points, int[] rows, int from, int to,
                                              int offset, int dimensions,
                                              RadixWorkspace workspace) {
        for (int objective = offset + dimensions - 1; objective >= offset; objective--) {
            radixSortByCoordinate(points, rows, from, to, objective, workspace);
        }
    }

    private static void radixSortByCoordinate(double[][] points, int[] rows, int from, int to,
                                              int objective, RadixWorkspace workspace) {
        radixSortByCoordinate(points, rows, from, to, objective, false, workspace);
    }

    private static void radixSortByCoordinate(double[][] points, int[] rows, int from, int to,
                                              int objective, boolean maximise,
                                              RadixWorkspace workspace) {
        int size = to - from;
        if (size < 2) {
            return;
        }

        int[] histogram = workspace.radixHistogram;
        Arrays.fill(histogram, 0);
        for (int i = from; i < to; i++) {
            int row = rows[i];
            double value = points[row][objective];
            long key = sortableKey(maximise ? -value : value);
            workspace.sortKeys[row] = key;
            for (int pass = 0; pass < RADIX_PASSES; pass++) {
                histogram[pass * RADIX_SIZE + (int) ((key >>> (pass * Byte.SIZE)) & 0xffL)]++;
            }
        }

        int[] source = rows;
        int[] target = workspace.sortScratch;
        for (int pass = 0; pass < RADIX_PASSES; pass++) {
            int histogramOffset = pass * RADIX_SIZE;
            boolean invariant = false;
            for (int bucket = 0; bucket < RADIX_SIZE; bucket++) {
                if (histogram[histogramOffset + bucket] == size) {
                    invariant = true;
                    break;
                }
            }
            if (invariant) {
                continue;
            }

            int position = from;
            for (int bucket = 0; bucket < RADIX_SIZE; bucket++) {
                int count = histogram[histogramOffset + bucket];
                histogram[histogramOffset + bucket] = position;
                position += count;
            }
            int shift = pass * Byte.SIZE;
            for (int i = from; i < to; i++) {
                int row = source[i];
                int bucket = (int) ((workspace.sortKeys[row] >>> shift) & 0xffL);
                target[histogram[histogramOffset + bucket]++] = row;
            }
            int[] swap = source;
            source = target;
            target = swap;
        }
        if (source != rows) {
            System.arraycopy(source, from, rows, from, size);
        }
    }

    private static long sortableKey(double value) {
        long bits = Double.doubleToRawLongBits(canonicalZero(value));
        return bits < 0 ? ~bits : bits ^ Long.MIN_VALUE;
    }

    private static double directedValue(double value, boolean maximise) {
        return maximise ? -value : value;
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

    private static class RadixWorkspace {
        private final int[] sortScratch;
        private final long[] sortKeys;
        private final int[] radixHistogram = new int[RADIX_SIZE * RADIX_PASSES];

        private RadixWorkspace(int size) {
            sortScratch = new int[size];
            sortKeys = new long[size];
        }
    }

    private static final class Workspace extends RadixWorkspace {
        private final boolean[] selected;
        private final PrimitiveSkyline skyline;

        private Workspace(int size) {
            super(size);
            selected = new boolean[size];
            skyline = new PrimitiveSkyline(size);
        }
    }
}
