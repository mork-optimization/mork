package es.urjc.etsii.grafo.CAP.experiment;

import es.urjc.etsii.grafo.CAP.components.*;
import es.urjc.etsii.grafo.CAP.model.CAPInstance;
import es.urjc.etsii.grafo.CAP.model.CAPSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;
import es.urjc.etsii.grafo.services.TimeLimitCalculator;

import java.util.ArrayList;
import java.util.List;

public class FinalExperiment extends AbstractExperiment<CAPSolution, CAPInstance> {

    private final AutomaticAlgorithmBuilder<CAPSolution, CAPInstance> builder;
    private final TimeLimitCalculator<CAPSolution, CAPInstance> timeLimit;

    public FinalExperiment(AutomaticAlgorithmBuilder<CAPSolution, CAPInstance> algorithmBuilder, TimeLimitCalculator<CAPSolution, CAPInstance> timeLimit) {
        this.builder = algorithmBuilder;
        this.timeLimit = timeLimit;
    }

    @Override
    public List<Algorithm<CAPSolution, CAPInstance>> getAlgorithms() {
        var algorithms = new ArrayList<Algorithm<CAPSolution, CAPInstance>>();

        algorithms.add(new CAPC("SOTA", "src_c/prop-sota.txt", timeLimit));
        algorithms.add(new CAPC("AutoConfigPaper", "src_c/prop-autoconfig.txt", timeLimit));
        algorithms.add(new CAPC("AutoConfigPaper2", "src_c/prop-autoconfig2.txt", timeLimit));
        algorithms.add(new CAPC("AutoConfigNuevo", "src_c/prop-autoconfig-nuevo.txt", timeLimit));

        return algorithms;
    }
//
//    public Algorithm<CAPSolution, CAPInstance> sotaAlgorithm(){
//        var constructive = new CAPConstructive("greedyB2", 0.5);
//        var ls = new CAPLS("ext_fhi");
//        var shake = new CAPShake("2");
//        VNS.KMapper<CAPSolution, CAPInstance> kMapper = (s, k) -> k > 5? VNS.KMapper.STOPNOW: k;
//        var bvnsAlberto = new VNS<>("BVNSReimplementation", kMapper, constructive, shake, ls);
//
//        //return bvnsAlberto;
//        return new MultiStartAlgorithm<>("BVNSReimplementation", bvnsAlberto, 100, 100, 100);
//    }
//
//    public Algorithm<CAPSolution, CAPInstance> sotaAlgorithmTabuEscondio(){
//        var constructive = new CAPConstructive("greedyB2", 0.5);
//        var ls = new CAPLS("ext_fhi");
//        var shake = new CAPShake("2");
//
//        var bvnsAlberto = new MyVNS("BVNSReimplementationTS", constructive, shake, ls);
//
//        //return bvnsAlberto;
//        return new MultiStartAlgorithm<>("BVNSReimplementationTS", bvnsAlberto, 100, 100, 100);
//    }
}
