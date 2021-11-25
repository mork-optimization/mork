//package es.urjc.etsii.grafo.solver.algorithms;
//
//import es.urjc.etsii.grafo.io.Instance;
//import es.urjc.etsii.grafo.io.WorkingOnResult;
//import es.urjc.etsii.grafo.solution.ConstructiveNeighborhood;
//import es.urjc.etsii.grafo.solution.Solution;
//import es.urjc.etsii.grafo.solver.algorithms.config.ExchangerILSConfig;
//import es.urjc.etsii.grafo.solver.create.Constructive;
//import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;
//import es.urjc.etsii.grafo.solver.destructor.Shake;
//import es.urjc.etsii.grafo.solver.improve.Improver;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.function.Supplier;
//
//import static es.urjc.etsii.grafo.util.ConcurrencyUtil.awaitAll;
//
//@Component
//public class ExchangerILS<S extends Solution<S,I>, I extends Instance> implements Algorithm<S,I> {
//
//    private final int nRotateRounds;
//    private final ILSConfig[] configs;
//
//    /**
//     * Create a new MultiStartAlgorithm, @see algorithm
//     */
//    public ExchangerILS(ExchangerILSConfig config) {
//        this.nRotateRounds = config.getnRotateRounds();
//        this.configs = config.getConfigs();
//    }
//
//    public WorkingOnResult execute(I ins, int repetitions) {
//        WorkingOnResult result = new WorkingOnResult(repetitions, this.toString(), ins.getName());
//        for (int i = 0; i < repetitions; i++) {
//            long startTime = System.nanoTime();
//            S s = algorithm(ins);
//            long ellapsedTime = System.nanoTime() - startTime;
//            result.addSolution(s, ellapsedTime);
//        }
//
//        return result;
//    }
//
//    /**
//     * Executes the algorythm for the given instance
//     * @param ins Instance the algorithm will process
//     * @return Best es.urjc.etsii.grafo.solution found
//     */
//    public S algorithm(I ins) {
//
//        int nThreads = Runtime.getRuntime().availableProcessors() / 2;
//
//        var nWorkers = this.configs.length;
//        if(nThreads < nWorkers){
//            System.out.format("[Warning] Available nThreads (%s) is less than the number of configured workers (%s), performance may be reduced\n", nThreads, nWorkers);
//        }
//
//        // Create threads and workers
//        var executor = Executors.newFixedThreadPool(nThreads);
//        var first = new LinkedBlockingQueue<S>();
//        var activeWorkers = new AtomicInteger(nWorkers);
//        var barrier = new CyclicBarrier(nWorkers, () -> activeWorkers.set(nWorkers));
//        var prev = first;
//        var workers = new ArrayList<Worker>();
//        for (int i = 0; i < nWorkers; i++) {
//            // Next is a new queue if not the last element, and the first element if we are the last
//            var next = i == nWorkers - 1? first: new LinkedBlockingQueue<S>();
//            workers.add(new Worker(executor, configs[i], prev, next, barrier, activeWorkers, nWorkers, nRotateRounds));
//            prev = next;
//        }
//
//        // Generate initial solutions IN PARALLEL
//        var futures = new ArrayList<Future<S>>();
//        for (var worker : workers) {
//            futures.add(worker.buildInitialSolution(ins));
//        }
//
//        // Wait until all solutions are build before starting the next phase
//        var solutions = awaitAll(futures);
//
//        // Shuffle list and start working on them
//        //Collections.shuffle(solutions, RandomManager.getRandom());
//
//        // Improvement rounds
//        futures = new ArrayList<>();
//        for (int j = 0; j < workers.size(); j++) {
//            futures.add(workers.get(j).startWorker(solutions.get(j)));
//        }
//
//        S best = Solution.getBest(awaitAll(futures));
//        executor.shutdown();
//
//        return best;
//    }
//
//    public static class ILSConfig<S extends Solution<S,I>, I extends Instance> {
//        final Constructive<S, I> constructive;
//        final ConstructiveNeighborhood<S,I> constructiveNeighborhood;
//        final SolutionBuilder<S,I> solutionBuilder;
//        final Shake<S,I> shake;
//        final Improver<S,I> improver;
//        final int shakeStrength;
//        final int nShakes;
//
//        public ILSConfig(int shakeStrength, int nShakes, Supplier<Constructive<S, I>> constructorSupplier, ConstructiveNeighborhood<S, I> constructiveNeighborhood, SolutionBuilder<S,I> solutionBuilder, Supplier<Shake<S,I>> destructorSupplier, Supplier<Improver<S,I>> improver) {
//            this.shakeStrength = shakeStrength;
//            this.nShakes = nShakes;
//            this.constructiveNeighborhood = constructiveNeighborhood;
//            this.solutionBuilder = solutionBuilder;
//            assert constructorSupplier != null && destructorSupplier != null && improver != null;
//            this.constructive = constructorSupplier.get();
//            this.shake = destructorSupplier.get();
//            this.improver = improver.get();
//        }
//
//        public ILSConfig(int shakeStrength, Supplier<Constructive<S, I>> constructorSupplier, ConstructiveNeighborhood<S,I> constructiveNeighborhood, SolutionBuilder<S,I> solutionBuilder, Supplier<Shake<S,I>> destructorSupplier, Supplier<Improver<S,I>> improver) {
//            this(shakeStrength, -1, constructorSupplier, constructiveNeighborhood, solutionBuilder, destructorSupplier, improver);
//        }
//
//        @Override
//        public String toString() {
//            return "ILSConfig{" +
//                    "shakeStrength=" + shakeStrength +
//                    ", nShakes=" + nShakes +
//                    ", constructor=" + constructive +
//                    ", destructor=" + shake +
//                    ", improver=" + improver +
//                    '}';
//        }
//    }
//
//    public class Worker {
//        private final ExecutorService executor;
//        private final ILSConfig<S,I> config;
//        private final BlockingQueue<S> prev;
//        private final BlockingQueue<S> next;
//        private final CyclicBarrier barrier;
//        private final AtomicInteger activeWorkers; // Used to sync the workers
//        private final int nWorkers;
//        private final int nRotaterounds;
//
//        public Worker(ExecutorService executor, ILSConfig<S,I> config, BlockingQueue<S> prev, BlockingQueue<S> next, CyclicBarrier barrier, AtomicInteger activeWorkers, int nWorkers, int nRotaterounds) {
//            this.executor = executor;
//            this.config = config;
//            this.prev = prev;
//            this.next = next;
//            this.activeWorkers = activeWorkers;
//            this.barrier = barrier;
//            this.nWorkers = nWorkers;
//            this.nRotaterounds = nRotaterounds;
//        }
//
//        public Future<S> buildInitialSolution(I instance){
//            return executor.submit(() -> config.constructive.construct(instance, config.solutionBuilder)); //config.constructiveNeighborhood
//        }
//
//        public Future<S> startWorker(S initialSolution){
//            return executor.submit(() -> work(initialSolution));
//        }
//
//        private S work(S initialSolution) throws InterruptedException, BrokenBarrierException {
//            S best = initialSolution;
//            var nShakes = this.config.nShakes;
//            var iterations = nShakes / (this.nWorkers * nRotaterounds);
//
//            for (int round = 0; round < nRotaterounds; round++) {
//                // Do assigned work
//                for (int i = 1; i <= iterations; i++) {
//                    best = iteration(best);
//                }
//
//                // Mark current worker as finished
//                activeWorkers.decrementAndGet();
//
//                // Keep working until everyone finishes
//                while(activeWorkers.get() != 0){
//                    best = iteration(best);
//                }
//
//                next.add(best);                       // Push para el siguiente thread
//                this.barrier.await();                 // Esperamos a que todos  hayan hecho push
//
//                best = prev.take();                   // Pull del thread previo
//            }
//
//            return best;
//        }
//
//        private S iteration(S best) {
//            S current = best.cloneSolution();
//            this.config.shake.shake(current, this.config.shakeStrength);
//            this.config.improver.improve(current);
//            if (current.getOptimalValue() < best.getOptimalValue()) {
//                best = current;
//            }
//            return best;
//        }
//    }
//}
