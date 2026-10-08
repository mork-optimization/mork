package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.results.ResultStore;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.ExceptionUtil;
import es.urjc.etsii.grafo.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;

/**
 * Synchronously retrieves results and exports them to disk when requested by
 * the execution workflow.
 *
 * <p>Serialization deliberately is not event driven: callers must finish the
 * applicable serialization before publishing the corresponding ending event.</p>
 */
@Component
public class ResultsSerializerListener<S extends Solution<S,I>, I extends Instance> {
    private static final Logger logger = LoggerFactory.getLogger(ResultsSerializerListener.class);
    private static final String TEMP_SUFFIX = ".tmp";

    /**
     * Access to stored experiment results
     */
    private final ResultStore<S,I> resultStore;

    /**
     * List of declared result serializers
     */
    private final List<ResultsSerializer<S,I>> serializers;

    /**
     * Construct a result serializer listener.
     * @param resultStore result storage service
     * @param serializers List of available Result Serializers
     */
    public ResultsSerializerListener(ResultStore<S,I> resultStore, List<ResultsSerializer<S,I>> serializers) {
        this.resultStore = resultStore;
        this.serializers = List.copyOf(serializers);
    }

    /**
     * Serialize the current experiment results with serializers configured to
     * run after each instance.
     *
     * @param expName experiment name, as defined in the configuration
     * @param expStart experiment start time, as UNIX timestamp
     */
    public void serializePerInstance(String expName, long expStart) {
        serialize(expName, expStart, ResultExportFrequency.PER_INSTANCE);
    }

    /**
     * Serialize the final experiment results with serializers configured to
     * run after the experiment.
     *
     * @param expName experiment name, as defined in the configuration
     * @param expStart experiment start time, as UNIX timestamp
     */
    public void serializeAtExperimentEnd(String expName, long expStart) {
        try {
            serialize(expName, expStart, ResultExportFrequency.EXPERIMENT_END);
        } finally {
            resultStore.releaseSolutionsForExperiment(expName);
        }
    }

    private void serialize(String expName, long expStart, ResultExportFrequency frequency) {
        var data = getExpData(expName);
        if (data.isEmpty()) {
            logger.warn("Cannot save empty list of results, skipping result serialization.");
            return;
        }

        SerializerExecutionException firstFailure = null;
        for(var serializer: serializers){
            Path realFile = null;
            Path tempFile = null;
            try {
                var config = serializer.getConfig();
                if (!config.isEnabled() || config.getFrequency() != frequency) {
                    continue;
                }
                IOUtil.createFolder(config.getFolder());
                String filename = getFilename(config, expName, expStart);
                realFile = Path.of(config.getFolder(), filename);
                tempFile = Path.of(config.getFolder(), filename + TEMP_SUFFIX);
                serializer.serializeResults(expName, data, tempFile);
                Files.move(tempFile, realFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException | RuntimeException e) {
                var failure = buildSerializationException(
                        serializer,
                        expName,
                        frequency,
                        data.size(),
                        tempFile,
                        realFile,
                        "preparing or executing serializer",
                        e
                );
                if (firstFailure == null) {
                    firstFailure = failure;
                } else {
                    firstFailure.addSuppressed(failure);
                }
            }
        }
        if (firstFailure != null) {
            throw firstFailure;
        }
    }

    private SerializerExecutionException buildSerializationException(
            ResultsSerializer<S, I> serializer,
            String expName,
            ResultExportFrequency frequency,
            int resultCount,
            Path tempFile,
            Path realFile,
            String operation,
            Exception cause
    ) {
        Throwable rootCause = ExceptionUtil.getRootCause(cause);
        return new SerializerExecutionException(String.format(
                "Result serializer '%s' failed while %s for experiment '%s' with frequency '%s'. Result count: %s. Temp file: %s. Final file: %s. Root cause: %s: %s",
                serializerName(serializer),
                operation,
                expName,
                frequency,
                resultCount,
                resolvedPath(tempFile),
                resolvedPath(realFile),
                rootCause.getClass().getSimpleName(),
                rootCause.getMessage()
        ), cause);
    }

    private String resolvedPath(Path path) {
        return path == null ? "<not resolved>" : path.toAbsolutePath().toString();
    }

    private String serializerName(ResultsSerializer<S, I> serializer) {
        String simpleName = serializer.getClass().getSimpleName();
        return simpleName.isBlank() ? serializer.getClass().getName() : simpleName;
    }

    private List<WorkUnitResult<S,I>> getExpData(String expName){
        return resultStore.getResultsForExperiment(expName);
    }

    /**
     * Get filename
     * @param experimentName experiment name
     * @return the file name
     */
    public String getFilename(AbstractSerializerConfig config, String experimentName, long startTimestamp) {
        return experimentName + new SimpleDateFormat(config.getFormat()).format(Date.from(Instant.ofEpochMilli(startTimestamp)));
    }
}
