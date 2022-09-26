package es.urjc.etsii.grafo.experiment;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.orchestrator.UserExperimentOrchestrator;
import es.urjc.etsii.grafo.services.ReflectiveSolutionBuilder;
import es.urjc.etsii.grafo.solution.Solution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Manages and configures all experiments to execute
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@Service
public class ExperimentManager<S extends Solution<S, I>, I extends Instance> {

    private static final int MAX_SHORTNAME_LENGTH = 30;
    private Pattern experimentFilter;

    private static final Logger log = LoggerFactory.getLogger(UserExperimentOrchestrator.class);

    /**
     * List of experiments
     */
    private final Map<String, Experiment<S,I>> experiments = new LinkedHashMap<>();

    private final List<ReferenceResultProvider> referenceResultProviders;

    /**
     * Constructor
     *
     * @param experimentImplementations list of experiments
     * @param solverConfig              solver configuration
     * @param solutionBuilders          solution builder
     * @param referenceResultProviders
     */
    public ExperimentManager(List<AbstractExperiment<S, I>> experimentImplementations, SolverConfig solverConfig, List<SolutionBuilder<S, I>> solutionBuilders, List<ReferenceResultProvider> referenceResultProviders) {
        this.referenceResultProviders = validateReferenceResultProviders(referenceResultProviders);
        var experimentPattern = solverConfig.getExperiments();
        experimentFilter = Pattern.compile(experimentPattern);
        var solutionBuilder = UserExperimentOrchestrator.decideImplementation(solutionBuilders, ReflectiveSolutionBuilder.class);
        log.info("Using SolutionBuilder implementation: "+solutionBuilder.getClass().getSimpleName());
        log.info("Using ReferenceResultProviders: " + referenceResultProviders);

        for (var experiment : experimentImplementations) {
            String experimentName = experiment.getName();
            Class<?> experimentClass = experiment.getClass();
            var matcher = experimentFilter.matcher(experimentName);
            if (matcher.matches()) {
                var algorithms = experiment.getAlgorithms();
                fillSolutionBuilder(algorithms, solutionBuilder);
                validateAlgorithmNames(experiment.getName(), algorithms);
                this.experiments.put(experimentName, new Experiment<>(experimentName, experimentClass, algorithms));
                log.debug(String.format("Experiment %s matches against %s", experimentName, experimentPattern));
            } else {
                log.debug(String.format("Experiment %s does not match against %s, ignoring", experimentName, experimentPattern));
            }
        }

        if(this.experiments.isEmpty()){
            if(experimentImplementations.isEmpty()){
                log.error("No experiment definitions found. Experiments are defined by extending AbstractExperiment<S, I>, see the docs for more information.");
            } else {
                log.error("Experiment definitions found, but none passed filters. Verify that 'solver.experiments={}' is correct.", experimentPattern);
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
    public Map<String, Experiment<S,I>> getExperiments() {
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
            if(shortName.length() > MAX_SHORTNAME_LENGTH){
                throw new IllegalArgumentException(String.format("Algorithms shortnames cannot be longer than %s chars. Bad algorithm: %s - %s", MAX_SHORTNAME_LENGTH, shortName, algorithm));
            }
            if (shortNames.contains(shortName)) {
                throw new IllegalArgumentException(String.format("Duplicated algorithm shortName in experiment %s. FIX: All algorithm getShortName() should be unique per experiment → %s", experimentName, shortName));
            }
            shortNames.add(shortName);
        }
    }

    private List<ReferenceResultProvider> validateReferenceResultProviders(List<ReferenceResultProvider> referenceResultProviders){
        Set<String> names = new HashSet<>();
        for(var r: referenceResultProviders){
            String name = r.getProviderName();
            if(names.contains(name)){
                throw new IllegalArgumentException("Duplicated provider name: " + name);
            }
            names.add(name);
        }
        return referenceResultProviders;
    }
}
