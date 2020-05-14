package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@Service
public class AlgorithmsManager {

    private static final Logger log = Logger.getLogger(Orquestrator.class.toString());

    private final List<Algorithm<?,?>> allAlgorithms = new ArrayList<>();

    public AlgorithmsManager() {}

    public List<? extends Algorithm> getAlgorithms(){
        return Collections.unmodifiableList(this.allAlgorithms);
    }

    public void addAlgorithm(Algorithm<?,?> algorithm){
        allAlgorithms.add(algorithm);
    }
}
