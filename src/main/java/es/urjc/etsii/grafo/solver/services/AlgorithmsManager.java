package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@Service
public class AlgorithmsManager<S extends Solution<I>, I extends Instance> {

    private static final Logger log = Logger.getLogger(Orquestrator.class.toString());

    private final List<Algorithm<S,I>> allAlgorithms = new ArrayList<>();

    public AlgorithmsManager() {}

    public List<Algorithm<S,I>> getAlgorithms(){
        return Collections.unmodifiableList(this.allAlgorithms);
    }

    public void addAlgorithm(Algorithm<S,I> algorithm){
        allAlgorithms.add(algorithm);
    }
}
