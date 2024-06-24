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

        @Override
        public Comparator<Solution<?,?>> comparator() {
            Comparator<Solution<?,?>> c = Comparator.comparingDouble(Solution::getScore);
            return c.reversed();
        }

        @Override
        public Comparator<Move<?, ?>> comparatorMove() {
            Comparator<Move<?,?>> c = Comparator.comparingDouble(Move::getValue);
            return c.reversed();
        }
    },

    /**
     * Objective function should be minimized
     */
    MINIMIZE {
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

        @Override
        public Comparator<Solution<?,?>> comparator() {
            return Comparator.comparingDouble(Solution::getScore);
        }


        @Override
        public Comparator<Move<?, ?>> comparatorMove() {
            return Comparator.comparingDouble(Move::getValue);
        }
    };

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

    /**
     * Returns a comparator that sorts solutions from best to worst
     * @return a new comparator
     */
    public abstract Comparator<Solution<?,?>> comparator();

    /**
     * Returns a comparator that sorts moves from best to worst
     * @return a new comparator
     */
    public abstract Comparator<Move<?,?>> comparatorMove();
}
