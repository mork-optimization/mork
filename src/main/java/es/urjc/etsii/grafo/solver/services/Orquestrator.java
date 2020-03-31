package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.WorkingOnResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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
        log.info("Available algorithms: " + this.algorithmsManager.getAlgorithms());

        List<WorkingOnResult> results = new ArrayList<>();
        io.getInstances().forEach(instance -> runAlgorithmsForInstance(results, instance));
        // TODO Update results file in disk after each instance is solved, not in the end
        this.io.saveResults(results);
    }

    public void runAlgorithmsForInstance(List<WorkingOnResult> results, Instance i){
        log.info("Running algorithms for instance: " + i.getName());
        for(var alg: this.algorithmsManager.getAlgorithms()){
            log.info("Algorithm: "+ alg);
            WorkingOnResult r = alg.execute(i, repetitions);
            results.add(r);
        }
    }
}
