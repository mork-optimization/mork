package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.List;
import java.util.Map;
import java.util.RandomAccess;

/**
 * Validation Util methods.
 * Implement different assertions to check solution validity
 */
public class ValidationUtil {

    /**
     * Check that the given list implements the RandomAccess interface
     *
     * @param list to check
     */
    public static boolean assertFastAccess(List<?> list){
        if(!(list instanceof RandomAccess)){
            throw new AssertionError("List should have O(1) access time");
        }
        return true;
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
            throw new AssertionError(String.format("Last modified time cannot be negative, current value: %s. Remember to call Solution::notifyUpdate if you have modified the solution without using a Move!", solution.getLastModifiedTime()));
        }
    }

    public static <M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> boolean scoreUpdate(S solution, Map<String, Double> oldValues, M move){
        var deltaValues = Context.evalDeltas(move);
        var newValues = Context.evalSolution(solution);
        for(var entry : oldValues.entrySet()){
            var objName = entry.getKey();
            double oldValue = entry.getValue();
            double expectedDelta = deltaValues.get(objName);
            double newValue = newValues.get(objName);
            if(!DoubleComparator.equals(expectedDelta, newValue - oldValue)){
                throw new AssertionError(String.format("Score update validation failed: Δ != new - old --> %s != %s - %s, current move: %s, solution state with move applied: %s. Last applied moves: %s", expectedDelta, newValue, oldValue, move, solution, solution.lastExecutesMovesAsString()));
            }
        }
        return true;
    }
}
