package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class AutoLlmStorageReportTest {
    @TempDir Path root;
    @Test void accountsForLogicalBytesWithoutClaimingPhysicalSavings() throws Exception {
        Path asset = root.resolve(".store/blob"); Files.createDirectories(asset.getParent()); Files.write(asset, new byte[123]);
        Path jar = root.resolve("trial/frozen/project.jar"); Files.createDirectories(jar.getParent()); Files.write(jar, new byte[57]);
        var report = AutoLlmStorageReport.inspect(root);
        assertEquals(180, report.logicalBytes()); assertEquals(2, report.files());
        assertEquals(123, report.categories().get("shared-assets").logicalBytes());
        assertEquals(57, report.categories().get("executables").logicalBytes());
        assertNull(report.allocatedBytes()); assertTrue(report.availableBytes() > 0);
        assertEquals(0, report.disappearedFiles());
    }
}
