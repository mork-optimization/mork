//package es.urjc.etsii.grafo.CAP.components;
//
//import es.urjc.etsii.grafo.CAP.model.CAPInstance;
//import es.urjc.etsii.grafo.CAP.model.CAPSolution;
//import es.urjc.etsii.grafo.algorithms.Algorithm;
//import es.urjc.etsii.grafo.create.Constructive;
//import es.urjc.etsii.grafo.improve.Improver;
//import es.urjc.etsii.grafo.metrics.BestObjective;
//import es.urjc.etsii.grafo.metrics.Metrics;
//import es.urjc.etsii.grafo.shake.Shake;
//import es.urjc.etsii.grafo.util.TimeControl;
//import es.urjc.etsii.grafo.util.ValidationUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.HashSet;
//
//public class MyVNS extends Algorithm<CAPSolution, CAPInstance> {
//
//    private final double BETA_COEFFICIENT = 0.1;
//
//    private static final Logger log = LoggerFactory.getLogger(MyVNS.class);
//    protected Improver<CAPSolution, CAPInstance> improver;
//
//    /**
//     * Constructive procedure
//     */
//    protected Constructive<CAPSolution, CAPInstance> constructive;
//
//    /**
//     * Shake procedure
//     */
//    protected Shake<CAPSolution, CAPInstance> shake;
//
////    @AutoconfigConstructor
////    public MyVNS(
////            @ProvidedParam String algorithmName,
////            Constructive<CAPSolution, CAPInstance> constructive,
////            Shake<CAPSolution, CAPInstance> shake,
////            Improver<CAPSolution, CAPInstance> improver
////    ) {
////        this(algorithmName, constructive, shake, improver);
////    }
//
//    /**
//     * Execute VNS until finished
//     *
//     * @param algorithmName Algorithm name, example: "VNSWithRandomConstructive"
//     * @param shake         Perturbation method
//     * @param constructive  Constructive method
//     * @param improver      List of improvers/local searches
//     */
//    public MyVNS(String algorithmName, Constructive<CAPSolution, CAPInstance> constructive, Shake<CAPSolution, CAPInstance> shake, Improver<CAPSolution, CAPInstance> improver) {
//        super(algorithmName);
//        this.shake = shake;
//        this.constructive = constructive;
//        this.improver = improver;
//    }
//
//    public CAPSolution algorithm(CAPInstance instance) {
//        int kmin = 1, currentK = kmin;
//        int kmax = (int) Math.ceil(BETA_COEFFICIENT * instance.nN);
//        var visitedSet = new HashSet<CAPSolution>(); // Cache of visited nodes to avoid repeating same work
//
//        var solution = this.newSolution(instance);
//        solution = constructive.construct(solution);
//
//        while(currentK <= kmax && !TimeControl.isTimeUp()) {
//            CAPSolution copy;
//            do {
//                copy = solution.cloneSolution();
//                copy = shake.shake(copy, currentK);
//            } while (visitedSet.contains(copy) && !TimeControl.isTimeUp());
//
//            copy = improver.improve(copy);
//            if (copy.isBetterThan(solution)) {
//                solution = copy;
//                currentK = kmin;
//                onSolutionChanged(visitedSet, solution);
//            } else {
//                currentK++;
//            }
//        }
//
//        return solution;
//    }
//
//    private static void onSolutionChanged(HashSet<CAPSolution> visited, CAPSolution solution) {
//        visited.add(solution);
//        Metrics.add(BestObjective.class, solution.getScore());
//        ValidationUtil.assertValidScore(solution);
//    }
//
//    @Override
//    public String toString() {
//        return "MyVNS{" +
//                "improver=" + improver +
//                ", constructive=" + constructive +
//                ", shake=" + shake +
//                '}';
//    }
//}
