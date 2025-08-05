package es.urjc.etsii.grafo.MST.model;

import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class MSTInstanceImporter extends InstanceImporter<MSTInstance> {

    /**
     * Load instance from file. This method is called by the framework when a new instance is being loaded.
     * Note that instance load time is never considered in the total execution time.
     * @param reader Input buffer, managed by the framework.
     * @param suggestedName Suggested filename for the instance, can be ignored.
     *                      By default, the suggested filename is built by removing the path and extension info.
     *                      For example, for the path "instances/TSP/TSP-1.txt", the suggestedName would be "TSP-1"
     * @return immutable instance
     * @throws IOException If an error is encountered while the instance is being parsed
     */
    @Override
    public MSTInstance importInstance(BufferedReader reader, String suggestedName) throws IOException {
        var parts = reader.readLine().split("\\s+");
        int v = parseInt(parts[0]), e = parseInt(parts[1]);

        List<Edge>[] graph = new ArrayList[v];
        for (int i = 0; i < v; i++) {
            graph[i] = new ArrayList<>();
        }

        List<Edge> edges = new ArrayList<>(e);
        for (int i = 0; i < e; i++) {
            parts = reader.readLine().split("\\s+");
            if(parts.length != 3){
                throw new IOException("Invalid edge format, expected [from, to, weight], got: " + Arrays.toString(parts));
            }
            int from = parseInt(parts[0]), to = parseInt(parts[1]);
            double weight = parseDouble(parts[2]);
            var edge = new Edge(from, to, weight);
            graph[from].add(edge);
            graph[to].add(edge);
            edges.add(edge);
        }

        // Call instance constructor when we have parsed all the data
        var instance = new MSTInstance(suggestedName, graph, edges);

        // IMPORTANT! Remember that instance data must be immutable from this point
        return instance;
    }
}
