package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.random.RandomType;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class Context {
    private static final Logger logger = Logger.getLogger(Context.class.getName());

    private static ContextLocal context = new ContextLocal();

    private static class ContextLocal extends InheritableThreadLocal<ContextData> {
        public ContextLocal() {
            set(new ContextData());
        }

        @Override
        protected ContextData childValue(ContextData parentValue) {
            var context = new ContextData();
            // reuse executor from parent, submitted tasks will be shared by them
            context.executor = parentValue.executor;
            if(parentValue.random != null){
                context.random = ((RandomGenerator.JumpableGenerator) parentValue.random).copyAndJump();
            }
            context.objectives = parentValue.objectives;
            context.mainObjective = parentValue.mainObjective;
            return context;
        }
    }

    /**
     * Destroy the solver context associated with the current thread.
     * Does not affect the context of other threads.
     */
    public static void destroy(){
        // todo: clean whatever needs cleaning and delete references
        context.remove();
    }

    /**
     * Recreate the solver context associated with the current thread.
     * Does not affect the context of other threads.
     */
    public static void reset(){
        // todo: clean whatever needs cleaning and delete references
        context.set(new ContextData());
    }


    public static RandomGenerator getRandom(){
        return context.get().random;
    }

    public static boolean isExecutionQueueAvailable(){
        var executor = context.get().executor;
        return executor != null && !executor.isShutdown();
    }


    public static <T> Future<T> submit(Callable<T> task){
        var executor = context.get().executor;
        if(executor != null){
            return executor.submit(task);
        }
        // no executor, run in the current thread
        try {
            return CompletableFuture.completedFuture(task.call());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Future<T> submit(Runnable task, T value){
        var executor = context.get().executor;
        if(executor != null){
            return executor.submit(task, value);
        }
        // no executor, run in the current thread
        try {
            task.run();
            return CompletableFuture.completedFuture(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <M extends Move<S,I>, S extends Solution<S,I>, I extends Instance>  Objective<M,S,I> getMainObjective(){
        return (Objective<M,S,I>) context.get().mainObjective;
    }

    public static Map<String, Objective<?,?,?>> getObjectives(){
        return context.get().objectives;
    }


    /**
     * Dumb class to hold the context data
     */
    private static class ContextData {
        // random manager
        public RandomGenerator random;

        // threadpool
        public ExecutorService executor;

        public Map<String, Objective<?, ?, ?>> objectives;
        public Objective<?, ?, ?> mainObjective;
    }

    public static class Configurator {

        public static void setRandom(RandomGenerator.JumpableGenerator jumpableGenerator){
            var ctx = Context.context;
            if(ctx == null){
                throw new IllegalStateException("Context not yet initialized");
            }
            ctx.get().random = jumpableGenerator;
        }

        /**
         * Initialize or reset random only for the current thread
         * @param config solver config
         * @param iteration current iteration, used to calculate a seed
         */
        public static void resetRandom(SolverConfig config, int iteration){
            resetRandom(config.getRandomType(), config.getSeed() + iteration);
        }

        /**
         * Initialize or reset random only for the current thread
         * @param randomType random type
         * @param seed seed
         */
        public static void resetRandom(RandomType randomType, long seed){
            var rnd = RandomGeneratorFactory.of(randomType.getJavaName()).create(seed);
            if(rnd instanceof RandomGenerator.JumpableGenerator jumpableGenerator){
                Context.Configurator.setRandom(jumpableGenerator);
            } else {
                throw new IllegalArgumentException("RandomGenerator %s is not of type JumpableGenerator".formatted(randomType));
            }
        }

        public static void setObjectives(Objective<?, ?, ?> objective) {
            setObjectives(new Objective[]{objective});
        }

        public static void setObjectives(Objective<?, ?, ?>[] objectives) {
            if(Objects.requireNonNull(objectives).length == 0){
                throw new IllegalArgumentException("Objectives array cannot be empty");
            }
            var localContext = context.get();
            var map = new HashMap<String, Objective<?, ?,?>>();
            for(var obj: objectives){
                map.put(obj.getName(), obj);
            }
            localContext.objectives = Collections.unmodifiableMap(map);
            localContext.mainObjective = objectives[0];
        }
    }
}
