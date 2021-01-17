package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;

@Service
public class ExperimentManager<S extends Solution<I>, I extends Instance> {

    private static final Logger log = Logger.getLogger(Orquestrator.class.toString());

    private final Map<String, List<Algorithm<S,I>>> experiments = new HashMap<>();

    public ExperimentManager(AbstractExperiment<S,I> experimentSetup) {
        experiments.put(experimentSetup.getName(), experimentSetup.getAlgorithms());
    }

    public Map<String, List<Algorithm<S, I>>> getExperiments(){
        return Collections.unmodifiableMap(this.experiments);
    }

}
