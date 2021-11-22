package es.urjc.etsii.grafo.io;

import es.urjc.etsii.grafo.solver.annotations.InheritedComponent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


/**
 * This class is used to generate an instance for the problem.
 *
 * @param <I> type of the problem instance
 */
@InheritedComponent
public abstract class InstanceImporter<I extends Instance> {

    /**
     * Create an instance from the format used by the problem.
     * After this method finishes executing, the Instance object MUST BE IMMUTABLE.
     *
     * @param f File from where we will load the data
     * @return The instance object that represents this object
     */
    public I importInstance(File f){
        try (var reader = Files.newBufferedReader(f.toPath())) {
            return importInstance(reader, f.getName());
        } catch (IOException e) {
            throw new RuntimeException("Error while loading file: " + f.getAbsolutePath(), e);
        }
    }

    /**
     * Create an instance from the format used by the problem.
     * After this method finishes executing, the Instance object MUST BE IMMUTABLE.
     *
     * @param reader Input buffer, managed by the framework
     * @param filename Filename on disk
     * @throws java.io.IOException If an error is encountered while the instance is being parsed
     * @return The instance object that represents this object
     */
    public abstract I importInstance(BufferedReader reader, String filename) throws IOException;
}
