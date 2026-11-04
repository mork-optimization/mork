package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import es.urjc.etsii.grafo.util.ConcurrencyUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import static org.junit.jupiter.api.Assertions.*;

class RemoteProcessOwnershipIT {
    @TempDir Path temp;
    @Test void localReconciliationCannotKillARemotePid() throws Exception {
        Path output = temp.resolve("remote"); Files.createDirectories(output);
        var process = new ProcessBuilder("sleep", "20").start();
        try {
            AutoLlmFileUtil.write(output.resolve("owner.json"), Map.of("pid", process.pid(), "start", process.info().startInstant().orElseThrow().toString(), "executionTarget", "SSH"));
            new AutoLlmProcessRunner().reconcile(temp); assertTrue(process.isAlive());
        } finally { process.destroyForcibly(); }
    }
}
