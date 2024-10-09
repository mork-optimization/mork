package es.urjc.etsii.grafo.io;

import es.urjc.etsii.grafo.annotations.InheritedComponent;
import es.urjc.etsii.grafo.exception.InstanceImportException;
import es.urjc.etsii.grafo.util.IOUtil;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


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
     * @param instancePath File path from where we will load the data
     * @return The instance object that represents this object
     */
    public I importInstance(String instancePath){
        try (var reader = new BufferedReader(new InputStreamReader(IOUtil.getInputStream(instancePath)))) {
            var suggestedInstanceId = FilenameUtils.getName(instancePath);
            return importInstance(reader, suggestedInstanceId);
        } catch (Exception e) {
            throw new InstanceImportException("Error while loading file: " + instancePath, e);
        }
    }

    /**
     * Create an instance from the format used by the problem.
     * After this method finishes executing, the Instance object MUST BE IMMUTABLE.
     *
     * @param reader Input buffer, managed by the framework
     * @param suggestedInstanceId Filename on disk
     * @throws IOException If an error is encountered while the instance is being parsed
     * @return The instance object that represents this object
     */
    public abstract I importInstance(BufferedReader reader, String suggestedInstanceId) throws IOException;
}
