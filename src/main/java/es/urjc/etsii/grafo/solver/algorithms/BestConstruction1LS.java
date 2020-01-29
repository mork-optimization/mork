//package es.urjc.etsii.grafo.solver.algorithms;
//
//import es.urjc.etsii.grafo.io.Instance;
//import es.urjc.etsii.grafo.solution.Solution;
//import es.urjc.etsii.grafo.solver.create.Constructor;
//import es.urjc.etsii.grafo.solver.improve.Improver;
//
//import java.es.urjc.etsii.grafo.util.concurrent.TimeUnit;
//import java.es.urjc.etsii.grafo.util.function.Supplier;
//
////TODO pass an executor to the algotythm, sequential or parallel
//public class BestConstruction1LS extends Algorithm {
//
//    private int executions;
//
//    /**
//     * Create a new MultiStartAlgorithm, @see algorithm
//     * @param i Tries, the algorythm will be executed i times, returns the best.
//     */
//    @SafeVarargs
//    public BestConstruction1LS(int i, Supplier<Constructor> constructorSupplier, Supplier<Improver>... improvers) {
//        super(constructorSupplier, improvers);
//        this.executions = i;
//    }
//
//    /**
//     * Executes the algorythm for the given instance
//     * @param ins Instance the algorithm will process
//     * @return Best es.urjc.etsii.grafo.solution found
//     */
//    @Override
//    public Solution algorithm(Instance ins) {
//        Solution s = this.constructor.construct(ins);
//        for (int i = 1; i < executions; i++) {
//            Solution temp = this.constructor.construct(ins);
//            s = temp.getBetterSolution(s);
//        }
//        Solution javaPlis = s;
//        improvers.forEach(i -> i.improve(javaPlis, 10, TimeUnit.MINUTES));
//        return javaPlis;
//    }
//
//    @Override
//    public String toString() {
//        return this.getClass().getSimpleName() + "{" +
//                "executions=" + executions +
//                ", constructor=" + constructor +
//                ", improver=" + improvers +
//                '}';
//    }
//}
