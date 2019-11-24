package util;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
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

    public static void printf(String s, Object... o) {
        System.out.println('[' + Thread.currentThread().getName() + "] " + String.format(s, o));
    }

    private static int getNThreads(){
        // divide by 2 as hyperthreading is enabled in most computers
        // and we do not want to overload it
        return Runtime.getRuntime().availableProcessors() / 2;
    }

    public static ExecutorService buildExecutorService(){
        var parallel = System.getProperty("parallel");
        if(parallel != null && Boolean.parseBoolean(parallel)){
            return Executors.newFixedThreadPool(getNThreads());
        } else {
            return Executors.newSingleThreadExecutor();
        }
    }


    /**
     * Block until the task is completed.
     * Wraps the annoying checked exception.
     * @param f Future we will wait for
     * @return Result of the task
     * @throws RuntimeException in case any error happened during the execution
     */
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
