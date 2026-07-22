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
    private static final int INSERTION_SORT_THRESHOLD = 128;

    private KungParetoAlgorithms() {
    }

    static boolean[] nondominated(FlatPoints points, boolean keepWeakly) {
        int[] rows = identity(points.rows());
        Workspace workspace = new Workspace(points.rows());
        int selected = nondominated(
                points, rows, points.objectives(), keepWeakly, workspace);
        boolean[] result = new boolean[points.rows()];
        for (int i = 0; i < selected; i++) {
            result[rows[i]] = true;
        }
        return result;
    }

    static boolean anyDominated(FlatPoints points, boolean keepWeakly) {
        int dimensions = points.objectives();
        int earlyResult = earlyDominanceCheck(points, dimensions, keepWeakly);
        if (earlyResult >= 0) {
            return earlyResult != 0;
        }
        int[] rows = identity(points.rows());
        Workspace workspace = new Workspace(points.rows());
        return nondominated(points, rows, dimensions, keepWeakly, workspace) != points.rows();
    }

    /** Returns one for dominated, zero for an exhaustive antichain, and minus one at the cutoff. */
    private static int earlyDominanceCheck(
            FlatPoints points, int dimensions, boolean keepWeakly) {
        int checked = 0;
        for (int left = 0; left < points.rows() - 1; left++) {
            for (int right = left + 1; right < points.rows(); right++) {
                int comparison = dominance(points, left, right, 0, dimensions);
                if (comparison != 0
                        || (!keepWeakly && equal(points, left, right, 0, dimensions))) {
                    return 1;
                }
                if (++checked == EARLY_DOMINANCE_PAIRS) {
                    return -1;
                }
            }
        }
        return 0;
    }

    static int[] ranks(FlatPoints points) {
        int dimensions = points.objectives();
        int[] ranks = new int[points.rows()];
        if (points.rows() < 2) {
            return ranks;
        }

        int[] active = identity(points.rows());
        int[] candidates = new int[points.rows()];
        boolean[] currentFront = new boolean[points.rows()];
        Workspace workspace = new Workspace(points.rows());
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
        int order = coordinateOrder(points, rows, 0, rows.length, 0, maximise);
        if (order >= 0) {
            return rows;
        }
        if (order == -2) {
            reverseStable(points, rows, 0, rows.length, 0, maximise);
            return rows;
        }
        RadixWorkspace workspace = new RadixWorkspace(points.length);
        radixSortByCoordinate(points, rows, 0, rows.length, 0, maximise, workspace);
        return rows;
    }

    static int[] sortLexicographically2d(double[][] points) {
        int[] rows = identity(points.length);
        RadixWorkspace workspace = new RadixWorkspace(points.length);
        sortLexicographically(points, rows, 0, rows.length, 0, 2, workspace);
        return rows;
    }

    static void sortByCoordinate(double[][] points, int[] rows, int length, int objective) {
        RadixWorkspace workspace = new RadixWorkspace(points.length);
        radixSortByCoordinate(points, rows, 0, length, objective, workspace);
    }

    static void sortByCoordinate(double[][] points, int[] rows, int length,
                                 int objective, boolean maximise) {
        RadixWorkspace workspace = new RadixWorkspace(points.length);
        radixSortByCoordinate(points, rows, 0, length, objective, maximise, workspace);
    }

    static void sortLexicographically(double[][] points, int[] rows, int length,
                                      int... objectives) {
        RadixWorkspace workspace = new RadixWorkspace(points.length);
        if (objectives.length == 0) {
            return;
        }
        radixSortByCoordinate(points, rows, 0, length, objectives[0], workspace);
        for (int i = 1; i < objectives.length; i++) {
            refineTiedRuns(points, rows, 0, length, objectives, i, workspace);
        }
    }

    private static int nondominated(FlatPoints points, int[] rows, int dimensions,
                                    boolean keepWeakly, Workspace workspace) {
        if (rows.length <= SMALL_THRESHOLD) {
            return maximaBruteForce(
                    points, rows, 0, rows.length, 0, dimensions, keepWeakly,
                    workspace.selected);
        }
        sortByCoordinate(points, rows, 0, rows.length, 0, workspace);
        return maxima(points, rows, 0, rows.length, 0, dimensions, keepWeakly, workspace);
    }

    private static int maxima(FlatPoints points, int[] rows, int from, int to, int offset,
                              int dimensions, boolean keepWeakly, Workspace workspace) {
        if (to - from <= SMALL_THRESHOLD) {
            return maximaBruteForce(
                    points, rows, from, to, offset, dimensions, keepWeakly,
                    workspace.selected);
        }
        return maximaKung(points, rows, from, to, offset, dimensions, keepWeakly, workspace);
    }

    private static int maximaKung(FlatPoints points, int[] rows, int from, int to, int offset,
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

    private static int mergeNextDimension(FlatPoints points, int[] rows,
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

    private static int merge(FlatPoints points, int[] rows,
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

    private static int mergeWithoutBase(FlatPoints points, int[] rows,
                                        int leftFrom, int leftTo,
                                        int rightFrom, int rightTo,
                                        int offset, int dimensions, Workspace workspace) {
        int rightSplit = halfSizeWithoutSplittingDuplicates(
                points, rows, rightFrom, rightTo, offset);
        double pivot = points.get(
                rows[rightSplit == rightTo ? rightSplit - 1 : rightSplit], offset);
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

    private static int mergeBruteForce(FlatPoints points, int[] rows,
                                       int leftFrom, int leftTo,
                                       int rightFrom, int rightTo,
                                       int offset, int dimensions) {
        int write = rightFrom;
        for (int candidatePosition = rightFrom; candidatePosition < rightTo; candidatePosition++) {
            int candidate = rows[candidatePosition];
            boolean dominated = false;
            for (int pointPosition = leftFrom; pointPosition < leftTo; pointPosition++) {
                if (weaklyDominates(
                        points, rows[pointPosition], candidate, offset, dimensions)) {
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

    private static int merge3d(FlatPoints points, int[] rows,
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
            double candidateFirst = points.get(candidate, offset);
            while (nextLeft < leftTo
                    && numericCompare(points.get(rows[nextLeft], offset), candidateFirst) <= 0) {
                addToSkyline(skyline, points, rows[nextLeft], offset + 1, offset + 2);
                nextLeft++;
            }
            double second = canonicalZero(points.get(candidate, offset + 1));
            int previous = skyline.floor(second);
            if (previous == 0 || skyline.value(previous) > points.get(candidate, offset + 2)) {
                rows[write++] = candidate;
            }
        }
        return write;
    }

    private static int maxima3d(FlatPoints points, int[] rows, int from, int to, int offset,
                                boolean keepWeakly, Workspace workspace) {
        sortLexicographically(points, rows, from, to, offset, 3, workspace);
        return maxima3dSorted(points, rows, from, to, offset, keepWeakly, workspace);
    }

    private static int maxima3dSorted(FlatPoints points, int[] rows, int from, int to, int offset,
                                      boolean keepWeakly, Workspace workspace) {
        PrimitiveSkyline skyline = workspace.skyline;
        skyline.reset();
        int write = from;
        int start = from;
        while (start < to) {
            int firstRow = rows[start];
            int end = start + 1;
            while (end < to && equal(points, firstRow, rows[end], offset, 3)) {
                end++;
            }
            double second = canonicalZero(points.get(firstRow, offset + 1));
            double third = points.get(firstRow, offset + 2);
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
                addToSkyline(skyline, points, firstRow, offset + 1, offset + 2);
            }
            start = end;
        }
        return write;
    }

    private static void addToSkyline(PrimitiveSkyline skyline, FlatPoints points, int row,
                                     int secondObjective, int thirdObjective) {
        double second = canonicalZero(points.get(row, secondObjective));
        double third = points.get(row, thirdObjective);
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

    private static int maximaBruteForce(FlatPoints points, int[] rows, int from, int to,
                                        int offset, int dimensions, boolean keepWeakly,
                                        boolean[] selected) {
        Arrays.fill(selected, from, to, true);
        for (int candidate = from + 1; candidate < to; candidate++) {
            for (int other = from; other < candidate; other++) {
                if (!selected[other]) {
                    continue;
                }
                int comparison = dominance(
                        points, rows[other], rows[candidate], offset, dimensions);
                if (comparison < 0) {
                    selected[candidate] = false;
                    break;
                }
                if (comparison > 0) {
                    selected[other] = false;
                } else if (!keepWeakly
                        && equal(points, rows[other], rows[candidate], offset, dimensions)) {
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

    private static int dominance(FlatPoints points, int left, int right,
                                 int offset, int dimensions) {
        boolean less = false;
        boolean greater = false;
        int end = offset + dimensions;
        for (int objective = offset; objective < end; objective++) {
            less |= points.get(left, objective) < points.get(right, objective);
            greater |= points.get(left, objective) > points.get(right, objective);
            if (less && greater) {
                return 0;
            }
        }
        if (less) {
            return -1;
        }
        return greater ? 1 : 0;
    }

    private static boolean weaklyDominates(FlatPoints points, int left, int right,
                                           int offset, int dimensions) {
        int end = offset + dimensions;
        for (int objective = offset; objective < end; objective++) {
            if (points.get(left, objective) > points.get(right, objective)) {
                return false;
            }
        }
        return true;
    }

    private static boolean equal(FlatPoints points, int left, int right,
                                 int offset, int dimensions) {
        int end = offset + dimensions;
        for (int objective = offset; objective < end; objective++) {
            if (points.get(left, objective) != points.get(right, objective)) {
                return false;
            }
        }
        return true;
    }

    private static int halfSizeWithoutSplittingDuplicates(
            FlatPoints points, int[] rows, int from, int to, int objective) {
        int middle = (from + to) >>> 1;
        double value = points.get(rows[middle], objective);
        int left = middle;
        while (left > from && points.get(rows[left - 1], objective) == value) {
            left--;
        }
        int right = middle + 1;
        while (right < to && points.get(rows[right], objective) == value) {
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

    private static int upperBound(FlatPoints points, int[] rows, int from, int to,
                                  int objective, double value) {
        int low = from;
        int high = to;
        while (low < high) {
            int middle = (low + high) >>> 1;
            if (numericCompare(points.get(rows[middle], objective), value) <= 0) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return low;
    }

    private static void sortByCoordinate(FlatPoints points, int[] rows, int from, int to,
                                         int objective, RadixWorkspace workspace) {
        radixSortByCoordinate(points, rows, from, to, objective, workspace);
    }

    private static void sortLexicographically(FlatPoints points, int[] rows, int from, int to,
                                              int offset, int dimensions,
                                              RadixWorkspace workspace) {
        radixSortByCoordinate(points, rows, from, to, offset, workspace);
        for (int objective = offset + 1; objective < offset + dimensions; objective++) {
            int start = from;
            while (start < to) {
                int end = start + 1;
                while (end < to && equalPrefix(
                        points, rows[start], rows[end], offset, objective)) {
                    end++;
                }
                if (end - start > 1) {
                    radixSortByCoordinate(points, rows, start, end, objective, workspace);
                }
                start = end;
            }
        }
    }

    private static void sortLexicographically(double[][] points, int[] rows, int from, int to,
                                              int offset, int dimensions,
                                              RadixWorkspace workspace) {
        radixSortByCoordinate(points, rows, from, to, offset, workspace);
        for (int objective = offset + 1; objective < offset + dimensions; objective++) {
            int start = from;
            while (start < to) {
                int end = start + 1;
                while (end < to && equalPrefix(
                        points, rows[start], rows[end], offset, objective)) {
                    end++;
                }
                if (end - start > 1) {
                    radixSortByCoordinate(points, rows, start, end, objective, workspace);
                }
                start = end;
            }
        }
    }

    private static void refineTiedRuns(double[][] points, int[] rows, int from, int to,
                                       int[] objectives, int objectiveIndex,
                                       RadixWorkspace workspace) {
        int start = from;
        while (start < to) {
            int end = start + 1;
            while (end < to && equalPrefix(
                    points, rows[start], rows[end], objectives, objectiveIndex)) {
                end++;
            }
            if (end - start > 1) {
                radixSortByCoordinate(
                        points, rows, start, end, objectives[objectiveIndex], workspace);
            }
            start = end;
        }
    }

    private static void radixSortByCoordinate(FlatPoints points, int[] rows, int from, int to,
                                              int objective, RadixWorkspace workspace) {
        int size = to - from;
        if (size < 2) {
            return;
        }

        int order = coordinateOrder(points, rows, from, to, objective);
        if (order >= 0) {
            return;
        }
        if (order == -2) {
            reverseStable(points, rows, from, to, objective);
            return;
        }
        if (size <= INSERTION_SORT_THRESHOLD) {
            insertionSort(points, rows, from, to, objective);
            return;
        }

        int[] histogram = workspace.radixHistogram;
        Arrays.fill(histogram, 0);
        for (int i = from; i < to; i++) {
            int row = rows[i];
            long key = sortableKey(points.get(row, objective));
            workspace.sortKeys[row] = key;
            for (int pass = 0; pass < RADIX_PASSES; pass++) {
                histogram[pass * RADIX_SIZE
                        + (int) ((key >>> (pass * Byte.SIZE)) & 0xffL)]++;
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

    /** Returns zero for ascending, -2 for descending, and -1 for unordered. */
    private static int coordinateOrder(FlatPoints points, int[] rows, int from, int to,
                                       int objective) {
        boolean ascending = true;
        boolean descending = true;
        double previous = points.get(rows[from], objective);
        for (int i = from + 1; i < to && (ascending || descending); i++) {
            double current = points.get(rows[i], objective);
            int comparison = numericCompare(previous, current);
            ascending &= comparison <= 0;
            descending &= comparison >= 0;
            previous = current;
        }
        if (ascending) {
            return 0;
        }
        return descending ? -2 : -1;
    }

    private static void insertionSort(FlatPoints points, int[] rows, int from, int to,
                                      int objective) {
        for (int i = from + 1; i < to; i++) {
            int row = rows[i];
            double value = points.get(row, objective);
            int position = i;
            while (position > from
                    && numericCompare(points.get(rows[position - 1], objective), value) > 0) {
                rows[position] = rows[position - 1];
                position--;
            }
            rows[position] = row;
        }
    }

    private static void reverseStable(FlatPoints points, int[] rows, int from, int to,
                                      int objective) {
        reverse(rows, from, to);
        int start = from;
        while (start < to) {
            double value = points.get(rows[start], objective);
            int end = start + 1;
            while (end < to && points.get(rows[end], objective) == value) {
                end++;
            }
            reverse(rows, start, end);
            start = end;
        }
    }

    private static boolean equalPrefix(FlatPoints points, int left, int right,
                                       int offset, int end) {
        for (int objective = offset; objective < end; objective++) {
            if (points.get(left, objective) != points.get(right, objective)) {
                return false;
            }
        }
        return true;
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

        int order = coordinateOrder(points, rows, from, to, objective, maximise);
        if (order >= 0) {
            return;
        }
        if (order == -2) {
            reverseStable(points, rows, from, to, objective, maximise);
            return;
        }
        if (size <= INSERTION_SORT_THRESHOLD) {
            insertionSort(points, rows, from, to, objective, maximise);
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

    /** Returns zero for ascending, -2 for descending, and -1 for unordered. */
    private static int coordinateOrder(double[][] points, int[] rows, int from, int to,
                                       int objective, boolean maximise) {
        boolean ascending = true;
        boolean descending = true;
        double previous = directedValue(points[rows[from]][objective], maximise);
        for (int i = from + 1; i < to && (ascending || descending); i++) {
            double current = directedValue(points[rows[i]][objective], maximise);
            int comparison = numericCompare(previous, current);
            ascending &= comparison <= 0;
            descending &= comparison >= 0;
            previous = current;
        }
        if (ascending) {
            return 0;
        }
        return descending ? -2 : -1;
    }

    private static void insertionSort(double[][] points, int[] rows, int from, int to,
                                      int objective, boolean maximise) {
        for (int i = from + 1; i < to; i++) {
            int row = rows[i];
            double value = directedValue(points[row][objective], maximise);
            int position = i;
            while (position > from && numericCompare(
                    directedValue(points[rows[position - 1]][objective], maximise), value) > 0) {
                rows[position] = rows[position - 1];
                position--;
            }
            rows[position] = row;
        }
    }

    private static void reverseStable(double[][] points, int[] rows, int from, int to,
                                      int objective, boolean maximise) {
        reverse(rows, from, to);
        int start = from;
        while (start < to) {
            double value = directedValue(points[rows[start]][objective], maximise);
            int end = start + 1;
            while (end < to
                    && directedValue(points[rows[end]][objective], maximise) == value) {
                end++;
            }
            reverse(rows, start, end);
            start = end;
        }
    }

    private static void reverse(int[] rows, int from, int to) {
        for (int left = from, right = to - 1; left < right; left++, right--) {
            int value = rows[left];
            rows[left] = rows[right];
            rows[right] = value;
        }
    }

    private static boolean equalPrefix(double[][] points, int left, int right,
                                       int offset, int end) {
        for (int objective = offset; objective < end; objective++) {
            if (points[left][objective] != points[right][objective]) {
                return false;
            }
        }
        return true;
    }

    private static boolean equalPrefix(double[][] points, int left, int right,
                                       int[] objectives, int count) {
        for (int i = 0; i < count; i++) {
            int objective = objectives[i];
            if (points[left][objective] != points[right][objective]) {
                return false;
            }
        }
        return true;
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
    static final class PrimitiveSkyline {
        private final double[] keys;
        private final double[] values;
        private final int[] left;
        private final int[] right;
        private final int[] parent;
        private final int[] height;
        private final int[] previous;
        private final int[] next;
        private final int[] payloads;
        private int root;
        private int allocated;

        PrimitiveSkyline(int capacity) {
            int arraySize = capacity + 1;
            keys = new double[arraySize];
            values = new double[arraySize];
            left = new int[arraySize];
            right = new int[arraySize];
            parent = new int[arraySize];
            height = new int[arraySize];
            previous = new int[arraySize];
            next = new int[arraySize];
            payloads = new int[arraySize];
        }

        void reset() {
            root = 0;
            allocated = 0;
        }

        int floor(double key) {
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

        int ceiling(double key) {
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

        double key(int node) {
            return keys[node];
        }

        double value(int node) {
            return values[node];
        }

        int payload(int node) {
            return payloads[node];
        }

        void add(double key, double value) {
            add(key, value, 0);
        }

        void add(double key, double value, int payload) {
            int node = ++allocated;
            keys[node] = key;
            values[node] = value;
            payloads[node] = payload;
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

        int remove(int requested) {
            int node = requested;
            int following;
            if (left[node] != 0 && right[node] != 0) {
                int successor = next[node];
                keys[node] = keys[successor];
                values[node] = values[successor];
                payloads[node] = payloads[successor];
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
