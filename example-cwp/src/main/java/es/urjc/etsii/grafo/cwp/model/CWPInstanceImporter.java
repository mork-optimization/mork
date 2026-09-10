package es.urjc.etsii.grafo.cwp.model;

import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Reads the CWP file format: header {@code numVertices numVertices numEdges} (the vertex count is
 * duplicated), then one {@code i j} line per edge. Mirrors the parser of the {@code jmh}
 * project.
 */
public class CWPInstanceImporter extends InstanceImporter<CWPInstance> {

    @Override
    public CWPInstance importInstance(BufferedReader reader, String suggestedName) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("The file is empty");
        }

        StringTokenizer header = new StringTokenizer(line);
        header.nextToken();
        int numVertices = Integer.parseInt(header.nextToken());
        int numEdges = Integer.parseInt(header.nextToken());

        int[][] weights = new int[numVertices][numVertices];

        line = reader.readLine();
        while (line != null) {
            StringTokenizer st = new StringTokenizer(line);
            int i = Integer.parseInt(st.nextToken());
            int j = Integer.parseInt(st.nextToken());
            weights[i][j] = 1;
            weights[j][i] = 1;
            line = reader.readLine();
        }

        return new CWPInstance(suggestedName, numVertices, numEdges, weights);
    }
}
