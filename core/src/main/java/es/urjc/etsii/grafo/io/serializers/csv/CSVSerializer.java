package es.urjc.etsii.grafo.io.serializers.csv;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import es.urjc.etsii.grafo.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.serializers.ResultsSerializer;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * CSV serializer. By changing the separator in the configuration from ',' to '\t', can serialize to other formats such as TSV.
 */
public class CSVSerializer<S extends Solution<S, I>, I extends Instance> extends ResultsSerializer<S, I> {
    private static final Logger log = LoggerFactory.getLogger(CSVSerializer.class);

    private final CSVConfig config;

    private final CsvMapper csvMapper;

    /**
     * Create a new CSV serializer with a given configuration.
     *
     * @param config CSV Serializer configuration
     */
    public CSVSerializer(CSVConfig config, List<ReferenceResultProvider> referenceResultProviders) {
        super(config, referenceResultProviders);
        this.config = config;
        this.csvMapper = new CsvMapper();
        csvMapper.setVisibility(csvMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    }

    /**
     * {@inheritDoc}
     */
    public void _serializeResults(String experimentName, List<SolutionGeneratedEvent<S, I>> results, Path p) {
        log.debug("Exporting result data to CSV...");
        var mainObjName = Context.getMainObjective().getName();

        var instaceNames = new HashSet<String>();
        var data = new ArrayList<CSVRow>(results.size());
        for (var event : results) {
            double score = event.getObjectives().getOrDefault(mainObjName, Double.NaN);
            data.add(new CSVRow(event.getInstanceName(), event.getAlgorithmName(), String.valueOf(event.getIteration()), score, event.getExecutionTime(), event.getTimeToBest()));
            instaceNames.add(event.getInstanceName());
        }

        // Add reference results if available
        for (var referenceProvider : referenceResultProviders) {
            for (var instanceName : instaceNames) {
                var providerName = referenceProvider.getProviderName();
                var refResult = referenceProvider.getValueFor(instanceName);
                double refScore = refResult.getScores().getOrDefault(mainObjName, Double.NaN);
                if (Double.isFinite(refScore)) {
                    data.add(new CSVRow(instanceName, providerName, "0", refScore, refResult.getTimeInNanos(), refResult.getTimeToBestInNanos()));
                }
            }
        }

        try (var br = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
            var writer = csvMapper.writer(getSchema());
            writer.writeValue(br, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CsvSchema getSchema() {
        return csvMapper
                .schemaFor(CSVRow.class)
                .withColumnSeparator(config.getSeparator())
                .sortedBy("instanceName", "algorithmName", "iteration")
                .withHeader();
    }

    private record CSVRow(String instanceName, String algorithmName, String iteration, double score, long time, long ttb) {}

}
