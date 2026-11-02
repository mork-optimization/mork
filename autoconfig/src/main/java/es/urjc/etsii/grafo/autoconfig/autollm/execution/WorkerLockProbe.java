package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import java.nio.channels.FileChannel;
import java.nio.file.*;

/** Dependency-free subprocess used to verify host filesystem lock exclusion. */
public final class WorkerLockProbe {
    private WorkerLockProbe() {}
    public static void main(String[] args) throws Exception {
        try (var channel = FileChannel.open(Path.of(args[0]), StandardOpenOption.WRITE);
             var lock = channel.tryLock()) {
            System.exit(lock == null ? 0 : 1);
        }
    }
}
