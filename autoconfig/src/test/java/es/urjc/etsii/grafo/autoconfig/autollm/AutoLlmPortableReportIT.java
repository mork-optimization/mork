package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

/** Produces the actual renderer output consumed by the offline browser/print smoke test. */
class AutoLlmPortableReportIT {
    @TempDir Path root;
    @Test void exportsARepresentativePortableReport() throws Exception {
        var campaign=AutoLlmReportFixture.chartCampaign(root);
        var receipt=AutoLlmHtmlReports.generate(campaign,root,AutoLlmHtmlReports.queued(campaign,root,null),null);
        assertEquals("SUCCEEDED",receipt.state(),receipt.failure());
        Path output=Path.of("target/portable-report-fixture.html");
        Files.copy(root.resolve(receipt.path()),output,StandardCopyOption.REPLACE_EXISTING);
        assertTrue(Files.size(output)>10000);
    }
}
