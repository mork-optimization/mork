package es.urjc.etsii.grafo.solver.services;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;
import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@Service
public class ExperimentManager<S extends Solution<S, I>, I extends Instance> {

    private Pattern experimentFilter;

    private static final Logger log = Logger.getLogger(Orquestrator.class.toString());

    /**
     * List of experiments
     */
    private final Map<String, List<Algorithm<S, I>>> experiments = new LinkedHashMap<>();

    /**
     * Constructor
     * @param experimentImplementations list of experiments
     * @param solverConfig solver configuration
     * @param solutionBuilder solution builder
     */
    public ExperimentManager(List<AbstractExperiment<S, I>> experimentImplementations, SolverConfig solverConfig, SolutionBuilder<S, I> solutionBuilder) {
        var experimentPattern = solverConfig.getExperiments();
        experimentFilter = Pattern.compile(experimentPattern);

        for (var experiment : experimentImplementations) {
            var algorithms = experiment.getAlgorithms();
            fillSolutionBuilder(algorithms, solutionBuilder);
            validateAlgorithmNames(experiment.getName(), algorithms);
            String experimentName = experiment.getName();
            var matcher = experimentFilter.matcher(experimentName);
            if (matcher.matches()) {
                this.experiments.put(experimentName, algorithms);
                log.fine(String.format("Experiment %s matches against %s", experimentName, experimentPattern));
            } else {
                log.fine(String.format("Experiment %s does not match against %s, ignoring", experimentName, experimentPattern));
            }
        }
    }

    /**
     * For each algorithm in each experiment, set the solution builder reference.
     *
     * @param algorithms      list of algorithms
     * @param solutionBuilder solution builder
     */
    private void fillSolutionBuilder(Iterable<Algorithm<S, I>> algorithms, SolutionBuilder<S, I> solutionBuilder) {
        for (var algorithm : algorithms) {
            algorithm.setBuilder(solutionBuilder);
        }
    }

    /**
     * Returns a map with the list of algorithms for each of the experiments.
     *
     * @return mapping of experiments and it associated list of algorithms
     */
    public Map<String, List<Algorithm<S, I>>> getExperiments() {
        return Collections.unmodifiableMap(this.experiments);
    }

    /**
     * This procedure validates that there are no more than one algorithm with the same name (both toString and shortName)
     *
     * @param experimentName experiment name
     * @param algorithms     list of algorithms
     */
    private void validateAlgorithmNames(String experimentName, List<Algorithm<S, I>> algorithms) {
        Set<String> toStrings = new HashSet<>();
        Set<String> shortNames = new HashSet<>();
        for (var algorithm : algorithms) {
            // Check Algorithm::toString
            var toString = algorithm.toString();
            if (toStrings.contains(toString)) {
                throw new IllegalArgumentException(String.format("Duplicated algorithm toString in experiment %s. FIX: All algorithm toString() should be unique per experiment → %s", experimentName, toString));
            }
            toStrings.add(toString);

            // Same check for Algorithm::getShortName
            var shortName = algorithm.getShortName();
            if (shortNames.contains(shortName)) {
                throw new IllegalArgumentException(String.format("Duplicated algorithm shortName in experiment %s. FIX: All algorithm getShortName() should be unique per experiment → %s", experimentName, shortName));
            }
            shortNames.add(shortName);
        }
    }
}
