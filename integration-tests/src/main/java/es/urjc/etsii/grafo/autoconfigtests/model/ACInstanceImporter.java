package es.urjc.etsii.grafo.autoconfigtests.model;

import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Load a test instance used to validate the autoconfig behaviour, instance data consists on several lines with random words
 */
public class ACInstanceImporter extends InstanceImporter<ACInstance> {
    @Override
    public ACInstance importInstance(BufferedReader reader, String filename) throws IOException {
        int total = 0;
        var line = reader.readLine();
        try {
            total = Integer.parseInt(line);
        } catch (NumberFormatException e) {
            while (line != null) {
                total += line.length();
                line = reader.readLine();
            }
        }
        return new ACInstance(filename, total);
    }
}
