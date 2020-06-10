//package es.urjc.etsii.grafo.solver.algorithms;
//
//import es.urjc.etsii.grafo.io.Instance;
//import es.urjc.etsii.grafo.solution.Solution;
//import es.urjc.etsii.grafo.solver.create.Constructor;
//import es.urjc.etsii.grafo.solver.destructor.Shake;
//import es.urjc.etsii.grafo.solver.improve.Improver;
//import es.urjc.etsii.grafo.solver.improve.VND;
//import es.urjc.etsii.grafo.util.ConcurrencyUtil;
//
//import java.es.urjc.etsii.grafo.util.ArrayList;
//import java.es.urjc.etsii.grafo.util.Collections;
//import java.es.urjc.etsii.grafo.util.concurrent.Executors;
//import java.es.urjc.etsii.grafo.util.concurrent.Future;
//import java.es.urjc.etsii.grafo.util.function.Supplier;
//
//import static es.urjc.etsii.grafo.util.ConcurrencyUtil.await;
//
//public class MultiStartILS extends Algorithm{
//
//    private final int nShakes;
//    private final int k;
//    private final int n;
//    private final Shake shake;
//    private final VND vnd;
//
//    /**
//     * Create a new MultiStartAlgorithm, @see algorithm
//     * @param nShakes number of times we are going to shake the es.urjc.etsii.grafo.solution
//     *          If -1, calculate automatically the number of iterations
//     * @param n number of different solutions to evaluate
//     * @param k destructor strength
//     */
//    @SafeVarargs
//    public MultiStartILS(int nShakes, int k, int n, Supplier<Constructor> constructorSupplier, Supplier<Shake> destructor, Supplier<Improver>... improvers) {
//        super(constructorSupplier, improvers);
//        this.nShakes = nShakes;
//        this.k = k;
//        this.n = n;
//        this.shake = destructor.get();
//        vnd = new VND();
//    }
//
//    /**
//     * Executes the algorythm for the given instance
//     * @param ins Instance the algorithm will process
//     * @return Best es.urjc.etsii.grafo.solution found
//     */
//    @Override
//    public Solution algorithm(Instance ins) {
//        int nThreads = ConcurrencyUtil.getNThreads();
//        var executor = Executors.newFixedThreadPool(nThreads);
//        var futures = Collections.synchronizedList(new ArrayList<Future<Solution>>());
//        for (int i = 0; i < n; i++) {
//            futures.add(executor.submit(() -> getSolution(ins)));
//        }
//        Solution best = null;
//        for (var future : futures) {
//            Solution s = await(future);
//            if(best == null || s.getOptimalValue() > best.getOptimalValue()){
//                best = s;
//            }
//        }
//
//        return best;
//    }
//
//    private Solution getSolution(Instance ins) {
//        Solution best = this.constructor.construct(ins);
//        best = vnd.doIt(best, improvers);
//
//        for (int i = 1; i <= this.nShakes; i++) {
//            var copy = best.clone();
//            this.shake.iteration(copy, k);
//            copy = this.vnd.doIt(copy, this.improvers);
//            best = copy.getBetterSolution(best);
//        }
//
//        return best;
//    }
//
//    @Override
//    public String toString() {
//        return this.getClass().getSimpleName() + "{" +
//                "iterations=" + (nShakes ==-1? "AUTO": nShakes) +
//                ", n=" + n +
//                ", k=" + k +
//                ", constructor=" + constructor +
//                ", destructor=" + shake +
//                ", improver=" + improvers +
//                '}';
//    }
//}
