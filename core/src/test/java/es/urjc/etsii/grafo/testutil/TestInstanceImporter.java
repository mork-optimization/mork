package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.util.Map;
import java.util.stream.Collectors;

public class TestInstanceImporter extends InstanceImporter<TestInstance> {
    @Override
    public TestInstance importInstance(BufferedReader reader, String filename) {
        Map<String, Object> map = reader.lines()
                .map(s -> s.split(","))
                .collect(Collectors.toMap(s -> s[0], s -> tryParse(s[1])));
        return new TestInstance(filename, map);
    }

    private static Object tryParse(String s){
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e){
            return s;
        }
    }
}
