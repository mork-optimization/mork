package es.urjc.etsii.grafo.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static es.urjc.etsii.grafo.algorithms.FMode.MAXIMIZE;
import static es.urjc.etsii.grafo.algorithms.FMode.MINIMIZE;
import static org.junit.jupiter.api.Assertions.*;

class DoubleComparatorTest {

    @AfterEach
    void resetDoubleComparatorPrecision(){
        DoubleComparator.setPrecision(DoubleComparator.DEFAULT_EPSILON);
    }

    @Test
    void testIsNegative(){
        assertTrue(DoubleComparator.isNegative(-0.1));
        // This should not be regarded as negative because of the default precision
        assertFalse(DoubleComparator.isNegative(-0.000000000000001));
        assertFalse(DoubleComparator.isNegative(0.000000000000001));
        assertFalse(DoubleComparator.isNegative(0.1));
        assertFalse(DoubleComparator.isNegative(Double.MAX_VALUE));
        DoubleComparator.setPrecision(0.0000000001);
        assertTrue(DoubleComparator.isNegative(-0.0000001));
        assertFalse(DoubleComparator.isNegative(0.0000001));
    }

    @Test
    void testIsNegativeOrZero(){
        assertTrue(DoubleComparator.isNegativeOrZero(-0.1));
        assertTrue(DoubleComparator.isNegativeOrZero(-0.000000000000001));
        // This should be true due to the default precision
        assertTrue(DoubleComparator.isNegativeOrZero(0.000000000000001));
        assertFalse(DoubleComparator.isNegativeOrZero(0.1));
        assertFalse(DoubleComparator.isNegativeOrZero(Double.MAX_VALUE));
        DoubleComparator.setPrecision(0.0000000001);
        assertTrue(DoubleComparator.isNegativeOrZero(-0.0000001));
        assertFalse(DoubleComparator.isNegativeOrZero(0.0000001));
    }

    @Test
    void testIsPositive(){
        assertFalse(DoubleComparator.isPositive(-0.1));
        assertFalse(DoubleComparator.isPositive(-0.0000001));
        // This should be false due to the default precision
        assertFalse(DoubleComparator.isPositive(0.0000001));
        assertTrue(DoubleComparator.isPositive(0.1));
        assertTrue(DoubleComparator.isPositive(Double.MAX_VALUE));
        DoubleComparator.setPrecision(0.0000000001);
        assertFalse(DoubleComparator.isPositive(-0.0000001));
        assertTrue(DoubleComparator.isPositive(0.0000001));
    }

    @Test
    void testIsPositiveOrZero(){
        assertFalse(DoubleComparator.isPositiveOrZero(-0.1));
        assertTrue(DoubleComparator.isPositiveOrZero(-0.0000001));
        // This should be true due to the default precision
        assertTrue(DoubleComparator.isPositiveOrZero(0.0000001));
        assertTrue(DoubleComparator.isPositiveOrZero(0.1));
        assertTrue(DoubleComparator.isPositiveOrZero(Double.MAX_VALUE));
        DoubleComparator.setPrecision(0.0000000001);
        assertFalse(DoubleComparator.isPositiveOrZero(-0.0000001));
        assertTrue(DoubleComparator.isPositiveOrZero(0.0000001));
    }

    @Test
    void testIsGreater(){
        assertTrue(DoubleComparator.isGreater(0.3, 0.2));
        assertTrue(DoubleComparator.isGreater(3, 2));
        assertFalse(DoubleComparator.isGreater(Double.MAX_VALUE, Double.MAX_VALUE));
        assertFalse(DoubleComparator.isGreater(0.00000001, 0.0));
        assertFalse(DoubleComparator.isGreater(-0.00000001, 0.0));
        assertTrue(DoubleComparator.isGreater(-3.3, -4.4));
        assertFalse(DoubleComparator.isGreater(-4.4, -3.3));
        DoubleComparator.setPrecision(0.00000001);
        assertTrue(DoubleComparator.isGreater(0.00000001, 0.0));
        assertFalse(DoubleComparator.isGreater(-0.00000001, 0.0));
    }

    @Test
    void testGreaterOrEquals(){
        assertTrue(DoubleComparator.isGreaterOrEquals(0.3, 0.2));
        assertTrue(DoubleComparator.isGreaterOrEquals(3, 2));
        assertTrue(DoubleComparator.isGreaterOrEquals(Double.MAX_VALUE, Double.MAX_VALUE));
        assertTrue(DoubleComparator.isGreaterOrEquals(0.00000001, 0.0));
        assertTrue(DoubleComparator.isGreaterOrEquals(-0.00000001, 0.0));
        assertTrue(DoubleComparator.isGreaterOrEquals(-3.3, -4.4));
        assertFalse(DoubleComparator.isGreaterOrEquals(-4.4, -3.3));
        DoubleComparator.setPrecision(0.00000001);
        assertTrue(DoubleComparator.isGreaterOrEquals(0.00000001, 0.0));
        assertFalse(DoubleComparator.isGreaterOrEquals(-0.00000001, 0.0));
    }

    @Test
    void testIsLess(){
        assertFalse(DoubleComparator.isLess(0.3, 0.2));
        assertFalse(DoubleComparator.isLess(3, 2));
        assertFalse(DoubleComparator.isLess(Double.MAX_VALUE, Double.MAX_VALUE));
        assertFalse(DoubleComparator.isLess(0.00000001, 0.0));
        assertFalse(DoubleComparator.isLess(-0.00000001, 0.0));
        assertFalse(DoubleComparator.isLess(-3.3, -4.4));
        assertTrue(DoubleComparator.isLess(-4.4, -3.3));
        DoubleComparator.setPrecision(0.00000001);
        assertFalse(DoubleComparator.isLess(0.00000001, 0.0));
        assertTrue(DoubleComparator.isLess(-0.00000001, 0.0));
    }

    @Test
    void testIsLessOrEquals(){
        assertFalse(DoubleComparator.isLessOrEquals(0.3, 0.2));
        assertFalse(DoubleComparator.isLessOrEquals(3, 2));
        assertTrue(DoubleComparator.isLessOrEquals(Double.MAX_VALUE, Double.MAX_VALUE));
        assertTrue(DoubleComparator.isLessOrEquals(0.00000001, 0.0));
        assertTrue(DoubleComparator.isLessOrEquals(-0.00000001, 0.0));
        assertFalse(DoubleComparator.isLessOrEquals(-3.3, -4.4));
        assertTrue(DoubleComparator.isLessOrEquals(-4.4, -3.3));
        DoubleComparator.setPrecision(0.00000001);
        assertFalse(DoubleComparator.isLessOrEquals(0.00000001, 0.0));
        assertTrue(DoubleComparator.isLessOrEquals(-0.00000001, 0.0));
    }

    @Test
    void testImprovesFunction(){
        assertFalse(MAXIMIZE.improves(0));
        assertFalse(MAXIMIZE.improves(-0.1));
        assertTrue(MAXIMIZE.improves(0.1));

        assertFalse(MINIMIZE.improves(0));
        assertTrue(MINIMIZE.improves(-0.1));
        assertFalse(MINIMIZE.improves(0.1));
    }

    @Test
    void testIsBetterFunction(){
        assertFalse(MAXIMIZE.isBetter(3.2, 3.2));
        assertFalse(MAXIMIZE.isBetter(3.1, 3.2));
        assertTrue(MAXIMIZE.isBetter(3.2, 3.1));

        assertFalse(MINIMIZE.isBetter(3.2, 3.2));
        assertTrue(MINIMIZE.isBetter(3.1, 3.2));
        assertFalse(MINIMIZE.isBetter(3.2, 3.1));
    }

    @Test
    void testIsBetterOrEqualsFunction(){
        assertTrue(MAXIMIZE.isBetterOrEqual(3.2, 3.2));
        assertFalse(MAXIMIZE.isBetterOrEqual(3.1, 3.2));
        assertTrue(MAXIMIZE.isBetterOrEqual(3.2, 3.1));

        assertTrue(MINIMIZE.isBetterOrEqual(3.2, 3.2));
        assertTrue(MINIMIZE.isBetterOrEqual(3.1, 3.2));
        assertFalse(MINIMIZE.isBetterOrEqual(3.2, 3.1));
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

}