package es.urjc.etsii.grafo.util;

/**
 * Helper methods to perform comparisons between doubles (or floats)
 * DANGER: DoubleComparator VIOLATES COMPARISON CONTRACT, DO NOT USE IN SORTS

 */
public class DoubleComparator {

    public static final double DEFAULT_EPSILON = 0.000_001;

    /**
     * Test two doubles for equality, uses default error margin
     * @param d1 first double to test
     * @param d2 second double to test
     * @return True, if the difference between them is less than 0.001%, false otherwise
     */
    public static boolean equals(double d1, double d2) {
        return equals(d1, d2, DEFAULT_EPSILON);
    }

    /**
     * Test two doubles for equality
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

    public static int comparator(double d1, double d2, double eps){
        if(equals(d1, d2, eps))
            return 0;
        return Double.compare(d1, d2);
    }

    public static int comparator(double d1, double d2){
        return comparator(d1, d2, DEFAULT_EPSILON);
    }

    public static boolean isNegative(double d1){
        return comparator(d1, 0d, DEFAULT_EPSILON) < 0;
    }

    public static boolean isNegativeOrZero(double d1){
        return comparator(d1, 0d, DEFAULT_EPSILON) <= 0;
    }

    public static boolean isZero(double d1){
        return comparator(d1, 0d, DEFAULT_EPSILON) == 0;
    }

    public static boolean isPositive(double d1){
        return comparator(d1, 0d, DEFAULT_EPSILON) > 0;
    }

    public static boolean isPositiveOrZero(double d1){
        return comparator(d1, 0d, DEFAULT_EPSILON) >= 0;
    }

    public static boolean isGreaterThan(double d1, double d2){
        return isPositive(d1 - d2);
    }

    public static boolean isGreaterOrEqualsThan(double d1, double d2){
        return isPositiveOrZero(d1 - d2);
    }

    public static boolean isLessThan(double d1, double d2){
        return isNegative(d1 - d2);
    }

    public static boolean isLessOrEquals(double d1, double d2){
        return isNegativeOrZero(d1 - d2);
    }

}
