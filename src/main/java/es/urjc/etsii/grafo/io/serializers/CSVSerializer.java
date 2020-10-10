package es.urjc.etsii.grafo.io.serializers;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import es.urjc.etsii.grafo.io.Result;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CSVSerializer extends ResultsSerializer{

    @Value("${serializers.csv.separator}")
    private char csvSeparator;

    private final CsvMapper csvMapper;

    public CSVSerializer(
            @Value("${serializers.csv.enabled}") boolean enabled,
            @Value("${serializers.csv.folder}") String folder,
            @Value("${serializers.csv.format}") String format
    ) {
        super(enabled, folder, format);
        this.csvMapper = new CsvMapper();
        csvMapper.setVisibility(csvMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    }

    public void _serializeResults(List<Result> s, Path p) {
        log.info("Exporting result data to CSV...");

        var schema = csvMapper.schemaFor(s.get(0).getClass())
                .withColumnSeparator(csvSeparator)
                .sortedBy("instanceName", "algorithmName")
                .withHeader();
        try(var br = Files.newBufferedWriter(p)){
           var writer =  csvMapper.writer(schema);
           writer.writeValue(br, s);
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
