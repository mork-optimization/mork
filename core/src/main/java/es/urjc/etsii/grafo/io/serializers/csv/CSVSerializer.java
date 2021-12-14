package es.urjc.etsii.grafo.io.serializers.csv;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import es.urjc.etsii.grafo.io.serializers.ResultsSerializer;
import es.urjc.etsii.grafo.solver.services.events.AbstractEventStorage;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

/**
 * CSV serializer. By changing the separator in the configuration from ',' to '\t', can serialize to other formats such as TSV.
 */
public class CSVSerializer extends ResultsSerializer {

    private static final Logger log = Logger.getLogger(CSVSerializer.class.getName());

    private final CSVConfig config;

    private final CsvMapper csvMapper;

    /**
     * Create a new CSV serializer with a given configuration.
     *
     * @param config CSV Serializer configuration
     */
    public CSVSerializer(AbstractEventStorage eventStorage, CSVConfig config) {
        super(eventStorage, config);
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

        // Problema: Reusar el path si existe, refactorizar el serializer comun, metodo helper a IOManager o donde sea, oo como ejemplo
        try(var br = Files.newBufferedWriter(p, StandardCharsets.UTF_8)){
           var writer =  csvMapper.writer(schema);
           writer.writeValue(br, results);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
