package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Service
public class ExperimentManager<S extends Solution<I>, I extends Instance> {

    private Pattern experimentFilter;

    private static final Logger log = Logger.getLogger(Orquestrator.class.toString());

    private final Map<String, List<Algorithm<S,I>>> experiments = new LinkedHashMap<>();

    public ExperimentManager(List<AbstractExperiment<S,I>> experimentImplementations, @Value("${solver.experiments}") String experimentFilterString) {
        experimentFilter = Pattern.compile(experimentFilterString);
        for (var experiment : experimentImplementations) {
            validateAlgorithmNames(experiment);
            String experimentName = experiment.getName();
            var matcher = experimentFilter.matcher(experimentName);
            if(matcher.matches()){
                experiments.put(experimentName, experiment.getAlgorithms());
                log.fine(String.format("Experiment %s matches against %s", experimentName, experimentFilterString));
            } else {
                log.fine(String.format("Experiment %s does not match against %s", experimentName, experimentFilterString));
            }
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
                throw new IllegalArgumentException(String.format("Duplicated algorithm name in experiment %s. FIX: All algorithm toString() should be unique per experiment → %s", experiment.getName(), name));
            }
            names.add(name);
        }
    }
}
