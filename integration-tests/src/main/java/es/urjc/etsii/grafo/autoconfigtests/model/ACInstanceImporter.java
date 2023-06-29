package es.urjc.etsii.grafo.autoconfigtests.model;

import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.io.IOException;

public class ACInstanceImporter extends InstanceImporter<ACInstance> {
    @Override
    public ACInstance importInstance(BufferedReader reader, String filename) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return new ACInstance(filename, sb.toString());
    }
}
