//package es.urjc.etsii.grafo.flayouts.experiments;
//
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//public class PreviousExperiment extends AbstractExperiment<FLPSolution, FLPInstance> {
//
//    protected PreviousExperiment(SolverConfig config) {
//        super(config);
//    }
//
//    @Override
//    public List<Algorithm<FLPSolution, FLPInstance>> getAlgorithms() {
//        var gurobi = new DRFLPGurobiAdapter();
//        return Arrays.asList(
//                new Heuristic1(gurobi, 1, TimeUnit.HOURS),
//                new Heuristic2(gurobi, 1, TimeUnit.HOURS),
//                new Heuristic3(gurobi, 1, TimeUnit.HOURS),
//                new Heuristic4(gurobi, 1, TimeUnit.HOURS)
//        );
//    }
//}
