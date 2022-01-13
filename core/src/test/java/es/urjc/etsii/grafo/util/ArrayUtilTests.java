package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.testutil.TestInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class ArrayUtilTests {

    @Test
    public void testInsertAndDeleteRightIntegersDistance1() {
        Integer[] original = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        Integer[] expected = new Integer[]{1, 0, 2, 3, 4, 5, 6, 7, 8, 9};
        ArrayUtil.deleteAndInsert(original, 0, 1);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    public void testInsertAndDeleteRightIntegersDistance4() {
        Integer[] original = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        Integer[] expected = new Integer[]{0, 2, 3, 4, 5, 1, 6, 7, 8, 9};
        ArrayUtil.deleteAndInsert(original, 1, 5);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    public void testInsertAndDeleteRightStrings() {
        String[] original = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"};
        String[] expected = new String[]{"a", "c", "d", "e", "f", "g", "h", "i", "j", "k", "b", "l"};
        ArrayUtil.deleteAndInsert(original, 1, 10);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    public void testInsertAndDeleteRightInts() {
        int[] original = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] expected = new int[]{0, 2, 3, 4, 5, 6, 7, 8, 9, 1};
        ArrayUtil.deleteAndInsert(original, 1, 9);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    public void testInsertAndDeleteRightLongs() {
        long[] original = new long[]{0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L};
        long[] expected = new long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 0L};
        ArrayUtil.deleteAndInsert(original, 0, 9);
        Assertions.assertArrayEquals(original, expected);
    }

    // Same as above but for left insertion
    @Test
    public void testInsertAndDeleteLeftIntegersDistance1() {
        Integer[] original = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        Integer[] expected = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 9, 8};
        ArrayUtil.deleteAndInsert(original, 9, 8);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    public void testInsertAndDeleteLeftIntegersDistance4() {
        Integer[] original = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        Integer[] expected = new Integer[]{0, 1, 2, 3, 8, 4, 5, 6, 7, 9};
        ArrayUtil.deleteAndInsert(original, 8, 4);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    public void testInsertAndDeleteLeftStrings() {
        String[] original = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"};
        String[] expected = new String[]{"a", "k", "b", "c", "d", "e", "f", "g", "h", "i", "j", "l"};
        ArrayUtil.deleteAndInsert(original, 10, 1);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    public void testInsertAndDeleteLeftInts() {
        int[] original = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] expected = new int[]{0, 9, 1, 2, 3, 4, 5, 6, 7, 8};
        ArrayUtil.deleteAndInsert(original, 9, 1);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    public void testInsertAndDeleteLeftLongs() {
        long[] original = new long[]{0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L};
        long[] expected = new long[]{9L, 0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L};
        ArrayUtil.deleteAndInsert(original, 9, 0);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    public void testUndo() {
        long[] original = new long[]{0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L};
        long[] copy = Arrays.copyOf(original, original.length);
        ArrayUtil.deleteAndInsert(original, 3, 8);
        ArrayUtil.deleteAndInsert(original, 8, 3);
        Assertions.assertArrayEquals(original, copy);
    }

    @Test
    public void testIntInsertArray() {
        int[] orig = {0, 1, 2, 3, 4};
        ArrayUtil.insert(orig, 0, 9);
        Assertions.assertArrayEquals(orig, new int[]{9, 0, 1, 2, 3});

        ArrayUtil.insert(orig, 4, 99);
        Assertions.assertArrayEquals(orig, new int[]{9, 0, 1, 2, 99});
    }

    @Test
    public void testLongInsertArray() {
        long[] orig = {0, 1, 2, 3, 4};
        ArrayUtil.insert(orig, 0, 9);
        Assertions.assertArrayEquals(orig, new long[]{9, 0, 1, 2, 3});

        ArrayUtil.insert(orig, 4, 99);
        Assertions.assertArrayEquals(orig, new long[]{9, 0, 1, 2, 99});
    }

    @Test
    public void testDoubleInsertArray() {
        double[] orig = {0d, 1d, 2d, 3d, 4d};
        ArrayUtil.insert(orig, 0, 9);
        Assertions.assertArrayEquals(orig, new double[]{9d, 0d, 1d, 2d, 3d});

        ArrayUtil.insert(orig, 4, 99);
        Assertions.assertArrayEquals(orig, new double[]{9d, 0d, 1d, 2d, 99d});
    }

    @Test
    public void testTInsertArray() {
        Integer[] orig = {0, 1, 2, 3, 4};
        ArrayUtil.insert(orig, 0, 9);
        Assertions.assertArrayEquals(orig, new Integer[]{9, 0, 1, 2, 3});

        ArrayUtil.insert(orig, 4, 99);
        Assertions.assertArrayEquals(orig, new Integer[]{9, 0, 1, 2, 99});
    }

    @Test
    public void testIntDeleteArray() {
        int[] orig = {0, 1, 2, 3, 4};
        ArrayUtil.remove(orig, 0);
        Assertions.assertArrayEquals(orig, new int[]{1, 2, 3, 4, 4});

        ArrayUtil.remove(orig, 4);
        Assertions.assertArrayEquals(orig, new int[]{1, 2, 3, 4, 4});

        ArrayUtil.remove(orig, 1);
        Assertions.assertArrayEquals(orig, new int[]{1, 3, 4, 4, 4});
    }

    @Test
    public void testLongDeleteArray() {
        long[] orig = {0, 1, 2, 3, 4};
        ArrayUtil.remove(orig, 0);
        Assertions.assertArrayEquals(orig, new long[]{1, 2, 3, 4, 4});

        ArrayUtil.remove(orig, 4);
        Assertions.assertArrayEquals(orig, new long[]{1, 2, 3, 4, 4});

        ArrayUtil.remove(orig, 1);
        Assertions.assertArrayEquals(orig, new long[]{1, 3, 4, 4, 4});
    }

    @Test
    public void testDoubleDeleteArray() {
        double[] orig = {0, 1, 2, 3, 4};
        ArrayUtil.remove(orig, 0);
        Assertions.assertArrayEquals(orig, new double[]{1, 2, 3, 4, 4});

        ArrayUtil.remove(orig, 4);
        Assertions.assertArrayEquals(orig, new double[]{1, 2, 3, 4, 4});

        ArrayUtil.remove(orig, 1);
        Assertions.assertArrayEquals(orig, new double[]{1, 3, 4, 4, 4});
    }
    @Test
    public void testTDeleteArray() {
        Integer[] orig = {0, 1, 2, 3, 4};
        ArrayUtil.remove(orig, 0);
        Assertions.assertArrayEquals(orig, new Integer[]{1, 2, 3, 4, 4});

        ArrayUtil.remove(orig, 4);
        Assertions.assertArrayEquals(orig, new Integer[]{1, 2, 3, 4, 4});

        ArrayUtil.remove(orig, 1);
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
        int[] flatten = ArrayUtil.flatten(original);
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
        long[] flatten = ArrayUtil.flatten(original);
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
        double[] flatten = ArrayUtil.flatten(original);
        Assertions.assertArrayEquals(flatten, expected);
    }

    @Test
    public void countNulls(){
        Object[] empty = new Integer[]{};
        Assertions.assertEquals(0, ArrayUtil.countNull(empty));
        Assertions.assertEquals(0, ArrayUtil.countNonNull(empty));

        Object[] data = new Integer[]{null, 1, null, 5, null};
        Assertions.assertEquals(3, ArrayUtil.countNull(data));
        Assertions.assertEquals(2, ArrayUtil.countNonNull(data));
    }

    @Test
    public void testSumInts(){
        Assertions.assertThrows(ArithmeticException.class, () -> ArrayUtil.sum(new int[]{Integer.MAX_VALUE, 1}));
        Assertions.assertEquals(5, ArrayUtil.sum(new int[]{2, 3}));
        Assertions.assertEquals(-1, ArrayUtil.sum(new int[]{-2, 1}));
    }

    @Test
    public void testSumLongs(){
        Assertions.assertThrows(ArithmeticException.class, () -> ArrayUtil.sum(new long[]{Long.MAX_VALUE, 1}));
        Assertions.assertEquals(5, ArrayUtil.sum(new long[]{2, 3}));
        Assertions.assertEquals(-1, ArrayUtil.sum(new long[]{-2, 1}));
    }

    @Test
    public void testSumDouble(){
        Assertions.assertEquals(5D, ArrayUtil.sum(new double[]{2, 3}), 0.001);
        Assertions.assertEquals(-1D, ArrayUtil.sum(new double[]{-2, 1}), 0.001);
    }

    @Test
    public void testSwapObjects(){
        TestInstance first = new TestInstance("first");
        TestInstance second = new TestInstance("second");
        TestInstance third = new TestInstance("third");
        TestInstance fourth = new TestInstance("fourth");
        Object[] data = new Object[]{first, second, third, fourth};
        ArrayUtil.swap(data, 0, 2);
        Object[] expected1 = new Object[]{third, second, first, fourth};
        Assertions.assertArrayEquals(data, expected1);
        ArrayUtil.swap(data, 1, 3);
        Object[] expected2 = new Object[]{third, fourth, first, second};
        Assertions.assertArrayEquals(data, expected2);
    }

    @Test
    public void testSwapInts(){
        int[] data = new int[]{0,1,2,3,4,5};
        ArrayUtil.swap(data, 0, 2);
        Assertions.assertArrayEquals(data, new int[]{2,1,0,3,4,5});
        ArrayUtil.swap(data, 4, 5);
        Assertions.assertArrayEquals(data, new int[]{2,1,0,3,5,4});
    }

    @Test
    public void testSwapDoubles(){
        double[] data = new double[]{0,1,2,3,4,5};
        ArrayUtil.swap(data, 0, 2);
        Assertions.assertArrayEquals(data, new double[]{2,1,0,3,4,5});
        ArrayUtil.swap(data, 4, 5);
        Assertions.assertArrayEquals(data, new double[]{2,1,0,3,5,4});
    }

    @Test
    public void testSwapLongs(){
        long[] data = new long[]{0,1,2,3,4,5};
        ArrayUtil.swap(data, 0, 2);
        Assertions.assertArrayEquals(data, new long[]{2,1,0,3,4,5});
        ArrayUtil.swap(data, 4, 5);
        Assertions.assertArrayEquals(data, new long[]{2,1,0,3,5,4});
    }

}
