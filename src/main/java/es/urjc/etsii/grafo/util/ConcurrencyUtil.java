package es.urjc.etsii.grafo.util;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper methods to deal with concurrent tasks
 */
public class ConcurrencyUtil {

    /**
     * Awaits termination for the given executor service.
     * Wraps InterruptedException in an unchecked RuntimeException
     * @param executor
     */
    public static void await(ExecutorService executor) {
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Block until the task is completed.
     * Wraps the annoying checked exception.
     * @param f Future we will wait for
     * @return SimplifiedResult of the task
     * @throws RuntimeException in case any error happened during the execution
     */
    public static <T> T await(Future<T> f){
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Block until the task is completed.
     * Handles the exception with the given handler
     * @param f Future we will wait for
     * @return SimplifiedResult of the task
     * @throws RuntimeException in case any error happened during the execution
     */
    public static <T> Optional<T> await(Future<T> f, Consumer<Exception> exceptionHandler){
        try {
            return Optional.of(f.get());
        } catch (InterruptedException | ExecutionException e) {
            exceptionHandler.accept(e);
            return Optional.empty();
        }
    }

    public static <T> List<T> awaitAll(Collection<Future<T>> futures){
        return awaitAll(futures.stream());
    }
    public static <T> List<T> awaitAll(Stream<Future<T>> futures){
        return futures.map(ConcurrencyUtil::await).collect(Collectors.toList());
    }
}
