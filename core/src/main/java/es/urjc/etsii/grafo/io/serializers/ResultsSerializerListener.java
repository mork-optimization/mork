package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.events.MorkEventListener;
import es.urjc.etsii.grafo.events.types.ExperimentEndedEvent;
import es.urjc.etsii.grafo.events.types.InstanceProcessingEndedEvent;
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
 * Retrieve results and export to disk when appropriate
 */
@Component
public class ResultsSerializerListener<S extends Solution<S,I>, I extends Instance> {
    private static final Logger logger = LoggerFactory.getLogger(ResultsSerializerListener.class);
    private static final String TEMP_SUFFIX = ".tmp";

    /**
     * Access to historic event stream
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
        this.serializers = serializers;
    }

    /**
     * Save results using each serializer with the given frequency
     *
     * @param expName experiment name, as defined in the configuration
     * @param expStart experiment start time, as UNIX timestamp
     * @param frequency save frequency, will check each serializer config
     *                  and skip if the frequency configured does not match
     */
    public void saveOnFreq(String expName, long expStart, ResultExportFrequency frequency){
        var data = getExpData(expName);
        if (data.isEmpty()) {
            logger.warn("Cannot save empty list of results, skipping result serialization.");
            return;
        }

        for(var serializer: serializers){
            var config = serializer.getConfig();
            if (config.isEnabled() && config.getFrequency() == frequency) {
                IOUtil.createFolder(config.getFolder());
                String filename = getFilename(config, expName, expStart);
                Path realFile = Path.of(config.getFolder(), filename);
                Path tempFile = Path.of(config.getFolder(), filename + TEMP_SUFFIX);
                try {
                    serializer.serializeResults(expName, data, tempFile);
                    Files.move(tempFile, realFile, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException | RuntimeException e) {
                    throw buildSerializationException(serializer, expName, frequency, data.size(), tempFile, realFile, "executing serializer", e);
                }
            }
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
                tempFile.toAbsolutePath(),
                realFile.toAbsolutePath(),
                rootCause.getClass().getSimpleName(),
                rootCause.getMessage()
        ), cause);
    }

    private String serializerName(ResultsSerializer<S, I> serializer) {
        String simpleName = serializer.getClass().getSimpleName();
        return simpleName.isBlank() ? serializer.getClass().getName() : simpleName;
    }

    /**
     * <p>Save results when experiment ends.</p>
     *
     * @param event a {@link ExperimentEndedEvent} object.
     */
    @MorkEventListener
    public void saveOnExperimentEnd(ExperimentEndedEvent event){
        saveOnFreq(
                event.getExperimentName(),
                event.getExperimentStartTime(),
                ResultExportFrequency.EXPERIMENT_END
        );
    }

    /**
     * Save results each time an instance processing ends
     *
     * @param event a {@link ExperimentEndedEvent} object.
     */
    @MorkEventListener
    public void saveOnInstanceEnd(InstanceProcessingEndedEvent event){
        saveOnFreq(
                event.getExperimentName(),
                event.getExperimentStartTime(),
                ResultExportFrequency.PER_INSTANCE
        );
    }

    public List<WorkUnitResult<S,I>> getExpData(String expName){
        long solutionsInMemory = this.resultStore.solutionsInMemory(expName);
        var expData = resultStore.getResultsForExperiment(expName).toList();
        logger.debug("Solutions in memory: {} of {}", solutionsInMemory, expData.size());
        return expData;
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
