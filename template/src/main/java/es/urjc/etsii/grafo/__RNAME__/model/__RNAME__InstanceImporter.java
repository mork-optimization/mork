package es.urjc.etsii.grafo.__RNAME__.model;

import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.io.IOException;

public class __RNAME__InstanceImporter extends InstanceImporter<__RNAME__Instance> {

    @Override
    public __RNAME__Instance importInstance(BufferedReader reader, String filename) throws IOException {
        // Create and return instance object from file data


        var instance = new __RNAME__Instance(filename);

        // IMPORTANT! Remember that instance data must be immutable from this point
        return instance;
    }
}
