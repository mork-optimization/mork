package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.List;
import java.util.RandomAccess;

public class ValidationUtil {
    public static <S extends Solution<I>, I extends Instance> void validSolution(S solution){
        assert DoubleComparator.equals(solution.getScore(), solution.recalculateScore()) :
                String.format("Score mismatch, getScore() %s, recalculateScore() %s. Review your incremental score calculation.", solution.getScore(), solution.recalculateScore());
    }

    public static void fastAccessList(List<?> list){
        assert list instanceof RandomAccess : "List should have O(1) access time";
    }

    public static <S extends Solution<I>, I extends Instance> void positiveTTB(S solution){
        if(solution.getLastModifiedTime() < 0){
            throw new AssertionError(String.format("Last modified time cannot be negative, current value: %s. Remember to call Solution::updateLastModifiedTime if you have modified the solution without using a Move!", solution.getLastModifiedTime()));
        }
    }
}
