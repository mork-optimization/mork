package es.urjc.etsii.grafo.mreflp.io;

import java.io.IOException;
import java.nio.file.*;

/** Readers see either the previous complete file or the new complete file. */
public final class AtomicFileUtil {
    private AtomicFileUtil() {}
    public static void write(Path target, String content) throws IOException {
        Path folder = target.toAbsolutePath().getParent();
        Files.createDirectories(folder);
        Path temporary = Files.createTempFile(folder, ".mreflp-", ".tmp");
        try {
            Files.writeString(temporary, content);
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
