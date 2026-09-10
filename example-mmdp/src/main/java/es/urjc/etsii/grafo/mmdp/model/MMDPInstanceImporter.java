package es.urjc.etsii.grafo.mmdp.model;

import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Reads the MMDP file format: header {@code numNodes numSolutionNodes}, then one
 * {@code i j weight} line per pair (0-based). Mirrors the parser of the {@code jmh} project.
 */
public class MMDPInstanceImporter extends InstanceImporter<MMDPInstance> {

    @Override
    public MMDPInstance importInstance(BufferedReader reader, String suggestedName) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("The file is empty");
        }

        StringTokenizer header = new StringTokenizer(line);
        int numNodes = Integer.parseInt(header.nextToken());
        int numSolutionNodes = Integer.parseInt(header.nextToken());

        double[][] weights = new double[numNodes][numNodes];

        line = reader.readLine();
        while (line != null) {
            StringTokenizer st = new StringTokenizer(line);
            int i = Integer.parseInt(st.nextToken());
            int j = Integer.parseInt(st.nextToken());
            double weight = Double.parseDouble(st.nextToken());
            weights[i][j] = weight;
            weights[j][i] = weight;
            line = reader.readLine();
        }

        return new MMDPInstance(suggestedName, numNodes, numSolutionNodes, weights);
    }
}
