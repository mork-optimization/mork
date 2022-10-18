package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.util.random.RandomManager;

import java.util.stream.Stream;

/**
 * Util methods to manipulate collections and arrays that are not part of the standard java API
 */
@SuppressWarnings("DuplicatedCode") // Due to primitive types some methods must be duplicated
public class ArrayUtil {

    private ArrayUtil(){}

    /**
     * Reverse an array
     *
     * @param arr array to reverse
     */
    public static void reverse(int[] arr) {
        reverseFragment(arr, 0, arr.length);
    }

    /**
     * Reverse a fragment inside an array from start to end (inclusive)
     *
     * @param arr   Array to reverse
     * @param start start index, inclusive
     * @param end   end index, inclusive
     */
    public static void reverseFragment(int[] arr, int start, int end) {
        assert start >= 0 && start <= arr.length : String.format("Start index (%s) must be in range [0, %s)", start, arr.length);
        assert end >= 0 && end <= arr.length : String.format("End index (%s) must be in range [0, %s)", start, arr.length);
        assert start <= end : String.format("Start index (%s) must be <= end (%s)", start, end);
        for (int i = start, j = end; i < j; i++, j--) {
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }

    /**
     * Reverse a fragment inside an array from start to end (inclusive)
     *
     * @param arr   Array to reverse
     * @param start start index, inclusive
     * @param end   end index, inclusive
     */
    public static void reverseFragment(Object[] arr, int start, int end) {
        assert start >= 0 && start <= arr.length : String.format("Start index (%s) must be in range [0, %s)", start, arr.length);
        assert end >= 0 && end <= arr.length : String.format("End index (%s) must be in range [0, %s)", start, arr.length);
        assert start <= end : String.format("Start index (%s) must be <= end (%s)", start, end);
        for (int i = start, j = end; i < j; i++, j--) {
            Object temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
    }

    /**
     * Shuffle an array IN PLACE using Fisher–Yates shuffle
     *
     * @param array Array to shuffle IN PLACE
     */
    public static void shuffle(int[] array) {
        var rnd = RandomManager.getRandom();
        for (int i = array.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            int a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
    }

    /**
     * Swaps the two specified elements in the specified array.
     *
     * @param arr array
     * @param i   origin destination index
     * @param j   destination index
     */
    public static void swap(Object[] arr, int i, int j) {
        Object tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }


    /**
     * Swaps the two specified elements in the specified array.
     *
     * @param arr array
     * @param i   origin destination index
     * @param j   destination index
     */
    public static void swap(int[] arr, int i, int j) {
        int tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    /**
     * Swaps the two specified elements in the specified array.
     *
     * @param arr array
     * @param i   origin destination index
     * @param j   destination index
     */
    public static void swap(double[] arr, int i, int j) {
        double tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    /**
     * Swaps the two specified elements in the specified array.
     *
     * @param arr array
     * @param i   origin destination index
     * @param j   destination index
     */
    public static void swap(long[] arr, int i, int j) {
        long tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    /**
     * Copy and shuffle an array without modifying the original array.
     * Uses Fisher–Yates shuffle
     *
     * @param array Array to shuffle
     * @return shuffled array. Original array is not modified
     */
    public static int[] copyAndshuffle(int[] array) {
        int[] copy = array.clone();
        shuffle(copy);
        return copy;
    }

    /**
     * Shuffle an array IN PLACE using Fisher–Yates shuffle
     *
     * @param array Array to shuffle IN PLACE
     */
    public static void shuffle(Object[] array) {
        var rnd = RandomManager.getRandom();
        for (int i = array.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            swap(array, index, i);
        }
    }

    /**
     * Deletes an item from and array and inserts it in the specified position.
     * Example: deleteAndInsert([a,b,c,d,e,f], 0, 1) = [b,a,c,d,e,f]
     * Example: deleteAndInsert([a,b,c,d,e,f], 1, 4) = [a,c,d,e,b,f]
     * Example: deleteAndInsert([a,b,c,d,e,f], 5, 3) = [a,b,c,f,d,e]
     *
     * @param array       Array to modify
     * @param origin      index of element to be removed
     * @param destination index where element will be inserted
     * @param <T>         Array type
     * @return Modified Array
     */
    public static <T> T[] deleteAndInsert(T[] array, int origin, int destination) {
        if (origin == destination) return array;
        T element = array[origin];
        int length;
        if (origin < destination) {
            length = destination - origin;
            System.arraycopy(array, origin + 1, array, origin, length);
        } else { // destination > origin
            length = origin - destination;
            System.arraycopy(array, destination, array, destination + 1, length);
        }
        array[destination] = element;
        return array;
    }

    /**
     * Deletes an item from and array and inserts it in the specified position.
     * Example: deleteAndInsert([a,b,c,d,e,f], 0, 1) = [b,a,c,d,e,f]
     * Example: deleteAndInsert([a,b,c,d,e,f], 1, 4) = [a,c,d,e,b,f]
     *
     * @param array       Array to modify
     * @param origin      index of element to be removed
     * @param destination index where element will be inserted
     * @return Modified Array
     */
    public static int[] deleteAndInsert(int[] array, int origin, int destination) {
        if (origin == destination) return array;
        int element = array[origin];
        int length;
        if (origin < destination) {
            length = destination - origin;
            System.arraycopy(array, origin + 1, array, origin, length);
        } else { // destination > origin
            length = origin - destination;
            System.arraycopy(array, destination, array, destination + 1, length);
        }
        array[destination] = element;
        return array;
    }

    /**
     * Deletes an item from and array and inserts it in the specified position,
     * moving all elements in between one to the left
     * Example: deleteAndInsert([a,b,c,d,e,f], 0, 1) = [b,a,c,d,e,f]
     * Example: deleteAndInsert([a,b,c,d,e,f], 1, 4) = [a,c,d,e,b,f]
     *
     * @param array       Array to modify
     * @param origin      index of element to be removed
     * @param destination index where element will be inserted
     * @return Modified Array
     */
    public static long[] deleteAndInsert(long[] array, int origin, int destination) {
        if (origin == destination) return array;
        long element = array[origin];
        int length;
        if (origin < destination) {
            length = destination - origin;
            System.arraycopy(array, origin + 1, array, origin, length);
        } else { // destination > origin
            length = origin - destination;
            System.arraycopy(array, destination, array, destination + 1, length);
        }
        array[destination] = element;
        return array;
    }

    /**
     * Insert element in given position. Elements to the right are shifted one position to the right.
     * Rightmost element is dropped.
     *
     * @param arr   Array to modify
     * @param index Position in which insert the element
     * @param value Element to insert
     */
    public static void insert(int[] arr, int index, int value) {
        System.arraycopy(arr, index, arr, index + 1, arr.length - index - 1);
        arr[index] = value;
    }

    /**
     * Insert element in given position. Elements to the right are shifted one position to the right.
     * Rightmost element is dropped.
     *
     * @param arr   Array to modify
     * @param index Position in which insert the element
     * @param value Element to insert
     */
    public static void insert(long[] arr, int index, long value) {
        System.arraycopy(arr, index, arr, index + 1, arr.length - index - 1);
        arr[index] = value;
    }

    /**
     * Insert element in given position. Elements to the right are shifted one position to the right.
     * Rightmost element is dropped.
     *
     * @param arr   Array to modify
     * @param index Position in which insert the element
     * @param value Element to insert
     */
    public static void insert(double[] arr, int index, double value) {
        System.arraycopy(arr, index, arr, index + 1, arr.length - index - 1);
        arr[index] = value;
    }

    /**
     * Insert element in given position. Elements to the right are shifted one position to the right.
     * Rightmost element is dropped.
     *
     * @param arr   Array to modify
     * @param index Position in which insert the element
     * @param value Element to insert
     * @param <T>   type
     */
    public static <T> void insert(T[] arr, int index, T value) {
        System.arraycopy(arr, index, arr, index + 1, arr.length - index - 1);
        arr[index] = value;
    }

    /**
     * Remove element at given index and shift elements to the left. Rightmost element is duplicated.
     * Example: remove([9,10,11,12], 1) → [9,11,12,12]
     *
     * @param arr   array to modify
     * @param index index of element to delete
     * @return removed element
     */
    public static int remove(int[] arr, int index) {
        int value = arr[index];
        System.arraycopy(arr, index + 1, arr, index, arr.length - index - 1);
        return value;
    }

    /**
     * Remove element at given index and shift elements to the left. Rightmost element is duplicated.
     * Example: remove([9,10,11,12], 1) → [9,11,12,12]
     *
     * @param arr   array to modify
     * @param index index of element to delete
     * @return removed element
     */
    public static long remove(long[] arr, int index) {
        long value = arr[index];
        System.arraycopy(arr, index + 1, arr, index, arr.length - index - 1);
        return value;
    }

    /**
     * Remove element at given index and shift elements to the left. Rightmost element is duplicated.
     * Example: remove([9,10,11,12], 1) → [9,11,12,12]
     *
     * @param arr   array to modify
     * @param index index of element to delete
     * @return removed element
     */
    public static double remove(double[] arr, int index) {
        double value = arr[index];
        System.arraycopy(arr, index + 1, arr, index, arr.length - index - 1);
        return value;
    }

    /**
     * Remove element at given index and shift elements to the left. Rightmost element is duplicated.
     * Example: remove([9,10,11,12], 1) → [9,11,12,12]
     *
     * @param arr   array to modify
     * @param index index of element to delete
     * @param <T>   type
     * @return removed element
     */
    public static <T> T remove(T[] arr, int index) {
        T value = arr[index];
        System.arraycopy(arr, index + 1, arr, index, arr.length - index - 1);
        return value;
    }

    /**
     * Flatten matrix to array
     *
     * @param data array data
     * @return flattened array
     */
    public static int[] flatten(int[][] data) {
        int size = 0;
        for (var row : data) {
            size += row.length;
        }
        int left = 0;
        var result = new int[size];
        for (var row : data) {
            System.arraycopy(row, 0, result, left, row.length);
            left += row.length;
        }
        return result;
    }

    /**
     * Flatten matrix to array
     *
     * @param data array data
     * @return flattened array
     */
    public static double[] flatten(double[][] data) {
        int size = 0;
        for (var row : data) {
            size += row.length;
        }
        int left = 0;
        var result = new double[size];
        for (var row : data) {
            System.arraycopy(row, 0, result, left, row.length);
            left += row.length;
        }
        return result;
    }

    /**
     * Flatten matrix to array
     *
     * @param data array data
     * @return flattened array
     */
    public static long[] flatten(long[][] data) {
        int size = 0;
        for (var row : data) {
            size += row.length;
        }
        int left = 0;
        var result = new long[size];
        for (var row : data) {
            System.arraycopy(row, 0, result, left, row.length);
            left += row.length;
        }
        return result;
    }

    /**
     * Count how many elements are not null in given array
     *
     * @param data array
     * @return number of non null elements
     */
    public static int countNonNull(Object[] data) {
        return data.length - countNull(data);
    }

    /**
     * Count how many elements are null in given array
     *
     * @param data array
     * @return number of null elements
     */
    public static int countNull(Object[] data) {
        int count = 0;
        for (var d : data) {
            if (d == null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Sum all elements in array
     *
     * @param data numbers to sum
     * @return sum of all numbers
     * @throws java.lang.ArithmeticException if there is an overflow
     */
    public static int sum(int[] data) {
        int sum = 0;
        for (int i : data) {
            sum = Math.addExact(sum, i);
        }
        return sum;
    }

    /**
     * Sum all elements in array
     *
     * @param data numbers to sum
     * @return sum of all numbers
     */
    public static double sum(double[] data) {
        int sum = 0;
        for (double i : data) {
            sum += i;
        }
        return sum;
    }

    /**
     * Sum all elements in array
     *
     * @param data numbers to sum
     * @return sum of all numbers
     * @throws java.lang.ArithmeticException if there is an overflow
     */
    public static long sum(long[] data) {
        long sum = 0;
        for (long i : data) {
            sum = Math.addExact(sum, i);
        }
        return sum;
    }

    /**
     * Find the biggest value in the given array
     * @param values array of double numbers. Both positive and negative infinity are valid values.
     * @return biggest value in array
     * @throws IllegalArgumentException if the array contains a NaN
     */
    public static double max(double[] values) {
        if(values.length == 0){
            throw new IllegalArgumentException("Empty array");
        }
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < values.length; i++) {
            double d = values[i];
            if (Double.isNaN(d)) {
                throw new IllegalArgumentException("NaN at index " + i);
            }
            if (d > max) {
                max = d;
            }
        }
        return max;
    }

    /**
     * Find the biggest value in the given array
     * @param values array of integer numbers.
     * @return biggest value in array
     */
    public static int max(int[] values) {
        if(values.length == 0){
            throw new IllegalArgumentException("Empty array");
        }
        int max = Integer.MIN_VALUE;
        for (int d : values) {
            if (d > max) {
                max = d;
            }
        }
        return max;
    }

    /**
     * Find the biggest value in the given array
     * @param values array of long numbers.
     * @return biggest value in array
     */
    public static long max(long[] values) {
        if(values.length == 0){
            throw new IllegalArgumentException("Empty array");
        }
        long max = Long.MIN_VALUE;
        for (long d : values) {
            if (d > max) {
                max = d;
            }
        }
        return max;
    }

    /**
     * Find the smallest value in the given array
     * @param values array of double numbers. Both positive and negative infinity are valid values.
     * @return smallest value in array
     * @throws IllegalArgumentException if the array contains a NaN
     */
    public static double min(double[] values) {
        if(values.length == 0){
            throw new IllegalArgumentException("Empty array");
        }
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < values.length; i++) {
            double d = values[i];
            if (Double.isNaN(d)) {
                throw new IllegalArgumentException("NaN at index " + i);
            }
            if (d < min) {
                min = d;
            }
        }
        return min;
    }

    /**
     * Find the smallest value in the given array
     * @param values array of integer numbers.
     * @return smallest value in array
     */
    public static int min(int[] values) {
        if(values.length == 0){
            throw new IllegalArgumentException("Empty array");
        }
        int min = Integer.MAX_VALUE;
        for (int d : values) {
            if (d < min) {
                min = d;
            }
        }
        return min;
    }

    /**
     * Find the smallest value in the given array
     * @param values array of long numbers.
     * @return smallest value in array
     */
    public static long min(long[] values) {
        if(values.length == 0){
            throw new IllegalArgumentException("Empty array");
        }
        long min = Long.MAX_VALUE;
        for (long d : values) {
            if (d < min) {
                min = d;
            }
        }
        return min;
    }

    /**
     * Find the position of the minimum value in the array.
     * If multiple positions have the same minimum value, returns the lower index.
     * @param values array of values
     * @return position of the minimum value in the array
     */
    public static int minIndex(int[] values){
        if(values.length == 0){
            throw new IllegalArgumentException("Empty array");
        }
        int min = Integer.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < values.length; i++) {
            int v = values[i];
            if (v < min) {
                index = i;
                min = v;
            }
        }
        assert index != -1;
        return index;
    }

    /**
     * Find the position of the minimum value in the array.
     * If multiple positions have the same minimum value, returns the lower index.
     * @param values array of values
     * @return position of the minimum value in the array
     */
    public static int minIndex(long[] values){
        if(values.length == 0){
            throw new IllegalArgumentException("Empty array");
        }
        long min = Long.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < values.length; i++) {
            long v = values[i];
            if (v < min) {
                index = i;
                min = v;
            }
        }
        assert index != -1;
        return index;
    }

    /**
     * Find the position of the minimum value in the array.
     * If multiple positions have the same minimum value, returns the lower index.
     * @param values array of values
     * @return position of the minimum value in the array
     */
    public static int minIndex(double[] values){
        if(values.length == 0){
            throw new IllegalArgumentException("Empty array");
        }
        double min = Double.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < values.length; i++) {
            double v = values[i];
            if (Double.isNaN(v)) {
                throw new IllegalArgumentException("NaN at index " + i);
            }
            if (v < min) {
                index = i;
                min = v;
            }
        }
        assert index != -1;
        return index;
    }

    /**
     * Merge arrays
     * @param arrs arrays to merge
     * @return array with all elements in same order
     * @param <T> element type
     */
    public static <T> Object[] merge(T[]... arrs)
    {
        return Stream.of(arrs).flatMap(Stream::of).toArray();
    }


}
