package es.urjc.etsii.grafo.io;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class CSVSerializer {

    @Value("${csv.separator}")
    private char CSV_SEPARATOR;
    private final CsvMapper csvMapper;

    public CSVSerializer() {
        this.csvMapper = new CsvMapper();
        csvMapper.setVisibility(csvMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    }

    public void saveResult(List<Result> s, Path p) {
        if(s.isEmpty()){
            throw new IllegalArgumentException("Cannot save empty list of results");
        }

        var schema = csvMapper.schemaFor(s.get(0).getClass())
                .withColumnSeparator(CSV_SEPARATOR)
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
