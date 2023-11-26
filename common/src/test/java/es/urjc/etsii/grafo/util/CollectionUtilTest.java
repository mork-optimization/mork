package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.testutil.TestCommonUtils;
import es.urjc.etsii.grafo.util.random.RandomType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

class CollectionUtilTest {

    @BeforeEach
    public void initDefaultRandom(){
        TestCommonUtils.initRandom(RandomType.DEFAULT, 0, 1);
    }

    @Test
    void reverseFragmentTest() {
        var list = Arrays.asList(0, 1, 2, 3, 4, 5);
        CollectionUtil.reverseFragment(list, 0, 3);
        Assertions.assertEquals(list, Arrays.asList(3, 2, 1, 0, 4, 5));
        CollectionUtil.reverseFragment(list, 0, 3);
        Assertions.assertEquals(list, Arrays.asList(0, 1, 2, 3, 4, 5));

        CollectionUtil.reverseFragment(list, 3, list.size() - 1);
        Assertions.assertEquals(list, Arrays.asList(0, 1, 2, 5, 4, 3));

        // index is inclusive, bounds checks
        Assertions.assertThrows(AssertionError.class, () -> CollectionUtil.reverseFragment(list, 0, list.size()));
        Assertions.assertThrows(AssertionError.class, () -> CollectionUtil.reverseFragment(list, 3, 2));
        Assertions.assertThrows(AssertionError.class, () -> CollectionUtil.reverseFragment(list, -1, 2));

        // LinkedList for O(1) access should trigger assertion error
        Assertions.assertThrows(AssertionError.class, () -> CollectionUtil.reverseFragment(new LinkedList<>(list), 0, 5));
    }

    @Test
    void reverseTest() {
        var list = Arrays.asList(0, 1, 2, 3, 4, 5);
        CollectionUtil.reverse(list);
        Assertions.assertEquals(list, Arrays.asList(5, 4, 3, 2, 1, 0));
    }

    @Test
    void pickRandomFromSetTest() {
        var set = new HashSet<>(Arrays.asList(0, 1, 2, 3, 4, 5));
        for (int i = 0; i < 100; i++) {
            int n = CollectionUtil.pickRandom(set);
            Assertions.assertTrue(set.contains(n), String.format("Chosen element %s not in set %s", n, set));
        }
        Assertions.assertThrows(IllegalArgumentException.class, () -> CollectionUtil.pickRandom(new HashSet<>()));
    }

    @Test
    void pickRandomFromListTest() {
        var list = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5));
        for (int i = 0; i < 100; i++) {
            int n = CollectionUtil.pickRandom(list);
            Assertions.assertTrue(list.contains(n), String.format("Chosen element %s not in list %s", n, list));
        }
        Assertions.assertThrows(IllegalArgumentException.class, () -> CollectionUtil.pickRandom(new ArrayList<>()));
    }

    @Test
    void intToArray() {
        var input = Arrays.asList(1, 7, 4, -9, Integer.MAX_VALUE, Integer.MIN_VALUE);
        var expectedOutput = new int[]{1, 7, 4, -9, Integer.MAX_VALUE, Integer.MIN_VALUE};

        var output = CollectionUtil.toIntArray(input);
        Assertions.assertArrayEquals(expectedOutput, output);
    }

    @Test
    void longToArray() {
        var input = Arrays.asList(1L, 7L, 4L, -9L, Long.MAX_VALUE, Long.MIN_VALUE);
        var expectedOutput = new long[]{1, 7L, 4, -9L, Long.MAX_VALUE, Long.MIN_VALUE};

        var output = CollectionUtil.toLongArray(input);
        Assertions.assertArrayEquals(expectedOutput, output);
    }

    @Test
    void doubleToArray() {
        var input = Arrays.asList(1D, 7D, 4D, -9D, Double.MAX_VALUE, Double.MIN_VALUE);
        var expectedOutput = new double[]{1D, 7D, 4D, -9D, Double.MAX_VALUE, Double.MIN_VALUE};

        var output = CollectionUtil.toDoubleArray(input);
        Assertions.assertArrayEquals(expectedOutput, output);
    }

    @Test
    void generateIntegerList() {
        var start = 0;
        var end = 100;

        var expectedOutput = new ArrayList<Integer>();
        for (int i = start; i < end; i++) {
            expectedOutput.add(i);
        }

        var expectedEmptyOutput = new ArrayList<Integer>();

        Assertions.assertEquals(expectedOutput, CollectionUtil.generateIntegerList(start, end));
        Assertions.assertEquals(expectedEmptyOutput, CollectionUtil.generateIntegerList(start, start));
        Assertions.assertEquals(expectedEmptyOutput, CollectionUtil.generateIntegerList(end, start));
        Assertions.assertEquals(expectedOutput, CollectionUtil.generateIntegerList(end));
        Assertions.assertEquals(expectedEmptyOutput, CollectionUtil.generateIntegerList(start));

        start = 50;
        expectedOutput = new ArrayList<>();
        for (int i = start; i < end; i++) {
            expectedOutput.add(i);
        }
        Assertions.assertEquals(expectedOutput, CollectionUtil.generateIntegerList(start, end));
        Assertions.assertEquals(expectedEmptyOutput, CollectionUtil.generateIntegerList(start, start));
        Assertions.assertEquals(expectedEmptyOutput, CollectionUtil.generateIntegerList(end, start));


        start = -25;
        end = -1;
        expectedOutput = new ArrayList<>();
        for (int i = start; i < end; i++) {
            expectedOutput.add(i);
        }
        Assertions.assertEquals(expectedOutput, CollectionUtil.generateIntegerList(start, end));
        Assertions.assertEquals(expectedEmptyOutput, CollectionUtil.generateIntegerList(start, start));
        Assertions.assertEquals(expectedEmptyOutput, CollectionUtil.generateIntegerList(end, start));
    }
}
