package es.urjc.etsii.grafo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;
import java.util.Set;

/**
 * Util methods to manipulate collections and arrays that are not part of the standard java API
 */
public class CollectionUtils {
    /**
     * Reverse a fragment in a list, from start to end (inclusive)
     * @param list  list to reverse
     * @param start start index, inclusive
     * @param end   end index, inclusive
     * @param <T> List type
     */
    public static <T> void reverseFragment(List<T> list, int start, int end) {
        assert list instanceof RandomAccess: "Reversing a fragment of a list where access is not O(1) is extremely slow. Think of a better way to do it ;)";
        assert start >= 0 && start <= list.size(): String.format("Start index (%s) must be in range [0, %s)", start, list.size());
        assert end >= 0 && end < list.size(): String.format("End index (%s) must be in range [0, %s)", start, list.size());
        assert start <= end: String.format("Start index (%s) must be <= end (%s)", start, end);
        for (int i = start, j = end; i < j; i++, j--) {
            T temp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, temp);
        }
    }

    /**
     * Reverse a list
     * @param list list to reverse
     */
    public static void reverse(List<Integer> list) {
        reverseFragment(list, 0, list.size() -1);
    }

    /**
     * Picks a random element from the given set. Each element has the same probability of being chosen.
     * @param set Set where random element will be chosen from
     * @param <T> Set type
     * @return Chosen element
     */
    public static <T> T pickRandom(Set<T> set){
        int index = RandomManager.nextInt(0, set.size());
        int i = 0;
        for(T t : set) {
            if (i++ == index)
                return t;
        }

        throw new IllegalStateException("Never going to execute, but compiler does not think so, lets see");
    }

    /**
     * Picks a random element from the given list. Each element has the same probability of being chosen.
     * @param list List where random element will be chosen from
     * @param <T> List type
     * @return Chosen element
     */
    public static <T> T pickRandom(List<T> list){
        var random = RandomManager.getRandom();
        return list.get(random.nextInt(list.size()));
    }
}
