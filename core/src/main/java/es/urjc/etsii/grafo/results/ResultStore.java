package es.urjc.etsii.grafo.results;

import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import org.springframework.stereotype.Service;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * In-memory generated-result store.
 *
 * <p>Solutions are retained strongly until explicitly released after final
 * serialization. Released solutions remain available on a best-effort basis
 * through a {@link SoftReference}.</p>
 *
 * @param <S> solution type
 * @param <I> instance type
 */
@Service
public class ResultStore<S extends Solution<S, I>, I extends Instance> {
    private final LinkedHashMap<UUID, StoredResult<S, I>> results = new LinkedHashMap<>();

    /**
     * Store a work-unit result.
     *
     * @param result work-unit result
     * @return stored work-unit result
     */
    public synchronized WorkUnitResult<S, I> store(WorkUnitResult<S, I> result) {
        Objects.requireNonNull(result, "result cannot be null");
        if (results.containsKey(result.resultId())) {
            throw new IllegalStateException("Duplicate result id " + result.resultId());
        }
        results.put(result.resultId(), new StoredResult<>(result));
        return result;
    }

    /**
     * Find the stored result metadata by id.
     *
     * <p>After release, the returned result no longer directly contains the
     * solution. Use {@link #findSolution(UUID)} when the solution is needed.</p>
     */
    public synchronized Optional<WorkUnitResult<S, I>> findResult(UUID resultId) {
        var stored = results.get(resultId);
        return stored == null ? Optional.empty() : Optional.of(stored.result());
    }

    /**
     * Find a solution by result id.
     *
     * <p>After release this lookup is best-effort and may be empty if memory
     * pressure has cleared the soft reference.</p>
     */
    public synchronized Optional<S> findSolution(UUID resultId) {
        var stored = results.get(resultId);
        return stored == null ? Optional.empty() : stored.solution();
    }

    /**
     * Get an immutable insertion-ordered snapshot for an experiment.
     */
    public synchronized List<WorkUnitResult<S, I>> getResultsForExperiment(String experimentName) {
        return results.values().stream()
                .map(StoredResult::result)
                .filter(result -> Objects.equals(result.experimentName(), experimentName))
                .toList();
    }

    /**
     * Release all strong solution references belonging to an experiment.
     * Cached objective values and custom properties remain in the stored result.
     */
    public synchronized void releaseSolutionsForExperiment(String experimentName) {
        results.values().stream()
                .filter(stored -> Objects.equals(stored.result().experimentName(), experimentName))
                .forEach(StoredResult::releaseSolution);
    }

    private static final class StoredResult<S extends Solution<S, I>, I extends Instance> {
        private WorkUnitResult<S, I> result;
        private SoftReference<S> releasedSolution;

        private StoredResult(WorkUnitResult<S, I> result) {
            this.result = result;
        }

        private WorkUnitResult<S, I> result() {
            return result;
        }

        private Optional<S> solution() {
            if (result.solution() != null) {
                return Optional.of(result.solution());
            }
            return releasedSolution == null ? Optional.empty() : Optional.ofNullable(releasedSolution.get());
        }

        private void releaseSolution() {
            var solution = result.solution();
            if (solution == null) {
                return;
            }
            releasedSolution = new SoftReference<>(solution);
            result = result.withoutSolution();
        }
    }
}
