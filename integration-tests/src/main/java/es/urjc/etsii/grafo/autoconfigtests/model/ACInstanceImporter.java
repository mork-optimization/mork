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
        double logar;
        var line1 = reader.readLine();
        var line2 = reader.readLine();
        try {
            total = Integer.parseInt(line1);
            logar = Double.parseDouble(line2);
        } catch (NumberFormatException e) {
            while (line1 != null) {
                total += line1.length();
                line1 = reader.readLine();
            }
            logar = Math.exp(total);
        }
        return new ACInstance(filename, total, logar);
    }
}
