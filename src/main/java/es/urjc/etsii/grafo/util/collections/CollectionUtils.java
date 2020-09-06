package es.urjc.etsii.grafo.util.collections;

import es.urjc.etsii.grafo.util.RandomManager;

import java.util.*;
import java.util.stream.Stream;

/**
 * Util methods to manipulate collections and arrays that are not part of the standard java API
 */
public class CollectionUtils {
    /**
     * Reverse a fragment in a list, from start to end (inclusive)
     * @param list  list to reverse
     * @param start start index, inclusive
     * @param end   end index, inclusive
     */
    public static <T> void reverseFragment(List<T> list, int start, int end) {
        assert list instanceof RandomAccess: "Reversing a fragment of a list where access is not O(1) is extremely slow. Think of a better way to do it ;)";
        assert start >= 0 && start <= list.size(): String.format("Start index (%s) must be in range [0, %s)", start, list.size());
        assert end >= 0 && end <= list.size(): String.format("End index (%s) must be in range [0, %s)", start, list.size());
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
        reverseFragment(list, 0, list.size());
    }

    /**
     * Reverse an array
     * @param arr array to reverse
     */
    public static void reverse(int[] arr) {
        reverseFragment(arr, 0, arr.length);
    }

    /**
     * Reverse a fragment inside an array from start to end (inclusive)
     * @param arr Array to reverse
     * @param start start index, inclusive
     * @param end   end index, inclusive
     */
    public static void reverseFragment(int[] arr, int start, int end) {
        assert start >= 0 && start <= arr.length: String.format("Start index (%s) must be in range [0, %s)", start, arr.length);
        assert end >= 0 && end <= arr.length: String.format("End index (%s) must be in range [0, %s)", start, arr.length);
        assert start <= end: String.format("Start index (%s) must be <= end (%s)", start, end);
        for (int i = start, j = end; i < j; i++, j--) {
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }

    /**
     * Shuffle an array IN PLACE using Fisher–Yates shuffle
     * @param array Array to shuffle IN PLACE
     */
    public static int[] shuffle(int[] array){
        var rnd = RandomManager.getRandom();
        for (int i = array.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
        return array;
    }

    /**
     * Merges several streams into one
     * @param streams Streams to merge
     * @return A single stream containing all the given streams
     */
    @SafeVarargs
    public static <T> Stream<T> merge(Stream<T>... streams){
        return Arrays.stream(streams).flatMap(s -> s);
    }

    public static <T> T pickRandom(Set<T> set){
        int index = RandomManager.nextInt(0, set.size());
        int i = 0;
        for(T t : set) {
            if (i++ == index)
                return t;
        }

        throw new IllegalStateException("Never going to execute, but compiler does not think so, lets see");
    }

    public static <T> T pickRandom(ArrayList<T> list){
        var random = RandomManager.getRandom();
        return list.get(random.nextInt(list.size()));
    }

}
