package es.urjc.etsii.grafo.util.random;

import es.urjc.etsii.grafo.solver.SolverConfig;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class RandomInitializer {

    private final SolverConfig solverConfig;

    public RandomInitializer(SolverConfig config) {
        this.solverConfig = config;
    }

    @PostConstruct
    public void initializeRandomManager(){
        RandomManager.reinitialize(solverConfig.getRandomType(), solverConfig.getSeed(), solverConfig.getRepetitions());
    }
}
