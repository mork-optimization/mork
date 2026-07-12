package es.urjc.etsii.grafo.results;

import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * In-memory generated-result store.
 *
 * @param <S> solution type
 * @param <I> instance type
 */
@Service
public class ResultStore<S extends Solution<S, I>, I extends Instance> {
    private final List<WorkUnitResult<S, I>> results = new ArrayList<>();
    private final Map<String, WorkUnitResult<S, I>> resultsById = new LinkedHashMap<>();

    /**
     * Store a work-unit result.
     *
     * @param result work-unit result
     * @return stored work-unit result
     */
    public synchronized WorkUnitResult<S, I> store(WorkUnitResult<S, I> result) {
        if (resultsById.containsKey(result.resultId())) {
            throw new IllegalStateException("Duplicate result id " + result.resultId());
        }
        results.add(result);
        resultsById.put(result.resultId(), result);
        return result;
    }

    /**
     * Find a result by id.
     *
     * @param resultId result id
     * @return result if present
     */
    public synchronized Optional<WorkUnitResult<S, I>> findResult(String resultId) {
        return Optional.ofNullable(resultsById.get(resultId));
    }

    /**
     * Get generated results for an experiment.
     *
     * @param experimentName experiment name
     * @return ordered stream of results
     */
    public synchronized Stream<WorkUnitResult<S, I>> getResultsForExperiment(String experimentName) {
        return new ArrayList<>(results).stream()
                .filter(result -> result.experimentName().equals(experimentName));
    }

    /**
     * Count solution references that have not been reclaimed for an experiment.
     *
     * @param experimentName experiment name
     * @return number of available solution references
     */
    public synchronized long solutionsInMemory(String experimentName) {
        return getResultsForExperiment(experimentName)
                .map(WorkUnitResult::getSolution)
                .filter(Optional::isPresent)
                .count();
    }
}
