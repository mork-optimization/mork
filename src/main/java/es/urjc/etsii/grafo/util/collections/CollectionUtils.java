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
     * Reverse a fragment inside an array from start to end (inclusive)
     * @param arr Array to reverse
     * @param start start index, inclusive
     * @param end   end index, inclusive
     */
    public static void reverseFragment(Object[] arr, int start, int end) {
        assert start >= 0 && start <= arr.length: String.format("Start index (%s) must be in range [0, %s)", start, arr.length);
        assert end >= 0 && end <= arr.length: String.format("End index (%s) must be in range [0, %s)", start, arr.length);
        assert start <= end: String.format("Start index (%s) must be <= end (%s)", start, end);
        for (int i = start, j = end; i < j; i++, j--) {
            Object temp = arr[i];
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
     * Shuffle an array IN PLACE using Fisher–Yates shuffle
     * @param array Array to shuffle IN PLACE
     */
    public static void shuffle(Object[] array){
        var rnd = RandomManager.getRandom();
        for (int i = array.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            Object a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
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

    /**
     * Deletes an item from and array and inserts it in the specified position.
     * Example: deleteAndInsert([a,b,c,d,e,f], 0, 1) = [b,a,c,d,e,f]
     * Example: deleteAndInsert([a,b,c,d,e,f], 1, 4) = [a,c,d,e,b,f]
     * Example: deleteAndInsert([a,b,c,d,e,f], 5, 3) = [a,b,c,f,d,e]
     * @param array Array to modify
     * @param origin index of element to be removed
     * @param destination index where element will be inserted
     * @param <T> Array type
     * @return Modified Array
     */
    public static <T> T[] deleteAndInsert(T[] array, int origin, int destination){
        if(origin == destination) return array;
        T element = array[origin];
        int length;
        if(origin < destination){
            length = destination - origin;
            System.arraycopy(array, origin+1, array, origin,length);
        } else { // destination > origin
            length = origin - destination;
            System.arraycopy(array, destination, array, destination+1,length);
        }
        array[destination] = element;
        return array;
    }

    /**
     * Deletes an item from and array and inserts it in the specified position.
     * Example: deleteAndInsert([a,b,c,d,e,f], 0, 1) = [b,a,c,d,e,f]
     * Example: deleteAndInsert([a,b,c,d,e,f], 1, 4) = [a,c,d,e,b,f]
     * @param array Array to modify
     * @param origin index of element to be removed
     * @param destination index where element will be inserted
     * @return Modified Array
     */
    public static int[] deleteAndInsert(int[] array, int origin, int destination){
        if(origin == destination) return array;
        int element = array[origin];
        int length;
        if(origin < destination){
            length = destination - origin;
            System.arraycopy(array, origin+1, array, origin,length);
        } else { // destination > origin
            length = origin - destination;
            System.arraycopy(array, destination, array, destination+1,length);
        }
        array[destination] = element;
        return array;
    }

    /**
     * Deletes an item from and array and inserts it in the specified position,
     * moving all elements in between one to the left
     * Example: deleteAndInsert([a,b,c,d,e,f], 0, 1) = [b,a,c,d,e,f]
     * Example: deleteAndInsert([a,b,c,d,e,f], 1, 4) = [a,c,d,e,b,f]
     * @param array Array to modify
     * @param origin index of element to be removed
     * @param destination index where element will be inserted
     * @return Modified Array
     */
    public static long[] deleteAndInsert(long[] array, int origin, int destination){
        if(origin == destination) return array;
        long element = array[origin];
        int length;
        if(origin < destination){
            length = destination - origin;
            System.arraycopy(array, origin+1, array, origin,length);
        } else { // destination > origin
            length = origin - destination;
            System.arraycopy(array, destination, array, destination+1,length);
        }
        array[destination] = element;
        return array;
    }

}
