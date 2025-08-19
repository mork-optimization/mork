package es.urjc.etsii.grafo.BMSSC.model;

import es.urjc.etsii.grafo.exception.InstanceImportException;
import es.urjc.etsii.grafo.io.InstanceImporter;
import es.urjc.etsii.grafo.util.CollectionUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BMSSCInstanceImporter extends InstanceImporter<BMSSCInstance> {

    private static final String SEPARATOR = ",";

    @Override
    public BMSSCInstance importInstance(BufferedReader reader, String filename) throws IOException {
        // Create and return instance object from file data
        String[] header = reader.readLine().split(SEPARATOR);
        if(header.length != 3){
            throw new InstanceImportException("Invalid instance header, expected 3 fields, got: " + header.length);
        }
        int n = Integer.parseInt(header[0]);
        int d = Integer.parseInt(header[1]);
        int k = Integer.parseInt(header[2]);

        double[][] pointData = new double[n][d];
        for (int i = 0; i < n; i++) {
            String[] lineData = reader.readLine().split(SEPARATOR);
            if(lineData.length != d){
                throw new InstanceImportException("Error at line %s, expected %s dimensions, got %s".formatted(i, d, lineData.length));
            }
            for (int j = 0; j < lineData.length; j++) {
                pointData[i][j] = Double.parseDouble(lineData[j]);
            }
        }

        var instance = new BMSSCInstance(filename, n, d, k,pointData);

        calculateInstanceProperties(instance);
        return instance;
    }

    private void calculateInstanceProperties(BMSSCInstance instance) {
        instance.setProperty("n", instance.n);
        instance.setProperty("k", instance.k);
        instance.setProperty("d", instance.d);
        instance.setProperty("unbalancedK", instance.n % instance.k);

        List<Double> t_distances = new ArrayList<>(instance.n * instance.n);
        for (int i = 0; i < instance.distances.length; i++) {
            for (int j = 0; j < instance.distances.length; j++) {
                if(i != j){
                    t_distances.add(instance.distances[i][j]);
                }
            }
        }
        var distances = CollectionUtil.toDoubleArray(t_distances);
        var distanceStats = calculateStats(distances);
        instance.setProperty("distance_min", distanceStats.min());
        instance.setProperty("distance_max", distanceStats.max());
        instance.setProperty("distance_avg", distanceStats.avg());
        instance.setProperty("distance_std", distanceStats.std());
    }


    private DoubleStats calculateStats(double[] data){
        double min = Integer.MAX_VALUE;
        double max = Integer.MIN_VALUE;
        double sum = 0;

        for (double n : data) {
            if (n < min) min = n;
            if (n > max) max = n;
            sum += n;
        }

        double avg = sum / data.length;
        double std = 0;
        for(double n: data){
            std += Math.pow(n - avg, 2);
        }
        std = Math.sqrt(std / data.length);
        return new DoubleStats(min, max, sum, avg, std);
    }
    private record DoubleStats(double min, double max, double sum, double avg, double std) {}

}
