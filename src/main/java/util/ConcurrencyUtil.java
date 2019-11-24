package util;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConcurrencyUtil {

    /**
     * Awaits termination for the given executor service.
     * Wraps InterruptedException in an unchecked RuntimeException
     *
     * @param executor
     */
    public static void awaitTermination(ExecutorService executor) {
        try {
            executor.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Extract and propagate exceptions in case any thread failed executing
     *
     * @param futures Tasks to check for errors
     */
    public static <T> void checkErrors(ArrayList<Future<T>> futures) {
        for (Future f : futures) {
            try {
                if (f.isCancelled()) {
                    throw new RuntimeException("Task cancelled: " + f);
                }
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public static void printf(String s, Object... o) {
        System.out.println('[' + Thread.currentThread().getName() + "] " + String.format(s, o));
    }

    public static int getRecommendedNThreads(){
        return Runtime.getRuntime().availableProcessors() / 2;
        // Hyperthreading is enabled in most computers,
        // and some VMs crash if there are too many threads.
    }


    public static <T> T await(Future<T> f){
        try {
            return f.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> awaitAll(Collection<Future<T>> futures){
        return awaitAll(futures.stream());
    }
    public static <T> List<T> awaitAll(Stream<Future<T>> futures){
        return futures.map(ConcurrencyUtil::await).collect(Collectors.toList());
    }

//    TODO Not good enough as is, but can be a starting point for future work
//    public static Map<Instance, List<Result>> parallelExecutor(Function<Instance, List<Result>> doWork, File[] instanceFiles, int nThreads) {
//        System.out.println("Starting solver with " + nThreads + " workers");
//        Map<Instance, List<Result>> results = new ConcurrentHashMap<>(instanceFiles.length);
//        var executor = Executors.newFixedThreadPool(nThreads);
//        var futures = new ArrayList<Future<Object>>();
//        for (File file : instanceFiles) {
//            var instance = Instance.loadInstance(file);
//            futures.add(executor.submit(
//                    () -> results.put(instance, doWork.apply(instance))
//            ));
//        }
//
//        System.out.println("Tasks scheduled, awaiting termination");
//        executor.shutdown();
//        checkErrors(futures);
//        awaitTermination(executor);
//        return results;
//    }
//
//    public static Map<Instance, List<Result>> sequentialExecutor(Function<Instance, List<Result>> doWork, File[] instanceFiles) {
//        System.out.println("Starting sequential solver");
//        Map<Instance, List<Result>> results = new HashMap<>(instanceFiles.length);
//        for (File file : instanceFiles) {
//            var instance = Instance.loadInstance(file);
//            results.put(instance, doWork.apply(instance));
//        }
//        return results;
//    }
}
