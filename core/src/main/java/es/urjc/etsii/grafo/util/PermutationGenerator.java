package es.urjc.etsii.grafo.util;

import java.util.*;

/**
 * Contains methods to generate permutations
 */
public class PermutationGenerator {

    /**
     * Generate all the possible permutations for the given set
     *
     * @param elements collection of elements to permute
     * @param <T> Generic Type
     * @return returns a list with all the possible permutations
     */
    public static <T> List<List<T>> generate(Collection<T> elements){
        return r(new ArrayList<>(), new HashSet<>(elements));
    }

    private static <T> List<List<T>> r(List<T> used, Set<T> unused){
        var all = new ArrayList<List<T>>();
        if(unused.size() == 1){
            used.add(unused.iterator().next());
            all.add(new ArrayList<>(used));
            used.remove(used.size() - 1);
            return all;
        }

        for (T t : unused) {
            var copy = new HashSet<>(unused);
            copy.remove(t);
            used.add(t);
            all.addAll(r(used, copy));
            copy.add(t);
            used.remove(used.size() - 1);
        }
        return all;
    }
}
