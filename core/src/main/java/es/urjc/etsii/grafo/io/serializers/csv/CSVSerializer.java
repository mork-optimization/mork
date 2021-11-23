package es.urjc.etsii.grafo.io.serializers.csv;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import es.urjc.etsii.grafo.io.serializers.ResultsSerializer;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

/**
 * CSV serializer. By changing the separator in the configuration from ',' to '\t', can serialize to other formats such as TSV.
 */
public class CSVSerializer extends ResultsSerializer {

    private static final Logger log = Logger.getLogger(CSVSerializer.class.getName());

    private final CSVSerializerConfig config;

    private final CsvMapper csvMapper;

    /**
     * Create a new CSV serializer with a given configuration.
     *
     * @param config CSV Serializer configuration
     */
    public CSVSerializer(CSVSerializerConfig config) {
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
    public void _serializeResults(List<? extends SolutionGeneratedEvent<?,?>> results, Path p) {
        log.info("Exporting result data to CSV...");

        var schema = csvMapper.schemaFor(SolutionGeneratedEvent.class)
                .withColumnSeparator(config.getSeparator())
                .sortedBy("instanceName", "algorithmName", "iteration")
                .withHeader();
        try(var br = Files.newBufferedWriter(p)){
           var writer =  csvMapper.writer(schema);
           writer.writeValue(br, results);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    /**
//     * It makes no sense to serialize an instance or a solution to a CSV results file, ignore them
//     * @return Fields that should not be serialized to CSV (solutions and instances)
//     */
//    private FilterProvider getFilters(){
//        SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.serializeAllExcept("solutions", "bestSolution", "instance");
//        return new SimpleFilterProvider().addFilter("Solution/Instance filter in CSV", filter);
//    }
}
