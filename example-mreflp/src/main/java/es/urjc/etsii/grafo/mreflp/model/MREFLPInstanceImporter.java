package es.urjc.etsii.grafo.mreflp.model;

import es.urjc.etsii.grafo.io.InstanceImporter;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;

/** A case descriptor contains a module-relative source path and capacity, on separate lines. */
public class MREFLPInstanceImporter extends InstanceImporter<MREFLPInstance> {
    @Override public MREFLPInstance importInstance(BufferedReader reader, String suggestedInstanceId) throws IOException {
        String source = reader.readLine();
        String capacity = reader.readLine();
        if (source == null || capacity == null || reader.readLine() != null) throw new IllegalArgumentException("Invalid case descriptor");
        var instance = MREFLPInstanceUtil.read(Path.of(source), Integer.parseInt(capacity));
        if (!suggestedInstanceId.equals(instance.getId() + ".case")) throw new IllegalArgumentException("Case ID does not match source");
        return instance;
    }
}
