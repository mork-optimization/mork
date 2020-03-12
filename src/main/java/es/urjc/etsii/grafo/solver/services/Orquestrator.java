package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.algorithms.BaseAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class Orquestrator implements CommandLineRunner {

    private static final Logger log = Logger.getLogger(Orquestrator.class.toString());

    private final IOManager io;
    private final AlgorithmsManager algorithmsManager;

    @Value("${solver.repetitions:1}")
    private int repetitions;

    public Orquestrator(IOManager io, AlgorithmsManager algorithmsManager) {
        this.io = io;
        this.algorithmsManager = algorithmsManager;
    }

    @Override
    public void run(String... args) {
        log.info("App started, lets rock & roll...");
        io.getInstances().forEach(this::runAlgorithmsForInstance);
    }

    public void runAlgorithmsForInstance(Instance i){
        log.info("Running algorithms for instance: " + i.getName());
        for(BaseAlgorithm<?,?> alg: this.algorithmsManager.getAlgorithms()){
            alg.execute(i, repetitions);
        }
    }
}
