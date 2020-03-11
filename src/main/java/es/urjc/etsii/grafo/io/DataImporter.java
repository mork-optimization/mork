package es.urjc.etsii.grafo.io;

import org.springframework.stereotype.Component;

import java.io.File;

@Component
public interface DataImporter<T extends Instance> {

    /**
     * Create an instance from the format used by the problem.
     * After this method finishes executing, the Instance object MUST BE IMMUTABLE.
     * @param f File from where we will load the data
     * @return The instance object that represents this object
     */
    T importInstance(File f);
}
