package es.urjc.etsii.grafo.io;

import es.urjc.etsii.grafo.solver.services.InheritedComponent;

import java.io.File;

@InheritedComponent
public abstract class InstanceImporter<T extends Instance> {

    /**
     * Create an instance from the format used by the problem.
     * After this method finishes executing, the Instance object MUST BE IMMUTABLE.
     * @param f File from where we will load the data
     * @return The instance object that represents this object
     */
    public abstract T importInstance(File f);
}
