package es.urjc.etsii.grafo.autoconfig.irace;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class IracePrecisionContractIT {
    @Test void javaPolicyAgreesWithTheInstalledIraceParser() throws Exception {
        Process prerequisite;
        try { prerequisite = new ProcessBuilder("Rscript", "-e", "quit(status=if(requireNamespace('irace',quietly=TRUE)) 0 else 1)").start(); }
        catch (java.io.IOException missing) { assumeTrue(false, "Rscript not installed"); return; }
        assumeTrue(prerequisite.waitFor(20, TimeUnit.SECONDS) && prerequisite.exitValue() == 0, "irace not installed");
        String valid = IraceParameterFileUtil.toFileContents(new IraceParameterSpace(List.of("cooling \"cooling=\" r (0.9, 0.99)"), List.of()));
        String encoded = es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil.JSON.writeValueAsString(valid);
        var process = new ProcessBuilder("Rscript", "-e", "irace::readParameters(text=" + encoded + "); rejected <- tryCatch({irace::readParameters(text=gsub('0.99)', '0.9999)', " + encoded + ", fixed=TRUE)); FALSE}, error=function(e) TRUE); stopifnot(rejected)")
                .redirectErrorStream(true).start();
        assertTrue(process.waitFor(20, TimeUnit.SECONDS));
        assertEquals(0, process.exitValue(), new String(process.getInputStream().readAllBytes()));
        assertNull(IracePrecision.domainError(-0.01, 0.99));
        assertNotNull(IracePrecision.domainError(0.9, 0.9999));
    }
}
