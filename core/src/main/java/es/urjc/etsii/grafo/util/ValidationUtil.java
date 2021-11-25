package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.List;
import java.util.RandomAccess;

/**
 * Validation Util methods.
 * Implement different assertions to check solution validity
 */
public class ValidationUtil {
    /**
     * Check if the cached score of a solution matches the recalculated score
     *
     * @param solution solution to check
     * @param <S> Solution class
     * @param <I> Instance class
     */
    public static <S extends Solution<S,I>, I extends Instance> void assertValidScore(S solution){
        assert DoubleComparator.equals(solution.getScore(), solution.recalculateScore()) :
                String.format("Score mismatch, getScore() %s, recalculateScore() %s. Review your incremental score calculation.", solution.getScore(), solution.recalculateScore());
    }

    /**
     * Check that the given list implements the RandomAccess interface
     *
     * @param list to check
     */
    public static void assertFastAccess(List<?> list){
        assert list instanceof RandomAccess : "List should have O(1) access time";
    }

    /**
     * Check that the Time To Best is positive (so it has been updated by the user at least once)
     *
     * @param solution Solution to check
     * @param <S> Solution class
     * @param <I> Instance class
     */
    public static <S extends Solution<S,I>, I extends Instance> void positiveTTB(S solution){
        if(solution.getLastModifiedTime() < 0){
            throw new AssertionError(String.format("Last modified time cannot be negative, current value: %s. Remember to call Solution::updateLastModifiedTime if you have modified the solution without using a Move!", solution.getLastModifiedTime()));
        }
    }
}
