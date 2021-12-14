package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;
import es.urjc.etsii.grafo.solver.services.events.AbstractEventStorage;
import es.urjc.etsii.grafo.solver.services.events.MorkEventListener;
import es.urjc.etsii.grafo.solver.services.events.types.ExperimentEndedEvent;
import es.urjc.etsii.grafo.solver.services.events.types.InstanceProcessingEndedEvent;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.util.IOUtil;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * This class handles the transformation of the results of the experiments to a file in a specific format.
 */
@InheritedComponent
public abstract class ResultsSerializer {
    private static final Logger logger = Logger.getLogger(ResultsSerializer.class.getName());

    /**
     * Access to historic event stream
     */
    protected final AbstractEventStorage eventStorage;

    /**
     * Serializer config
     */
    protected final AbstractResultSerializerConfig config;

    /**
     * Construct a result serializer given a specific configuration. {@see AbstractResultSerializerConfig.java}
     * @param eventStorage
     * @param config serializer configuration
     */
    public ResultsSerializer(AbstractEventStorage eventStorage, AbstractResultSerializerConfig config) {
        this.eventStorage = eventStorage;
        this.config = config;
    }

    /**
     * <p>Save results when experiment ends.</p>
     *
     * @param event a {@link es.urjc.etsii.grafo.solver.services.events.types.ExperimentEndedEvent} object.
     */
    @MorkEventListener
    public void saveOnExperimentEnd(ExperimentEndedEvent event){
        if (!config.isEnabled() || config.getFrequency() != AbstractResultSerializerConfig.Frequency.EXPERIMENT_END) {
            return;
        }

        String expName = event.getExperimentName();
        long startTimestamp = event.getExperimentStartTime();
        doSerialize(expName, startTimestamp);

    }

    /**
     * Save results each time an instance processing ends
     *
     * @param event a {@link es.urjc.etsii.grafo.solver.services.events.types.ExperimentEndedEvent} object.
     */
    @MorkEventListener
    public void saveOnInstanceEnd(InstanceProcessingEndedEvent event){
        if (!config.isEnabled() || config.getFrequency() != AbstractResultSerializerConfig.Frequency.PER_INSTANCE) {
            return;
        }
        String expName = event.getExperimentName();
        long startTimestamp = event.getExperimentStartTime();
        doSerialize(expName, startTimestamp);
    }

    public void doSerialize(String expName, long startTimestamp){

        long solutionsInMemory = this.eventStorage.solutionsInMemory(expName);
        var expData = eventStorage.getGeneratedSolEventForExp(expName).collect(Collectors.toList());
        logger.info(String.format("Solutions in memory at finish: %s of %s", solutionsInMemory, expData.size()));

        if (expData.isEmpty()) {
            logger.warning("Cannot save empty list of results, skipping result serialization.");
            return;
        }

        IOUtil.createFolder(config.getFolder());
        Path p = Path.of(config.getFolder(), getFilename(expName, startTimestamp));
        _serializeResults(expData, p);
    }

    /**
     * This procedure serialize the list of results to a specific format and generate the resultant file in a given path
     *
     * @param results list of results
     * @param p       path
     */
    protected abstract void _serializeResults(List<? extends SolutionGeneratedEvent<?, ?>> results, Path p);

    /**
     * Get filename
     * @param experimentName experiment name
     * @return the file name
     */
    private String getFilename(String experimentName, long startTimestamp) {
        return experimentName + new SimpleDateFormat(config.getFormat()).format(new Date(startTimestamp));
    }
}
