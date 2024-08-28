package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.testutil.TestCommonUtils;
import es.urjc.etsii.grafo.testutil.TestInstance;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ArrayUtilTest {

    @BeforeAll
    static void setupRnd(){
        TestCommonUtils.initRandom();
    }

    @Test
    void testInsertAndDeleteRightIntegersDistance1() {
        Integer[] original = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        Integer[] expected = new Integer[]{1, 0, 2, 3, 4, 5, 6, 7, 8, 9};
        ArrayUtil.deleteAndInsert(original, 0, 1);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    void testInsertAndDeleteRightIntegersDistance4() {
        Integer[] original = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        Integer[] expected = new Integer[]{0, 2, 3, 4, 5, 1, 6, 7, 8, 9};
        ArrayUtil.deleteAndInsert(original, 1, 5);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    void testInsertAndDeleteRightStrings() {
        String[] original = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"};
        String[] expected = new String[]{"a", "c", "d", "e", "f", "g", "h", "i", "j", "k", "b", "l"};
        ArrayUtil.deleteAndInsert(original, 1, 10);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    void testInsertAndDeleteRightInts() {
        int[] original = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] expected = new int[]{0, 2, 3, 4, 5, 6, 7, 8, 9, 1};
        ArrayUtil.deleteAndInsert(original, 1, 9);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    void testInsertAndDeleteRightLongs() {
        long[] original = new long[]{0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L};
        long[] expected = new long[]{1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 0L};
        ArrayUtil.deleteAndInsert(original, 0, 9);
        Assertions.assertArrayEquals(original, expected);
    }

    // Same as above but for left insertion
    @Test
    void testInsertAndDeleteLeftIntegersDistance1() {
        Integer[] original = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        Integer[] expected = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 9, 8};
        ArrayUtil.deleteAndInsert(original, 9, 8);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    void testInsertAndDeleteLeftIntegersDistance4() {
        Integer[] original = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        Integer[] expected = new Integer[]{0, 1, 2, 3, 8, 4, 5, 6, 7, 9};
        ArrayUtil.deleteAndInsert(original, 8, 4);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    void testInsertAndDeleteLeftStrings() {
        String[] original = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l"};
        String[] expected = new String[]{"a", "k", "b", "c", "d", "e", "f", "g", "h", "i", "j", "l"};
        ArrayUtil.deleteAndInsert(original, 10, 1);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    void testInsertAndDeleteLeftInts() {
        int[] original = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        int[] expected = new int[]{0, 9, 1, 2, 3, 4, 5, 6, 7, 8};
        ArrayUtil.deleteAndInsert(original, 9, 1);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    void testInsertAndDeleteLeftLongs() {
        long[] original = new long[]{0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L};
        long[] expected = new long[]{9L, 0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L};
        ArrayUtil.deleteAndInsert(original, 9, 0);
        Assertions.assertArrayEquals(original, expected);
    }

    @Test
    void testUndo() {
        long[] original = new long[]{0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L};
        long[] copy = Arrays.copyOf(original, original.length);
        ArrayUtil.deleteAndInsert(original, 3, 8);
        ArrayUtil.deleteAndInsert(original, 8, 3);
        Assertions.assertArrayEquals(original, copy);
    }

    @Test
    void testIntInsertArray() {
        int[] orig = {0, 1, 2, 3, 4};
        ArrayUtil.insert(orig, 0, 9);
        Assertions.assertArrayEquals(new int[]{9, 0, 1, 2, 3}, orig);

        ArrayUtil.insert(orig, 4, 99);
        Assertions.assertArrayEquals(new int[]{9, 0, 1, 2, 99}, orig);
    }

    @Test
    void testLongInsertArray() {
        long[] orig = {0, 1, 2, 3, 4};
        ArrayUtil.insert(orig, 0, 9);
        Assertions.assertArrayEquals(new long[]{9, 0, 1, 2, 3}, orig);

        ArrayUtil.insert(orig, 4, 99);
        Assertions.assertArrayEquals(new long[]{9, 0, 1, 2, 99}, orig);
    }

    @Test
    void testDoubleInsertArray() {
        double[] orig = {0d, 1d, 2d, 3d, 4d};
        ArrayUtil.insert(orig, 0, 9);
        Assertions.assertArrayEquals(new double[]{9d, 0d, 1d, 2d, 3d}, orig);

        ArrayUtil.insert(orig, 4, 99);
        Assertions.assertArrayEquals(new double[]{9d, 0d, 1d, 2d, 99d}, orig);
    }

    @Test
    void testTInsertArray() {
        Integer[] orig = {0, 1, 2, 3, 4};
        ArrayUtil.insert(orig, 0, 9);
        Assertions.assertArrayEquals(new Integer[]{9, 0, 1, 2, 3}, orig);

        ArrayUtil.insert(orig, 4, 99);
        Assertions.assertArrayEquals(new Integer[]{9, 0, 1, 2, 99}, orig);
    }

    @Test
    void testIntDeleteArray() {
        int[] orig = {0, 1, 2, 3, 4};
        ArrayUtil.remove(orig, 0);
        Assertions.assertArrayEquals(new int[]{1, 2, 3, 4, 4}, orig);

        ArrayUtil.remove(orig, 4);
        Assertions.assertArrayEquals(new int[]{1, 2, 3, 4, 4}, orig);

        ArrayUtil.remove(orig, 1);
        Assertions.assertArrayEquals(new int[]{1, 3, 4, 4, 4}, orig);
    }

    @Test
    void testLongDeleteArray() {
        long[] orig = {0, 1, 2, 3, 4};
        ArrayUtil.remove(orig, 0);
        Assertions.assertArrayEquals(new long[]{1, 2, 3, 4, 4}, orig);

        ArrayUtil.remove(orig, 4);
        Assertions.assertArrayEquals(new long[]{1, 2, 3, 4, 4}, orig);

        ArrayUtil.remove(orig, 1);
        Assertions.assertArrayEquals(new long[]{1, 3, 4, 4, 4}, orig);
    }

    @Test
    void testDoubleDeleteArray() {
        double[] orig = {0, 1, 2, 3, 4};
        ArrayUtil.remove(orig, 0);
        Assertions.assertArrayEquals(new double[]{1, 2, 3, 4, 4}, orig);

        ArrayUtil.remove(orig, 4);
        Assertions.assertArrayEquals(new double[]{1, 2, 3, 4, 4}, orig);

        ArrayUtil.remove(orig, 1);
        Assertions.assertArrayEquals(new double[]{1, 3, 4, 4, 4}, orig);
    }
    @Test
    void testTDeleteArray() {
        Integer[] orig = {0, 1, 2, 3, 4};
        ArrayUtil.remove(orig, 0);
        Assertions.assertArrayEquals(new Integer[]{1, 2, 3, 4, 4}, orig);

        ArrayUtil.remove(orig, 4);
        Assertions.assertArrayEquals(new Integer[]{1, 2, 3, 4, 4}, orig);

        ArrayUtil.remove(orig, 1);
        Assertions.assertArrayEquals(new Integer[]{1, 3, 4, 4, 4}, orig);
    }

    @Test
    void testFlattenInt() {
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
    void testFlattenLong() {
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
    void testFlattenDouble() {
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
    void testFlattenObject() {
        String[][] original = {
                {"a", "b", "c"},
                {"d"},
                {"e"},
                {},
                {"f"}
        };
        String[] expected = {"a", "b", "c", "d", "e", "f"};
        String[] flatten = ArrayUtil.flatten(original);
        Assertions.assertArrayEquals(expected, flatten);
    }

    @Test
    void countNulls(){
        Object[] empty = new Integer[]{};
        Assertions.assertEquals(0, ArrayUtil.countNull(empty));
        Assertions.assertEquals(0, ArrayUtil.countNonNull(empty));

        Object[] data = new Integer[]{null, 1, null, 5, null};
        Assertions.assertEquals(3, ArrayUtil.countNull(data));
        Assertions.assertEquals(2, ArrayUtil.countNonNull(data));
    }

    @Test
    void testSumInts(){
        assertThrows(ArithmeticException.class, () -> ArrayUtil.sum(new int[]{Integer.MAX_VALUE, 1}));
        Assertions.assertEquals(5, ArrayUtil.sum(new int[]{2, 3}));
        Assertions.assertEquals(-1, ArrayUtil.sum(new int[]{-2, 1}));
    }

    @Test
    void testSumLongs(){
        assertThrows(ArithmeticException.class, () -> ArrayUtil.sum(new long[]{Long.MAX_VALUE, 1}));
        Assertions.assertEquals(5, ArrayUtil.sum(new long[]{2, 3}));
        Assertions.assertEquals(-1, ArrayUtil.sum(new long[]{-2, 1}));
    }

    @Test
    void testSumDouble(){
        Assertions.assertEquals(5D, ArrayUtil.sum(new double[]{2, 3}), 0.001);
        Assertions.assertEquals(-1D, ArrayUtil.sum(new double[]{-2, 1}), 0.001);
        Assertions.assertEquals(0.2, ArrayUtil.sum(new double[]{0.1, 0.1}), 0.001);
    }

    @Test
    void testSwapObjects(){
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
    void testSwapInts(){
        int[] data = new int[]{0,1,2,3,4,5};
        ArrayUtil.swap(data, 0, 2);
        Assertions.assertArrayEquals(new int[]{2, 1, 0, 3, 4, 5}, data);
        ArrayUtil.swap(data, 4, 5);
        Assertions.assertArrayEquals(new int[]{2, 1, 0, 3, 5, 4}, data);
    }

    @Test
    void testSwapDoubles(){
        double[] data = new double[]{0,1,2,3,4,5};
        ArrayUtil.swap(data, 0, 2);
        Assertions.assertArrayEquals(new double[]{2, 1, 0, 3, 4, 5}, data);
        ArrayUtil.swap(data, 4, 5);
        Assertions.assertArrayEquals(new double[]{2, 1, 0, 3, 5, 4}, data);
    }

    @Test
    void testSwapLongs(){
        long[] data = new long[]{0,1,2,3,4,5};
        ArrayUtil.swap(data, 0, 2);
        Assertions.assertArrayEquals(new long[]{2, 1, 0, 3, 4, 5}, data);
        ArrayUtil.swap(data, 4, 5);
        Assertions.assertArrayEquals(new long[]{2, 1, 0, 3, 5, 4}, data);
    }

    @Test
    void testMaxInt(){
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.max(new int[]{}));
        Assertions.assertEquals(0, ArrayUtil.max(new int[]{-1, 0, -4123}));
        Assertions.assertEquals(0, ArrayUtil.max(new int[]{0}));
    }

    @Test
    void testMaxLong(){
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.max(new long[]{}));
        Assertions.assertEquals(0L, ArrayUtil.max(new long[]{-1, 0, -4123}));
        Assertions.assertEquals(0L, ArrayUtil.max(new long[]{0}));
    }

    @Test
    void testMaxDouble(){
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.max(new double[]{}));
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.max(new double[]{1,4,Double.NaN,8}));
        Assertions.assertEquals(0, ArrayUtil.max(new double[]{-1, 0, -4123}));
        Assertions.assertEquals(0, ArrayUtil.max(new double[]{0}));
    }

    @Test
    void testMinInt(){
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.min(new int[]{}));
        Assertions.assertEquals(-4123, ArrayUtil.min(new int[]{-1, 0, -4123}));
        Assertions.assertEquals(0, ArrayUtil.min(new int[]{0}));
    }

    @Test
    void testMinLong(){
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.min(new long[]{}));
        Assertions.assertEquals(-4123L, ArrayUtil.min(new long[]{-1, 0, -4123}));
        Assertions.assertEquals(0L, ArrayUtil.min(new long[]{0}));
    }

    @Test
    void testMinDouble(){
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.min(new double[]{}));
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.min(new double[]{1,4,Double.NaN,8}));
        Assertions.assertEquals(-4123D, ArrayUtil.min(new double[]{-1, 0, -4123}));
        Assertions.assertEquals(0D, ArrayUtil.min(new double[]{0}));
    }

    @Test
    void testMinIndexInt(){
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.minIndex(new int[]{}));
        Assertions.assertEquals(2, ArrayUtil.minIndex(new int[]{-1, 0, -4123}));
        Assertions.assertEquals(2, ArrayUtil.minIndex(new int[]{-1, 0, -4123, -4123}));
        Assertions.assertEquals(3, ArrayUtil.minIndex(new int[]{-1, 0, -4123, -4124}));
        Assertions.assertEquals(0, ArrayUtil.minIndex(new int[]{0}));
    }

    @Test
    void testMinIndexLong(){
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.minIndex(new long[]{}));
        Assertions.assertEquals(2, ArrayUtil.minIndex(new long[]{-1, 0, -4123}));
        Assertions.assertEquals(2, ArrayUtil.minIndex(new long[]{-1, 0, -4123, -4123}));
        Assertions.assertEquals(3, ArrayUtil.minIndex(new long[]{-1, 0, -4123, -4124}));
        Assertions.assertEquals(0, ArrayUtil.minIndex(new long[]{0}));
    }

    @Test
    void testMinIndexDouble(){
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.minIndex(new double[]{}));
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.minIndex(new double[]{1,4,Double.NaN,8}));
        Assertions.assertEquals(2, ArrayUtil.minIndex(new double[]{-1, 0, -4123}));
        Assertions.assertEquals(2, ArrayUtil.minIndex(new double[]{-1, 0, -4123, -4123}));
        Assertions.assertEquals(3, ArrayUtil.minIndex(new double[]{-1, 0, -4123, -4124}));
        Assertions.assertEquals(0, ArrayUtil.minIndex(new double[]{0}));
    }

    @Test
    void mergeTest(){
        Assertions.assertArrayEquals(new String[]{"1", "2", "3", "4"}, ArrayUtil.merge(new String[]{"1", "2"}, new String[]{"3", "4"}));
        Assertions.assertArrayEquals(new String[]{"1", "2", "3", "4"}, ArrayUtil.merge(new String[]{"1"}, new String[]{"2", "3", "4"}));
        Assertions.assertArrayEquals(new String[]{"1", "2", "3", "4"}, ArrayUtil.merge(new String[]{}, new String[]{"1", "2", "3", "4"}));
        Assertions.assertArrayEquals(new String[]{"", ""}, ArrayUtil.merge(new String[]{""}, new String[]{""}));
        Assertions.assertArrayEquals(new String[]{}, ArrayUtil.merge(new String[]{}, new String[]{}));
    }


    @Test
    void reverseInts(){
        int[] data = new int[]{1,2,3,4,5};
        ArrayUtil.reverse(data);
        Assertions.assertArrayEquals(new int[]{5,4,3,2,1}, data);

        data = new int[]{1,2,3,4,5};
        ArrayUtil.reverse(data, 1, 3);
        Assertions.assertArrayEquals(new int[]{1,4,3,2,5}, data);

        int[] finalData = data;
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.reverse(finalData, -1, 3));
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.reverse(finalData, 0, 5));
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.reverse(finalData, 3, 1));

        data = new int[]{};
        ArrayUtil.reverse(data);
        Assertions.assertArrayEquals(new int[]{}, data);

        data = new int[]{-1};
        ArrayUtil.reverse(data);
        Assertions.assertArrayEquals(new int[]{-1}, data);

    }

    @Test
    void reverseObjects(){
        String[] data = new String[]{"1","2","3","4","5"};
        ArrayUtil.reverse(data);
        Assertions.assertArrayEquals(new String[]{"5","4","3","2","1"}, data);

        data = new String[]{"1","2","3","4","5"};
        ArrayUtil.reverse(data, 1, 3);
        Assertions.assertArrayEquals(new String[]{"1","4","3","2","5"}, data);

        String[] finalData = data;
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.reverse(finalData, -1, 3));
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.reverse(finalData, 0, 5));
        assertThrows(IllegalArgumentException.class, () -> ArrayUtil.reverse(finalData, 3, 1));

        data = new String[]{};
        ArrayUtil.reverse(data);
        Assertions.assertArrayEquals(new String[]{}, data);

        data = new String[]{"-1"};
        ArrayUtil.reverse(data);
        Assertions.assertArrayEquals(new String[]{"-1"}, data);
    }

    @Test
    void stats(){
        /*
        From Excel
        5		Std	2.9933259094191533
        8		Mean	5.2
        9		Sum	26
        1		Max	9
        3		Min	1
         */
        var dataInt = new int[]{5, 8, 9, 1, 3};
        var dataDouble = new double[]{5, 8, 9, 1, 3};

        var statsInt = ArrayUtil.stats(dataInt);
        var statsDouble = ArrayUtil.stats(dataDouble);

        Assertions.assertEquals(2.9933259094191533, statsInt.std());
        Assertions.assertEquals(2.9933259094191533, statsDouble.std());

        Assertions.assertEquals(5.2, statsInt.avg());
        Assertions.assertEquals(5.2, statsDouble.avg());

        Assertions.assertEquals(26, statsInt.sum());
        Assertions.assertEquals(26, statsDouble.sum());

        Assertions.assertEquals(9, statsInt.max());
        Assertions.assertEquals(9, statsDouble.max());

        Assertions.assertEquals(1, statsInt.min());
        Assertions.assertEquals(1, statsDouble.min());
    }

    @Test
    void sanityCheckShuffleInt(){
        int[] data = new int[]{0, 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        ArrayUtil.shuffle(data);
        // count numbers that do not change position
        int samePosition = 0;
        for(int i = 0; i < data.length; i++){
            if(data[i] == i){
                samePosition++;
            }
        }
        // At least one has moved
        Assertions.assertTrue(samePosition < data.length);
        // Check there are no new or missing elements
        Arrays.sort(data);
        for(int i = 0; i < data.length; i++){
            Assertions.assertEquals(i, data[i]);
        }
    }

    @Test
    void checkShuffleIntCopyDoesNotModifyOriginal(){
        int[] data = new int[]{0, 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        var result = ArrayUtil.copyAndshuffle(data);
        Assertions.assertNotSame(data, result);
        for(int i = 0; i < data.length; i++){
            Assertions.assertEquals(i, data[i]);
        }
    }

    @Test
    void sanityCheckShuffleObject(){
        Object[] data = new Object[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        ArrayUtil.shuffle(data);
        // count numbers that do not change position
        int samePosition = 0;
        for(int i = 0; i < data.length; i++){
            if((int) data[i] == i){
                samePosition++;
            }
        }
        // At least one has moved
        Assertions.assertTrue(samePosition < data.length);
        // Check there are no new or missing elements
        Arrays.sort(data);
        for(int i = 0; i < data.length; i++){
            Assertions.assertEquals(i, data[i]);
        }
    }

    @Test
    void checkShuffleObjectCopyDoesNotModifyOriginal(){
        Object[] data = new Object[]{0, 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        var result = ArrayUtil.copyAndshuffle(data);
        Assertions.assertNotSame(data, result);
        for(int i = 0; i < data.length; i++){
            Assertions.assertEquals(i, data[i]);
        }
    }
}
