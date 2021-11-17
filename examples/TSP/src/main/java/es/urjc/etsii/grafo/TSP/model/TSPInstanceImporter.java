package es.urjc.etsii.grafo.TSP.model;

import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.io.IOException;

public class TSPInstanceImporter extends InstanceImporter<TSPInstance> {

    @Override
    public TSPInstance importInstance(BufferedReader reader, String filename) throws IOException {
        // Create and return instance object from file data
        // TODO parse all data from the given reader however I want
        // TIP You may use a Scanner if you prefer it to a Buffered Reader:
        // Scanner sc = new Scanner(reader);

        var instance = new TSPInstance(filename);

        // IMPORTANT! Remember that instance data must be immutable from this point
        return instance;
    }
}
