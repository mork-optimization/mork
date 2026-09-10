package es.urjc.etsii.grafo.cph.model;

import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Reads the CPH file format: header {@code numNodes numHubs hubCapacity}, then one
 * {@code node x y demand} line per node. Mirrors the parser of the {@code jmh} project.
 */
public class CPHInstanceImporter extends InstanceImporter<CPHInstance> {

    @Override
    public CPHInstance importInstance(BufferedReader reader, String suggestedName) throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("The file is empty");
        }

        StringTokenizer header = new StringTokenizer(line);
        int numNodes = Integer.parseInt(header.nextToken());
        int numHubs = Integer.parseInt(header.nextToken());
        int hubCapacity = Integer.parseInt(header.nextToken());

        int[][] nodeData = new int[numNodes][3];

        line = reader.readLine();
        while (line != null && !line.isBlank()) {
            StringTokenizer st = new StringTokenizer(line);
            int node = Integer.parseInt(st.nextToken());
            nodeData[node][0] = Integer.parseInt(st.nextToken());
            nodeData[node][1] = Integer.parseInt(st.nextToken());
            nodeData[node][2] = Integer.parseInt(st.nextToken());
            line = reader.readLine();
        }

        return new CPHInstance(suggestedName, numNodes, numHubs, hubCapacity, nodeData);
    }
}
