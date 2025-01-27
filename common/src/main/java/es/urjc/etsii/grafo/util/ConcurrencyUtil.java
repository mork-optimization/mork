package es.urjc.etsii.grafo.util;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
     *
     * @param executor Executor service
     */
    public static void await(ExecutorService executor) {
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    /**
     * Block until the task is completed.
     * Wraps the annoying checked exception.
     *
     * @param f Future we will wait for
     * @param <T> Future type
     * @return SimplifiedResult of the task
     * @throws java.lang.RuntimeException in case any error happened during the execution
     */
    public static <T> T await(Future<T> f){
        try {
            return f.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Block until the task is completed.
     * Handles the exception with the given handler
     *
     * @param f Future we will wait for
     * @param <T> Optional type
     * @param exceptionHandler pass the exception to handler instead of promoting to RuntimeException
     * @return SimplifiedResult of the task
     * @throws java.lang.RuntimeException in case any error happened during the execution
     */
    public static <T> Optional<T> await(Future<T> f, Consumer<Exception> exceptionHandler){
        try {
            return Optional.of(f.get());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            exceptionHandler.accept(e);
            return Optional.empty();
        } catch (ExecutionException e) {
            exceptionHandler.accept(e);
            return Optional.empty();
        }
    }

    /**
     * Await a collection of futures
     *
     * @param futures collection of futures
     * @param <T> Futures type
     * @return Objects inside futures
     */
    public static <T> List<T> awaitAll(Collection<Future<T>> futures){
        return awaitAll(futures.stream());
    }

    /**
     * Await a stream of futures
     *
     * @param futures stream of futures
     * @param <T> Futures type
     * @return Objects inside futures
     */
    public static <T> List<T> awaitAll(Stream<Future<T>> futures){
        return futures.map(ConcurrencyUtil::await).collect(Collectors.toList());
    }

    /**
     * Sleep without having to deal with InterruptedException
     * @param time time to sleep
     * @param unit unit of time
     */
    public static void sleep(long time, TimeUnit unit){
        try {
            unit.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
