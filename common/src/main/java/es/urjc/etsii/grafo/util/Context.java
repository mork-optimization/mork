package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.config.BlockConfig;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultManager;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.mo.pareto.ParetoSet;
import es.urjc.etsii.grafo.mo.pareto.ParetoSimpleList;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.util.random.RandomType;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.stream.Stream;

public class Context {
    private static final Logger log = Logger.getLogger(Context.class.getName());

    private static ContextLocal context = new ContextLocal();

    private static class ContextLocal<S extends Solution<S,I>, I extends Instance> extends InheritableThreadLocal<ContextData<S, I>> {
        public ContextLocal() {
            set(new ContextData<>());
        }

        @Override
        protected ContextData<S,I> childValue(ContextData<S,I> parentValue) {
            var context = new ContextData<S,I>();
            // reuse executor from parent, submitted tasks will be shared by them
            context.executor = parentValue.executor;
            if(parentValue.random != null){
                context.random = ((RandomGenerator.JumpableGenerator) parentValue.random).copyAndJump();
            }
            context.objectives = parentValue.objectives;
            context.mainObjective = parentValue.mainObjective;
            context.solverConfig = parentValue.solverConfig;
            context.validator = parentValue.validator;
            context.validationEnabled = parentValue.validationEnabled;
            context.multiObjective = parentValue.multiObjective;
            context.referenceResultManager = parentValue.referenceResultManager;
            // context.timeEvents; // do not copy! thread responsible for managing its own events

            context.paretoSet = parentValue.paretoSet;
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

    private static <S extends Solution<S,I>, I extends Instance> ContextData<S,I> get(){
        return (ContextData<S, I>) context.get();
    }

    public static <S extends Solution<S,I>, I extends Instance> boolean validate(S solution){
        ContextData<S,I> ctx = get();
        if(!ctx.validationEnabled){
            // frequently called from asserts, need to return a true value if a exception should not be thrown
            return true;
        }
        SolutionValidator<S,I> userValidator = ctx.validator;
        if(userValidator != null){
            var result = userValidator.validate(solution);
            result.throwIfFail();
        }
        ValidationUtil.positiveTTB(solution);
        if(ctx.referenceResultManager != null){
            ValidationUtil.validateWithRefValues(solution, getObjectives(), ctx.referenceResultManager);
        }
        return true;
    }


    public static RandomGenerator getRandom(){
        return get().random;
    }

    public static boolean isExecutionQueueAvailable(){
        var executor = get().executor;
        return executor != null && !executor.isShutdown();
    }


    public static <T> Future<T> submit(Callable<T> task){
        var executor = get().executor;
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
        var executor = get().executor;
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

        ContextData<S,I> contextData = get();
        if(contextData.multiObjective){
            throw new IllegalStateException("Cannot get main objective in multi-objective mode. Probable fix: manually specify objective to optimize in algorithm component");
        }
        return (Objective<M,S,I>) contextData.mainObjective;
    }

    public static <S extends Solution<S,I>, I extends Instance> Map<String, Objective<?,S,I>> getObjectives(){
        ContextData<S,I> ctx = get();
        return ctx.objectives;
    }

    public static Map<String, Objective<?,?,?>> getObjectivesW(){
        ContextData<?,?> ctx = get();
        return (Map<String, Objective<?,?,?>>) (Object) ctx.objectives;
    }

    public static <S extends Solution<S,I>, I extends Instance> Map<String, Double> evalSolution(S solution){
        ContextData<S,I> ctx = get();
        var objectives = ctx.objectives;
        Map<String, Double> data = HashMap.newHashMap(objectives.size());
        for(var e: objectives.entrySet()){
            var objective = e.getValue();
            double v = objective.evalSol(solution);
            data.put(e.getKey(), v);
        }
        return data;
    }

    public static <M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> Map<String, Double> evalDeltas(M move){
        ContextData<S,I> ctx = get();
        var objectives = ctx.objectives;
        Map<String, Double> data = HashMap.newHashMap(objectives.size());
        for(var e: objectives.entrySet()){
            Objective<M,S,I> objective = (Objective<M,S,I>) e.getValue();
            double v = objective.evalMove(move);
            data.put(e.getKey(), v);
        }
        return data;
    }

    public static void addTimeEvent(boolean enter, long when, String clazz, String methodName){
        get().timeEvents.add(new TimeStatsEvent(enter, when, clazz, methodName));
    }

    /**
     * Dumb class to hold the context data
     */
    private static class ContextData<S extends Solution<S, I>, I extends Instance> {
        // random manager
        public RandomGenerator random;

        // threadpool
        public ExecutorService executor;

        public Map<String, Objective<?, S, I>> objectives;
        public Objective<?, S, I> mainObjective;
        public SolverConfig solverConfig;
        public BlockConfig blockConfig;
        public List<TimeStatsEvent> timeEvents = new ArrayList<>();
        public SolutionValidator<S,I> validator;
        public boolean validationEnabled = true;
        public boolean multiObjective;
        public ReferenceResultManager referenceResultManager;
        public ParetoSet<S,I> paretoSet;
    }

    public static class Configurator {

        public static void setRandom(RandomGenerator.JumpableGenerator jumpableGenerator){
            var ctx = get();
            if(ctx == null){
                throw new IllegalStateException("Context not yet initialized");
            }
            ctx.random = jumpableGenerator;
        }

        public static void setSolverConfig(SolverConfig config){
            get().solverConfig = config;
        }

        public static SolverConfig getSolverConfig(){
            return get().solverConfig;
        }

        public static void setBlockConfig(BlockConfig config){
            get().blockConfig = config;
        }

        public static BlockConfig getBlockConfig(){
            return get().blockConfig;
        }

        public static <S extends Solution<S,I>, I extends Instance> void setValidator(SolutionValidator<S,I> validator){
            ContextData<S,I> ctx = get();
            ctx.validator = validator;
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
            setObjectives(false, new Objective[]{objective});
        }

        public static <S extends Solution<S,I>, I extends Instance> void setObjectives(boolean multiObjective, Objective<?, S, I>[] objectives) {
            if(Objects.requireNonNull(objectives).length == 0){
                throw new IllegalArgumentException("Objectives array cannot be empty");
            }
            ContextData<S,I> ctx = get();
            var map = new LinkedHashMap<String, Objective<?,S,I>>();
            for(var obj: objectives){
                map.put(obj.getName(), obj);
            }
            ctx.multiObjective = multiObjective;
            ctx.objectives = Collections.unmodifiableMap(map);
            ctx.mainObjective = objectives[0];

            if(multiObjective){
                // TODO make configurable, and change by default to NDTree when behaviour is verified
                ctx.paretoSet = new ParetoSimpleList<>(objectives.length);
            }
        }

        public static List<TimeStatsEvent> getAndResetTimeEvents(){
            var data = get();
            var timeEvents = data.timeEvents;
            data.timeEvents = new ArrayList<>();
            return timeEvents;

        }

        public static void setRefResultManager(ReferenceResultManager referenceResultManager) {
            var ctx = get();
            ctx.referenceResultManager = referenceResultManager;
        }

        public static ReferenceResultManager getRefResultManager() {
            return get().referenceResultManager;
        }

        public static <S extends Solution<S,I>, I extends Instance> Iterable<S> getTrackedSolutions(){
            ContextData<S,I> ctx = get();
            return ctx.paretoSet.getTrackedSolutions();
        }

        /**
         * Enables validations for the current thread. Enabled by default.
         */
        public static void enableValidation(){
            get().validationEnabled = true;
        }

        /**
         * Disables validations for the current thread. By default all validations are run.
         */
        public static void disableValidation(){
            get().validationEnabled = false;
        }

        public static boolean isValidationEnabled(){
            return get().validationEnabled;
        }
    }


    public static class Pareto {
        public static int MAX_ELITE_SOLS_PER_OBJ = 0;
        public static int MAX_TRACKED_SOLS = 0;

        public static <S extends Solution<S, I>, I extends Instance> int size() {
            ContextData<S,I> ctx = get();
            return ctx.paretoSet.size();
        }

        public static Stream<double[]> stream(){
            return get().paretoSet.stream();
        }

        public static void reset() {
            get().paretoSet.clear();
        }

        public static void resetElites(){
            get().paretoSet.resetElites();
        }

        public static <S extends Solution<S, I>, I extends Instance> Map<String, TreeSet<S>> getElites() {
            ContextData<S, I> ctx = get();
            return ctx.paretoSet.getElites();
        }

        public static <S extends Solution<S, I>, I extends Instance> boolean add(S newSol) {
            ContextData<S, I> ctx = get();
            return ctx.paretoSet.add(newSol);
        }

        public static long getLastModifiedTime() {
            return get().paretoSet.getLastModifiedTime();
        }
    }
}
