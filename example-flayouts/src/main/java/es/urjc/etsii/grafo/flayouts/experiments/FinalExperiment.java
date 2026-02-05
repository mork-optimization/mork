//package es.urjc.etsii.grafo.flayouts.experiments;
//
//import es.urjc.etsii.grafo.config.SolverConfig;
//import es.urjc.etsii.grafo.experiment.AbstractExperiment;
//import es.urjc.etsii.grafo.flayouts.model.FLPInstance;
//import es.urjc.etsii.grafo.flayouts.model.FLPSolution;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//public class FinalExperiment extends AbstractExperiment<FLPSolution, FLPInstance> {
//
//    protected FinalExperiment(SolverConfig config) {
//        super(config);
//    }
//
//    @Override
//    public List<Algorithm<FLPSolution, FLPInstance>> getAlgorithms() {
//        var algorithms = new ArrayList<Algorithm<FLPSolution, FLPInstance>>();
//
//        algorithms.add(iraceConfig());
//        //algorithms.add(myConfig());
//
//        return algorithms;
//    }
//
//
//
////    public Algorithm<FLPSolution, FLPInstance> myConfig(){
////        // el nuevo shake es: new BestPositionShake(), el que supera a sora es el interrow
////        return new FakeIncrementer("RealIG", (fakes) -> new ParallelMultiStartAlgorithm(
////                1000, "RealIG", new IteratedGreedy<>(1000, 100,
////                new DRFPTetrisConstructive(fakes, 0.5d),
////                new DestroyRebuild<>(new DRFPTetrisConstructive(fakes, 0.5d), new IRSDestructive()),
////                moveBySwapLS
////            )),
////            new FIUtil.FibonacciFakesForIteration(),
////            new FIUtil.FractionOfWidthStop(0.1D),
////            10,
////            TimeUnit.MINUTES
////        );
////    }
//
//    public Algorithm<FLPSolution, FLPInstance> iraceConfig(){
///*
//2022-02-06 03:19:08.777  INFO 838498 --- [main] e.u.e.g.s.irace.runners.RLangRunner      :    constructive    reconstructive   stop       increment           iterationsratio alpha1 alpha2 destratio fractionv constantv linearratio
//2022-02-06 03:19:08.777  INFO 838498 --- [main] e.u.e.g.s.irace.runners.RLangRunner      :    graspgr         tetris           constant   fiboinc             160             0.0898 0.5518      <NA>        NA        27          NA
//*/
//        int totalIterations = 1000 * 1000;
//        int multiStartIterations = 160; // el ratio
//        int iteratedGreedyIterations = totalIterations / multiStartIterations;
//
//        return new FakeIncrementer("MSIG", (fakes) -> new ParallelMultiStartAlgorithm(
//                multiStartIterations, "MSIG", new IteratedGreedy<>(iteratedGreedyIterations, 100,
//                new GreedyRandomGRASPConstructive<>(new FakeFacilitiesDRFPListManager(fakes), 0.0898d, false),
//                new DestroyRebuild<>(new DRFPTetrisConstructive(fakes, 0.5518d), new IRSDestructive()),
//                moveBySwapLS
//                )),
//                new FIUtil.FibonacciFakesForIteration(),
//                new FIUtil.ConstantStop(27),
//                10,
//                TimeUnit.MINUTES);
//    }
//}
