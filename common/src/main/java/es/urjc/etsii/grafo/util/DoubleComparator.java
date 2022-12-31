package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.algorithms.FMode;

import java.util.function.BiPredicate;
import java.util.function.DoublePredicate;

/**
 * Helper methods to perform comparisons between doubles (or floats)
 * DANGER: DoubleComparator VIOLATES COMPARISON CONTRACT, DO NOT USE IN SORTS
 */
public class DoubleComparator {

    /**
     * Util class should never be instantiated
     */
    private DoubleComparator() {}

    /**
     * Default epsilon value. If the difference between two numbers is less than epsilon, the numbers are considered equal.
     */
    public static final double DEFAULT_EPSILON = 0.000_001;
    private static double epsilon = DEFAULT_EPSILON;

    /**
     * BE CAREFUL WITH THIS METHOD! AFFECTS ALL THREADS
     * Change default comparator precision. Defaults to DEFAULT_EPSILON
     *
     * @param epsilon differences less than epsilon will not be counted as true differences.
     *                See <a href="https://en.wikipedia.org/wiki/Double-precision_floating-point_format">the Wikipedia page</a> for an intro on IEE754
     */
    public static void setPrecision(double epsilon){
        DoubleComparator.epsilon = epsilon;
    }

    /**
     * Test two doubles for equality, uses default error margin
     *
     * @param d1 first double to test
     * @param d2 second double to test
     * @return True, if the difference between them is less than 0.001%, false otherwise
     */
    public static boolean equals(double d1, double d2) {
        return equals(d1, d2, epsilon);
    }

    /**
     * Test two doubles for equality
     *
     * @param d1 first double to test
     * @param d2 second double to test
     * @param eps Error margin
     * @return True, if the difference between them is less than the error margin, false otherwise
     */
    public static boolean equals(double d1, double d2, double eps) {
        if(Double.isFinite(d1) && Double.isFinite(d2)){
            return Math.abs(d1 - d2) - eps < 0;
        } else {
            return Double.compare(d1, d2) == 0;
        }
    }

    /**
     * Compares the two specified {@code double} values. The sign
     * of the integer value returned is the same as that of the
     * integer that would be returned by the call:
     * <pre>
     *    new Double(d1).compareTo(new Double(d2))
     * </pre>
     *
     * @param   d1        the first {@code double} to compare
     * @param   d2        the second {@code double} to compare
     * @param eps epsilon to use in comparison
     * @return  the value {@code 0} if {@code d1} is
     *          numerically equal to {@code d2}; a value less than
     *          {@code 0} if {@code d1} is numerically less than
     *          {@code d2}; and a value greater than {@code 0}
     *          if {@code d1} is numerically greater than
     *          {@code d2}.
     */
    public static int comparator(double d1, double d2, double eps){
        if(equals(d1, d2, eps))
            return 0;
        return Double.compare(d1, d2);
    }


    /**
     * Compares the two specified {@code double} values. The sign
     * of the integer value returned is the same as that of the
     * integer that would be returned by the call:
     * <pre>
     *    new Double(d1).compareTo(new Double(d2))
     * </pre>
     *
     * @param   d1        the first {@code double} to compare
     * @param   d2        the second {@code double} to compare
     * @return  the value {@code 0} if {@code d1} is
     *          numerically equal to {@code d2}; a value less than
     *          {@code 0} if {@code d1} is numerically less than
     *          {@code d2}; and a value greater than {@code 0}
     *          if {@code d1} is numerically greater than
     *          {@code d2}.
     */
    public static int comparator(double d1, double d2){
        return comparator(d1, d2, epsilon);
    }

    /**
     * Check if a given double has a negative value.
     *
     * @param d1 double to check
     * @return true if d1 is strictly less than 0, false otherwise.
     */
    public static boolean isNegative(double d1){
        return comparator(d1, 0d, epsilon) < 0;
    }

    /**
     * Check if a given double has a negative value or equals zero.
     *
     * @param d1 double to check
     * @return true if d1 is less than or equal to 0, false otherwise.
     */
    public static boolean isNegativeOrZero(double d1){
        return comparator(d1, 0d, epsilon) <= 0;
    }

    /**
     * Check if a given double equals zero.
     *
     * @param d1 double to check
     * @return true if d1 equals 0, false otherwise.
     */
    public static boolean isZero(double d1){
        return comparator(d1, 0d, epsilon) == 0;
    }

    /**
     * Check if a given double has a positive value.
     *
     * @param d1 double to check
     * @return true if d1 is strictly greater than 0, false otherwise.
     */
    public static boolean isPositive(double d1){
        return comparator(d1, 0d, epsilon) > 0;
    }

    /**
     * Check if a given double has a positive value or equals to zero.
     *
     * @param d1 double to check
     * @return true if d1 is greater than or equals to 0, false otherwise.
     */
    public static boolean isPositiveOrZero(double d1){
        return comparator(d1, 0d, epsilon) >= 0;
    }

    /**
     * Check if the first double is stricly greater than the second
     *
     * @param d1 first double
     * @param d2 second double
     * @return true if d1 &gt; d2, false otherwise
     */
    public static boolean isGreater(double d1, double d2){
        return isPositive(d1 - d2);
    }

    /**
     * Check if the first double is greater than or equals the second
     *
     * @param d1 first double
     * @param d2 second double
     * @return true if d1 &gt;= d2, false otherwise
     */
    public static boolean isGreaterOrEquals(double d1, double d2){
        return isPositiveOrZero(d1 - d2);
    }

    /**
     * Check if the first double is strictly smaller than the second
     *
     * @param d1 first double
     * @param d2 second double
     * @return true if d1 &gt; d2, false otherwise
     */
    public static boolean isLess(double d1, double d2){
        return isNegative(d1 - d2);
    }

    /**
     * Check if the first double is smaller than or equals the second
     *
     * @param d1 first double
     * @param d2 second double
     * @return true if d1 &lt;= d2, false otherwise
     */
    public static boolean isLessOrEquals(double d1, double d2){
        return isNegativeOrZero(d1 - d2);
    }

    /**
     * Returns a function reference that can evaluate for any two given numbers if the first one is strictly better than the second
     * @param mode MAXIMIZE if the values that will be compared should be maximized problem, MINIMIZE if they should be minimized
     * @return BiPredicate
     */
    public static BiPredicate<Double, Double> isBetterFunction(FMode mode){
        return switch (mode){
            case MAXIMIZE -> DoubleComparator::isGreater;
            case MINIMIZE -> DoubleComparator::isLess;
        };
    }

    /**
     * Returns a function reference that can evaluate for any two given numbers if the first one is better than or equal to the second
     * @param mode MAXIMIZE if the values that will be compared should be maximized problem, MINIMIZE if they should be minimized
     * @return BiPredicate
     */
    public static BiPredicate<Double, Double> isBetterOrEqualsFunction(FMode mode){
        return switch (mode){
            case MAXIMIZE -> DoubleComparator::isGreaterOrEquals;
            case MINIMIZE -> DoubleComparator::isLessOrEquals;
        };
    }

    /**
     * Returns a function reference that evaluates if any given number improves the score, for both the maximization and minimization cases
     * @param maximize true if maximizing, false otherwise
     * @return Predicate
     */
    public static DoublePredicate improvesFunction(FMode maximize){
        return switch (maximize){
            case MAXIMIZE -> DoubleComparator::isPositive;
            case MINIMIZE -> DoubleComparator::isNegative;
        };
    }
}
