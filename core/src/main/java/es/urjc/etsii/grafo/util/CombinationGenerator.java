package es.urjc.etsii.grafo.util;

import java.util.*;

/**
 * Contains methods to generate permutations
 */
public class CombinationGenerator {

    /**
     * Generate all the possible permutations for the given set
     *
     * @param elements collection of elements to permute
     * @param <T> Generic Type
     * @return returns a list with all the possible permutations
     */
    public static <T> Set<Set<T>> generate(Collection<T> elements){
        var result = new HashSet<Set<T>>();
        r(0, result, new HashSet<>(), new ArrayList<>(elements));
        return result;
    }

    private static <T> void r(int index, Set<Set<T>> total, Set<T> used, ArrayList<T> unused){
        if (index == unused.size()) {
            total.add(new HashSet<>(used));
            return;
        }
        r(index + 1, total, used, unused);
        T element = unused.get(index);
        used.add(element);
        r(index + 1, total, used, unused);
        used.remove(element);
    }
}
