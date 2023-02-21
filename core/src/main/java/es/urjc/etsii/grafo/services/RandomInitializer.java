package es.urjc.etsii.grafo.services;

import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.util.random.RandomManager;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class RandomInitializer {

    private final SolverConfig solverConfig;

    public RandomInitializer(SolverConfig config) {
        this.solverConfig = config;
    }

    @PostConstruct
    public void initializeRandomManager(){
        RandomManager.globalConfiguration(solverConfig.getRandomType(), solverConfig.getSeed(), solverConfig.getRepetitions());
    }
}
