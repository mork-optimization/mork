package es.urjc.etsii.grafo.mreflp.model;

import es.urjc.etsii.grafo.io.InstanceImporter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;

/** A case carries its source label, capacity, and complete raw instance data. */
public class MREFLPInstanceImporter extends InstanceImporter<MREFLPInstance> {
    @Override public MREFLPInstance importInstance(BufferedReader reader, String suggestedInstanceId) throws IOException {
        String source = reader.readLine();
        String capacity = reader.readLine();
        if (source == null || capacity == null) throw new IllegalArgumentException("Invalid case descriptor");
        var data = new StringWriter();
        reader.transferTo(data);
        var label = Path.of(source);
        var instance = MREFLPInstanceUtil.read(label.getFileName().toString(), label.getParent().getFileName().toString(),
                Integer.parseInt(capacity), data.toString());
        if (!suggestedInstanceId.equals(instance.getId() + ".case")) throw new IllegalArgumentException("Case ID does not match source");
        return instance;
    }
}
