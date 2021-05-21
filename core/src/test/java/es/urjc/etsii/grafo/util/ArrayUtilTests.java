package es.urjc.etsii.grafo.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class ArrayUtilTests {

    @Test
    public void testInsertAndDeleteRightIntegersDistance1() {
        Integer[] original = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        Integer[] expected = new Integer[]{1, 0, 2, 3, 4, 5, 6, 7, 8, 9};
        ArrayUtils.deleteAndInsert(original, 0, 1);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    public void testInsertAndDeleteRightIntegersDistance4() {
        Integer[] original = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        Integer[] expected = new Integer[]{0, 2, 3, 4, 5, 1, 6, 7, 8, 9};
        ArrayUtils.deleteAndInsert(original, 1, 5);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    public void testInsertAndDeleteRightStrings() {
        String[] original = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"};
        String[] expected = new String[]{"a", "c", "d", "e", "f", "g", "h", "i", "j", "k", "b", "l"};
        ArrayUtils.deleteAndInsert(original, 1, 10);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    public void testInsertAndDeleteRightInts() {
        int[] original = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] expected = new int[]{0, 2, 3, 4, 5, 6, 7, 8, 9, 1};
        ArrayUtils.deleteAndInsert(original, 1, 9);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    public void testInsertAndDeleteRightLongs() {
        long[] original = new long[]{0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L};
        long[] expected = new long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 0L};
        ArrayUtils.deleteAndInsert(original, 0, 9);
        Assertions.assertArrayEquals(original, expected);
    }

    // Same as above but for left insertion
    @Test
    public void testInsertAndDeleteLeftIntegersDistance1() {
        Integer[] original = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        Integer[] expected = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 9, 8};
        ArrayUtils.deleteAndInsert(original, 9, 8);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    public void testInsertAndDeleteLeftIntegersDistance4() {
        Integer[] original = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        Integer[] expected = new Integer[]{0, 1, 2, 3, 8, 4, 5, 6, 7, 9};
        ArrayUtils.deleteAndInsert(original, 8, 4);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    public void testInsertAndDeleteLeftStrings() {
        String[] original = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"};
        String[] expected = new String[]{"a", "k", "b", "c", "d", "e", "f", "g", "h", "i", "j", "l"};
        ArrayUtils.deleteAndInsert(original, 10, 1);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    public void testInsertAndDeleteLeftInts() {
        int[] original = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] expected = new int[]{0, 9, 1, 2, 3, 4, 5, 6, 7, 8};
        ArrayUtils.deleteAndInsert(original, 9, 1);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    public void testInsertAndDeleteLeftLongs() {
        long[] original = new long[]{0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L};
        long[] expected = new long[]{9L, 0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L};
        ArrayUtils.deleteAndInsert(original, 9, 0);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    public void testUndo() {
        long[] original = new long[]{0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L};
        long[] copy = Arrays.copyOf(original, original.length);
        ArrayUtils.deleteAndInsert(original, 3, 8);
        ArrayUtils.deleteAndInsert(original, 8, 3);
        Assertions.assertArrayEquals(original, copy);
    }

    @Test
    public void testIntInsertArray() {
        int[] orig = {0, 1, 2, 3, 4};
        ArrayUtils.insert(orig, 0, 9);
        Assertions.assertArrayEquals(orig, new int[]{9, 0, 1, 2, 3});

        ArrayUtils.insert(orig, 4, 99);
        Assertions.assertArrayEquals(orig, new int[]{9, 0, 1, 2, 99});
    }

    @Test
    public void testLongInsertArray() {
        long[] orig = {0, 1, 2, 3, 4};
        ArrayUtils.insert(orig, 0, 9);
        Assertions.assertArrayEquals(orig, new long[]{9, 0, 1, 2, 3});

        ArrayUtils.insert(orig, 4, 99);
        Assertions.assertArrayEquals(orig, new long[]{9, 0, 1, 2, 99});
    }

    @Test
    public void testDoubleInsertArray() {
        double[] orig = {0d, 1d, 2d, 3d, 4d};
        ArrayUtils.insert(orig, 0, 9);
        Assertions.assertArrayEquals(orig, new double[]{9d, 0d, 1d, 2d, 3d});

        ArrayUtils.insert(orig, 4, 99);
        Assertions.assertArrayEquals(orig, new double[]{9d, 0d, 1d, 2d, 99d});
    }

    @Test
    public void testTInsertArray() {
        Integer[] orig = {0, 1, 2, 3, 4};
        ArrayUtils.insert(orig, 0, 9);
        Assertions.assertArrayEquals(orig, new Integer[]{9, 0, 1, 2, 3});

        ArrayUtils.insert(orig, 4, 99);
        Assertions.assertArrayEquals(orig, new Integer[]{9, 0, 1, 2, 99});
    }

    @Test
    public void testIntDeleteArray() {
        int[] orig = {0, 1, 2, 3, 4};
        ArrayUtils.remove(orig, 0);
        Assertions.assertArrayEquals(orig, new int[]{1, 2, 3, 4, 4});

        ArrayUtils.remove(orig, 4);
        Assertions.assertArrayEquals(orig, new int[]{1, 2, 3, 4, 4});

        ArrayUtils.remove(orig, 1);
        Assertions.assertArrayEquals(orig, new int[]{1, 3, 4, 4, 4});
    }

    @Test
    public void testLongDeleteArray() {
        long[] orig = {0, 1, 2, 3, 4};
        ArrayUtils.remove(orig, 0);
        Assertions.assertArrayEquals(orig, new long[]{1, 2, 3, 4, 4});

        ArrayUtils.remove(orig, 4);
        Assertions.assertArrayEquals(orig, new long[]{1, 2, 3, 4, 4});

        ArrayUtils.remove(orig, 1);
        Assertions.assertArrayEquals(orig, new long[]{1, 3, 4, 4, 4});
    }

    @Test
    public void testDoubleDeleteArray() {
        double[] orig = {0, 1, 2, 3, 4};
        ArrayUtils.remove(orig, 0);
        Assertions.assertArrayEquals(orig, new double[]{1, 2, 3, 4, 4});

        ArrayUtils.remove(orig, 4);
        Assertions.assertArrayEquals(orig, new double[]{1, 2, 3, 4, 4});

        ArrayUtils.remove(orig, 1);
        Assertions.assertArrayEquals(orig, new double[]{1, 3, 4, 4, 4});
    }
    @Test
    public void testTDeleteArray() {
        Integer[] orig = {0, 1, 2, 3, 4};
        ArrayUtils.remove(orig, 0);
        Assertions.assertArrayEquals(orig, new Integer[]{1, 2, 3, 4, 4});

        ArrayUtils.remove(orig, 4);
        Assertions.assertArrayEquals(orig, new Integer[]{1, 2, 3, 4, 4});

        ArrayUtils.remove(orig, 1);
        Assertions.assertArrayEquals(orig, new Integer[]{1, 3, 4, 4, 4});
    }

    @Test
    public void testFlattenInt() {
        int[][] original = {
                {0,1,2},
                {3},
                {4},
                {},
                {5},
                {6,7,8,9},
                {},
                {10}
        };
        int[] expected = {0,1,2,3,4,5,6,7,8,9,10};
        int[] flatten = ArrayUtils.flatten(original);
        Assertions.assertArrayEquals(flatten, expected);
    }

    @Test
    public void testFlattenLong() {
        long[][] original = {
                {0,1,2},
                {3},
                {4},
                {},
                {5},
                {6,7,8,9},
                {},
                {10}
        };
        long[] expected = {0,1,2,3,4,5,6,7,8,9,10};
        long[] flatten = ArrayUtils.flatten(original);
        Assertions.assertArrayEquals(flatten, expected);
    }

    @Test
    public void testFlattenDouble() {
        double[][] original = {
                {0,1,2},
                {3},
                {4},
                {},
                {5},
                {6,7,8,9},
                {},
                {10}
        };
        double[] expected = {0,1,2,3,4,5,6,7,8,9,10};
        double[] flatten = ArrayUtils.flatten(original);
        Assertions.assertArrayEquals(flatten, expected);
    }
}
