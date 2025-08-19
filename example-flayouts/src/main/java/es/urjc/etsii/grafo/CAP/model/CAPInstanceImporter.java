package es.urjc.etsii.grafo.CAP.model;

import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class CAPInstanceImporter extends InstanceImporter<CAPInstance> {

    private static final int N_FAKES = 0;

    @Override
    public CAPInstance importInstance(BufferedReader reader, String filename) throws IOException {

        // Loads the Problem dimensions
        var data = new CAPInstance(filename);
        var sc = new Scanner(reader);

        data.nM = sc.nextInt();
        data.nN = sc.nextInt();

        // Includes fake facilities
        int F = N_FAKES; // Raul: refactor changed to 0
        data.nN += F;

        // Loads the Length vector
        data.L = new int[data.nN];
        for (int j = 0; j < data.nN - F; j++) {
            data.L[j] = sc.nextInt();
            data.L[j] = 4 * data.L[j];
        }
        for (int j = data.nN - F; j < data.nN; j++) data.L[j] = 2;

        // Loads the Costs matrix
        data.W = new int[data.nN][data.nN];
        for (int u = 0; u < data.nN - F; u++) {
            for (int v = 0; v < data.nN - F; v++) {
                data.W[u][v] = sc.nextInt();
                if (v < u) data.W[u][v] = data.W[v][u];
            }
        }

        calculateInstanceProperties(data);

        return data;
    }

    private record IntStats(int min, int max, int sum, double avg, double std) {}
    private IntStats calculateStats(int[] data){
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int sum = 0;

        for (int n : data) {
            if (n < min) min = n;
            if (n > max) max = n;
            sum = Math.addExact(sum, n);
        }

        double avg = (double) sum / data.length;
        double std = 0;
        for(int n: data){
            std += Math.pow(n - avg, 2);
        }
        std = Math.sqrt(std / data.length);
        return new IntStats(min, max, sum, avg, std);
    }

    private void calculateInstanceProperties(CAPInstance data) {

        int[] edgesPerNode = new int[data.nN];
        List<Integer> t_edges = new ArrayList<>();
        for (int i = 0; i < data.W.length; i++) {
            for (int j = 0; j < data.W.length; j++) {
                if(data.W[i][j] > 0){
                    t_edges.add(data.W[i][j]);
                    edgesPerNode[i]++;
                }
            }
        }

        int[] edges = t_edges.stream().mapToInt(Integer::intValue).toArray();


        var degreeStats = calculateStats(edgesPerNode);
        data.setProperty("num_vertices", data.nN);
        data.setProperty("num_edges", edges.length);
        data.setProperty("facility_max_degree", degreeStats.max());
        data.setProperty("facility_min_degree", degreeStats.min());
        data.setProperty("facility_avg_degree", degreeStats.avg());
        data.setProperty("facility_std_degree", degreeStats.std());
        data.setProperty("is_regular", degreeStats.min() == degreeStats.max());

        var nodeStats = calculateStats(data.L);
        data.setProperty("facility_max_size", nodeStats.max());
        data.setProperty("facility_min_size", nodeStats.min());
        data.setProperty("facility_avg_size", nodeStats.avg());
        data.setProperty("facility_std_size", nodeStats.std());
        
        var edgeStats = calculateStats(edges);
        data.setProperty("edge_max_weight", edgeStats.max());
        data.setProperty("edge_min_weight", edgeStats.min());
        data.setProperty("edge_avg_weight", edgeStats.avg());
        data.setProperty("edge_std_weight", edgeStats.std());

        int maxEdges = Math.multiplyExact(data.nN, data.nN - 1);
        data.setProperty("density", (double) edges.length / maxEdges);

        data.setProperty("number_of_rows", data.nM);
    }
}
