package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.ToDoubleFunction;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Util methods to manipulate collections and arrays that are not part of the standard java API
 */
public class CollectionUtil {

    /**
     * Magic value calculated empirically for performance, taken from the reference Collections implementation
     */
    private static final int SHUFFLE_THRESHOLD = 5;

    /**
     * Reverse a fragment in a list, from start to end (inclusive)
     *
     * @param list  list to reverse
     * @param start start index, inclusive
     * @param end   end index, inclusive
     * @param <T>   List type
     */
    public static <T> void reverseFragment(List<T> list, int start, int end) {
        assert list instanceof RandomAccess : "Reversing a fragment of a list where access is not O(1) is extremely slow. Think of a better way to do it ;)";
        assert start >= 0 && start <= list.size() : String.format("Start index (%s) must be in range [0, %s)", start, list.size());
        assert end >= 0 && end < list.size() : String.format("End index (%s) must be in range [0, %s)", start, list.size());
        assert start <= end : String.format("Start index (%s) must be <= end (%s)", start, end);
        for (int i = start, j = end; i < j; i++, j--) {
            T temp = list.get(i);
            list.set(i, list.get(j));
            list.set(j, temp);
        }
    }

    /**
     * Reverse a list
     *
     * @param list list to reverse
     */
    public static void reverse(List<Integer> list) {
        reverseFragment(list, 0, list.size() - 1);
    }

    /**
     * Picks a random element from the given set. Each element has the same probability of being chosen.
     *
     * @param set Set where random element will be chosen from
     * @param <T> Set type
     * @return Chosen element
     */
    public static <T> T pickRandom(Set<T> set) {
        int index = RandomManager.getRandom().nextInt(0, set.size());
        int i = 0;
        for (T t : set) {
            if (i++ == index) return t;
        }

        throw new IllegalStateException("Never going to execute, but compiler does not think so, lets see");
    }

    /**
     * Picks a random element from the given list. Each element has the same probability of being chosen.
     *
     * @param list List where random element will be chosen from
     * @param <T>  List type
     * @return Chosen element
     */
    public static <T> T pickRandom(List<T> list) {
        var random = RandomManager.getRandom();
        return list.get(random.nextInt(list.size()));
    }

    /**
     * Return a primitive array with all the numbers of the given collection.
     *
     * @param c Input collection
     * @return Primitive array
     */
    public static int[] toIntArray(Collection<Integer> c) {
        // From Guava
        Object[] boxedArray = c.toArray();
        int len = boxedArray.length;
        int[] array = new int[len];
        for (int i = 0; i < len; i++) {
            array[i] = (int) boxedArray[i];
        }
        return array;
    }

    /**
     * Return a primitive array with all the numbers of the given collection.
     *
     * @param c Input collection
     * @return Primitive array
     */
    public static double[] toDoubleArray(Collection<Double> c) {
        // From Guava
        Object[] boxedArray = c.toArray();
        int len = boxedArray.length;
        double[] array = new double[len];
        for (int i = 0; i < len; i++) {
            array[i] = (double) boxedArray[i];
        }
        return array;
    }

    /**
     * Return a primitive array with all the numbers of the given collection.
     *
     * @param c Input collection
     * @return Primitive array
     */
    public static long[] toLongArray(Collection<Long> c) {
        // From Guava
        Object[] boxedArray = c.toArray();
        int len = boxedArray.length;
        long[] array = new long[len];
        for (int i = 0; i < len; i++) {
            array[i] = (long) boxedArray[i];
        }
        return array;
    }


    /**
     * From the official javadocs
     * Swaps the elements at the specified positions in the specified list.
     * (If the specified positions are equal, invoking this method leaves
     * the list unchanged.)
     *
     * @param list The list in which to swap elements.
     * @param i    the index of one element to be swapped.
     * @param j    the index of the other element to be swapped.
     * @throws java.lang.IndexOutOfBoundsException if either {@code i} or {@code j}
     *                                             is out of range (i &lt; 0 || i &gt;= list.size()
     *                                             || j &lt; 0 || j &gt;= list.size()).
     * @since 1.4
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void swap(List<?> list, int i, int j) {
        // instead of using a raw type here, it's possible to capture
        // the wildcard but it will require a call to a supplementary
        // private method
        final List l = list;
        l.set(i, l.set(j, l.get(i)));
    }

    /**
     * Randomly permute the specified list using the specified source of
     * randomness.  All permutations occur with equal likelihood
     * assuming that the source of randomness is fair.
     *
     * This implementation traverses the list backwards, from the last element
     * up to the second, repeatedly swapping a randomly selected element into
     * the "current position".  Elements are randomly selected from the
     * portion of the list that runs from the first element to the current
     * position, inclusive.
     *
     * This method runs in linear time.  If the specified list does not
     * implement the {@link java.util.RandomAccess} interface and is large, this
     * implementation dumps the specified list into an array before shuffling
     * it, and dumps the shuffled array back into the list.  This avoids the
     * quadratic behavior that would result from shuffling a "sequential
     * access" list in place.
     *
     * @param list the list to be shuffled.
     * @throws java.lang.UnsupportedOperationException if the specified list or its
     *                                                 list-iterator does not support the {@code set} operation.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void shuffle(List<?> list) {
        RandomGenerator rnd = RandomManager.getRandom();
        int size = list.size();
        if (size < SHUFFLE_THRESHOLD || list instanceof RandomAccess) {
            for (int i = size; i > 1; i--)
                swap(list, i - 1, rnd.nextInt(i));
        } else {
            Object[] arr = list.toArray();

            // Shuffle array
            for (int i = size; i > 1; i--) {
                ArrayUtil.swap(arr, i - 1, rnd.nextInt(i));
            }

            // Dump array back into list
            // instead of using a raw type here, it's possible to capture
            // the wildcard but it will require a call to a supplementary
            // private method
            ListIterator it = list.listIterator();
            for (Object e : arr) {
                it.next();
                it.set(e);
            }
        }
    }

    /**
     * returns a sequential ordered List from startInclusive (inclusive) to endExclusive (exclusive) by an incremental step of 1.
     *
     * If the start is greater or equal than the end, the returned list will be empty.
     *
     * @param start the (inclusive) initial start value
     * @param end   the exclusive upper bound
     * @return a sequential List for the range of int elements
     */
    public static List<Integer> generateIntegerList(int start, int end) {
        return IntStream.range(start, end).boxed().collect(Collectors.toList());
    }

    /**
     * Generate a list of integers between 0, and a given value.
     *
     * If end number is less than 0, the returned list will be empty.
     *
     * @param end End value
     * @return List of integers
     */
    public static List<Integer> generateIntegerList(int end) {
        return generateIntegerList(0, end);
    }

    /**
     * Return best element from list
     * @param list list of elements
     * @param f function to map an element to a score
     * @param isBetter function to compare two scores and returns which one is better
     * @return best element in the list
     * @param <T> generic type
     */
    public static <T> T getBest(List<T> list, ToDoubleFunction<T> f, BiPredicate<Double, Double> isBetter){
        T best = null;
        double bestScore = Double.NaN;
        for(var move: list){
            if(best == null){
                best = move;
                bestScore = f.applyAsDouble(move);
            } else {
                double currentScore = f.applyAsDouble(move);
                if(isBetter.test(currentScore, bestScore)){
                    best = move;
                    bestScore = currentScore;
                }
            }
        }
        return best;
    }
}
