package es.urjc.etsii.grafo.io.serializers.csv;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.serializers.ResultsSerializer;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * CSV serializer. By changing the separator in the configuration from ',' to '\t', can serialize to other formats such as TSV.
 */
public class CSVSerializer<S extends Solution<S,I>, I extends Instance>  extends ResultsSerializer<S,I> {

    private static final Logger log = Logger.getLogger(CSVSerializer.class.getName());

    private final CSVConfig config;

    private final CsvMapper csvMapper;

    /**
     * Create a new CSV serializer with a given configuration.
     *
     * @param config CSV Serializer configuration
     */
    public CSVSerializer(CSVConfig config) {
        super(config);
        this.config = config;
        this.csvMapper = new CsvMapper();
        csvMapper.setVisibility(csvMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    }

    /** {@inheritDoc} */
    public void _serializeResults(List<SolutionGeneratedEvent<S,I>> results, Path p) {
        log.info("Exporting result data to CSV...");

        var data = new ArrayList<CSVRow>(results.size());
        for(var r: results){
            data.add(new CSVRow(r));
        }

        try(var br = Files.newBufferedWriter(p, StandardCharsets.UTF_8)){
           var writer =  csvMapper.writer(getSchema());
           writer.writeValue(br, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CsvSchema getSchema(){
        return csvMapper.schemaFor(CSVRow.class)
                .withColumnSeparator(config.getSeparator())
                .sortedBy("instanceName", "algorithmName", "iteration")
                .withHeader();
    }

    /**
     * Private DTO to map required data to CSV
     * @param instanceName
     * @param algorithmName
     * @param iteration
     * @param score
     * @param time
     * @param ttb
     */
    private record CSVRow(String instanceName, String algorithmName, int iteration, double score, long time, long ttb){
        public CSVRow(SolutionGeneratedEvent<?,?> event){
            this(event.getInstanceName(), event.getAlgorithmName(), event.getIteration(), event.getScore(), event.getExecutionTime(), event.getTimeToBest());
        }
    }

}
