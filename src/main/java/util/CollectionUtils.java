package util;

import java.util.Arrays;
import java.util.List;
import java.util.RandomAccess;
import java.util.stream.Stream;

public class CollectionUtils {
    /**
     * Reverse a fragment in a list
     *
     * @param list  list to reverse
     * @param start start index, inclusive
     * @param end   end index, inclusive
     */
    public static <T> void reverseFragment(List<T> list, int start, int end) {
        if (!(list instanceof RandomAccess)) {
            throw new IllegalArgumentException("Reversing a fragment of a list where access is not O(1) is veeery slooow. Dont do it please.");
        }
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
        reverseFragment(list, 0, list.size());
    }

    /**
     * Reverse an array
     * @param arr list to reverse
     */
    public static void reverse(int[] arr) {
        reverseFragment(arr, 0, arr.length);
    }

    public static void reverseFragment(int[] arr, int start, int end) {
        for (int i = start, j = end; i < j; i++, j--) {
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }

    @SafeVarargs
    public static <T> Stream<T> merge(Stream<T>... streams){
        return Arrays.stream(streams).flatMap(s -> s);
    }
}
