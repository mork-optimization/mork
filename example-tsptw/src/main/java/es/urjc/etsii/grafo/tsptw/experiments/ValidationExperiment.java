package es.urjc.etsii.grafo.tsptw.experiments;

import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.tsptw.alg.GVNS;
import es.urjc.etsii.grafo.tsptw.model.TSPTWConfig;
import es.urjc.etsii.grafo.tsptw.model.TSPTWInstance;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.experiment.AbstractExperiment;

import java.util.List;

public class ValidationExperiment extends AbstractExperiment<TSPTWSolution, TSPTWInstance> {

    private final TSPTWConfig config;
    private final AutomaticAlgorithmBuilder<TSPTWSolution, TSPTWInstance> algorithmBuilder;

    public ValidationExperiment(TSPTWConfig config, AutomaticAlgorithmBuilder<TSPTWSolution, TSPTWInstance> algorithmBuilder) {
        // Any config class can be requested via the constructor
        this.config = config;
        this.algorithmBuilder = algorithmBuilder;
    }

    @Override
    public List<Algorithm<TSPTWSolution, TSPTWInstance>> getAlgorithms() {
        return List.of(
                // best 3 autoconfig
                algorithmBuilder.buildFromStringParams("""
                        ROOT=GVNS ROOT_GVNS.attemptsPerLevel=12 ROOT_GVNS.constructive=TSPTWFeasibleConstructive ROOT_GVNS.constructive_TSPTWFeasibleConstructive.initial=TSPTWRandomConstructiveNew ROOT_GVNS.constructive_TSPTWFeasibleConstructive.initial_TSPTWRandomConstructiveNew.candidateListSize=3 ROOT_GVNS.constructive_TSPTWFeasibleConstructive.initial_TSPTWRandomConstructiveNew.urgencyWeight=0.73 ROOT_GVNS.constructive_TSPTWFeasibleConstructive.perturbation=TSPTWUnrestrictedInsertShake ROOT_GVNS.constructive_TSPTWFeasibleConstructive.repair=TSPTWFeasibilityRepairFullNew ROOT_GVNS.levelMax=13 ROOT_GVNS.localSearch=TSPTWVNDNew ROOT_GVNS.localSearch_TSPTWVNDNew.searches.item0=TSPTWTwoOptSearch ROOT_GVNS.localSearch_TSPTWVNDNew.searches.item0_TSPTWTwoOptSearch.item1=TSPTWTwoOptSearchNew ROOT_GVNS.localSearch_TSPTWVNDNew.searches.item0_TSPTWTwoOptSearch.item1_TSPTWTwoOptSearchNew.item2=TSPTWInsertionSearch ROOT_GVNS.shake=TSPTWFeasibleInsertShakeNew ROOT_GVNS.shake_TSPTWFeasibleInsertShakeNew.attemptFactor=7
                        """),
                algorithmBuilder.buildFromStringParams("""
                        ROOT=GVNS ROOT_GVNS.attemptsPerLevel=8 ROOT_GVNS.constructive=TSPTWFeasibleConstructive ROOT_GVNS.constructive_TSPTWFeasibleConstructive.initial=TSPTWRandomConstructiveNew ROOT_GVNS.constructive_TSPTWFeasibleConstructive.initial_TSPTWRandomConstructiveNew.candidateListSize=3 ROOT_GVNS.constructive_TSPTWFeasibleConstructive.initial_TSPTWRandomConstructiveNew.urgencyWeight=0.49 ROOT_GVNS.constructive_TSPTWFeasibleConstructive.perturbation=TSPTWUnrestrictedInsertShake ROOT_GVNS.constructive_TSPTWFeasibleConstructive.repair=TSPTWFeasibilityRepairFullNew ROOT_GVNS.levelMax=12 ROOT_GVNS.localSearch=TSPTWVNDNew ROOT_GVNS.localSearch_TSPTWVNDNew.searches.item0=TSPTWTwoOptSearch ROOT_GVNS.localSearch_TSPTWVNDNew.searches.item0_TSPTWTwoOptSearch.item1=TSPTWInsertionSearch ROOT_GVNS.localSearch_TSPTWVNDNew.searches.item0_TSPTWTwoOptSearch.item1_TSPTWInsertionSearch.item2=TSPTWOrOptSearchNew ROOT_GVNS.localSearch_TSPTWVNDNew.searches.item0_TSPTWTwoOptSearch.item1_TSPTWInsertionSearch.item2_TSPTWOrOptSearchNew.component.blockLength=3 ROOT_GVNS.shake=TSPTWFeasibleInsertShakeNew ROOT_GVNS.shake_TSPTWFeasibleInsertShakeNew.attemptFactor=4
                        """),
                algorithmBuilder.buildFromStringParams("""
                        ROOT=GVNS ROOT_GVNS.attemptsPerLevel=16 ROOT_GVNS.constructive=TSPTWFeasibleConstructiveBestNew ROOT_GVNS.constructive_TSPTWFeasibleConstructiveBestNew.initial=TSPTWRandomConstructiveNew ROOT_GVNS.constructive_TSPTWFeasibleConstructiveBestNew.initial_TSPTWRandomConstructiveNew.candidateListSize=6 ROOT_GVNS.constructive_TSPTWFeasibleConstructiveBestNew.initial_TSPTWRandomConstructiveNew.urgencyWeight=0.86 ROOT_GVNS.constructive_TSPTWFeasibleConstructiveBestNew.perturbation=TSPTWUnrestrictedInsertShake ROOT_GVNS.constructive_TSPTWFeasibleConstructiveBestNew.repair=TSPTWFeasibilityRepairFullNew ROOT_GVNS.levelMax=6 ROOT_GVNS.localSearch=TSPTWVNDNew ROOT_GVNS.localSearch_TSPTWVNDNew.searches.item0=TSPTWTwoOptSearch ROOT_GVNS.localSearch_TSPTWVNDNew.searches.item0_TSPTWTwoOptSearch.item1=TSPTWInsertionSearch ROOT_GVNS.localSearch_TSPTWVNDNew.searches.item0_TSPTWTwoOptSearch.item1_TSPTWInsertionSearch.item2=TSPTWOrOptSearchNew ROOT_GVNS.localSearch_TSPTWVNDNew.searches.item0_TSPTWTwoOptSearch.item1_TSPTWInsertionSearch.item2_TSPTWOrOptSearchNew.component.blockLength=3 ROOT_GVNS.shake=TSPTWFeasibleInsertShake
                        """),

                // sota
                new GVNS()

        );
    }
}
