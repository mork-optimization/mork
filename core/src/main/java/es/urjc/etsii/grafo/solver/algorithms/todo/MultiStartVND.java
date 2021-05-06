//package es.urjc.etsii.grafo.solver.algorithms;
//
//import es.urjc.etsii.grafo.io.Instance;
//import es.urjc.etsii.grafo.solution.Solution;
//import es.urjc.etsii.grafo.solver.create.Constructor;
//import es.urjc.etsii.grafo.solver.improve.Improver;
//import es.urjc.etsii.grafo.solver.improve.VND;
//
//import java.es.urjc.etsii.grafo.util.function.Supplier;
//
//public class MultiStartVND extends Algorithm {
//
//    private int executions;
//    private VND vnd;
//    /**
//     * Create a new MultiStartAlgorithm, @see algorithm
//     * @param nStarts Tries, the algorythm will be executed i times, returns the best.
//     *          If -1, calculate automatically the number of iterations
//     */
//    @SafeVarargs
//    public MultiStartVND(int nStarts, Supplier<Constructor> constructorSupplier, Supplier<Improver>... improvers) {
//        super(constructorSupplier, improvers);
//        this.executions = nStarts;
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
//
//        Solution best = null;
//        for (int i = 1; i <= executions; i++) {
//            Solution current = this.constructor.construct(ins);
//            current = vnd.doIt(current, improvers);
//            best = current.getBetterSolution(best);
//        }
//
//        return best;
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
