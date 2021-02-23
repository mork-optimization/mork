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

    public ExperimentManager(List<AbstractExperiment<S,I>> experimentImplementations) {
        for (var experiment : experimentImplementations) {
            validateAlgorithmNames(experiment);
            experiments.put(experiment.getName(), experiment.getAlgorithms());
        }
    }

    public Map<String, List<Algorithm<S, I>>> getExperiments(){
        return Collections.unmodifiableMap(this.experiments);
    }

    private void validateAlgorithmNames(AbstractExperiment<S,I> experiment){
        var algorithms = experiment.getAlgorithms();
        Set<String> names = new HashSet<>();
        for(var algorithm: algorithms){
            var name = algorithm.toString();
            if(names.contains(name)){
                throw new IllegalArgumentException(String.format("Duplicated algorithm name in experiment %s. FIX: All algorithm toString() should be unique per experiment --> %s", experiment.getName(), name));
            }
            names.add(name);
        }
    }
}
