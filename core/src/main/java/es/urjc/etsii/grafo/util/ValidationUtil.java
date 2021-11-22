package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.List;
import java.util.RandomAccess;

/**
 * <p>ValidationUtil class.</p>
 *
 */
public class ValidationUtil {
    /**
     * <p>validSolution.</p>
     *
     * @param solution a S object.
     * @param <S> a S object.
     * @param <I> a I object.
     */
    public static <S extends Solution<S,I>, I extends Instance> void validSolution(S solution){
        assert DoubleComparator.equals(solution.getScore(), solution.recalculateScore()) :
                String.format("Score mismatch, getScore() %s, recalculateScore() %s. Review your incremental score calculation.", solution.getScore(), solution.recalculateScore());
    }

    /**
     * <p>fastAccessList.</p>
     *
     * @param list a {@link java.util.List} object.
     */
    public static void fastAccessList(List<?> list){
        assert list instanceof RandomAccess : "List should have O(1) access time";
    }

    /**
     * <p>positiveTTB.</p>
     *
     * @param solution a S object.
     * @param <S> a S object.
     * @param <I> a I object.
     */
    public static <S extends Solution<S,I>, I extends Instance> void positiveTTB(S solution){
        if(solution.getLastModifiedTime() < 0){
            throw new AssertionError(String.format("Last modified time cannot be negative, current value: %s. Remember to call Solution::updateLastModifiedTime if you have modified the solution without using a Move!", solution.getLastModifiedTime()));
        }
    }
}
