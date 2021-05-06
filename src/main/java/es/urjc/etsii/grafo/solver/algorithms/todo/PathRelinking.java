//package es.urjc.etsii.grafo.quasi_clique.path_relinking;
//
//import es.urjc.etsii.grafo.quasi_clique.model.CliqueInstance;
//import es.urjc.etsii.grafo.quasi_clique.model.CliqueSolution;
//import es.urjc.etsii.grafo.quasi_clique.model.RemoveNeighbourhood;
//import es.urjc.etsii.grafo.solution.Move;
//import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
//import es.urjc.etsii.grafo.solver.create.Constructive;
//import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;
//import es.urjc.etsii.grafo.solver.destructor.Shake;
//import es.urjc.etsii.grafo.solver.improve.Improver;
//import es.urjc.etsii.grafo.util.CollectionUtils;
//import es.urjc.etsii.grafo.util.DoubleComparator;
//
//import java.util.Arrays;
//import java.util.Set;
//import java.util.logging.Logger;
//
//public class PathRelinking implements Algorithm<CliqueSolution, CliqueInstance> {
//
//    private static final Logger log = Logger.getLogger(PathRelinking.class.getName());
//    private final Constructive<CliqueSolution, CliqueInstance> constructive;
//    private final Shake<CliqueSolution, CliqueInstance> shake;
//    private final Improver<CliqueSolution, CliqueInstance>[] improvers;
//
//    @SafeVarargs
//    public PathRelinking(Constructive<CliqueSolution, CliqueInstance> constructive, Shake<CliqueSolution, CliqueInstance> shake, Improver<CliqueSolution, CliqueInstance>... improvers) {
//        this.constructive = constructive;
//        this.shake = shake;
//        this.improvers = improvers;
//    }
//
//    @Override
//    public CliqueSolution algorithm(CliqueInstance ins, SolutionBuilder<CliqueSolution, CliqueInstance> builder) {
//        CliqueSolution solution;
//        CliqueSolution best = null;
//        do {
//            solution = iteration(ins, builder);
//            if(best == null){
//                best = solution;
//            }
//            best = best.getBetterSolution(solution);
//            System.out.print(best.getOptimalValue() + ", ");
//        } while (!best.stop()); // TODO Possible bug, each iteration resets the solution time counter
//        System.out.println();
//        return best;
//    }
//
//    private CliqueSolution iteration(CliqueInstance ins, SolutionBuilder<CliqueSolution, CliqueInstance> builder) {
//        var solution = builder.initializeSolution(ins);
//        solution = constructive.construct(solution);
//        CliqueSolution s1 = solution.cloneSolution();
//        CliqueSolution s2 = solution.cloneSolution();
//
//        // Path relinking goes from s1 to s2
//        localSearch(s1);
//        shake.shake(s2, 1, 1, true);
//        localSearch(s2);
//
//        //     We now have
//        //         S
//        //       /   \
//        //  LS  /     \  LS +
//        //     /       \ Shake
//        //    /         \
//        //   v           v
//        // S1 --- PATH → S2
//
//        var bestIntermediate = analyzeAllSolutionsInPath(s1, s2);
//
//        // Get best and return
//        var bestPrevious = s1.getBetterSolution(s2);
//        return bestIntermediate.getBetterSolution(bestPrevious);
//    }
//
//    private void localSearch(CliqueSolution solution) {
//        for (Improver<CliqueSolution, CliqueInstance> ls : improvers) {
//            solution = ls.improve(solution);
//        }
//    }
//
//    private CliqueSolution analyzeAllSolutionsInPath(CliqueSolution cs1, CliqueSolution cs2){
//
//        var difference = CliqueSolution.cliqueDifference(cs1, cs2);
//        var best1 = doPath(cs1, difference);
//        var best2 = doPath(cs2, difference);
//
//        return best1.getBetterSolution(best2);
//    }
//
//    private CliqueSolution lastToBest(CliqueSolution best,  CliqueSolution last){
//        repairSolution(last);
//        localSearch(last);
//        return best.getBetterSolution(last);
//    }
//
//    private CliqueSolution doPath(CliqueSolution a,CliqueSolution.CliqueDifference difference) {
//        // Adjust sizes
//
//        CliqueSolution best = a;
//        CliqueSolution last = a;
//
//        while (difference.inANotInB.size() > difference.inBNotInA.size()){
//            var s = removeNode(difference.inANotInB, last);
//            best = lastToBest(best, last);
//            last = s;
//        }
//
//        while (difference.inANotInB.size() < difference.inBNotInA.size()){
//            var s = addNode(difference.inBNotInA, last);
//            best = lastToBest(best, last);
//            last = s;
//        }
//
//        // Equal sizes, start swapping
//        while(difference.inANotInB.size() > 0){
//            var s = swapNodes(difference.inANotInB, difference.inBNotInA, last);
//            best = lastToBest(best, last);
//            last = s;
//        }
//
//        assert difference.inBNotInA.size() == 0;
//        assert difference.inANotInB.size() == 0;
//
//        return best;
//    }
//
//    private CliqueSolution addNode(Set<Integer> set, CliqueSolution s){
//        var prima = (CliqueSolution) s.cloneSolution();
//        int n = CollectionUtils.pickRandom(set);
//        set.remove(n);
//        prima.add(n, prima.addCost(n));
//        return prima;
//    }
//
//    private CliqueSolution removeNode(Set<Integer> set, CliqueSolution s){
//        var prima = (CliqueSolution) s.cloneSolution();
//        int n = CollectionUtils.pickRandom(set);
//        set.remove(n);
//        prima.remove(n, prima.addCost(n));
//        return prima;
//    }
//
//    private CliqueSolution swapNodes(Set<Integer> set1, Set<Integer> set2, CliqueSolution s){
//        var prima = (CliqueSolution) s.cloneSolution();
//        int n1 = CollectionUtils.pickRandom(set1);
//        int n2 = CollectionUtils.pickRandom(set2);
//        set1.remove(n1);
//        set2.remove(n2);
//        prima.swap(n1, n2, prima.swapCost(n1, n2));
//        return prima;
//    }
//
//    private void repairSolution(CliqueSolution cliqueSolution) {
//        // TODO outsource?
//        var removeNeighbourhood = new RemoveNeighbourhood();
//        while (DoubleComparator.isLessThan(cliqueSolution.getRatio(), cliqueSolution.getMinimumRatio())) {
//            var ratioBefore = cliqueSolution.getRatio();
//            var move = removeNeighbourhood.stream(cliqueSolution).reduce(Move::getBestMove).orElseThrow(() -> new IllegalStateException(String.format("Solution rate (%s) is less than minimum %s and there arent any viable remove moves", cliqueSolution.getRatio(), cliqueSolution.getMinimumRatio())));
//            move.execute();
//            var ratioAfter = cliqueSolution.getRatio();
//            assert DoubleComparator.isGreaterThan(ratioAfter, ratioBefore); // Si no mejora el ratio algo hemos hecho mal :(
//        }
//    }
//
//    @Override
//    public String toString() {
//        return "PathRelinking{" +
//                "constructive=" + constructive +
//                ", shake=" + shake +
//                ", improvers=" + Arrays.toString(improvers) +
//                '}';
//    }
//}
