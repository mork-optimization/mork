package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.algorithms.BaseAlgorithm;
import es.urjc.etsii.grafo.solver.algorithms.config.AlgorithmConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class AlgorithmsManager {

    private static final Logger log = Logger.getLogger(Orquestrator.class.toString());

    private final List<Algorithm> allAlgorithms = new ArrayList<>();

    public AlgorithmsManager() {}

    public List<Algorithm> getAlgorithms(){
        return Collections.unmodifiableList(this.allAlgorithms);
    }

    public void addAlgorithm(Algorithm algorithm){
        allAlgorithms.add(algorithm);
    }
}
