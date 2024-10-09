package es.urjc.etsii.grafo.__RNAME__.model;

import es.urjc.etsii.grafo.io.InstanceImporter;

import java.io.BufferedReader;
import java.io.IOException;

public class __RNAME__InstanceImporter extends InstanceImporter<__RNAME__Instance> {

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
    public __RNAME__Instance importInstance(BufferedReader reader, String suggestedName) throws IOException {
        // Create and return instance object from file data
        // TODO parse all data from the given reader however I want

        // TIP: You may use a Scanner if you prefer it to a Buffered Reader:
        // Scanner sc = new Scanner(reader);
        // You do no need to handle or catch IOExceptions, they are handled by the framework

        // Call instance constructor when we have parsed all the data
        var instance = new __RNAME__Instance(suggestedName);

        // IMPORTANT! Remember that instance data must be immutable from this point
        return instance;
    }
}
