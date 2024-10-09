package es.urjc.etsii.grafo.algorithms;

import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.Comparator;

/**
 * Solving mode, can be either maximize or minimize
 */
public enum FMode {
    /**
     * Objective function should be maximized
     */
    MAXIMIZE {
        @Override
        public double best(double a, double b) {
            return Math.max(a, b);
        }

        @Override
        public double getBadValue() {
            return Long.MIN_VALUE;
        }

        @Override
        public boolean isBetter(double a, double b) {
            return DoubleComparator.isGreater(a, b);
        }

        @Override
        public boolean isBetterOrEqual(double a, double b) {
            return DoubleComparator.isGreaterOrEquals(a, b);
        }

        @Override
        public boolean improves(double a) {
            return DoubleComparator.isPositive(a);
        }
    },

    /**
     * Objective function should be minimized
     */
    MINIMIZE {
        @Override
        public double best(double a, double b) {
            return Math.min(a, b);
        }

        @Override
        public double getBadValue() {
            return Long.MAX_VALUE;
        }

        @Override
        public boolean isBetter(double a, double b) {
            return DoubleComparator.isLess(a, b);
        }

        @Override
        public boolean isBetterOrEqual(double a, double b) {
            return DoubleComparator.isLessOrEquals(a, b);
        }

        @Override
        public boolean improves(double a) {
            return DoubleComparator.isNegative(a);
        }
    };

    /**
     * Get the best value between two values
     * @param a value
     * @param b value
     * @return the best value between a and b
     */
    public abstract double best(double a, double b);

    /**
     * Get a really bad value for this mode, so any other value is better than this one.
     * Useful for example for giving an initial value to the "bestValue" variable when looping through solutions
     * @return a really big value if minimizing, a negative number if maximizing.
     */
    public abstract double getBadValue();

    /**
     * Compare two scores
     * @param a score
     * @param b score
     * @return true if the score a is better than score b
     */
    public abstract boolean isBetter(double a, double b);

    /**
     * Compare two scores
     * @param a score
     * @param b score
     * @return true if the score a is better or equal than score b
     */
    public abstract boolean isBetterOrEqual(double a, double b);

    /**
     * Check if score improves
     * @param a score to check
     * @return true if score improves the solution, false otherwise
     */
    public abstract boolean improves(double a);
}
