package es.urjc.etsii.grafo.mreflp.experiments;

import es.urjc.etsii.grafo.mreflp.io.PublishedReference;
import es.urjc.etsii.grafo.mreflp.io.WorkbookUtil;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class ReferenceCatalog {
    private final Map<String, PublishedReference> values = new HashMap<>();
    public ReferenceCatalog() throws IOException {
        for (var reference : WorkbookUtil.read()) values.put(reference.caseId() + ":" + reference.method(), reference);
    }
    public PublishedReference get(String caseId, String method) { return values.get(caseId + ":" + method); }
}
