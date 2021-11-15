package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.util.IOUtil;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * This class handles the transformation of the results of the experiments to a file in a specific format.
 */
@InheritedComponent
public abstract class ResultsSerializer {
    protected final Logger log;
    protected final AbstractSerializerConfig config;


    /**
     * Construct a result serializer given a specific configuration. {@see AbstractSerializerConfig.java}
     *
     * @param config serializer configuration
     */
    public ResultsSerializer(AbstractSerializerConfig config) {
        this.config = config;
        log = Logger.getLogger(this.getClass().getName());
    }

    /**
     * Serialize a list of experiment results to a specific format
     *
     * @param experimentName experiment name
     * @param results        list of results
     */
    public void serializeResults(String experimentName, List<? extends SolutionGeneratedEvent<?, ?>> results) {
        if (!config.isEnabled()) {
            return;
        }
        if (results.isEmpty()) {
            log.warning("Cannot save empty list of results, skipping result serialization.");
            return;
        }
        IOUtil.createFolder(config.getFolder());
        Path p = Path.of(config.getFolder(), getFilename(experimentName));
        _serializeResults(results, p);
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
    private String getFilename(String experimentName) {
        return experimentName + new SimpleDateFormat(config.getFormat()).format(new Date());
    }
}
