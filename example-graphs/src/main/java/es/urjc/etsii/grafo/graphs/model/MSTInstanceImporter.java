package es.urjc.etsii.grafo.graphs.model;

import es.urjc.etsii.grafo.exception.InstanceImportException;
import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;

public class MSTInstanceImporter extends InstanceImporter<MSTInstance> {

    private static final int UNKNOWN = -1;

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
        String line = reader.readLine();
        if(line == null){
            throw new InstanceImportException("Empty instance file");
        }
        var parts = line.split("\\s+");

        if (suggestedName.endsWith(".gen")){
            int v = parseInt(parts[0]);
            double density = parseDouble(parts[1]);
            int seed = parseInt(parts[2]);
            return generateErdosRenyi(v, density, seed);
        }

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
        var instance = new MSTInstance(suggestedName, graph, edges, UNKNOWN);

        // IMPORTANT! Remember that instance data must be immutable from this point
        return instance;
    }

    /**
     * Generate a Graph using the Erdos-Renyi model.
     * Based on NetworkX generators: <a href="https://github.com/networkx/networkx/blob/e9ecdff26c50cc749d3bc0b2cf3415b79afd74fc/networkx/generators/random_graphs.py">...</a>
     * @return MSTInstance with a random graph generated using the Erdos-Renyi model
     */
    public static MSTInstance generateErdosRenyi(int n, double p, int seed){
        var rnd = new Random(seed);
        List<Edge>[] graph = new ArrayList[n];
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            graph[i] = new ArrayList<>();
        }
        for (int i = 0; i < n; i++) {
            for (int j = i+1; j < n; j++) {
                if(rnd.nextDouble() < p){
                    var edge = new Edge(i, j, rnd.nextDouble()); // random weight in [0,1]
                    graph[i].add(edge);
                    graph[j].add(edge);
                    edges.add(edge);
                }
            }
        }
        // if we have multiple components, add edges to connect them
        boolean[] visited = new boolean[n];
        int nextNode = 0, lastNode = -1;
        while(true){
            bfs(visited, graph, nextNode);
            lastNode = nextNode;
            nextNode = notVisited(visited);
            if(nextNode == -1){
                break;
            } else {
                var edge = new Edge(lastNode, nextNode, rnd.nextDouble());
                graph[lastNode].add(edge);
                graph[nextNode].add(edge);
                edges.add(edge);
            }
        }
        return new MSTInstance("er-%s-%s-%s".formatted(n, p, seed), graph, edges, seed);
    }

    private static int notVisited(boolean[] visited){
        for (int i = 0; i < visited.length; i++) {
            if (!visited[i]) return i;
        }
        return -1;
    }

    private static void bfs(boolean[] visited, List<Edge>[] graph, int startNode) {
        var queue = new LinkedList<Integer>();
        queue.add(startNode);
        visited[startNode] = true;
        while (!queue.isEmpty()) {
            int node = queue.poll();
            for (Edge edge : graph[node]) {
                int nextNode = edge.from() == node ? edge.to() : edge.from();
                if (!visited[nextNode]) {
                    visited[nextNode] = true;
                    queue.add(nextNode);
                }
            }
        }
    }
}
