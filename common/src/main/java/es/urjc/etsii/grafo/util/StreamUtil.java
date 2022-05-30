package es.urjc.etsii.grafo.util;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Stream Utils
 */
public class StreamUtil {

    /**
     * Merges several streams into one
     *
     * @param streams Streams to merge
     * @param <T> Stream type. All streams must have same type.
     * @return A single stream containing all the given streams
     */
    @SafeVarargs
    public static <T> Stream<T> merge(Stream<T>... streams){
        return Arrays.stream(streams).flatMap(s -> s);
    }
}
