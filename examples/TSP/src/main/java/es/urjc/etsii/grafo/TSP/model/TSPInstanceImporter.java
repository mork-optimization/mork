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
        String name = sc.nextLine().trim().split(":")[1];
        String type = sc.nextLine().split(":")[1];
        String comment = sc.nextLine().split(":")[1];
        int dimension = Integer.parseInt(sc.nextLine().split(":")[1].trim());
        String edgeWeightType = sc.nextLine().split(":")[1];
        String displayDataType = sc.nextLine();
        String nodeCoordSection = sc.nextLine();
        TSPInstance.Coordinate[] locations = new TSPInstance.Coordinate[dimension];
        while (!sc.hasNext("EOF")) {
            int id = sc.nextInt() - 1;
            double x = sc.nextDouble();
            double y = sc.nextDouble();
            locations[id] = new TSPInstance.Coordinate(x, y);
        }
        return new TSPInstance(name, locations);
    }
}
