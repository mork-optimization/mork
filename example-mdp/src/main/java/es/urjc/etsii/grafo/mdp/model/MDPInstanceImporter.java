package es.urjc.etsii.grafo.mdp.model;

import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Reads the MDP file format: header {@code numNodes numSolutionNodes}, then one
 * {@code col row weight} line per pair (lower-triangular). Reused unchanged from
 * {@code mork-experiments}.
 */
public class MDPInstanceImporter extends InstanceImporter<MDPInstance> {

    @Override
    public MDPInstance importInstance(BufferedReader reader, String suggestedName) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("The file is empty");
        }

        StringTokenizer header = new StringTokenizer(line);
        int numberOfNodes = Integer.parseInt(header.nextToken());
        int numSolutionNodes = Integer.parseInt(header.nextToken());

        double[][] weights = new double[numberOfNodes - 1][];
        for (int i = 0; i < numberOfNodes - 1; i++) {
            weights[i] = new double[i + 1];
        }

        line = reader.readLine();
        while (line != null) {
            StringTokenizer st = new StringTokenizer(line);
            int colNumber = Integer.parseInt(st.nextToken());
            int rowNumber = Integer.parseInt(st.nextToken());
            double weight = Double.parseDouble(st.nextToken());
            weights[rowNumber - 1][colNumber] = weight;
            line = reader.readLine();
        }

        return new MDPInstance(suggestedName, numSolutionNodes, weights);
    }
}
