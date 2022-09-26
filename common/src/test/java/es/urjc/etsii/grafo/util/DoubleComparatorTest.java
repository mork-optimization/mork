package es.urjc.etsii.grafo.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DoubleComparatorTest {

    @Test
    void testImprovesFunction(){
        var f1 = DoubleComparator.improvesFunction(true);
        assertFalse(f1.test(0));
        assertFalse(f1.test(-0.1));
        assertTrue(f1.test(0.1));

        var f2 = DoubleComparator.improvesFunction(false);
        assertFalse(f2.test(0));
        assertTrue(f2.test(-0.1));
        assertFalse(f2.test(0.1));
    }

    @Test
    void testIsBetterFunction(){
        var f1 = DoubleComparator.isBetterFunction(true);
        assertFalse(f1.test(3.2, 3.2));
        assertFalse(f1.test(3.1, 3.2));
        assertTrue(f1.test(3.2, 3.1));

        var f2 = DoubleComparator.isBetterFunction(false);
        assertFalse(f2.test(3.2, 3.2));
        assertTrue(f2.test(3.1, 3.2));
        assertFalse(f2.test(3.2, 3.1));
    }

    @Test
    void testIsBetterOrEqualsFunction(){
        var f1 = DoubleComparator.isBetterOrEqualsFunction(true);
        assertTrue(f1.test(3.2, 3.2));
        assertFalse(f1.test(3.1, 3.2));
        assertTrue(f1.test(3.2, 3.1));

        var f2 = DoubleComparator.isBetterOrEqualsFunction(false);
        assertTrue(f2.test(3.2, 3.2));
        assertTrue(f2.test(3.1, 3.2));
        assertFalse(f2.test(3.2, 3.1));
    }

    @Test
    void withCustomPrecision() {
        assertFalse(DoubleComparator.equals(0.01, -0.01));
        assertFalse(DoubleComparator.isZero(0.05));
        DoubleComparator.setPrecision(0.1);
        assertTrue(DoubleComparator.equals(0.01, -0.01));
        assertTrue(DoubleComparator.isZero(0.05));
        DoubleComparator.setPrecision(DoubleComparator.DEFAULT_EPSILON);
    }

    @Test
    void testEquals(){
        // Easy cases
        assertFalse(DoubleComparator.equals(1, 2));
        assertTrue(DoubleComparator.equals(6, 6));

        // Precision cases
        assertFalse(DoubleComparator.equals(1.00001, 0.99999));
        assertTrue(DoubleComparator.equals(1.00000000001, 0.99999999999));

        // Tricky cases
        assertTrue(DoubleComparator.equals(-0D, +0D)); // Positive and negative zero are NOT equal, but should be for our usage
        assertTrue(DoubleComparator.equals(Double.NaN, Double.NaN)); // NaN != NaN by default, but when comparing should be equal
    }

    @Test
    void testComparator() {
        assertEquals(0, DoubleComparator.comparator(Double.NaN, Double.NaN));
        assertEquals(0, DoubleComparator.comparator(0.99999999999, 1.00000000001));
        assertEquals(-1, DoubleComparator.comparator(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        assertEquals(-1, DoubleComparator.comparator(Double.NEGATIVE_INFINITY, 0));
        assertEquals(1, DoubleComparator.comparator(0, Double.NEGATIVE_INFINITY));
        assertEquals(1, DoubleComparator.comparator(Double.POSITIVE_INFINITY, 0));
    }

    @Test
    void testIsZero() {
        assertTrue(DoubleComparator.isZero(0));
        assertTrue(DoubleComparator.isZero(0.0000000001));
        assertTrue(DoubleComparator.isZero(-0.0000000001));
        assertTrue(DoubleComparator.isZero(-0));

        assertFalse(DoubleComparator.isZero(Double.NaN));
        assertFalse(DoubleComparator.isZero(-0.01));
        assertFalse(DoubleComparator.isZero(+0.01));
        assertFalse(DoubleComparator.isZero(Double.NEGATIVE_INFINITY));
        assertFalse(DoubleComparator.isZero(Double.POSITIVE_INFINITY));
    }

    // TODO easy: complete DoubleComparator test methods: isNegative, isNegativeOrZero, isPositive, isPositiveOrZero, isGreater, isGreaterOrEquals, isLess, isLessOrEquals
}