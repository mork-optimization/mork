package es.urjc.etsii.grafo.io;

import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
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
    }

    public void saveResult(List<Result> s, Path p) {
        if(s.isEmpty()){
            throw new IllegalArgumentException("Cannot save empty list of results");
        }

        var schema = csvMapper.schemaFor(s.get(0).getClass())
                .withColumnSeparator(CSV_SEPARATOR)
                .sortedBy("instanceName", "algorythmName")
                .withHeader();
        try(var br = Files.newBufferedWriter(p)){
           var writer =  csvMapper.writer(schema);
           writer.writeValue(br, s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FilterProvider getFilters(){
        SimpleBeanPropertyFilter filter = SimpleBeanPropertyFilter.serializeAllExcept("s", "instance");
        return new SimpleFilterProvider().addFilter("Solution/Instance filter in CSV", filter);
    }
}
