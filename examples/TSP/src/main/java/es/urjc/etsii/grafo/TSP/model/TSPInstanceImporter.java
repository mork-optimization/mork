package es.urjc.etsii.grafo.TSP.model;

import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Locale;
import java.util.Scanner;

public class TSPInstanceImporter extends InstanceImporter<TSPInstance> {

    @Override
    public TSPInstance importInstance(BufferedReader reader, String filename) throws IOException {
        Scanner sc = new Scanner(reader).useLocale(Locale.US);
        String name = sc.nextLine().split(":")[1].trim();
        String type = sc.nextLine().split(":")[1];
        String comment = sc.nextLine().split(":")[1];
        int dimension = Integer.parseInt(sc.nextLine().split(":")[1].trim());
        String edgeWeightType = sc.nextLine().split(":")[1];
        String nodeCoordSection = sc.nextLine();
        TSPInstance.Coordinate[] locations = new TSPInstance.Coordinate[dimension];
        while (!sc.hasNext("EOF")) {
            int id = sc.nextInt() - 1;
            double x = sc.nextDouble();
            double y = sc.nextDouble();
            locations[id] = new TSPInstance.Coordinate(x, y);
        }
        double[][] distances = getMatrixOfDistances(locations);
        return new TSPInstance(name, locations, distances);
    }


    /**
     * Calculate all euclidean distances between all locations
     *
     * @param locations list of locations
     * @return a matrix of distances
     */
    private double[][] getMatrixOfDistances(TSPInstance.Coordinate[] locations) {
        var dimension = locations.length;
        double[][] distances = new double[dimension][dimension];
        for (int i = 0; i < dimension; i++) {
            for (int j = i+1; j < dimension; j++) {
                var distance = this.calculateEuclideanDistance(locations[i], locations[j]);
                distances[i][j] = distance;
                distances[j][i] = distance;
            }
        }
        return distances;
    }


    /**
     * Calculate the Euclidian distance of two given coordinates
     *
     * @param i first coordinate
     * @param j second coordinate
     * @return the euclidean distance between two coordiantes
     */
    public double calculateEuclideanDistance(TSPInstance.Coordinate i, TSPInstance.Coordinate j) {
        var di = i.x() - j.x();
        var dj = i.y() - j.y();
        return Math.sqrt((di * di) + (dj * dj));
    }
}
